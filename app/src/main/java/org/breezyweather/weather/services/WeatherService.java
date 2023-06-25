package org.breezyweather.weather.services;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.WorkerThread;

import java.util.List;

import org.breezyweather.common.basic.models.Location;
import org.breezyweather.common.basic.models.weather.Weather;
import org.breezyweather.main.utils.RequestErrorType;

/**
 * Weather service.
 * */

public abstract class WeatherService {

    public static class WeatherResultWrapper {
        final Weather result;

        public WeatherResultWrapper(@Nullable Weather weather) {
            result = weather;
        }
    }

    public interface RequestWeatherCallback {
        void requestWeatherSuccess(@NonNull Location requestLocation);
        void requestWeatherFailed(@NonNull Location requestLocation, @NonNull RequestErrorType requestErrorType);
    }

    public interface RequestLocationCallback {
        void requestLocationSuccess(String query, List<Location> locationList);
        void requestLocationFailed(String query, RequestErrorType requestErrorType);
    }

    public abstract Boolean isConfigured(Context context);

    public abstract void requestWeather(Context context, Location location,
                                        @NonNull RequestWeatherCallback callback);

    @WorkerThread
    @NonNull
    public abstract List<Location> requestLocation(Context context, String query);

    public abstract void requestLocation(Context context, Location location,
                                         @NonNull RequestLocationCallback callback);

    public abstract void cancel();
}
