package wangdaye.com.geometricweather.weather.converters;

import android.content.Context;
import android.graphics.Color;
import android.text.TextUtils;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

import us.dustinj.timezonemap.TimeZoneMap;
import wangdaye.com.geometricweather.common.basic.models.Location;
import wangdaye.com.geometricweather.common.basic.models.options.provider.WeatherSource;
import wangdaye.com.geometricweather.common.basic.models.weather.AirQuality;
import wangdaye.com.geometricweather.common.basic.models.weather.Alert;
import wangdaye.com.geometricweather.common.basic.models.weather.Astro;
import wangdaye.com.geometricweather.common.basic.models.weather.Base;
import wangdaye.com.geometricweather.common.basic.models.weather.Current;
import wangdaye.com.geometricweather.common.basic.models.weather.Daily;
import wangdaye.com.geometricweather.common.basic.models.weather.HalfDay;
import wangdaye.com.geometricweather.common.basic.models.weather.Hourly;
import wangdaye.com.geometricweather.common.basic.models.weather.Minutely;
import wangdaye.com.geometricweather.common.basic.models.weather.MoonPhase;
import wangdaye.com.geometricweather.common.basic.models.weather.Pollen;
import wangdaye.com.geometricweather.common.basic.models.weather.Precipitation;
import wangdaye.com.geometricweather.common.basic.models.weather.PrecipitationDuration;
import wangdaye.com.geometricweather.common.basic.models.weather.PrecipitationProbability;
import wangdaye.com.geometricweather.common.basic.models.weather.Temperature;
import wangdaye.com.geometricweather.common.basic.models.weather.UV;
import wangdaye.com.geometricweather.common.basic.models.weather.Weather;
import wangdaye.com.geometricweather.common.basic.models.weather.WeatherCode;
import wangdaye.com.geometricweather.common.basic.models.weather.Wind;
import wangdaye.com.geometricweather.common.basic.models.weather.WindDegree;
import wangdaye.com.geometricweather.common.utils.DisplayUtils;
import wangdaye.com.geometricweather.weather.json.owm.OwmAirPollutionResult;
import wangdaye.com.geometricweather.weather.json.owm.OwmLocationResult;
import wangdaye.com.geometricweather.weather.json.owm.OwmOneCallResult;
import wangdaye.com.geometricweather.weather.services.WeatherService;

public class OwmResultConverter {
    @NonNull
    public static List<Location> convert(List<OwmLocationResult> resultList) {
        List<Location> locationList = new ArrayList<>();
        if (resultList != null && resultList.size() != 0) {
            // Since we don't have timezones in the result, we need to initialize a TimeZoneMap
            // Since it takes a lot of time, we make boundaries
            // However, even then, it can take a lot of time, even on good performing smartphones.
            // TODO: To improve performances, create a Location() with a null TimeZone.
            // When clicking in the location search result on a specific location, if TimeZone is
            // null, then make a TimeZoneMap of the lat/lon and find its TimeZone
            double minLat = resultList.get(0).lat;
            double maxLat = resultList.get(0).lat + 0.00001;
            double minLon = resultList.get(0).lon;
            double maxLon = resultList.get(0).lon + 0.00001;
            for (OwmLocationResult r : resultList) {
                if (r.lat < minLat) {
                    minLat = r.lat;
                }
                if (r.lat > maxLat) {
                    maxLat = r.lat;
                }
                if (r.lon < minLon) {
                    minLon = r.lon;
                }
                if (r.lon > maxLon) {
                    maxLon = r.lon;
                }
            }
            TimeZoneMap map = TimeZoneMap.forRegion(minLat, minLon, maxLat, maxLon);
            for (OwmLocationResult r : resultList) {
                locationList.add(convert(null, r, map));
            }
        }
        return locationList;
    }

    @NonNull
    public static Location convert(@Nullable Location location, OwmLocationResult result) {
        TimeZoneMap map = TimeZoneMap.forRegion(result.lat, result.lon, result.lat + 0.00001, result.lon + 0.00001);
        return convert(location, result, map);
    }

