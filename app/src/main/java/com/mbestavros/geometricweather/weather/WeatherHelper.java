package com.mbestavros.geometricweather.weather;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.List;

import io.reactivex.Observable;
import io.reactivex.disposables.CompositeDisposable;
import com.mbestavros.geometricweather.basic.model.location.Location;
import com.mbestavros.geometricweather.basic.model.option.provider.WeatherSource;
import com.mbestavros.geometricweather.basic.model.weather.Weather;
import com.mbestavros.geometricweather.db.DatabaseHelper;
import com.mbestavros.geometricweather.weather.observer.BaseObserver;
import com.mbestavros.geometricweather.weather.observer.ObserverContainer;
import com.mbestavros.geometricweather.weather.service.AccuWeatherService;
import com.mbestavros.geometricweather.weather.service.WeatherService;

/**
 * Weather helper.
 * */

public class WeatherHelper {

    @Nullable private WeatherService weatherService;

    @Nullable private WeatherService[] searchServices;
    private CompositeDisposable compositeDisposable;

    public WeatherHelper() {
        weatherService = null;
        searchServices = null;
        compositeDisposable = new CompositeDisposable();
    }

    @NonNull
    private static WeatherService getWeatherService(WeatherSource source) {
        switch (source) {
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

    public void requestLocation(Context context, String query, @NonNull final OnRequestLocationListener l) {
        searchServices = new WeatherService[] {
                getWeatherService(WeatherSource.ACCU)
        };

        Observable<List<Location>> accu = Observable.create(emitter ->
                emitter.onNext(searchServices[0].requestLocation(context, query)));

        accu.compose(SchedulerTransformer.create())
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
