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

package org.breezyweather.sources.nws

import android.content.Context
import android.graphics.Color
import breezyweather.domain.location.model.Location
import breezyweather.domain.weather.model.Alert
import breezyweather.domain.weather.model.Precipitation
import breezyweather.domain.weather.model.PrecipitationProbability
import breezyweather.domain.weather.model.Wind
import breezyweather.domain.weather.reference.AlertSeverity
import breezyweather.domain.weather.reference.WeatherCode
import breezyweather.domain.weather.wrappers.CurrentWrapper
import breezyweather.domain.weather.wrappers.DailyWrapper
import breezyweather.domain.weather.wrappers.HalfDayWrapper
import breezyweather.domain.weather.wrappers.HourlyWrapper
import breezyweather.domain.weather.wrappers.TemperatureWrapper
import org.breezyweather.R
import org.breezyweather.common.exceptions.ParsingException
import org.breezyweather.common.extensions.toCalendarWithTimeZone
import org.breezyweather.common.utils.ISO8601Utils
import org.breezyweather.sources.computeMeanSeaLevelPressure
import org.breezyweather.sources.getWindDegree
import org.breezyweather.sources.nws.json.NwsAlert
import org.breezyweather.sources.nws.json.NwsCurrentResult
import org.breezyweather.sources.nws.json.NwsDailyResult
import org.breezyweather.sources.nws.json.NwsGridPointProperties
import org.breezyweather.sources.nws.json.NwsPointProperties
import org.breezyweather.sources.nws.json.NwsValueDoubleContainer
import org.breezyweather.sources.nws.json.NwsValueIntContainer
import org.breezyweather.sources.nws.json.NwsValueWeatherContainer
import org.breezyweather.sources.nws.json.NwsValueWeatherValue
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.TimeZone
import kotlin.time.Duration.Companion.parseIsoString

internal fun convert(
    location: Location,
    locationProperties: NwsPointProperties,
): Location {
    return location.copy(
        timeZone = locationProperties.timeZone,
        countryCode = "US",
        admin1 = locationProperties.relativeLocation?.properties?.state,
        admin1Code = locationProperties.relativeLocation?.properties?.state,
        city = locationProperties.relativeLocation?.properties?.city ?: ""
    )
}

internal fun getCurrent(
    currentResult: NwsCurrentResult,
): CurrentWrapper? {
    return currentResult.properties?.let {
        CurrentWrapper(
            weatherText = it.textDescription,
            weatherCode = getWeatherCode(it.icon),
            temperature = TemperatureWrapper(
                temperature = it.temperature?.value,
                feelsLike = it.windChill?.value
            ),
            // stations where the anemometer is not working would report 0 wind speed; ignore them
            wind = if (it.windSpeed?.value != null && it.windSpeed.value != 0.0) {
                Wind(
                    degree = it.windDirection?.value,
                    speed = it.windSpeed.value.div(3.6),
                    gusts = it.windGust?.value?.div(3.6)
                )
            } else {
                null
            },
            relativeHumidity = it.relativeHumidity?.value,
            dewPoint = it.dewpoint?.value,
            pressure = if (it.seaLevelPressure != null) {
                it.seaLevelPressure.value?.div(100.0)
            } else {
                computeMeanSeaLevelPressure(
                    barometricPressure = it.barometricPressure?.value?.div(100.0),
                    altitude = it.elevation?.value,
                    temperature = it.temperature?.value,
                    humidity = it.relativeHumidity?.value,
                    latitude = currentResult.geometry?.coordinates?.getOrNull(1)
                )
            },
            visibility = it.visibility?.value
        )
    }
}

