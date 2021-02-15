package wangdaye.com.geometricweather.location;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;

import java.util.List;
import java.util.TimeZone;

import wangdaye.com.geometricweather.basic.models.Location;
import wangdaye.com.geometricweather.basic.models.options.provider.WeatherSource;
import wangdaye.com.geometricweather.db.DatabaseHelper;
import wangdaye.com.geometricweather.location.services.AMapLocationService;
import wangdaye.com.geometricweather.location.services.AndroidLocationService;
import wangdaye.com.geometricweather.location.services.ip.BaiduIPLocationService;
import wangdaye.com.geometricweather.location.services.BaiduLocationService;
import wangdaye.com.geometricweather.location.services.LocationService;
import wangdaye.com.geometricweather.settings.SettingsOptionManager;
import wangdaye.com.geometricweather.utils.NetworkUtils;
import wangdaye.com.geometricweather.weather.WeatherHelper;
import wangdaye.com.geometricweather.weather.services.WeatherService;

/**
 * Location helper.
 * */

public class LocationHelper {

    private final @NonNull LocationService mLocationService;
    private @Nullable WeatherService mWeatherService;

    public interface OnRequestLocationListener {
        void requestLocationSuccess(Location requestLocation);
        void requestLocationFailed(Location requestLocation);
    }

    public LocationHelper(Context context) {
        switch (SettingsOptionManager.getInstance(context).getLocationProvider()) {
            case BAIDU:
                mLocationService = new BaiduLocationService(context);
                break;

            case BAIDU_IP:
                mLocationService = new BaiduIPLocationService();
                break;

            case AMAP:
                mLocationService = new AMapLocationService(context);
                break;

            default: // NATIVE
                mLocationService = new AndroidLocationService(context);
                break;
        }

        mWeatherService = null;
    }

    public void requestLocation(Context context, Location location, boolean background,
                                @NonNull OnRequestLocationListener l) {
        if (mLocationService.getPermissions().length != 0) {
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

        mLocationService.requestLocation(
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
        WeatherSource source = SettingsOptionManager.getInstance(context).getWeatherSource();
        final Location target = new Location(location, source);

        mWeatherService = WeatherHelper.getWeatherService(source);
        mWeatherService.requestLocation(context, target, new WeatherService.RequestLocationCallback() {
            @Override
            public void requestLocationSuccess(String query, List<Location> locationList) {
                if (locationList.size() > 0) {
                    Location src = locationList.get(0);
                    Location result = new Location(src, true, src.isResidentPosition());
                    DatabaseHelper.getInstance(context).writeLocation(result);
                    l.requestLocationSuccess(result);
                } else {
                    requestLocationFailed(query);
                }
            }

            @Override
            public void requestLocationFailed(String query) {
                l.requestLocationFailed(target);
            }
        });
    }

    public void cancel() {
        mLocationService.cancel();
        if (mWeatherService != null) {
            mWeatherService.cancel();
        }
    }

    public String[] getPermissions() {
        // if IP:    none.
        // else:
        //      R:   foreground location. (set background location enabled manually)
        //      Q:   foreground location + background location.
        //      K-P: foreground location.

        String[] permissions = mLocationService.getPermissions();
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q || permissions.length == 0) {
            // device has no background location permission or locate by IP.
            return permissions;
        }

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.R) {
            String[] qPermissions = new String[permissions.length + 1];
            System.arraycopy(permissions, 0, qPermissions, 0, permissions.length);
            qPermissions[qPermissions.length - 1] = Manifest.permission.ACCESS_BACKGROUND_LOCATION;
            return qPermissions;
        }

        return permissions;
    }
}