    @NonNull
    public static Location convert(@Nullable Location location, OwmLocationResult result, TimeZoneMap map) {
        if (location != null
                && !TextUtils.isEmpty(location.getProvince())
                && !TextUtils.isEmpty(location.getCity())
                && !TextUtils.isEmpty(location.getDistrict())) {
            return new Location(
                    Double.toString(result.lat) + ',' + Double.toString(result.lon),
                    (float) result.lat,
                    (float) result.lon,
                    CommonConverterKt.getTimeZoneForPosition(map, result.lat, result.lon),
                    result.country,
                    "",
                    location.getCity(),
                    "",
                    null,
                    WeatherSource.OWM,
                    false,
                    false,
                    !TextUtils.isEmpty(result.country)
                            && (result.country.equals("CN")
                            || result.country.equals("cn")
                            || result.country.equals("HK")
                            || result.country.equals("hk")
                            || result.country.equals("TW")
                            || result.country.equals("tw"))
            );
        } else {
            return new Location(
                    Double.toString(result.lat) + ',' + Double.toString(result.lon),
                    (float) result.lat,
                    (float) result.lon,
                    CommonConverterKt.getTimeZoneForPosition(map, result.lat, result.lon),
                    result.country,
                    "",
                    result.name,
                    "",
                    null,
                    WeatherSource.OWM,
                    false,
                    false,
                    !TextUtils.isEmpty(result.country)
                            && (result.country.equals("CN")
                            || result.country.equals("cn")
                            || result.country.equals("HK")
                            || result.country.equals("hk")
                            || result.country.equals("TW")
                            || result.country.equals("tw"))
            );
        }
    }

    @NonNull
    public static WeatherService.WeatherResultWrapper convert(Context context,
                                                              Location location,
                                                              OwmOneCallResult oneCallResult,
                                                              @Nullable OwmAirPollutionResult airPollutionCurrentResult,
                                                              @Nullable OwmAirPollutionResult airPollutionForecastResult) {
        try {
            Weather weather = new Weather(
                    new Base(
                            location.getCityId(),
                            new Date(),
                            new Date()
                    ),
                    new Current(
                            oneCallResult.current.weather.get(0).description,
                            getWeatherCode(oneCallResult.current.weather.get(0).id),
                            new Temperature(
                                    toInt(oneCallResult.current.temp),
                                    toInt(oneCallResult.current.feelsLike),
                                    null,
                                    null,
                                    null,
                                    null,
                                    null
                            ),
                            new Precipitation(
                                    getTotalPrecipitation((oneCallResult.current.rain != null) ? oneCallResult.current.rain.cumul1h : null, (oneCallResult.current.snow != null) ? oneCallResult.current.snow.cumul1h : null),
                                    null,
                                    (oneCallResult.current.rain != null) ? oneCallResult.current.rain.cumul1h : null,
                                    (oneCallResult.current.snow != null) ? oneCallResult.current.snow.cumul1h : null,
                                    null
                            ),
                            new Wind(
                                    CommonConverterKt.getWindDirection(context, (float) oneCallResult.current.windDeg),
                                    new WindDegree((float) oneCallResult.current.windDeg, false),
                                    oneCallResult.current.windSpeed * 3.6f,
                                    CommonConverterKt.getWindLevel(context, oneCallResult.current.windSpeed * 3.6f)
                            ),
                            new UV(toInt(oneCallResult.current.uvi), null, null),
                            airPollutionCurrentResult == null ? null : new AirQuality(
                                    getAqiFromIndex(airPollutionCurrentResult.list.get(0).main.aqi),
                                    (float) airPollutionCurrentResult.list.get(0).components.pm2_5,
                                    (float) airPollutionCurrentResult.list.get(0).components.pm10,
                                    (float) airPollutionCurrentResult.list.get(0).components.so2,
                                    (float) airPollutionCurrentResult.list.get(0).components.no2,
                                    (float) airPollutionCurrentResult.list.get(0).components.o3,
                                    (float) airPollutionCurrentResult.list.get(0).components.co
                            ),
                            (float) oneCallResult.current.humidity,
                            (float) oneCallResult.current.pressure,
                            (float) (oneCallResult.current.visibility / 1000),
                            toInt(oneCallResult.current.dewPoint),
                            oneCallResult.current.clouds,
                            null,
                            null,
                            null
                    ),
                    null,
                    getDailyList(
                            context,
                            location.getTimeZone(),
                            oneCallResult.daily,
                            airPollutionForecastResult
                    ),
                    getHourlyList(
                            context,
                            oneCallResult.current.sunrise,
                            oneCallResult.current.sunset,
                            location.getTimeZone(),
                            oneCallResult.hourly
                    ),
                    getMinutelyList(
                            oneCallResult.current.sunrise,
                            oneCallResult.current.sunset,
                            oneCallResult.minutely
                    ),
                    getAlertList(oneCallResult.alerts)
            );
            return new WeatherService.WeatherResultWrapper(weather);
        } catch (Exception ignored) {
            return new WeatherService.WeatherResultWrapper(null);
        }
    }

