package org.breezyweather.sources.veduris

import android.content.Context
import breezyweather.domain.location.model.Location
import breezyweather.domain.location.model.LocationAddressInfo
import breezyweather.domain.source.SourceContinent
import breezyweather.domain.source.SourceFeature
import breezyweather.domain.weather.model.Alert
import breezyweather.domain.weather.model.Precipitation
import breezyweather.domain.weather.model.Wind
import breezyweather.domain.weather.reference.AlertSeverity
import breezyweather.domain.weather.reference.WeatherCode
import breezyweather.domain.weather.wrappers.CurrentWrapper
import breezyweather.domain.weather.wrappers.DailyWrapper
import breezyweather.domain.weather.wrappers.HourlyWrapper
import breezyweather.domain.weather.wrappers.TemperatureWrapper
import breezyweather.domain.weather.wrappers.WeatherWrapper
import com.google.maps.android.PolyUtil
import com.google.maps.android.data.geojson.GeoJsonFeature
import com.google.maps.android.data.geojson.GeoJsonMultiPolygon
import com.google.maps.android.data.geojson.GeoJsonParser
import com.google.maps.android.data.geojson.GeoJsonPolygon
import com.google.maps.android.model.LatLng
import dagger.hilt.android.qualifiers.ApplicationContext
import io.reactivex.rxjava3.core.Observable
import org.breezyweather.R
import org.breezyweather.common.exceptions.InvalidLocationException
import org.breezyweather.common.extensions.code
import org.breezyweather.common.extensions.currentLocale
import org.breezyweather.common.extensions.getCountryName
import org.breezyweather.common.source.HttpSource
import org.breezyweather.common.source.LocationParametersSource
import org.breezyweather.common.source.ReverseGeocodingSource
import org.breezyweather.common.source.WeatherSource
import org.breezyweather.common.source.WeatherSource.Companion.PRIORITY_HIGHEST
import org.breezyweather.common.source.WeatherSource.Companion.PRIORITY_NONE
import org.breezyweather.sources.veduris.json.VedurIsAlertRegionsResult
import org.breezyweather.sources.veduris.json.VedurIsAlertResult
import org.breezyweather.sources.veduris.json.VedurIsLatestObservation
import org.breezyweather.sources.veduris.json.VedurIsResult
import org.breezyweather.sources.veduris.json.VedurIsStationForecast
import org.breezyweather.sources.veduris.json.VedurIsStationResult
import org.breezyweather.unit.pressure.Pressure.Companion.hectopascals
import org.json.JSONObject
import retrofit2.Retrofit
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.TimeZone
import javax.inject.Inject
import javax.inject.Named
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.max
import kotlin.math.min

