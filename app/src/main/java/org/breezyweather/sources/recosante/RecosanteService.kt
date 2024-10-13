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

package org.breezyweather.sources.recosante

import android.content.Context
import breezyweather.domain.location.model.Location
import breezyweather.domain.weather.wrappers.SecondaryWeatherWrapper
import dagger.hilt.android.qualifiers.ApplicationContext
import io.reactivex.rxjava3.core.Observable
import org.breezyweather.R
import org.breezyweather.common.exceptions.InvalidLocationException
import org.breezyweather.common.exceptions.SecondaryWeatherException
import org.breezyweather.common.preference.EditTextPreference
import org.breezyweather.common.preference.Preference
import org.breezyweather.common.source.ConfigurableSource
import org.breezyweather.common.source.HttpSource
import org.breezyweather.common.source.LocationParametersSource
import org.breezyweather.common.source.PollenIndexSource
import org.breezyweather.common.source.SecondaryWeatherSource
import org.breezyweather.common.source.SecondaryWeatherSourceFeature
import org.breezyweather.settings.SourceConfigStore
import retrofit2.Retrofit
import javax.inject.Inject
import javax.inject.Named

/**
 * Recosanté pollen service.
 */
class RecosanteService @Inject constructor(
    @ApplicationContext context: Context,
    @Named("JsonClient") val client: Retrofit.Builder
) : HttpSource(), SecondaryWeatherSource, PollenIndexSource, LocationParametersSource,
    ConfigurableSource {

    override val id = "recosante"
    override val name = "Recosanté"
    override val privacyPolicyUrl = "https://recosante.beta.gouv.fr/donnees-personnelles/"

    private val mGeoApi: GeoApi
        get() {
            return client
                .baseUrl(geocodingInstance)
                .build()
                .create(GeoApi::class.java)
        }
    private val mPollenApi: RecosanteApi
        get() {
            return client
                .baseUrl(instance)
                .build()
                .create(RecosanteApi::class.java)
        }

    override val supportedFeaturesInSecondary = listOf(SecondaryWeatherSourceFeature.FEATURE_POLLEN)
    override fun isFeatureSupportedInSecondaryForLocation(
        location: Location,
        feature: SecondaryWeatherSourceFeature
    ): Boolean {
        return !location.countryCode.isNullOrEmpty()
            && location.countryCode.equals("FR", ignoreCase = true)
    }
    override val airQualityAttribution = null
    override val pollenAttribution = "Recosanté, Le Réseau national de surveillance aérobiologique (RNSA) https://www.pollens.fr/"
    override val minutelyAttribution = null
    override val alertAttribution = null
    override val normalsAttribution = null

    override fun requestSecondaryWeather(
        context: Context, location: Location,
        requestedFeatures: List<SecondaryWeatherSourceFeature>
    ): Observable<SecondaryWeatherWrapper> {
        if (!isFeatureSupportedInSecondaryForLocation(location, SecondaryWeatherSourceFeature.FEATURE_POLLEN)) {
            // TODO: return Observable.error(UnsupportedFeatureForLocationException())
            return Observable.error(SecondaryWeatherException())
        }
        val insee = location.parameters
            .getOrElse(id) { null }?.getOrElse("insee") { null }
        if (insee.isNullOrEmpty()) {
            return Observable.error(SecondaryWeatherException())
        }

        return mPollenApi.getData(
            true,
            insee
        ).map {
            convert(location, it)
        }
    }

    // Location parameters
    override fun needsLocationParametersRefresh(
        location: Location,
        coordinatesChanged: Boolean,
        features: List<SecondaryWeatherSourceFeature>
    ): Boolean {
        if (coordinatesChanged) return true

        val currentInsee = location.parameters
            .getOrElse(id) { null }?.getOrElse("insee") { null }

        return currentInsee.isNullOrEmpty()
    }

    override fun requestLocationParameters(
        context: Context, location: Location
    ): Observable<Map<String, String>> {
        return mGeoApi.getCommunes(location.longitude, location.latitude)
            .map { result ->
                if (result.isNotEmpty()) {
                    mapOf("insee" to result[0].code)
                } else {
                    throw InvalidLocationException()
                }
            }
    }

    // CONFIG
    private val config = SourceConfigStore(context, id)
    override val isConfigured = true
    override val isRestricted = false
    private var instance: String
        set(value) {
            config.edit().putString("instance", value).apply()
        }
        get() = config.getString("instance", null) ?: RECOSANTE_BASE_URL
    private var geocodingInstance: String
        set(value) {
            config.edit().putString("geocoding_instance", value).apply()
        }
        get() = config.getString("geocoding_instance", null) ?: GEO_BASE_URL

    override fun getPreferences(context: Context): List<Preference> {
        return listOf(
            EditTextPreference(
                titleId = R.string.settings_weather_source_recosante_instance,
                summary = { _, content ->
                    content.ifEmpty {
                        RECOSANTE_BASE_URL
                    }
                },
                content = instance,
                regex = EditTextPreference.URL_REGEX,
                regexError = context.getString(R.string.settings_source_instance_invalid),
                onValueChanged = {
                    instance = it
                }
            ),
            EditTextPreference(
                titleId = R.string.settings_weather_source_recosante_instance_geocoding,
                summary = { _, content ->
                    content.ifEmpty {
                        GEO_BASE_URL
                    }
                },
                content = geocodingInstance,
                regex = EditTextPreference.URL_REGEX,
                regexError = context.getString(R.string.settings_source_instance_invalid),
                onValueChanged = {
                    geocodingInstance = it
                }
            )
        )
    }

    override val pollenLabels = R.array.pollen_recosante_levels
    override val pollenColors = R.array.pollen_recosante_level_colors

    companion object {
        private const val GEO_BASE_URL = "https://geo.api.gouv.fr/"
        private const val RECOSANTE_BASE_URL = "https://api.recosante.beta.gouv.fr/"
    }
}