    private static List<Daily> getDailyList(Context context, TimeZone timeZone,
                                            List<OwmOneCallResult.Daily> dailyResult,
                                            @Nullable OwmAirPollutionResult airPollutionForecastResult) {
        List<Daily> dailyList = new ArrayList<>(dailyResult.size());

        for (OwmOneCallResult.Daily forecasts : dailyResult) {
            dailyList.add(
                    new Daily(
                            new Date(forecasts.dt * 1000),
                            new HalfDay(
                                    forecasts.weather.get(0).description,
                                    forecasts.weather.get(0).description,
                                    getWeatherCode(forecasts.weather.get(0).id),
                                    new Temperature(
                                            toInt(forecasts.temp.day),
                                            toInt(forecasts.feelsLike.day),
                                            null,
                                            null,
                                            null,
                                            null,
                                            null
                                    ),
                                    new Precipitation(
                                            getTotalPrecipitation(getPrecipitationForDaily(forecasts.rain), getPrecipitationForDaily(forecasts.snow)),
                                            null,
                                            getPrecipitationForDaily(forecasts.rain),
                                            getPrecipitationForDaily(forecasts.snow),
                                            null
                                    ),
                                    new PrecipitationProbability(
                                            forecasts.pop,
                                            null,
                                            null,
                                            null,
                                            null
                                    ),
                                    new PrecipitationDuration(
                                            null,
                                            null,
                                            null,
                                            null,
                                            null
                                    ),
                                    new Wind(
                                            CommonConverterKt.getWindDirection(context, (float) forecasts.windDeg),
                                            new WindDegree((float) forecasts.windDeg, false),
                                            forecasts.windSpeed * 3.6f,
                                            CommonConverterKt.getWindLevel(context, forecasts.windSpeed * 3.6f)
                                    ),
                                    forecasts.clouds
                            ),
                            new HalfDay(
                                    forecasts.weather.get(0).description,
                                    forecasts.weather.get(0).description,
                                    getWeatherCode(forecasts.weather.get(0).id),
                                    new Temperature(
                                            toInt(forecasts.temp.night),
                                            toInt(forecasts.feelsLike.night),
                                            null,
                                            null,
                                            null,
                                            null,
                                            null
                                    ),
                                    new Precipitation(
                                            getTotalPrecipitation(getPrecipitationForDaily(forecasts.rain), getPrecipitationForDaily(forecasts.snow)),
                                            null,
                                            getPrecipitationForDaily(forecasts.rain),
                                            getPrecipitationForDaily(forecasts.snow),
                                            null
                                    ),
                                    new PrecipitationProbability(
                                            forecasts.pop,
                                            null,
                                            null,
                                            null,
                                            null
                                    ),
                                    new PrecipitationDuration(
                                            null,
                                            null,
                                            null,
                                            null,
                                            null
                                    ),
                                    new Wind(
                                            CommonConverterKt.getWindDirection(context, (float) forecasts.windDeg),
                                            new WindDegree((float) forecasts.windDeg, false),
                                            forecasts.windSpeed * 3.6f,
                                            CommonConverterKt.getWindLevel(context, forecasts.windSpeed * 3.6f)
                                    ),
                                    forecasts.clouds
                            ),
                            new Astro(new Date(forecasts.sunrise * 1000), new Date(forecasts.sunset * 1000)),
                            new Astro(new Date(forecasts.moonrise * 1000), new Date(forecasts.moonset * 1000)),
                            new MoonPhase(null, null),
                            getAirQuality(new Date(forecasts.dt * 1000), timeZone, airPollutionForecastResult),
                            new Pollen(null, null, null, null, null, null, null, null, null, null, null, null),
                            new UV(toInt(forecasts.uvi), null, null),
                            0.0f
                    )
            );
        }
        return dailyList;
    }