class VedurIsService @Inject constructor(
    @ApplicationContext context: Context,
    @Named("JsonClient") client: Retrofit.Builder,
) : HttpSource(), WeatherSource, ReverseGeocodingSource, LocationParametersSource {
    override val id = "veduris"
    private val countryName = context.currentLocale.getCountryName("IS")
    override val name by lazy {
        if (context.currentLocale.code.startsWith("is")) {
            "Veðurstofa Íslands"
        } else {
            "Icelandic Met Office"
        }.let {
            if (it.contains(countryName)) {
                it
            } else {
                "$it ($countryName)"
            }
        }
    }
    override val continent = SourceContinent.EUROPE
    override val privacyPolicyUrl = "https://vedur.is/um-vi/vefurinn/personuvernd/"

    private val mApi by lazy {
        client
            .baseUrl(VEDUR_IS_BASE_URL)
            .build()
            .create(VedurIsApi::class.java)
    }

    private val weatherAttribution by lazy {
        if (context.currentLocale.code.startsWith("is")) {
            "Veðurstofa Íslands"
        } else {
            "Icelandic Met Office"
        }
    }
    override val supportedFeatures = mapOf(
        SourceFeature.FORECAST to weatherAttribution,
        SourceFeature.CURRENT to weatherAttribution,
        SourceFeature.ALERT to weatherAttribution,
        SourceFeature.REVERSE_GEOCODING to weatherAttribution
    )
    override val attributionLinks = mapOf(
        weatherAttribution to "https://gottvedur.is/"
    )

    override fun isFeatureSupportedForLocation(
        location: Location,
        feature: SourceFeature,
    ): Boolean {
        return location.countryCode.equals("IS", ignoreCase = true)
    }

    override fun getFeaturePriorityForLocation(
        location: Location,
        feature: SourceFeature,
    ): Int {
        return when {
            isFeatureSupportedForLocation(location, feature) -> PRIORITY_HIGHEST
            else -> PRIORITY_NONE
        }
    }

    override fun requestWeather(
        context: Context,
        location: Location,
        requestedFeatures: List<SourceFeature>,
    ): Observable<WeatherWrapper> {
        val forecastStationId = location.parameters.getOrElse(id) { null }?.getOrElse("forecastStationId") { null }
        val currentStationId = location.parameters.getOrElse(id) { null }?.getOrElse("currentStationId") { null }
        val alertRegions = location.parameters.getOrElse(id) { null }?.getOrElse("alertRegions") { null }
        if (forecastStationId.isNullOrEmpty() || alertRegions.isNullOrEmpty()) {
            return Observable.error(InvalidLocationException())
        }
        val failedFeatures = mutableMapOf<SourceFeature, Throwable>()

        val forecast = if (SourceFeature.FORECAST in requestedFeatures) {
            mApi.getForecast(forecastStationId).onErrorResumeNext {
                failedFeatures[SourceFeature.FORECAST] = it
                Observable.just(VedurIsResult())
            }
        } else {
            Observable.just(VedurIsResult())
        }

        val current = if (SourceFeature.CURRENT in requestedFeatures && !currentStationId.isNullOrEmpty()) {
            mApi.getCurrent(currentStationId).onErrorResumeNext {
                failedFeatures[SourceFeature.CURRENT] = it
                Observable.just(VedurIsResult())
            }
        } else {
            Observable.just(VedurIsResult())
        }

        val alert = if (SourceFeature.ALERT in requestedFeatures) {
            mApi.getAlerts(
                if (context.currentLocale.code.startsWith("is")) "is" else "en"
            ).onErrorResumeNext {
                failedFeatures[SourceFeature.ALERT] = it
                Observable.just(VedurIsAlertResult())
            }
        } else {
            Observable.just(VedurIsAlertResult())
        }

        return Observable.zip(forecast, current, alert) {
                forecastResult: VedurIsResult,
                currentResult: VedurIsResult,
                alertResult: VedurIsAlertResult,
            ->
            WeatherWrapper(
                dailyForecast = getDailyForecast(forecastResult.pageProps?.stationForecast),
                hourlyForecast = getHourlyForecast(context, forecastResult.pageProps?.stationForecast),
                current = getCurrent(context, currentResult.pageProps?.latestObservation),
                alertList = getAlertList(context, location, alertResult),
                failedFeatures = failedFeatures
            )
        }
    }

    private fun getHourlyForecast(
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

    private fun getDailyForecast(
        forecast: VedurIsStationForecast?,
    ): List<DailyWrapper> {
        val dailyList = mutableListOf<DailyWrapper>()
        val formatter = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.ENGLISH)
        formatter.timeZone = TimeZone.getTimeZone("Atlantic/Reykjavik")

        forecast?.let { fcst ->
            if (!fcst.hourlyForecasts.isNullOrEmpty()) {
                dailyList.add(
                    DailyWrapper(
                        date = formatter.parse(fcst.hourlyForecasts.first().forecastDate)!!
                    )
                )
            }
            fcst.dailyForecasts?.forEach {
                dailyList.add(
                    DailyWrapper(
                        date = formatter.parse(it.forecastDate)!!
                    )
                )
            }
        }
        return dailyList
    }

    private fun getCurrent(
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
                pressure = it.pressure?.hectopascals,
                cloudCover = it.cloudCover?.toInt()
            )
        } ?: CurrentWrapper()
    }

    private fun getAlertList(
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
            "SleetShowers", "SleetShowersNight" ->
                context.getString(R.string.common_weather_text_rain_snow_mixed_showers)
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
            "LightRain", "Rain", "LightRainShowers", "LightRainShowersNight", "RainShowers", "RainShowersNight",
            "LightDrizzle", "Drizzle",
            -> WeatherCode.RAIN
            "LightSleet", "Sleet", "SleetShowers", "SleetShowersNight", "FreezingRain" -> WeatherCode.SLEET
            "DustDevil", "Storm", "DustStorm" -> WeatherCode.WIND
            "Fog", "FogDrizzle", "FogMist" -> WeatherCode.FOG
            "Hail", "HailNight" -> WeatherCode.HAIL
            "LightThunder", "Thunder" -> WeatherCode.THUNDERSTORM
            else -> null
        }
    }

    override fun requestNearestLocation(
        context: Context,
        latitude: Double,
        longitude: Double,
    ): Observable<List<LocationAddressInfo>> {
        val bbox = getBbox(latitude, longitude, DISTANCE_LIMIT)
        return mApi.getStations(bbox[0].longitude, bbox[1].longitude, bbox[0].latitude, bbox[1].latitude).map {
            convertLocation(latitude, longitude, it)
        }
    }

    private fun convertLocation(
        latitude: Double,
        longitude: Double,
        stationResult: VedurIsStationResult,
    ): List<LocationAddressInfo> {
        val stations = mutableMapOf<String, LatLng>()
        val stationNames = mutableMapOf<String, String>()
        val key = stationResult.forecasts?.keys?.first()
        stationResult.forecasts?.get(key)?.featureCollection?.features?.forEach { feature ->
            val latLng = LatLng(feature.geometry.coordinates[1], feature.geometry.coordinates[0])
            val id = feature.properties.station.id.toString()
            stations[id] = latLng
            stationNames[id] = feature.properties.station.name
        }
        val stationId = LatLng(latitude, longitude).getNearestLocation(stations)
        if (stationId == null) {
            throw InvalidLocationException()
        }
        return listOf(
            LocationAddressInfo(
                timeZoneId = "Atlantic/Reykjavik",
                countryCode = "IS",
                city = stationNames[stationId]
            )
        )
    }

    override fun needsLocationParametersRefresh(
        location: Location,
        coordinatesChanged: Boolean,
        features: List<SourceFeature>,
    ): Boolean {
        if (coordinatesChanged) return true

        // Not all locations have an observation station for current, so don't check for it.
        // If there's no current station, we can fall back to nearest forecast location instead.
        val forecastStationId = location.parameters.getOrElse(id) { null }?.getOrElse("forecastStationId") { null }
        val alertRegions = location.parameters.getOrElse(id) { null }?.getOrElse("alertRegion") { null }

        return forecastStationId.isNullOrEmpty() || alertRegions.isNullOrEmpty()
    }

    override fun requestLocationParameters(context: Context, location: Location): Observable<Map<String, String>> {
        val bbox = getBbox(location.latitude, location.longitude, DISTANCE_LIMIT)
        val alertRegions = mApi.getAlertRegions(
            if (context.currentLocale.code.startsWith("is")) "is" else "en"
        )
        val stations = mApi.getStations(bbox[0].longitude, bbox[1].longitude, bbox[0].latitude, bbox[1].latitude)
        return Observable.zip(stations, alertRegions) {
                stationResult: VedurIsStationResult,
                alertRegionsResult: VedurIsAlertRegionsResult,
            ->
            getLocationParameters(location, stationResult, alertRegionsResult)
        }
    }

    private fun getLocationParameters(
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

    private fun getBbox(
        latitude: Double,
        longitude: Double,
        distance: Double,
    ): List<LatLng> {
        val north = min(latitude + distance / (2 * PI * EARTH_POLAR_RADIUS) * 360, 90.0)
        val south = max(latitude - distance / (2 * PI * EARTH_POLAR_RADIUS) * 360, -90.0)
        val multiple = cos(latitude / 180 * PI)
        val east = if (multiple != 0.0) {
            min(longitude + distance / (2 * PI * EARTH_EQUATORIAL_RADIUS * multiple) * 360, 180.0)
        } else {
            180.0
        }
        val west = if (multiple != 0.0) {
            max(longitude - distance / (2 * PI * EARTH_EQUATORIAL_RADIUS * multiple) * 360, -180.0)
        } else {
            -180.0
        }
        return listOf(
            LatLng(south, west),
            LatLng(north, east)
        )
    }

    override val testingLocations: List<Location> = emptyList()

    companion object {
        private const val VEDUR_IS_BASE_URL = "https://gottvedur.is/"
        private const val EARTH_POLAR_RADIUS = 6356752
        private const val EARTH_EQUATORIAL_RADIUS = 6378137
        private const val DISTANCE_LIMIT = 60000.0
    }
}
