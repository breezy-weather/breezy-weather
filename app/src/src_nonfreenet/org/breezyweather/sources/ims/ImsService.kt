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
import android.graphics.Color
import breezyweather.domain.location.model.Location
import breezyweather.domain.weather.wrappers.SecondaryWeatherWrapper
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
import org.breezyweather.common.source.MainWeatherSource
import org.breezyweather.common.source.ReverseGeocodingSource
import org.breezyweather.common.source.SecondaryWeatherSource
import org.breezyweather.common.source.SecondaryWeatherSourceFeature
import org.breezyweather.common.utils.helpers.LogHelper
import retrofit2.Retrofit
import javax.inject.Inject
import javax.inject.Named

/**
 * Israel Meteorological Service
 */
class ImsService @Inject constructor(
    @ApplicationContext context: Context,
    @Named("JsonClient") client: Retrofit.Builder
) : HttpSource(), MainWeatherSource, SecondaryWeatherSource,
    ReverseGeocodingSource, LocationParametersSource {

    override val id = "ims"
    override val name by lazy {
        with (context.currentLocale.code) {
            when {
                startsWith("he") || startsWith("iw") -> "השירות המטאורולוגי הישראלי"
                else -> "Israel Meteorological Service"
            }
        }
    }
    override val privacyPolicyUrl by lazy {
        with (context.currentLocale.code) {
            when {
                startsWith("he") || startsWith("iw") -> "https://ims.gov.il/he/termOfuse"
                else -> "https://ims.gov.il/en/termOfuse"
            }
        }
    }

    override val color = Color.rgb(52, 79, 110)
    override val weatherAttribution = "Israel Meteorological Service"

    private val mApi by lazy {
        client
            .baseUrl(IMS_BASE_URL)
            .build()
            .create(ImsApi::class.java)
    }

    override val supportedFeaturesInMain = listOf(
        SecondaryWeatherSourceFeature.FEATURE_ALERT
    )

    override fun isFeatureSupportedInMainForLocation(
        location: Location,
        feature: SecondaryWeatherSourceFeature?
    ): Boolean {
        return location.countryCode.equals("IL", ignoreCase = true) || // Israel
            location.countryCode.equals("PS", ignoreCase = true) // West Bank + Gaza Strip
    }

    override fun requestWeather(
        context: Context, location: Location, ignoreFeatures: List<SecondaryWeatherSourceFeature>
    ): Observable<WeatherWrapper> {
        val locationId = location.parameters
            .getOrElse(id) { null }?.getOrElse("locationId") { null }

        if (locationId.isNullOrEmpty()) {
            throw InvalidLocationException()
        }

        val languageCode = when (context.currentLocale.code) {
            "ar" -> "ar"
            "he", "iw" -> "he"
            else -> "en"
        }

        return mApi.getWeather(languageCode, locationId)
            .map {
                convert(it, location)
            }
    }

    // SECONDARY WEATHER SOURCE
    override val supportedFeaturesInSecondary = listOf(
        SecondaryWeatherSourceFeature.FEATURE_ALERT
    )
    override fun isFeatureSupportedInSecondaryForLocation(
        location: Location,
        feature: SecondaryWeatherSourceFeature
    ): Boolean {
        return isFeatureSupportedInMainForLocation(location, feature)
    }
    override val currentAttribution = weatherAttribution
    override val airQualityAttribution = null
    override val pollenAttribution = null
    override val minutelyAttribution = null
    override val alertAttribution = weatherAttribution
    override val normalsAttribution = null

    override fun requestSecondaryWeather(
        context: Context, location: Location,
        requestedFeatures: List<SecondaryWeatherSourceFeature>
    ): Observable<SecondaryWeatherWrapper> {
        val locationId = location.parameters
            .getOrElse(id) { null }?.getOrElse("locationId") { null }

        if (locationId.isNullOrEmpty()) {
            throw InvalidLocationException()
        }

        val languageCode = when (context.currentLocale.code) {
            "ar" -> "ar"
            "he", "iw" -> "he"
            else -> "en"
        }

        return mApi.getWeather(languageCode, locationId)
            .map {
                convertSecondary(it)
            }
    }

    override fun requestReverseGeocodingLocation(
        context: Context,
        location: Location
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
        features: List<SecondaryWeatherSourceFeature>
    ): Boolean {
        if (coordinatesChanged) return true

        val currentLocationId = location.parameters
            .getOrElse(id) { null }?.getOrElse("locationId") { null }

        return currentLocationId.isNullOrEmpty()
    }

    // TODO: Redundant with reverse geocoding
    override fun requestLocationParameters(
        context: Context, location: Location
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

    companion object {
        private const val IMS_BASE_URL = "https://ims.gov.il/"
        private const val MINIMUM_DISTANCE_WITH_STATION = 50000 // 50 km is more than enough
    }
}
