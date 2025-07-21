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

package org.breezyweather.sources.metno

import android.content.Context
import breezyweather.domain.location.model.Location
import breezyweather.domain.weather.model.Alert
import breezyweather.domain.weather.model.AlertSeverity
import breezyweather.domain.weather.model.Minutely
import breezyweather.domain.weather.model.Precipitation
import breezyweather.domain.weather.model.PrecipitationProbability
import breezyweather.domain.weather.model.Temperature
import breezyweather.domain.weather.model.UV
import breezyweather.domain.weather.model.WeatherCode
import breezyweather.domain.weather.model.Wind
import breezyweather.domain.weather.wrappers.CurrentWrapper
import breezyweather.domain.weather.wrappers.DailyWrapper
import breezyweather.domain.weather.wrappers.HourlyWrapper
import breezyweather.domain.weather.wrappers.TemperatureWrapper
import org.breezyweather.R
import org.breezyweather.common.extensions.getIsoFormattedDate
import org.breezyweather.common.extensions.toDateNoHour
import org.breezyweather.sources.metno.json.MetNoAlertResult
import org.breezyweather.sources.metno.json.MetNoForecastTimeseries
import org.breezyweather.sources.metno.json.MetNoNowcastResult
import kotlin.math.roundToInt
import kotlin.time.Duration.Companion.minutes

internal fun getCurrent(nowcastResult: MetNoNowcastResult, context: Context): CurrentWrapper? {
    val currentTimeseries = nowcastResult.properties?.timeseries?.getOrNull(0)?.data
    return if (currentTimeseries != null) {
        CurrentWrapper(
            weatherText = getWeatherText(context, currentTimeseries.symbolCode),
            weatherCode = getWeatherCode(currentTimeseries.symbolCode),
            temperature = TemperatureWrapper(
                temperature = currentTimeseries.instant?.details?.airTemperature
            ),
            wind = if (currentTimeseries.instant?.details != null) {
                Wind(
                    degree = currentTimeseries.instant.details.windFromDirection,
                    speed = currentTimeseries.instant.details.windSpeed
                )
            } else {
                null
            },
            relativeHumidity = currentTimeseries.instant?.details?.relativeHumidity,
            dewPoint = currentTimeseries.instant?.details?.dewPointTemperature,
            pressure = currentTimeseries.instant?.details?.airPressureAtSeaLevel,
            cloudCover = currentTimeseries.instant?.details?.cloudAreaFraction?.roundToInt()
        )
    } else {
        null
    }
}

internal fun getHourlyList(
    context: Context,
    forecastTimeseries: List<MetNoForecastTimeseries>?,
): List<HourlyWrapper> {
    if (forecastTimeseries.isNullOrEmpty()) return emptyList()
    return forecastTimeseries.map { hourlyForecast ->
        HourlyWrapper(
            date = hourlyForecast.time,
            weatherText = getWeatherText(context, hourlyForecast.data?.symbolCode),
            weatherCode = getWeatherCode(hourlyForecast.data?.symbolCode),
            temperature = TemperatureWrapper(
                temperature = hourlyForecast.data?.instant?.details?.airTemperature
            ),
            precipitation = Precipitation(
                total = hourlyForecast.data?.next1Hours?.details?.precipitationAmount
                    ?: hourlyForecast.data?.next6Hours?.details?.precipitationAmount
                    ?: hourlyForecast.data?.next12Hours?.details?.precipitationAmount
            ),
            precipitationProbability = PrecipitationProbability(
                total = hourlyForecast.data?.next1Hours?.details?.probabilityOfPrecipitation
                    ?: hourlyForecast.data?.next6Hours?.details?.probabilityOfPrecipitation
                    ?: hourlyForecast.data?.next12Hours?.details?.probabilityOfPrecipitation,
                thunderstorm = hourlyForecast.data?.next1Hours?.details?.probabilityOfThunder
                    ?: hourlyForecast.data?.next6Hours?.details?.probabilityOfThunder
                    ?: hourlyForecast.data?.next12Hours?.details?.probabilityOfThunder
            ),
            wind = if (hourlyForecast.data?.instant?.details != null) {
                Wind(
                    degree = hourlyForecast.data.instant.details.windFromDirection,
                    speed = hourlyForecast.data.instant.details.windSpeed
                )
            } else {
                null
            },
            uV = UV(index = hourlyForecast.data?.instant?.details?.ultravioletIndexClearSky),
            relativeHumidity = hourlyForecast.data?.instant?.details?.relativeHumidity,
            dewPoint = hourlyForecast.data?.instant?.details?.dewPointTemperature,
            pressure = hourlyForecast.data?.instant?.details?.airPressureAtSeaLevel,
            cloudCover = hourlyForecast.data?.instant?.details?.cloudAreaFraction?.roundToInt()
        )
    }
}

internal fun getDailyList(
    location: Location,
    forecastTimeseries: List<MetNoForecastTimeseries>?,
): List<DailyWrapper> {
    if (forecastTimeseries.isNullOrEmpty()) return emptyList()
    val dailyList = mutableListOf<DailyWrapper>()
    val hourlyListByDay = forecastTimeseries.groupBy {
        it.time.getIsoFormattedDate(location)
    }
    for (i in 0 until hourlyListByDay.entries.size - 1) {
        hourlyListByDay.keys.toTypedArray()[i].toDateNoHour(location.javaTimeZone)?.let { dayDate ->
            dailyList.add(
                DailyWrapper(date = dayDate)
            )
        }
    }
    return dailyList
}