    // Function that divide by two the precipitation for daily because it's a 24 hour forecast and we only need half day
    private static Float getPrecipitationForDaily(Float precipitation) {
        return (precipitation != null) ? (precipitation / 2) : null;
    }

    // Function that checks for null before sum up
    private static Float getTotalPrecipitation(Float rain, Float snow) {
        if (rain == null) {
            return snow;
        }
        if (snow == null) {
            return rain;
        }

        return rain + snow;
    }

    private static List<Hourly> getHourlyList(Context context, long sunrise, long sunset, TimeZone timeZone, List<OwmOneCallResult.Hourly> resultList) {
        List<Hourly> hourlyList = new ArrayList<>(resultList.size());
        for (OwmOneCallResult.Hourly result : resultList) {
            hourlyList.add(
                    new Hourly(
                            new Date(result.dt * 1000),
                            CommonConverterKt.isDaylight(new Date(sunrise * 1000), new Date(sunset * 1000), new Date(result.dt * 1000), timeZone),
                            result.weather.get(0).main,
                            getWeatherCode(result.weather.get(0).id),
                            new Temperature(
                                    toInt(result.temp),
                                    toInt(result.feelsLike),
                                    null,
                                    null,
                                    null,
                                    null,
                                    null
                            ),
                            new Precipitation(
                                    getTotalPrecipitation((result.rain != null) ? result.rain.cumul1h : null, (result.snow != null) ? result.snow.cumul1h : null),
                                    null,
                                    (result.rain != null) ? result.rain.cumul1h : null,
                                    (result.snow != null) ? result.snow.cumul1h : null,
                                    null
                            ),
                            new PrecipitationProbability(
                                    result.pop,
                                    null,
                                    null,
                                    null,
                                    null
                            ),
                            new Wind(
                                    CommonConverterKt.getWindDirection(context, (float) result.windDeg),
                                    new WindDegree((float) result.windDeg, false),
                                    result.windSpeed * 3.6f,
                                    CommonConverterKt.getWindLevel(context, result.windSpeed * 3.6f)
                            ),
                            null,
                            null,
                            new UV(toInt(result.uvi), null, null)
                    )
            );
        }
        return hourlyList;
    }

    private static List<Minutely> getMinutelyList(long sunrise, long sunset,
                                                  @Nullable List<OwmOneCallResult.Minutely> minuteResult) {
        //if (minuteResult == null) {
            return new ArrayList<>();
        /*}
        List<Minutely> minutelyList = new ArrayList<>(minuteResult.size());
        for (OwmOneCallResult.Minutely interval : minuteResult) {
            minutelyList.add(
                    new Minutely(
                            interval.StartDateTime,
                            interval.StartEpochDateTime,
                            CommonConverter.isDaylight(new Date(sunrise * 1000), new Date(sunset * 1000), interval.StartDateTime),
                            interval.ShortPhrase,
                            getWeatherCode(interval.IconCode),
                            interval.Minute,
                            toInt(interval.Dbz),
                            interval.CloudCover
                    )
            );
        }
        return minutelyList;*/
    }

    private static Integer getAqiFromIndex (Integer aqi) {
        if (aqi == null || aqi <= 0) {
            return null;
        } if (aqi <= AirQuality.AQI_INDEX_1) {
            return AirQuality.AQI_INDEX_1;
        } else if (aqi <= AirQuality.AQI_INDEX_2) {
            return AirQuality.AQI_INDEX_2;
        } else if (aqi <= AirQuality.AQI_INDEX_3) {
            return AirQuality.AQI_INDEX_3;
        } else if (aqi <= AirQuality.AQI_INDEX_4) {
            return AirQuality.AQI_INDEX_4;
        } else if (aqi <= AirQuality.AQI_INDEX_5) {
            return AirQuality.AQI_INDEX_5;
        } else {
            return 400;
        }
    }

