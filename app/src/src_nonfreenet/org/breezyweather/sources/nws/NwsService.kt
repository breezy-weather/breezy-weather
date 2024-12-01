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
import breezyweather.domain.feature.SourceFeature
import breezyweather.domain.location.model.Location
import breezyweather.domain.weather.wrappers.SecondaryWeatherWrapper
import breezyweather.domain.weather.wrappers.WeatherWrapper
import io.reactivex.rxjava3.core.Observable
import org.breezyweather.BuildConfig
import org.breezyweather.common.exceptions.InvalidLocationException
import org.breezyweather.common.exceptions.SecondaryWeatherException
import org.breezyweather.common.source.HttpSource
import org.breezyweather.common.source.LocationParametersSource
import org.breezyweather.common.source.MainWeatherSource
import org.breezyweather.common.source.ReverseGeocodingSource
import org.breezyweather.common.source.SecondaryWeatherSource
import org.breezyweather.sources.nws.json.NwsAlertsResult
import retrofit2.Retrofit
import javax.inject.Inject
import javax.inject.Named

class NwsService @Inject constructor(
    @Named("JsonClient") client: Retrofit.Builder,
) : HttpSource(), MainWeatherSource, SecondaryWeatherSource, ReverseGeocodingSource, LocationParametersSource {

    override val id = "nws"
    override val name = "National Weather Service (NWS)"
    override val privacyPolicyUrl = "https://www.weather.gov/privacy"

    override val color = Color.rgb(51, 176, 225)
    override val weatherAttribution = "National Weather Service (NWS)"

    private val mApi by lazy {
        client
            .baseUrl(NWS_BASE_URL)
            .build()
            .create(NwsApi::class.java)
    }

    override val supportedFeaturesInMain = listOf(
        SourceFeature.FEATURE_ALERT
    )

    private val supportedCountries = setOf(
        "US",
        "PR", // Puerto Rico
        "VI", // St Thomas Islands
        "MP", // Mariana Islands
        "GU", // Guam
        "FM", // Palikir
        "PW", // Melekeok
        "AS", // Pago Pago
        "UM", "XB", "XH", "XQ", "XU", "XM", "QM", "XV", "XL", "QW" // Minor Outlying Islands
    )

    override fun isFeatureSupportedInMainForLocation(
        location: Location,
        feature: SourceFeature?,
    ): Boolean {
        return supportedCountries.any {
            location.countryCode.equals(it, ignoreCase = true)
        }
    }

    override fun requestWeather(
        context: Context,
        location: Location,
        ignoreFeatures: List<SourceFeature>,
    ): Observable<WeatherWrapper> {
        val gridId = location.parameters
            .getOrElse(id) { null }?.getOrElse("gridId") { null }
        val gridX = location.parameters
            .getOrElse(id) { null }?.getOrElse("gridX") { null }
        val gridY = location.parameters
            .getOrElse(id) { null }?.getOrElse("gridY") { null }

        if (gridId.isNullOrEmpty() || gridX.isNullOrEmpty() || gridY.isNullOrEmpty()) {
            return Observable.error(InvalidLocationException())
        }

        val nwsForecastResult = mApi.getForecast(
            USER_AGENT,
            gridId,
            gridX.toInt(),
            gridY.toInt()
        )

        val failedFeatures = mutableListOf<SourceFeature>()
        val nwsAlertsResult = if (!ignoreFeatures.contains(SourceFeature.FEATURE_ALERT)) {
            mApi.getActiveAlerts(
                USER_AGENT,
                "${location.latitude},${location.longitude}"
            ).onErrorResumeNext {
                failedFeatures.add(SourceFeature.FEATURE_ALERT)
                Observable.just(NwsAlertsResult())
            }
        } else {
            Observable.just(NwsAlertsResult())
        }

        return Observable.zip(
            nwsForecastResult,
            nwsAlertsResult
        ) { forecastResult, alertResult ->
            convert(forecastResult, alertResult, location, failedFeatures)
        }
    }

    // SECONDARY WEATHER SOURCE
    override val supportedFeaturesInSecondary = listOf(
        SourceFeature.FEATURE_ALERT
    )
    override fun isFeatureSupportedInSecondaryForLocation(
        location: Location,
        feature: SourceFeature,
    ): Boolean {
        return isFeatureSupportedInMainForLocation(location, feature)
    }
    override val currentAttribution = null
    override val airQualityAttribution = null
    override val pollenAttribution = null
    override val minutelyAttribution = null
    override val alertAttribution = weatherAttribution
    override val normalsAttribution = null

    override fun requestSecondaryWeather(
        context: Context,
        location: Location,
        requestedFeatures: List<SourceFeature>,
    ): Observable<SecondaryWeatherWrapper> {
        if (!isFeatureSupportedInSecondaryForLocation(location, SourceFeature.FEATURE_ALERT)) {
            // TODO: return Observable.error(UnsupportedFeatureForLocationException())
            return Observable.error(SecondaryWeatherException())
        }

        return mApi.getActiveAlerts(
            USER_AGENT,
            "${location.latitude},${location.longitude}"
        ).map {
            convertSecondary(it)
        }
    }

    // Reverse geocoding
    override fun requestReverseGeocodingLocation(
        context: Context,
        location: Location,
    ): Observable<List<Location>> {
        return mApi.getPoints(
            USER_AGENT,
            location.latitude,
            location.longitude
        ).map {
            if (it.properties == null) {
                throw InvalidLocationException()
            }
            val locationList = mutableListOf<Location>()
            locationList.add(convert(location, it.properties))
            locationList
        }
    }

    // Location parameters
    override fun needsLocationParametersRefresh(
        location: Location,
        coordinatesChanged: Boolean,
        features: List<SourceFeature>,
    ): Boolean {
        // Not needed for alert endpoint
        if (features.contains(SourceFeature.FEATURE_ALERT)) return false
        if (coordinatesChanged) return true

        val currentGridId = location.parameters
            .getOrElse(id) { null }?.getOrElse("gridId") { null }
        val currentGridX = location.parameters
            .getOrElse(id) { null }?.getOrElse("gridX") { null }
        val currentGridY = location.parameters
            .getOrElse(id) { null }?.getOrElse("gridY") { null }

        return currentGridId.isNullOrEmpty() ||
            currentGridX.isNullOrEmpty() ||
            currentGridY.isNullOrEmpty()
    }

    override fun requestLocationParameters(
        context: Context,
        location: Location,
    ): Observable<Map<String, String>> {
        return mApi.getPoints(
            USER_AGENT,
            location.latitude,
            location.longitude
        ).map {
            if (it.properties == null) {
                throw InvalidLocationException()
            }
            mapOf(
                "gridId" to it.properties.gridId,
                "gridX" to it.properties.gridX.toString(),
                "gridY" to it.properties.gridY.toString()
            )
        }
    }

    companion object {
        private const val NWS_BASE_URL = "https://api.weather.gov/"
        private const val USER_AGENT =
            "(BreezyWeather/${BuildConfig.VERSION_NAME}, github.com/breezy-weather/breezy-weather/issues)"
    }
}
