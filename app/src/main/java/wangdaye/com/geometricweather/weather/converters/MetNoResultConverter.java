package wangdaye.com.geometricweather.weather.converters;

import android.content.Context;
import android.text.TextUtils;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.TimeZone;

import us.dustinj.timezonemap.TimeZoneMap;
import wangdaye.com.geometricweather.common.basic.models.Location;
import wangdaye.com.geometricweather.common.basic.models.options.provider.WeatherSource;
import wangdaye.com.geometricweather.common.basic.models.weather.AirQuality;
import wangdaye.com.geometricweather.common.basic.models.weather.Astro;
import wangdaye.com.geometricweather.common.basic.models.weather.Base;
import wangdaye.com.geometricweather.common.basic.models.weather.Current;
import wangdaye.com.geometricweather.common.basic.models.weather.Daily;
import wangdaye.com.geometricweather.common.basic.models.weather.HalfDay;
import wangdaye.com.geometricweather.common.basic.models.weather.Hourly;
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
import wangdaye.com.geometricweather.weather.json.metno.MetNoLocationForecastResult;
import wangdaye.com.geometricweather.weather.json.metno.MetNoSunsetResult;
import wangdaye.com.geometricweather.weather.json.nominatim.NominatimLocationResult;
import wangdaye.com.geometricweather.weather.services.WeatherService;

public class MetNoResultConverter {
    @NonNull
    public static List<Location> convert(List<NominatimLocationResult> resultList) {
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
            for (NominatimLocationResult r : resultList) {
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
            for (NominatimLocationResult r : resultList) {
                locationList.add(convert(null, r, map));
            }
        }
        return locationList;
    }

    @NonNull
    public static Location convert(@Nullable Location location, NominatimLocationResult result) {
        TimeZoneMap map = TimeZoneMap.forRegion(result.lat, result.lon, result.lat + 0.00001, result.lon + 0.00001);
        return convert(location, result, map);
    }

    @NonNull
    public static Location convert(@Nullable Location location, NominatimLocationResult result, TimeZoneMap map) {
        if (location != null
                && !TextUtils.isEmpty(location.getProvince())
                && !TextUtils.isEmpty(location.getCity())
                && !TextUtils.isEmpty(location.getDistrict())) {
            return new Location(
                    result.place_id.toString(),
                    result.lat,
                    result.lon,
                    CommonConverter.getTimeZoneForPosition(map, result.lat, result.lon),
                    result.address.country,
                    location.getProvince(),
                    location.getCity(),
                    location.getDistrict(),
                    null,
                    WeatherSource.METNO,
                    false,
                    false,
                    !TextUtils.isEmpty(result.address.country_code)
                            && (result.address.country_code.equals("CN")
                            || result.address.country_code.equals("cn")
                            || result.address.country_code.equals("HK")
                            || result.address.country_code.equals("hk")
                            || result.address.country_code.equals("TW")
                            || result.address.country_code.equals("tw"))
            );
        } else {
            return new Location(
                    result.place_id.toString(),
                    result.lat,
                    result.lon,
                    CommonConverter.getTimeZoneForPosition(map, result.lat, result.lon),
                    result.address.country,
                    result.address.state == null ? "" : result.address.state,
                    result.display_name,
                    "",
                    null,
                    WeatherSource.METNO,
                    false,
                    false,
                    !TextUtils.isEmpty(result.address.country_code)
                            && (result.address.country_code.equals("CN")
                            || result.address.country_code.equals("cn")
                            || result.address.country_code.equals("HK")
                            || result.address.country_code.equals("hk")
                            || result.address.country_code.equals("TW")
                            || result.address.country_code.equals("tw"))
            );
        }
    }

