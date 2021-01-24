package wangdaye.com.geometricweather.location;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;

import java.util.List;
import java.util.TimeZone;

import wangdaye.com.geometricweather.basic.model.Location;
import wangdaye.com.geometricweather.basic.model.option.provider.WeatherSource;
import wangdaye.com.geometricweather.db.DatabaseHelper;
import wangdaye.com.geometricweather.location.service.AMapLocationService;
import wangdaye.com.geometricweather.location.service.AndroidLocationService;
import wangdaye.com.geometricweather.location.service.ip.BaiduIPLocationService;
import wangdaye.com.geometricweather.location.service.BaiduLocationService;
import wangdaye.com.geometricweather.location.service.LocationService;
import wangdaye.com.geometricweather.settings.SettingsOptionManager;
import wangdaye.com.geometricweather.utils.NetworkUtils;
import wangdaye.com.geometricweather.weather.service.AccuWeatherService;
import wangdaye.com.geometricweather.weather.service.CNWeatherService;
import wangdaye.com.geometricweather.weather.service.CaiYunWeatherService;
import wangdaye.com.geometricweather.weather.service.WeatherService;

/**
 * Location helper.
 * */

public class LocationHelper {

    @NonNull private final LocationService locationService;
    @NonNull private final WeatherService accuWeather;
    @NonNull private final WeatherService cnWeather;
    @NonNull private final WeatherService caiyunWeather;

    public LocationHelper(Context context) {
        switch (SettingsOptionManager.getInstance(context).getLocationProvider()) {
            case BAIDU:
                locationService = new BaiduLocationService(context);
                break;

            case BAIDU_IP:
                locationService = new BaiduIPLocationService();
                break;

            case AMAP:
                locationService = new AMapLocationService(context);
                break;
            default: // NATIVE
                locationService = new AndroidLocationService(context);
                break;
        }

        accuWeather = new AccuWeatherService();
        cnWeather = new CNWeatherService();
        caiyunWeather = new CaiYunWeatherService();
    }

    public void requestLocation(Context context, Location location, boolean background,
                                @NonNull OnRequestLocationListener l) {
        if (locationService.getPermissions().length != 0) {
            // if needs any location permission.
            if (!NetworkUtils.isAvailable(context)
                    || ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION)
                    != PackageManager.PERMISSION_GRANTED
                    || ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION)
                    != PackageManager.PERMISSION_GRANTED) {
                l.requestLocationFailed(location);
                return;
            }
            if (background) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q
                        && ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_BACKGROUND_LOCATION)
                        != PackageManager.PERMISSION_GRANTED) {
                    l.requestLocationFailed(location);
                    return;
                }
            }
        }

        // 1. get location by location service.
        // 2. get available location by weather service.

        locationService.requestLocation(
                context,
                result -> {
                    if (result == null) {
                        l.requestLocationFailed(location);
                        return;
                    }

                    requestAvailableWeatherLocation(context, new Location(
                            location, result.latitude, result.longitude, TimeZone.getDefault(),
                            result.country, result.province, result.city, result.district, result.inChina
                    ), l);
                }
        );
    }

    private void requestAvailableWeatherLocation(Context context,
                                                 @NonNull Location location,
                                                 @NonNull OnRequestLocationListener l) {
        switch (SettingsOptionManager.getInstance(context).getWeatherSource()) {
            case ACCU:
                location = new Location(location, WeatherSource.ACCU);
                accuWeather.requestLocation(context, location, new AccuLocationCallback(context, location, l));
                break;

            case CN:
                location = new Location(location, WeatherSource.CN);
                cnWeather.requestLocation(context, location, new ChineseCityLocationCallback(context, location, l));
                break;

            case CAIYUN:
                location = new Location(location, WeatherSource.CAIYUN);
                caiyunWeather.requestLocation(context, location, new ChineseCityLocationCallback(context, location, l));
                break;
        }
    }

    public void cancel() {
        locationService.cancel();
        accuWeather.cancel();
        cnWeather.cancel();
        caiyunWeather.cancel();
    }

    public String[] getPermissions(boolean background) {
        String[] permissions = locationService.getPermissions();
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q || permissions.length == 0) {
            return permissions;
        }

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.R) {
            String[] qPermissions = new String[permissions.length + 1];
            System.arraycopy(permissions, 0, qPermissions, 0, permissions.length);
            qPermissions[qPermissions.length - 1] = Manifest.permission.ACCESS_BACKGROUND_LOCATION;
            return qPermissions;
        }

        if (background) {
            return new String[] {Manifest.permission.ACCESS_BACKGROUND_LOCATION};
        } else {
            return permissions;
        }
    }

    // interface.

    public interface OnRequestLocationListener {
        void requestLocationSuccess(Location requestLocation);
        void requestLocationFailed(Location requestLocation);
    }
}

class AccuLocationCallback implements WeatherService.RequestLocationCallback {

    private final Context context;
    private final Location location;
    private final LocationHelper.OnRequestLocationListener listener;

    AccuLocationCallback(Context context, Location location,
                         @NonNull LocationHelper.OnRequestLocationListener l) {
        this.context = context;
        this.location = location;
        this.listener = l;
    }

    @Override
    public void requestLocationSuccess(String query, List<Location> locationList) {
        if (locationList.size() > 0) {
            Location src = locationList.get(0);
            Location result = new Location(src, true, src.isResidentPosition());
            DatabaseHelper.getInstance(context).writeLocation(result);
            listener.requestLocationSuccess(result);
        } else {
            requestLocationFailed(query);
        }
    }

    @Override
    public void requestLocationFailed(String query) {
        listener.requestLocationFailed(location);
    }
}

class ChineseCityLocationCallback implements WeatherService.RequestLocationCallback {

    private final Context context;
    private final Location location;
    private final LocationHelper.OnRequestLocationListener listener;

    ChineseCityLocationCallback(Context context, Location location,
                                @NonNull LocationHelper.OnRequestLocationListener l) {
        this.context = context;
        this.location = location;
        this.listener = l;
    }

    @Override
    public void requestLocationSuccess(String query, List<Location> locationList) {
        if (locationList.size() > 0) {
            Location src = locationList.get(0);
            Location result = new Location(src, true, src.isResidentPosition());
            DatabaseHelper.getInstance(context).writeLocation(result);
            listener.requestLocationSuccess(result);
        } else {
            requestLocationFailed(query);
        }
    }

    @Override
    public void requestLocationFailed(String query) {
        listener.requestLocationFailed(location);
    }
}