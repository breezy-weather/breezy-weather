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
import wangdaye.com.geometricweather.common.rxjava.BaseObserver;
import wangdaye.com.geometricweather.common.rxjava.ObserverContainer;
import wangdaye.com.geometricweather.weather.services.WeatherService;
import wangdaye.com.geometricweather.common.rxjava.SchedulerTransformer;

/**
 * Weather helper.
 * */

public class WeatherHelper {

    private final WeatherServiceSet mServiceSet;
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
    public WeatherHelper(WeatherServiceSet weatherServiceSet,
                         CompositeDisposable compositeDisposable) {
        mServiceSet = weatherServiceSet;
        mCompositeDisposable = compositeDisposable;
    }

    public void requestWeather(Context c, Location location, @NonNull final OnRequestWeatherListener l) {
        final WeatherService service = mServiceSet.get(location.getWeatherSource());
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
            services[i] = mServiceSet.get(sourceList.get(i));
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
        for (WeatherService s : mServiceSet.getAll()) {
            s.cancel();
        }
        mCompositeDisposable.clear();
    }
}
