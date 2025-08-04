package org.breezyweather.sources.veduris

import android.content.Context
import breezyweather.domain.location.model.Location
import breezyweather.domain.weather.model.Alert
import breezyweather.domain.weather.model.Precipitation
import breezyweather.domain.weather.model.Wind
import breezyweather.domain.weather.reference.AlertSeverity
import breezyweather.domain.weather.reference.WeatherCode
import breezyweather.domain.weather.wrappers.CurrentWrapper
import breezyweather.domain.weather.wrappers.DailyWrapper
import breezyweather.domain.weather.wrappers.HourlyWrapper
import breezyweather.domain.weather.wrappers.TemperatureWrapper
import com.google.maps.android.PolyUtil
import com.google.maps.android.data.geojson.GeoJsonFeature
import com.google.maps.android.data.geojson.GeoJsonMultiPolygon
import com.google.maps.android.data.geojson.GeoJsonParser
import com.google.maps.android.data.geojson.GeoJsonPolygon
import com.google.maps.android.model.LatLng
import org.breezyweather.R
import org.breezyweather.common.exceptions.InvalidLocationException
import org.breezyweather.common.extensions.code
import org.breezyweather.common.extensions.currentLocale
import org.breezyweather.sources.veduris.json.VedurIsAlertRegionsResult
import org.breezyweather.sources.veduris.json.VedurIsAlertResult
import org.breezyweather.sources.veduris.json.VedurIsLatestObservation
import org.breezyweather.sources.veduris.json.VedurIsStationForecast
import org.breezyweather.sources.veduris.json.VedurIsStationResult
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.TimeZone

fun convert(
    context: Context,
    location: Location,
    stationResult: VedurIsStationResult,
): List<Location> {
    val locationList = mutableListOf<Location>()
    val stations = mutableMapOf<String, LatLng>()
    val stationNames = mutableMapOf<String, String>()
    val key = stationResult.forecasts?.keys?.first()
    stationResult.forecasts?.get(key)?.featureCollection?.features?.forEach { feature ->
        val latLng = LatLng(feature.geometry.coordinates[1], feature.geometry.coordinates[0])
        val id = feature.properties.station.id.toString()
        stations[id] = latLng
        stationNames[id] = feature.properties.station.name
    }
    val stationId = LatLng(location.latitude, location.longitude).getNearestLocation(stations)
    if (stationId == null) {
        throw InvalidLocationException()
    }
    locationList.add(
        location.copy(
            latitude = location.latitude,
            longitude = location.longitude,
            timeZone = "Atlantic/Reykjavik",
            country = Locale(context.currentLocale.code, "IS").displayCountry,
            countryCode = "IS",
            city = stationNames[stationId] ?: ""
        )
    )
    return locationList
}

fun getLocationParameters(
    location: Location,
    stationResult: VedurIsStationResult,
    alertRegionsResult: VedurIsAlertRegionsResult,
): Map<String, String> {
    val locationParameters = mutableMapOf<String, String>()
    val forecastStations = mutableMapOf<String, LatLng>()
    val currentStations = mutableMapOf<String, LatLng>()
    val key = stationResult.forecasts?.keys?.first()
    stationResult.forecasts?.get(key)?.featureCollection?.features?.forEach { feature ->
        val latLng = LatLng(feature.geometry.coordinates[1], feature.geometry.coordinates[0])
        val stationId = feature.properties.station.id.toString()
        forecastStations[stationId] = latLng
        if (!feature.properties.station.isVirtual) {
            currentStations[stationId] = latLng
        }
    }
    val forecastStationId = LatLng(location.latitude, location.longitude).getNearestLocation(forecastStations)
    val currentStationId = LatLng(location.latitude, location.longitude).getNearestLocation(currentStations)
    if (forecastStationId == null) {
        throw InvalidLocationException()
    }
    locationParameters["forecastStationId"] = forecastStationId
    if (currentStationId != null) {
        locationParameters["currentStationId"] = currentStationId
    }

    val alertRegions = getMatchingAlertRegions(location, alertRegionsResult.features).joinToString(",") {
        if (it.getProperty("id").isNullOrEmpty()) {
            throw InvalidLocationException()
        }
        it.getProperty("id")!!
    }
    locationParameters["alertRegions"] = alertRegions

    return locationParameters
}

