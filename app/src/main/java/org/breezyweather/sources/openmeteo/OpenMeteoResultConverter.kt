/**
 * This file is part of Breezy Weather.
 *
 * Breezy Weather is free software: you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published by the
 * Free Software Foundation, version 3 of the License.
 *
 * Breezy Weather is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public
 * License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Breezy Weather. If not, see <https://www.gnu.org/licenses/>.
 */

package org.breezyweather.sources.openmeteo

import android.content.Context
import breezyweather.domain.location.model.Location
import breezyweather.domain.location.model.LocationAddressInfo
import breezyweather.domain.weather.model.AirQuality
import breezyweather.domain.weather.model.DailyCloudCover
import breezyweather.domain.weather.model.DailyDewPoint
import breezyweather.domain.weather.model.DailyPressure
import breezyweather.domain.weather.model.DailyRelativeHumidity
import breezyweather.domain.weather.model.DailyVisibility
import breezyweather.domain.weather.model.Minutely
import breezyweather.domain.weather.model.Pollen
import breezyweather.domain.weather.model.Precipitation
import breezyweather.domain.weather.model.PrecipitationProbability
import breezyweather.domain.weather.model.UV
import breezyweather.domain.weather.model.Wind
import breezyweather.domain.weather.reference.WeatherCode
import breezyweather.domain.weather.wrappers.AirQualityWrapper
import breezyweather.domain.weather.wrappers.CurrentWrapper
import breezyweather.domain.weather.wrappers.DailyWrapper
import breezyweather.domain.weather.wrappers.HalfDayWrapper
import breezyweather.domain.weather.wrappers.HourlyWrapper
import breezyweather.domain.weather.wrappers.PollenWrapper
import breezyweather.domain.weather.wrappers.TemperatureWrapper
import breezyweather.domain.weather.wrappers.WeatherWrapper
import org.breezyweather.R
import org.breezyweather.common.extensions.plus
import org.breezyweather.common.extensions.toCalendarWithTimeZone
import org.breezyweather.common.extensions.toDate
import org.breezyweather.sources.openmeteo.json.OpenMeteoAirQualityHourly
import org.breezyweather.sources.openmeteo.json.OpenMeteoAirQualityResult
import org.breezyweather.sources.openmeteo.json.OpenMeteoLocationResult
import org.breezyweather.sources.openmeteo.json.OpenMeteoWeatherCurrent
import org.breezyweather.sources.openmeteo.json.OpenMeteoWeatherDaily
import org.breezyweather.sources.openmeteo.json.OpenMeteoWeatherHourly
import org.breezyweather.sources.openmeteo.json.OpenMeteoWeatherMinutely
import org.breezyweather.sources.openmeteo.json.OpenMeteoWeatherResult
import java.util.Calendar
import java.util.Date
import java.util.TimeZone
import kotlin.math.roundToInt
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds

internal fun getCurrent(
    current: OpenMeteoWeatherCurrent?,
    context: Context,
): CurrentWrapper? {
    if (current == null) return null

    return CurrentWrapper(
        weatherText = getWeatherText(context, current.weatherCode),
        weatherCode = getWeatherCode(current.weatherCode),
        temperature = TemperatureWrapper(
            temperature = current.temperature,
            feelsLike = current.apparentTemperature
        ),
        wind = Wind(
            degree = current.windDirection,
            speed = current.windSpeed,
            gusts = current.windGusts
        ),
        uV = UV(index = current.uvIndex),
        relativeHumidity = current.relativeHumidity?.toDouble(),
        dewPoint = current.dewPoint,
        pressure = current.pressureMsl,
        cloudCover = current.cloudCover,
        visibility = current.visibility
    )
}

