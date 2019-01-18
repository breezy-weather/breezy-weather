package wangdaye.com.geometricweather.utils.helpter;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;

import java.util.List;

import wangdaye.com.geometricweather.GeometricWeather;
import wangdaye.com.geometricweather.data.entity.model.Location;
import wangdaye.com.geometricweather.data.service.location.AMapLocationService;
import wangdaye.com.geometricweather.data.service.location.AndroidLocationService;
import wangdaye.com.geometricweather.data.service.location.BaiduIPLocationService;
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
                location.city = locationList.get(0).city;
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
                locationService.requestLocation(c, new RequestLocationListener(c, location, l));
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
            if (GeometricWeather.getInstance().getLocationService().equals("baidu_ip")) {
                weatherService.requestLocation(
                        c,
                        TextUtils.isEmpty(location.district) ? location.city : location.district,
                        new AccuLocationCallback(location, l));
            } else {
                weatherService.requestLocation(
                        c, location.lat, location.lon, new AccuLocationCallback(location, l));
            }
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
        void requestLocationSuccess(Location requestLocation);
        void requestLocationFailed(Location requestLocation);
    }
}