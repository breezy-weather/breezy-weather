package org.breezyweather.sources.pirateweather

import org.breezyweather.common.basic.models.weather.Alert
import org.breezyweather.common.basic.models.weather.Astro
import org.breezyweather.common.basic.models.weather.Base
import org.breezyweather.common.basic.models.weather.Current
import org.breezyweather.common.basic.models.weather.Daily
import org.breezyweather.common.basic.models.weather.HalfDay
import org.breezyweather.common.basic.models.weather.Minutely
import org.breezyweather.common.basic.models.weather.MoonPhase
import org.breezyweather.common.basic.models.weather.Precipitation
import org.breezyweather.common.basic.models.weather.PrecipitationProbability
import org.breezyweather.common.basic.models.weather.Temperature
import org.breezyweather.common.basic.models.weather.UV
import org.breezyweather.common.basic.models.weather.WeatherCode
import org.breezyweather.common.basic.models.weather.Wind
import org.breezyweather.common.basic.wrappers.HourlyWrapper
import org.breezyweather.common.basic.wrappers.WeatherResultWrapper
import org.breezyweather.common.exceptions.WeatherException
import org.breezyweather.common.extensions.toDate
import org.breezyweather.sources.pirateweather.json.PirateWeatherAlert
import org.breezyweather.sources.pirateweather.json.PirateWeatherCurrently
import org.breezyweather.sources.pirateweather.json.PirateWeatherDaily
import org.breezyweather.sources.pirateweather.json.PirateWeatherForecastResult
import org.breezyweather.sources.pirateweather.json.PirateWeatherHourly
import org.breezyweather.sources.pirateweather.json.PirateWeatherMinutely
import java.util.Date
import kotlin.math.roundToInt


/**
 * Converts PirateWeather result into a forecast
 */
fun convert(
    forecastResult: PirateWeatherForecastResult
): WeatherResultWrapper {
    // If the API doesn’t return hourly or daily, consider data as garbage and keep cached data
    if (forecastResult.daily?.data.isNullOrEmpty() || forecastResult.hourly?.data.isNullOrEmpty()) {
        throw WeatherException()
    }

    return WeatherResultWrapper(
        base = Base(
            publishDate = forecastResult.currently?.time?.times(1000)?.toDate() ?: Date()
        ),
        current = getCurrentForecast(forecastResult.currently),
        dailyForecast = getDailyForecast(forecastResult.daily!!.data!!),
        hourlyForecast = getHourlyForecast(forecastResult.hourly!!.data!!),
        minutelyForecast = getMinutelyForecast(forecastResult.minutely?.data),
        alertList = getAlertList(forecastResult.alerts)
    )
}

/**
 * Returns current weather forecast
 */
private fun getCurrentForecast(result: PirateWeatherCurrently?): Current? {
    if (result == null) return null
    return Current(
        weatherText = result.summary,
        weatherCode = getWeatherCode(result.icon),
        temperature = Temperature(
            temperature = result.temperature,
            apparentTemperature = result.apparentTemperature,
        ),
        wind = Wind(
            degree = result.windBearing,
            speed = result.windSpeed?.times(3.6f),
        ),
        uV = UV(index = result.uvIndex),
        relativeHumidity = result.humidity?.times(100),
        dewPoint = result.dewPoint,
        pressure = result.pressure,
        cloudCover = result.cloudCover?.times(100)?.roundToInt(),
        visibility = result.visibility,
    )
}

private fun getDailyForecast(
    dailyResult: List<PirateWeatherDaily>
): List<Daily> {
    return dailyResult.map { result ->
        Daily(
            date = Date(result.time.times(1000)),
            day = HalfDay(
                weatherText = result.summary,
                weatherCode = getWeatherCode(result.icon),
                temperature = Temperature(
                    temperature = result.temperatureHigh,
                    apparentTemperature = result.apparentTemperatureHigh,
                )
            ),
            night = HalfDay(
                weatherText = result.summary,
                weatherCode = getWeatherCode(result.icon),
                // temperatureLow/High are always forward-looking
                // See https://docs.pirateweather.net/en/latest/API/#temperaturelow
                temperature = Temperature(
                    temperature = result.temperatureLow,
                    apparentTemperature = result.apparentTemperatureLow,
                ),
            ),
            sun = Astro(
                riseDate = result.sunrise?.times(1000)?.toDate(),
                setDate = result.sunset?.times(1000)?.toDate(),
            ),
            moon = Astro(),
            moonPhase = MoonPhase(
                angle = result.moonPhase?.times(360)?.roundToInt(), // Seems correct
            ),
            uV = UV(index = result.uvIndex),
        )
    }
}

