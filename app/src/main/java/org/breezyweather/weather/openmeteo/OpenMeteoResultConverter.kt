package org.breezyweather.weather.openmeteo

import android.content.Context
import org.breezyweather.BreezyWeather
import org.breezyweather.R
import org.breezyweather.common.basic.models.Location
import org.breezyweather.common.basic.models.options.provider.WeatherSource
import org.breezyweather.common.basic.models.weather.*
import org.breezyweather.common.utils.DisplayUtils
import org.breezyweather.weather.*
import org.breezyweather.weather.openmeteo.json.OpenMeteoAirQualityResult
import org.breezyweather.weather.openmeteo.json.OpenMeteoWeatherDaily
import org.breezyweather.weather.openmeteo.json.OpenMeteoLocationResult
import org.breezyweather.weather.openmeteo.json.OpenMeteoWeatherResult
import org.breezyweather.weather.mf.getFrenchDepartmentCode
import org.breezyweather.weather.WeatherService.WeatherResultWrapper
import java.util.Date
import java.util.TimeZone
import kotlin.math.roundToInt

fun convert(
    location: Location?,
    result: OpenMeteoLocationResult,
    weatherSource: WeatherSource
): Location {
    return if (location != null && !location.province.isNullOrEmpty()
        && location.city.isNotEmpty()
        && !location.district.isNullOrEmpty()
    ) {
        Location(
            cityId = result.id.toString(),
            latitude = result.latitude,
            longitude = result.longitude,
            timeZone = TimeZone.getTimeZone(result.timezone),
            country = if (!result.country.isNullOrEmpty()) result.country else result.countryCode,
            countryCode = result.countryCode,
            province = location.province,
            provinceCode = location.provinceCode,
            city = location.city,
            weatherSource = weatherSource,
            isChina = result.countryCode.isNotEmpty()
                    && (result.countryCode.equals("cn", ignoreCase = true)
                    || result.countryCode.equals("hk", ignoreCase = true)
                    || result.countryCode.equals("tw", ignoreCase = true))
        )
    } else {
        Location(
            cityId = result.id.toString(),
            latitude = result.latitude,
            longitude = result.longitude,
            timeZone = TimeZone.getTimeZone(result.timezone),
            country = if (!result.country.isNullOrEmpty()) result.country else result.countryCode,
            countryCode = result.countryCode,
            province = if (result.admin2.isNullOrEmpty()) {
                if (result.admin1.isNullOrEmpty()) {
                    if (result.admin3.isNullOrEmpty()) {
                        result.admin4
                    } else result.admin3
                } else result.admin1
            } else result.admin2,
            // Province code is mandatory for MF provider to have alerts/air quality, and MF provider uses Open-Meteo search
            provinceCode = if (result.countryCode == "FR") getFrenchDepartmentCode(result.admin2 ?: "") else null,
            city = result.name,
            weatherSource = weatherSource,
            isChina = result.countryCode.isNotEmpty()
                    && (result.countryCode.equals("cn", ignoreCase = true)
                    || result.countryCode.equals("hk", ignoreCase = true)
                    || result.countryCode.equals("tw", ignoreCase = true))
        )
    }
}