internal fun getMinutelyList(nowcastTimeseries: List<MetNoForecastTimeseries>?): List<Minutely> {
    val minutelyList: MutableList<Minutely> = arrayListOf()
    if (nowcastTimeseries.isNullOrEmpty() || nowcastTimeseries.size < 2) return minutelyList

    nowcastTimeseries.forEachIndexed { i, nowcastForecast ->
        minutelyList.add(
            Minutely(
                date = nowcastForecast.time,
                minuteInterval = if (i < nowcastTimeseries.size - 1) {
                    (nowcastTimeseries[i + 1].time.time - nowcastForecast.time.time)
                        .div(1.minutes.inWholeMilliseconds)
                        .toDouble().roundToInt()
                } else {
                    (nowcastForecast.time.time - nowcastTimeseries[i - 1].time.time)
                        .div(1.minutes.inWholeMilliseconds)
                        .toDouble().roundToInt()
                },
                precipitationIntensity = nowcastForecast.data?.instant?.details?.precipitationRate
            )
        )
    }

    return minutelyList
}

internal fun getAlerts(metNoAlerts: MetNoAlertResult): List<Alert>? {
    return metNoAlerts.features?.mapNotNull { alert ->
        alert.properties?.let {
            val severity = when (it.severity?.lowercase()) {
                "extreme" -> AlertSeverity.EXTREME
                "severe" -> AlertSeverity.SEVERE
                "moderate" -> AlertSeverity.MODERATE
                "minor" -> AlertSeverity.MINOR
                else -> AlertSeverity.UNKNOWN
            }
            Alert(
                alertId = it.id,
                startDate = alert.whenAlert?.interval?.getOrNull(0),
                endDate = alert.whenAlert?.interval?.getOrNull(1) ?: it.eventEndingTime,
                headline = it.eventAwarenessName,
                description = it.description,
                instruction = it.instruction,
                source = "MET Norway",
                severity = severity,
                color = Alert.colorFromSeverity(severity)
            )
        }
    }
}

private fun getWeatherCode(icon: String?): WeatherCode? {
    return if (icon == null) {
        null
    } else {
        when (icon.replace("_night", "").replace("_day", "")) {
            "clearsky", "fair" -> WeatherCode.CLEAR
            "partlycloudy" -> WeatherCode.PARTLY_CLOUDY
            "cloudy" -> WeatherCode.CLOUDY
            "fog" -> WeatherCode.FOG
            "heavyrain", "heavyrainshowers", "lightrain", "lightrainshowers", "rain", "rainshowers" -> WeatherCode.RAIN
            "heavyrainandthunder", "heavyrainshowersandthunder", "heavysleetandthunder", "heavysleetshowersandthunder",
            "heavysnowandthunder", "heavysnowshowersandthunder", "lightrainandthunder", "lightrainshowersandthunder",
            "lightsleetandthunder", "lightsleetshowersandthunder", "lightsnowandthunder", "lightsnowshowersandthunder",
            "rainandthunder", "rainshowersandthunder", "sleetandthunder", "sleetshowersandthunder", "snowandthunder",
            "snowshowersandthunder",
            -> WeatherCode.THUNDERSTORM
            "heavysnow", "heavysnowshowers", "lightsnow", "lightsnowshowers", "snow", "snowshowers" -> WeatherCode.SNOW
            "heavysleet", "heavysleetshowers", "lightsleet", "lightsleetshowers", "sleet", "sleetshowers" ->
                WeatherCode.SLEET
            else -> null
        }
    }
}

private fun getWeatherText(context: Context, icon: String?): String? {
    if (icon == null) return null
    val weatherWithoutThunder = when (
        icon.replace("_night", "")
            .replace("_day", "")
            .replace("andthunder", "")
    ) {
        "clearsky" -> context.getString(R.string.metno_weather_text_clearsky)
        "cloudy" -> context.getString(R.string.metno_weather_text_cloudy)
        "fair" -> context.getString(R.string.metno_weather_text_fair)
        "fog" -> context.getString(R.string.metno_weather_text_fog)
        "heavyrain" -> context.getString(R.string.metno_weather_text_heavyrain)
        "heavyrainshowers" -> context.getString(R.string.metno_weather_text_heavyrainshowers)
        "heavysleet" -> context.getString(R.string.metno_weather_text_heavysleet)
        "heavysleetshowers" -> context.getString(R.string.metno_weather_text_heavysleetshowers)
        "heavysnow" -> context.getString(R.string.metno_weather_text_heavysnow)
        "heavysnowshowers" -> context.getString(R.string.metno_weather_text_heavysnowshowers)
        "lightrain" -> context.getString(R.string.metno_weather_text_lightrain)
        "lightrainshowers" -> context.getString(R.string.metno_weather_text_lightrainshowers)
        "lightsleet" -> context.getString(R.string.metno_weather_text_lightsleet)
        "lightsleetshowers" -> context.getString(R.string.metno_weather_text_lightsleetshowers)
        "lightsnow" -> context.getString(R.string.metno_weather_text_lightsnow)
        "lightsnowshowers" -> context.getString(R.string.metno_weather_text_lightsnowshowers)
        "partlycloudy" -> context.getString(R.string.metno_weather_text_partlycloudy)
        "rain" -> context.getString(R.string.metno_weather_text_rain)
        "rainshowers" -> context.getString(R.string.metno_weather_text_rainshowers)
        "sleet" -> context.getString(R.string.metno_weather_text_sleet)
        "sleetshowers" -> context.getString(R.string.metno_weather_text_sleetshowers)
        "snow" -> context.getString(R.string.metno_weather_text_snow)
        "snowshowers" -> context.getString(R.string.metno_weather_text_snowshowers)
        else -> null
    }

    return if (icon.contains("andthunder")) {
        context.getString(
            R.string.metno_weather_text_andthunder,
            weatherWithoutThunder ?: context.getString(R.string.null_data_text)
        )
    } else {
        weatherWithoutThunder
    }
}