    private static @Nullable AirQuality getAirQuality(Date requestedDate, TimeZone timeZone, @Nullable OwmAirPollutionResult owmAirPollutionForecastResult) {
        if (owmAirPollutionForecastResult != null) {
            for (OwmAirPollutionResult.AirPollution airPollutionForecast : owmAirPollutionForecastResult.list) {
                if (DisplayUtils.getFormattedDate(requestedDate, timeZone, "yyyyMMdd")
                        .equals(DisplayUtils.getFormattedDate(new Date(airPollutionForecast.dt * 1000), timeZone, "yyyyMMdd"))) {
                    return new AirQuality(
                            getAqiFromIndex(airPollutionForecast.main.aqi),
                            (float) airPollutionForecast.components.pm2_5,
                            (float) airPollutionForecast.components.pm10,
                            (float) airPollutionForecast.components.so2,
                            (float) airPollutionForecast.components.no2,
                            (float) airPollutionForecast.components.o3,
                            (float) airPollutionForecast.components.co
                    );
                }
            }
        }
        return null;
    }

    private static List<Alert> getAlertList(@Nullable List<OwmOneCallResult.Alert> resultList) {
        int i = 0;
        if (resultList != null) {
            List<Alert> alertList = new ArrayList<>(resultList.size());
            for (OwmOneCallResult.Alert result : resultList) {
                alertList.add(
                        new Alert(
                                i, // Does not exist
                                new Date(result.start * 1000),
                                result.start * 1000,
                                result.event,
                                result.description,
                                result.event,
                                1, // Does not exist
                                Color.rgb(255, 184, 43) // Defaulting to orange as we don't know
                        )
                );
                ++i;
            }
            Alert.deduplication(alertList);
            Alert.descByTime(alertList);
            return alertList;
        } else {
            return new ArrayList<>();
        }
    }

    private static int toInt(double value) {
        return (int) Math.round(value);
    }

    private static WeatherCode getWeatherCode(int icon) {
        if (icon == 200 || icon == 201 || icon == 202) {
            return WeatherCode.THUNDERSTORM;
        } else if (icon == 210 || icon == 211 || icon == 212) {
            return WeatherCode.THUNDER;
        } else if (icon == 221 || icon == 230 || icon == 231 || icon == 232) {
            return WeatherCode.THUNDERSTORM;
        } else if (icon == 300 || icon == 301 || icon == 302
                || icon == 310 || icon == 311 || icon == 312
                || icon == 313 || icon == 314 || icon == 321) {
            return WeatherCode.RAIN;
        } else if (icon == 500 || icon == 501 || icon == 502
                || icon == 503 || icon == 504) {
            return WeatherCode.RAIN;
        } else if (icon == 511) {
            return WeatherCode.SLEET;
        } else if (icon == 600 || icon == 601 || icon == 602) {
            return WeatherCode.SNOW;
        } else if (icon == 611 || icon == 612 || icon == 613
                || icon == 614 || icon == 615 || icon == 616) {
            return WeatherCode.SLEET;
        } else if (icon == 620 || icon == 621 || icon == 622) {
            return WeatherCode.SNOW;
        } else if (icon == 701 || icon == 711 || icon == 721 || icon == 731) {
            return WeatherCode.HAZE;
        } else if (icon == 741) {
            return WeatherCode.FOG;
        } else if (icon == 751 || icon == 761 || icon == 762) {
            return WeatherCode.HAZE;
        } else if (icon == 771 || icon == 781) {
            return WeatherCode.WIND;
        } else if (icon == 800) {
            return WeatherCode.CLEAR;
        } else if (icon == 801 || icon == 802) {
            return WeatherCode.PARTLY_CLOUDY;
        } else if (icon == 803 || icon == 804) {
            return WeatherCode.CLOUDY;
        } else {
            return WeatherCode.CLOUDY;
        }
    }
}