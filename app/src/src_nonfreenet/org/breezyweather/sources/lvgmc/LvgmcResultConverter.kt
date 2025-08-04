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

package org.breezyweather.sources.lvgmc

import android.content.Context
import breezyweather.domain.location.model.Location
import breezyweather.domain.weather.model.Precipitation
import breezyweather.domain.weather.model.PrecipitationProbability
import breezyweather.domain.weather.model.UV
import breezyweather.domain.weather.model.Wind
import breezyweather.domain.weather.reference.WeatherCode
import breezyweather.domain.weather.wrappers.CurrentWrapper
import breezyweather.domain.weather.wrappers.DailyWrapper
import breezyweather.domain.weather.wrappers.HalfDayWrapper
import breezyweather.domain.weather.wrappers.HourlyWrapper
import breezyweather.domain.weather.wrappers.TemperatureWrapper
import com.google.maps.android.model.LatLng
import org.breezyweather.R
import org.breezyweather.common.exceptions.InvalidLocationException
import org.breezyweather.common.extensions.code
import org.breezyweather.common.extensions.currentLocale
import org.breezyweather.sources.lvgmc.json.LvgmcAirQualityLocationResult
import org.breezyweather.sources.lvgmc.json.LvgmcCurrentLocation
import org.breezyweather.sources.lvgmc.json.LvgmcCurrentResult
import org.breezyweather.sources.lvgmc.json.LvgmcForecastResult
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone
import kotlin.time.Duration.Companion.hours

// reverse geocoding
internal fun convert(
    context: Context,
    location: Location,
    forecastLocationsResult: List<LvgmcForecastResult>,
): List<Location> {
    val locationList = mutableListOf<Location>()
    val forecastLocations = forecastLocationsResult.filter {
        it.point != null && it.latitude != null && it.longitude != null
    }.associate {
        it.point!! to LatLng(it.latitude!!.toDouble(), it.longitude!!.toDouble())
    }
    val forecastLocation = LatLng(location.latitude, location.longitude).getNearestLocation(forecastLocations)

    forecastLocationsResult.firstOrNull { it.point == forecastLocation }?.let {
        locationList.add(
            location.copy(
                latitude = location.latitude,
                longitude = location.longitude,
                timeZone = "Europe/Riga",
                country = Locale(context.currentLocale.code, "LV").displayCountry,
                countryCode = "LV",
                admin1 = it.municipality,
                city = it.name ?: ""
            )
        )
    }
    return locationList
}

// location parameters
internal fun convert(
    location: Location,
    currentLocationsResult: List<LvgmcCurrentLocation>,
    forecastLocationsResult: List<LvgmcForecastResult>,
    airQualityLocationsResult: List<LvgmcAirQualityLocationResult>,
): Map<String, String> {
    val forecastLocations = forecastLocationsResult.filter {
        it.point != null && it.latitude != null && it.longitude != null
    }.associate {
        it.point!! to LatLng(it.latitude!!.toDouble(), it.longitude!!.toDouble())
    }

    val currentLocations = currentLocationsResult.filter {
        it.code != null && it.latitude != null && it.longitude != null
    }.associate {
        it.code!! to LatLng(it.latitude!!.toDouble(), it.longitude!!.toDouble())
    }

    val airQualityLocations = airQualityLocationsResult.filter {
        it.id != null &&
            it.latitude != null &&
            it.longitude != null &&
            it.group == "Atmosfēras gaisa novērojumu stacija" &&
            it.isActive == true
    }.associate {
        it.id.toString() to LatLng(it.latitude!!, it.longitude!!)
    }

    val forecastLocation = LatLng(location.latitude, location.longitude).getNearestLocation(forecastLocations)
    val currentLocation = LatLng(location.latitude, location.longitude).getNearestLocation(currentLocations)
    val airQualityLocation = LatLng(location.latitude, location.longitude).getNearestLocation(airQualityLocations)

    if (forecastLocation.isNullOrEmpty() || currentLocation.isNullOrEmpty() || airQualityLocation.isNullOrEmpty()) {
        throw InvalidLocationException()
    }

    return mapOf(
        "forecastLocation" to forecastLocation,
        "currentLocation" to currentLocation,
        "airQualityLocation" to airQualityLocation
    )
}

internal fun getCurrent(
    location: Location,
    currentResult: List<LvgmcCurrentResult>,
): CurrentWrapper? {
    val id = "lvgmc"
    val currentLocation = location.parameters.getOrElse(id) { null }?.getOrElse("currentLocation") { null }
    if (currentLocation.isNullOrEmpty()) {
        throw InvalidLocationException()
    }

    return currentResult.filter { it.stationCode == currentLocation }
        .sortedByDescending { it.time }.firstOrNull()?.let {
            CurrentWrapper(
                temperature = TemperatureWrapper(
                    it.temperature?.toDoubleOrNull()
                ),
                wind = Wind(
                    degree = it.windDirection?.toDoubleOrNull(),
                    speed = it.windSpeed?.toDoubleOrNull(),
                    gusts = it.windGusts?.toDoubleOrNull()
                ),
                uV = UV(
                    index = it.uvIndex?.toDoubleOrNull()
                ),
                relativeHumidity = it.relativeHumidity?.toDoubleOrNull(),
                pressure = it.pressure?.toDoubleOrNull(),
                visibility = it.visibility?.toDoubleOrNull()
            )
        }
}