fun getHourlyForecast(
    context: Context,
    forecast: VedurIsStationForecast?,
): List<HourlyWrapper> {
    val hourlyList = mutableListOf<HourlyWrapper>()
    val formatter = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.ENGLISH)
    formatter.timeZone = TimeZone.getTimeZone("Atlantic/Reykjavik")

    forecast?.hourlyForecasts?.forEach {
        val date = formatter.parse(it.forecastTime)
        hourlyList.add(
            HourlyWrapper(
                date = date!!,
                weatherText = getWeatherText(context, it.icon),
                weatherCode = getWeatherCode(it.icon),
                temperature = TemperatureWrapper(
                    temperature = it.temperature
                ),
                precipitation = Precipitation(
                    total = it.precipitation
                ),
                precipitationProbability = null,
                wind = Wind(
                    degree = it.windDirection,
                    speed = it.windSpeed
                ),
                relativeHumidity = if (it.humidity != 0.0) it.humidity else null
            )
        )
    }

    forecast?.dailyForecasts?.forEach { daily ->
        daily.hourlyForecasts?.forEach {
            val date = formatter.parse(it.forecastTime)
            hourlyList.add(
                HourlyWrapper(
                    date = date!!,
                    weatherText = getWeatherText(context, it.icon),
                    weatherCode = getWeatherCode(it.icon),
                    temperature = TemperatureWrapper(
                        temperature = it.temperature
                    ),
                    precipitation = Precipitation(
                        total = it.precipitation
                    ),
                    precipitationProbability = null,
                    wind = Wind(
                        degree = it.windDirection,
                        speed = it.windSpeed
                    ),
                    relativeHumidity = if (it.humidity != 0.0) it.humidity else null
                )
            )
        }
    }

    return hourlyList
}

fun getDailyForecast(
    forecast: VedurIsStationForecast?,
): List<DailyWrapper> {
    val dailyList = mutableListOf<DailyWrapper>()
    val formatter = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.ENGLISH)
    formatter.timeZone = TimeZone.getTimeZone("Atlantic/Reykjavik")

    forecast?.let {
        if (!it.hourlyForecasts.isNullOrEmpty()) {
            dailyList.add(
                DailyWrapper(
                    date = formatter.parse(forecast.hourlyForecasts!!.first().forecastDate)!!
                )
            )
        }
        forecast.dailyForecasts?.forEach {
            dailyList.add(
                DailyWrapper(
                    date = formatter.parse(it.forecastDate)!!
                )
            )
        }
    }
    return dailyList
}

fun getCurrent(
    context: Context,
    observation: VedurIsLatestObservation?,
): CurrentWrapper {
    return observation?.let {
        CurrentWrapper(
            weatherText = getWeatherText(context, it.icon),
            weatherCode = getWeatherCode(it.icon),
            temperature = TemperatureWrapper(
                temperature = it.temperature
            ),
            wind = Wind(
                degree = it.windDirection,
                speed = it.windSpeed,
                gusts = it.maxWindGust
            ),
            relativeHumidity = if (it.humidity != 0.0) it.humidity else null,
            dewPoint = it.dewPoint,
            pressure = it.pressure,
            cloudCover = it.cloudCover?.toInt()
        )
    } ?: CurrentWrapper()
}

fun getAlertList(
    context: Context,
    location: Location,
    alertResult: VedurIsAlertResult,
): List<Alert> {
    val alertList = mutableListOf<Alert>()
    val id = "veduris"
    val alertRegions = location.parameters.getOrElse(id) { null }?.getOrElse("alertRegions") { null }?.split(",")
    alertRegions?.forEach { regionId ->
        alertResult.alertsByArea?.getOrElse(regionId) { null }?.forEach {
            val severity = with(it.impact) {
                when {
                    equals("extreme", ignoreCase = true) -> AlertSeverity.EXTREME
                    equals("severe", ignoreCase = true) -> AlertSeverity.SEVERE
                    equals("moderate", ignoreCase = true) -> AlertSeverity.MODERATE
                    equals("minor", ignoreCase = true) -> AlertSeverity.MINOR
                    else -> AlertSeverity.UNKNOWN
                }
            }
            val color = Alert.colorFromSeverity(severity)
            alertList.add(
                Alert(
                    alertId = it.identifier,
                    startDate = it.startsAt,
                    endDate = it.endsAt,
                    headline = it.headline,
                    description = it.description,
                    source = if (context.currentLocale.code.startsWith("is")) {
                        "Veðurstofa Íslands"
                    } else {
                        "Icelandic Met Office"
                    },
                    severity = severity,
                    color = color
                )
            )
        }
    }
    return alertList
}

