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
import android.graphics.Color
import breezyweather.domain.feature.SourceFeature
import breezyweather.domain.location.model.Location
import breezyweather.domain.weather.wrappers.SecondaryWeatherWrapper
import breezyweather.domain.weather.wrappers.WeatherWrapper
import dagger.hilt.android.qualifiers.ApplicationContext
import io.reactivex.rxjava3.core.Observable
import org.breezyweather.common.exceptions.InvalidLocationException
import org.breezyweather.common.exceptions.SecondaryWeatherException
import org.breezyweather.common.extensions.code
import org.breezyweather.common.extensions.currentLocale
import org.breezyweather.common.source.HttpSource
import org.breezyweather.common.source.LocationParametersSource
import org.breezyweather.common.source.MainWeatherSource
import org.breezyweather.common.source.ReverseGeocodingSource
import org.breezyweather.common.source.SecondaryWeatherSource
import org.breezyweather.sources.ipma.json.IpmaAlertResult
import org.breezyweather.sources.ipma.json.IpmaDistrictResult
import org.breezyweather.sources.ipma.json.IpmaForecastResult
import org.breezyweather.sources.ipma.json.IpmaLocationResult
import retrofit2.Retrofit
import javax.inject.Inject
import javax.inject.Named
import kotlin.text.startsWith

class IpmaService @Inject constructor(
    @ApplicationContext context: Context,
    @Named("JsonClient") client: Retrofit.Builder,
) : HttpSource(), MainWeatherSource, SecondaryWeatherSource, ReverseGeocodingSource, LocationParametersSource {

    override val id = "ipma"
    override val name = "IPMA"
    override val privacyPolicyUrl by lazy {
        if (context.currentLocale.code.startsWith("pt")) {
            "https://www.ipma.pt/pt/siteinfo/index.html"
        } else {
            "https://www.ipma.pt/en/siteinfo/index.html"
        }
    }
    override val color = Color.rgb(32, 196, 244)
    override val weatherAttribution = "Instituto Português do Mar e da Atmosfera"

    private val mApi by lazy {
        client
            .baseUrl(IPMA_BASE_URL)
            .build()
            .create(IpmaApi::class.java)
    }

    override val supportedFeaturesInMain = listOf(
        SourceFeature.FEATURE_ALERT
    )

    override fun isFeatureSupportedInMainForLocation(
        location: Location,
        feature: SourceFeature?,
    ): Boolean {
        return location.countryCode.equals("PT", ignoreCase = true)
    }

    override fun requestWeather(
        context: Context,
        location: Location,
        ignoreFeatures: List<SourceFeature>,
    ): Observable<WeatherWrapper> {
        val globalIdLocal = location.parameters.getOrElse(id) { null }?.getOrElse("globalIdLocal") { null }
        val idAreaAviso = location.parameters.getOrElse(id) { null }?.getOrElse("idAreaAviso") { null }
        if (globalIdLocal.isNullOrEmpty() || idAreaAviso.isNullOrEmpty()) {
            return Observable.error(InvalidLocationException())
        }

        val forecast = mApi.getForecast(globalIdLocal)
        val alerts = if (!ignoreFeatures.contains(SourceFeature.FEATURE_ALERT)) {
            mApi.getAlerts().onErrorResumeNext {
                // TODO: Log warning
                Observable.just(IpmaAlertResult())
            }
        } else {
            Observable.just(IpmaAlertResult())
        }

        return Observable.zip(forecast, alerts) {
                forecastResult: List<IpmaForecastResult>,
                alertResult: IpmaAlertResult,
            ->
            convert(
                context = context,
                location = location,
                forecastResult = forecastResult,
                alertResult = alertResult
            )
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

        val globalIdLocal = location.parameters.getOrElse(id) { null }?.getOrElse("globalIdLocal") { null }
        val idAreaAviso = location.parameters.getOrElse(id) { null }?.getOrElse("idAreaAviso") { null }
        if (globalIdLocal.isNullOrEmpty() || idAreaAviso.isNullOrEmpty()) {
            return Observable.error(InvalidLocationException())
        }
        val alerts = if (requestedFeatures.contains(SourceFeature.FEATURE_ALERT)) {
            mApi.getAlerts()
        } else {
            Observable.just(IpmaAlertResult())
        }
        return alerts.map {
            convertSecondary(
                location = location,
                alertResult = if (requestedFeatures.contains(SourceFeature.FEATURE_ALERT)) {
                    it
                } else {
                    null
                }
            )
        }
    }

    // Reverse geocoding
    override fun requestReverseGeocodingLocation(
        context: Context,
        location: Location,
    ): Observable<List<Location>> {
        val districts = mApi.getDistricts()
        val locations = mApi.getLocations()
        return Observable.zip(districts, locations) {
                districtResult: List<IpmaDistrictResult>,
                locationResult: List<IpmaLocationResult>,
            ->
            convert(location, districtResult, locationResult)
        }
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

    companion object {
        private const val IPMA_BASE_URL = "https://api.ipma.pt/"
    }
}
