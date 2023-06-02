package wangdaye.com.geometricweather.weather.converters;

import android.content.Context;
import android.text.TextUtils;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;

import wangdaye.com.geometricweather.R;
import wangdaye.com.geometricweather.common.basic.models.Location;
import wangdaye.com.geometricweather.common.basic.models.options.provider.WeatherSource;
import wangdaye.com.geometricweather.common.basic.models.weather.AirQuality;
import wangdaye.com.geometricweather.common.basic.models.weather.Astro;
import wangdaye.com.geometricweather.common.basic.models.weather.Base;
import wangdaye.com.geometricweather.common.basic.models.weather.Current;
import wangdaye.com.geometricweather.common.basic.models.weather.Daily;
import wangdaye.com.geometricweather.common.basic.models.weather.HalfDay;
import wangdaye.com.geometricweather.common.basic.models.weather.History;
import wangdaye.com.geometricweather.common.basic.models.weather.Hourly;
import wangdaye.com.geometricweather.common.basic.models.weather.Pollen;
import wangdaye.com.geometricweather.common.basic.models.weather.Precipitation;
import wangdaye.com.geometricweather.common.basic.models.weather.PrecipitationProbability;
import wangdaye.com.geometricweather.common.basic.models.weather.Temperature;
import wangdaye.com.geometricweather.common.basic.models.weather.UV;
import wangdaye.com.geometricweather.common.basic.models.weather.Weather;
import wangdaye.com.geometricweather.common.basic.models.weather.WeatherCode;
import wangdaye.com.geometricweather.common.basic.models.weather.Wind;
import wangdaye.com.geometricweather.common.basic.models.weather.WindDegree;
import wangdaye.com.geometricweather.common.utils.DisplayUtils;
import wangdaye.com.geometricweather.common.utils.helpers.LogHelper;
import wangdaye.com.geometricweather.weather.json.openmeteo.OpenMeteoAirQualityResult;
import wangdaye.com.geometricweather.weather.json.openmeteo.OpenMeteoLocationResults;
import wangdaye.com.geometricweather.weather.json.openmeteo.OpenMeteoWeatherResult;
import wangdaye.com.geometricweather.weather.services.WeatherService;

public class OpenMeteoResultConverter {

    @NonNull
    public static Location convert(@Nullable Location location, OpenMeteoLocationResults.Result result, WeatherSource weatherSource) {
        if (location != null
                && !TextUtils.isEmpty(location.getProvince())
                && !TextUtils.isEmpty(location.getCity())
                && !TextUtils.isEmpty(location.getDistrict())) {
            return new Location(
                    String.valueOf(result.id),
                    result.latitude,
                    result.longitude,
                    TimeZone.getTimeZone(result.timezone),
                    !TextUtils.isEmpty(result.country) ? result.country : result.countryCode,
                    location.getProvince(),
                    location.getCity(),
                    "",
                    null,
                    weatherSource,
                    false,
                    false,
                    !TextUtils.isEmpty(result.countryCode)
                            && (result.countryCode.equalsIgnoreCase("cn")
                            || result.countryCode.equalsIgnoreCase("hk")
                            || result.countryCode.equalsIgnoreCase("tw"))
            );
        } else {
            return new Location(
                    String.valueOf(result.id),
                    result.latitude,
                    result.longitude,
                    TimeZone.getTimeZone(result.timezone),
                    !TextUtils.isEmpty(result.country) ? result.country : result.countryCode,
                    TextUtils.isEmpty(result.admin2)
                            ? (TextUtils.isEmpty(result.admin1)
                                ? (TextUtils.isEmpty(result.admin3)
                                    ? (TextUtils.isEmpty(result.admin4)
                                        ? ""
                                        : result.admin4)
                                    : result.admin3)
                                : result.admin1)
                            : result.admin2,
                    result.name,
                    "",
                    null,
                    weatherSource,
                    false,
                    false,
                    !TextUtils.isEmpty(result.countryCode)
                            && (result.countryCode.equalsIgnoreCase("cn")
                            || result.countryCode.equalsIgnoreCase("hk")
                            || result.countryCode.equalsIgnoreCase("tw"))
            );
        }
    }

