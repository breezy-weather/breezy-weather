package wangdaye.com.geometricweather.background.interfaces;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import android.text.TextUtils;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import cyanogenmod.providers.WeatherContract;
import cyanogenmod.weather.RequestInfo;
import cyanogenmod.weather.WeatherInfo;
import cyanogenmod.weatherservice.ServiceRequest;
import cyanogenmod.weatherservice.ServiceRequestResult;
import cyanogenmod.weatherservice.WeatherProviderService;
import dagger.hilt.android.AndroidEntryPoint;
import wangdaye.com.geometricweather.common.basic.models.Location;
import wangdaye.com.geometricweather.common.basic.models.options.provider.WeatherSource;
import wangdaye.com.geometricweather.common.basic.models.weather.Weather;
import wangdaye.com.geometricweather.common.basic.models.weather.WeatherCode;
import wangdaye.com.geometricweather.location.LocationHelper;
import wangdaye.com.geometricweather.settings.SettingsManager;
import wangdaye.com.geometricweather.weather.WeatherHelper;

/**
 * CM weather provider service.
 * */

@AndroidEntryPoint
public class CMWeatherProviderService extends WeatherProviderService
        implements WeatherHelper.OnRequestWeatherListener {

    @Nullable
    private ServiceRequest mRequest;

    @Inject LocationHelper mLocationHelper;
    @Inject WeatherHelper mWeatherHelper;

    private final LocationHelper.OnRequestLocationListener locationListener
            = new LocationHelper.OnRequestLocationListener() {

        @Override
        public void requestLocationSuccess(Location requestLocation) {
            if (mRequest != null) {
                mWeatherHelper.requestWeather(
                        CMWeatherProviderService.this,
                        requestLocation,
                        CMWeatherProviderService.this);
            }
        }

        @Override
        public void requestLocationFailed(Location requestLocation) {
            if (mRequest != null) {
                mRequest.fail();
            }
        }
    };

    private final WeatherHelper.OnRequestLocationListener weatherLocationListener
            = new WeatherHelper.OnRequestLocationListener() {
        @Override
        public void requestLocationSuccess(String query, List<Location> locationList) {
            if (mRequest != null) {
                if (locationList != null && locationList.size() > 0) {
                    mWeatherHelper.requestWeather(
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
            if (mRequest != null) {
                mRequest.fail();
            }
        }
    };

    @Override
    public void onCreate() {
        super.onCreate();
        mRequest = null;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        cancelRequest();
    }

    @Override
    protected void onRequestSubmitted(ServiceRequest serviceRequest) {
        cancelRequest();
        mRequest = serviceRequest;

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
        mLocationHelper.requestLocation(
                this,
                Location.buildLocal(),
                true,
                locationListener
        );
    }

    private void requestWeather(String cityName) {
        if (!TextUtils.isEmpty(cityName)) {
            List<WeatherSource> list = new ArrayList<>();
            list.add(SettingsManager.getInstance(this).getWeatherSource());
            mWeatherHelper.requestLocation(this, cityName, list, weatherLocationListener);
        } else if (mRequest != null) {
            mRequest.fail();
        }
    }

    private void cancelRequest() {
        mRequest = null;
        mLocationHelper.cancel();
        mWeatherHelper.cancel();
    }

    // interface.

    // on request weather listener.

    @Override
    public void requestWeatherSuccess(@NonNull Location requestLocation) {
        try {
            Weather weather = requestLocation.getWeather();
            if (mRequest != null && weather != null) {
                List<WeatherInfo.DayForecast> forecastList = new ArrayList<>();
                for (int i = 0; i < weather.getDailyForecast().size(); i ++) {
                    forecastList.add(
                            new WeatherInfo.DayForecast.Builder(
                                    WeatherConditionConvertHelper.getConditionCode(
                                            weather.getDailyForecast().get(i).day().getWeatherCode(),
                                            true
                                    ))
                                    .setHigh(weather.getDailyForecast().get(i).day().getTemperature().getTemperature())
                                    .setHigh(weather.getDailyForecast().get(i).night().getTemperature().getTemperature())
                                    .build()
                    );
                }
                WeatherInfo.Builder builder = new WeatherInfo.Builder(
                        requestLocation.getCityName(getApplicationContext()),
                        weather.getCurrent().getTemperature().getTemperature(),
                        WeatherContract.WeatherColumns.TempUnit.CELSIUS
                ).setWeatherCondition(
                                WeatherConditionConvertHelper.getConditionCode(
                                        weather.getCurrent().getWeatherCode(),
                                        requestLocation.isDaylight()
                                )
                ).setTodaysHigh(
                        weather.getDailyForecast().get(0).day().getTemperature().getTemperature()
                ).setTodaysLow(
                        weather.getDailyForecast().get(0).night().getTemperature().getTemperature()
                ).setTimestamp(
                        weather.getBase().getTimeStamp()
                );
                if (weather.getCurrent().getRelativeHumidity() != null) {
                    builder.setHumidity(weather.getCurrent().getRelativeHumidity());
                }
                if (weather.getCurrent().getWind().getSpeed() != null) {
                    builder.setWind(
                            weather.getCurrent().getWind().getSpeed(),
                            weather.getCurrent().getWind().getDegree().getDegree(),
                            WeatherContract.WeatherColumns.WindSpeedUnit.KPH
                    ).setForecast(forecastList);
                }

                mRequest.complete(new ServiceRequestResult.Builder(builder.build()).build());
            }
        } catch (Exception ignore) {
            requestWeatherFailed(requestLocation, false);
        }
    }

    @Override
    public void requestWeatherFailed(@NonNull Location requestLocation, @NonNull Boolean apiLimitReached) {
        if (mRequest != null) {
            mRequest.fail();
        }
    }
}

class WeatherConditionConvertHelper {
    static int getConditionCode(WeatherCode code, boolean dayTime) {
        switch (code) {
            case CLEAR:
                if (dayTime) {
                    return WeatherContract.WeatherColumns.WeatherCode.SUNNY;
                } else {
                    return WeatherContract.WeatherColumns.WeatherCode.CLEAR_NIGHT;
                }

            case PARTLY_CLOUDY:
                if (dayTime) {
                    return WeatherContract.WeatherColumns.WeatherCode.PARTLY_CLOUDY_DAY;
                } else {
                    return WeatherContract.WeatherColumns.WeatherCode.PARTLY_CLOUDY_NIGHT;
                }

            case CLOUDY:
                return WeatherContract.WeatherColumns.WeatherCode.CLOUDY;

            case RAIN:
                return WeatherContract.WeatherColumns.WeatherCode.SHOWERS;

            case SNOW:
                return WeatherContract.WeatherColumns.WeatherCode.SNOW;

            case WIND:
                return WeatherContract.WeatherColumns.WeatherCode.WINDY;

            case FOG:
                return WeatherContract.WeatherColumns.WeatherCode.FOGGY;

            case HAZE:
                return WeatherContract.WeatherColumns.WeatherCode.HAZE;

            case SLEET:
                return WeatherContract.WeatherColumns.WeatherCode.SLEET;

            case HAIL:
                return WeatherContract.WeatherColumns.WeatherCode.HAIL;

            case THUNDER:
                return WeatherContract.WeatherColumns.WeatherCode.THUNDERSTORMS;

            case THUNDERSTORM:
                return WeatherContract.WeatherColumns.WeatherCode.THUNDERSHOWER;
        }
        return WeatherContract.WeatherColumns.WeatherCode.NOT_AVAILABLE;
    }
}
