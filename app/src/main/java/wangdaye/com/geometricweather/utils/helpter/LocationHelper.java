package wangdaye.com.geometricweather.utils.helpter;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import com.baidu.location.BDAbstractLocationListener;
import com.baidu.location.BDLocation;
import com.baidu.location.LocationClient;

import java.util.List;

import wangdaye.com.geometricweather.data.entity.model.Location;
import wangdaye.com.geometricweather.data.service.WeatherService;
import wangdaye.com.geometricweather.data.service.LocationService;

/**
 * Location utils.
 * */

public class LocationHelper {

    private LocationClient client;
    private WeatherService weather;

    private static final String PREFERENCE_LOCAL = "LOCAL_PREFERENCE";
    private static final String KEY_LAST_RESULT = "LAST_RESULT";

    private class SimpleLocationListener extends BDAbstractLocationListener {
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
        public void onReceiveLocation(BDLocation bdLocation) {
            if (finish) {
                return;
            }
            switch (bdLocation.getLocType()) {
                case BDLocation.TypeGpsLocation:
                case BDLocation.TypeNetWorkLocation:
                case BDLocation.TypeOffLineLocation:
                    if (listener != null) {
                        SharedPreferences sharedPreferences = c.getSharedPreferences(
                                PREFERENCE_LOCAL, Context.MODE_PRIVATE);
                        String oldCity = sharedPreferences.getString(KEY_LAST_RESULT, ".");

                        location.local = true;
                        location.city = bdLocation.getDistrict();
                        location.prov = bdLocation.getProvince();
                        location.cnty = bdLocation.getCountry();
                        location.lat = String.valueOf(bdLocation.getLatitude());
                        location.lon = String.valueOf(bdLocation.getLongitude());

                        sharedPreferences.edit()
                                .putString(KEY_LAST_RESULT, location.city)
                                .apply();

                        if (!location.isUsable() || !location.city.equals(oldCity)) {
                            requestWeatherLocationByGeoPosition(
                                    c,
                                    location,
                                    new SimpleWeatherLocationListener(location, listener));
                        } else {
                            listener.requestLocationSuccess(location, false);
                        }
                        finish = true;
                    }
                    break;

                default:
                    if (listener != null) {
                        listener.requestLocationFailed(location);
                    }
                    break;
            }
        }
    }

    private class SimpleWeatherLocationListener implements OnRequestWeatherLocationListener {
        // data
        private Location location;
        private OnRequestLocationListener listener;

        SimpleWeatherLocationListener(Location location, OnRequestLocationListener l) {
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

    public LocationHelper(Context c) {
        client = new LocationClient(c);
        weather = new WeatherService();
    }

    public void requestLocation(Context c, Location location, OnRequestLocationListener l) {
        NetworkInfo info = ((ConnectivityManager) c.getSystemService(Context.CONNECTIVITY_SERVICE))
                .getActiveNetworkInfo();
        if (info != null && info.isAvailable()) {
            LocationService.requestLocation(client, new SimpleLocationListener(c, location, l));
        } else {
            l.requestLocationFailed(location);
        }
    }

    public void requestWeatherLocation(Context c, String query, OnRequestWeatherLocationListener l) {
        weather = WeatherService.getService().requestNewLocation(c, query, l);
    }

    private void requestWeatherLocationByGeoPosition(Context c, Location location, OnRequestWeatherLocationListener l) {
        weather = WeatherService.getService().requestNewLocationByGeoPosition(c, location.lat, location.lon, l);
    }

    public void cancel() {
        if (client != null) {
            client.stop();
        }
        if (weather != null) {
            weather.cancel();
        }
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