fun convert(
    context: Context,
    location: Location,
    weatherResult: OpenMeteoWeatherResult,
    airQualityResult: OpenMeteoAirQualityResult
): WeatherResultWrapper {
    // If the API doesn’t return hourly or daily, consider data as garbage and keep cached data
    if (weatherResult.hourly == null || weatherResult.daily == null) {
        return WeatherResultWrapper(null)
    }

    return try {
        val hourlyByHalfDay: MutableMap<String?, Map<String, MutableList<Hourly>>> = HashMap()
        val hourlyList: MutableList<Hourly> = ArrayList()
        var currentI: Int? = null

        for (i in weatherResult.hourly.time.indices) {
            val airQualityIndex = airQualityResult.hourly?.time?.indexOfFirst { it == weatherResult.hourly.time[i] }

            val hourly = Hourly(
                date = Date(weatherResult.hourly.time[i].times(1000)),
                isDaylight = if (weatherResult.hourly.isDay?.getOrNull(i) != null) weatherResult.hourly.isDay[i] > 0 else true,
                weatherText = getWeatherText(context, weatherResult.hourly.weatherCode?.getOrNull(i)),
                weatherCode = getWeatherCode(weatherResult.hourly.weatherCode?.getOrNull(i)),
                temperature = Temperature(
                    temperature = weatherResult.hourly.temperature?.getOrNull(i)?.roundToInt(),
                    apparentTemperature = weatherResult.hourly.apparentTemperature?.getOrNull(i)?.roundToInt()
                ),
                precipitation = Precipitation(
                    // TODO: It’s not clear why the sum of rain + showers + snowfall is sometimes < precipitation
                    total = weatherResult.hourly.precipitation?.getOrNull(i),
                    rain = weatherResult.hourly.rain?.getOrNull(i), // TODO: Add showers.getOrNull(i)
                    snow = weatherResult.hourly.snowfall?.getOrNull(i)
                ),
                precipitationProbability = PrecipitationProbability(
                    total = weatherResult.hourly.precipitationProbability?.getOrNull(i)?.toFloat()
                ),
                wind = Wind(
                    direction = getWindDirection(context, weatherResult.hourly.windDirection?.getOrNull(i)?.toFloat()),
                    degree = WindDegree(weatherResult.hourly.windDirection?.getOrNull(i)?.toFloat(), false),
                    speed = weatherResult.hourly.windSpeed?.getOrNull(i),
                    level = getWindLevel(context, weatherResult.hourly.windSpeed?.getOrNull(i))
                ),
                airQuality = if (airQualityIndex != null) AirQuality(
                    pM25 = airQualityResult.hourly.pm25?.getOrNull(airQualityIndex),
                    pM10 = airQualityResult.hourly.pm10?.getOrNull(airQualityIndex),
                    sO2 = airQualityResult.hourly.sulphurDioxide?.getOrNull(airQualityIndex),
                    nO2 = airQualityResult.hourly.nitrogenDioxide?.getOrNull(airQualityIndex),
                    o3 = airQualityResult.hourly.ozone?.getOrNull(airQualityIndex),
                    cO = airQualityResult.hourly.carbonMonoxide?.getOrNull(airQualityIndex)?.div(1000),
                ) else null,
                // pollen = TODO
                uV = UV(
                    index = weatherResult.hourly.uvIndex?.getOrNull(i)?.roundToInt(),
                    level = getUVLevel(context, weatherResult.hourly.uvIndex?.getOrNull(i)?.roundToInt())
                )
            )

            // We shift by 6 hours the hourly date, otherwise nighttime (00:00 to 05:59) would be on the wrong day
            val theDayAtMidnight = DisplayUtils.toTimezoneNoHour(
                Date((weatherResult.hourly.time[i] - 6 * 3600) * 1000),
                location.timeZone
            )
            val theDayFormatted = DisplayUtils.getFormattedDate(theDayAtMidnight, location.timeZone, "yyyyMMdd")
            if (!hourlyByHalfDay.containsKey(theDayFormatted)) {
                hourlyByHalfDay[theDayFormatted] = hashMapOf(
                    "day" to ArrayList(),
                    "night" to ArrayList()
                )
            }
            if (weatherResult.hourly.time[i] < theDayAtMidnight.time / 1000 + 18 * 3600) {
                // 06:00 to 17:59 is the day
                hourlyByHalfDay[theDayFormatted]!!["day"]!!.add(hourly)
            } else {
                // 18:00 to 05:59 is the night
                hourlyByHalfDay[theDayFormatted]!!["night"]!!.add(hourly)
            }

            // Add to the app only if starts in the current hour
            if (weatherResult.hourly.time[i] >= System.currentTimeMillis() / 1000 - 3600) {
                if (currentI == null) {
                    currentI = i + 1
                }
                hourlyList.add(hourly)
            }
        }

        val dailyList = getDailyList(context, location.timeZone, weatherResult.daily, hourlyList, hourlyByHalfDay)
        val weather = Weather(
            base = Base(cityId = location.cityId),
            current = Current(
                weatherText = getWeatherText(context, weatherResult.currentWeather?.weatherCode),
                weatherCode = getWeatherCode(weatherResult.currentWeather?.weatherCode),
                temperature = Temperature(temperature = weatherResult.currentWeather?.temperature?.roundToInt()),
                wind = Wind(
                    direction = if (weatherResult.currentWeather?.windDirection != null) getWindDirection(
                        context, weatherResult.currentWeather.windDirection.toFloat()
                    ) else null,
                    degree = if (weatherResult.currentWeather?.windDirection != null) WindDegree(
                        weatherResult.currentWeather.windDirection.toFloat(), false
                    ) else null,
                    speed = weatherResult.currentWeather?.windSpeed,
                    level = getWindLevel(context, weatherResult.currentWeather?.windSpeed)
                ),
                uV = getCurrentUV(
                    context,
                    dailyList.getOrNull(0)?.uV?.index,
                    Date(),
                    dailyList.getOrNull(0)?.sun?.riseDate,
                    dailyList.getOrNull(0)?.sun?.setDate,
                    location.timeZone
                ),
                airQuality = hourlyList.getOrNull(1)?.airQuality,
                relativeHumidity = if (currentI != null) weatherResult.hourly.relativeHumidity?.getOrNull(currentI)?.toFloat() else null,
                pressure = if (currentI != null) weatherResult.hourly.surfacePressure?.getOrNull(currentI) else null,
                visibility = if (currentI != null) weatherResult.hourly.visibility?.getOrNull(currentI)?.div(1000) else null,
                dewPoint = if (currentI != null) weatherResult.hourly.dewPoint?.getOrNull(currentI)?.roundToInt() else null,
                cloudCover = if (currentI != null) weatherResult.hourly.cloudCover?.getOrNull(currentI) else null
            ),
            yesterday = History(
                date = Date(weatherResult.daily.time[0].times(1000)),
                daytimeTemperature = weatherResult.daily.temperatureMax?.getOrNull(0)?.roundToInt(),
                nighttimeTemperature = weatherResult.daily.temperatureMin?.getOrNull(0)?.roundToInt(),
            ),
            dailyForecast = dailyList,
            hourlyForecast = hourlyList
        )
        WeatherResultWrapper(weather)
    } catch (e: Exception) {
        if (BreezyWeather.instance.debugMode) {
            e.printStackTrace()
        }
        WeatherResultWrapper(null)
    }
}