    @NonNull
    public static WeatherService.WeatherResultWrapper convert(Context context,
                                                              Location location,
                                                              OpenMeteoWeatherResult weatherResult/*,
                                                              OpenMeteoAirQualityResult airQualityResult*/) {
        try {
            List<Daily> initialDailyList = getInitialDailyList(context, weatherResult.daily);

            Map<String, Map<String, List<Hourly>>> hourlyByDate = new HashMap<>();
            List<Hourly> hourlyList = new ArrayList<>();
            for (int i = 0; i < weatherResult.hourly.time.length; ++i) {
                Hourly hourly = new Hourly(
                        new Date(weatherResult.hourly.time[i] * 1000),
                        weatherResult.hourly.time[i] * 1000,
                        weatherResult.hourly.isDay[i] > 0,
                        weatherResult.hourly.weatherCode[i] != null ? getWeatherText(context, weatherResult.hourly.weatherCode[i]) : null,
                        weatherResult.hourly.weatherCode[i] != null ? getWeatherCode(weatherResult.hourly.weatherCode[i]) : null,
                        new Temperature(
                                weatherResult.hourly.temperature[i] != null ? Math.round(weatherResult.hourly.temperature[i]) : null,
                                null,
                                null,
                                weatherResult.hourly.apparentTemperature[i] != null ? Math.round(weatherResult.hourly.apparentTemperature[i]) : null,
                                null,
                                null,
                                null
                        ),
                        new Precipitation(
                                weatherResult.hourly.precipitation[i], // TODO: It seems to not be the addition of the later
                                null,
                                weatherResult.hourly.rain[i], // Add showers[i]
                                weatherResult.hourly.snowfall[i],
                                null
                        ),
                        new PrecipitationProbability(
                                weatherResult.hourly.precipitationProbability[i] != null ? (float) weatherResult.hourly.precipitationProbability[i] : null,
                                null,
                                null,
                                null,
                                null
                        ),
                        new Wind(
                                weatherResult.hourly.windDirection[i] != null ? CommonConverter.getWindDirection(Float.valueOf(weatherResult.hourly.windDirection[i]), location.isChina()) : null,
                                weatherResult.hourly.windDirection[i] != null ? new WindDegree((float) weatherResult.hourly.windDirection[i], false) : null,
                                weatherResult.hourly.windSpeed[i],
                                weatherResult.hourly.windSpeed[i] != null ? CommonConverter.getWindLevel(
                                        context,
                                        weatherResult.hourly.windSpeed[i]
                                ) : null
                        ),
                        new AirQuality(null, null, null, null, null, null, null, null),
                        new Pollen(null, null, null, null, null, null, null, null, null, null, null, null),
                        new UV(weatherResult.hourly.uvIndex[i] != null ? Math.round(weatherResult.hourly.uvIndex[i]) : null, null, null)
                );

                // We shift by 6 hours the hourly date, otherwise night times (00:00 to 05:59) would be on the wrong day
                Date theDayAtMidnight = DisplayUtils.toTimezoneNoHour(new Date((weatherResult.hourly.time[i] - 6 * 3600) * 1000), location.getTimeZone());
                String theDayFormatted = DisplayUtils.getFormattedDate(theDayAtMidnight, location.getTimeZone(), "yyyyMMdd");
                if (!hourlyByDate.containsKey(theDayFormatted)) {
                    hourlyByDate.put(theDayFormatted, new HashMap<String, List<Hourly>>() {{
                        put("day", new ArrayList<>());
                        put("night", new ArrayList<>());
                    }});
                }
                if (weatherResult.hourly.time[i] < (theDayAtMidnight.getTime() / 1000) + 18 * 3600) {
                    // 06:00 to 17:59 is the day
                    hourlyByDate.get(theDayFormatted).get("day").add(hourly);
                } else {
                    // 18:00 to 05:59 is the night
                    hourlyByDate.get(theDayFormatted).get("night").add(hourly);
                }

                // Add to the app only if starts in the current hour
                if ((weatherResult.hourly.time[i]) >= (System.currentTimeMillis() / 1000) - 3600) {
                    hourlyList.add(hourly);
                }
            }
            List<Daily> dailyList = CommonConverter.completeDailyListWithHourlyList(initialDailyList, hourlyByDate, location.getTimeZone());

            Weather weather = new Weather(
                    new Base(
                            location.getCityId(),
                            System.currentTimeMillis(),
                            new Date(),
                            System.currentTimeMillis(),
                            new Date(),
                            System.currentTimeMillis()
                    ),
                    new Current(
                            getWeatherText(context, weatherResult.currentWeather.weatherCode),
                            getWeatherCode(weatherResult.currentWeather.weatherCode),
                            new Temperature(Math.round(weatherResult.currentWeather.temperature), null, null, null, null, null, null),
                            new Precipitation(null, null, null, null, null),
                            new Wind(
                                    weatherResult.currentWeather.windDirection != null ? CommonConverter.getWindDirection(Float.valueOf(weatherResult.currentWeather.windDirection), location.isChina()) : null,
                                    weatherResult.currentWeather.windDirection != null ? new WindDegree((float) weatherResult.currentWeather.windDirection, false) : null,
                                    weatherResult.currentWeather.windSpeed,
                                    weatherResult.currentWeather.windSpeed != null ? CommonConverter.getWindLevel(
                                            context,
                                            weatherResult.currentWeather.windSpeed
                                    ) : null
                            ),
                            null,
                            null,
                            null,
                            null,
                            null,
                            null,
                            null,
                            null,
                            null,
                            null
                    ),
                    new History(
                            new Date(weatherResult.daily.time[0] * 1000),
                            weatherResult.daily.time[0] * 1000,
                            weatherResult.daily.temperatureMax[0] != null ? Math.round(weatherResult.daily.temperatureMax[0]) : null,
                            weatherResult.daily.temperatureMin[0] != null ? Math.round(weatherResult.daily.temperatureMin[0]) : null
                    ),
                    dailyList,
                    hourlyList,
                    new ArrayList<>(),
                    new ArrayList<>()
            );
            return new WeatherService.WeatherResultWrapper(weather);
        } catch (Exception ignored) {
            return new WeatherService.WeatherResultWrapper(null);
        }
    }