internal fun getDailyForecast(
    context: Context,
    dailyResult: List<LvgmcForecastResult>,
): List<DailyWrapper> {
    val dailyList = mutableListOf<DailyWrapper>()
    val formatter = SimpleDateFormat("yyyyMMdd", Locale.ENGLISH)
    formatter.timeZone = TimeZone.getTimeZone("Europe/Riga")
    val dayParts = mutableMapOf<Long, HalfDayWrapper>()
    val nightParts = mutableMapOf<Long, HalfDayWrapper>()
    val uviMap = mutableMapOf<Long, Double?>()
    var time: Long
    dailyResult.forEach {
        if (it.time != null && Regex("""^\d{12}$""").matches(it.time)) {
            if (it.time.substring(8, 10) == "00") {
                // night part of previous day:
                // Subtracting 23 hours will keep it within the previous day
                // during daylight saving time switchover
                time = formatter.parse(it.time.substring(0, 8))!!.time - 23.hours.inWholeMilliseconds
                nightParts[time] = HalfDayWrapper(
                    weatherText = getWeatherText(context, it.icon),
                    weatherCode = getWeatherCode(it.icon),
                    temperature = TemperatureWrapper(
                        temperature = it.temperature?.toDoubleOrNull()
                    ),
                    precipitation = Precipitation(
                        total = it.precipitation12h?.toDoubleOrNull()
                    ),
                    wind = Wind(
                        degree = it.windDirection?.toDoubleOrNull(),
                        speed = it.windSpeed?.toDoubleOrNull(),
                        gusts = it.windGusts?.toDoubleOrNull()
                    )
                )
            }
            if (it.time.substring(8, 10) == "12") {
                // day part of current day:
                time = formatter.parse(it.time.substring(0, 8))!!.time
                dayParts[time] = HalfDayWrapper(
                    weatherText = getWeatherText(context, it.icon),
                    weatherCode = getWeatherCode(it.icon),
                    temperature = TemperatureWrapper(
                        temperature = it.temperature?.toDoubleOrNull()
                    ),
                    precipitation = Precipitation(
                        total = it.precipitation12h?.toDoubleOrNull()
                    ),
                    wind = Wind(
                        degree = it.windDirection?.toDoubleOrNull(),
                        speed = it.windSpeed?.toDoubleOrNull(),
                        gusts = it.windGusts?.toDoubleOrNull()
                    )
                )
                uviMap[time] = it.uvIndex?.toDoubleOrNull()
            }
        }
    }
    nightParts.keys.sorted().forEach { key ->
        dailyList.add(
            DailyWrapper(
                date = Date(key),
                day = dayParts.getOrElse(key) { null },
                night = nightParts.getOrElse(key) { null },
                uV = UV(
                    index = uviMap.getOrElse(key) { null }
                )
            )
        )
    }
    if (dayParts.keys.maxOf { it } != nightParts.keys.maxOf { it }) {
        val lastKey = dayParts.keys.maxOf { it }
        dailyList.add(
            DailyWrapper(
                date = Date(lastKey),
                day = dayParts.getOrElse(lastKey) { null },
                uV = UV(
                    index = uviMap.getOrElse(lastKey) { null }
                )
            )
        )
    }
    return dailyList
}

internal fun getHourlyForecast(
    context: Context,
    hourlyResult: List<LvgmcForecastResult>,
): List<HourlyWrapper> {
    val hourlyList = mutableListOf<HourlyWrapper>()
    val formatter = SimpleDateFormat("yyyyMMddHHmm", Locale.ENGLISH)
    formatter.timeZone = TimeZone.getTimeZone("Europe/Riga")
    hourlyResult.forEach {
        if (it.time != null && Regex("""^\d{12}$""").matches(it.time)) {
            hourlyList.add(
                HourlyWrapper(
                    date = formatter.parse(it.time)!!,
                    weatherText = getWeatherText(context, it.icon),
                    weatherCode = getWeatherCode(it.icon),
                    temperature = TemperatureWrapper(
                        temperature = it.temperature?.toDoubleOrNull(),
                        feelsLike = it.apparentTemperature?.toDoubleOrNull()
                    ),
                    precipitation = Precipitation(
                        total = it.precipitation1h?.toDoubleOrNull(),
                        snow = it.snow?.toDoubleOrNull()
                    ),
                    precipitationProbability = PrecipitationProbability(
                        total = it.precipitationProbability?.toDoubleOrNull(),
                        thunderstorm = it.thunderstormProbability?.toDoubleOrNull()
                    ),
                    wind = Wind(
                        degree = it.windDirection?.toDoubleOrNull(),
                        speed = it.windSpeed?.toDoubleOrNull(),
                        gusts = it.windGusts?.toDoubleOrNull()
                    ),
                    uV = UV(
                        index = it.uvIndex?.toDoubleOrNull()
                    ),
                    relativeHumidity = it.relativeHumidity?.toDoubleOrNull(),
                    pressure = it.pressure?.toDoubleOrNull(),
                    cloudCover = it.cloudCover?.toIntOrNull()
                )
            )
        }
    }
    return hourlyList
}