internal fun getDailyForecast(
    location: Location,
    dailyResult: NwsDailyResult,
): List<DailyWrapper> {
    val dailyList = mutableListOf<DailyWrapper>()
    val formatter = SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH)
    formatter.timeZone = TimeZone.getTimeZone(location.timeZone)
    val dayParts = mutableMapOf<String, HalfDayWrapper>()
    val nightParts = mutableMapOf<String, HalfDayWrapper>()
    var date: String
    dailyResult.properties?.periods?.forEach {
        date = formatter.format(it.startTime.time)
        if (it.isDaytime) {
            dayParts[date] = HalfDayWrapper(
                weatherText = it.shortForecast,
                weatherCode = getWeatherCode(it.icon),
                temperature = TemperatureWrapper(
                    temperature = it.temperature?.value
                ),
                precipitationProbability = PrecipitationProbability(
                    total = it.probabilityOfPrecipitation?.value
                ),
                wind = Wind(
                    degree = getWindDegree(it.windDirection),
                    speed = (it.windSpeed?.maxValue ?: it.windSpeed?.value)?.div(3.6),
                    gusts = it.windGust?.value?.div(3.6)
                )
            )
        } else {
            nightParts[date] = HalfDayWrapper(
                weatherText = it.shortForecast,
                weatherCode = getWeatherCode(it.icon),
                temperature = TemperatureWrapper(
                    temperature = it.temperature?.value
                ),
                precipitationProbability = PrecipitationProbability(
                    total = it.probabilityOfPrecipitation?.value
                ),
                wind = Wind(
                    degree = getWindDegree(it.windDirection),
                    speed = (it.windSpeed?.maxValue ?: it.windSpeed?.value)?.div(3.6),
                    gusts = it.windGust?.value?.div(3.6)
                )
            )
        }
    }
    nightParts.keys.sorted().forEach { key ->
        dailyList.add(
            DailyWrapper(
                date = formatter.parse(key)!!,
                day = dayParts.getOrElse(key) { null },
                night = nightParts.getOrElse(key) { null }
            )
        )
    }
    if (dayParts.keys.maxOf { it } != nightParts.keys.maxOf { it }) {
        val lastKey = dayParts.keys.maxOf { it }
        dailyList.add(
            DailyWrapper(
                date = formatter.parse(lastKey)!!,
                day = dayParts.getOrElse(lastKey) { null }
            )
        )
    }
    return dailyList
}

internal fun getHourlyForecast(
    properties: NwsGridPointProperties?,
    location: Location,
    context: Context,
): List<HourlyWrapper>? {
    if (properties == null) return null
    val weatherForecastList = getWeatherForecast(properties.weather, location)

    val temperatureForecastList = getDoubleForecast(properties.temperature, false, location)
    val apparentTemperatureForecastList = getDoubleForecast(properties.apparentTemperature, false, location)
    val wetBulbGlobeTemperatureForecastList =
        getDoubleForecast(properties.wetBulbGlobeTemperature, false, location)
    // val heatIndexForecastList = getDoubleForecast(properties.heatIndex, false, timeZone)
    val windChillForecastList = getDoubleForecast(properties.windChill, false, location)

    val dewpointForecastList = getDoubleForecast(properties.dewpoint, false, location)
    val relativeHumidityList = getIntForecast(properties.relativeHumidity, location)

    val quantitativePrecipitationForecastList =
        getDoubleForecast(properties.quantitativePrecipitation, true, location)
    val snowfallAmountForecastList = getDoubleForecast(properties.snowfallAmount, true, location)
    val iceAccumulationForecastList = getDoubleForecast(properties.iceAccumulation, true, location)

    val probabilityOfPrecipitationForecastList = getIntForecast(properties.probabilityOfPrecipitation, location)
    val probabilityOfThunderForecastList = getDoubleForecast(properties.probabilityOfThunder, false, location)

    val windDirectionForecastList = getIntForecast(properties.windDirection, location)
    val windSpeedForecastList = getDoubleForecast(properties.windSpeed, false, location)
    val windGustForecastList = getDoubleForecast(properties.windGust, false, location)

    val pressureForecastList = getDoubleForecast(properties.pressure, false, location)

    val skyCoverForecastList = getIntForecast(properties.skyCover, location)
    val visibilityForecastList = getDoubleForecast(properties.visibility, false, location)
    // val ceilingHeightForecastList = getDoubleForecast(properties.ceilingHeight, false, timeZone)

    val uniqueDates = (
        temperatureForecastList.keys +
            dewpointForecastList.keys +
            relativeHumidityList.keys +
            apparentTemperatureForecastList.keys +
            wetBulbGlobeTemperatureForecastList.keys +
            // heatIndexForecastList.keys +
            windChillForecastList.keys +
            skyCoverForecastList.keys +
            windDirectionForecastList.keys +
            windSpeedForecastList.keys +
            windGustForecastList.keys +
            weatherForecastList.keys +
            probabilityOfPrecipitationForecastList.keys +
            quantitativePrecipitationForecastList.keys +
            iceAccumulationForecastList.keys +
            snowfallAmountForecastList.keys +
            // ceilingHeightForecastList.keys +
            visibilityForecastList.keys +
            pressureForecastList.keys +
            probabilityOfThunderForecastList.keys
        ).sorted()

    return uniqueDates.map {
        HourlyWrapper(
            date = it,
            weatherText = getWeatherText(
                context = context,
                weather = weatherForecastList.getOrElse(it) { null },
                windSpeed = windSpeedForecastList.getOrElse(it) { null }?.div(3.6),
                cloudCover = skyCoverForecastList.getOrElse(it) { null }
            ),
            weatherCode = getWeatherCode(
                weather = weatherForecastList.getOrElse(it) { null },
                windSpeed = windSpeedForecastList.getOrElse(it) { null }?.div(3.6),
                cloudCover = skyCoverForecastList.getOrElse(it) { null }
            ),
            temperature = TemperatureWrapper(
                temperature = temperatureForecastList.getOrElse(it) { null },
                feelsLike = apparentTemperatureForecastList.getOrElse(it) { null }
            ),
            precipitation = Precipitation(
                total = quantitativePrecipitationForecastList.getOrElse(it) { null },
                snow = snowfallAmountForecastList.getOrElse(it) { null },
                ice = iceAccumulationForecastList.getOrElse(it) { null }
            ),
            precipitationProbability = PrecipitationProbability(
                total = probabilityOfPrecipitationForecastList.getOrElse(it) { null }?.toDouble(),
                thunderstorm = probabilityOfThunderForecastList.getOrElse(it) { null }?.toDouble()
            ),
            wind = Wind(
                degree = windDirectionForecastList.getOrElse(it) { null }?.toDouble(),
                speed = windSpeedForecastList.getOrElse(it) { null }?.div(3.6),
                gusts = windGustForecastList.getOrElse(it) { null }?.div(3.6)
            ),
            relativeHumidity = relativeHumidityList.getOrElse(it) { null }?.toDouble(),
            dewPoint = dewpointForecastList.getOrElse(it) { null },
            // Pressure is given in inHg - convert to hPa with conventional multiple
            pressure = pressureForecastList.getOrElse(it) { null }?.times(33.86389),
            cloudCover = skyCoverForecastList.getOrElse(it) { null },
            visibility = visibilityForecastList.getOrElse(it) { null }
        )
    }
}