/**
 * Returns hourly forecast
 */
private fun getHourlyForecast(
    hourlyResult: List<PirateWeatherHourly>
): List<HourlyWrapper> {
    return hourlyResult.map { result ->
        HourlyWrapper(
            date = Date(result.time.times(1000)),
            weatherText = result.summary,
            weatherCode = getWeatherCode(result.icon),
            temperature = Temperature(
                temperature = result.temperature,
                apparentTemperature = result.apparentTemperature,
            ),
            // see https://docs.pirateweather.net/en/latest/API/#preciptype
            precipitation = Precipitation(
                total = result.precipAccumulation,
                rain = if (result.precipType.equals("rain")) result.precipIntensity else null,
                snow = if (result.precipType.equals("snow")) result.precipIntensity else null,
            ),
            precipitationProbability = PrecipitationProbability(
                total = result.precipProbability,
            ),
            wind = Wind(
                degree = result.windBearing,
                speed = result.windSpeed?.times(3.6f),
            ),
            uV = UV(
                index = result.uvIndex,
            ),
            relativeHumidity = result.humidity?.times(100),
            dewPoint = result.dewPoint,
            pressure = result.pressure,
            cloudCover = result.cloudCover?.times(100)?.roundToInt(),
            visibility = result.visibility,
        )
    }
}


/**
 * Returns minutely forecast
 * Copied from openweather implementation
 */
private fun getMinutelyForecast(minutelyResult: List<PirateWeatherMinutely>?): List<Minutely>? {
    if (minutelyResult.isNullOrEmpty()) return null
    val minutelyList: MutableList<Minutely> = arrayListOf()
    minutelyResult.forEachIndexed { i, minutelyForecast ->
        minutelyList.add(
            Minutely(
                date = Date(minutelyForecast.time * 1000),
                minuteInterval = if (i < minutelyResult.size - 1) {
                    ((minutelyResult[i + 1].time - minutelyForecast.time) / 60).toDouble()
                        .roundToInt()
                } else ((minutelyForecast.time - minutelyResult[i - 1].time) / 60).toDouble()
                    .roundToInt(),
                precipitationIntensity = minutelyForecast.precipIntensity?.toDouble()
            )
        )
    }
    return minutelyList
}

/**
 * Returns alerts
 */
private fun getAlertList(alertList: List<PirateWeatherAlert>?): List<Alert>? {
    if (alertList.isNullOrEmpty()) return null
    return alertList.map { alert ->
        Alert(
            // TODO: Avoid having the same ID for two different alerts starting at the same time
            alertId = alert.start + alert.end,
            startDate = Date(alert.start.times(1000)),
            endDate = Date(alert.end.times(1000)),
            description = alert.title ?: "",
            content = alert.description,
            // TODO: Add more refined priority states (not evident from PirateWeather docs)
            priority = when (alert.severity) {
                "Moderate" -> 1
                else -> 1
            }
        )
    }
}

/**
 * Gets weather code
 *
 * Unfortunately, PirateWeather does not report weather codes
 * See https://docs.pirateweather.net/en/latest/API/#icon
 */
private fun getWeatherCode(icon: String?): WeatherCode? {
    return when (icon) {
        "rain" -> WeatherCode.RAIN
        "sleet" -> WeatherCode.SLEET
        "snow" -> WeatherCode.SNOW
        "fog" -> WeatherCode.FOG
        "wind" -> WeatherCode.WIND
        "clear-day", "clear-night" -> WeatherCode.CLEAR
        "partly-cloudy-day", "partly-cloudy-night" -> WeatherCode.PARTLY_CLOUDY
        "cloudy" -> WeatherCode.CLOUDY
        else -> null
    }
}