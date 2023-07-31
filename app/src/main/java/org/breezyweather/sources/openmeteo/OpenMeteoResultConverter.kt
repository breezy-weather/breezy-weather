package org.breezyweather.sources.openmeteo

import android.content.Context
import org.breezyweather.R
import org.breezyweather.common.basic.models.Location
import org.breezyweather.common.basic.models.weather.AirQuality
import org.breezyweather.common.basic.models.weather.Allergen
import org.breezyweather.common.basic.models.weather.Astro
import org.breezyweather.common.basic.models.weather.Current
import org.breezyweather.common.basic.models.weather.Daily
import org.breezyweather.common.basic.models.weather.HalfDay
import org.breezyweather.common.basic.models.weather.History
import org.breezyweather.common.basic.models.weather.Precipitation
import org.breezyweather.common.basic.models.weather.PrecipitationProbability
import org.breezyweather.common.basic.models.weather.Temperature
import org.breezyweather.common.basic.models.weather.UV
import org.breezyweather.common.basic.models.weather.WeatherCode
import org.breezyweather.common.basic.models.weather.Wind
import org.breezyweather.common.basic.wrappers.AirQualityWrapper
import org.breezyweather.common.basic.wrappers.AllergenWrapper
import org.breezyweather.common.basic.wrappers.HourlyWrapper
import org.breezyweather.common.basic.wrappers.SecondaryWeatherWrapper
import org.breezyweather.common.basic.wrappers.WeatherWrapper
import org.breezyweather.common.exceptions.SecondaryWeatherException
import org.breezyweather.common.exceptions.WeatherException
import org.breezyweather.common.extensions.plus
import org.breezyweather.common.extensions.toDate
import org.breezyweather.common.source.SecondaryWeatherSourceFeature
import org.breezyweather.sources.mf.getFrenchDepartmentCode
import org.breezyweather.sources.openmeteo.json.OpenMeteoAirQualityResult
import org.breezyweather.sources.openmeteo.json.OpenMeteoLocationResult
import org.breezyweather.sources.openmeteo.json.OpenMeteoWeatherDaily
import org.breezyweather.sources.openmeteo.json.OpenMeteoWeatherHourly
import org.breezyweather.sources.openmeteo.json.OpenMeteoWeatherResult
import java.util.Date
import java.util.TimeZone
import kotlin.math.roundToInt

fun convert(
    result: OpenMeteoLocationResult
): Location {
    return Location(
        cityId = result.id.toString(),
        latitude = result.latitude,
        longitude = result.longitude,
        timeZone = TimeZone.getTimeZone(result.timezone),
        country = if (!result.country.isNullOrEmpty()) result.country else result.countryCode ?: "",
        countryCode = result.countryCode,
        province = if (result.admin2.isNullOrEmpty()) {
            if (result.admin1.isNullOrEmpty()) {
                if (result.admin3.isNullOrEmpty()) {
                    result.admin4
                } else result.admin3
            } else result.admin1
        } else result.admin2,
        // Province code is mandatory for MF source to have alerts/air quality, and MF source uses Open-Meteo search
        provinceCode = if (result.countryCode == "FR") getFrenchDepartmentCode(result.admin2 ?: "") else null,
        city = result.name,
        weatherSource = "openmeteo"
    )
}

fun convert(
    context: Context,
    weatherResult: OpenMeteoWeatherResult,
    airQualityResult: OpenMeteoAirQualityResult
): WeatherWrapper {
    // If the API doesn’t return hourly or daily, consider data as garbage and keep cached data
    if (weatherResult.hourly == null || weatherResult.daily == null) {
        throw WeatherException()
    }

    return WeatherWrapper(
        current = Current(
            weatherText = getWeatherText(context, weatherResult.currentWeather?.weatherCode),
            weatherCode = getWeatherCode(weatherResult.currentWeather?.weatherCode),
            temperature = Temperature(temperature = weatherResult.currentWeather?.temperature),
            wind = Wind(
                degree = weatherResult.currentWeather?.windDirection,
                speed = weatherResult.currentWeather?.windSpeed
            )
        ),
        yesterday = History(
            date = Date(weatherResult.daily.time[0].times(1000)),
            daytimeTemperature = weatherResult.daily.temperatureMax?.getOrNull(0),
            nighttimeTemperature = weatherResult.daily.temperatureMin?.getOrNull(0),
        ),
        dailyForecast = getDailyList(weatherResult.daily),
        hourlyForecast = getHourlyList(context, weatherResult.hourly, airQualityResult)
    )
}

