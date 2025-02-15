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
import breezyweather.domain.location.model.Location
import breezyweather.domain.source.SourceContinent
import breezyweather.domain.source.SourceFeature
import breezyweather.domain.weather.wrappers.WeatherWrapper
import com.google.maps.android.SphericalUtil
import com.google.maps.android.model.LatLng
import dagger.hilt.android.qualifiers.ApplicationContext
import io.reactivex.rxjava3.core.Observable
import org.breezyweather.BreezyWeather
import org.breezyweather.common.exceptions.InvalidLocationException
import org.breezyweather.common.exceptions.ReverseGeocodingException
import org.breezyweather.common.extensions.code
import org.breezyweather.common.extensions.currentLocale
import org.breezyweather.common.source.HttpSource
import org.breezyweather.common.source.LocationParametersSource
import org.breezyweather.common.source.ReverseGeocodingSource
import org.breezyweather.common.source.WeatherSource
import org.breezyweather.common.utils.helpers.LogHelper
import retrofit2.Retrofit
import java.util.Locale
import javax.inject.Inject
import javax.inject.Named

/**
 * Israel Meteorological Service
 */
class ImsService @Inject constructor(
    @ApplicationContext context: Context,
    @Named("JsonClient") client: Retrofit.Builder,
) : HttpSource(), WeatherSource, ReverseGeocodingSource, LocationParametersSource {

    override val id = "ims"
    override val name by lazy {
        with(context.currentLocale.code) {
            when {
                startsWith("ar") -> "خدمة الأرصاد الجوية الإسرائيلية"
                startsWith("he") || startsWith("iw") -> "השירות המטאורולוגי הישראלי"
                else -> "IMS (${Locale(context.currentLocale.code, "IL").displayCountry})"
            }
        }
    }
    override val continent = SourceContinent.ASIA
    override val privacyPolicyUrl by lazy {
        with(context.currentLocale.code) {
            when {
                startsWith("he") || startsWith("iw") -> "https://ims.gov.il/he/termOfuse"
                else -> "https://ims.gov.il/en/termOfuse"
            }
        }
    }

    private val mApi by lazy {
        client
            .baseUrl(IMS_BASE_URL)
            .build()
            .create(ImsApi::class.java)
    }

    private val weatherAttribution by lazy {
        with(context.currentLocale.code) {
            when {
                startsWith("ar") -> "خدمة الأرصاد الجوية الإسرائيلية"
                startsWith("he") || startsWith("iw") -> "השירות המטאורולוגי הישראלי"
                else -> "Israel Meteorological Service"
            }
        }
    }
    override val reverseGeocodingAttribution = weatherAttribution
    override val supportedFeatures = mapOf(
        SourceFeature.FORECAST to weatherAttribution,
        SourceFeature.CURRENT to weatherAttribution,
        SourceFeature.ALERT to weatherAttribution
    )

    override fun isFeatureSupportedForLocation(
        location: Location,
        feature: SourceFeature,
    ): Boolean {
        return isReverseGeocodingSupportedForLocation(location)
    }

    override fun isReverseGeocodingSupportedForLocation(location: Location): Boolean {
        // Israel + West Bank + Gaza Strip
        return location.countryCode.equals("IL", ignoreCase = true) ||
            location.countryCode.equals("PS", ignoreCase = true)
    }

    override fun requestWeather(
        context: Context,
        location: Location,
        requestedFeatures: List<SourceFeature>,
    ): Observable<WeatherWrapper> {
        val locationId = location.parameters.getOrElse(id) { null }?.getOrElse("locationId") { null }

        if (locationId.isNullOrEmpty()) {
            throw InvalidLocationException()
        }

        val languageCode = when (context.currentLocale.code) {
            "ar" -> "ar"
            "he", "iw" -> "he"
            else -> "en"
        }

        return mApi.getWeather(languageCode, locationId).map {
            WeatherWrapper(
                dailyForecast = if (SourceFeature.FORECAST in requestedFeatures) {
                    getDailyForecast(location, it.data)
                } else {
                    null
                },
                hourlyForecast = if (SourceFeature.FORECAST in requestedFeatures) {
                    getHourlyForecast(context, location, it.data)
                } else {
                    null
                },
                current = if (SourceFeature.CURRENT in requestedFeatures) {
                    getCurrent(context, it.data)
                } else {
                    null
                },
                alertList = if (SourceFeature.ALERT in requestedFeatures) {
                    getAlerts(context, it.data)
                } else {
                    null
                }
            )
        }
    }

    override fun requestReverseGeocodingLocation(
        context: Context,
        location: Location,
    ): Observable<List<Location>> {
        val languageCode = when (context.currentLocale.code) {
            "ar" -> "ar"
            "he", "iw" -> "he"
            else -> "en"
        }
        return mApi.getLocations(languageCode)
            .map { result ->
                if (result.data.isNullOrEmpty()) {
                    throw ReverseGeocodingException()
                }

                val nearestStation = result.data
                    .values
                    .filter { station ->
                        station.lat.toDoubleOrNull() != null && station.lon.toDoubleOrNull() != null
                    }
                    .minByOrNull { station ->
                        SphericalUtil.computeDistanceBetween(
                            LatLng(location.latitude, location.longitude),
                            LatLng(station.lat.toDouble(), station.lon.toDouble())
                        )
                    } ?: throw ReverseGeocodingException()

                val distanceWithNearestStation = SphericalUtil.computeDistanceBetween(
                    LatLng(location.latitude, location.longitude),
                    LatLng(nearestStation.lat.toDouble(), nearestStation.lon.toDouble())
                )

                if (BreezyWeather.instance.debugMode) {
                    LogHelper.log(msg = "${nearestStation.name}: $distanceWithNearestStation meters")
                }

                // Only add if within a reasonable distance
                if (distanceWithNearestStation > MINIMUM_DISTANCE_WITH_STATION) {
                    throw InvalidLocationException()
                }

                listOf(convert(location, nearestStation))
            }
    }

    // Location parameters
    override fun needsLocationParametersRefresh(
        location: Location,
        coordinatesChanged: Boolean,
        features: List<SourceFeature>,
    ): Boolean {
        if (coordinatesChanged) return true

        val currentLocationId = location.parameters.getOrElse(id) { null }?.getOrElse("locationId") { null }

        return currentLocationId.isNullOrEmpty()
    }

    // TODO: Redundant with reverse geocoding
    override fun requestLocationParameters(
        context: Context,
        location: Location,
    ): Observable<Map<String, String>> {
        val languageCode = when (context.currentLocale.code) {
            "ar" -> "ar"
            "he", "iw" -> "he"
            else -> "en"
        }
        return mApi.getLocations(languageCode)
            .map { result ->
                if (result.data.isNullOrEmpty()) {
                    throw ReverseGeocodingException()
                }

                val nearestStation = result.data
                    .values
                    .filter { station ->
                        station.lat.toDoubleOrNull() != null && station.lon.toDoubleOrNull() != null
                    }
                    .minByOrNull { station ->
                        SphericalUtil.computeDistanceBetween(
                            LatLng(location.latitude, location.longitude),
                            LatLng(station.lat.toDouble(), station.lon.toDouble())
                        )
                    } ?: throw ReverseGeocodingException()

                val distanceWithNearestStation = SphericalUtil.computeDistanceBetween(
                    LatLng(location.latitude, location.longitude),
                    LatLng(nearestStation.lat.toDouble(), nearestStation.lon.toDouble())
                )

                if (BreezyWeather.instance.debugMode) {
                    LogHelper.log(msg = "${nearestStation.name}: $distanceWithNearestStation meters")
                }

                // Only add if within a reasonable distance
                if (distanceWithNearestStation > MINIMUM_DISTANCE_WITH_STATION) {
                    throw InvalidLocationException()
                }

                mapOf("locationId" to nearestStation.lid)
            }
    }

    override val testingLocations: List<Location> = emptyList()

    companion object {
        private const val IMS_BASE_URL = "https://ims.gov.il/"
        private const val MINIMUM_DISTANCE_WITH_STATION = 50000 // 50 km is more than enough
    }
}
