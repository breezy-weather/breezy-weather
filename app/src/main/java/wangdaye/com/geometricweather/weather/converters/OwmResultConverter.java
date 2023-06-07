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
                            && (result.country.equalsIgnoreCase("cn")
                            || result.country.equalsIgnoreCase("hk")
                            || result.country.equalsIgnoreCase("tw"))
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
                            && (result.country.equalsIgnoreCase("cn")
                            || result.country.equalsIgnoreCase("hk")
                            || result.country.equalsIgnoreCase("tw"))
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
                                    null,
                                    null,
                                    toInt(oneCallResult.current.feelsLike),
                                    null,
                                    null,
                                    null
                            ),
                            new Wind(
                                    CommonConverterKt.getWindDirection(context, (float) oneCallResult.current.windDeg),
                                    new WindDegree((float) oneCallResult.current.windDeg, false),
                                    oneCallResult.current.windSpeed * 3.6f,
                                    CommonConverterKt.getWindLevel(context, oneCallResult.current.windSpeed * 3.6f)
                            ),
                            new UV(toInt(oneCallResult.current.uvi), CommonConverterKt.getUVLevel(context, toInt(oneCallResult.current.uvi)), null),
                            airPollutionCurrentResult == null ? null : new AirQuality(
                                    (float) airPollutionCurrentResult.list.get(0).components.pm2_5,
                                    (float) airPollutionCurrentResult.list.get(0).components.pm10,
                                    (float) airPollutionCurrentResult.list.get(0).components.so2,
                                    (float) airPollutionCurrentResult.list.get(0).components.no2,
                                    (float) airPollutionCurrentResult.list.get(0).components.o3,
                                    airPollutionCurrentResult.list.get(0).components.co != 0 ? (float) airPollutionCurrentResult.list.get(0).components.co / 1000 : 0
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

        for (OwmOneCallResult.Daily dailyForecast : dailyResult) {
            dailyList.add(
                    new Daily(
                            new Date(dailyForecast.dt * 1000),
                            new HalfDay(
                                    dailyForecast.weather.get(0).description,
                                    dailyForecast.weather.get(0).description,
                                    getWeatherCode(dailyForecast.weather.get(0).id),
                                    new Temperature(
                                            toInt(dailyForecast.temp.day),
                                            null,
                                            null,
                                            toInt(dailyForecast.feelsLike.day),
                                            null,
                                            null,
                                            null
                                    ),
                                    new Precipitation(
                                            getTotalPrecipitation(getPrecipitationForDaily(dailyForecast.rain), getPrecipitationForDaily(dailyForecast.snow)),
                                            null,
                                            getPrecipitationForDaily(dailyForecast.rain),
                                            getPrecipitationForDaily(dailyForecast.snow),
                                            null
                                    ),
                                    new PrecipitationProbability(
                                            dailyForecast.pop,
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
                                            CommonConverterKt.getWindDirection(context, (float) dailyForecast.windDeg),
                                            new WindDegree((float) dailyForecast.windDeg, false),
                                            dailyForecast.windSpeed * 3.6f,
                                            CommonConverterKt.getWindLevel(context, dailyForecast.windSpeed * 3.6f)
                                    ),
                                    dailyForecast.clouds
                            ),
                            new HalfDay(
                                    dailyForecast.weather.get(0).description,
                                    dailyForecast.weather.get(0).description,
                                    getWeatherCode(dailyForecast.weather.get(0).id),
                                    new Temperature(
                                            toInt(dailyForecast.temp.night),
                                            null,
                                            null,
                                            toInt(dailyForecast.feelsLike.night),
                                            null,
                                            null,
                                            null
                                    ),
                                    new Precipitation(
                                            getTotalPrecipitation(getPrecipitationForDaily(dailyForecast.rain), getPrecipitationForDaily(dailyForecast.snow)),
                                            null,
                                            getPrecipitationForDaily(dailyForecast.rain),
                                            getPrecipitationForDaily(dailyForecast.snow),
                                            null
                                    ),
                                    new PrecipitationProbability(
                                            dailyForecast.pop,
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
                                            CommonConverterKt.getWindDirection(context, (float) dailyForecast.windDeg),
                                            new WindDegree((float) dailyForecast.windDeg, false),
                                            dailyForecast.windSpeed * 3.6f,
                                            CommonConverterKt.getWindLevel(context, dailyForecast.windSpeed * 3.6f)
                                    ),
                                    dailyForecast.clouds
                            ),
                            new Astro(new Date(dailyForecast.sunrise * 1000), new Date(dailyForecast.sunset * 1000)),
                            new Astro(new Date(dailyForecast.moonrise * 1000), new Date(dailyForecast.moonset * 1000)),
                            null,
                            getAirQuality(new Date(dailyForecast.dt * 1000), timeZone, airPollutionForecastResult),
                            null,
                            new UV(toInt(dailyForecast.uvi), CommonConverterKt.getUVLevel(context, toInt(dailyForecast.uvi)), null),
                            null
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
                            null, // TODO: use forecast API
                            null,
                            new UV(toInt(result.uvi), CommonConverterKt.getUVLevel(context, toInt(result.uvi)), null)
                    )
            );
        }
        return hourlyList;
    }

    // TODO
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

    private static @Nullable AirQuality getAirQuality(Date requestedDate, TimeZone timeZone, @Nullable OwmAirPollutionResult owmAirPollutionForecastResult) {
        if (owmAirPollutionForecastResult != null) {
            for (OwmAirPollutionResult.AirPollution airPollutionForecast : owmAirPollutionForecastResult.list) {
                if (DisplayUtils.getFormattedDate(requestedDate, timeZone, "yyyyMMdd")
                        .equals(DisplayUtils.getFormattedDate(new Date(airPollutionForecast.dt * 1000), timeZone, "yyyyMMdd"))) {
                    return new AirQuality(
                            (float) airPollutionForecast.components.pm2_5,
                            (float) airPollutionForecast.components.pm10,
                            (float) airPollutionForecast.components.so2,
                            (float) airPollutionForecast.components.no2,
                            (float) airPollutionForecast.components.o3,
                            airPollutionForecast.components.co != 0 ? (float) airPollutionForecast.components.co / 1000 : 0
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