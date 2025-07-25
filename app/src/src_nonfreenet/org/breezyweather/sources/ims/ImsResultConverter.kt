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

package org.breezyweather.sources.ims

import android.content.Context
import androidx.core.graphics.toColorInt
import breezyweather.domain.location.model.Location
import breezyweather.domain.weather.model.Alert
import breezyweather.domain.weather.model.AlertSeverity
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
import org.breezyweather.common.extensions.code
import org.breezyweather.common.extensions.currentLocale
import org.breezyweather.common.extensions.toCalendarWithTimeZone
import org.breezyweather.common.extensions.toDateNoHour
import org.breezyweather.sources.ims.json.ImsLocation
import org.breezyweather.sources.ims.json.ImsWeatherData
import java.util.Calendar
import java.util.Date
import kotlin.text.startsWith

internal fun convert(
    location: Location,
    result: ImsLocation,
): Location {
    return location.copy(
        timeZone = "Asia/Jerusalem",
        country = "", // We don’t put any country name to avoid political issues
        countryCode = "IL", // but we need to identify the location as being part of the coverage of IMS
        city = result.name
    )
}

internal fun getDailyForecast(
    location: Location,
    data: ImsWeatherData?,
): List<DailyWrapper>? {
    return data?.forecastData?.keys?.mapNotNull {
        it.toDateNoHour(location.javaTimeZone)?.let { dayDate ->
            DailyWrapper(
                date = dayDate,
                uV = data.forecastData[it]!!.daily?.maximumUVI?.toDoubleOrNull()?.let { uvi ->
                    UV(uvi)
                }
            )
        }
    }
}

internal fun getHourlyForecast(
    context: Context,
    location: Location,
    data: ImsWeatherData?,
): List<HourlyWrapper> {
    val hourlyList = mutableListOf<HourlyWrapper>()
    var previousWeatherCode = ""
    var currentWeatherCode = ""
    data?.forecastData?.keys?.forEach {
        it.toDateNoHour(location.javaTimeZone)?.let { dayDate ->
            data.forecastData[it]!!.hourly?.forEach { hourlyResult ->
                val hourlyDate = dayDate.toCalendarWithTimeZone(location.javaTimeZone).apply {
                    set(Calendar.HOUR_OF_DAY, hourlyResult.value.hour.toInt())
                }.time
                if (!hourlyResult.value.weatherCode.isNullOrEmpty()) {
                    currentWeatherCode = hourlyResult.value.weatherCode!!
                    previousWeatherCode = hourlyResult.value.weatherCode!!
                } else {
                    currentWeatherCode = previousWeatherCode
                }

                hourlyList.add(
                    HourlyWrapper(
                        date = hourlyDate,
                        weatherText = getWeatherText(context, currentWeatherCode),
                        weatherCode = getWeatherCode(currentWeatherCode),
                        temperature = TemperatureWrapper(
                            temperature = hourlyResult.value.preciseTemperature?.toDoubleOrNull(),
                            feelsLike = hourlyResult.value.windChill?.toDoubleOrNull()
                        ),
                        precipitationProbability = PrecipitationProbability(
                            total = hourlyResult.value.rainChance?.toDoubleOrNull()
                        ),
                        wind = hourlyResult.value.windSpeed?.let { windSpeed ->
                            Wind(
                                speed = windSpeed.div(3.6),
                                degree = hourlyResult.value.windDirectionId?.let { windDirId ->
                                    data.windDirections?.getOrElse(windDirId) { null }?.direction?.toDoubleOrNull()
                                }
                            )
                        },
                        uV = hourlyResult.value.uvIndex?.toDoubleOrNull()?.let { uvi ->
                            UV(uvi)
                        },
                        relativeHumidity = hourlyResult.value.relativeHumidity?.toDoubleOrNull()
                    )
                )
            }
        }
    }
    return hourlyList
}

internal fun getCurrent(
    context: Context,
    data: ImsWeatherData?,
): CurrentWrapper? {
    if (data?.analysis == null) return null
    val firstDay = data.forecastData?.keys?.minOrNull()
    val dailyForecast = firstDay?.let { data.forecastData.getOrElse(it) { null }?.country?.description }

    return CurrentWrapper(
        weatherText = getWeatherText(context, data.analysis.weatherCode),
        weatherCode = getWeatherCode(data.analysis.weatherCode),
        temperature = data.analysis.temperature?.toDoubleOrNull()?.let {
            TemperatureWrapper(
                temperature = it,
                feelsLike = data.analysis.feelsLike?.toDoubleOrNull()
            )
        },
        wind = data.analysis.windSpeed?.let { windSpeed ->
            Wind(
                speed = windSpeed.div(3.6),
                degree = data.analysis.windDirectionId?.let {
                    data.windDirections?.getOrElse(it) { null }?.direction?.toDoubleOrNull()
                }
            )
        },
        uV = data.analysis.uvIndex?.toDoubleOrNull()?.let { UV(it) },
        relativeHumidity = data.analysis.relativeHumidity?.toDoubleOrNull(),
        dewPoint = data.analysis.dewPointTemp?.toDoubleOrNull(),
        dailyForecast = dailyForecast
    )
}

