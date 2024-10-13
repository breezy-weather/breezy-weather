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
import breezyweather.domain.weather.model.AirQuality
import breezyweather.domain.weather.model.Alert
import breezyweather.domain.weather.model.AlertSeverity
import breezyweather.domain.weather.model.Astro
import breezyweather.domain.weather.model.Current
import breezyweather.domain.weather.model.Daily
import breezyweather.domain.weather.model.Minutely
import breezyweather.domain.weather.model.MoonPhase
import breezyweather.domain.weather.model.Precipitation
import breezyweather.domain.weather.model.PrecipitationProbability
import breezyweather.domain.weather.model.Temperature
import breezyweather.domain.weather.model.UV
import breezyweather.domain.weather.model.WeatherCode
import breezyweather.domain.weather.model.Wind
import breezyweather.domain.weather.wrappers.AirQualityWrapper
import breezyweather.domain.weather.wrappers.HourlyWrapper
import breezyweather.domain.weather.wrappers.SecondaryWeatherWrapper
import breezyweather.domain.weather.wrappers.WeatherWrapper
import org.breezyweather.R
import org.breezyweather.common.exceptions.InvalidOrIncompleteDataException
import org.breezyweather.common.extensions.getFormattedDate
import org.breezyweather.common.extensions.toDate
import org.breezyweather.common.extensions.toDateNoHour
import org.breezyweather.sources.metno.json.MetNoAirQualityResult
import org.breezyweather.sources.metno.json.MetNoAlertResult
import org.breezyweather.sources.metno.json.MetNoForecastResult
import org.breezyweather.sources.metno.json.MetNoForecastTimeseries
import org.breezyweather.sources.metno.json.MetNoMoonProperties
import org.breezyweather.sources.metno.json.MetNoMoonResult
import org.breezyweather.sources.metno.json.MetNoNowcastResult
import org.breezyweather.sources.metno.json.MetNoSunProperties
import org.breezyweather.sources.metno.json.MetNoSunResult
import java.util.Date
import java.util.Objects
import kotlin.math.roundToInt
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds

fun convert(
    context: Context,
    location: Location,
    forecastResult: MetNoForecastResult,
    sunResult: MetNoSunResult,
    moonResult: MetNoMoonResult,
    nowcastResult: MetNoNowcastResult,
    airQualityResult: MetNoAirQualityResult,
    metNoAlerts: MetNoAlertResult
): WeatherWrapper {
    // If the API doesn’t return hourly, consider data as garbage and keep cached data
    if (forecastResult.properties == null ||
        forecastResult.properties.timeseries.isNullOrEmpty()) {
        throw InvalidOrIncompleteDataException()
    }

    val currentTimeseries = nowcastResult.properties?.timeseries?.getOrNull(0)?.data
    return WeatherWrapper(
        /*base = Base(
            // TODO: Use nowcast updatedAt if available
            publishDate = forecastResult.properties.meta?.updatedAt ?: Date()
        ),*/
        current = if (currentTimeseries != null) Current(
            weatherText = getWeatherText(context, currentTimeseries.symbolCode),
            weatherCode = getWeatherCode(currentTimeseries.symbolCode),
            temperature = Temperature(
                temperature = currentTimeseries.instant?.details?.airTemperature,
            ),
            wind = if (currentTimeseries.instant?.details != null) Wind(
                degree = currentTimeseries.instant.details.windFromDirection,
                speed = currentTimeseries.instant.details.windSpeed
            ) else null,
            relativeHumidity = currentTimeseries.instant?.details?.relativeHumidity,
            dewPoint = currentTimeseries.instant?.details?.dewPointTemperature,
            pressure = currentTimeseries.instant?.details?.airPressureAtSeaLevel,
            cloudCover = currentTimeseries.instant?.details?.cloudAreaFraction?.roundToInt()
        ) else null,
        dailyForecast = getDailyList(
            location,
            sunResult.properties,
            moonResult.properties,
            forecastResult.properties.timeseries
        ),
        hourlyForecast = getHourlyList(
            context,
            forecastResult.properties.timeseries,
            airQualityResult
        ),
        minutelyForecast = getMinutelyList(nowcastResult.properties?.timeseries),
        alertList = getAlerts(metNoAlerts)
    )
}