internal fun getAlerts(alerts: List<NwsAlert>?): List<Alert>? {
    if (alerts.isNullOrEmpty()) return null
    // Look for SINGLE line breaks surrounded by letters, numbers, and punctuation.
    val regex = Regex("""([0-9A-Za-z.,]) *\n([0-9A-Za-z])""")
    return alerts.filter { it.properties != null }.map {
        val severity = when (it.properties!!.severity?.lowercase()) {
            "extreme" -> AlertSeverity.EXTREME
            "severe" -> AlertSeverity.SEVERE
            "moderate" -> AlertSeverity.MODERATE
            "minor" -> AlertSeverity.MINOR
            else -> AlertSeverity.UNKNOWN
        }
        Alert(
            alertId = it.properties.id,
            startDate = it.properties.onset,
            endDate = it.properties.expires,
            headline = it.properties.event ?: it.properties.headline,
            description = it.properties.description?.let { d -> regex.replace(d, "$1 $2") },
            instruction = it.properties.instruction?.let { d -> regex.replace(d, "$1 $2") },
            source = it.properties.senderName?.ifEmpty { null } ?: it.properties.sender ?: "NWS",
            severity = severity,
            color = getAlertColor(it.properties.event) ?: Alert.colorFromSeverity(severity)
        )
    }
}

private fun getDoubleForecast(
    doubleProperties: NwsValueDoubleContainer?,
    multipleHoursAreDivided: Boolean = false,
    location: Location,
): Map<Date, Double> {
    if (doubleProperties?.values == null) return emptyMap()

    val doubleForecast = mutableMapOf<Date, Double>()
    doubleProperties.values.forEach {
        if (it.value != null) {
            val dateInterval = it.validTime.split("/")
            val date = ISO8601Utils.parse(dateInterval[0])
            val durationInHours = parseIsoString(dateInterval[1]).inWholeHours.toInt()

            // Just to be sure we didn't get sent 1 century
            if (durationInHours > 360) { // 15 days
                throw ParsingException()
            }

            for (i in 1..durationInHours) {
                val newDate = if (i > 1) {
                    date.toCalendarWithTimeZone(location.javaTimeZone).apply {
                        add(Calendar.HOUR_OF_DAY, i - 1)
                    }.time
                } else {
                    date
                }
                doubleForecast[newDate] = if (multipleHoursAreDivided) {
                    it.value.div(durationInHours)
                } else {
                    it.value
                }
            }
        }
    }

    return doubleForecast
}