private fun getWeatherText(
    context: Context,
    icon: String?,
): String? {
    return when (icon) {
        "ClearSky", "ClearSkyNight" -> context.getString(R.string.common_weather_text_clear_sky)
        "Cloudy", "CloudyNight" -> context.getString(R.string.common_weather_text_cloudy)
        "PartlyCloudy", "PartlyCloudyNight" -> context.getString(R.string.common_weather_text_partly_cloudy)
        "LightSnow" -> context.getString(R.string.common_weather_text_snow_light)
        "Snow" -> context.getString(R.string.common_weather_text_snow)
        "Overcast" -> context.getString(R.string.common_weather_text_overcast)
        "LightRain" -> context.getString(R.string.common_weather_text_rain_light)
        "Rain" -> context.getString(R.string.common_weather_text_rain)
        "LightSleet" -> context.getString(R.string.common_weather_text_rain_snow_mixed_light)
        "Sleet" -> context.getString(R.string.common_weather_text_rain_snow_mixed)
        "LightRainShowers", "LightRainShowersNight" -> context.getString(
            R.string.common_weather_text_rain_showers_light
        )
        "RainShowers", "RainShowersNight" -> context.getString(R.string.common_weather_text_rain_showers)
        "SleetShowers", "SleetShowersNight" -> context.getString(R.string.common_weather_text_rain_snow_mixed_showers)
        "SnowShowers", "SnowShowersNight" -> context.getString(R.string.common_weather_text_snow_showers)
        // "DustDevil" -> "Dust devil" // not used in site explanations
        // "Storm" -> "Storm" // unsure about meaning: not used in site explanations
        "DustStorm" -> context.getString(R.string.common_weather_text_dust_storm) + " / " +
            context.getString(R.string.common_weather_text_sand_storm) // original: Dust storm / sand storm
        "BlowingSnow" -> context.getString(R.string.common_weather_text_blowing_snow)
        "Fog" -> context.getString(R.string.common_weather_text_fog)
        "FogDrizzle" -> context.getString(R.string.veduris_weather_text_fog_drizzle)
        "FogMist" -> context.getString(R.string.common_weather_text_mist)
        "LightDrizzle" -> context.getString(R.string.common_weather_text_drizzle_light)
        "Drizzle" -> context.getString(R.string.common_weather_text_drizzle)
        "FreezingRain" -> context.getString(R.string.common_weather_text_rain_freezing)
        "Hail", "HailNight" -> context.getString(R.string.weather_kind_hail)
        "LightThunder" -> context.getString(R.string.veduris_weather_text_thunderstorm_light)
        "Thunder" -> context.getString(R.string.weather_kind_thunderstorm)
        else -> null
    }
}

private fun getWeatherCode(
    icon: String?,
): WeatherCode? {
    return when (icon) {
        "ClearSky", "ClearSkyNight" -> WeatherCode.CLEAR
        "Cloudy", "CloudyNight", "Overcast" -> WeatherCode.CLOUDY
        "PartlyCloudy", "PartlyCloudyNight" -> WeatherCode.PARTLY_CLOUDY
        "LightSnow", "Snow", "SnowShowers", "SnowShowersNight", "BlowingSnow" -> WeatherCode.SNOW
        "LightRain", "Rain", "LightRainShowers", "LightRainShowersNight",
        "RainShowers", "RainShowersNight", "LightDrizzle", "Drizzle",
        -> WeatherCode.RAIN
        "LightSleet", "Sleet", "SleetShowers", "SleetShowersNight", "FreezingRain" -> WeatherCode.SLEET
        "DustDevil", "Storm", "DustStorm" -> WeatherCode.WIND
        "Fog", "FogDrizzle", "FogMist" -> WeatherCode.FOG
        "Hail", "HailNight" -> WeatherCode.HAIL
        "LightThunder", "Thunder" -> WeatherCode.THUNDERSTORM
        else -> null
    }
}

private fun getMatchingAlertRegions(
    location: Location,
    features: List<Any?>,
): List<GeoJsonFeature> {
    val json = """{"type":"FeatureCollection","features":[${features.joinToString(",")}]}"""
    val geoJsonParser = GeoJsonParser(JSONObject(json))
    return geoJsonParser.features.filter { feature ->
        when (feature.geometry) {
            is GeoJsonPolygon -> (feature.geometry as GeoJsonPolygon).coordinates.any { polygon ->
                PolyUtil.containsLocation(location.latitude, location.longitude, polygon, true)
            }
            is GeoJsonMultiPolygon -> (feature.geometry as GeoJsonMultiPolygon).polygons.any {
                it.coordinates.any { polygon ->
                    PolyUtil.containsLocation(location.latitude, location.longitude, polygon, true)
                }
            }
            else -> false
        }
    }
}
