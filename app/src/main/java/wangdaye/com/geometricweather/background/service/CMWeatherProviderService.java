package wangdaye.com.geometricweather.background.service;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import android.text.TextUtils;

import java.util.ArrayList;
import java.util.List;

import cyanogenmod.providers.WeatherContract;
import cyanogenmod.weather.RequestInfo;
import cyanogenmod.weather.WeatherInfo;
import cyanogenmod.weatherservice.ServiceRequest;
import cyanogenmod.weatherservice.ServiceRequestResult;
import cyanogenmod.weatherservice.WeatherProviderService;
import wangdaye.com.geometricweather.basic.model.History;
import wangdaye.com.geometricweather.basic.model.Location;
import wangdaye.com.geometricweather.basic.model.weather.Weather;
import wangdaye.com.geometricweather.location.LocationHelper;
import wangdaye.com.geometricweather.weather.WeatherHelper;
import wangdaye.com.geometricweather.utils.manager.TimeManager;

/**
 * CM weather provider service.
 * */

public class CMWeatherProviderService extends WeatherProviderService
        implements WeatherHelper.OnRequestWeatherListener {

    @Nullable
    private ServiceRequest request;

    private LocationHelper locationHelper;
    private WeatherHelper weatherHelper;

    private LocationHelper.OnRequestLocationListener locationListener
            = new LocationHelper.OnRequestLocationListener() {

        @Override
        public void requestLocationSuccess(Location requestLocation) {
            if (request != null) {
                weatherHelper.requestWeather(
                        CMWeatherProviderService.this,
                        requestLocation,
                        CMWeatherProviderService.this);
            }
        }

        @Override
        public void requestLocationFailed(Location requestLocation) {
            if (request != null) {
                request.fail();
            }
        }
    };

    private WeatherHelper.OnRequestLocationListener weatherLocationListener
            = new WeatherHelper.OnRequestLocationListener() {
        @Override
        public void requestLocationSuccess(String query, List<Location> locationList) {
            if (request != null) {
                if (locationList != null && locationList.size() > 0) {
                    weatherHelper.requestWeather(
                            CMWeatherProviderService.this,
                            locationList.get(0),
                            CMWeatherProviderService.this);
                } else {
                    requestLocationFailed(query);
                }
            }
        }

        @Override
        public void requestLocationFailed(String query) {
            if (request != null) {
                request.fail();
            }
        }
    };

    @Override
    public void onCreate() {
        super.onCreate();
        request = null;
        locationHelper = new LocationHelper(this);
        weatherHelper = new WeatherHelper();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        cancelRequest();
    }

    @Override
    protected void onRequestSubmitted(ServiceRequest serviceRequest) {
        cancelRequest();
        request = serviceRequest;

        RequestInfo info = serviceRequest.getRequestInfo();
        switch (info.getRequestType()) {
            case RequestInfo.TYPE_WEATHER_BY_WEATHER_LOCATION_REQ:
                requestWeather(info.getWeatherLocation().getCity());
                break;

            case RequestInfo.TYPE_WEATHER_BY_GEO_LOCATION_REQ:
                requestLocation();
                break;

            case RequestInfo.TYPE_LOOKUP_CITY_NAME_REQ:
                requestWeather(info.getCityName());
                break;

            default:
                serviceRequest.fail();
                break;
        }
    }

    @Override
    protected void onRequestCancelled(ServiceRequest serviceRequest) {
        cancelRequest();
    }

    // control.

    private void requestLocation() {
        locationHelper.requestLocation(this, Location.buildLocal(), locationListener);
    }

    private void requestWeather(String cityName) {
        if (!TextUtils.isEmpty(cityName)) {
            weatherHelper.requestLocation(this, cityName, weatherLocationListener);
        } else if (request != null) {
            request.fail();
        }
    }

    private void cancelRequest() {
        request = null;
        locationHelper.cancel();
        weatherHelper.cancel();
    }

    // interface.

    // on request weather listener.

    @Override
    public void requestWeatherSuccess(@Nullable Weather weather, @Nullable History history,
                                      @NonNull Location requestLocation) {
        try {
            if (request != null && weather != null) {
                List<WeatherInfo.DayForecast> forecastList = new ArrayList<>();
                for (int i = 0; i < weather.dailyList.size(); i ++) {
                    forecastList.add(
                            new WeatherInfo.DayForecast.Builder(
                                    WeatherConditionConvertHelper.getConditionCode(
                                            weather.dailyList.get(i).weatherKinds[0],
                                            true))
                                    .setHigh(weather.dailyList.get(i).temps[0])
                                    .setLow(weather.dailyList.get(i).temps[1])
                                    .build());
                }
                WeatherInfo.Builder builder = new WeatherInfo.Builder(
                        weather.base.city,
                        weather.realTime.temp,
                        WeatherContract.WeatherColumns.TempUnit.CELSIUS)
                        .setWeatherCondition(
                                WeatherConditionConvertHelper.getConditionCode(
                                        weather.realTime.weatherKind,
                                        TimeManager.getInstance(this)
                                                .getDayTime(this, weather, false)
                                                .isDayTime()))
                        .setTodaysHigh(weather.dailyList.get(0).temps[0])
                        .setTodaysLow(weather.dailyList.get(0).temps[1])
                        .setTimestamp(weather.base.timeStamp)
                        .setHumidity(
                                Double.parseDouble(
                                        weather.index.humidity
                                                .split(" : ")[1]
                                                .split("%")[0]))
                        .setWind(
                                Double.parseDouble(weather.realTime.windSpeed.split("km/h")[0]),
                                weather.realTime.windDegree,
                                WeatherContract.WeatherColumns.WindSpeedUnit.KPH)
                        .setForecast(forecastList);

                request.complete(new ServiceRequestResult.Builder(builder.build()).build());
            }
        } catch (Exception ignore) {
            requestWeatherFailed(requestLocation);
        }
    }

    @Override
    public void requestWeatherFailed(@NonNull Location requestLocation) {
        if (request != null) {
            request.fail();
        }
    }
}