private fun getIntForecast(
    intProperties: NwsValueIntContainer?,
    location: Location,
): Map<Date, Int> {
    if (intProperties?.values == null) return emptyMap()

    val intForecast = mutableMapOf<Date, Int>()
    intProperties.values.forEach {
        if (it.value != null) {
            val dateInterval = it.validTime.split("/")
            val date = ISO8601Utils.parse(dateInterval[0])
            val durationInHours = parseIsoString(dateInterval[1]).inWholeHours.toInt()

            // Just to be sure we didn't get sent 1 century
            if (durationInHours > 360) { // 15 days
                throw ParsingException()
            }

            for (i in 1..durationInHours) {
                val newDate = if (i > 1) {
                    date.toCalendarWithTimeZone(location.javaTimeZone).apply {
                        add(Calendar.HOUR_OF_DAY, i - 1)
                    }.time
                } else {
                    date
                }
                intForecast[newDate] = it.value
            }
        }
    }

    return intForecast
}

private fun getWeatherForecast(
    weatherProperties: NwsValueWeatherContainer?,
    location: Location,
): Map<Date, NwsValueWeatherValue> {
    if (weatherProperties?.values == null) return emptyMap()

    val weatherForecast = mutableMapOf<Date, NwsValueWeatherValue>()
    weatherProperties.values.forEach {
        if (!it.value?.getOrNull(0)?.weather.isNullOrEmpty()) {
            val dateInterval = it.validTime.split("/")
            val date = ISO8601Utils.parse(dateInterval[0])
            val durationInHours = parseIsoString(dateInterval[1]).inWholeHours.toInt()

            // Just to be sure we didn't get sent 1 century
            if (durationInHours > 360) { // 15 days
                throw ParsingException()
            }

            for (i in 1..durationInHours) {
                val newDate = if (i > 1) {
                    date.toCalendarWithTimeZone(location.javaTimeZone).apply {
                        add(Calendar.HOUR_OF_DAY, i - 1)
                    }.time
                } else {
                    date
                }
                weatherForecast[newDate] = it.value!![0]
            }
        }
    }

    return weatherForecast
}

/**
 * Based on https://www.weather.gov/help-map
 * Last updated October 1, 2024
 */
