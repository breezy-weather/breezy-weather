package org.breezyweather.sources.ipma

import android.content.Context
import breezyweather.domain.feature.SourceFeature
import breezyweather.domain.location.model.Location
import breezyweather.domain.weather.model.Alert
import breezyweather.domain.weather.model.AlertSeverity
import breezyweather.domain.weather.model.Daily
import breezyweather.domain.weather.model.HalfDay
import breezyweather.domain.weather.model.PrecipitationProbability
import breezyweather.domain.weather.model.Temperature
import breezyweather.domain.weather.model.UV
import breezyweather.domain.weather.model.WeatherCode
import breezyweather.domain.weather.model.Wind
import breezyweather.domain.weather.wrappers.HourlyWrapper
import breezyweather.domain.weather.wrappers.SecondaryWeatherWrapper
import breezyweather.domain.weather.wrappers.WeatherWrapper
import com.google.maps.android.SphericalUtil
import com.google.maps.android.model.LatLng
import org.breezyweather.R
import org.breezyweather.common.exceptions.InvalidLocationException
import org.breezyweather.sources.getWindDegree
import org.breezyweather.sources.ipma.json.IpmaAlertResult
import org.breezyweather.sources.ipma.json.IpmaDistrictResult
import org.breezyweather.sources.ipma.json.IpmaForecastResult
import org.breezyweather.sources.ipma.json.IpmaLocationResult
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.TimeZone

fun convert(
    location: Location,
    districts: List<IpmaDistrictResult>,
    locations: List<IpmaLocationResult>,
): List<Location> {
    val locationList = mutableListOf<Location>()
    val nearestLocation = getNearestLocation(location, locations)
    var districtName: String? = null
    districts.forEach {
        if (it.idRegiao == nearestLocation.idRegiao && it.idDistrito == nearestLocation.idDistrito) {
            districtName = it.nome
        }
    }
    locationList.add(
        location.copy(
            latitude = location.latitude,
            longitude = location.longitude,
            timeZone = when (nearestLocation.idRegiao) {
                2 -> "Atlantic/Madeira"
                3 -> "Atlantic/Azores"
                else -> "Europe/Lisbon"
            },
            country = "Portugal",
            countryCode = "PT",
            admin1 = when (nearestLocation.idRegiao) {
                2 -> "Madeira"
                3 -> "Azores"
                else -> districtName
            },
            admin2 = when (nearestLocation.idRegiao) {
                2, 3 -> districtName
                else -> null
            },
            city = nearestLocation.local ?: ""
        )
    )
    return locationList
}

fun convert(
    location: Location,
    locations: List<IpmaLocationResult>,
): Map<String, String> {
    val nearestLocation = getNearestLocation(location, locations)
    return mapOf(
        "globalIdLocal" to nearestLocation.globalIdLocal.toString(),
        "idAreaAviso" to nearestLocation.idAreaAviso.toString()
    )
}

fun convert(
    context: Context,
    location: Location,
    forecastResult: List<IpmaForecastResult>,
    alertResult: IpmaAlertResult,
    failedFeatures: List<SourceFeature>,
): WeatherWrapper {
    return WeatherWrapper(
        dailyForecast = getDailyForecast(context, location, forecastResult),
        hourlyForecast = getHourlyForecast(context, location, forecastResult),
        alertList = getAlertList(location, alertResult),
        failedFeatures = failedFeatures
    )
}

fun convertSecondary(
    location: Location,
    alertResult: IpmaAlertResult?,
): SecondaryWeatherWrapper {
    return SecondaryWeatherWrapper(
        alertList = alertResult?.let { getAlertList(location, it) }
    )
}

private fun getDailyForecast(
    context: Context,
    location: Location,
    forecastResult: List<IpmaForecastResult>,
): List<Daily> {
    val formatter = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.ENGLISH)
    formatter.timeZone = TimeZone.getTimeZone(location.timeZone)
    val dailyList = mutableListOf<Daily>()
    forecastResult.forEach {
        if (it.idPeriodo == 24) {
            dailyList.add(
                Daily(
                    date = formatter.parse(it.dataPrev)!!,
                    day = HalfDay(
                        weatherText = getWeatherText(context, it.idTipoTempo),
                        weatherCode = getWeatherCode(it.idTipoTempo),
                        temperature = Temperature(
                            temperature = it.tMax?.toDoubleOrNull()
                        ),
                        precipitationProbability = PrecipitationProbability(
                            total = it.probabilidadePrecipita?.toDoubleOrNull()
                        ),
                        wind = Wind(
                            degree = getWindDegree(it.ddVento)
                        )
                    ),
                    night = HalfDay(
                        weatherText = getWeatherText(context, it.idTipoTempo),
                        weatherCode = getWeatherCode(it.idTipoTempo),
                        temperature = Temperature(
                            temperature = it.tMin?.toDoubleOrNull()
                        ),
                        precipitationProbability = PrecipitationProbability(
                            total = it.probabilidadePrecipita?.toDoubleOrNull()
                        ),
                        wind = Wind(
                            degree = getWindDegree(it.ddVento)
                        )
                    ),
                    uV = UV(
                        index = it.iUv?.toDoubleOrNull()
                    )
                )
            )
        }
    }
    return dailyList
}

