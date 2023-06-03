package wangdaye.com.geometricweather.weather.converters;

import android.content.Context;
import android.graphics.Color;
import android.text.TextUtils;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
    public static List<Location> convert(List<MfLocationResult> resultList) {
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
            for (MfLocationResult r : resultList) {
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
            for (MfLocationResult r : resultList) {
                locationList.add(convert(null, r, map));
            }
        }
        return locationList;
    }

    @NonNull
    protected static Location convert(@Nullable Location location, MfLocationResult result, TimeZoneMap map) {
        if (location != null
                && !TextUtils.isEmpty(location.getProvince())
                && !TextUtils.isEmpty(location.getCity())
                && !TextUtils.isEmpty(location.getDistrict())) {
            return new Location(
                    result.lat + "," + result.lon, // Use coordinates as cityId
                    (float) result.lat,
                    (float) result.lon,
                    CommonConverterKt.getTimeZoneForPosition(map, result.lat, result.lon),
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
                    result.lat + "," + result.lon, // Use coordinates as cityId
                    (float) result.lat,
                    (float) result.lon,
                    CommonConverterKt.getTimeZoneForPosition(map, result.lat, result.lon),
                    result.country,
                    result.admin2 != null ? result.admin2 : "", // Domain (département)
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
            Map<String, Map<String, Map<String, Integer>>> normalizedAqiAtmoAuraPolluants = getNormalizedAqiAtmoAuraPolluants(aqiAtmoAuraResult, location.getTimeZone());
            List<Hourly> hourly = getHourlyList(context, forecastV2Result.properties.forecast, forecastV2Result.properties.probabilityForecast);
            Weather weather = new Weather(
                    new Base(
                            location.getCityId(),
                            forecastV2Result.updateTime,
                            new Date()
                    ),
                    new Current(
                            currentResult.properties != null && currentResult.properties.gridded != null ? currentResult.properties.gridded.weatherDescription : "",
                            getWeatherCode(currentResult.properties != null && currentResult.properties.gridded != null ? currentResult.properties.gridded.weatherIcon : null),
                            new Temperature(
                                    currentResult.properties != null && currentResult.properties.gridded != null ? toInt(currentResult.properties.gridded.temperature) : hourly.get(0).getTemperature().getTemperature(),
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
                            currentResult.properties != null && currentResult.properties.gridded != null ? new Wind(
                                    currentResult.properties.gridded.windIcon,
                                    new WindDegree((float) currentResult.properties.gridded.windDirection, currentResult.properties.gridded.windDirection == -1),
                                    currentResult.properties.gridded.windSpeed * 3.6f,
                                    CommonConverterKt.getWindLevel(context, currentResult.properties.gridded.windSpeed * 3.6f)
                            ) : null,
                            getCurrentUV(context, new Date(), location.getTimeZone(), forecastV2Result.properties.dailyForecast),
                            getAirQuality(context, new Date(), location.getTimeZone(), normalizedAqiAtmoAuraPolluants, true),
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
                    getDailyList(context, location.getTimeZone(), forecastV2Result.properties, hourly, ephemerisResult, normalizedAqiAtmoAuraPolluants),
                    hourly,
                    getMinutelyList(rainResult),
                    getWarningsList(warningsResult)
            );
            return new WeatherService.WeatherResultWrapper(weather);
        } catch (Exception e) {
            e.printStackTrace();
            return new WeatherService.WeatherResultWrapper(null);
        }
    }

    /**
     * Returns a Map that looks like:
     * {
     * "20230528": {
     * "2023052800": {
     * "o3": 40,
     * "pm25": 10
     * ...
     * },
     * "2023052801": {
     * "o3": 40,
     * "pm25": 10
     * ...
     * },
     * }
     * }
     */
    private static Map<String, Map<String, Map<String, Integer>>> getNormalizedAqiAtmoAuraPolluants(@Nullable AtmoAuraQAResult aqiAtmoAuraResult, TimeZone timeZone) {
        if (aqiAtmoAuraResult == null) {
            return null;
        } else {
            Map<String, Map<String, Map<String, Integer>>> normalizedAqiAtmoAuraPolluants = new HashMap<>();
            for (AtmoAuraQAResult.Polluant polluant : aqiAtmoAuraResult.polluants) {
                if (polluant.horaires != null) {
                    for (AtmoAuraQAResult.Polluant.Horaire horaire : polluant.horaires) {
                        if (horaire.concentration != null) {
                            String date = DisplayUtils.getFormattedDate(horaire.datetimeEcheance, timeZone, "yyyyMMdd");
                            String hour = DisplayUtils.getFormattedDate(horaire.datetimeEcheance, timeZone, "yyyyMMddHH");

                            if (!normalizedAqiAtmoAuraPolluants.containsKey(date)) {
                                normalizedAqiAtmoAuraPolluants.put(date, new HashMap<>());
                            }
                            if (!normalizedAqiAtmoAuraPolluants.get(date).containsKey(hour)) {
                                normalizedAqiAtmoAuraPolluants.get(date).put(hour, new HashMap<>());
                            }
                            normalizedAqiAtmoAuraPolluants.get(date).get(hour).put(polluant.polluant, horaire.concentration);
                        }
                    }
                }
            }
            return normalizedAqiAtmoAuraPolluants;
        }
    }

    // This can be improved by adding Aqi results from other regions
    private static @Nullable AirQuality getAirQuality(Context context, Date requestedDate, TimeZone timeZone, @Nullable Map<String, Map<String, Map<String, Integer>>> normalizedAqiAtmoAuraPolluants, Boolean hourlyAqi) {
        AirQuality emptyAirQuality = null;
        if (normalizedAqiAtmoAuraPolluants == null) {
            return emptyAirQuality;
        } else {
            String requestDate = DisplayUtils.getFormattedDate(requestedDate, timeZone, "yyyyMMdd");
            if (!normalizedAqiAtmoAuraPolluants.containsKey(requestDate)
                    || normalizedAqiAtmoAuraPolluants.get(requestDate).size() < 24) {
                return emptyAirQuality;
            }

            Integer highestO3 = null;
            Integer highestNO2 = null;
            Integer totalPM25 = null;
            int countPM25 = 0;
            Integer totalPM10 = null;
            int countPM10 = 0;
            Integer highestSO2 = null;
            if (hourlyAqi) {
                String requestHour = DisplayUtils.getFormattedDate(requestedDate, timeZone, "yyyyMMddHH");
                if (!normalizedAqiAtmoAuraPolluants.get(requestDate).containsKey(requestHour)) {
                    return emptyAirQuality;
                } else {
                    for (Map.Entry<String, Integer> polluantSet : normalizedAqiAtmoAuraPolluants.get(requestDate).get(requestHour).entrySet()) {
                        // For PM 2.5 and PM 10, it's the daily mean of the day
                        // For the others (O3, No2, SO2), it's the maximum value
                        switch (polluantSet.getKey()) {
                            case "o3":
                                highestO3 = polluantSet.getValue();
                                break;
                            case "no2":
                                highestNO2 = polluantSet.getValue();
                                break;
                            case "pm2.5":
                                totalPM25 = polluantSet.getValue();
                                ++countPM25;
                                break;
                            case "pm10":
                                totalPM10 = polluantSet.getValue();
                                ++countPM10;
                                break;
                            case "so2":
                                highestSO2 = polluantSet.getValue();
                                break;
                        }
                    }
                }
            } else {
                // Loop the day
                for (Map.Entry<String, Map<String, Integer>> hourSet : normalizedAqiAtmoAuraPolluants.get(requestDate).entrySet()) {
                    for (Map.Entry<String, Integer> polluantSet : hourSet.getValue().entrySet()) {
                        // For PM 2.5 and PM 10, it's the daily mean of the day
                        // For the others (O3, No2, SO2), it's the maximum value
                        switch (polluantSet.getKey()) {
                            case "o3":
                                if (highestO3 == null || highestO3 < polluantSet.getValue()) {
                                    highestO3 = polluantSet.getValue();
                                }
                                break;
                            case "no2":
                                if (highestNO2 == null || highestNO2 < polluantSet.getValue()) {
                                    highestNO2 = polluantSet.getValue();
                                }
                                break;
                            case "pm2.5":
                                if (totalPM25 == null) {
                                    totalPM25 = polluantSet.getValue();
                                } else {
                                    totalPM25 += polluantSet.getValue();
                                }
                                ++countPM25;
                                break;
                            case "pm10":
                                if (totalPM10 == null) {
                                    totalPM10 = polluantSet.getValue();
                                } else {
                                    totalPM10 += polluantSet.getValue();
                                }
                                ++countPM10;
                                break;
                            case "so2":
                                if (highestSO2 == null || highestSO2 < polluantSet.getValue()) {
                                    highestSO2 = polluantSet.getValue();
                                }
                                break;
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
                    aqiIndex,
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

    private static HalfDay getHalfDay(boolean isDaytime, List<Hourly> hourly, List<MfForecastV2Result.ForecastProperties.HourForecast> hourlyForecast, MfForecastV2Result.ForecastProperties.ForecastV2 dailyForecast, Date theDayInLocal) {
        Integer temp = null;
        Integer tempWindChill = null;

        float precipitationTotal = 0.0f;
        float precipitationRain = 0.0f;
        float precipitationSnow = 0.0f;

        Float probPrecipitationTotal = 0.0f;
        Float probPrecipitationRain = 0.0f;
        Float probPrecipitationSnow = 0.0f;
        Float probPrecipitationIce = 0.0f;

        Wind wind = null;

        // In AccuWeather provider, a day is considered from 6:00 to 17:59 and night from 18:00 to 5:59 the next day
        // So we implement it this way (same as MET Norway provider)
        for (Hourly hour : hourly) {
            // For temperatures, we loop through all hours from 6:00 to 5:59 (next day) to avoid having no max temperature after 18:00
            if (hour.getDate().getTime() >= theDayInLocal.getTime() + 6 * 3600 * 1000 && hour.getDate().getTime() < theDayInLocal.getTime() + 30 * 3600 * 1000) {
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

            if ((isDaytime && hour.getDate().getTime() >= theDayInLocal.getTime() + 6 * 3600 * 1000 && hour.getDate().getTime() < theDayInLocal.getTime() + 18 * 3600 * 1000)
                    || (!isDaytime && hour.getDate().getTime() >= theDayInLocal.getTime() + 18 * 3600 * 1000 && hour.getDate().getTime() < theDayInLocal.getTime() + 30 * 3600 * 1000)) {
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
                if ((hour.getWind() != null && hour.getWind().getSpeed() != null) && (wind == null || wind.getSpeed() == null || hour.getWind().getSpeed() > wind.getSpeed())) {
                    wind = hour.getWind();
                }
            }
        }

        Integer cloudCover = null;
        for (MfForecastV2Result.ForecastProperties.HourForecast hourForecast : hourlyForecast) {
            if ((isDaytime && hourForecast.time.getTime() >= theDayInLocal.getTime() + 6 * 3600 * 1000 && hourForecast.time.getTime() < theDayInLocal.getTime() + 18 * 3600 * 1000)
                    || (!isDaytime && hourForecast.time.getTime() >= theDayInLocal.getTime() + 18 * 3600 * 1000 && hourForecast.time.getTime() < theDayInLocal.getTime() + 30 * 3600 * 1000)) {
                if (cloudCover == null || (hourForecast.totalCloudCover != null && hourForecast.totalCloudCover > cloudCover)) {
                    cloudCover = hourForecast.totalCloudCover;
                }
            }
        }

        if (temp == null) {
            // Only add the daily reported value if we have no hourly data
            if (isDaytime && dailyForecast.tMax != null) {
                temp = toInt(dailyForecast.tMax);
            } else if (!isDaytime && dailyForecast.tMin != null) {
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

    private static List<Daily> getDailyList(Context context, TimeZone timeZone, MfForecastV2Result.ForecastProperties forecastsResult, List<Hourly> hourly, MfEphemerisResult ephemerisResult, @Nullable Map<String, Map<String, Map<String, Integer>>> normalizedAqiAtmoAuraPolluants) {
        List<Daily> dailyList = new ArrayList<>(forecastsResult.dailyForecast.size());

        for (MfForecastV2Result.ForecastProperties.ForecastV2 dailyForecast : forecastsResult.dailyForecast) {
            // Given as UTC, we need to convert in the correct timezone at 00:00
            Calendar dayInUTCCalendar = DisplayUtils.toCalendarWithTimeZone(dailyForecast.time, TimeZone.getTimeZone("UTC"));
            Calendar dayInLocalCalendar = Calendar.getInstance(timeZone);
            dayInLocalCalendar.set(Calendar.YEAR, dayInUTCCalendar.get(Calendar.YEAR));
            dayInLocalCalendar.set(Calendar.MONTH, dayInUTCCalendar.get(Calendar.MONTH));
            dayInLocalCalendar.set(Calendar.DAY_OF_MONTH, dayInUTCCalendar.get(Calendar.DAY_OF_MONTH));
            dayInLocalCalendar.set(Calendar.HOUR_OF_DAY, 0);
            dayInLocalCalendar.set(Calendar.MINUTE, 0);
            dayInLocalCalendar.set(Calendar.SECOND, 0);
            Date theDayInLocal = dayInLocalCalendar.getTime();

            HalfDay halfDayDaytime = getHalfDay(true, hourly, forecastsResult.forecast, dailyForecast, theDayInLocal);
            HalfDay halfDayNighttime = getHalfDay(false, hourly, forecastsResult.forecast, dailyForecast, theDayInLocal);

            // Don’t add to the list if we have no data on it
            if (halfDayDaytime != null && halfDayNighttime != null) {
                dailyList.add(
                        new Daily(
                                theDayInLocal,
                                halfDayDaytime,
                                halfDayNighttime,
                                new Astro(dailyForecast.sunriseTime, dailyForecast.sunsetTime),
                                new Astro(ephemerisResult.properties.ephemeris.moonriseTime, ephemerisResult.properties.ephemeris.moonsetTime),
                                new MoonPhase(CommonConverterKt.getMoonPhaseAngle(ephemerisResult.properties.ephemeris.moonPhaseDescription), ephemerisResult.properties.ephemeris.moonPhaseDescription),
                                getAirQuality(context, theDayInLocal, timeZone, normalizedAqiAtmoAuraPolluants, false),
                                new Pollen(null, null, null, null, null, null, null, null, null, null, null, null),
                                new UV(dailyForecast.uvIndex, null, null),
                                CommonConverterKt.getHoursOfDay(dailyForecast.sunriseTime, dailyForecast.sunsetTime)
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

    private static UV getCurrentUV(Context context, Date currentDate, TimeZone timeZone, List<MfForecastV2Result.ForecastProperties.ForecastV2> dailyForecasts) {
        if (dailyForecasts == null || dailyForecasts.isEmpty())
            return new UV(null, null, null);

        MfForecastV2Result.ForecastProperties.ForecastV2 todayForecast = dailyForecasts.get(0);
        if (todayForecast == null || todayForecast.sunriseTime == null || todayForecast.sunsetTime == null || todayForecast.uvIndex == null) {
            return new UV(null, null, null);
        }

        return CommonConverterKt.getCurrentUV(context, todayForecast.uvIndex, currentDate, todayForecast.sunriseTime, todayForecast.sunsetTime, timeZone);
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

    private static PrecipitationProbability getHourlyPrecipitationProbability(List<MfForecastV2Result.ForecastProperties.ProbabilityForecastV2> probabilityForecastResult, Date dt) {
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
            if (probabilityForecast.time.getTime() == dt.getTime() || probabilityForecast.time.getTime() + 3600 * 1000 == dt.getTime() || probabilityForecast.time.getTime() + 3600 * 2 * 1000 == dt.getTime()) {
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
            if ((probabilityForecast.time.getTime() + 3600 * 3 * 1000) == dt.getTime()
                    || (probabilityForecast.time.getTime() + 3600 * 4 * 1000) == dt.getTime()
                    || (probabilityForecast.time.getTime() + 3600 * 5 * 1000) == dt.getTime()) {
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
                                hourlyForecast.time,
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
                                probabilityForecastResult != null ? getHourlyPrecipitationProbability(probabilityForecastResult, hourlyForecast.time) : new PrecipitationProbability(null, null, null, null, null),
                                new Wind(
                                        hourlyForecast.windIcon,
                                        new WindDegree(hourlyForecast.windDirection.equals("Variable") ? 0.0f : Float.parseFloat(hourlyForecast.windDirection), hourlyForecast.windDirection.equals("Variable")),
                                        hourlyForecast.windSpeed * 3.6f,
                                        CommonConverterKt.getWindLevel(context, hourlyForecast.windSpeed * 3.6f)
                                ),
                                null,
                                null,
                                null
                        )
                );
            }
        }
        return hourlyList;
    }

    private static List<Minutely> getMinutelyList(@Nullable MfRainResult rainResult) {
        if (rainResult == null || rainResult.properties == null || rainResult.properties.rainForecasts == null) {
            return new ArrayList<>();
        }
        List<Minutely> minutelyList = new ArrayList<>(rainResult.properties.rainForecasts.size());
        int minuteInterval;
        for (int i = 0; i < rainResult.properties.rainForecasts.size(); ++i) {
            if (i + 1 < rainResult.properties.rainForecasts.size()) {
                minuteInterval = toInt((rainResult.properties.rainForecasts.get(i + 1).time.getTime() - rainResult.properties.rainForecasts.get(i).time.getTime()) / (60 * 1000));
            } else {
                minuteInterval = 10; // Last one is 10 minutes
            }
            minutelyList.add(
                    new Minutely(
                            rainResult.properties.rainForecasts.get(i).time,
                            rainResult.properties.rainForecasts.get(i).time.getTime(),
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
        switch (rain) {
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
                                        warningsResult.timelaps.get(i).timelapsItems.get(j).beginTime,
                                        warningsResult.timelaps.get(i).timelapsItems.get(j).beginTime.getTime(),
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