private fun getAlertColor(event: String?): Int? {
    return when (event) {
        "Tsunami Warning" -> Color.rgb(253, 99, 71)
        "Tornado Warning" -> Color.rgb(255, 0, 0)
        "Extreme Wind Warning" -> Color.rgb(255, 140, 0)
        "Severe Thunderstorm Warning" -> Color.rgb(255, 165, 0)
        "Flash Flood Warning" -> Color.rgb(139, 0, 0)
        "Flash Flood Statement" -> Color.rgb(139, 0, 0)
        "Severe Weather Statement" -> Color.rgb(0, 255, 255)
        "Shelter In Place Warning" -> Color.rgb(250, 128, 114)
        "Evacuation Immediate" -> Color.rgb(127, 255, 0)
        "Civil Danger Warning" -> Color.rgb(255, 182, 193)
        "Nuclear Power Plant Warning" -> Color.rgb(75, 0, 130)
        "Radiological Hazard Warning" -> Color.rgb(75, 0, 130)
        "Hazardous Materials Warning" -> Color.rgb(75, 0, 130)
        "Fire Warning" -> Color.rgb(160, 82, 45)
        "Civil Emergency Message" -> Color.rgb(255, 182, 193)
        "Law Enforcement Warning" -> Color.rgb(192, 192, 192)
        "Storm Surge Warning" -> Color.rgb(181, 36, 247)
        "Hurricane Force Wind Warning" -> Color.rgb(205, 92, 92)
        "Hurricane Warning" -> Color.rgb(220, 20, 60)
        "Typhoon Warning" -> Color.rgb(220, 20, 60)
        "Special Marine Warning" -> Color.rgb(255, 165, 0)
        "Blizzard Warning" -> Color.rgb(255, 69, 0)
        "Snow Squall Warning" -> Color.rgb(199, 21, 133)
        "Ice Storm Warning" -> Color.rgb(139, 0, 139)
        "Heavy Freezing Spray Warning" -> Color.rgb(0, 191, 255)
        "Winter Storm Warning" -> Color.rgb(255, 105, 180)
        "Lake Effect Snow Warning" -> Color.rgb(0, 139, 139)
        "Dust Storm Warning" -> Color.rgb(255, 228, 196)
        "Blowing Dust Warning" -> Color.rgb(255, 228, 196)
        "High Wind Warning" -> Color.rgb(218, 165, 32)
        "Tropical Storm Warning" -> Color.rgb(178, 34, 34)
        "Storm Warning" -> Color.rgb(148, 0, 211)
        "Tsunami Advisory" -> Color.rgb(210, 105, 30)
        "Tsunami Watch" -> Color.rgb(255, 0, 255)
        "Avalanche Warning" -> Color.rgb(30, 144, 255)
        "Earthquake Warning" -> Color.rgb(139, 69, 19)
        "Volcano Warning" -> Color.rgb(47, 79, 79)
        "Ashfall Warning" -> Color.rgb(169, 169, 169)
        "Flood Warning" -> Color.rgb(0, 255, 0)
        "Coastal Flood Warning" -> Color.rgb(34, 139, 34)
        "Lakeshore Flood Warning" -> Color.rgb(34, 139, 34)
        "Ashfall Advisory" -> Color.rgb(105, 105, 105)
        "High Surf Warning" -> Color.rgb(34, 139, 34)
        "Excessive Heat Warning" -> Color.rgb(199, 21, 133)
        "Tornado Watch" -> Color.rgb(255, 255, 0)
        "Severe Thunderstorm Watch" -> Color.rgb(219, 112, 147)
        "Flash Flood Watch" -> Color.rgb(46, 139, 87)
        "Gale Warning" -> Color.rgb(221, 160, 221)
        "Flood Statement" -> Color.rgb(0, 255, 0)
        "Extreme Cold Warning" -> Color.rgb(0, 0, 255)
        "Freeze Warning" -> Color.rgb(72, 61, 139)
        "Red Flag Warning" -> Color.rgb(255, 20, 147)
        "Storm Surge Watch" -> Color.rgb(219, 127, 247)
        "Hurricane Watch" -> Color.rgb(255, 0, 255)
        "Hurricane Force Wind Watch" -> Color.rgb(153, 50, 204)
        "Typhoon Watch" -> Color.rgb(255, 0, 255)
        "Tropical Storm Watch" -> Color.rgb(240, 128, 128)
        "Storm Watch" -> Color.rgb(255, 228, 181)
        "Tropical Cyclone Local Statement" -> Color.rgb(255, 228, 181)
        "Winter Weather Advisory" -> Color.rgb(123, 104, 238)
        "Avalanche Advisory" -> Color.rgb(205, 133, 63)
        "Cold Weather Advisory" -> Color.rgb(175, 238, 238)
        "Heat Advisory" -> Color.rgb(255, 127, 80)
        "Flood Advisory" -> Color.rgb(0, 255, 127)
        "Coastal Flood Advisory" -> Color.rgb(124, 252, 0)
        "Lakeshore Flood Advisory" -> Color.rgb(124, 252, 0)
        "High Surf Advisory" -> Color.rgb(186, 85, 211)
        "Dense Fog Advisory" -> Color.rgb(112, 128, 144)
        "Dense Smoke Advisory" -> Color.rgb(240, 230, 140)
        "Small Craft Advisory" -> Color.rgb(216, 191, 216)
        "Brisk Wind Advisory" -> Color.rgb(216, 191, 216)
        "Hazardous Seas Warning" -> Color.rgb(216, 191, 216)
        "Dust Advisory" -> Color.rgb(189, 183, 107)
        "Blowing Dust Advisory" -> Color.rgb(189, 183, 107)
        "Lake Wind Advisory" -> Color.rgb(210, 180, 140)
        "Wind Advisory" -> Color.rgb(210, 180, 140)
        "Frost Advisory" -> Color.rgb(100, 149, 237)
        "Freezing Fog Advisory" -> Color.rgb(0, 128, 128)
        "Freezing Spray Advisory" -> Color.rgb(0, 191, 255)
        "Low Water Advisory" -> Color.rgb(165, 42, 42)
        "Local Area Emergency" -> Color.rgb(192, 192, 192)
        "Winter Storm Watch" -> Color.rgb(70, 130, 180)
        "Rip Current Statement" -> Color.rgb(64, 224, 208)
        "Beach Hazards Statement" -> Color.rgb(64, 224, 208)
        "Gale Watch" -> Color.rgb(255, 192, 203)
        "Avalanche Watch" -> Color.rgb(244, 164, 96)
        "Hazardous Seas Watch" -> Color.rgb(72, 61, 139)
        "Heavy Freezing Spray Watch" -> Color.rgb(188, 143, 143)
        "Flood Watch" -> Color.rgb(46, 139, 87)
        "Coastal Flood Watch" -> Color.rgb(102, 205, 170)
        "Lakeshore Flood Watch" -> Color.rgb(102, 205, 170)
        "High Wind Watch" -> Color.rgb(184, 134, 11)
        "Excessive Heat Watch" -> Color.rgb(128, 0, 0)
        "Extreme Cold Watch" -> Color.rgb(0, 0, 255)
        "Freeze Watch" -> Color.rgb(0, 255, 255)
        "Fire Weather Watch" -> Color.rgb(255, 222, 173)
        "Extreme Fire Danger" -> Color.rgb(233, 150, 122)
        "911 Telephone Outage" -> Color.rgb(192, 192, 192)
        "Coastal Flood Statement" -> Color.rgb(107, 142, 35)
        "Lakeshore Flood Statement" -> Color.rgb(107, 142, 35)
        "Special Weather Statement" -> Color.rgb(255, 228, 181)
        "Marine Weather Statement" -> Color.rgb(255, 239, 213)
        "Air Quality Alert" -> Color.rgb(128, 128, 128)
        "Air Stagnation Advisory" -> Color.rgb(128, 128, 128)
        "Hazardous Weather Outlook" -> Color.rgb(238, 232, 170)
        "Hydrologic Outlook" -> Color.rgb(144, 238, 144)
        "Short Term Forecast" -> Color.rgb(152, 251, 152)
        "Administrative Message" -> Color.rgb(192, 192, 192)
        "Test" -> Color.rgb(240, 255, 255)
        "Child Abduction Emergency" -> Color.rgb(255, 255, 255)
        "Blue Alert" -> Color.rgb(255, 255, 255)
        else -> null
    }
}

