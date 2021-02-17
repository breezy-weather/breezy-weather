package wangdaye.com.geometricweather.weather;

import android.content.Context;
import androidx.annotation.NonNull;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import io.reactivex.Observable;
import io.reactivex.disposables.CompositeDisposable;
import wangdaye.com.geometricweather.common.basic.models.Location;
import wangdaye.com.geometricweather.common.basic.models.options.provider.WeatherSource;
import wangdaye.com.geometricweather.common.basic.models.weather.Weather;
import wangdaye.com.geometricweather.db.DatabaseHelper;
import wangdaye.com.geometricweather.settings.SettingsOptionManager;
import wangdaye.com.geometricweather.weather.observers.BaseObserver;
import wangdaye.com.geometricweather.weather.observers.ObserverContainer;
import wangdaye.com.geometricweather.weather.services.AccuWeatherService;
import wangdaye.com.geometricweather.weather.services.CNWeatherService;
import wangdaye.com.geometricweather.weather.services.CaiYunWeatherService;
import wangdaye.com.geometricweather.weather.services.MfWeatherService;
import wangdaye.com.geometricweather.weather.services.WeatherService;

/**
 * Weather helper.
 * */

public class WeatherHelper {

    private final WeatherService[] mWeatherServices;
    private final CompositeDisposable mCompositeDisposable;

    public interface OnRequestWeatherListener {
        void requestWeatherSuccess(@NonNull Location requestLocation);
        void requestWeatherFailed(@NonNull Location requestLocation);
    }

    public interface OnRequestLocationListener {
        void requestLocationSuccess(String query, List<Location> locationList);
        void requestLocationFailed(String query);
    }

    @Inject
    public WeatherHelper(AccuWeatherService accuWeatherService,
                         CNWeatherService cnWeatherService,
                         CaiYunWeatherService caiYunWeatherService,
                         MfWeatherService mfWeatherService,
                         CompositeDisposable compositeDisposable) {
        mWeatherServices = new WeatherService[] {
                accuWeatherService,
                cnWeatherService,
                caiYunWeatherService,
                mfWeatherService
        };
        mCompositeDisposable = compositeDisposable;
    }

    @NonNull
    public static WeatherService getWeatherService(WeatherService[] services, WeatherSource source) {
        switch (source) {
            case MF:
                for (WeatherService service : services) {
                    if (service instanceof MfWeatherService) {
                        return service;
                    }
                }
                break;

            case CAIYUN:
                for (WeatherService service : services) {
                    if (service instanceof CaiYunWeatherService) {
                        return service;
                    }
                }
                break;

            case CN:
                for (WeatherService service : services) {
                    if (service instanceof CNWeatherService) {
                        return service;
                    }
                }
                break;

            default: // ACCU.
                for (WeatherService service : services) {
                    if (service instanceof AccuWeatherService) {
                        return service;
                    }
                }
                break;
        }
        throw new RuntimeException("Cannot find a valid weather service object.");
    }

    public void requestWeather(Context c, Location location, @NonNull final OnRequestWeatherListener l) {
        final WeatherService service = getWeatherService(mWeatherServices, location.getWeatherSource());
        service.requestWeather(c, location, new WeatherService.RequestWeatherCallback() {

            @Override
            public void requestWeatherSuccess(@NonNull Location requestLocation) {
                Weather weather = requestLocation.getWeather();
                if (weather != null) {
                    DatabaseHelper.getInstance(c).writeWeather(requestLocation, weather);
                    if (weather.getYesterday() == null) {
                        weather.setYesterday(
                                DatabaseHelper.getInstance(c).readHistory(requestLocation, weather));
                    }
                    l.requestWeatherSuccess(requestLocation);
                } else {
                    requestWeatherFailed(requestLocation);
                }
            }

            @Override
            public void requestWeatherFailed(@NonNull Location requestLocation) {
                requestLocation.setWeather(DatabaseHelper.getInstance(c).readWeather(requestLocation));
                l.requestWeatherFailed(requestLocation);
            }
        });
    }

    public void requestLocation(Context context, String query, boolean multiSource,
                                @NonNull final OnRequestLocationListener l) {
        // build weather source list.
        List<WeatherSource> sourceList = new ArrayList<>();
        // default weather source at first index.
        sourceList.add(SettingsOptionManager.getInstance(context).getWeatherSource());
        if (multiSource) {
            // ensure no duplicates.
            for (WeatherSource source : WeatherSource.values()) {
                if (sourceList.get(0) != source) {
                    sourceList.add(source);
                }
            }
        }

        // generate weather services.
        final WeatherService[] services = new WeatherService[sourceList.size()];
        for (int i = 0; i < services.length; i ++) {
            services[i] = getWeatherService(mWeatherServices, sourceList.get(i));
        }

        // generate observable list.
        List<Observable<List<Location>>> observableList = new ArrayList<>();
        for (int i = 0; i < services.length; i ++) {
            int finalI = i;
            observableList.add(
                    Observable.create(emitter ->
                            emitter.onNext(services[finalI].requestLocation(context, query)))
            );
        }

        Observable.zip(observableList, objects -> {
            List<Location> locationList = new ArrayList<>();
            for (Object o : objects) {
                locationList.addAll((List<Location>) o);
            }
            return locationList;
        }).compose(SchedulerTransformer.create())
                .subscribe(new ObserverContainer<>(mCompositeDisposable, new BaseObserver<List<Location>>() {
                    @Override
                    public void onSucceed(List<Location> locationList) {
                        if (locationList != null && locationList.size() != 0) {
                            l.requestLocationSuccess(query, locationList);
                        } else {
                            onFailed();
                        }
                    }

                    @Override
                    public void onFailed() {
                        l.requestLocationFailed(query);
                    }
                }));
    }

    public void cancel() {
        for (WeatherService s : mWeatherServices) {
            s.cancel();
        }
        mCompositeDisposable.clear();
    }
}