private fun getDailyList(
    dailyResult: OpenMeteoWeatherDaily
): List<Daily> {
    val dailyList: MutableList<Daily> = ArrayList(dailyResult.time.size - 2)
    for (i in 1 until dailyResult.time.size - 1) {
        val theDay = dailyResult.time[i].times(1000).toDate()
        val daily = Daily(
            date = theDay,
            day = HalfDay(
                temperature = Temperature(
                    temperature = dailyResult.temperatureMax?.getOrNull(i),
                    apparentTemperature = dailyResult.apparentTemperatureMax?.getOrNull(i)
                ),
            ),
            night = HalfDay(
                temperature = Temperature(
                    // For night temperature, we take the minTemperature from the following day
                    temperature = dailyResult.temperatureMin?.getOrNull(i + 1),
                    apparentTemperature = dailyResult.apparentTemperatureMin?.getOrNull(i + 1)
                )
            ),
            sun = Astro(
                riseDate = dailyResult.sunrise?.getOrNull(i)?.times(1000)?.toDate(),
                setDate = dailyResult.sunset?.getOrNull(i)?.times(1000)?.toDate()
            ),
            uV = UV(index = dailyResult.uvIndexMax?.getOrNull(i))
        )
        dailyList.add(daily)
    }
    return dailyList
}

private fun getHourlyList(
    context: Context,
    hourlyResult: OpenMeteoWeatherHourly,
    airQualityResult: OpenMeteoAirQualityResult
): List<HourlyWrapper> {
    val hourlyList = mutableListOf<HourlyWrapper>()
    for (i in hourlyResult.time.indices) {
        val airQualityIndex = airQualityResult.hourly?.time?.indexOfFirst { it == hourlyResult.time[i] }

        hourlyList.add(
            HourlyWrapper(
                date = hourlyResult.time[i].times(1000).toDate(),
                isDaylight = if (hourlyResult.isDay?.getOrNull(i) != null) hourlyResult.isDay[i] > 0 else null,
                weatherText = getWeatherText(context, hourlyResult.weatherCode?.getOrNull(i)),
                weatherCode = getWeatherCode(hourlyResult.weatherCode?.getOrNull(i)),
                temperature = Temperature(
                    temperature = hourlyResult.temperature?.getOrNull(i),
                    apparentTemperature = hourlyResult.apparentTemperature?.getOrNull(i)
                ),
                precipitation = Precipitation(
                    total = hourlyResult.precipitation?.getOrNull(i),
                    rain = hourlyResult.rain?.getOrNull(i) + hourlyResult.showers?.getOrNull(i),
                    snow = hourlyResult.snowfall?.getOrNull(i)
                ),
                precipitationProbability = PrecipitationProbability(
                    total = hourlyResult.precipitationProbability?.getOrNull(i)?.toFloat()
                ),
                wind = Wind(
                    degree = hourlyResult.windDirection?.getOrNull(i)?.toFloat(),
                    speed = hourlyResult.windSpeed?.getOrNull(i),
                    gusts = hourlyResult.windGusts?.getOrNull(i)
                ),
                airQuality = if (airQualityIndex != null && airQualityIndex != -1) AirQuality(
                    pM25 = airQualityResult.hourly.pm25?.getOrNull(airQualityIndex),
                    pM10 = airQualityResult.hourly.pm10?.getOrNull(airQualityIndex),
                    sO2 = airQualityResult.hourly.sulphurDioxide?.getOrNull(airQualityIndex),
                    nO2 = airQualityResult.hourly.nitrogenDioxide?.getOrNull(airQualityIndex),
                    o3 = airQualityResult.hourly.ozone?.getOrNull(airQualityIndex),
                    cO = airQualityResult.hourly.carbonMonoxide?.getOrNull(airQualityIndex)?.div(1000.0)?.toFloat(),
                ) else null,
                allergen = if (airQualityIndex != null && airQualityIndex != -1) Allergen(
                    alder = airQualityResult.hourly.alderPollen?.getOrNull(airQualityIndex)?.roundToInt(),
                    birch = airQualityResult.hourly.birchPollen?.getOrNull(airQualityIndex)?.roundToInt(),
                    grass = airQualityResult.hourly.grassPollen?.getOrNull(airQualityIndex)?.roundToInt(),
                    mugwort = airQualityResult.hourly.mugwortPollen?.getOrNull(airQualityIndex)?.roundToInt(),
                    olive = airQualityResult.hourly.olivePollen?.getOrNull(airQualityIndex)?.roundToInt(),
                    ragweed = airQualityResult.hourly.ragweedPollen?.getOrNull(airQualityIndex)?.roundToInt(),
                ) else null,
                uV = UV(index = hourlyResult.uvIndex?.getOrNull(i)),
                relativeHumidity = hourlyResult.relativeHumidity?.getOrNull(i)?.toFloat(),
                dewPoint = hourlyResult.dewPoint?.getOrNull(i),
                pressure = hourlyResult.surfacePressure?.getOrNull(i),
                cloudCover = hourlyResult.cloudCover?.getOrNull(i),
                visibility = hourlyResult.visibility?.getOrNull(i)?.toFloat()
            )
        )
    }
    return hourlyList
}