// Weather texts for hourly forecasts
//
// Source for strings:
// https://www.weather.gov/documentation/services-web-api
// Go to Specification > Schemas > Gridpoint > weather > values > value
//
// Source for text construction:
// https://www.weather.gov/bgm/forecast_terms
private fun getWeatherText(
    context: Context,
    weather: NwsValueWeatherValue?,
    windSpeed: Double?,
    cloudCover: Int?,
): String? {
    var weatherText: String?

    // First build the text around precipitation
    weatherText = when (weather?.weather) {
        "blowing_dust" -> context.getString(R.string.common_weather_text_dust_storm)
        "blowing_sand" -> context.getString(R.string.common_weather_text_sand_storm)
        "blowing_snow" -> context.getString(R.string.common_weather_text_blowing_snow)
        "drizzle" -> context.getString(R.string.common_weather_text_drizzle)
        "fog" -> context.getString(R.string.common_weather_text_fog)
        "freezing_drizzle" -> context.getString(R.string.common_weather_text_drizzle_freezing)
        "freezing_rain" -> context.getString(R.string.common_weather_text_rain_freezing)
        "freezing_spray" -> context.getString(R.string.nws_weather_text_freezing_spray)
        "frost" -> context.getString(R.string.common_weather_text_frost)
        "hail" -> context.getString(R.string.weather_kind_hail)
        "haze" -> context.getString(R.string.weather_kind_haze)
        "ice_crystals" -> context.getString(R.string.nws_weather_text_ice_crystals)
        "ice_fog" -> context.getString(R.string.nws_weather_text_ice_fog)
        "rain" -> context.getString(R.string.common_weather_text_rain)
        "rain_showers" -> context.getString(R.string.common_weather_text_rain_showers)
        "sleet" -> context.getString(R.string.common_weather_text_rain_snow_mixed)
        "smoke" -> context.getString(R.string.common_weather_text_smoke)
        "snow" -> context.getString(R.string.common_weather_text_snow)
        "snow_showers" -> context.getString(R.string.common_weather_text_snow_showers)
        "thunderstorms" -> context.getString(R.string.weather_kind_thunderstorm)
        "volcanic_ash" -> context.getString(R.string.nws_weather_text_volcanic_ash)
        "water_spouts" -> context.getString(R.string.nws_weather_text_waterspout)
        else -> null
    }

    if (!weatherText.isNullOrEmpty()) {
        // Qualify precipitation with intensity
        weatherText = when (weather?.intensity) {
            // Qualifying "very light" as "light" should be sufficient
            "very_light" -> context.getString(R.string.nws_weather_text_intensity_light, weatherText)
            "light" -> context.getString(R.string.nws_weather_text_intensity_light, weatherText)
            "moderate" -> weatherText // don't qualify "moderate"
            "heavy" -> context.getString(R.string.nws_weather_text_intensity_heavy, weatherText)
            else -> weatherText
        }

        // Qualify precipitation with coverage
        weatherText = when (weather?.coverage) {
            "areas" -> context.getString(R.string.nws_weather_text_coverage_areas_of, weatherText)
            "brief" -> context.getString(R.string.nws_weather_text_coverage_brief, weatherText)
            "chance" -> context.getString(R.string.nws_weather_text_coverage_chance_of, weatherText)
            "definite" -> weatherText // don't qualify "definite"
            "few" -> context.getString(R.string.nws_weather_text_coverage_few, weatherText)
            "frequent" -> context.getString(R.string.nws_weather_text_coverage_frequent, weatherText)
            "intermittent" -> context.getString(R.string.nws_weather_text_coverage_intermittent, weatherText)
            "isolated" -> context.getString(R.string.nws_weather_text_coverage_isolated, weatherText)
            "likely" -> context.getString(R.string.nws_weather_text_coverage_likely, weatherText)
            "numerous" -> context.getString(R.string.nws_weather_text_coverage_numerous, weatherText)
            "occasional" -> context.getString(R.string.nws_weather_text_coverage_occasional, weatherText)
            "patchy" -> context.getString(R.string.nws_weather_text_coverage_patchy, weatherText)
            "periods" -> context.getString(R.string.nws_weather_text_coverage_periods_of, weatherText)
            "scattered" -> context.getString(R.string.nws_weather_text_coverage_scattered, weatherText)
            "slight_chance" -> context.getString(R.string.nws_weather_text_coverage_slight_chance_of, weatherText)
            "widespread" -> context.getString(R.string.nws_weather_text_coverage_widespread, weatherText)
            else -> weatherText
        }
    } else {
        // No precipitation: describe condition as cloud cover
        if (cloudCover != null) {
            weatherText = when {
                cloudCover >= 87.5 -> context.getString(R.string.common_weather_text_cloudy)
                // NWS labels 67.5% to 87.5% "Mostly Cloudy"
                cloudCover >= 67.5 -> context.getString(R.string.common_weather_text_cloudy)
                cloudCover >= 37.5 -> context.getString(R.string.common_weather_text_partly_cloudy)
                cloudCover >= 12.5 -> context.getString(R.string.common_weather_text_mainly_clear)
                else -> context.getString(R.string.common_weather_text_clear_sky)
            }
        }
    }

    // Add wind descriptions if wind speed >= 15 mph
    // (skip if windy attributes are present)
    if (weather?.attributes.isNullOrEmpty() ||
        (!weather!!.attributes!!.contains("gusty_wind") && !weather.attributes!!.contains("damaging_wind"))
    ) {
        val mphInMetersPerSecond = 0.44704
        val windDescription: String?
        if (windSpeed != null) {
            windDescription = when {
                windSpeed >= 40.0 * mphInMetersPerSecond -> context.getString(R.string.nws_weather_text_wind_high_wind)
                windSpeed >= 30.0 * mphInMetersPerSecond -> context.getString(R.string.nws_weather_text_wind_very_windy)
                windSpeed >= 20.0 * mphInMetersPerSecond -> context.getString(R.string.nws_weather_text_wind_windy)
                windSpeed >= 15.0 * mphInMetersPerSecond -> context.getString(R.string.nws_weather_text_wind_breezy)
                else -> null
            }
            if (!windDescription.isNullOrEmpty()) {
                weatherText = if (!weatherText.isNullOrEmpty()) {
                    context.getString(R.string.nws_weather_text_condition_and_wind, weatherText, windDescription)
                } else {
                    windDescription
                }
            }
        }
    }

    // Add attributes
    var attributes = ""
    var separator: String
    weather?.attributes?.forEachIndexed { i, attr ->
        separator = when {
            i == 0 -> ""
            (i > 0) && (i < weather.attributes.size - 1) -> context.getString(R.string.comma_separator)
            else -> context.getString(R.string.nws_weather_text_separator_and)
        }
        when (attr) {
            "damaging_wind" ->
                attributes += separator + context.getString(R.string.nws_weather_text_attribute_wind_damaging)
            "dry_thunderstorms" -> attributes += separator + context.getString(R.string.weather_kind_thunder)
            "flooding" -> attributes += separator + context.getString(R.string.nws_weather_text_attribute_flooding)
            "gusty_wind" -> attributes += separator + context.getString(R.string.nws_weather_text_attribute_wind_gusty)
            "heavy_rain" -> attributes += separator + context.getString(R.string.common_weather_text_rain_heavy)
            "large_hail" -> attributes += separator + context.getString(R.string.nws_weather_text_attribute_hail_large)
            "small_hail" -> attributes += separator + context.getString(R.string.nws_weather_text_attribute_hail_small)
            "tornadoes" -> attributes += separator + context.getString(R.string.common_weather_text_tornado)
        }
    }
    if (attributes != "") {
        weatherText = if (!weatherText.isNullOrEmpty()) {
            context.getString(R.string.nws_weather_text_condition_with_attribute, weatherText, attributes)
        } else {
            attributes
        }
    }

    return weatherText
}

