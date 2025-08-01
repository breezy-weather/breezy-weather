package org.breezyweather.sources.fmi

import android.content.Context
import breezyweather.domain.location.model.Location
import breezyweather.domain.source.SourceContinent
import breezyweather.domain.source.SourceFeature
import breezyweather.domain.weather.wrappers.WeatherWrapper
import com.google.maps.android.model.LatLng
import com.google.maps.android.model.LatLngBounds
import dagger.hilt.android.qualifiers.ApplicationContext
import io.reactivex.rxjava3.core.Observable
import org.breezyweather.common.exceptions.InvalidLocationException
import org.breezyweather.common.exceptions.InvalidOrIncompleteDataException
import org.breezyweather.common.extensions.code
import org.breezyweather.common.extensions.currentLocale
import org.breezyweather.common.extensions.getCountryName
import org.breezyweather.common.source.HttpSource
import org.breezyweather.common.source.LocationParametersSource
import org.breezyweather.common.source.WeatherSource
import org.breezyweather.common.source.WeatherSource.Companion.PRIORITY_HIGHEST
import org.breezyweather.common.source.WeatherSource.Companion.PRIORITY_NONE
import org.breezyweather.sources.common.xml.CapAlert
import org.breezyweather.sources.fmi.xml.FmiSimpleResult
import retrofit2.Retrofit
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone
import javax.inject.Inject
import javax.inject.Named
import kotlin.time.Duration.Companion.days
import kotlin.time.Duration.Companion.hours

