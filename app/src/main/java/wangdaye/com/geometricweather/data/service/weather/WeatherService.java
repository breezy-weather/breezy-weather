package wangdaye.com.geometricweather.data.service.weather;

import android.content.Context;
import android.support.annotation.Nullable;
import android.text.TextUtils;

import java.util.List;

import wangdaye.com.geometricweather.GeometricWeather;
import wangdaye.com.geometricweather.data.entity.model.Location;
import wangdaye.com.geometricweather.utils.LanguageUtils;
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
        if (LanguageUtils.isChinese(location.city)
                && GeometricWeather.getInstance().getChineseSource().equals("cn")) {
            cn.requestLocation(
                    c, new String[] {location.city, location.prov}, false,
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
            if (LanguageUtils.isChinese(queries[0])
                    && GeometricWeather.getInstance().getChineseSource().equals("cn")) {
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

    public WeatherService fuzzyRequestLocation(final Context c, final String query,
                                               final LocationHelper.OnRequestWeatherLocationListener l) {
        if (LanguageUtils.isChinese(query)
                && GeometricWeather.getInstance().getChineseSource().equals("cn")) {
            cn.requestLocation(
                    c, new String[] {query}, true,
                    new LocationHelper.OnRequestWeatherLocationListener() {
                        @Override
                        public void requestWeatherLocationSuccess(String query, List<Location> locationList) {
                            if (l != null) {
                                l.requestWeatherLocationSuccess(query, locationList);
                            }
                        }

                        @Override
                        public void requestWeatherLocationFailed(String query) {
                            accu.requestLocation(c, query, l);
                        }
                    });
        } else {
            accu.requestLocation(c, query, l);
        }
        return this;
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