// Weather codes for hourly forecasts
private fun getWeatherCode(
    weather: NwsValueWeatherValue?,
    windSpeed: Double?,
    cloudCover: Int?,
): WeatherCode? {
    val mphInMetersPerSecond = 0.44704

    // Return WeatherCode for extreme conditions first
    weather?.attributes?.let {
        if (it.contains("tornadoes")) {
            return WeatherCode.WIND
        }
        if (it.contains("large_hail") || it.contains("small_hail")) {
            return WeatherCode.HAIL
        }
    }

    // No extreme conditions:
    // return WeatherCode for precipitation and fog (skip if "slight chance")
    if (!weather?.coverage.isNullOrEmpty() && weather!!.coverage != "slight_chance") {
        when (weather.weather) {
            "blowing_dust" -> return WeatherCode.WIND
            "blowing_sand" -> return WeatherCode.WIND
            "blowing_snow" -> return WeatherCode.SNOW
            "drizzle" -> return WeatherCode.RAIN
            "fog" -> return WeatherCode.FOG
            "freezing_drizzle" -> return WeatherCode.SLEET
            "freezing_rain" -> return WeatherCode.SLEET
            "freezing_spray" -> return WeatherCode.SLEET // "Freezing spray" is used in marine forecasts
            "frost" -> return WeatherCode.SNOW
            "hail" -> return WeatherCode.HAIL
            "haze" -> return WeatherCode.HAZE
            "ice_crystals" -> return WeatherCode.FOG
            "ice_fog" -> return WeatherCode.FOG
            "rain" -> return WeatherCode.RAIN
            "rain_showers" -> return WeatherCode.RAIN
            "sleet" -> return WeatherCode.SLEET
            "smoke" -> return WeatherCode.HAZE
            "snow" -> return WeatherCode.SNOW
            "snow_showers" -> return WeatherCode.SNOW
            "thunderstorms" -> return WeatherCode.THUNDERSTORM
            "volcanic_ash" -> return WeatherCode.HAZE
            "water_spouts" -> return WeatherCode.WIND
        }
    }

    // No extreme conditions or precipitation:
    // return WeatherCode for dry thunderstorms, heavy rain (?) and strong winds
    // (Not returning a code for "flooding")
    weather?.attributes?.let {
        if (it.contains("dry_thunderstorms")) {
            return WeatherCode.THUNDER
        }
        if (it.contains("heavy_rain")) {
            return WeatherCode.RAIN
        }
        if (it.contains("damaging_wind") || it.contains("gusty_wind")) {
            return WeatherCode.WIND
        }
    }
    // use 40mph as the threshold for High Wind Advisory in the U.S.
    if (windSpeed != null && windSpeed >= 40.0 * mphInMetersPerSecond) {
        return WeatherCode.WIND
    }

    // No extreme conditions, precipitation, dry thunder, and strong winds
    // return code based on cloud cover
    cloudCover?.let {
        return when {
            it >= 67.5 -> WeatherCode.CLOUDY
            it >= 37.5 -> WeatherCode.PARTLY_CLOUDY
            else -> WeatherCode.CLEAR
        }
    }

    // Nothing usable: let CommonConverter.kt deal with it
    return null
}