private fun getDailyList(
    context: Context,
    timeZone: TimeZone,
    dailyResult: OpenMeteoWeatherDaily,
    hourlyList: List<Hourly>,
    hourlyByDate: Map<String?, Map<String, List<Hourly>>>
): List<Daily> {
    val dailyList: MutableList<Daily> = ArrayList(dailyResult.time.size - 1)
    val hourlyListByDay = hourlyList.groupBy { DisplayUtils.getFormattedDate(it.date, timeZone, "yyyyMMdd") }
    for (i in 1 until dailyResult.time.size) {
        val theDay = Date(dailyResult.time[i].times(1000))
        val dailyDateFormatted = DisplayUtils.getFormattedDate(theDay, timeZone, "yyyyMMdd")
        val daily = Daily(
            date = theDay,
            day = completeHalfDayFromHourlyList(
                dailyDate = theDay,
                initialHalfDay = HalfDay(
                    temperature = Temperature(
                        temperature = dailyResult.temperatureMax?.getOrNull(i)?.roundToInt(),
                        apparentTemperature = dailyResult.apparentTemperatureMax?.getOrNull(i)?.roundToInt()
                    ),
                ),
                halfDayHourlyList = hourlyByDate.getOrDefault(dailyDateFormatted, null)?.get("day"),
                isDay = true
            ),
            night = completeHalfDayFromHourlyList(
                dailyDate = theDay,
                initialHalfDay = HalfDay(
                    temperature = Temperature(
                        // For night temperature, we take the minTemperature from the following day
                        temperature = dailyResult.temperatureMin?.getOrNull(i + 1)?.roundToInt(),
                        apparentTemperature = dailyResult.apparentTemperatureMin?.getOrNull(i + 1)?.roundToInt()
                    ),
                ),
                halfDayHourlyList = hourlyByDate.getOrDefault(dailyDateFormatted, null)?.get("night"),
                isDay = false
            ),
            sun = Astro(
                riseDate = if (dailyResult.sunrise?.getOrNull(i) != null) Date(dailyResult.sunrise[i]!!.times(1000)) else null,
                setDate = if (dailyResult.sunset?.getOrNull(i) != null) Date(dailyResult.sunset[i]!!.times(1000)) else null
            ),
            airQuality = getDailyAirQualityFromHourlyList(hourlyListByDay.getOrDefault(dailyDateFormatted, null)),
            // pollen = TODO
            uV = UV(
                index = dailyResult.uvIndexMax?.getOrNull(i)?.roundToInt(),
                level = getUVLevel(context, dailyResult.uvIndexMax?.getOrNull(i)?.roundToInt())
            ),
            hoursOfSun = if (dailyResult.sunrise?.getOrNull(i) != null && dailyResult.sunset?.getOrNull(i) != null) getHoursOfDay(
                Date(dailyResult.sunrise[i]!!.times(1000)), Date(dailyResult.sunset[i]!!.times(1000))
            ) else null
        )
        dailyList.add(daily)
    }
    return dailyList
}