class WeatherConditionConvertHelper {
    static int getConditionCode(String weatherKind, boolean dayTime) {
        switch (weatherKind) {
            case Weather.KIND_CLEAR:
                if (dayTime) {
                    return WeatherContract.WeatherColumns.WeatherCode.SUNNY;
                } else {
                    return WeatherContract.WeatherColumns.WeatherCode.CLEAR_NIGHT;
                }

            case Weather.KIND_PARTLY_CLOUDY:
                if (dayTime) {
                    return WeatherContract.WeatherColumns.WeatherCode.PARTLY_CLOUDY_DAY;
                } else {
                    return WeatherContract.WeatherColumns.WeatherCode.PARTLY_CLOUDY_NIGHT;
                }

            case Weather.KIND_CLOUDY:
                return WeatherContract.WeatherColumns.WeatherCode.CLOUDY;

            case Weather.KIND_RAIN:
                return WeatherContract.WeatherColumns.WeatherCode.SHOWERS;

            case Weather.KIND_SNOW:
                return WeatherContract.WeatherColumns.WeatherCode.SNOW;

            case Weather.KIND_WIND:
                return WeatherContract.WeatherColumns.WeatherCode.WINDY;

            case Weather.KIND_FOG:
                return WeatherContract.WeatherColumns.WeatherCode.FOGGY;

            case Weather.KIND_HAZE:
                return WeatherContract.WeatherColumns.WeatherCode.HAZE;

            case Weather.KIND_SLEET:
                return WeatherContract.WeatherColumns.WeatherCode.SLEET;

            case Weather.KIND_HAIL:
                return WeatherContract.WeatherColumns.WeatherCode.HAIL;

            case Weather.KIND_THUNDER:
                return WeatherContract.WeatherColumns.WeatherCode.THUNDERSTORMS;

            case Weather.KIND_THUNDERSTORM:
                return WeatherContract.WeatherColumns.WeatherCode.THUNDERSHOWER;
        }
        return WeatherContract.WeatherColumns.WeatherCode.NOT_AVAILABLE;
    }
}
