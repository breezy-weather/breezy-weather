package wangdaye.com.geometricweather.utils.helpter;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;

import java.util.List;

import wangdaye.com.geometricweather.data.entity.model.Location;
import wangdaye.com.geometricweather.data.service.NewWeather;
import wangdaye.com.geometricweather.data.service.BaiduLocation;

/**
 * Location utils.
 * */

public class LocationHelper {
    // data
    private LocationClient client;
    private NewWeather weather;

    /** <br> life cycle. */

    public LocationHelper(Context c) {
        client = new LocationClient(c);
        weather = new NewWeather();
    }

    /** <br> data. */

    public void requestLocation(Context c, Location location, OnRequestLocationListener l) {
        NetworkInfo info = ((ConnectivityManager) c.getSystemService(Context.CONNECTIVITY_SERVICE))
                .getActiveNetworkInfo();
        if (info != null && info.isAvailable()) {
            BaiduLocation.requestLocation(client, new SimpleLocationListener(c, location, l));
        } else {
            l.requestLocationFailed(location);
        }
    }

    public void requestWeatherLocation(Context c, String query, OnRequestWeatherLocationListener l) {
        weather = NewWeather.getService().requestNewLocation(c, query, l);
    }

    public void cancel() {
        if (client != null) {
            client.stop();
        }
        if (weather != null) {
            weather.cancel();
        }
    }

    /** <br> interface */

    public interface OnRequestLocationListener {
        void requestLocationSuccess(Location requestLocation);
        void requestLocationFailed(Location requestLocation);
    }

    public interface OnRequestWeatherLocationListener {
        void requestWeatherLocationSuccess(String query, List<Location> locationList);
        void requestWeatherLocationFailed(String query);
    }

    private class SimpleLocationListener implements BDLocationListener {
        // data
        private Context c;
        private Location location;
        private OnRequestLocationListener listener;

        SimpleLocationListener(Context c, Location location, OnRequestLocationListener l) {
            this.c = c;
            this.location = location;
            this.listener = l;
        }

        @Override
        public void onReceiveLocation(BDLocation bdLocation) {
            switch (bdLocation.getLocType()) {
                case BDLocation.TypeGpsLocation: // GPS定位结果
                case BDLocation.TypeNetWorkLocation: // 网络定位结果
                case BDLocation.TypeOffLineLocation: // 离线定位
                    if (listener != null) {
                        location.local = true;
                        String oldCity = bdLocation.getDistrict();
                        location.city = bdLocation.getDistrict();
                        location.cnty = bdLocation.getCountry();
                        location.lat = String.valueOf(bdLocation.getLatitude());
                        location.lon = String.valueOf(bdLocation.getLongitude());
                        location.prov = bdLocation.getProvince().split("省")[0];
                        if (!location.isUsable() || !location.city.equals(oldCity)) {
                            requestWeatherLocation(c, location.city, new SimpleWeatherLocationListener(location, listener));
                        } else {
                            listener.requestLocationSuccess(location);
                        }
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
                listener.requestLocationSuccess(locationList.get(0).setLocal());
            } else {
                listener.requestLocationFailed(location);
            }
        }

        @Override
        public void requestWeatherLocationFailed(String query) {
            listener.requestLocationFailed(location);
        }
    }
}