private fun getWeatherText(context: Context, icon: Int?): String? {
    return if (icon == null) {
        null
    } else when (icon) {
        0 -> context.getString(R.string.openmeteo_weather_text_clear_sky)
        1 -> context.getString(R.string.openmeteo_weather_text_mainly_clear)
        2 -> context.getString(R.string.openmeteo_weather_text_partly_cloudy)
        3 -> context.getString(R.string.openmeteo_weather_text_overcast)
        45 -> context.getString(R.string.openmeteo_weather_text_fog)
        48 -> context.getString(R.string.openmeteo_weather_text_depositing_rime_fog)
        51 -> context.getString(R.string.openmeteo_weather_text_drizzle_light_intensity)
        53 -> context.getString(R.string.openmeteo_weather_text_drizzle_moderate_intensity)
        55 -> context.getString(R.string.openmeteo_weather_text_drizzle_dense_intensity)
        56 -> context.getString(R.string.openmeteo_weather_text_freezing_drizzle_light_intensity)
        57 -> context.getString(R.string.openmeteo_weather_text_freezing_drizzle_dense_intensity)
        61 -> context.getString(R.string.openmeteo_weather_text_rain_slight_intensity)
        63 -> context.getString(R.string.openmeteo_weather_text_rain_moderate_intensity)
        65 -> context.getString(R.string.openmeteo_weather_text_rain_heavy_intensity)
        66 -> context.getString(R.string.openmeteo_weather_text_freezing_rain_light_intensity)
        67 -> context.getString(R.string.openmeteo_weather_text_freezing_rain_heavy_intensity)
        71 -> context.getString(R.string.openmeteo_weather_text_snow_slight_intensity)
        73 -> context.getString(R.string.openmeteo_weather_text_snow_moderate_intensity)
        75 -> context.getString(R.string.openmeteo_weather_text_snow_heavy_intensity)
        77 -> context.getString(R.string.openmeteo_weather_text_snow_grains)
        80 -> context.getString(R.string.openmeteo_weather_text_rain_showers_slight)
        81 -> context.getString(R.string.openmeteo_weather_text_rain_showers_moderate)
        82 -> context.getString(R.string.openmeteo_weather_text_rain_showers_violent)
        85 -> context.getString(R.string.openmeteo_weather_text_snow_showers_slight)
        86 -> context.getString(R.string.openmeteo_weather_text_snow_showers_heavy)
        95 -> context.getString(R.string.openmeteo_weather_text_thunderstorm_slight_or_moderate)
        96 -> context.getString(R.string.openmeteo_weather_text_thunderstorm_with_slight_hail)
        99 -> context.getString(R.string.openmeteo_weather_text_thunderstorm_with_heavy_hail)
        else -> null
    }
}

