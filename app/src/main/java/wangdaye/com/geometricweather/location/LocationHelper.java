package wangdaye.com.geometricweather.location;

import android.content.Context;
import android.content.SharedPreferences;
import android.location.Geocoder;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import androidx.annotation.NonNull;
import android.text.TextUtils;

import java.util.List;

import androidx.annotation.Nullable;
import wangdaye.com.geometricweather.GeometricWeather;
import wangdaye.com.geometricweather.R;
import wangdaye.com.geometricweather.basic.model.Location;
import wangdaye.com.geometricweather.location.service.AMapLocationService;
import wangdaye.com.geometricweather.location.service.AndroidLocationService;
import wangdaye.com.geometricweather.location.service.ip.BaiduIPLocationService;
import wangdaye.com.geometricweather.location.service.BaiduLocationService;
import wangdaye.com.geometricweather.location.service.LocationService;
import wangdaye.com.geometricweather.weather.service.AccuWeatherService;
import wangdaye.com.geometricweather.weather.service.CNWeatherService;
import wangdaye.com.geometricweather.weather.service.CaiYunWeatherService;
import wangdaye.com.geometricweather.weather.service.WeatherService;

/**
 * Location utils.
 * */

public class LocationHelper {

    private LocationService locationService;
    private WeatherService weatherService;

    private class AccuLocationCallback implements WeatherService.RequestLocationCallback {
        private Location location;
        private OnRequestLocationListener listener;

        AccuLocationCallback(Location location, @NonNull OnRequestLocationListener l) {
            this.location = location;
            this.listener = l;
        }

        @Override
        public void requestLocationSuccess(String query, List<Location> locationList) {
            if (locationList.size() > 0) {
                listener.requestLocationSuccess(locationList.get(0).setLocal());
            } else {
                requestLocationFailed(query);
            }
        }

        @Override
        public void requestLocationFailed(String query) {
            listener.requestLocationFailed(location);
        }
    }

    private class CNLocationCallback implements WeatherService.RequestLocationCallback {
        // data
        private Location location;
        private OnRequestLocationListener listener;

        CNLocationCallback(Location location, @NonNull OnRequestLocationListener l) {
            this.location = location;
            this.listener = l;
        }

        @Override
        public void requestLocationSuccess(String query, List<Location> locationList) {
            if (locationList.size() > 0) {
                location.cityId = locationList.get(0).cityId;
                location.setLocal();
                listener.requestLocationSuccess(location);
            } else {
                requestLocationFailed(query);
            }
        }

        @Override
        public void requestLocationFailed(String query) {
            listener.requestLocationFailed(location);
        }
    }

    public LocationHelper(Context context) {
        switch (GeometricWeather.getInstance().getLocationService()) {
            case "baidu":
                locationService = new BaiduLocationService(context);
                break;

            case "baidu_ip":
                locationService = new BaiduIPLocationService();
                break;

            case "amap":
                locationService = new AMapLocationService(context);
                break;

            default:
                locationService = new AndroidLocationService(context);
                break;
        }
    }

    public void requestLocation(Context c, Location location, @NonNull OnRequestLocationListener l) {
        // 1. get location by location service.
        // 2. get available location by weather service.
        ConnectivityManager manager = (ConnectivityManager) c.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (manager != null) {
            NetworkInfo info = manager.getActiveNetworkInfo();
            if (info != null && info.isAvailable()) {
                locationService.requestLocation(c, new LocationService.LocationCallback() {
                    boolean completed = false;
                    @Override
                    public void onCompleted(@Nullable LocationService.Result result) {
                        if (completed) {
                            return;
                        }

                        completed = true;
                        locationService.cancel();
                        if (result == null) {
                            l.requestLocationFailed(location);
                            return;
                        }

                        location.local = true;
                        location.district = result.district;
                        location.city = result.city;
                        location.province = result.province;
                        location.country = result.country;
                        location.lat = result.latitude;
                        location.lon = result.longitude;
                        location.china = result.inChina;

                        requestAvailableWeatherLocation(c, location, l);
                    }
                });
                return;
            }
        }
        l.requestLocationFailed(location);
    }

    private void requestAvailableWeatherLocation(Context c,
                                                 Location location,
                                                 @NonNull OnRequestLocationListener l) {
        if (!location.canUseChineseSource()
                || GeometricWeather.getInstance().getChineseSource().equals("accu")) {
            // use accu as weather service api.
            location.source = "accu";
            weatherService = new AccuWeatherService();
            weatherService.requestLocation(c, location, new AccuLocationCallback(location, l));
        } else if (GeometricWeather.getInstance().getChineseSource().equals("cn")) {
            // use cn weather net as the weather service api.
            location.source = "cn";
            weatherService = new CNWeatherService();
            weatherService.requestLocation(c, location, new CNLocationCallback(location, l));
        } else {
            // caiyun.
            location.source = "caiyun";
            weatherService = new CaiYunWeatherService();
            weatherService.requestLocation(c, location, new CNLocationCallback(location, l));
        }
    }

    public void cancel() {
        if (locationService != null) {
            locationService.cancel();
        }
        if (weatherService != null) {
            weatherService.cancel();
        }
    }

    public boolean hasPermissions(Context context) {
        return locationService.hasPermissions(context);
    }

    public String[] getPermissions() {
        return locationService.getPermissions();
    }

    public static String getLocationServiceProvider(Context context, SharedPreferences sharedPreferences) {
        String provider = sharedPreferences.getString(
                context.getString(R.string.key_location_service),
                "");
        if (TextUtils.isEmpty(provider)) {
            if (Geocoder.isPresent()) {
                provider = "native";
            } else {
                provider = "baidu";
            }
        }
        sharedPreferences.edit()
                .putString(context.getString(R.string.key_location_service), provider)
                .apply();
        return provider;
    }

    // interface.

    public interface OnRequestLocationListener {
        void requestLocationSuccess(Location requestLocation);
        void requestLocationFailed(Location requestLocation);
    }
}