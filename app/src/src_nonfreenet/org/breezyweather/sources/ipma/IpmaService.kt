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

package org.breezyweather.sources.ipma

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
import org.breezyweather.sources.ipma.json.IpmaAlertResult
import org.breezyweather.sources.ipma.json.IpmaDistrictResult
import org.breezyweather.sources.ipma.json.IpmaForecastResult
import org.breezyweather.sources.ipma.json.IpmaLocationResult
import retrofit2.Retrofit
import javax.inject.Inject
import javax.inject.Named

class IpmaService @Inject constructor(
    @ApplicationContext context: Context,
    @Named("JsonClient") client: Retrofit.Builder,
) : HttpSource(), WeatherSource, ReverseGeocodingSource, LocationParametersSource {

    override val id = "ipma"
    override val name = "IPMA (${context.currentLocale.getCountryName("PT")})"
    override val continent = SourceContinent.EUROPE
    override val privacyPolicyUrl by lazy {
        if (context.currentLocale.code.startsWith("pt")) {
            "https://www.ipma.pt/pt/siteinfo/index.html"
        } else {
            "https://www.ipma.pt/en/siteinfo/index.html"
        }
    }

    private val mApi by lazy {
        client
            .baseUrl(IPMA_BASE_URL)
            .build()
            .create(IpmaApi::class.java)
    }

    private val weatherAttribution = "Instituto Português do Mar e da Atmosfera"
    override val supportedFeatures = mapOf(
        SourceFeature.FORECAST to weatherAttribution,
        SourceFeature.ALERT to weatherAttribution,
        SourceFeature.REVERSE_GEOCODING to weatherAttribution
    )
    override val attributionLinks = mapOf(
        weatherAttribution to "https://www.ipma.pt/"
    )

    override fun isFeatureSupportedForLocation(
        location: Location,
        feature: SourceFeature,
    ): Boolean {
        return location.countryCode.equals("PT", ignoreCase = true)
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
        val globalIdLocal = location.parameters.getOrElse(id) { null }?.getOrElse("globalIdLocal") { null }
        val idAreaAviso = location.parameters.getOrElse(id) { null }?.getOrElse("idAreaAviso") { null }
        if (globalIdLocal.isNullOrEmpty() || idAreaAviso.isNullOrEmpty()) {
            return Observable.error(InvalidLocationException())
        }

        val failedFeatures = mutableMapOf<SourceFeature, Throwable>()
        val forecast = if (SourceFeature.FORECAST in requestedFeatures) {
            mApi.getForecast(globalIdLocal).onErrorResumeNext {
                failedFeatures[SourceFeature.FORECAST] = it
                Observable.just(emptyList())
            }
        } else {
            Observable.just(emptyList())
        }
        val alerts = if (SourceFeature.ALERT in requestedFeatures) {
            mApi.getAlerts().onErrorResumeNext {
                failedFeatures[SourceFeature.ALERT] = it
                Observable.just(IpmaAlertResult())
            }
        } else {
            Observable.just(IpmaAlertResult())
        }

        return Observable.zip(forecast, alerts) {
                forecastResult: List<IpmaForecastResult>,
                alertResult: IpmaAlertResult,
            ->
            WeatherWrapper(
                dailyForecast = if (SourceFeature.FORECAST in requestedFeatures) {
                    getDailyForecast(context, location, forecastResult.filter { it.idPeriodo == 24 })
                } else {
                    null
                },
                hourlyForecast = if (SourceFeature.FORECAST in requestedFeatures) {
                    getHourlyForecast(context, location, forecastResult.filter { it.idPeriodo != 24 })
                } else {
                    null
                },
                alertList = if (SourceFeature.ALERT in requestedFeatures) {
                    getAlertList(location, alertResult)
                } else {
                    null
                },
                failedFeatures = failedFeatures
            )
        }
    }

    // Reverse geocoding
    override fun requestNearestLocation(
        context: Context,
        latitude: Double,
        longitude: Double,
    ): Observable<List<LocationAddressInfo>> {
        val districts = mApi.getDistricts()
        val locations = mApi.getLocations()
        return Observable.zip(districts, locations) {
                districtResult: List<IpmaDistrictResult>,
                locationResult: List<IpmaLocationResult>,
            ->
            convertLocation(latitude, longitude, districtResult, locationResult)
        }
    }

    private fun convertLocation(
        latitude: Double,
        longitude: Double,
        districts: List<IpmaDistrictResult>,
        locations: List<IpmaLocationResult>,
    ): List<LocationAddressInfo> {
        val locationList = mutableListOf<LocationAddressInfo>()
        val locationMap = mutableMapOf<String, LatLng>()
        locations.mapIndexed { i, loc ->
            i.toString() to LatLng(loc.latitude.toDouble(), loc.longitude.toDouble())
        }
        LatLng(latitude, longitude).getNearestLocation(locationMap, 50000.0)?.let {
            val nearestLocation = locations[it.toInt()]
            var districtName: String? = null
            districts.forEach { d ->
                if (d.idRegiao == nearestLocation.idRegiao && d.idDistrito == nearestLocation.idDistrito) {
                    districtName = d.nome
                }
            }
            locationList.add(
                LocationAddressInfo(
                    timeZoneId = when (nearestLocation.idRegiao) {
                        2 -> "Atlantic/Madeira"
                        3 -> "Atlantic/Azores"
                        else -> "Europe/Lisbon"
                    },
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
                    city = nearestLocation.local
                )
            )
        }
        return locationList
    }

    // Location parameters
    override fun needsLocationParametersRefresh(
        location: Location,
        coordinatesChanged: Boolean,
        features: List<SourceFeature>,
    ): Boolean {
        if (coordinatesChanged) return true
        val globalIdLocal = location.parameters.getOrElse(id) { null }?.getOrElse("globalIdLocal") { null }
        val idAreaAviso = location.parameters.getOrElse(id) { null }?.getOrElse("idAreaAviso") { null }

        return globalIdLocal.isNullOrEmpty() || idAreaAviso.isNullOrEmpty()
    }

    override fun requestLocationParameters(
        context: Context,
        location: Location,
    ): Observable<Map<String, String>> {
        return mApi.getLocations().map {
            convert(location, it)
        }
    }

    override val testingLocations: List<Location> = emptyList()

    companion object {
        private const val IPMA_BASE_URL = "https://api.ipma.pt/"
    }
}
