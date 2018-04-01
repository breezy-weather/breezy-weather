package wangdaye.com.geometricweather.service;

import android.support.annotation.Nullable;
import android.text.TextUtils;

import java.util.ArrayList;
import java.util.List;

import cyanogenmod.providers.WeatherContract;
import cyanogenmod.weather.RequestInfo;
import cyanogenmod.weather.WeatherInfo;
import cyanogenmod.weatherservice.ServiceRequest;
import cyanogenmod.weatherservice.ServiceRequestResult;
import cyanogenmod.weatherservice.WeatherProviderService;
import wangdaye.com.geometricweather.data.entity.model.Location;
import wangdaye.com.geometricweather.data.entity.model.weather.Weather;
import wangdaye.com.geometricweather.utils.helpter.LocationHelper;
import wangdaye.com.geometricweather.utils.helpter.WeatherHelper;
import wangdaye.com.geometricweather.utils.manager.TimeManager;

/**
 * CM weather provider service.
 * */

public class CMWeatherProviderService extends WeatherProviderService
        implements LocationHelper.OnRequestLocationListener,
        LocationHelper.OnRequestWeatherLocationListener, WeatherHelper.OnRequestWeatherListener {

    @Nullable
    private ServiceRequest request;

    private LocationHelper locationHelper;
    private WeatherHelper weatherHelper;

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
        locationHelper.requestLocation(this, Location.buildLocal(), this);
    }

    private void requestWeather(String cityName) {
        if (!TextUtils.isEmpty(cityName)) {
            locationHelper.requestWeatherLocation(this, cityName, false, this);
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

    // on request location listener.

    @Override
    public void requestLocationSuccess(Location requestLocation, boolean locationChanged) {
        if (request != null) {
            weatherHelper.requestWeather(this, requestLocation, this);
        }
    }

    @Override
    public void requestLocationFailed(Location requestLocation) {
        if (request != null) {
            request.fail();
        }
    }

    // on request weather location listener.

    @Override
    public void requestWeatherLocationSuccess(String query, List<Location> locationList) {
        if (request != null) {
            if (locationList != null && locationList.size() > 0) {
                weatherHelper.requestWeather(this, locationList.get(0), this);
            } else {
                requestWeatherLocationFailed(query);
            }
        }
    }

    @Override
    public void requestWeatherLocationFailed(String query) {
        if (request != null) {
            request.fail();
        }
    }

    // on request weather listener.

    @Override
    public void requestWeatherSuccess(Weather weather, Location requestLocation) {
        try {
            if (request != null) {
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
                                        weather.index.humidities[1]
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
    public void requestWeatherFailed(Location requestLocation) {
        if (request != null) {
            request.fail();
        }
    }
}

class WeatherConditionConvertHelper {
    static int getConditionCode(String weatherKind, boolean dayTime) {
        switch (weatherKind) {
            case WeatherHelper.KIND_CLEAR:
                if (dayTime) {
                    return WeatherContract.WeatherColumns.WeatherCode.SUNNY;
                } else {
                    return WeatherContract.WeatherColumns.WeatherCode.CLEAR_NIGHT;
                }

            case WeatherHelper.KIND_PARTLY_CLOUDY:
                if (dayTime) {
                    return WeatherContract.WeatherColumns.WeatherCode.PARTLY_CLOUDY_DAY;
                } else {
                    return WeatherContract.WeatherColumns.WeatherCode.PARTLY_CLOUDY_NIGHT;
                }

            case WeatherHelper.KIND_CLOUDY:
                return WeatherContract.WeatherColumns.WeatherCode.CLOUDY;

            case WeatherHelper.KIND_RAIN:
                return WeatherContract.WeatherColumns.WeatherCode.SHOWERS;

            case WeatherHelper.KIND_SNOW:
                return WeatherContract.WeatherColumns.WeatherCode.SNOW;

            case WeatherHelper.KIND_WIND:
                return WeatherContract.WeatherColumns.WeatherCode.WINDY;

            case WeatherHelper.KIND_FOG:
                return WeatherContract.WeatherColumns.WeatherCode.FOGGY;

            case WeatherHelper.KIND_HAZE:
                return WeatherContract.WeatherColumns.WeatherCode.HAZE;

            case WeatherHelper.KIND_SLEET:
                return WeatherContract.WeatherColumns.WeatherCode.SLEET;

            case WeatherHelper.KIND_HAIL:
                return WeatherContract.WeatherColumns.WeatherCode.HAIL;

            case WeatherHelper.KIND_THUNDER:
                return WeatherContract.WeatherColumns.WeatherCode.THUNDERSTORMS;

            case WeatherHelper.KIND_THUNDERSTORM:
                return WeatherContract.WeatherColumns.WeatherCode.THUNDERSHOWER;
        }
        return WeatherContract.WeatherColumns.WeatherCode.NOT_AVAILABLE;
    }
}
