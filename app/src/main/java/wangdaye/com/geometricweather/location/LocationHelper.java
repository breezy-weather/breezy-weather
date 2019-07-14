package wangdaye.com.geometricweather.location;

import android.content.Context;
import androidx.annotation.NonNull;

import java.util.List;

import wangdaye.com.geometricweather.basic.model.Location;
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
 * Location utils.
 * */

public class LocationHelper {

    private LocationService locationService;
    private WeatherService weatherService;

    private class AccuLocationCallback implements WeatherService.RequestLocationCallback {
        private Context context;
        private Location location;
        private OnRequestLocationListener listener;

        AccuLocationCallback(Context context, Location location,
                             @NonNull OnRequestLocationListener l) {
            this.context = context;
            this.location = location;
            this.listener = l;
        }

        @Override
        public void requestLocationSuccess(String query, List<Location> locationList) {
            if (locationList.size() > 0) {
                Location location = locationList.get(0).setLocal();
                DatabaseHelper.getInstance(context).writeLocation(location);
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

    private class CNLocationCallback implements WeatherService.RequestLocationCallback {
        private Context context;
        private Location location;
        private OnRequestLocationListener listener;

        CNLocationCallback(Context context, Location location,
                           @NonNull OnRequestLocationListener l) {
            this.context = context;
            this.location = location;
            this.listener = l;
        }

        @Override
        public void requestLocationSuccess(String query, List<Location> locationList) {
            if (locationList.size() > 0) {
                location.cityId = locationList.get(0).cityId;
                location.setLocal();
                DatabaseHelper.getInstance(context).writeLocation(location);
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
        switch (SettingsOptionManager.getInstance(context).getLocationService()) {
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

    public void requestLocation(Context context, Location location,
                                @NonNull OnRequestLocationListener l) {
        if (!NetworkUtils.isAvailable(context)) {
            l.requestLocationFailed(location);
            return;
        }

        // 1. get location by location service.
        // 2. get available location by weather service.

        String chineseSource = SettingsOptionManager.getInstance(context).getChineseSource();
        WeatherService service;
        if (chineseSource.equals("accu")) {
            service = new AccuWeatherService();
        } else if (chineseSource.equals("cn")) {
            service = new CNWeatherService();
        } else {
            service = new CaiYunWeatherService();
        }

        locationService.requestLocation(
                context,
                service.needGeocodeInformation(),
                result -> {
                    if (result == null) {
                        l.requestLocationFailed(location);
                        return;
                    }

                    location.lat = result.latitude;
                    location.lon = result.longitude;

                    location.district = result.district;
                    location.city = result.city;
                    location.province = result.province;
                    location.country = result.country;

                    location.local = true;
                    location.china = result.inChina;

                    requestAvailableWeatherLocation(context, location, l);
                }
        );
    }

    private void requestAvailableWeatherLocation(Context context,
                                                 @NonNull Location location,
                                                 @NonNull OnRequestLocationListener l) {
        String chineseSource = SettingsOptionManager.getInstance(context).getChineseSource();
        if (!location.canUseChineseSource() || chineseSource.equals("accu")) {
            // use accu as weather service api.
            location.source = "accu";
            weatherService = new AccuWeatherService();
            weatherService.requestLocation(context, location, new AccuLocationCallback(context, location, l));
        } else if (chineseSource.equals("cn")) {
            // use cn weather net as the weather service api.
            location.source = "cn";
            weatherService = new CNWeatherService();
            weatherService.requestLocation(context, location, new CNLocationCallback(context, location, l));
        } else {
            // caiyun.
            location.source = "caiyun";
            weatherService = new CaiYunWeatherService();
            weatherService.requestLocation(context, location, new CNLocationCallback(context, location, l));
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

    public String[] getPermissions() {
        return locationService.getPermissions();
    }

    // interface.

    public interface OnRequestLocationListener {
        void requestLocationSuccess(Location requestLocation);
        void requestLocationFailed(Location requestLocation);
    }
}