class FmiService @Inject constructor(
    @ApplicationContext context: Context,
    @Named("XmlClient") client: Retrofit.Builder,
) : HttpSource(), WeatherSource, LocationParametersSource {
    override val id = "fmi"
    override val name = "FMI (${context.currentLocale.getCountryName("FI")})"
    override val continent = SourceContinent.EUROPE
    override val privacyPolicyUrl by lazy {
        with(context.currentLocale.code) {
            when {
                startsWith("fi") -> "https://www.ilmatieteenlaitos.fi/tietosuoja"
                startsWith("sv") -> "https://sv.ilmatieteenlaitos.fi/dataskydd"
                else -> "https://en.ilmatieteenlaitos.fi/data-protection"
            }
        }
    }

    private val mApi by lazy {
        client
            .baseUrl(FMI_BASE_URL)
            .build()
            .create(FmiApi::class.java)
    }

    private val mAlertsApi by lazy {
        client
            .baseUrl(FMI_ALERTS_BASE_URL)
            .build()
            .create(FmiAlertsApi::class.java)
    }

    private val weatherAttribution by lazy {
        with(context.currentLocale.code) {
            when {
                startsWith("fi") -> "Ilmatieteen Laitos"
                startsWith("sv") -> "Meteorologiska Institutet"
                else -> "Finnish Meteorological Institute"
            }
        }
    }

    override val supportedFeatures = mapOf(
        SourceFeature.FORECAST to weatherAttribution,
        SourceFeature.CURRENT to weatherAttribution,
        SourceFeature.AIR_QUALITY to weatherAttribution,
        SourceFeature.ALERT to weatherAttribution,
        SourceFeature.NORMALS to weatherAttribution
    )

    override fun isFeatureSupportedForLocation(location: Location, feature: SourceFeature): Boolean {
        return when (feature) {
            SourceFeature.FORECAST -> FMI_FORECAST_BBOX.contains(LatLng(location.latitude, location.longitude))
            SourceFeature.AIR_QUALITY -> FMI_SILAM_BBOX.contains(LatLng(location.latitude, location.longitude))
            else -> arrayOf("FI", "AX").any { location.countryCode.equals(it, ignoreCase = true) }
        }
    }

    override fun getFeaturePriorityForLocation(location: Location, feature: SourceFeature): Int {
        return when {
            arrayOf("FI", "AX").any { location.countryCode.equals(it, ignoreCase = true) } -> PRIORITY_HIGHEST
            else -> PRIORITY_NONE
        }
    }

    override fun requestWeather(
        context: Context,
        location: Location,
        requestedFeatures: List<SourceFeature>,
    ): Observable<WeatherWrapper> {
        val currentStation = location.parameters.getOrElse(id) { null }?.getOrElse("currentStation") { null }
        if (arrayOf(SourceFeature.CURRENT, SourceFeature.NORMALS).any { it in requestedFeatures } &&
            currentStation.isNullOrEmpty()
        ) {
            return Observable.error(InvalidLocationException())
        }

        val hourFormatter = SimpleDateFormat("yyyy-MM-dd'T'HH':00:00Z'", Locale.ENGLISH)
        hourFormatter.timeZone = TimeZone.getTimeZone("Etc/UTC")
        val minuteFormatter = SimpleDateFormat("yyyy-MM-dd'T'HH:mm':00Z'", Locale.ENGLISH)
        minuteFormatter.timeZone = TimeZone.getTimeZone("Etc/UTC")

        val latlon = "${location.latitude},${location.longitude}"

        val failedFeatures = mutableMapOf<SourceFeature, Throwable>()
        val current = if (SourceFeature.CURRENT in requestedFeatures) {
            // limit to the last hour, otherwise the call will download 720 minutes (12 hours)
            val starttime = System.currentTimeMillis().minus(1.hours.inWholeMilliseconds)
            mApi.getCurrent(
                fmisid = currentStation!!,
                starttime = minuteFormatter.format(Date(starttime))
            ).onErrorResumeNext {
                failedFeatures[SourceFeature.CURRENT] = it
                Observable.just(FmiSimpleResult())
            }
        } else {
            Observable.just(FmiSimpleResult())
        }

        val forecast = if (SourceFeature.FORECAST in requestedFeatures) {
            // FMI has 10-day forecast, but data points on the 10th day are non-uniformly sparse
            // leading to random crashes of the app. Safer to keep to 9 days.
            val endtime = System.currentTimeMillis().plus(9.days.inWholeMilliseconds)
            mApi.getForecast(
                latlon = latlon,
                storedQueryId = "fmi::forecast::edited::weather::scandinavia::point::simple",
                endtime = hourFormatter.format(Date(endtime))
            ).onErrorResumeNext {
                failedFeatures[SourceFeature.FORECAST] = it
                Observable.just(FmiSimpleResult())
            }
        } else {
            Observable.just(FmiSimpleResult())
        }

        val airQuality = if (SourceFeature.AIR_QUALITY in requestedFeatures) {
            // This endpoint gets the pollutant concentration forecasts only.
            // There are also two endpoints for current/recent pollutant observations,
            // but more work is needed figure out which station IDs report to either endpoint.
            val endtime = System.currentTimeMillis().plus(4.days.inWholeMilliseconds)
            mApi.getForecast(
                latlon = latlon,
                storedQueryId = "fmi::forecast::silam::airquality::surface::point::simple",
                endtime = hourFormatter.format(Date(endtime))
            ).onErrorResumeNext {
                failedFeatures[SourceFeature.AIR_QUALITY] = it
                Observable.just(FmiSimpleResult())
            }
        } else {
            Observable.just(FmiSimpleResult())
        }

        val normals = if (SourceFeature.NORMALS in requestedFeatures) {
            mApi.getNormals(
                fmisid = currentStation!!
            ).onErrorResumeNext {
                failedFeatures[SourceFeature.NORMALS] = it
                Observable.just(FmiSimpleResult())
            }
        } else {
            Observable.just(FmiSimpleResult())
        }

        var someAlertsFailed = false
        val alerts = Observable.zip(
            if (SourceFeature.ALERT in requestedFeatures) {
                val alertsResult = mAlertsApi.getAlerts().execute().body()
                if (alertsResult == null) {
                    someAlertsFailed = true
                }
                alertsResult?.let { alertFeed ->
                    alertFeed.channel?.items?.filter {
                        !(it.title?.value?.startsWith("REMOVED:", ignoreCase = true) ?: false) &&
                            !it.link?.value.isNullOrEmpty()
                    }?.mapNotNull {
                        it.link?.value?.let { link ->
                            mAlertsApi.getAlert(link).onErrorResumeNext {
                                someAlertsFailed = true
                                Observable.just(CapAlert())
                            }
                        }
                    }
                } ?: emptyList()
            } else {
                emptyList()
            }
        ) {
            it
        }
        if (someAlertsFailed) {
            failedFeatures[SourceFeature.ALERT] = InvalidOrIncompleteDataException()
        }

        return Observable.zip(forecast, current, airQuality, normals, alerts) {
                forecastResult: FmiSimpleResult,
                currentResult: FmiSimpleResult,
                airQualityResult: FmiSimpleResult,
                normalsResult: FmiSimpleResult,
                alertsResult,
            ->
            WeatherWrapper(
                current = getCurrent(context, currentResult),
                dailyForecast = getDailyForecast(location, forecastResult),
                hourlyForecast = getHourlyForecast(context, forecastResult),
                airQuality = getAirQuality(airQualityResult),
                normals = getNormals(normalsResult),
                alertList = getAlertList(context, location, alertsResult.filterIsInstance<CapAlert>()),
                failedFeatures = failedFeatures
            )
        }
    }

    override fun needsLocationParametersRefresh(
        location: Location,
        coordinatesChanged: Boolean,
        features: List<SourceFeature>,
    ): Boolean {
        if (coordinatesChanged) return true

        val currentStation = location.parameters.getOrElse(id) { null }?.getOrElse("currentStation") { null }

        return (SourceFeature.CURRENT in features && currentStation.isNullOrEmpty())
    }

    override fun requestLocationParameters(
        context: Context,
        location: Location,
    ): Observable<Map<String, String>> {
        val locationParameters = mutableMapOf<String, String>()
        val posPattern = Regex("""^-?\d+(\.\d+)? -?\d+(\.\d+)?$""")
        return mApi.getStations().map { stations ->
            val currentStations = stations.members?.filter { member ->
                member.environmentalMonitoringFacility.let {
                    !(it?.identifier?.value.isNullOrEmpty()) &&
                        posPattern.matches(it.representativePoint?.point?.pos?.value ?: "") &&
                        it.belongsTo?.any { belongsTo ->
                            belongsTo.title == "Automaattinen sääasema"
                        } ?: false
                }
            } ?: emptyList()

            locationParameters["currentStation"] = convert(location, currentStations) ?: ""
            locationParameters
        }
    }

    override val testingLocations: List<Location> = emptyList()

    companion object {
        private const val FMI_BASE_URL = "https://opendata.fmi.fi/"
        private const val FMI_ALERTS_BASE_URL = "https://alerts.fmi.fi/"
        private val FMI_FORECAST_BBOX = LatLngBounds(
            LatLng(53.900000, 4.888770),
            LatLng(72.269067, 32.931465)
        )
        private val FMI_SILAM_BBOX = LatLngBounds(
            LatLng(30.050000, -24.950000),
            LatLng(72.049999, 45.049999)
        )
    }
}