internal fun getDailyList(
    dailyResult: OpenMeteoWeatherDaily?,
    location: Location,
): List<DailyWrapper>? {
    if (dailyResult == null) return null

    val dailyList: MutableList<DailyWrapper> = ArrayList(dailyResult.time.size - 1)
    for (i in 0 until dailyResult.time.size - 1) {
        val theDayWithDstFixed = dailyResult.time[i].seconds.inWholeMilliseconds.toDate()
            .toCalendarWithTimeZone(location.timeZone)
            .apply {
                add(Calendar.HOUR_OF_DAY, 1)
                set(Calendar.HOUR_OF_DAY, 0)
            }.time
        val daily = DailyWrapper(
            date = theDayWithDstFixed,
            day = HalfDayWrapper(
                temperature = TemperatureWrapper(
                    temperature = dailyResult.temperatureMax?.getOrNull(i),
                    feelsLike = dailyResult.apparentTemperatureMax?.getOrNull(i)
                )
            ),
            night = HalfDayWrapper(
                temperature = TemperatureWrapper(
                    // For night temperature, we take the minTemperature from the following day
                    temperature = dailyResult.temperatureMin?.getOrNull(i + 1),
                    feelsLike = dailyResult.apparentTemperatureMin?.getOrNull(i + 1)
                )
            ),
            uV = UV(index = dailyResult.uvIndexMax?.getOrNull(i)),
            sunshineDuration = dailyResult.sunshineDuration?.getOrNull(i)?.div(3600),
            relativeHumidity = DailyRelativeHumidity(
                average = dailyResult.relativeHumidityMean?.getOrNull(i)?.toDouble(),
                max = dailyResult.relativeHumidityMax?.getOrNull(i)?.toDouble(),
                min = dailyResult.relativeHumidityMin?.getOrNull(i)?.toDouble()
            ),
            dewPoint = DailyDewPoint(
                average = dailyResult.dewPointMean?.getOrNull(i),
                max = dailyResult.dewPointMax?.getOrNull(i),
                min = dailyResult.dewPointMin?.getOrNull(i)
            ),
            pressure = DailyPressure(
                average = dailyResult.pressureMslMean?.getOrNull(i),
                max = dailyResult.pressureMslMax?.getOrNull(i),
                min = dailyResult.pressureMslMin?.getOrNull(i)
            ),
            cloudCover = DailyCloudCover(
                average = dailyResult.cloudCoverMean?.getOrNull(i),
                max = dailyResult.cloudCoverMax?.getOrNull(i),
                min = dailyResult.cloudCoverMin?.getOrNull(i)
            ),
            visibility = DailyVisibility(
                average = dailyResult.visibilityMean?.getOrNull(i),
                max = dailyResult.visibilityMax?.getOrNull(i),
                min = dailyResult.visibilityMin?.getOrNull(i)
            )
        )
        dailyList.add(daily)
    }
    return dailyList
}

internal fun getHourlyList(
    context: Context,
    hourlyResult: OpenMeteoWeatherHourly?,
): List<HourlyWrapper>? {
    if (hourlyResult == null) return null

    val hourlyList = mutableListOf<HourlyWrapper>()
    for (i in hourlyResult.time.indices) {
        hourlyList.add(
            HourlyWrapper(
                date = hourlyResult.time[i].seconds.inWholeMilliseconds.toDate(),
                isDaylight = if (hourlyResult.isDay?.getOrNull(i) != null) hourlyResult.isDay[i] > 0 else null,
                weatherText = getWeatherText(context, hourlyResult.weatherCode?.getOrNull(i)),
                weatherCode = getWeatherCode(hourlyResult.weatherCode?.getOrNull(i)),
                temperature = TemperatureWrapper(
                    temperature = hourlyResult.temperature?.getOrNull(i),
                    feelsLike = hourlyResult.apparentTemperature?.getOrNull(i)
                ),
                precipitation = Precipitation(
                    total = hourlyResult.precipitation?.getOrNull(i),
                    rain = hourlyResult.rain?.getOrNull(i) + hourlyResult.showers?.getOrNull(i),
                    snow = hourlyResult.snowfall?.getOrNull(i)?.times(10) // convert cm -> mm
                ),
                precipitationProbability = PrecipitationProbability(
                    total = hourlyResult.precipitationProbability?.getOrNull(i)?.toDouble()
                ),
                wind = Wind(
                    degree = hourlyResult.windDirection?.getOrNull(i)?.toDouble(),
                    speed = hourlyResult.windSpeed?.getOrNull(i),
                    gusts = hourlyResult.windGusts?.getOrNull(i)
                ),
                uV = UV(index = hourlyResult.uvIndex?.getOrNull(i)),
                relativeHumidity = hourlyResult.relativeHumidity?.getOrNull(i)?.toDouble(),
                dewPoint = hourlyResult.dewPoint?.getOrNull(i),
                pressure = hourlyResult.pressureMsl?.getOrNull(i),
                cloudCover = hourlyResult.cloudCover?.getOrNull(i),
                visibility = hourlyResult.visibility?.getOrNull(i)?.toDouble()
            )
        )
    }
    return hourlyList
}

internal fun getAirQuality(
    hourlyAirQualityResult: OpenMeteoAirQualityHourly?,
): AirQualityWrapper? {
    if (hourlyAirQualityResult == null) return null

    val airQualityHourly = mutableMapOf<Date, AirQuality>()
    for (i in hourlyAirQualityResult.time.indices) {
        airQualityHourly[hourlyAirQualityResult.time[i].seconds.inWholeMilliseconds.toDate()] = AirQuality(
            pM25 = hourlyAirQualityResult.pm25?.getOrNull(i),
            pM10 = hourlyAirQualityResult.pm10?.getOrNull(i),
            sO2 = hourlyAirQualityResult.sulphurDioxide?.getOrNull(i),
            nO2 = hourlyAirQualityResult.nitrogenDioxide?.getOrNull(i),
            o3 = hourlyAirQualityResult.ozone?.getOrNull(i),
            cO = hourlyAirQualityResult.carbonMonoxide?.getOrNull(i)?.div(1000.0)
        )
    }
    return AirQualityWrapper(
        hourlyForecast = airQualityHourly
    )
}

