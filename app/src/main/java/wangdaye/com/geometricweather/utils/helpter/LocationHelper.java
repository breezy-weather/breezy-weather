package wangdaye.com.geometricweather.utils.helpter;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.annotation.Nullable;
import android.text.TextUtils;

import java.util.List;

import wangdaye.com.geometricweather.GeometricWeather;
import wangdaye.com.geometricweather.data.entity.model.Location;
import wangdaye.com.geometricweather.data.service.location.BaiduLocationService;
import wangdaye.com.geometricweather.data.service.location.LocationService;
import wangdaye.com.geometricweather.data.service.weather.WeatherService;

/**
 * Location utils.
 * */

public class LocationHelper {

    private LocationService service;
    private WeatherService weather;

    private static final String PREFERENCE_LOCAL = "LOCAL_PREFERENCE";
    private static final String KEY_LAST_RESULT = "LAST_RESULT";

    private class SimpleLocationListener implements LocationService.LocationCallback {
        // data
        private Context c;
        private Location location;
        private OnRequestLocationListener listener;
        private boolean finish;

        SimpleLocationListener(Context c, Location location, OnRequestLocationListener l) {
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
            service.cancel();
            if (listener == null) {
                return;
            }
            if (result == null) {
                listener.requestLocationFailed(location);
                return;
            }

            location.local = true;
            location.city = result.district;
            location.prov = result.province;
            location.cnty = result.contry;
            location.lat = result.latitude;
            location.lon = result.longitude;

            if (result.inChina && GeometricWeather.getInstance().getChineseSource().equals("cn")) {
                requestCNWeatherLocation(c, location, result, listener);
            } else {
                requestAccuWeatherLocation(c, location, listener);
            }
        }
    }

    private class AccuWeatherLocationListener implements OnRequestWeatherLocationListener {
        // data
        private Location location;
        private OnRequestLocationListener listener;

        AccuWeatherLocationListener(Location location, OnRequestLocationListener l) {
            this.location = location;
            this.listener = l;
        }

        @Override
        public void requestWeatherLocationSuccess(String query, List<Location> locationList) {
            if (locationList.size() > 0) {
                listener.requestLocationSuccess(locationList.get(0).setLocal(), true);
            } else {
                listener.requestLocationFailed(location);
            }
        }

        @Override
        public void requestWeatherLocationFailed(String query) {
            listener.requestLocationFailed(location);
        }
    }

    private class CNWeatherLocationListener implements OnRequestWeatherLocationListener {
        // data
        private Context context;
        private Location location;
        private OnRequestLocationListener listener;

        CNWeatherLocationListener(Context context, Location location, OnRequestLocationListener l) {
            this.context = context;
            this.location = location;
            this.listener = l;
        }

        @Override
        public void requestWeatherLocationSuccess(String query, List<Location> locationList) {
            if (locationList.size() > 0) {
                String oldId = location.cityId;
                location.cityId = locationList.get(0).cityId;
                location.city = locationList.get(0).city;
                location.setLocal();
                listener.requestLocationSuccess(
                        location,
                        TextUtils.isEmpty(oldId) || !oldId.equals(location.cityId));
            } else {
                requestWeatherLocationFailed(query);
            }
        }

        @Override
        public void requestWeatherLocationFailed(String query) {
            requestAccuWeatherLocation(context, location, listener);
        }
    }

    public LocationHelper(Context context) {
        service = new BaiduLocationService(context);
        weather = new WeatherService();
    }

    public void requestLocation(Context c, Location location, OnRequestLocationListener l) {
        ConnectivityManager manager = (ConnectivityManager) c.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (manager != null) {
            NetworkInfo info = manager.getActiveNetworkInfo();
            if (info != null && info.isAvailable()) {
                service.requestLocation(c, new SimpleLocationListener(c, location, l));
            } else {
                l.requestLocationFailed(location);
            }
        }
    }

    private void requestAccuWeatherLocation(Context c, Location location, OnRequestLocationListener l) {
        SharedPreferences sharedPreferences = c.getSharedPreferences(
                PREFERENCE_LOCAL, Context.MODE_PRIVATE);
        String oldCity = sharedPreferences.getString(KEY_LAST_RESULT, ".");

        if (!TextUtils.isEmpty(location.city)) {
            sharedPreferences.edit()
                    .putString(KEY_LAST_RESULT, location.city)
                    .apply();
        }
        if (TextUtils.isEmpty(location.city) || !location.city.equals(oldCity)
                || !location.isUsable()) {
            requestWeatherLocation(
                    c,
                    null,
                    location.lat,
                    location.lon,
                    false,
                    new AccuWeatherLocationListener(location, l));
        } else {
            l.requestLocationSuccess(location, false);
        }
    }

    private void requestCNWeatherLocation(Context c,
                                          Location location, LocationService.Result result,
                                          OnRequestLocationListener l) {
        requestWeatherLocation(
                c,
                new String[] {result.district, result.city, result.province},
                location.lat,
                location.lon,
                false,
                new CNWeatherLocationListener(c, location, l));
    }

    public void requestWeatherLocation(Context c, String query, boolean fuzzy, OnRequestWeatherLocationListener l) {
        requestWeatherLocation(c, new String[] {query}, null, null, fuzzy, l);
    }

    private void requestWeatherLocation(Context c,
                                        String[] queries, @Nullable String lat, @Nullable String lon,
                                        boolean fuzzy,
                                        OnRequestWeatherLocationListener l) {
        weather = WeatherService.getService().requestLocation(c, queries, lat, lon, fuzzy, l);
    }

    public void cancel() {
        if (service != null) {
            service.cancel();
        }
        if (weather != null) {
            weather.cancel();
        }
    }

    public static void clearLocationCache(Context context) {
        SharedPreferences.Editor editor = context.getSharedPreferences(
                PREFERENCE_LOCAL, Context.MODE_PRIVATE).edit();
        editor.putString(KEY_LAST_RESULT, ".");
        editor.apply();
    }

    // interface.

    public interface OnRequestLocationListener {
        void requestLocationSuccess(Location requestLocation, boolean locationChanged);
        void requestLocationFailed(Location requestLocation);
    }

    public interface OnRequestWeatherLocationListener {
        void requestWeatherLocationSuccess(String query, List<Location> locationList);
        void requestWeatherLocationFailed(String query);
    }
}