package wangdaye.com.geometricweather.weather.converters;

import android.content.Context;
import android.graphics.Color;
import android.text.TextUtils;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
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
import wangdaye.com.geometricweather.common.basic.models.weather.History;
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
import wangdaye.com.geometricweather.weather.json.mf.MfForecastResult;
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
                                                              MfForecastResult forecastResult,
                                                              MfEphemerisResult ephemerisResult,
                                                              MfRainResult rainResult,
                                                              MfWarningsResult warningsResult,
                                                              @Nullable AtmoAuraQAResult aqiAtmoAuraResult) {
        try {
            Weather weather = new Weather(
                    new Base(
                            location.getCityId(),
                            System.currentTimeMillis(),
                            new Date(forecastResult.updatedOn * 1000),
                            forecastResult.updatedOn * 1000,
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
                                    (float) currentResult.observation.wind.speed,
                                    CommonConverter.getWindLevel(context, currentResult.observation.wind.speed)
                            ),
                            new UV(null, null, null),
                            getAirQuality(new Date(), aqiAtmoAuraResult),
                            null,
                            null,
                            null,
                            null,
                            null,
                            null,
                            null,
                            null
                    ),
                    new History( // FIXME: Fill in with data from yesterday observation instead of today
                            new Date((forecastResult.updatedOn - 24 * 60 * 60) * 1000),
                            (forecastResult.updatedOn - 24 * 60 * 60) * 1000,
                            toInt(forecastResult.dailyForecasts.get(0).temperature.max),
                            toInt(forecastResult.dailyForecasts.get(0).temperature.min)
                    ),
                    getDailyList(context, forecastResult, ephemerisResult, aqiAtmoAuraResult),
                    getHourlyList(forecastResult.forecasts),
                    getMinutelyList(forecastResult.dailyForecasts.get(0).sun.rise, forecastResult.dailyForecasts.get(0).sun.set, rainResult),
                    getWarningsList(warningsResult)
            );
            return new WeatherService.WeatherResultWrapper(weather);
        } catch (Exception ignored) {
            Log.d("GEOM", ignored.getMessage());
            for (StackTraceElement stackTraceElement : ignored.getStackTrace()) {
                Log.d("GEOM", stackTraceElement.toString());
            }
            return new WeatherService.WeatherResultWrapper(null);
        }
    }

    // This can be improved by adding Aqi results from other regions
    private static AirQuality getAirQuality(Date requestedDate, @Nullable AtmoAuraQAResult aqiAtmoAuraResult) {
        if (aqiAtmoAuraResult == null) {
            return new AirQuality(
                    null, null,
                    null, null,
                    null, null,
                    null, null
            );
        } else {
            SimpleDateFormat fmt = new SimpleDateFormat("yyyyMMdd");
            if (fmt.format(requestedDate).equals(fmt.format(aqiAtmoAuraResult.indexs.yesterday.date))) {
                return new AirQuality(
                        aqiAtmoAuraResult.indexs.yesterday.aggregatedIndex.quali, (int) Math.round(aqiAtmoAuraResult.indexs.yesterday.aggregatedIndex.val),
                        null, (float) aqiAtmoAuraResult.indexs.yesterday.pm10.val,
                        null, (float) aqiAtmoAuraResult.indexs.yesterday.no2.val,
                        (float) aqiAtmoAuraResult.indexs.yesterday.o3.val, null
                );
            } else if (fmt.format(requestedDate).equals(fmt.format(aqiAtmoAuraResult.indexs.today.date))) {
                return new AirQuality(
                        aqiAtmoAuraResult.indexs.today.aggregatedIndex.quali, (int) Math.round(aqiAtmoAuraResult.indexs.today.aggregatedIndex.val),
                        null, (float) aqiAtmoAuraResult.indexs.today.pm10.val,
                        null, (float) aqiAtmoAuraResult.indexs.today.no2.val,
                        (float) aqiAtmoAuraResult.indexs.today.o3.val, null
                );
            } else if (fmt.format(requestedDate).equals(fmt.format(aqiAtmoAuraResult.indexs.tomorrow.date))) {
                return new AirQuality(
                        aqiAtmoAuraResult.indexs.tomorrow.aggregatedIndex.quali, (int) Math.round(aqiAtmoAuraResult.indexs.tomorrow.aggregatedIndex.val),
                        null, (float) aqiAtmoAuraResult.indexs.tomorrow.pm10.val,
                        null, (float) aqiAtmoAuraResult.indexs.tomorrow.no2.val,
                        (float) aqiAtmoAuraResult.indexs.tomorrow.o3.val, null
                );
            } else if (aqiAtmoAuraResult.indexs.inTwoDays != null && fmt.format(requestedDate).equals(fmt.format(aqiAtmoAuraResult.indexs.inTwoDays.date))) {
                return new AirQuality(
                        aqiAtmoAuraResult.indexs.inTwoDays.aggregatedIndex.quali, (int) Math.round(aqiAtmoAuraResult.indexs.inTwoDays.aggregatedIndex.val),
                        null, (float) aqiAtmoAuraResult.indexs.inTwoDays.pm10.val,
                        null, (float) aqiAtmoAuraResult.indexs.inTwoDays.no2.val,
                        (float) aqiAtmoAuraResult.indexs.inTwoDays.o3.val, null
                );
            } else {
                return new AirQuality(
                        null, null,
                        null, null,
                        null, null,
                        null, null
                );
            }
        }
    }

    private static List<Daily> getDailyList(Context context, MfForecastResult forecastsResult, MfEphemerisResult ephemerisResult, @Nullable AtmoAuraQAResult aqiAtmoAuraResult) {
        List<Daily> dailyList = new ArrayList<>(forecastsResult.dailyForecasts.size());

        for (MfForecastResult.DailyForecast dailyForecast : forecastsResult.dailyForecasts) {
            dailyList.add(
                    new Daily(
                            new Date(dailyForecast.dt * 1000),
                            dailyForecast.dt * 1000,
                            new HalfDay(
                                    dailyForecast.weather12H == null ? "" : dailyForecast.weather12H.desc,
                                    dailyForecast.weather12H == null ? "" : dailyForecast.weather12H.desc,
                                    dailyForecast.weather12H == null ? WeatherCode.CLEAR : getWeatherCode(dailyForecast.weather12H.icon),
                                    new Temperature(
                                            dailyForecast.temperature.max == null ? 0 : toInt(dailyForecast.temperature.max), // FIXME ?
                                            null,
                                            null,
                                            null,
                                            null,
                                            null,
                                            null
                                    ),
                                    new Precipitation( // FIXME
                                            null,
                                            null,
                                            null,
                                            null,
                                            null
                                    ),
                                    new PrecipitationProbability( // FIXME
                                            null,
                                            null,
                                            null,
                                            null,
                                            null
                                    ),
                                    new PrecipitationDuration( // FIXME
                                            null,
                                            null,
                                            null,
                                            null,
                                            null
                                    ),
                                    new Wind( // FIXME: Info does not exist
                                            "Pas d’info",
                                            new WindDegree(0, false),
                                            null,
                                            "Pas d’info"
                                    ),
                                    null
                            ),
                            new HalfDay(
                                    dailyForecast.weather12H == null ? "" : dailyForecast.weather12H.desc,
                                    dailyForecast.weather12H == null ? "" : dailyForecast.weather12H.desc,
                                    dailyForecast.weather12H == null ? WeatherCode.CLEAR : getWeatherCode(dailyForecast.weather12H.icon), // FIXME
                                    new Temperature(
                                            dailyForecast.temperature.min == null ? 0 : toInt(dailyForecast.temperature.min), // FIXME ?
                                            null,
                                            null,
                                            null,
                                            null,
                                            null,
                                            null
                                    ),
                                    new Precipitation( // FIXME
                                            null,
                                            null,
                                            null,
                                            null,
                                            null
                                    ),
                                    new PrecipitationProbability( // FIXME
                                            null,
                                            null,
                                            null,
                                            null,
                                            null
                                    ),
                                    new PrecipitationDuration( // FIXME
                                            null,
                                            null,
                                            null,
                                            null,
                                            null
                                    ),
                                    new Wind( // FIXME: Info does not exist
                                            "Pas d’info",
                                            new WindDegree(0, false),
                                            null,
                                            "Pas d’info"
                                    ),
                                    null
                            ),
                            new Astro(new Date(dailyForecast.sun.rise * 1000), new Date(dailyForecast.sun.set * 1000)),
                            // Note: Below is the same moon data for all days, but since we are only showing the data for the current day in the app, this does not matter
                            //new Astro(ephemerisResult.properties.ephemeris.moonriseTime, ephemerisResult.properties.ephemeris.moonsetTime), // FIXME: Weird issue, input is UTC (due to Z) but system thinks it's system timezone
                            new Astro(null, null),
                            new MoonPhase(CommonConverter.getMoonPhaseAngle(ephemerisResult.properties.ephemeris.moonPhaseDescription), ephemerisResult.properties.ephemeris.moonPhaseDescription),
                            getAirQuality(new Date(dailyForecast.dt * 1000), aqiAtmoAuraResult),
                            new Pollen(null, null, null, null, null, null, null, null, null, null, null, null),
                            new UV(dailyForecast.uv, null, null),
                            getHoursOfDay(new Date(dailyForecast.sun.rise * 1000), new Date(dailyForecast.sun.set * 1000))
                    )
            );
        }
        return dailyList;
    }

    private static List<Hourly> getHourlyList(List<MfForecastResult.Forecast> hourlyForecastResult) {
        List<Hourly> hourlyList = new ArrayList<>(hourlyForecastResult.size());
        for (MfForecastResult.Forecast hourlyForecast : hourlyForecastResult) {
            hourlyList.add(
                    new Hourly(
                            new Date(hourlyForecast.dt * 1000),
                            hourlyForecast.dt * 1000,
                            // TODO: Probably not the best way to check if it is daytime or nighttime
                            // Use CommonConverter.isDaylight(sunrise, sunset, new Date(hourlyForecast.dt * 1000)) instead
                            !hourlyForecast.weather.icon.endsWith("n"),
                            hourlyForecast.weather.desc,
                            getWeatherCode(hourlyForecast.weather.icon),
                            new Temperature(
                                    toInt(hourlyForecast.temperature.value),
                                    null,
                                    null,
                                    null,
                                    toInt(hourlyForecast.temperature.windChill),
                                    null,
                                    null
                            ),
                            new Precipitation(
                                    null,
                                    null,
                                    hourlyForecast.rain.cumul1H,
                                    hourlyForecast.snow.cumul1H,
                                    null
                            ),
                            new PrecipitationProbability(
                                    null,
                                    null,
                                    null,
                                    null,
                                    null
                            )
                    )
            );
        }
        return hourlyList;
    }

    private static List<Minutely> getMinutelyList(long sunrise, long sunset, @Nullable MfRainResult rainResult) {
        //if (rainResult == null) {
        return new ArrayList<>();
        //}
        /*List<Minutely> minutelyList = new ArrayList<>(rainResult.rainForecasts.size());
        for (MfRainResult.RainForecast rainForecast : rainResult.rainForecasts) {
            minutelyList.add(
                    new Minutely(
                            new Date(rainForecast.date * 1000),
                            rainForecast.date,
                            CommonConverter.isDaylight(new Date(sunrise * 1000), new Date(sunset * 1000), new Date(rainForecast.date * 1000)),
                            rainForecast.desc,
                            getWeatherCode(interval.IconCode), // TODO
                            0, // TODO
                            0, // TODO
                            0 // TODO
                    )
            );
        }
        return minutelyList;*/
    }

    private static List<Alert> getWarningsList(MfWarningsResult warningsResult) {
        List<Alert> alertList = new ArrayList<>(warningsResult.phenomenonsItems == null ? 0 : warningsResult.phenomenonsItems.size());
        if (warningsResult.phenomenonsItems != null) {
            for (MfWarningsResult.PhenomenonMaxColor phemononItem : warningsResult.phenomenonsItems) {
                if (phemononItem.phenomenoMaxColorId > 1) { // Do not warn when there is nothing to warn (green alert)
                    alertList.add(
                            new Alert(
                                    phemononItem.phenomenonId,
                                    new Date(warningsResult.updateTime * 1000), // FIXME: Do not take updateTime but phenomonon time instead
                                    warningsResult.updateTime * 1000, // FIXME: Do not take updateTime but phenomonon time instead
                                    getWarningType(phemononItem.phenomenonId) + " — " + getWarningText(phemononItem.phenomenoMaxColorId),
                                    "", // TODO: Longer description (I think there is a report in the web service when alert is orange or red)
                                    getWarningType(phemononItem.phenomenonId),
                                    phemononItem.phenomenoMaxColorId, // TODO: Check Priority
                                    getWarningColor(phemononItem.phenomenoMaxColorId)
                            )
                    );
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

    private static float getHoursOfDay(Date sunrise, Date sunset) {
        return (float) (
                (sunset.getTime() - sunrise.getTime()) // get delta millisecond.
                        / 1000 // second.
                        / 60 // minutes.
                        / 60.0 // hours.
        );
    }


    /*private static AirQuality getDailyAirQuality(Context context,
                                                 List<MfDailyResult.DailyForecasts.AirAndPollen> list) {
        MfDailyResult.DailyForecasts.AirAndPollen aqi = getAirAndPollen(list, "AirQuality");
        Integer index = aqi == null ? null : aqi.Value;
        if (index != null && index == 0) {
            index = null;
        }
        return new AirQuality(
                CommonConverter.getAqiQuality(context, index),
                index,
                null,
                null,
                null,
                null,
                null,
                null
        );
    }
    private static Pollen getDailyPollen(List<MfDailyResult.DailyForecasts.AirAndPollen> list) {
        MfDailyResult.DailyForecasts.AirAndPollen grass = getAirAndPollen(list, "Grass");
        MfDailyResult.DailyForecasts.AirAndPollen mold = getAirAndPollen(list, "Mold");
        MfDailyResult.DailyForecasts.AirAndPollen ragweed = getAirAndPollen(list, "Ragweed");
        MfDailyResult.DailyForecasts.AirAndPollen tree = getAirAndPollen(list, "Tree");
        return new Pollen(
                grass == null ? null : grass.Value,
                grass == null ? null : grass.CategoryValue,
                grass == null ? null : grass.Category,
                mold == null ? null : mold.Value,
                mold == null ? null : mold.CategoryValue,
                mold == null ? null : mold.Category,
                ragweed == null ? null : ragweed.Value,
                ragweed == null ? null : ragweed.CategoryValue,
                ragweed == null ? null : ragweed.Category,
                tree == null ? null : tree.Value,
                tree == null ? null : tree.CategoryValue,
                tree == null ? null : tree.Category
        );
    }
    private static UV getDailyUV(List<MfDailyResult.DailyForecasts.AirAndPollen> list) {
        MfDailyResult.DailyForecasts.AirAndPollen uv = getAirAndPollen(list, "UVIndex");
        return new UV(
                uv == null ? null : uv.Value,
                uv == null ? null : uv.Category,
                null
        );
    }
    @Nullable
    private static MfDailyResult.DailyForecasts.AirAndPollen getAirAndPollen(
            List<MfDailyResult.DailyForecasts.AirAndPollen> list, String name) {
        for (MfDailyResult.DailyForecasts.AirAndPollen item : list) {
            if (item.Name.equals(name)) {
                return item;
            }
        }
        return null;
    }*/
}