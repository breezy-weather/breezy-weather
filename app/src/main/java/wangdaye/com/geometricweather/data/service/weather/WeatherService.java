package wangdaye.com.geometricweather.data.service.weather;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Size;

import java.util.List;

import wangdaye.com.geometricweather.basic.TLSCompactService;
import wangdaye.com.geometricweather.data.entity.model.Location;
import wangdaye.com.geometricweather.data.entity.model.weather.Weather;

/**
 * Weather service.
 * */

public abstract class WeatherService extends TLSCompactService {

    public abstract void requestWeather(Context context, Location location,
                                        @NonNull RequestWeatherCallback callback);

    public abstract void requestLocation(Context context, String query,
                                         @NonNull RequestLocationCallback callback);

    public abstract void requestLocation(Context context, @Size(3) String[] queries,
                                         @NonNull RequestLocationCallback callback);

    public abstract void requestLocation(Context context, String lat, String lon,
                                         @NonNull RequestLocationCallback callback);

    public abstract void cancel();

    public interface RequestWeatherCallback {
        void requestWeatherSuccess(Weather weather, Location requestLocation);
        void requestWeatherFailed(Location requestLocation);
    }

    public interface RequestLocationCallback {
        void requestLocationSuccess(String query, List<Location> locationList);
        void requestLocationFailed(String query);
    }
}
