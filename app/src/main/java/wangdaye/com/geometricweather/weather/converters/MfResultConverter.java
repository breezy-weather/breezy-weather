package wangdaye.com.geometricweather.weather.converters;

import android.content.Context;
import android.graphics.Color;
import android.text.TextUtils;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

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
import wangdaye.com.geometricweather.weather.json.atmoaura.AtmoAuraQAResult;
import wangdaye.com.geometricweather.weather.json.mf.MfCurrentResult;
import wangdaye.com.geometricweather.weather.json.mf.MfEphemerisResult;
import wangdaye.com.geometricweather.weather.json.mf.MfForecastV2Result;
import wangdaye.com.geometricweather.weather.json.mf.MfLocationResult;
import wangdaye.com.geometricweather.weather.json.mf.MfRainResult;
import wangdaye.com.geometricweather.weather.json.mf.MfWarningsResult;
import wangdaye.com.geometricweather.weather.services.WeatherService;

public class MfResultConverter {

    // Result of a coordinates search
    @NonNull
    public static Location convert(@Nullable Location location, MfForecastV2Result result) {
        if (location != null
                && !TextUtils.isEmpty(location.getProvince())
                && !TextUtils.isEmpty(location.getCity())
                && !TextUtils.isEmpty(location.getDistrict())) {
            return new Location(
                    result.properties.insee, // cityId
                    result.geometry.coordinates.get(1),
                    result.geometry.coordinates.get(0),
                    TimeZone.getTimeZone(result.properties.timezone),
                    result.properties.country,
                    location.getProvince(), // Domain (département)
                    location.getCity(),
                    location.getDistrict(),
                    null,
                    WeatherSource.MF,
                    false,
                    false,
                    !TextUtils.isEmpty(result.properties.country)
                            && (result.properties.country.startsWith("CN")
                            || result.properties.country.startsWith("cn")
                            || result.properties.country.startsWith("HK")
                            || result.properties.country.startsWith("hk")
                            || result.properties.country.startsWith("TW")
                            || result.properties.country.startsWith("tw"))
            );
        } else {
            return new Location(
                    result.properties.insee, // cityId
                    result.geometry.coordinates.get(1),
                    result.geometry.coordinates.get(0),
                    TimeZone.getTimeZone(result.properties.timezone),
                    result.properties.country,
                    result.properties.frenchDepartment, // Domain (département)
                    result.properties.name,
                    "",
                    null,
                    WeatherSource.MF,
                    false,
                    false,
                    !TextUtils.isEmpty(result.properties.country)
                            && (result.properties.country.startsWith("CN")
                            || result.properties.country.startsWith("cn")
                            || result.properties.country.startsWith("HK")
                            || result.properties.country.startsWith("hk")
                            || result.properties.country.startsWith("TW")
                            || result.properties.country.startsWith("tw"))
            );
        }
    }

