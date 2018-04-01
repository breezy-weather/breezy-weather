package wangdaye.com.geometricweather.data.service.weather;

import android.content.Context;
import android.support.annotation.Nullable;
import android.text.TextUtils;

import java.util.List;
import java.util.regex.Pattern;

import wangdaye.com.geometricweather.data.entity.model.Location;
import wangdaye.com.geometricweather.utils.helpter.LocationHelper;
import wangdaye.com.geometricweather.utils.helpter.WeatherHelper;

/**
 * Weather service.
 * */

public class WeatherService {

    private AccuWeatherService accu;
    private CNWeatherService cn;

    public WeatherService() {
        accu = AccuWeatherService.getService();
        cn = CNWeatherService.getService();
    }

    public WeatherService requestWeather(final Context c, final Location location,
                                         final WeatherHelper.OnRequestWeatherListener l) {
        if (isChinese(location.city)) {
            cn.requestLocation(
                    c, new String[] {location.city}, false,
                    new LocationHelper.OnRequestWeatherLocationListener() {
                        @Override
                        public void requestWeatherLocationSuccess(String query, List<Location> locationList) {
                            location.cityId = locationList.get(0).cityId;
                            cn.requestWeather(c, location, l);
                        }

                        @Override
                        public void requestWeatherLocationFailed(String query) {
                            accu.requestWeather(c, location, l);
                        }
                    });
        } else {
            accu.requestWeather(c, location, l);
        }
        return this;
    }

    public WeatherService requestLocation(final Context c,
                                          @Nullable final String[] queries,
                                          @Nullable final String lat, @Nullable final String lon,
                                          final boolean fuzzy,
                                          final LocationHelper.OnRequestWeatherLocationListener l) {
        if (queries != null && queries.length > 0) {
            if (isChinese(queries[0])) {
                cn.requestLocation(
                        c, queries, fuzzy,
                        new LocationHelper.OnRequestWeatherLocationListener() {
                            @Override
                            public void requestWeatherLocationSuccess(String query, List<Location> locationList) {
                                if (l != null) {
                                    l.requestWeatherLocationSuccess(query, locationList);
                                }
                            }

                            @Override
                            public void requestWeatherLocationFailed(String query) {
                                if (TextUtils.isEmpty(lat) || TextUtils.isEmpty(lon)) {
                                    accu.requestLocation(c, queries[0], l);
                                } else {
                                    accu.requestLocationByGeoPosition(c, lat, lon, l);
                                }
                            }
                        });
            } else {
                accu.requestLocation(c, queries[0], l);
            }
        } else if (!TextUtils.isEmpty(lat) && !TextUtils.isEmpty(lon)) {
            accu.requestLocationByGeoPosition(c, lat, lon, l);
        }
        return this;
    }

    private boolean isChinese(String text) {
        char[] chars = text.toCharArray();
        for (char c : chars) {
            if (!Pattern.compile("[\u4e00-\u9fa5]").matcher(String.valueOf(c)).matches()) {
                return false;
            }
        }
        return true;
    }

    public void cancel() {
        if (accu != null) {
            accu.cancel();
        }
    }

    public static WeatherService getService() {
        return new WeatherService();
    }
}