private fun getWeatherText(
    context: Context,
    icon: String?,
): String? {
    return when (icon?.substring(1, 4)) {
        "101" -> context.getString(R.string.common_weather_text_clear_sky)
        "102", "103" -> context.getString(R.string.common_weather_text_partly_cloudy)
        "104" -> context.getString(R.string.common_weather_text_cloudy)
        "105" -> context.getString(R.string.common_weather_text_overcast)
        "201", "202", "203" -> context.getString(R.string.common_weather_text_drizzle_freezing)
        "204", "205", "206" -> context.getString(R.string.common_weather_text_rain_freezing)
        "207", "208" -> context.getString(R.string.common_weather_text_rain_snow_mixed)
        "301", "302", "303", "304", "305", "306" -> context.getString(R.string.weather_kind_thunderstorm)
        "307", "308", "309", "310", "311", "312" -> context.getString(R.string.weather_kind_hail)
        "313", "314", "315", "316" -> context.getString(R.string.weather_kind_thunderstorm)
        "317", "318", "319", "320", "321", "322" -> context.getString(R.string.weather_kind_hail)
        "323", "324", "325" -> context.getString(R.string.weather_kind_thunderstorm)
        "401", "402", "403", "404" -> context.getString(R.string.common_weather_text_fog)
        "405", "406" -> context.getString(R.string.common_weather_text_drizzle)
        "407", "408" -> context.getString(R.string.common_weather_text_rain)
        // TODO: Migrate string
        "409", "410", "411", "412" -> context.getString(R.string.openmeteo_weather_text_depositing_rime_fog)
        "413", "414" -> context.getString(R.string.common_weather_text_snow)
        "415", "416" -> context.getString(R.string.common_weather_text_rain_snow_mixed)
        "501", "502", "503" -> context.getString(R.string.common_weather_text_drizzle)
        "504", "505", "506" -> context.getString(R.string.common_weather_text_rain)
        "507", "508", "509" -> context.getString(R.string.common_weather_text_rain_heavy)
        "510", "511", "512" -> context.getString(R.string.common_weather_text_rain_freezing)
        "601", "602", "603", "607", "608" -> context.getString(R.string.common_weather_text_snow)
        "604", "605", "606", "609", "610" -> context.getString(R.string.common_weather_text_snow_heavy)
        "611", "612", "613" -> context.getString(R.string.common_weather_text_snow_heavy)
        "614", "615", "616" -> context.getString(R.string.common_weather_text_rain_snow_mixed)
        "617", "618", "619" -> context.getString(R.string.common_weather_text_drizzle)
        "701" -> context.getString(R.string.common_weather_text_sand_storm)
        "702" -> context.getString(R.string.weather_kind_haze)
        "703" -> context.getString(R.string.common_weather_text_squall)
        else -> null
    }
}

private fun getWeatherCode(
    icon: String?,
): WeatherCode? {
    return when (icon?.substring(1, 4)) {
        "101" -> WeatherCode.CLEAR
        "102", "103" -> WeatherCode.PARTLY_CLOUDY
        "104", "105" -> WeatherCode.CLOUDY
        "201", "202", "203" -> WeatherCode.SLEET
        "204", "205", "206" -> WeatherCode.SLEET
        "207", "208" -> WeatherCode.SLEET
        "301", "302", "303", "304", "305", "306" -> WeatherCode.THUNDERSTORM
        "307", "308", "309", "310", "311", "312" -> WeatherCode.HAIL
        "313", "314", "315", "316" -> WeatherCode.THUNDERSTORM
        "317", "318", "319", "320", "321", "322" -> WeatherCode.HAIL
        "323", "324", "325" -> WeatherCode.THUNDERSTORM
        "401", "402", "403", "404" -> WeatherCode.FOG
        "405", "406" -> WeatherCode.RAIN
        "407", "408" -> WeatherCode.RAIN
        "409", "410", "411", "412" -> WeatherCode.FOG
        "413", "414" -> WeatherCode.SNOW
        "415", "416" -> WeatherCode.SLEET
        "501", "502", "503" -> WeatherCode.RAIN
        "504", "505", "506" -> WeatherCode.RAIN
        "507", "508", "509" -> WeatherCode.RAIN
        "510", "511", "512" -> WeatherCode.SLEET
        "601", "602", "603", "607", "608" -> WeatherCode.SNOW
        "604", "605", "606", "609", "610" -> WeatherCode.SNOW
        "611", "612", "613" -> WeatherCode.SNOW
        "614", "615", "616" -> WeatherCode.SLEET
        "617", "618", "619" -> WeatherCode.RAIN
        "701" -> WeatherCode.WIND
        "702" -> WeatherCode.HAZE
        "703" -> WeatherCode.WIND
        else -> null
    }
}
