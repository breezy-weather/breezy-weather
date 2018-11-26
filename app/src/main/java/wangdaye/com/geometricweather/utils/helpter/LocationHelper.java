package wangdaye.com.geometricweather.utils.helpter;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;

import java.util.List;

import wangdaye.com.geometricweather.GeometricWeather;
import wangdaye.com.geometricweather.data.entity.model.Location;
import wangdaye.com.geometricweather.data.service.location.AndroidLocationService;
import wangdaye.com.geometricweather.data.service.location.BaiduLocationService;
import wangdaye.com.geometricweather.data.service.location.LocationService;
import wangdaye.com.geometricweather.data.service.weather.AccuWeatherService;
import wangdaye.com.geometricweather.data.service.weather.CNWeatherService;
import wangdaye.com.geometricweather.data.service.weather.CaiYunWeatherService;
import wangdaye.com.geometricweather.data.service.weather.WeatherService;


/**
 * Location utils.
 * */

public class LocationHelper {

    private LocationService locationService;
    private WeatherService weatherService;

    private static final String PREFERENCE_LOCAL = "LOCAL_PREFERENCE";
    private static final String KEY_LAST_RESULT = "LAST_RESULT";
    private static final String KEY_LAST_ACCU_KEY = "LAST_ACCU_KEY";

    private class RequestLocationListener implements LocationService.LocationCallback {
        // data
        private Context c;
        private Location location;
        private OnRequestLocationListener listener;
        private boolean finish;

        RequestLocationListener(Context c, Location location, @NonNull OnRequestLocationListener l) {
            this.c = c;
            this.location = location;
            this.listener = l;
            this.finish = false;
        }

        @Override
        public void onCompleted(@Nullable LocationService.Result result) {
            if (finish) {
                return;
            }
            finish = true;
            locationService.cancel();
            if (listener == null) {
                return;
            }
            if (result == null) {
                listener.requestLocationFailed(location);
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

            requestAvailableWeatherLocation(c, location, listener);
        }
    }

    private class AccuLocationCallback implements WeatherService.RequestLocationCallback {
        // data
        private Context context;
        private Location location;
        private OnRequestLocationListener listener;

        AccuLocationCallback(Context c, Location location, @NonNull OnRequestLocationListener l) {
            this.context = c;
            this.location = location;
            this.listener = l;
        }

        @Override
        public void requestLocationSuccess(String query, List<Location> locationList) {
            if (locationList.size() > 0) {

                if (!TextUtils.isEmpty(locationList.get(0).cityId)) {
                    context.getSharedPreferences(PREFERENCE_LOCAL, Context.MODE_PRIVATE)
                            .edit()
                            .putString(KEY_LAST_ACCU_KEY, locationList.get(0).cityId)
                            .apply();
                }

                listener.requestLocationSuccess(locationList.get(0).setLocal(), true);
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
                String oldId = location.cityId;
                location.cityId = locationList.get(0).cityId;
                location.city = locationList.get(0).city;
                location.setLocal();
                listener.requestLocationSuccess(
                        location,
                        TextUtils.isEmpty(oldId) || !oldId.equals(location.cityId));
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
                locationService.requestLocation(c, new RequestLocationListener(c, location, l));
                return;
            }
        }
        l.requestLocationFailed(location);
    }

    private void requestAvailableWeatherLocation(Context c,
                                                 Location location,
                                                 @NonNull OnRequestLocationListener l) {
        SharedPreferences sharedPreferences = c.getSharedPreferences(
                PREFERENCE_LOCAL, Context.MODE_PRIVATE);
        if (!location.canUseChineseSource()
                || GeometricWeather.getInstance().getChineseSource().equals("accu")) {
            // use accu as weather service api.
            location.source = "accu";

            String oldCity = sharedPreferences.getString(KEY_LAST_RESULT, "");
            String oldKey = sharedPreferences.getString(KEY_LAST_ACCU_KEY, "");

            if (!TextUtils.isEmpty(location.city)) {
                sharedPreferences.edit()
                        .putString(KEY_LAST_RESULT, location.city)
                        .apply();
            }

            if (!TextUtils.isEmpty(location.city) && location.city.equals(oldCity)
                    && !TextUtils.isEmpty(location.cityId) && location.cityId.equals(oldKey)) {
                l.requestLocationSuccess(location, false);
                return;
            }

            weatherService = new AccuWeatherService();
            weatherService.requestLocation(
                    c, location.lat, location.lon, new AccuLocationCallback(c, location, l));
        } else if (GeometricWeather.getInstance().getChineseSource().equals("cn")) {
            // use cn weather net as the weather service api.
            location.source = "cn";
            weatherService = new CNWeatherService();
            weatherService.requestLocation(
                    c,
                    new String[]{location.district, location.city, location.province},
                    new CNLocationCallback(location, l));
        } else {
            // caiyun.
            location.source = "caiyun";
            weatherService = new CaiYunWeatherService();
            weatherService.requestLocation(
                    c,
                    new String[]{location.district, location.city, location.province},
                    new CNLocationCallback(location, l));
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

    // interface.

    public interface OnRequestLocationListener {
        void requestLocationSuccess(Location requestLocation, boolean locationChanged);
        void requestLocationFailed(Location requestLocation);
    }
}