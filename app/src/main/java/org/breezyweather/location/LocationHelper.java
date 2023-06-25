package org.breezyweather.location;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;

import java.util.List;
import java.util.TimeZone;

import javax.inject.Inject;

import dagger.hilt.android.qualifiers.ApplicationContext;
import org.breezyweather.common.basic.models.Location;
import org.breezyweather.common.basic.models.options.provider.LocationProvider;
import org.breezyweather.common.basic.models.options.provider.WeatherSource;
import org.breezyweather.db.repositories.LocationEntityRepository;
import org.breezyweather.location.services.BaiduIPLocationService;
import org.breezyweather.location.services.LocationService;
import org.breezyweather.main.utils.RequestErrorType;
import org.breezyweather.settings.SettingsManager;
import org.breezyweather.common.utils.NetworkUtils;
import org.breezyweather.location.services.AMapLocationService;
import org.breezyweather.location.services.AndroidLocationService;
import org.breezyweather.location.services.BaiduLocationService;
import org.breezyweather.weather.WeatherServiceSet;
import org.breezyweather.weather.services.WeatherService;

/**
 * Location helper.
 */

public class LocationHelper {

    private final LocationService[] mLocationServices;
    private final WeatherServiceSet mWeatherServiceSet;

    public interface OnRequestLocationListener {
        void requestLocationSuccess(Location requestLocation);

        void requestLocationFailed(Location requestLocation, RequestErrorType requestErrorType);
    }

    @Inject
    public LocationHelper(@ApplicationContext Context context,
                          BaiduIPLocationService baiduIPService,
                          WeatherServiceSet weatherServiceSet) {
        mLocationServices = new LocationService[] {
                new AndroidLocationService(),
                new BaiduLocationService(context),
                baiduIPService,
                new AMapLocationService(context)
        };

        mWeatherServiceSet = weatherServiceSet;
    }

    private LocationService getLocationService(LocationProvider provider) {
        switch (provider) {
            case BAIDU:
                return mLocationServices[1];

            case BAIDU_IP:
                return mLocationServices[2];

            case AMAP:
                return mLocationServices[3];

            default: // NATIVE
                return mLocationServices[0];
        }
    }

    public void requestLocation(Context context, Location location, boolean background,
                                @NonNull OnRequestLocationListener l) {
        final OnRequestLocationListener usableCheckListener = new OnRequestLocationListener() {
            @Override
            public void requestLocationSuccess(Location requestLocation) {
                l.requestLocationSuccess(requestLocation);
            }

            @Override
            public void requestLocationFailed(Location requestLocation, RequestErrorType requestErrorType) {
                l.requestLocationFailed(requestLocation, requestErrorType);
            }
        };

        final LocationProvider provider = SettingsManager.getInstance(context).getLocationProvider();
        final LocationService service = getLocationService(provider);
        if (service.getPermissions().length != 0) {
            if (!NetworkUtils.isAvailable(context)) {
                usableCheckListener.requestLocationFailed(location, RequestErrorType.NETWORK_UNAVAILABLE);
                return;
            }
            // if needs any location permission.
            if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) !=
                    PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) !=
                    PackageManager.PERMISSION_GRANTED
            ) {
                usableCheckListener.requestLocationFailed(location, RequestErrorType.ACCESS_LOCATION_PERMISSION_MISSING);
                return;
            }
            if (background) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q && ActivityCompat.checkSelfPermission(
                        context,
                        Manifest.permission.ACCESS_BACKGROUND_LOCATION
                ) != PackageManager.PERMISSION_GRANTED) {
                    usableCheckListener.requestLocationFailed(location, RequestErrorType.ACCESS_BACKGROUND_LOCATION_PERMISSION_MISSING);
                    return;
                }
            }
        }

        // 1. get location by location service.
        // 2. get available location by weather service.
        service.requestLocation(
                context,
                result -> {
                    if (result == null) {
                        usableCheckListener.requestLocationFailed(location, RequestErrorType.LOCATION_FAILED);
                        return;
                    }

                    requestAvailableWeatherLocation(
                            context,
                            Location.copy(
                                    location,
                                    result.getLatitude(),
                                    result.getLongitude(),
                                    TimeZone.getDefault()
                            ),
                            usableCheckListener
                    );
                }
        );
    }

    private void requestAvailableWeatherLocation(Context context,
                                                 @NonNull Location location,
                                                 @NonNull OnRequestLocationListener l) {
        WeatherSource source = SettingsManager.getInstance(context).getWeatherSource();

        final WeatherService service = mWeatherServiceSet.get(source);
        service.requestLocation(context, location, new WeatherService.RequestLocationCallback() {
            @Override
            public void requestLocationSuccess(String query, List<Location> locationList) {
                if (locationList.size() > 0) {
                    Location src = locationList.get(0);
                    Location result = Location.copy(src, true, src.isResidentPosition());
                    LocationEntityRepository.INSTANCE.writeLocation(result);
                    l.requestLocationSuccess(result);
                } else {
                    requestLocationFailed(query, RequestErrorType.LOCATION_FAILED);
                }
            }

            @Override
            public void requestLocationFailed(String query, RequestErrorType requestErrorType) {
                l.requestLocationFailed(location, requestErrorType);
            }
        });
    }

    public void cancel() {
        for (LocationService s : mLocationServices) {
            s.cancel();
        }
        for (WeatherService s : mWeatherServiceSet.getAll()) {
            s.cancel();
        }
    }

    public String[] getPermissions(Context context) {
        // if IP:    none.
        // else:
        //      R:   foreground location. (set background location enabled manually)
        //      Q:   foreground location + background location.
        //      K-P: foreground location.

        final LocationProvider provider = SettingsManager.getInstance(context).getLocationProvider();
        final LocationService service = getLocationService(provider);

        String[] permissions = service.getPermissions();
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