package org.breezyweather.sources.veduris

import android.content.Context
import breezyweather.domain.location.model.Location
import breezyweather.domain.location.model.LocationAddressInfo
import breezyweather.domain.source.SourceContinent
import breezyweather.domain.source.SourceFeature
import breezyweather.domain.weather.wrappers.WeatherWrapper
import com.google.maps.android.model.LatLng
import dagger.hilt.android.qualifiers.ApplicationContext
import io.reactivex.rxjava3.core.Observable
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
import org.breezyweather.sources.veduris.json.VedurIsResult
import org.breezyweather.sources.veduris.json.VedurIsStationResult
import retrofit2.Retrofit
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

    override fun requestNearestLocation(
        context: Context,
        latitude: Double,
        longitude: Double,
    ): Observable<List<LocationAddressInfo>> {
        val bbox = getBbox(latitude, longitude, DISTANCE_LIMIT)
        val stations = mApi.getStations(bbox[0].longitude, bbox[1].longitude, bbox[0].latitude, bbox[1].latitude)
        return stations.map {
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