private fun getHourlyForecast(
    context: Context,
    location: Location,
    forecastResult: List<IpmaForecastResult>,
): List<HourlyWrapper> {
    val formatter = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.ENGLISH)
    formatter.timeZone = TimeZone.getTimeZone(location.timeZone)
    val hourlyList = mutableListOf<HourlyWrapper>()
    var lastPrecipitationProbability: Double? = null
    forecastResult.forEach {
        if (it.idPeriodo != 24) {
            hourlyList.add(
                HourlyWrapper(
                    date = formatter.parse(it.dataPrev)!!,
                    weatherText = getWeatherText(context, it.idTipoTempo),
                    weatherCode = getWeatherCode(it.idTipoTempo),
                    temperature = Temperature(
                        temperature = it.tMed?.toDoubleOrNull(),
                        apparentTemperature = it.utci?.toDoubleOrNull()
                    ),
                    precipitationProbability = PrecipitationProbability(
                        total = if (it.probabilidadePrecipita != "-99.0") {
                            it.probabilidadePrecipita?.toDoubleOrNull()
                        } else {
                            lastPrecipitationProbability
                        }
                    ),
                    wind = Wind(
                        degree = getWindDegree(it.ddVento),
                        speed = it.ffVento?.toDoubleOrNull()?.div(3.6)
                    ),
                    relativeHumidity = it.hR?.toDoubleOrNull()
                )
            )
            if (it.probabilidadePrecipita != "-99.0") {
                lastPrecipitationProbability = it.probabilidadePrecipita?.toDoubleOrNull()
            }
        }
    }
    return hourlyList
}

private fun getAlertList(
    location: Location,
    alertResult: IpmaAlertResult,
): List<Alert> {
    val formatter = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.ENGLISH)
    formatter.timeZone = TimeZone.getTimeZone(location.timeZone)
    val alertList = mutableListOf<Alert>()
    alertResult.data?.forEach {
        val id = "ipma"
        var severity: AlertSeverity
        if (it.idAreaAviso == location.parameters.getOrElse(id) { null }?.getOrElse("idAreaAviso") { null }) {
            if (it.awarenessLevelID != "green") {
                severity = when (it.awarenessLevelID) {
                    "yellow" -> AlertSeverity.MODERATE
                    "orange" -> AlertSeverity.SEVERE
                    "red" -> AlertSeverity.EXTREME
                    else -> AlertSeverity.UNKNOWN
                }
                alertList.add(
                    Alert(
                        alertId = "${it.awarenessTypeName} ${it.startTime}",
                        startDate = formatter.parse(it.startTime)!!,
                        endDate = formatter.parse(it.endTime)!!,
                        headline = it.awarenessTypeName,
                        description = it.text,
                        source = "Instituto Português do Mar e da Atmosfera",
                        severity = severity,
                        color = Alert.colorFromSeverity(severity)
                    )
                )
            }
        }
    }
    return alertList
}

private fun getNearestLocation(
    location: Location,
    locations: List<IpmaLocationResult>,
): IpmaLocationResult {
    var nearestDistance = Double.POSITIVE_INFINITY
    var nearestLocation = Int.MAX_VALUE
    var distance = Double.POSITIVE_INFINITY
    locations.forEachIndexed { i, loc ->
        distance = SphericalUtil.computeDistanceBetween(
            LatLng(loc.latitude.toDouble(), loc.longitude.toDouble()),
            LatLng(location.latitude, location.longitude)
        )
        if (distance < nearestDistance) {
            nearestDistance = distance
            nearestLocation = i
        }
    }
    // Make sure location is within 50km of a known location in Portugal
    if (nearestDistance > 50000) {
        throw InvalidLocationException()
    }
    return locations[nearestLocation]
}

// Source: https://www.ipma.pt/opencms/bin/file.data/weathertypes.json
private fun getWeatherText(
    context: Context,
    code: Int?,
): String? {
    return when (code) {
        1 -> context.getString(R.string.common_weather_text_clear_sky)
        2, 3, 25 -> context.getString(R.string.common_weather_text_partly_cloudy)
        4, 5, 24, 27 -> context.getString(R.string.common_weather_text_cloudy)
        6, 9 -> context.getString(R.string.common_weather_text_rain_showers)
        7 -> context.getString(R.string.common_weather_text_rain_showers_light)
        8, 11 -> context.getString(R.string.common_weather_text_rain_showers_heavy)
        10, 13 -> context.getString(R.string.common_weather_text_rain_light)
        12 -> context.getString(R.string.common_weather_text_rain)
        14 -> context.getString(R.string.common_weather_text_rain_heavy)
        15 -> context.getString(R.string.common_weather_text_drizzle)
        16 -> context.getString(R.string.common_weather_text_mist)
        17, 26 -> context.getString(R.string.common_weather_text_fog)
        18 -> context.getString(R.string.common_weather_text_snow)
        19, 20, 23 -> context.getString(R.string.weather_kind_thunderstorm)
        21 -> context.getString(R.string.weather_kind_hail)
        22 -> context.getString(R.string.common_weather_text_frost)
        28 -> context.getString(R.string.common_weather_text_snow_showers)
        29, 30 -> context.getString(R.string.common_weather_text_rain_snow_mixed)
        else -> null
    }
}

private fun getWeatherCode(
    code: Int?,
): WeatherCode? {
    return when (code) {
        1 -> WeatherCode.CLEAR
        2, 3, 25 -> WeatherCode.PARTLY_CLOUDY
        4, 5, 24, 27 -> WeatherCode.CLOUDY
        6, 7, 8, 9, 10, 11, 12, 13, 14, 15 -> WeatherCode.RAIN
        16, 17, 26 -> WeatherCode.FOG
        18, 22, 28 -> WeatherCode.SNOW
        19, 20, 23 -> WeatherCode.THUNDERSTORM
        21 -> WeatherCode.HAIL
        29, 30 -> WeatherCode.SLEET
        else -> null
    }
}