private fun getHourlyList(
    context: Context,
    forecastTimeseries: List<MetNoForecastTimeseries>,
    airQualityResult: MetNoAirQualityResult
): List<HourlyWrapper> {
    return forecastTimeseries.map { hourlyForecast ->
        val airQualityDataResult = airQualityResult.data?.time?.firstOrNull { it.from.time == hourlyForecast.time.time }

        HourlyWrapper(
            date = hourlyForecast.time,
            weatherText = getWeatherText(context, hourlyForecast.data?.symbolCode),
            weatherCode = getWeatherCode(hourlyForecast.data?.symbolCode),
            temperature = Temperature(
                temperature = hourlyForecast.data?.instant?.details?.airTemperature,
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
            wind = if (hourlyForecast.data?.instant?.details != null) Wind(
                degree = hourlyForecast.data.instant.details.windFromDirection,
                speed = hourlyForecast.data.instant.details.windSpeed
            ) else null,
            airQuality = if (airQualityDataResult != null) AirQuality(
                pM25 = airQualityDataResult.variables?.pm25Concentration?.value,
                pM10 = airQualityDataResult.variables?.pm10Concentration?.value,
                sO2 = airQualityDataResult.variables?.so2Concentration?.value,
                nO2 = airQualityDataResult.variables?.no2Concentration?.value,
                o3 = airQualityDataResult.variables?.o3Concentration?.value
            ) else null,
            uV = UV(index = hourlyForecast.data?.instant?.details?.ultravioletIndexClearSky),
            relativeHumidity = hourlyForecast.data?.instant?.details?.relativeHumidity,
            dewPoint = hourlyForecast.data?.instant?.details?.dewPointTemperature,
            pressure = hourlyForecast.data?.instant?.details?.airPressureAtSeaLevel,
            cloudCover = hourlyForecast.data?.instant?.details?.cloudAreaFraction?.roundToInt()
        )
    }
}

private fun getDailyList(
    location: Location,
    sunResult: MetNoSunProperties?,
    moonResult: MetNoMoonProperties?,
    forecastTimeseries: List<MetNoForecastTimeseries>
): List<Daily> {
    val dailyList = mutableListOf<Daily>()
    val hourlyListByDay = forecastTimeseries.groupBy {
        it.time.getFormattedDate("yyyy-MM-dd", location)
    }
    for (i in 0 until hourlyListByDay.entries.size - 1) {
        val dayDate = hourlyListByDay.keys.toTypedArray()[i].toDateNoHour(location.javaTimeZone)
        if (dayDate != null) {
            dailyList.add(
                Daily(
                    date = dayDate,
                    sun = if (i == 0) Astro(
                        riseDate = sunResult?.sunrise?.time,
                        setDate = sunResult?.sunset?.time,
                    ) else null,
                    moon = if (i == 0) Astro(
                        riseDate = moonResult?.moonrise?.time,
                        setDate = moonResult?.moonset?.time,
                    ) else null,
                    moonPhase = if (i == 0) MoonPhase(
                        angle = moonResult?.moonphase?.roundToInt()
                    ) else null
                )
            )
        }
    }
    return dailyList
}

private fun getMinutelyList(nowcastTimeseries: List<MetNoForecastTimeseries>?): List<Minutely> {
    val minutelyList: MutableList<Minutely> = arrayListOf()
    if (nowcastTimeseries.isNullOrEmpty() || nowcastTimeseries.size < 2) return minutelyList

    nowcastTimeseries.forEachIndexed { i, nowcastForecast ->
        minutelyList.add(
            Minutely(
                date = nowcastForecast.time,
                minuteInterval = if (i < nowcastTimeseries.size - 1) {
                    ((nowcastTimeseries[i + 1].time.time - nowcastForecast.time.time) / 1.minutes.inWholeMilliseconds).toDouble()
                        .roundToInt()
                } else ((nowcastForecast.time.time - nowcastTimeseries[i - 1].time.time) / 1.minutes.inWholeMilliseconds).toDouble()
                    .roundToInt(),
                precipitationIntensity = nowcastForecast.data?.instant?.details?.precipitationRate
            )
        )
    }

    return minutelyList
}

private fun getAlerts(metNoAlerts: MetNoAlertResult): List<Alert>? {
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
    } else when(icon.replace("_night", "").replace("_day", "")) {
        "clearsky", "fair" -> WeatherCode.CLEAR
        "partlycloudy" -> WeatherCode.PARTLY_CLOUDY
        "cloudy" -> WeatherCode.CLOUDY
        "fog" -> WeatherCode.FOG
        "heavyrain", "heavyrainshowers", "lightrain", "lightrainshowers", "rain", "rainshowers" -> WeatherCode.RAIN
        "heavyrainandthunder", "heavyrainshowersandthunder", "heavysleetandthunder", "heavysleetshowersandthunder",
        "heavysnowandthunder", "heavysnowshowersandthunder", "lightrainandthunder", "lightrainshowersandthunder",
        "lightsleetandthunder", "lightsleetshowersandthunder", "lightsnowandthunder", "lightsnowshowersandthunder",
        "rainandthunder", "rainshowersandthunder", "sleetandthunder", "sleetshowersandthunder", "snowandthunder",
        "snowshowersandthunder" -> WeatherCode.THUNDERSTORM
        "heavysnow", "heavysnowshowers", "lightsnow", "lightsnowshowers", "snow", "snowshowers" -> WeatherCode.SNOW
        "heavysleet", "heavysleetshowers", "lightsleet", "lightsleetshowers", "sleet",
        "sleetshowers" -> WeatherCode.SLEET
        else -> null
    }
}

private fun getWeatherText(context: Context, icon: String?): String? {
    if (icon == null) return null
    val weatherWithoutThunder = when (icon
        .replace("_night", "")
        .replace("_day", "")
        .replace("andthunder", "")) {
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
        context.getString(R.string.metno_weather_text_andthunder, weatherWithoutThunder ?: context.getString(R.string.null_data_text))
    } else weatherWithoutThunder
}

fun convertSecondary(
    nowcastResult: MetNoNowcastResult?,
    airQualityResult: MetNoAirQualityResult?,
    metNoAlertResults: MetNoAlertResult?
): SecondaryWeatherWrapper {
    val airQualityHourly: MutableMap<Date, AirQuality> = mutableMapOf()

    airQualityResult?.data?.time?.forEach {
        airQualityHourly[it.from] = AirQuality(
            pM25 = it.variables?.pm25Concentration?.value,
            pM10 = it.variables?.pm10Concentration?.value,
            sO2 = it.variables?.so2Concentration?.value,
            nO2 = it.variables?.no2Concentration?.value,
            o3 = it.variables?.o3Concentration?.value
        )
    }

    return SecondaryWeatherWrapper(
        airQuality = if (airQualityResult != null) {
            AirQualityWrapper(hourlyForecast = airQualityHourly)
        } else null,
        minutelyForecast = if (nowcastResult != null) {
            getMinutelyList(nowcastResult.properties?.timeseries)
        } else null
    )
}