    @NonNull
    public static WeatherService.WeatherResultWrapper convert(Context context,
                                                              Location location,
                                                              MetNoLocationForecastResult locationForecastResult,
                                                              MetNoSunsetResult sunsetResult) {
        try {
            HashMap<String, MetNoSunsetResult.Location.Time> sunsetList = getSunsetResultAsHashMap(sunsetResult.location.time);
            List<Hourly> hourly = getHourlyList(context, location.getTimeZone(), location.isChina(), locationForecastResult, sunsetList);

            Weather weather = new Weather(
                    new Base(
                            location.getCityId(),
                            System.currentTimeMillis(),
                            locationForecastResult.properties.meta.updatedAt,
                            locationForecastResult.properties.meta.updatedAt.getTime(),
                            new Date(),
                            System.currentTimeMillis()
                    ),
                    new Current(
                            "",
                            getWeatherCode(getSymbolCode(locationForecastResult.properties.timeseries.get(0).data)),
                            new Temperature(
                                    toInt(Double.valueOf(locationForecastResult.properties.timeseries.get(0).data.instant.details.airTemperature)),
                                    null,
                                    null,
                                    null,
                                    null,
                                    null,
                                    null
                            ),
                            new Precipitation(
                                    getPrecipitationAmount(locationForecastResult.properties.timeseries.get(0).data),
                                    null,
                                    null,
                                    null,
                                    null
                            ),
                            new Wind(
                                    CommonConverter.getWindDirection(locationForecastResult.properties.timeseries.get(0).data.instant.details.windFromDirection, location.isChina()),
                                    new WindDegree(locationForecastResult.properties.timeseries.get(0).data.instant.details.windFromDirection, false),
                                    locationForecastResult.properties.timeseries.get(0).data.instant.details.windSpeed * 3.6f,
                                    CommonConverter.getWindLevel(context, locationForecastResult.properties.timeseries.get(0).data.instant.details.windSpeed * 3.6f)
                            ),
                            new UV(null,null,null), // FIXME: Use ultravioletIndexClearSky
                            new AirQuality(
                                    null, null, null, null,
                                    null, null, null, null
                            ),
                            locationForecastResult.properties.timeseries.get(0).data.instant.details.relativeHumidity,
                            locationForecastResult.properties.timeseries.get(0).data.instant.details.airPressureAtSeaLevel,
                            null,
                            null,
                            null,
                            null,
                            null,
                            null
                    ),
                    null,
                    getDailyList(context, location.getTimeZone(), hourly, sunsetList),
                    hourly,
                    new ArrayList<>(),
                    new ArrayList<>()
            );
            return new WeatherService.WeatherResultWrapper(weather);
        } catch (Exception ignored) {
            return new WeatherService.WeatherResultWrapper(null);
        }
    }

    protected static Float getMaxTemperature(List<MetNoLocationForecastResult.Properties.Timeseries> timeResults) {
         Float temperature = null;
         for (MetNoLocationForecastResult.Properties.Timeseries timeResult : timeResults) {
             if (temperature == null || timeResult.data.instant.details.airTemperature > temperature) {
                 temperature = timeResult.data.instant.details.airTemperature;
             }
         }

         return temperature;
    }

    protected static Float getMinTemperature(List<MetNoLocationForecastResult.Properties.Timeseries> timeResults) {
        Float temperature = null;
        for (MetNoLocationForecastResult.Properties.Timeseries timeResult : timeResults) {
            if (temperature == null || timeResult.data.instant.details.airTemperature < temperature) {
                temperature = timeResult.data.instant.details.airTemperature;
            }
        }

        return temperature;
    }