    private static List<Daily> getInitialDailyList(Context context, OpenMeteoWeatherResult.Daily dailyResult) {
        List<Daily> initialDailyList = new ArrayList<>(dailyResult.time.length - 1);
        for (int i = 1; i < dailyResult.time.length; ++i) {
            Daily daily = new Daily(
                    new Date(dailyResult.time[i] * 1000),
                    dailyResult.time[i] * 1000,
                    new HalfDay(
                            null, // Completed later with hourly data
                            null, // Completed later with hourly data
                            null, // Completed later with hourly data
                            new Temperature(
                                    dailyResult.temperatureMax[i] != null ? Math.round(dailyResult.temperatureMax[i]) : null,
                                    null,
                                    null,
                                    dailyResult.apparentTemperatureMax[i] != null ? Math.round(dailyResult.apparentTemperatureMax[i]) : null,
                                    null,
                                    null,
                                    null
                            ),
                            null, // TODO: Complete with hourly data
                            null, // TODO: Complete with hourly data
                            null, // TODO: Complete with hourly data
                            null, // TODO: Complete with hourly data
                            null
                    ),
                    // For night temperature, we take the minTemperature from the following day
                    (i + 1) < dailyResult.time.length ? new HalfDay(
                            null, // Completed later with hourly data
                            null, // Completed later with hourly data
                            null, // Completed later with hourly data
                            new Temperature(
                                    dailyResult.temperatureMin[i + 1] != null ? Math.round(dailyResult.temperatureMin[i + 1]) : null,
                                    null,
                                    null,
                                    dailyResult.apparentTemperatureMin[i + 1] != null ? Math.round(dailyResult.apparentTemperatureMin[i + 1]) : null,
                                    null,
                                    null,
                                    null
                            ),
                            null, // TODO: Complete with hourly data
                            null, // TODO: Complete with hourly data
                            null, // TODO: Complete with hourly data
                            null, // TODO: Complete with hourly data
                            null
                    ) : null,
                    new Astro(
                            dailyResult.sunrise[i] != null ? new Date(dailyResult.sunrise[i] * 1000) : null,
                            dailyResult.sunset[i] != null ? new Date(dailyResult.sunset[i] * 1000) : null
                    ),
                    null,
                    null,
                    null, // TODO: Complete with hourly data
                    null, // TODO: Complete with hourly data
                    dailyResult.uvIndexMax[i] != null ? new UV(
                            Math.round(dailyResult.uvIndexMax[i]),
                            CommonConverter.getUVLevel(context, Math.round(dailyResult.uvIndexMax[i])),
                            null
                    ) : null,
                    dailyResult.sunrise[i] != null && dailyResult.sunset[i] != null ?
                            CommonConverter.getHoursOfDay(new Date(dailyResult.sunrise[i] * 1000), new Date(dailyResult.sunset[i] * 1000))
                            : null
            );
            initialDailyList.add(daily);
        }
        return initialDailyList;
    }