    // Result of a query string search
    @NonNull
    public static Location convert(@Nullable Location location, MfLocationResult result) {
        if (location != null
                && !TextUtils.isEmpty(location.getProvince())
                && !TextUtils.isEmpty(location.getCity())
                && !TextUtils.isEmpty(location.getDistrict())) {
            return new Location(
                    result.postCode, // cityId
                    (float) result.lat,
                    (float) result.lon,
                    TimeZone.getTimeZone("Europe/Paris"), // TODO
                    result.country,
                    location.getProvince(), // Domain (département)
                    location.getCity(),
                    location.getDistrict(),
                    null,
                    WeatherSource.MF,
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
                    result.postCode, // cityId
                    (float) result.lat,
                    (float) result.lon,
                    TimeZone.getTimeZone("Europe/Paris"), // TODO
                    result.country,
                    result.admin2, // Domain (département)
                    result.name + (result.postCode == null ? "" : (" (" + result.postCode + ")")),
                    "",
                    null,
                    WeatherSource.MF,
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
                                                              MfCurrentResult currentResult,
                                                              MfForecastV2Result forecastV2Result,
                                                              MfEphemerisResult ephemerisResult,
                                                              MfRainResult rainResult,
                                                              MfWarningsResult warningsResult,
                                                              @Nullable AtmoAuraQAResult aqiAtmoAuraResult) {
        try {
            List<Hourly> hourly = getHourlyList(context, forecastV2Result.properties.forecast, forecastV2Result.properties.probabilityForecast);
            Weather weather = new Weather(
                    new Base(
                            location.getCityId(),
                            System.currentTimeMillis(),
                            new Date(forecastV2Result.updateTime * 1000),
                            forecastV2Result.updateTime * 1000,
                            new Date(),
                            System.currentTimeMillis()
                    ),
                    new Current(
                            currentResult.observation.weather.desc,
                            getWeatherCode(currentResult.observation.weather.icon),
                            new Temperature(
                                    toInt(currentResult.observation.temperature),
                                    null,
                                    null,
                                    null,
                                    null,
                                    null,
                                    null
                            ),
                            new Precipitation(
                                    null,
                                    null,
                                    null,
                                    null,
                                    null
                            ),
                            new PrecipitationProbability(
                                    null,
                                    null,
                                    null,
                                    null,
                                    null
                            ),
                            new Wind(
                                    currentResult.observation.wind.icon,
                                    new WindDegree(currentResult.observation.wind.direction, currentResult.observation.wind.direction == -1),
                                    currentResult.observation.wind.speed * 3.6f,
                                    CommonConverter.getWindLevel(context, currentResult.observation.wind.speed * 3.6f)
                            ),
                            getCurrentUV(new Date(), forecastV2Result.properties.dailyForecast),
                            getAirQuality(context, new Date(), aqiAtmoAuraResult, true),
                            null,
                            null,
                            null,
                            null,
                            null,
                            null,
                            null,
                            null
                    ),
                    null, // TODO: Fill in with observation data instead
                    getDailyList(context, forecastV2Result.properties, hourly, ephemerisResult, aqiAtmoAuraResult),
                    hourly,
                    getMinutelyList(forecastV2Result.properties.dailyForecast.get(0).sunriseTime, forecastV2Result.properties.dailyForecast.get(0).sunsetTime, rainResult),
                    getWarningsList(warningsResult)
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

    // This can be improved by adding Aqi results from other regions
    private static AirQuality getAirQuality(Context context, Date requestedDate, @Nullable AtmoAuraQAResult aqiAtmoAuraResult, Boolean hourlyAqi) {
        if (aqiAtmoAuraResult == null) {
            return new AirQuality(
                    null, null,
                    null, null,
                    null, null,
                    null, null
            );
        } else {
            Integer highestO3 = null;
            Integer highestNO2 = null;
            Integer totalPM25 = null;
            int countPM25 = 0;
            Integer totalPM10 = null;
            int countPM10 = 0;
            Integer highestSO2 = null;

            String pattern;
            if (hourlyAqi) {
                pattern = "yyyyMMddHH";
            } else {
                pattern = "yyyyMMdd";
            }
            SimpleDateFormat dateFormatLocal = new SimpleDateFormat(pattern);
            SimpleDateFormat dateFormatUtc = new SimpleDateFormat(pattern);
            dateFormatUtc.setTimeZone(TimeZone.getTimeZone("UTC")); // FIXME: Dirty workaround as the API gives UTC and request date is in local, we should move to ThreeTen to support natively
            for (AtmoAuraQAResult.Polluant polluant : aqiAtmoAuraResult.polluants) {
                for (AtmoAuraQAResult.Polluant.Horaire horaire : polluant.horaires) {
                    if (dateFormatUtc.format(requestedDate).equals(dateFormatLocal.format(horaire.datetimeEcheance))) {
                        if (horaire.concentration != null) {
                            // For PM 2.5 and PM 10, it's the daily mean of the day
                            // For the others (O3, No2, SO2), it's the maximum value
                            switch (polluant.polluant) {
                                case "o3":
                                    if (highestO3 == null || highestO3 < horaire.concentration) {
                                        highestO3 = horaire.concentration;
                                    }
                                    break;
                                case "no2":
                                    if (highestNO2 == null || highestNO2 < horaire.concentration) {
                                        highestNO2 = horaire.concentration;
                                    }
                                    break;
                                case "pm2.5":
                                    if (totalPM25 == null) {
                                        totalPM25 = horaire.concentration;
                                    } else {
                                        totalPM25 += horaire.concentration;
                                    }
                                    ++countPM25;
                                    break;
                                case "pm10":
                                    if (totalPM10 == null) {
                                        totalPM10 = horaire.concentration;
                                    } else {
                                        totalPM10 += horaire.concentration;
                                    }
                                    ++countPM10;
                                    break;
                                case "so2":
                                    if (highestSO2 == null || highestSO2 < horaire.concentration) {
                                        highestSO2 = horaire.concentration;
                                    }
                                    break;
                            }
                        }
                    }
                }
            }

            Float aqiPM25 = null;
            Float aqiPM10 = null;
            if (totalPM25 != null) {
                aqiPM25 = totalPM25.floatValue() / countPM25;
            }
            if (totalPM10 != null) {
                aqiPM10 = totalPM10.floatValue() / countPM10;
            }

            Integer aqiIndex = getAqiIndex(aqiPM25, aqiPM10, highestNO2, highestO3, highestSO2);

            return new AirQuality(
                CommonConverter.getAqiQuality(context, aqiIndex), aqiIndex,
                aqiPM25, aqiPM10,
                highestSO2 != null ? highestSO2.floatValue() : null, highestNO2 != null ? highestNO2.floatValue() : null,
                highestO3 != null ? highestO3.floatValue() : null, null
            );
        }
    }

    // European AQI, values from https://www.atmo-nouvelleaquitaine.org/article/le-nouvel-indice-atmo-de-la-qualite-de-lair-plus-precis-et-plus-complet
    private static Integer getAqiIndex(Float pm25, Float pm10, Integer no2, Integer o3, Integer so2) {
        if (pm25 == null && pm10 == null && no2 == null && o3 == null && so2 == null) {
            return null;
        }

        // Avoids null pointer exceptions
        if (pm25 == null) {
            pm25 = 0f;
        }
        if (pm10 == null) {
            pm10 = 0f;
        }
        if (no2 == null) {
            no2 = 0;
        }
        if (o3 == null) {
            o3 = 0;
        }
        if (so2 == null) {
            so2 = 0;
        }

        // As we don't have any values, we take middle values between minimum and maximum values for the index to be in the correct color
        if (pm25 > 75 || pm10 > 150 || no2 > 340 || o3 > 380 || so2 > 750) {
            return (AirQuality.AQI_INDEX_5 + ((AirQuality.AQI_INDEX_5 - AirQuality.AQI_INDEX_4) / 2));
        }
        if (pm25 > 50 || pm10 > 100 || no2 > 230 || o3 > 240 || so2 > 500) {
            return (AirQuality.AQI_INDEX_5 - ((AirQuality.AQI_INDEX_5 - AirQuality.AQI_INDEX_4) / 2));
        }
        if (pm25 > 25 || pm10 > 50 || no2 > 120 || o3 > 130 || so2 > 350) {
            return (AirQuality.AQI_INDEX_4 - ((AirQuality.AQI_INDEX_4 - AirQuality.AQI_INDEX_3) / 2));
        }
        if (pm25 > 20 || pm10 > 40 || no2 > 90 || o3 > 100 || so2 > 200) {
            return (AirQuality.AQI_INDEX_3 - ((AirQuality.AQI_INDEX_3 - AirQuality.AQI_INDEX_2) / 2));
        }
        if (pm25 > 10 || pm10 > 20 || no2 > 40 || o3 > 50 || so2 > 100) {
            return (AirQuality.AQI_INDEX_2 - ((AirQuality.AQI_INDEX_2 - AirQuality.AQI_INDEX_1) / 2));
        }
        return AirQuality.AQI_INDEX_1 / 2;
    }

    private static HalfDay getHalfDay(boolean isDaytime, List<Hourly> hourly, List<MfForecastV2Result.ForecastProperties.HourForecast> hourlyForecast, MfForecastV2Result.ForecastProperties.ForecastV2 dailyForecast) {
        Integer temp = null;
        Integer tempWindChill = null;

        float precipitationTotal = 0.0f;
        float precipitationRain = 0.0f;
        float precipitationSnow = 0.0f;

        Float probPrecipitationTotal = 0.0f;
        Float probPrecipitationRain = 0.0f;
        Float probPrecipitationSnow = 0.0f;
        Float probPrecipitationIce = 0.0f;

        Wind wind = new Wind("Pas d’info", new WindDegree(0, false), null, "Pas d’info");

        // In AccuWeather provider, a day is considered from 6:00 to 17:59 and night from 18:00 to 5:59 the next day
        // So we implement it this way (same as MET Norway provider)
        for (Hourly hour : hourly) {
            // For temperatures, we loop through all hours from 6:00 to 5:59 (next day) to avoid having no max temperature after 18:00
            if ((hour.getTime() / 1000) >= dailyForecast.time + 6 * 3600 && (hour.getTime() / 1000) < dailyForecast.time + 30 * 3600) {
                if (isDaytime) {
                    if (temp == null || hour.getTemperature().getTemperature() > temp) {
                        temp = hour.getTemperature().getTemperature();
                    }
                    if (hour.getTemperature().getWindChillTemperature() != null && (tempWindChill == null || hour.getTemperature().getWindChillTemperature() > tempWindChill)) {
                        tempWindChill = hour.getTemperature().getWindChillTemperature();
                    }
                }
                if (!isDaytime) {
                    if (temp == null || hour.getTemperature().getTemperature() < temp) {
                        temp = hour.getTemperature().getTemperature();
                    }
                    if (hour.getTemperature().getWindChillTemperature() != null && (tempWindChill == null || hour.getTemperature().getWindChillTemperature() < tempWindChill)) {
                        tempWindChill = hour.getTemperature().getWindChillTemperature();
                    }
                }
            }

            if ((isDaytime && (hour.getTime() / 1000) >= dailyForecast.time + 6 * 3600 && (hour.getTime() / 1000) < dailyForecast.time + 18 * 3600)
                    || (!isDaytime && (hour.getTime() / 1000) >= dailyForecast.time + 18 * 3600 && (hour.getTime() / 1000) < dailyForecast.time + 30 * 3600)) {
                // Precipitation
                precipitationTotal += (hour.getPrecipitation().getTotal() == null) ? 0 : hour.getPrecipitation().getTotal();
                precipitationRain += (hour.getPrecipitation().getRain() == null) ? 0 : hour.getPrecipitation().getRain();
                precipitationSnow += (hour.getPrecipitation().getSnow() == null) ? 0 : hour.getPrecipitation().getSnow();

                // Precipitation probability
                if (hour.getPrecipitationProbability().getTotal() != null && hour.getPrecipitationProbability().getTotal() > probPrecipitationTotal) {
                    probPrecipitationTotal = hour.getPrecipitationProbability().getTotal();
                }
                if (hour.getPrecipitationProbability().getRain() != null && hour.getPrecipitationProbability().getRain() > probPrecipitationRain) {
                    probPrecipitationRain = hour.getPrecipitationProbability().getRain();
                }
                if (hour.getPrecipitationProbability().getSnow() != null && hour.getPrecipitationProbability().getSnow() > probPrecipitationSnow) {
                    probPrecipitationSnow = hour.getPrecipitationProbability().getSnow();
                }
                if (hour.getPrecipitationProbability().getIce() != null && hour.getPrecipitationProbability().getIce() > probPrecipitationIce) {
                    probPrecipitationIce = hour.getPrecipitationProbability().getIce();
                }

                // Wind
                if ((hour.getWind() != null && hour.getWind().getSpeed() != null) && (wind.getSpeed() == null || hour.getWind().getSpeed() > wind.getSpeed())) {
                    wind = hour.getWind();
                }
            }
        }

        Integer cloudCover = null;
        for (MfForecastV2Result.ForecastProperties.HourForecast hourForecast : hourlyForecast) {
            if ((isDaytime && hourForecast.time >= dailyForecast.time + 6 * 3600 && hourForecast.time < dailyForecast.time + 18 * 3600)
                    || (!isDaytime && hourForecast.time >= dailyForecast.time + 18 * 3600 && hourForecast.time < dailyForecast.time + 30 * 3600)) {
                if (cloudCover == null || (hourForecast.totalCloudCover != null && hourForecast.totalCloudCover > cloudCover)) {
                    cloudCover = hourForecast.totalCloudCover;
                }
            }
        }

        if (temp == null) {
            // Only add the daily reported value if we have no hourly data
            if (isDaytime && dailyForecast.tMax != null) {
                temp = toInt(dailyForecast.tMax);
            } else if(!isDaytime && dailyForecast.tMin != null) {
                temp = toInt(dailyForecast.tMin);
            }
        }

        // Return null so we don't add a garbage day
        return temp == null ? null : new HalfDay(
                dailyForecast.dailyWeatherDescription == null ? "" : dailyForecast.dailyWeatherDescription,
                dailyForecast.dailyWeatherDescription == null ? "" : dailyForecast.dailyWeatherDescription,
                dailyForecast.dailyWeatherDescription == null ? WeatherCode.CLEAR : getWeatherCode(dailyForecast.dailyWeatherIcon),
                new Temperature(
                        temp,
                        null,
                        null,
                        null,
                        tempWindChill,
                        null,
                        null
                ),
                new Precipitation(
                        precipitationTotal,
                        null,
                        precipitationRain,
                        precipitationSnow,
                        null
                ),
                new PrecipitationProbability(
                        probPrecipitationTotal,
                        null,
                        probPrecipitationRain,
                        probPrecipitationSnow,
                        probPrecipitationIce
                ),
                new PrecipitationDuration(
                        null,
                        null,
                        null,
                        null,
                        null
                ),
                wind,
                cloudCover
        );
    }

    private static List<Daily> getDailyList(Context context, MfForecastV2Result.ForecastProperties forecastsResult, List<Hourly> hourly, MfEphemerisResult ephemerisResult, @Nullable AtmoAuraQAResult aqiAtmoAuraResult) {
        List<Daily> dailyList = new ArrayList<>(forecastsResult.dailyForecast.size());

        for (MfForecastV2Result.ForecastProperties.ForecastV2 dailyForecast : forecastsResult.dailyForecast) {
            HalfDay halfDayDaytime = getHalfDay(true, hourly, forecastsResult.forecast, dailyForecast);
            HalfDay halfDayNighttime = getHalfDay(false, hourly, forecastsResult.forecast, dailyForecast);

            // Don’t add to the list if we have no data on it
            if (halfDayDaytime != null && halfDayNighttime != null) {
                dailyList.add(
                    new Daily(
                        new Date(dailyForecast.time * 1000),
                        dailyForecast.time * 1000,
                        halfDayDaytime,
                        halfDayNighttime,
                        new Astro(new Date(dailyForecast.sunriseTime * 1000), new Date(dailyForecast.sunsetTime * 1000)),
                        new Astro(new Date(ephemerisResult.properties.ephemeris.moonriseTime * 1000), new Date(ephemerisResult.properties.ephemeris.moonsetTime * 1000)),
                        new MoonPhase(CommonConverter.getMoonPhaseAngle(ephemerisResult.properties.ephemeris.moonPhaseDescription), ephemerisResult.properties.ephemeris.moonPhaseDescription),
                        getAirQuality(context, new Date(dailyForecast.time * 1000), aqiAtmoAuraResult, false),
                        new Pollen(null, null, null, null, null, null, null, null, null, null, null, null),
                        new UV(dailyForecast.uvIndex, null, null),
                        CommonConverter.getHoursOfDay(new Date(dailyForecast.sunriseTime * 1000), new Date(dailyForecast.sunsetTime * 1000))
                    )
                );
            }
        }
        return dailyList;
    }

    private static Float getRainCumul(MfForecastV2Result.ForecastProperties.HourForecast hourForecast) {
        if (hourForecast.rain1h != null) {
            return hourForecast.rain1h;
        } else if (hourForecast.rain3h != null) {
            return hourForecast.rain3h;
        } else if (hourForecast.rain6h != null) {
            return hourForecast.rain6h;
        } else if (hourForecast.rain12h != null) {
            return hourForecast.rain12h;
        } else if (hourForecast.rain24h != null) {
            return hourForecast.rain24h;
        }
        return null;
    }

    private static Float getSnowCumul(MfForecastV2Result.ForecastProperties.HourForecast hourForecast) {
        if (hourForecast.snow1h != null) {
            return hourForecast.snow1h;
        } else if (hourForecast.snow3h != null) {
            return hourForecast.snow3h;
        } else if (hourForecast.snow6h != null) {
            return hourForecast.snow6h;
        } else if (hourForecast.snow12h != null) {
            return hourForecast.snow12h;
        } else if (hourForecast.snow24h != null) {
            return hourForecast.snow24h;
        }
        return null;
    }

    private static UV getCurrentUV(Date currentDate, List<MfForecastV2Result.ForecastProperties.ForecastV2> dailyForecasts) {
        if (dailyForecasts == null || dailyForecasts.isEmpty())
            return new UV(null, null, null);

        MfForecastV2Result.ForecastProperties.ForecastV2 todayForecast = dailyForecasts.get(0);
        if (todayForecast == null || todayForecast.sunriseTime == null || todayForecast.sunsetTime == null)
            return new UV(null, null, null);

        return CommonConverter.getCurrentUV(todayForecast.uvIndex, currentDate, new Date(todayForecast.sunriseTime * 1000), new Date(todayForecast.sunsetTime * 1000));
    }

    private static Precipitation getHourlyPrecipitation(MfForecastV2Result.ForecastProperties.HourForecast hourlyForecast) {
        Float rainCumul = getRainCumul(hourlyForecast);
        Float snowCumul = getSnowCumul(hourlyForecast);
        Float totalCumul;

        if (rainCumul == null) {
            totalCumul = snowCumul;
        } else if (snowCumul == null) {
            totalCumul = rainCumul;
        } else {
            totalCumul = snowCumul + rainCumul;
        }

        return new Precipitation(
                totalCumul,
                null,
                rainCumul,
                snowCumul,
                null
        );
    }

    private static PrecipitationProbability getHourlyPrecipitationProbability(List<MfForecastV2Result.ForecastProperties.ProbabilityForecastV2> probabilityForecastResult, long dt) {
        Float rainProbability = null;
        Float snowProbability = null;
        Float iceProbability = null;

        for (MfForecastV2Result.ForecastProperties.ProbabilityForecastV2 probabilityForecast : probabilityForecastResult) {
            /*
             * Probablity are given every 3 hours, sometimes every 6 hours.
             * Sometimes every 3 hour-schedule give 3 hours probability AND 6 hours probability,
             * sometimes only one of them
             * It's not very clear but we take all hours in order.
             */
            if (probabilityForecast.time == dt || (probabilityForecast.time + 3600) == dt || (probabilityForecast.time + 3600 * 2) == dt) {
                if (probabilityForecast.rainHazard3h != null) {
                    rainProbability = probabilityForecast.rainHazard3h * 1f;
                } else if (probabilityForecast.rainHazard6h != null) {
                    rainProbability = probabilityForecast.rainHazard6h * 1f;
                }
                if (probabilityForecast.snowHazard3h != null) {
                    snowProbability = probabilityForecast.snowHazard3h * 1f;
                } else if (probabilityForecast.snowHazard6h != null) {
                    snowProbability = probabilityForecast.snowHazard6h * 1f;
                }
                iceProbability = probabilityForecast.freezingHazard * 1f;
            }

            /*
             * If it's found as part of the "6 hour schedule" and we find later a "3 hour schedule"
             * the "3 hour schedule" will overwrite the "6 hour schedule" below with the above
             */
            if ((probabilityForecast.time + 3600 * 3) == dt || (probabilityForecast.time + 3600 * 4) == dt || (probabilityForecast.time + 3600 * 5) == dt) {
                if (probabilityForecast.rainHazard6h != null) {
                    rainProbability = probabilityForecast.rainHazard6h * 1f;
                }
                if (probabilityForecast.snowHazard6h != null) {
                    snowProbability = probabilityForecast.snowHazard6h * 1f;
                }
                iceProbability = probabilityForecast.freezingHazard * 1f;
            }
        }

        List<Float> allProbabilities = new ArrayList<>();
        allProbabilities.add(rainProbability != null ? rainProbability : 0f);
        allProbabilities.add(snowProbability != null ? snowProbability : 0f);
        allProbabilities.add(iceProbability != null ? iceProbability : 0f);

        return new PrecipitationProbability(
                Collections.max(allProbabilities, null),
                null,
                rainProbability,
                snowProbability,
                iceProbability
        );
    }

    private static List<Hourly> getHourlyList(Context context, List<MfForecastV2Result.ForecastProperties.HourForecast> hourlyForecastResult, List<MfForecastV2Result.ForecastProperties.ProbabilityForecastV2> probabilityForecastResult) {
        List<Hourly> hourlyList = new ArrayList<>(hourlyForecastResult.size());
        for (MfForecastV2Result.ForecastProperties.HourForecast hourlyForecast : hourlyForecastResult) {
            if (hourlyForecast.t != null) {
                hourlyList.add(
                    new Hourly(
                        new Date(hourlyForecast.time * 1000),
                        hourlyForecast.time * 1000,
                        // TODO: Probably not the best way to check if it is daytime or nighttime
                        // Use CommonConverter.isDaylight(sunrise, sunset, new Date(hourlyForecast.time * 1000)) instead
                        !hourlyForecast.weatherIcon.endsWith("n"),
                        hourlyForecast.weatherDescription,
                        getWeatherCode(hourlyForecast.weatherIcon),
                        new Temperature(
                                toInt(hourlyForecast.t),
                                null,
                                null,
                                null,
                                hourlyForecast.tWindchill == null ? null : toInt(hourlyForecast.tWindchill),
                                null,
                                null
                        ),
                        getHourlyPrecipitation(hourlyForecast),
                        getHourlyPrecipitationProbability(probabilityForecastResult, hourlyForecast.time),
                        new Wind(
                                hourlyForecast.windIcon,
                                new WindDegree(hourlyForecast.windDirection.equals("Variable") ? 0.0f : Float.parseFloat(hourlyForecast.windDirection), hourlyForecast.windDirection.equals("Variable")),
                                hourlyForecast.windSpeed * 3.6f,
                                CommonConverter.getWindLevel(context, hourlyForecast.windSpeed * 3.6f)
                        ),
                        new UV(null, null, null)
                    )
                );
            }
        }
        return hourlyList;
    }

    private static List<Minutely> getMinutelyList(long sunrise, long sunset, @Nullable MfRainResult rainResult) {
        if (rainResult == null) {
            return new ArrayList<>();
        }
        List<Minutely> minutelyList = new ArrayList<>(rainResult.properties.rainForecasts.size());
        int minuteInterval;
        for (int i = 0; i < rainResult.properties.rainForecasts.size() ; ++i) {
            if (i + 1 < rainResult.properties.rainForecasts.size()) {
                minuteInterval = toInt((rainResult.properties.rainForecasts.get(i+1).time - rainResult.properties.rainForecasts.get(i).time) / 60);
            } else {
                minuteInterval = 10; // Last one is 10 minutes
            }
            minutelyList.add(
                    new Minutely(
                            new Date(rainResult.properties.rainForecasts.get(i).time * 1000),
                            rainResult.properties.rainForecasts.get(i).time * 1000,
                            CommonConverter.isDaylight(new Date(sunrise * 1000), new Date(sunset * 1000), new Date(rainResult.properties.rainForecasts.get(i).time * 1000)),
                            rainResult.properties.rainForecasts.get(i).rainIntensityDescription,
                            rainResult.properties.rainForecasts.get(i).rainIntensity > 1 ? WeatherCode.RAIN : getWeatherCode(null),
                            minuteInterval,
                            getPrecipitationIntensity(rainResult.properties.rainForecasts.get(i).rainIntensity),
                            null
                    )
            );
        }
        return minutelyList;
    }

    private static Double getPrecipitationIntensity(Integer rain) {
        switch(rain) {
            case 4: // More than 8 mm/hr
                return 10.0d;

            case 3: // Between 4 and 7 mm/hr
                return 5.5d;

            case 2: // Between 1 and 3 mm/hr
                return 2d;

            default:
                return 0d;
        }
    }

    private static List<Alert> getWarningsList(MfWarningsResult warningsResult) {
        List<Alert> alertList = new ArrayList<>(warningsResult.phenomenonsItems == null ? 0 : warningsResult.phenomenonsItems.size());
        if (warningsResult.timelaps != null) {
            for (int i = 0; i < warningsResult.timelaps.size(); ++i) {
                for (int j = 0; j < warningsResult.timelaps.get(i).timelapsItems.size(); ++j) {
                    // Do not warn when there is nothing to warn (green alert)
                    if (warningsResult.timelaps.get(i).timelapsItems.get(j).colorId > 1) {
                        alertList.add(
                            new Alert(
                                warningsResult.timelaps.get(i).phenomenonId,
                                new Date(warningsResult.timelaps.get(i).timelapsItems.get(j).beginTime * 1000),
                                warningsResult.timelaps.get(i).timelapsItems.get(j).beginTime * 1000,
                                getWarningType(warningsResult.timelaps.get(i).phenomenonId) + " — " + getWarningText(warningsResult.timelaps.get(i).timelapsItems.get(j).colorId),
                                "", // TODO: Longer description (I think there is a report in the web service when alert is orange or red)
                                getWarningType(warningsResult.timelaps.get(i).phenomenonId),
                                warningsResult.timelaps.get(i).timelapsItems.get(j).colorId,
                                getWarningColor(warningsResult.timelaps.get(i).timelapsItems.get(j).colorId)
                            )
                        );
                    }
                }
            }
            Alert.deduplication(alertList);
        }
        return alertList;
    }

    private static int toInt(double value) {
        return (int) (value + 0.5);
    }

    private static String getWarningType(int phemononId) {
        if (phemononId == 1) {
            return "Vent";
        } else if (phemononId == 2) {
            return "Pluie-Inondation";
        } else if (phemononId == 3) {
            return "Orages";
        } else if (phemononId == 4) {
            return "Crues";
        } else if (phemononId == 5) {
            return "Neige-Verglas";
        } else if (phemononId == 6) {
            return "Canicule";
        } else if (phemononId == 7) {
            return "Grand Froid";
        } else if (phemononId == 8) {
            return "Avalanches";
        } else if (phemononId == 9) {
            return "Vagues-Submersion";
        } else {
            return "Divers";
        }
    }

    private static String getWarningText(int colorId) {
        if (colorId == 4) {
            return "Vigilance absolue";
        } else if (colorId == 3) {
            return "Soyez très vigilant";
        } else if (colorId == 2) {
            return "Soyez attentif";
        } else {
            return "Pas de vigilance particulière";
        }
    }

    private static int getWarningColor(int colorId) {
        if (colorId == 4) {
            return Color.rgb(204, 0, 0);
        } else if (colorId == 3) {
            return Color.rgb(255, 184, 43);
        } else if (colorId == 2) {
            return Color.rgb(255, 246, 0);
        } else {
            return Color.rgb(49, 170, 53);
        }
    }

    private static WeatherCode getWeatherCode(@Nullable String icon) {
        if (icon == null) {
            return WeatherCode.CLEAR;
        }

        // Note: Météo France doesn't have icons for WIND
        if (icon.equals("p1") || icon.equals("p1j") || icon.equals("p1n")
                || icon.equals("p1bis") || icon.equals("p1bisj") || icon.equals("p1bisn")) {
            return WeatherCode.CLEAR;
        } else if (icon.equals("p2") || icon.equals("p2j") || icon.equals("p2n")
                || icon.equals("p2bis") || icon.equals("p2bisj") || icon.equals("p2bisn")) {
            return WeatherCode.PARTLY_CLOUDY;
        } else if (icon.equals("p3") || icon.equals("p3j") || icon.equals("p3n")
                || icon.equals("p3bis") || icon.equals("p3bisj") || icon.equals("p3bisn")) {
            return WeatherCode.CLOUDY;
        } else if (icon.equals("p4") || icon.equals("p4j") || icon.equals("p4n")
                || icon.equals("p5") || icon.equals("p5j") || icon.equals("p5n")
                || icon.equals("p5bis") || icon.equals("p5bisj") || icon.equals("p5bisn")) {
            return WeatherCode.HAZE;
        } else if (icon.equals("p6") || icon.equals("p6j") || icon.equals("p6n")
                || icon.equals("p6bis") || icon.equals("p6bisj") || icon.equals("p6bisn")
                || icon.equals("p6ter") || icon.equals("p6terj") || icon.equals("p6tern")
                || icon.equals("p7") || icon.equals("p7j") || icon.equals("p7n")
                || icon.equals("p7bis") || icon.equals("p7bisj") || icon.equals("p7bisn")
                || icon.equals("p8") || icon.equals("p8j") || icon.equals("p8n")
                || icon.equals("p8bis") || icon.equals("p8bisj") || icon.equals("p8bisn")) {
            return WeatherCode.FOG;
        } else if (icon.equals("p9") || icon.equals("p9j") || icon.equals("p9n")
                || icon.startsWith("p10") || icon.startsWith("p11") || icon.startsWith("p12")
                || icon.startsWith("p13") || icon.startsWith("p14")) { // We can start using "startsWith" when there are 2 digits
            return WeatherCode.RAIN;
        } else if (icon.startsWith("p16") || icon.startsWith("p24") || icon.startsWith("p25")) {
            return WeatherCode.THUNDERSTORM;
        } else if (icon.startsWith("p17") || icon.startsWith("p18")) {
            return WeatherCode.SLEET;
        } else if (icon.startsWith("p19") || icon.startsWith("p20")) {
            return WeatherCode.HAIL;
        } else if (icon.startsWith("p21") || icon.startsWith("p22") || icon.startsWith("p23")) {
            return WeatherCode.SNOW;
        } else if (icon.startsWith("p26") || icon.startsWith("p27") || icon.startsWith("p28")
                || icon.startsWith("p29")) {
            return WeatherCode.THUNDER;
        } else {
            return WeatherCode.CLEAR;
        }
    }
}