internal fun getPollen(
    hourlyAirQualityResult: OpenMeteoAirQualityHourly?,
): PollenWrapper? {
    if (hourlyAirQualityResult == null) return null

    val pollenHourly = mutableMapOf<Date, Pollen>()
    for (i in hourlyAirQualityResult.time.indices) {
        pollenHourly[hourlyAirQualityResult.time[i].seconds.inWholeMilliseconds.toDate()] = Pollen(
            alder = hourlyAirQualityResult.alderPollen?.getOrNull(i)?.roundToInt(),
            birch = hourlyAirQualityResult.birchPollen?.getOrNull(i)?.roundToInt(),
            grass = hourlyAirQualityResult.grassPollen?.getOrNull(i)?.roundToInt(),
            mugwort = hourlyAirQualityResult.mugwortPollen?.getOrNull(i)?.roundToInt(),
            olive = hourlyAirQualityResult.olivePollen?.getOrNull(i)?.roundToInt(),
            ragweed = hourlyAirQualityResult.ragweedPollen?.getOrNull(i)?.roundToInt()
        )
    }
    return PollenWrapper(
        hourlyForecast = pollenHourly
    )
}

internal fun getMinutelyList(
    minutelyFifteen: OpenMeteoWeatherMinutely?,
): List<Minutely>? {
    if (minutelyFifteen?.time == null || minutelyFifteen.time.isEmpty()) return null

    val currentMinutelyIndex = minutelyFifteen.time.indexOfFirst {
        it.seconds.inWholeMilliseconds >= (Date().time - 15.minutes.inWholeMilliseconds)
    }
    val maxMinutelyIndex = minOf(currentMinutelyIndex + 8, minutelyFifteen.time.size - 1)
    val precipitationMinutely = minutelyFifteen.precipitation?.slice(currentMinutelyIndex until maxMinutelyIndex)
    // val precipitationProbabilityMinutely =
    //     minutelyFifteen.precipitationProbability?.slice(currentMinutelyIndex until maxMinutelyIndex)
    return minutelyFifteen.time.slice(currentMinutelyIndex until maxMinutelyIndex)
        // 2 hours
        .mapIndexed { i, time ->
            Minutely(
                date = time.seconds.inWholeMilliseconds.toDate(),
                minuteInterval = 15,
                // mm/15 min -> mm/h
                precipitationIntensity = precipitationMinutely?.getOrNull(i)?.times(4)
                /*if (precipitationProbabilityMinutely?.getOrNull(i) != null &&
                    precipitationProbabilityMinutely[i]!! > 30
                ) {
                    precipitationMinutely?.getOrNull(i)?.times(4) // mm/15 min -> mm/h
                } else {
                    null
                }*/
            )
        }
}

private fun getWeatherText(
    context: Context,
    icon: Int?,
): String? {
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

private fun getWeatherCode(
    icon: Int?,
): WeatherCode? {
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
        80, 81, 82, // Rain showers: Slight, moderate, and violent
        -> WeatherCode.RAIN
        71, 73, 75, // Snow fall: Slight, moderate, and heavy intensity
        85, 86, // Snow showers slight and heavy
        -> WeatherCode.SNOW
        77 -> WeatherCode.SLEET // Snow grains
        95, 96, 99 -> WeatherCode.THUNDERSTORM // Thunderstorm with slight and heavy hail
        else -> null
    }
}

/**
 * Functions for debugging purposes (tracking NPE)
 */
// Sometimes used in dev to make some null-safety checks
// TODO: Should be moved to its own DebugWeatherService
internal fun debugConvert(
    context: Context,
    weatherResult: OpenMeteoWeatherResult,
    airQualityResult: OpenMeteoAirQualityResult,
): WeatherWrapper {
    val dailyList = mutableListOf<DailyWrapper>()
    if (weatherResult.daily != null) {
        for (i in 1 until weatherResult.daily.time.size) {
            val daily = DailyWrapper(date = weatherResult.daily.time[i].seconds.inWholeMilliseconds.toDate())
            dailyList.add(daily)
        }
    }

    val hourlyList = mutableListOf<HourlyWrapper>()
    if (weatherResult.hourly != null) {
        for (i in weatherResult.hourly.time.indices) {
            // Add to the app only if starts in the current hour
            if (weatherResult.hourly.time[i] >= System.currentTimeMillis() / 1000 - 3600) {
                hourlyList.add(HourlyWrapper(date = weatherResult.hourly.time[i].seconds.inWholeMilliseconds.toDate()))
            }
        }
    }

    return WeatherWrapper(
        dailyForecast = dailyList,
        hourlyForecast = hourlyList
    )
}