    private static String getWeatherText(Context context, Integer icon) {
        if (icon == null) {
            return null;
        }
        if (icon == 0) {
            return context.getString(R.string.weather_text_clear_sky);
        } else if (icon == 1) {
            return context.getString(R.string.weather_text_mainly_clear);
        } else if (icon == 2) {
            return context.getString(R.string.weather_text_partly_cloudy);
        } else if (icon == 3) {
            return context.getString(R.string.weather_text_overcast);
        } else if (icon == 45) {
            return context.getString(R.string.weather_text_fog);
        } else if (icon == 48) {
            return context.getString(R.string.weather_text_depositing_rime_fog);
        } else if (icon == 51) {
            return context.getString(R.string.weather_text_drizzle_light_intensity);
        } else if (icon == 53) {
            return context.getString(R.string.weather_text_drizzle_moderate_intensity);
        } else if (icon == 55) {
            return context.getString(R.string.weather_text_drizzle_dense_intensity);
        } else if (icon == 56) {
            return context.getString(R.string.weather_text_freezing_drizzle_light_intensity);
        } else if (icon == 57) {
            return context.getString(R.string.weather_text_freezing_drizzle_dense_intensity);
        } else if (icon == 61) {
            return context.getString(R.string.weather_text_rain_slight_intensity);
        } else if (icon == 63) {
            return context.getString(R.string.weather_text_rain_moderate_intensity);
        } else if (icon == 65) {
            return context.getString(R.string.weather_text_rain_heavy_intensity);
        } else if (icon == 66) {
            return context.getString(R.string.weather_text_freezing_rain_light_intensity);
        } else if (icon == 67) {
            return context.getString(R.string.weather_text_freezing_rain_heavy_intensity);
        } else if (icon == 71) {
            return context.getString(R.string.weather_text_snow_slight_intensity);
        } else if (icon == 73) {
            return context.getString(R.string.weather_text_snow_moderate_intensity);
        } else if (icon == 75) {
            return context.getString(R.string.weather_text_snow_heavy_intensity);
        } else if (icon == 77) {
            return context.getString(R.string.weather_text_snow_grains);
        } else if (icon == 80) {
            return context.getString(R.string.weather_text_rain_showers_slight);
        } else if (icon == 81) {
            return context.getString(R.string.weather_text_rain_showers_moderate);
        } else if (icon == 82) {
            return context.getString(R.string.weather_text_rain_showers_violent);
        } else if (icon == 85) {
            return context.getString(R.string.weather_text_snow_showers_slight);
        } else if (icon == 86) {
            return context.getString(R.string.weather_text_snow_showers_heavy);
        } else if (icon == 95) {
            return context.getString(R.string.weather_text_thunderstorm_slight_or_moderate);
        } else if (icon == 96) {
            return context.getString(R.string.weather_text_thunderstorm_with_slight_hail);
        } else if (icon == 99) {
            return context.getString(R.string.weather_text_thunderstorm_with_heavy_hail);
        } else {
            return null;
        }
    }

    private static WeatherCode getWeatherCode(Integer icon) {
        if (icon == null) {
            return null;
        }
        if (icon == 0 || icon == 1) { // Clear sky or Mainly clear
            return WeatherCode.CLEAR;
        } else if (icon == 2) { // Partly cloudy
            return WeatherCode.PARTLY_CLOUDY;
        } else if (icon == 3) { // Overcast
            return WeatherCode.CLOUDY;
        } else if (icon == 45 || icon == 48) { // Fog and depositing rime fog
            return WeatherCode.FOG;
        } else if (icon == 51 || icon == 53 || icon == 55 // Drizzle: Light, moderate, and dense intensity
                || icon == 56 || icon == 57 // Freezing Drizzle: Light and dense intensity
                || icon == 61 || icon == 63 || icon == 65 // Rain: Slight, moderate and heavy intensity
                || icon == 66 || icon == 67 // Freezing Rain: Light and heavy intensity
                || icon == 80 || icon == 81 || icon == 82) { // Rain showers: Slight, moderate, and violent
            return WeatherCode.RAIN;
        } else if (icon == 71 || icon == 73 || icon == 75 // Snow fall: Slight, moderate, and heavy intensity
                || icon == 85 || icon == 86) { // Snow showers slight and heavy
            return WeatherCode.SNOW;
        } else if (icon == 77) { // Snow grains
            return WeatherCode.SLEET;
        } else if (icon == 95 // Thunderstorm: Slight or moderate
                || icon == 96 || icon == 99) { // Thunderstorm with slight and heavy hail
            return WeatherCode.THUNDERSTORM;
        } else {
            return WeatherCode.CLOUDY;
        }
    }
}