// Weather codes for current observations and daily forecasts
// Source: https://api.weather.gov/icons
private fun getWeatherCode(
    icon: String?,
): WeatherCode? {
    return icon?.let {
        with(it) {
            when {
                contains("tornado") -> WeatherCode.WIND
                contains("hurricane") -> WeatherCode.WIND
                contains("tropical_storm") -> WeatherCode.WIND
                contains("sleet") -> WeatherCode.SLEET // includes "rain_sleet" and "snow_sleet"
                contains("fzra") -> WeatherCode.SLEET // includes "rain_fzra" and "snow_fzra"
                contains("rain_snow") -> WeatherCode.SLEET
                contains("snow") -> WeatherCode.SNOW
                contains("blizzard") -> WeatherCode.SNOW
                contains("tsra") -> WeatherCode.THUNDERSTORM // includes "tsra_sct" and "tsra_hi"
                contains("rain") -> WeatherCode.RAIN // includes "rain_showers" and "rain_showers_hi"
                contains("dust") -> WeatherCode.HAZE
                contains("smoke") -> WeatherCode.HAZE
                contains("haze") -> WeatherCode.HAZE
                contains("fog") -> WeatherCode.FOG
                contains("wind") -> WeatherCode.WIND
                contains("bkn") -> WeatherCode.CLOUDY
                contains("ovc") -> WeatherCode.CLOUDY
                contains("few") -> WeatherCode.PARTLY_CLOUDY
                contains("sct") -> WeatherCode.CLEAR
                contains("skc") -> WeatherCode.CLEAR
                else -> null
            }
        }
    }
}