private fun getWeatherText(context: Context, icon: Int?): String? {
    return when (icon) {
        null -> null
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
    return when (icon) {
        null -> null
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
 * Secondary convert
 */

fun convertSecondary(
    airQualityResult: OpenMeteoAirQualityResult,
    requestedFeatures: List<SecondaryWeatherSourceFeature>
): SecondaryWeatherWrapper {
    // If the API doesn’t return hourly or daily, consider data as garbage and keep cached data
    if (airQualityResult.hourly == null) {
        throw SecondaryWeatherException()
    }

    val airQualityHourly: MutableMap<Date, AirQuality> = mutableMapOf()
    val allergenHourly: MutableMap<Date, Allergen> = mutableMapOf()

    for (i in airQualityResult.hourly.time.indices) {
        if (requestedFeatures.contains(SecondaryWeatherSourceFeature.FEATURE_AIR_QUALITY)) {
            airQualityHourly[airQualityResult.hourly.time[i].times(1000).toDate()] = AirQuality(
                pM25 = airQualityResult.hourly.pm25?.getOrNull(i),
                pM10 = airQualityResult.hourly.pm10?.getOrNull(i),
                sO2 = airQualityResult.hourly.sulphurDioxide?.getOrNull(i),
                nO2 = airQualityResult.hourly.nitrogenDioxide?.getOrNull(i),
                o3 = airQualityResult.hourly.ozone?.getOrNull(i),
                cO = airQualityResult.hourly.carbonMonoxide?.getOrNull(i)?.div(1000.0)?.toFloat(),
            )
        }
        if (requestedFeatures.contains(SecondaryWeatherSourceFeature.FEATURE_ALLERGEN)) {
            allergenHourly[airQualityResult.hourly.time[i].times(1000).toDate()] = Allergen(
                alder = airQualityResult.hourly.alderPollen?.getOrNull(i)?.roundToInt(),
                birch = airQualityResult.hourly.birchPollen?.getOrNull(i)?.roundToInt(),
                grass = airQualityResult.hourly.grassPollen?.getOrNull(i)?.roundToInt(),
                mugwort = airQualityResult.hourly.mugwortPollen?.getOrNull(i)?.roundToInt(),
                olive = airQualityResult.hourly.olivePollen?.getOrNull(i)?.roundToInt(),
                ragweed = airQualityResult.hourly.ragweedPollen?.getOrNull(i)?.roundToInt(),
            )
        }
    }


    return SecondaryWeatherWrapper(
        airQuality = if (requestedFeatures.contains(SecondaryWeatherSourceFeature.FEATURE_AIR_QUALITY)) {
            AirQualityWrapper(hourlyForecast = airQualityHourly)
        } else null,
        allergen = if (requestedFeatures.contains(SecondaryWeatherSourceFeature.FEATURE_ALLERGEN)) {
            AllergenWrapper(hourlyForecast = allergenHourly)
        } else null
    )
}

/**
 * Functions for debugging purposes (tracking NPE)
 */
fun debugConvert(
    location: Location?,
    result: OpenMeteoLocationResult
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
            country = if (!result.country.isNullOrEmpty()) result.country else result.countryCode ?: "",
            city = location.city,
            weatherSource = "openmeteo",
            airQualitySource = location.airQualitySource,
            allergenSource = location.allergenSource,
            minutelySource = location.minutelySource,
            alertSource = location.alertSource
        )
    } else {
        Location(
            cityId = result.id.toString(),
            latitude = result.latitude,
            longitude = result.longitude,
            timeZone = TimeZone.getTimeZone(result.timezone),
            country = if (!result.country.isNullOrEmpty()) result.country else result.countryCode ?: "",
            city = result.name,
            weatherSource = "openmeteo",
            airQualitySource = location?.airQualitySource,
            allergenSource = location?.allergenSource,
            minutelySource = location?.minutelySource,
            alertSource = location?.alertSource
        )
    }
}

// Sometimes used in dev to make some null-safety checks
// TODO: Should be moved to its own DebugWeatherService
fun debugConvert(
    context: Context,
    weatherResult: OpenMeteoWeatherResult,
    airQualityResult: OpenMeteoAirQualityResult
): WeatherWrapper {
    val dailyList: MutableList<Daily> = ArrayList()
    if (weatherResult.daily != null) {
        for (i in 1 until weatherResult.daily.time.size) {
            val daily = Daily(date = Date(weatherResult.daily.time[i].times(1000)))
            dailyList.add(daily)
        }
    }

    val hourlyList: MutableList<HourlyWrapper> = ArrayList()
    if (weatherResult.hourly != null) {
        for (i in weatherResult.hourly.time.indices) {
            // Add to the app only if starts in the current hour
            if (weatherResult.hourly.time[i] >= System.currentTimeMillis() / 1000 - 3600) {
                hourlyList.add(HourlyWrapper(date = Date(weatherResult.hourly.time[i].times(1000))))
            }
        }
    }

    return WeatherWrapper(
        dailyForecast = dailyList,
        hourlyForecast = hourlyList
    )
}