private fun getWeatherCode(icon: Int?): WeatherCode? {
    return if (icon == null) {
         null
    } else when (icon) {
        0, 1 -> WeatherCode.CLEAR // Clear sky or Mainly clear
        2 -> WeatherCode.PARTLY_CLOUDY // Partly cloudy
        3 -> WeatherCode.CLOUDY // Overcast
        45, 48 -> WeatherCode.FOG // Fog and depositing rime fog
        51, 53, 55, // Drizzle: Light, moderate, and dense intensity
        56, 57, // Freezing Drizzle: Light and dense intensity
        61, 63, 65, // Rain: Slight, moderate and heavy intensity
        66, 67, // Freezing Rain: Light and heavy intensity
        80, 81, 82 -> WeatherCode.RAIN // Rain showers: Slight, moderate, and violent
        71, 73, 75, // Snow fall: Slight, moderate, and heavy intensity
        85, 86 -> WeatherCode.SNOW // Snow showers slight and heavy
        77 -> WeatherCode.SLEET // Snow grains
        95, 96, 99 -> WeatherCode.THUNDERSTORM // Thunderstorm with slight and heavy hail
        else -> null
    }
}

/**
 * Functions for debugging purposes (tracking NPE)
 */
fun debugConvert(
    location: Location?,
    result: OpenMeteoLocationResult,
    weatherSource: WeatherSource
): Location {
    return if (location != null && !location.province.isNullOrEmpty()
        && location.city.isNotEmpty()
        && !location.district.isNullOrEmpty()
    ) {
        Location(
            cityId = result.id.toString(),
            latitude = result.latitude,
            longitude = result.longitude,
            timeZone = TimeZone.getTimeZone(result.timezone),
            country = if (!result.country.isNullOrEmpty()) result.country else result.countryCode,
            city = location.city,
            weatherSource = weatherSource,
            isChina = result.countryCode.isNotEmpty()
                    && (result.countryCode.equals("cn", ignoreCase = true)
                    || result.countryCode.equals("hk", ignoreCase = true)
                    || result.countryCode.equals("tw", ignoreCase = true))
        )
    } else {
        Location(
            cityId = result.id.toString(),
            latitude = result.latitude,
            longitude = result.longitude,
            timeZone = TimeZone.getTimeZone(result.timezone),
            country = if (!result.country.isNullOrEmpty()) result.country else result.countryCode,
            city = result.name,
            weatherSource = weatherSource,
            isChina = result.countryCode.isNotEmpty()
                    && (result.countryCode.equals("cn", ignoreCase = true)
                    || result.countryCode.equals("hk", ignoreCase = true)
                    || result.countryCode.equals("tw", ignoreCase = true))
        )
    }
}

// Sometimes used in dev to make some null-safety checks
// TODO: Should be moved to its own DebugWeatherService
fun debugConvert(
    context: Context,
    location: Location,
    weatherResult: OpenMeteoWeatherResult,
    airQualityResult: OpenMeteoAirQualityResult
): WeatherResultWrapper {
    return try {
        val dailyList: MutableList<Daily> = ArrayList()
        if (weatherResult.daily != null) {
            for (i in 1 until weatherResult.daily.time.size) {
                val daily = Daily(date = Date(weatherResult.daily.time[i].times(1000)))
                dailyList.add(daily)
            }
        }

        val hourlyList: MutableList<Hourly> = ArrayList()
        if (weatherResult.hourly != null) {
            for (i in weatherResult.hourly.time.indices) {
                // Add to the app only if starts in the current hour
                if (weatherResult.hourly.time[i] >= System.currentTimeMillis() / 1000 - 3600) {
                    hourlyList.add(Hourly(date = Date(weatherResult.hourly.time[i].times(1000))))
                }
            }
        }

        val weather = Weather(
            base = Base(cityId = location.cityId),
            dailyForecast = dailyList,
            hourlyForecast = hourlyList
        )

        WeatherResultWrapper(weather)
    } catch (ignored: Exception) {
        WeatherResultWrapper(null)
    }
}