internal fun getAlerts(
    context: Context,
    data: ImsWeatherData?,
): List<Alert>? {
    return data?.allWarnings?.mapNotNull { warningEntry ->
        val severity = when (warningEntry.value.severityId) {
            "6" -> AlertSeverity.EXTREME
            "4", "5" -> AlertSeverity.SEVERE
            "2", "3" -> AlertSeverity.MODERATE
            "0", "1" -> AlertSeverity.MINOR
            else -> AlertSeverity.UNKNOWN
        }
        Alert(
            alertId = warningEntry.value.alertId,
            startDate = warningEntry.value.validFromUnix?.let { Date(it.times(1000L)) },
            // TODO: endDate = warningEntry.value.validTo, // TimeZone-dependant
            headline = warningEntry.value.warningTypeId?.let {
                data.warningsMetadata?.imsWarningType?.getOrElse(it) { null }?.name
            },
            description = warningEntry.value.text,
            source = with(context.currentLocale.code) {
                when {
                    startsWith("ar") -> "خدمة الأرصاد الجوية الإسرائيلية"
                    startsWith("he") || startsWith("iw") -> "השירות המטאורולוגי הישראלי"
                    else -> "Israel Meteorological Service"
                }
            },
            severity = severity,
            color = warningEntry.value.severityId?.let { severityId ->
                data.warningsMetadata?.warningSeverity?.getOrElse(severityId) { null }?.color?.toColorInt()
            } ?: Alert.colorFromSeverity(severity)
        )
    }
}

// Source: https://ims.gov.il/en/weather_codes
private fun getWeatherText(
    context: Context,
    weatherCode: String?,
): String? {
    return when (weatherCode) {
        "1250" -> context.getString(R.string.common_weather_text_clear_sky)
        "1220" -> context.getString(R.string.common_weather_text_partly_cloudy)
        "1230" -> context.getString(R.string.common_weather_text_cloudy)
        "1570" -> context.getString(R.string.common_weather_text_dust)
        "1010" -> context.getString(R.string.common_weather_text_sand_storm)
        "1160" -> context.getString(R.string.common_weather_text_fog)
        "1310", "1580" -> context.getString(R.string.common_weather_text_hot)
        "1270" -> context.getString(R.string.common_weather_text_humid)
        "1320", "1590" -> context.getString(R.string.common_weather_text_cold)
        "1300" -> context.getString(R.string.common_weather_text_frost)
        "1140", "1530", "1540" -> context.getString(R.string.common_weather_text_rain)
        "1560" -> context.getString(R.string.common_weather_text_rain_light)
        "1020" -> context.getString(R.string.weather_kind_thunderstorm)
        "1510" -> context.getString(R.string.common_weather_text_rain_heavy)
        "1260" -> context.getString(R.string.weather_kind_wind)
        "1080" -> context.getString(R.string.common_weather_text_rain_snow_mixed)
        "1070" -> context.getString(R.string.common_weather_text_snow_light)
        "1060" -> context.getString(R.string.common_weather_text_snow)
        "1520" -> context.getString(R.string.common_weather_text_snow_heavy)
        else -> null
    }
}

// Source: https://ims.gov.il/en/weather_codes
private fun getWeatherCode(
    weatherCode: String?,
): WeatherCode? {
    return when (weatherCode) {
        "1250" -> WeatherCode.CLEAR
        "1220" -> WeatherCode.PARTLY_CLOUDY
        "1230" -> WeatherCode.CLOUDY
        "1570" -> WeatherCode.HAZE
        "1010" -> WeatherCode.WIND
        "1160" -> WeatherCode.FOG
        // "1310", "1580" -> Hot
        // "1270" -> Humid
        // "1320", "1590" -> Cold
        "1300" -> WeatherCode.SNOW // Frost
        "1140", "1530", "1540" -> WeatherCode.RAIN
        "1560" -> WeatherCode.RAIN
        "1020" -> WeatherCode.THUNDERSTORM
        "1510" -> WeatherCode.RAIN
        "1260" -> WeatherCode.WIND
        "1080" -> WeatherCode.SLEET
        "1070" -> WeatherCode.SNOW
        "1060" -> WeatherCode.SNOW
        "1520" -> WeatherCode.SNOW
        else -> null
    }
}
