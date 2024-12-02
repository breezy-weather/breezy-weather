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

package org.breezyweather.sources.dmi

import android.content.Context
import android.graphics.Color
import breezyweather.domain.location.model.Location
import breezyweather.domain.source.SourceContinent
import breezyweather.domain.source.SourceFeature
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
import org.breezyweather.sources.dmi.json.DmiResult
import org.breezyweather.sources.dmi.json.DmiWarningResult
import retrofit2.Retrofit
import java.util.Locale
import javax.inject.Inject
import javax.inject.Named

class DmiService @Inject constructor(
    @ApplicationContext context: Context,
    @Named("JsonClient") client: Retrofit.Builder,
) : HttpSource(), MainWeatherSource, SecondaryWeatherSource, ReverseGeocodingSource, LocationParametersSource {

    override val id = "dmi"
    override val name = "DMI (${Locale(context.currentLocale.code, "DK").displayCountry})"
    override val continent = SourceContinent.EUROPE
    override val privacyPolicyUrl = "https://www.dmi.dk/om-hjemmesiden/privatliv/"

    override val color = Color.rgb(12, 45, 131)
    override val weatherAttribution = "DMI (Creative Commons CC BY)"

    private val mApi by lazy {
        client
            .baseUrl(DMI_BASE_URL)
            .build()
            .create(DmiApi::class.java)
    }

    override val supportedFeaturesInMain = listOf(
        SourceFeature.FEATURE_ALERT
    )

    override fun requestWeather(
        context: Context,
        location: Location,
        ignoreFeatures: List<SourceFeature>,
    ): Observable<WeatherWrapper> {
        val weather = mApi.getWeather(
            location.latitude,
            location.longitude,
            DMI_WEATHER_CMD
        )

        val failedFeatures = mutableListOf<SourceFeature>()
        val alerts = if (!ignoreFeatures.contains(SourceFeature.FEATURE_ALERT) &&
            arrayOf("DK", "FO", "GL").any { it.equals(location.countryCode, ignoreCase = true) }
        ) {
            val id = location.parameters.getOrElse(id) { null }?.getOrElse("id") { null }
            if (!id.isNullOrEmpty()) {
                mApi.getAlerts(id).onErrorResumeNext {
                    failedFeatures.add(SourceFeature.FEATURE_ALERT)
                    Observable.just(DmiWarningResult())
                }
            } else {
                failedFeatures.add(SourceFeature.FEATURE_ALERT)
                Observable.just(DmiWarningResult())
            }
        } else {
            Observable.just(DmiWarningResult())
        }

        return Observable.zip(
            weather,
            alerts
        ) { weatherResult: DmiResult, alertsResult: DmiWarningResult ->
            convert(weatherResult, alertsResult, location, failedFeatures)
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
        return arrayOf("DK", "FO", "GL").any { it.equals(location.countryCode, ignoreCase = true) }
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

        val id = location.parameters.getOrElse(id) { null }?.getOrElse("id") { null }
        if (id.isNullOrEmpty()) {
            return Observable.error(SecondaryWeatherException())
        }
        return mApi.getAlerts(id).map {
            convertSecondary(it)
        }
    }

    // Reverse geocoding
    override fun requestReverseGeocodingLocation(
        context: Context,
        location: Location,
    ): Observable<List<Location>> {
        return mApi.getWeather(
            location.latitude,
            location.longitude,
            DMI_WEATHER_CMD
        ).map {
            val locationList = mutableListOf<Location>()
            locationList.add(convert(location, it))
            locationList
        }
    }

    // Location parameters
    override fun needsLocationParametersRefresh(
        location: Location,
        coordinatesChanged: Boolean,
        features: List<SourceFeature>,
    ): Boolean {
        // If we are in Denmark, Faroe Islands, or Greenland, we need location parameters
        // (update it if coordinates changes, OR if we didn't have it yet)
        return arrayOf("DK", "FO", "GL").any { it.equals(location.countryCode, ignoreCase = true) } &&
            (coordinatesChanged || location.parameters.getOrElse(id) { null }?.getOrElse("id") { null }.isNullOrEmpty())
    }

    override fun requestLocationParameters(
        context: Context,
        location: Location,
    ): Observable<Map<String, String>> {
        return mApi.getWeather(
            location.latitude,
            location.longitude,
            DMI_WEATHER_CMD
        ).map {
            if (it.id.isNullOrEmpty()) {
                throw InvalidLocationException()
            }
            mapOf("id" to it.id)
        }
    }

    companion object {
        private const val DMI_BASE_URL = "https://www.dmi.dk/"
        private const val DMI_WEATHER_CMD = "llj"
    }
}
