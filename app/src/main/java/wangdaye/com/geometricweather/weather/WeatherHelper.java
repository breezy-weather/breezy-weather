package wangdaye.com.geometricweather.weather;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.Observable;
import io.reactivex.disposables.CompositeDisposable;
import wangdaye.com.geometricweather.basic.models.Location;
import wangdaye.com.geometricweather.basic.models.options.provider.WeatherSource;
import wangdaye.com.geometricweather.basic.models.weather.Weather;
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

    private @Nullable WeatherService weatherService;
    private @Nullable WeatherService[] searchServices;
    private final CompositeDisposable compositeDisposable;

    public WeatherHelper() {
        weatherService = null;
        searchServices = null;
        compositeDisposable = new CompositeDisposable();
    }

    @NonNull
    public static WeatherService getWeatherService(WeatherSource source) {
        switch (source) {
            case MF:
                return new MfWeatherService();

            case CN:
                return new CNWeatherService();

            case CAIYUN:
                return new CaiYunWeatherService();

            default: // ACCU.
                return new AccuWeatherService();
        }
    }

    public void requestWeather(Context c, Location location, @NonNull final OnRequestWeatherListener l) {
        weatherService = getWeatherService(location.getWeatherSource());
        weatherService.requestWeather(c, location, new WeatherService.RequestWeatherCallback() {

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
        searchServices = new WeatherService[sourceList.size()];
        for (int i = 0; i < searchServices.length; i ++) {
            searchServices[i] = getWeatherService(sourceList.get(i));
        }

        // generate observable list.
        List<Observable<List<Location>>> observableList = new ArrayList<>();
        for (int i = 0; i < searchServices.length; i ++) {
            int finalI = i;
            observableList.add(
                    Observable.create(emitter ->
                            emitter.onNext(searchServices[finalI].requestLocation(context, query)))
            );
        }

        Observable.zip(observableList, objects -> {
            List<Location> locationList = new ArrayList<>();
            for (Object o : objects) {
                locationList.addAll((List<Location>) o);
            }
            return locationList;
        }).compose(SchedulerTransformer.create())
                .subscribe(new ObserverContainer<>(compositeDisposable, new BaseObserver<List<Location>>() {
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
        if (weatherService != null) {
            weatherService.cancel();
        }
        if (searchServices != null) {
            for (WeatherService s : searchServices) {
                if (s != null) {
                    s.cancel();
                }
            }
        }
        compositeDisposable.clear();
    }

    // interface.

    public interface OnRequestWeatherListener {
        void requestWeatherSuccess(@NonNull Location requestLocation);
        void requestWeatherFailed(@NonNull Location requestLocation);
    }

    public interface OnRequestLocationListener {
        void requestLocationSuccess(String query, List<Location> locationList);
        void requestLocationFailed(String query);
    }
}