    private static HalfDay getHalfDay(Context context, boolean isDaytime, Date date, List<Hourly> hourly, HashMap<String, MetNoSunsetResult.Location.Time> sunsetList) {
        Integer temp = null;

        float precipitationTotal = 0.0f;
        float precipitationRain = 0.0f;

        Float probPrecipitationTotal = 0.0f;
        Float probPrecipitationThunderstorm = 0.0f;
        Float probPrecipitationRain = 0.0f;

        WeatherCode weatherCode = WeatherCode.CLOUDY;

        Wind wind = null;

        // In AccuWeather provider, a day is considered from 6:00 to 17:59 and night from 18:00 to 5:59 the next day
        // So we implement it this way (same as Météo France provider)
        for (Hourly hour : hourly) {
            // For temperatures, we loop through all hours from 6:00 to 5:59 (next day) to avoid having no max temperature after 18:00
            if ((hour.getTime() / 1000) >= (date.getTime() / 1000) + 6 * 3600 && (hour.getTime() / 1000) < (date.getTime() / 1000) + 30 * 3600) {
                if (isDaytime) {
                    if (temp == null || hour.getTemperature().getTemperature() > temp) {
                        temp = hour.getTemperature().getTemperature();
                    }
                }
                if (!isDaytime) {
                    if (temp == null || hour.getTemperature().getTemperature() < temp) {
                        temp = hour.getTemperature().getTemperature();
                    }
                }
            }

            // For weather code, we look at 12:00 and 00:00
            if (isDaytime && (hour.getTime() / 1000) == (date.getTime() / 1000) + 12 * 3600) {
                weatherCode = hour.getWeatherCode();
            }
            if (!isDaytime && (hour.getTime() / 1000) == (date.getTime() / 1000) + 24 * 3600) {
                weatherCode = hour.getWeatherCode();
            }

            if ((isDaytime && (hour.getTime() / 1000) >= (date.getTime() / 1000) + 6 * 3600 && (hour.getTime() / 1000) < (date.getTime() / 1000) + 18 * 3600)
                    || (!isDaytime && (hour.getTime() / 1000) >= (date.getTime() / 1000) + 18 * 3600 && (hour.getTime() / 1000) < (date.getTime() / 1000) + 30 * 3600)) {
                // Precipitation
                precipitationTotal += (hour.getPrecipitation().getTotal() == null) ? 0 : hour.getPrecipitation().getTotal();
                precipitationRain += (hour.getPrecipitation().getRain() == null) ? 0 : hour.getPrecipitation().getRain();

                // Precipitation probability
                if (hour.getPrecipitationProbability().getTotal() != null && hour.getPrecipitationProbability().getTotal() > probPrecipitationTotal) {
                    probPrecipitationTotal = hour.getPrecipitationProbability().getTotal();
                }
                if (hour.getPrecipitationProbability().getThunderstorm() != null && hour.getPrecipitationProbability().getThunderstorm() > probPrecipitationThunderstorm) {
                    probPrecipitationThunderstorm = hour.getPrecipitationProbability().getThunderstorm();
                }
                if (hour.getPrecipitationProbability().getRain() != null && hour.getPrecipitationProbability().getRain() > probPrecipitationRain) {
                    probPrecipitationRain = hour.getPrecipitationProbability().getRain();
                }

                // Wind
                if ((hour.getWind() != null && hour.getWind().getSpeed() != null) && (wind.getSpeed() == null || hour.getWind().getSpeed() > wind.getSpeed())) {
                    wind = hour.getWind();
                }
            }
        }

        // Return null so we don't add a garbage day
        return temp == null ? null : new HalfDay(
                "",
                "",
                weatherCode,
                new Temperature(
                        temp,
                        null,
                        null,
                        null,
                        null,
                        null,
                        null
                ),
                new Precipitation(
                        precipitationTotal,
                        null,
                        precipitationRain,
                        null,
                        null
                ),
                new PrecipitationProbability(
                        probPrecipitationTotal,
                        probPrecipitationThunderstorm,
                        probPrecipitationRain,
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
                wind,
                null
        );
    }

    private static List<Daily> getDailyList(Context context, TimeZone timeZone, List<Hourly> hourlyConverted, HashMap<String, MetNoSunsetResult.Location.Time> sunsetList) {
        List<String> dateList = new ArrayList<>();
        List<Daily> dailyList = new ArrayList<>();
        for (Hourly hourly : hourlyConverted) {
            Date date = DisplayUtils.toTimezoneNoHour(hourly.getDate(), timeZone);
            String formattedDate = DisplayUtils.getFormattedDate(hourly.getDate(), timeZone, "yyyy-MM-dd");
            if (!dateList.contains(formattedDate)) {
                dateList.add(formattedDate);

                HalfDay halfDayDaytime = getHalfDay(context, true, hourly.getDate(), hourlyConverted, sunsetList);
                HalfDay halfDayNighttime = getHalfDay(context, false, hourly.getDate(), hourlyConverted, sunsetList);

                // Don’t add to the list if we have no data on it
                if (halfDayDaytime != null && halfDayNighttime != null) {
                    dailyList.add(
                        new Daily(
                            date,
                            date.getTime(),
                            halfDayDaytime,
                            halfDayNighttime,
                            new Astro(
                                sunsetList.containsKey(formattedDate) && sunsetList.get(formattedDate).sunrise != null ? sunsetList.get(formattedDate).sunrise.time : null,
                                sunsetList.containsKey(formattedDate) && sunsetList.get(formattedDate).sunset != null ? sunsetList.get(formattedDate).sunset.time : null
                            ),
                            new Astro(
                                sunsetList.containsKey(formattedDate) && sunsetList.get(formattedDate).moonrise != null ? sunsetList.get(formattedDate).moonrise.time : null,
                                sunsetList.containsKey(formattedDate) && sunsetList.get(formattedDate).moonset != null ? sunsetList.get(formattedDate).moonset.time : null
                            ),
                            new MoonPhase(
                                sunsetList.containsKey(formattedDate) && sunsetList.get(formattedDate).moonposition != null ? toInt(Double.valueOf(sunsetList.get(formattedDate).moonposition.phase)) : null,
                                sunsetList.containsKey(formattedDate) && sunsetList.get(formattedDate).moonposition != null ? sunsetList.get(formattedDate).moonposition.desc : null
                            ),
                            new AirQuality(
                                null, null, null, null,
                                null, null, null, null
                            ),
                            new Pollen(null, null, null, null, null, null, null, null, null, null, null, null),
                            new UV(null, null, null),
                            sunsetList.containsKey(formattedDate) && sunsetList.get(formattedDate).sunrise != null && sunsetList.get(formattedDate).sunset != null ? CommonConverter.getHoursOfDay(
                                sunsetList.get(formattedDate).sunrise.time,
                                sunsetList.get(formattedDate).sunset.time
                            ) : 0
                        )
                    );
                }
            }
        }
        return dailyList;
    }

    private static boolean isDaylight(Date time, TimeZone timeZone, HashMap<String, MetNoSunsetResult.Location.Time> sunsetList) {
        String formattedDate = DisplayUtils.getFormattedDate(time, timeZone, "yyyy-MM-dd");
        if (!sunsetList.containsKey(formattedDate)
                || sunsetList.get(formattedDate).sunrise == null
                || sunsetList.get(formattedDate).sunset == null
                || sunsetList.get(formattedDate).sunrise.time == null
                || sunsetList.get(formattedDate).sunset.time == null) {
            return true;
        }
        return CommonConverter.isDaylight(sunsetList.get(formattedDate).sunrise.time, sunsetList.get(formattedDate).sunset.time, time, timeZone);
    }

    private static List<Hourly> getHourlyList(Context context, TimeZone timeZone, boolean isChina, MetNoLocationForecastResult resultList, HashMap<String, MetNoSunsetResult.Location.Time> sunsetList) {
        List<Hourly> hourlyList = new ArrayList<>(resultList.properties.timeseries.size());
        for (MetNoLocationForecastResult.Properties.Timeseries result : resultList.properties.timeseries) {
            hourlyList.add(
                    new Hourly(
                            result.time,
                            result.time.getTime(),
                            isDaylight(result.time, timeZone, sunsetList),
                            null,
                            getWeatherCode(getSymbolCode(result.data)),
                            result.data.instant == null || result.data.instant.details == null || result.data.instant.details.airTemperature == null ? null : new Temperature(
                                    toInt(Double.valueOf(result.data.instant.details.airTemperature)),
                                    null,
                                    null,
                                    null,
                                    null,
                                    null,
                                    null
                            ),
                            new Precipitation(
                                    getPrecipitationAmount(result.data),
                                    null,
                                    null,
                                    null,
                                    null
                            ),
                            getPrecipitationProbability(result.data),
                            result.data.instant == null || result.data.instant.details == null ? null : new Wind(
                                    CommonConverter.getWindDirection(result.data.instant.details.windFromDirection, isChina),
                                    new WindDegree(result.data.instant.details.windFromDirection, false),
                                    result.data.instant.details.windSpeed * 3.6f,
                                    CommonConverter.getWindLevel(context, result.data.instant.details.windSpeed * 3.6f)
                            ),
                            new AirQuality(null, null, null, null, null, null, null, null),
                            new Pollen(null, null, null, null, null, null, null, null, null, null, null, null),
                            new UV(null, null, null) // FIXME: Use ultravioletIndexClearSky
                    )
            );
        }
        return hourlyList;
    }

    /*private static List<Minutely> getMinutelyList(long sunrise, long sunset,
                                                  @Nullable MetNoLocationForecastResult minuteResult) {
        if (minuteResult == null) {
            return new ArrayList<>();
        }
        List<Minutely> minutelyList = new ArrayList<>(minuteResult.properties.timeseries.size());
        for (MetNoLocationForecastResult.Property.Timeseries interval : minuteResult.properties.timeseries) {
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
        return minutelyList;
    }*/

    private static int toInt(Double value) {
        return (int) (value + 0.5);
    }

    private static Float getPrecipitationAmount(MetNoLocationForecastResult.Properties.Timeseries.Data timeData) {
        if (timeData.next1Hours != null && timeData.next1Hours.details != null) {
            return timeData.next1Hours.details.precipitationAmount;
        } else if (timeData.next6Hours != null && timeData.next6Hours.details != null) {
            return timeData.next6Hours.details.precipitationAmount;
        } else if (timeData.next12Hours != null && timeData.next12Hours.details != null) {
            return timeData.next12Hours.details.precipitationAmount;
        } else {
            return null;
        }
    }

    private static PrecipitationProbability getPrecipitationProbability(MetNoLocationForecastResult.Properties.Timeseries.Data timeData) {
        if (timeData.next1Hours != null && timeData.next1Hours.details != null) {
            List<Float> allProbabilities = new ArrayList<>();
            allProbabilities.add(timeData.next1Hours.details.probabilityOfThunder != null ? timeData.next1Hours.details.probabilityOfThunder : 0f);
            allProbabilities.add(timeData.next1Hours.details.probabilityOfPrecipitation != null ? timeData.next1Hours.details.probabilityOfPrecipitation : 0f);

            return new PrecipitationProbability(
                Collections.max(allProbabilities, null),
                timeData.next1Hours.details.probabilityOfThunder,
                timeData.next1Hours.details.probabilityOfPrecipitation,
                null,
                null
            );
        } else if (timeData.next6Hours != null && timeData.next6Hours.details != null) {
            List<Float> allProbabilities = new ArrayList<>();
            allProbabilities.add(timeData.next6Hours.details.probabilityOfThunder != null ? timeData.next6Hours.details.probabilityOfThunder : 0f);
            allProbabilities.add(timeData.next6Hours.details.probabilityOfPrecipitation != null ? timeData.next6Hours.details.probabilityOfPrecipitation : 0f);

            return new PrecipitationProbability(
                    Collections.max(allProbabilities, null),
                    timeData.next6Hours.details.probabilityOfThunder,
                    timeData.next6Hours.details.probabilityOfPrecipitation,
                    null,
                    null
            );
        } else if (timeData.next12Hours != null && timeData.next12Hours.details != null) {
            List<Float> allProbabilities = new ArrayList<>();
            allProbabilities.add(timeData.next12Hours.details.probabilityOfThunder != null ? timeData.next12Hours.details.probabilityOfThunder : 0f);
            allProbabilities.add(timeData.next12Hours.details.probabilityOfPrecipitation != null ? timeData.next12Hours.details.probabilityOfPrecipitation : 0f);

            return new PrecipitationProbability(
                    Collections.max(allProbabilities, null),
                    timeData.next12Hours.details.probabilityOfThunder,
                    timeData.next12Hours.details.probabilityOfPrecipitation,
                    null,
                    null
            );
        } else {
            return new PrecipitationProbability(null, null, null, null, null);
        }
    }

    private static String getSymbolCode(MetNoLocationForecastResult.Properties.Timeseries.Data timeData) {
        if (timeData.next1Hours != null && timeData.next1Hours.summary != null && timeData.next1Hours.summary.symbolCode != null) {
            return timeData.next1Hours.summary.symbolCode;
        } else if (timeData.next6Hours != null && timeData.next6Hours.summary != null && timeData.next6Hours.summary.symbolCode != null) {
            return timeData.next6Hours.summary.symbolCode;
        } else if (timeData.next12Hours != null && timeData.next12Hours.summary != null && timeData.next12Hours.summary.symbolCode != null) {
            return timeData.next12Hours.summary.symbolCode;
        } else {
            return "";
        }
    }

    private static WeatherCode getWeatherCode(String icon) {
        switch (icon) {
            case "clearsky":
            case "fair":
                return WeatherCode.CLEAR;

            case "partlycloudy":
                return WeatherCode.PARTLY_CLOUDY;

            case "cloudy":
                return WeatherCode.CLOUDY;

            case "fog":
                return WeatherCode.FOG;

            case "heavyrain":
            case "heavyrainshowers":
            case "lightrain":
            case "lightrainshowers":
            case "rain":
            case "rainshowers":
                return WeatherCode.RAIN;

            case "heavyrainandthunder":
            case "heavyrainshowersandthunder":
            case "heavysleetandthunder":
            case "heavysleetshowersandthunder":
            case "heavysnowandthunder":
            case "heavysnowshowersandthunder":
            case "lightrainandthunder":
            case "lightrainshowersandthunder":
            case "lightsleetandthunder":
            case "lightsleetshowersandthunder":
            case "lightsnowandthunder":
            case "lightsnowshowersandthunder":
            case "rainandthunder":
            case "rainshowersandthunder":
            case "sleetandthunder":
            case "sleetshowersandthunder":
            case "snowandthunder":
            case "snowshowersandthunder":
                return WeatherCode.THUNDERSTORM;

            case "heavysnow":
            case "heavysnowshowers":
            case "lightsnow":
            case "lightsnowshowers":
            case "snow":
            case "snowshowers":
                return WeatherCode.SNOW;

            case "heavysleet":
            case "heavysleetshowers":
            case "lightsleet":
            case "lightsleetshowers":
            case "sleet":
            case "sleetshowers":
                return WeatherCode.SLEET;

            default:
                return WeatherCode.CLOUDY;
        }
    }

    protected static HashMap<String, MetNoSunsetResult.Location.Time> getSunsetResultAsHashMap(List<MetNoSunsetResult.Location.Time> sunsetTimeResults) {
        HashMap<String, MetNoSunsetResult.Location.Time> sunsetList = new HashMap<>();
        for (MetNoSunsetResult.Location.Time sunsetTimeResult : sunsetTimeResults) {
            sunsetList.put(sunsetTimeResult.date, sunsetTimeResult);
        }
        return sunsetList;
    }
}
