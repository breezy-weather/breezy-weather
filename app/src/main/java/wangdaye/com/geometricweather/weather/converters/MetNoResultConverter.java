package wangdaye.com.geometricweather.weather.converters;

import android.content.Context;
import android.text.TextUtils;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.TimeZone;

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
import wangdaye.com.geometricweather.weather.json.metno.MetNoLocationForecastResult;
import wangdaye.com.geometricweather.weather.json.metno.MetNoSunsetResult;
import wangdaye.com.geometricweather.weather.json.nominatim.NominatimLocationResult;
import wangdaye.com.geometricweather.weather.services.WeatherService;

public class MetNoResultConverter {

    @NonNull
    public static Location convert(@Nullable Location location, NominatimLocationResult result) {
        if (location != null
                && !TextUtils.isEmpty(location.getProvince())
                && !TextUtils.isEmpty(location.getCity())
                && !TextUtils.isEmpty(location.getDistrict())) {
            return new Location(
                    result.place_id.toString(),
                    result.lat,
                    result.lon,
                    TimeZone.getDefault(),
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
                    TimeZone.getDefault(),
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
            List<Hourly> hourly = getHourlyList(context, locationForecastResult, sunsetList);

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
                            getPrecipitationProbability(locationForecastResult.properties.timeseries.get(0).data),
                            new Wind(
                                    getWindDirection(locationForecastResult.properties.timeseries.get(0).data.instant.details.windFromDirection),
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
                    getDailyList(context, hourly, sunsetList),
                    hourly,
                    new ArrayList<>(),
                    new ArrayList<>()
            );
            return new WeatherService.WeatherResultWrapper(weather);
        } catch (Exception ignored) {
            /*Log.d("GEOM", ignored.getMessage());
            for (StackTraceElement stackTraceElement : ignored.getStackTrace()) {
                Log.d("GEOM", stackTraceElement.toString());
            }*/
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

        Float precipitationTotal = 0.0f;
        Float precipitationRain = 0.0f;

        Float probPrecipitationTotal = 0.0f;
        Float probPrecipitationThunderstorm = 0.0f;
        Float probPrecipitationRain = 0.0f;

        WeatherCode weatherCode = WeatherCode.CLOUDY;

        Wind wind = new Wind("Pas d’info", new WindDegree(0, false), null, "Pas d’info");

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

    private static List<Daily> getDailyList(Context context, List<Hourly> hourlyConverted, HashMap<String, MetNoSunsetResult.Location.Time> sunsetList) {
        List<String> dateList = new ArrayList<>();
        List<Daily> dailyList = new ArrayList<>();
        for (Hourly hourly : hourlyConverted) {
            SimpleDateFormat fmt = new SimpleDateFormat("yyyy-MM-dd");
            String dateString = fmt.format(hourly.getDate());
            Date date;
            try {
                date = fmt.parse(dateString); // Setup Date at 00:00
            } catch (ParseException ignored) {
                // Should never happen
                date = hourly.getDate();
            }
            if (!dateList.contains(dateString)) {
                dateList.add(dateString);

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
                                sunsetList.get(dateString).sunrise.time,
                                sunsetList.get(dateString).sunset.time
                            ),
                            new Astro(
                                sunsetList.get(dateString).moonrise.time,
                                sunsetList.get(dateString).moonset.time
                            ),
                            new MoonPhase(
                                toInt(Double.valueOf(sunsetList.get(dateString).moonposition.phase)),
                                sunsetList.get(dateString).moonposition.desc
                            ),
                            new AirQuality(
                                null, null, null, null,
                                null, null, null, null
                            ),
                            new Pollen(null, null, null, null, null, null, null, null, null, null, null, null),
                            new UV(null, null, null),
                            CommonConverter.getHoursOfDay(
                                sunsetList.get(dateString).sunrise.time,
                                sunsetList.get(dateString).sunset.time
                            )
                        )
                    );
                }
            }
        }
        return dailyList;
    }

    private static boolean isDaytime(Date time, HashMap<String, MetNoSunsetResult.Location.Time> sunsetList) {
        SimpleDateFormat fmt = new SimpleDateFormat("yyyy-MM-dd");
        if (!sunsetList.containsKey(fmt.format(time))
                || sunsetList.get(fmt.format(time)).sunrise == null
                || sunsetList.get(fmt.format(time)).sunset == null
                || sunsetList.get(fmt.format(time)).sunrise.time == null
                || sunsetList.get(fmt.format(time)).sunset.time == null) {
            return true;
        }
        return (time.getTime() > sunsetList.get(fmt.format(time)).sunrise.time.getTime())
                && (time.getTime() < sunsetList.get(fmt.format(time)).sunset.time.getTime());
    }

    private static List<Hourly> getHourlyList(Context context, MetNoLocationForecastResult resultList, HashMap<String, MetNoSunsetResult.Location.Time> sunsetList) {
        List<Hourly> hourlyList = new ArrayList<>(resultList.properties.timeseries.size());
        for (MetNoLocationForecastResult.Properties.Timeseries result : resultList.properties.timeseries) {
            hourlyList.add(
                    new Hourly(
                            result.time,
                            result.time.getTime(),
                            isDaytime(result.time, sunsetList),
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
                                    getWindDirection(result.data.instant.details.windFromDirection),
                                    new WindDegree(result.data.instant.details.windFromDirection, false),
                                    result.data.instant.details.windSpeed * 3.6f,
                                    CommonConverter.getWindLevel(context, result.data.instant.details.windSpeed * 3.6f)
                            ),
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

    private static String getWindDirection(float degree) {
        if (degree < 0) {
            return "Variable";
        }
        if (22.5 < degree && degree <= 67.5) {
            return "NE";
        } else if (67.5 < degree && degree <= 112.5) {
            return "E";
        } else if (112.5 < degree && degree <= 157.5) {
            return "SE";
        } else if (157.5 < degree && degree <= 202.5) {
            return "S";
        } else if (202.5 < degree && degree <= 247.5) {
            return "SO";
        } else if (247.5 < degree && degree <= 292.5) {
            return "O";
        } else if (292. < degree && degree <= 337.5) {
            return "NO";
        } else {
            return "N";
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
