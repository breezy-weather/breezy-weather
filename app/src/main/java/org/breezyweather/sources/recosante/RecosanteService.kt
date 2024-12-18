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
import android.graphics.Color
import breezyweather.domain.location.model.Location
import breezyweather.domain.source.SourceContinent
import breezyweather.domain.source.SourceFeature
import breezyweather.domain.weather.wrappers.WeatherWrapper
import dagger.hilt.android.qualifiers.ApplicationContext
import io.reactivex.rxjava3.core.Observable
import org.breezyweather.R
import org.breezyweather.common.exceptions.InvalidLocationException
import org.breezyweather.common.extensions.code
import org.breezyweather.common.extensions.currentLocale
import org.breezyweather.common.preference.EditTextPreference
import org.breezyweather.common.preference.Preference
import org.breezyweather.common.source.ConfigurableSource
import org.breezyweather.common.source.HttpSource
import org.breezyweather.common.source.LocationParametersSource
import org.breezyweather.common.source.PollenIndexSource
import org.breezyweather.common.source.WeatherSource
import org.breezyweather.settings.SourceConfigStore
import retrofit2.Retrofit
import java.util.Locale
import javax.inject.Inject
import javax.inject.Named

/**
 * Recosanté pollen service.
 */
class RecosanteService @Inject constructor(
    @ApplicationContext context: Context,
    @Named("JsonClient") val client: Retrofit.Builder,
) : HttpSource(), WeatherSource, PollenIndexSource, LocationParametersSource, ConfigurableSource {

    override val id = "recosante"
    override val name = "Recosanté (${Locale(context.currentLocale.code, "FR").displayCountry})"
    override val color = Color.rgb(0, 0, 145)
    override val continent = SourceContinent.EUROPE
    override val privacyPolicyUrl = "https://recosante.beta.gouv.fr/donnees-personnelles/"

    private val mGeoApi: GeoApi
        get() {
            return client
                .baseUrl(geocodingInstance!!)
                .build()
                .create(GeoApi::class.java)
        }
    private val mPollenApi: RecosanteApi
        get() {
            return client
                .baseUrl(instance!!)
                .build()
                .create(RecosanteApi::class.java)
        }

    override val supportedFeatures = mapOf(
        SourceFeature.POLLEN to
            "Recosanté, Le Réseau national de surveillance aérobiologique (RNSA) https://www.pollens.fr/"
    )
    override fun isFeatureSupportedForLocation(
        location: Location,
        feature: SourceFeature,
    ): Boolean {
        return !location.countryCode.isNullOrEmpty() && location.countryCode.equals("FR", ignoreCase = true)
    }

    override fun requestWeather(
        context: Context,
        location: Location,
        requestedFeatures: List<SourceFeature>,
    ): Observable<WeatherWrapper> {
        val insee = location.parameters.getOrElse(id) { null }?.getOrElse("insee") { null }
        if (insee.isNullOrEmpty()) {
            return Observable.error(InvalidLocationException())
        }

        return mPollenApi.getData(
            true,
            insee
        ).map {
            WeatherWrapper(
                pollen = getPollen(location, it)
            )
        }
    }

    // Location parameters
    override fun needsLocationParametersRefresh(
        location: Location,
        coordinatesChanged: Boolean,
        features: List<SourceFeature>,
    ): Boolean {
        if (coordinatesChanged) return true

        val currentInsee = location.parameters.getOrElse(id) { null }?.getOrElse("insee") { null }

        return currentInsee.isNullOrEmpty()
    }

    override fun requestLocationParameters(
        context: Context,
        location: Location,
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
    private var instance: String?
        set(value) {
            value?.let {
                config.edit().putString("instance", it).apply()
            } ?: config.edit().remove("instance").apply()
        }
        get() = config.getString("instance", null) ?: RECOSANTE_BASE_URL
    private var geocodingInstance: String?
        set(value) {
            value?.let {
                config.edit().putString("geocoding_instance", it).apply()
            } ?: config.edit().remove("geocoding_instance").apply()
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
                content = if (instance != RECOSANTE_BASE_URL) instance else null,
                placeholder = RECOSANTE_BASE_URL,
                regex = EditTextPreference.URL_REGEX,
                regexError = context.getString(R.string.settings_source_instance_invalid),
                onValueChanged = {
                    instance = if (it == RECOSANTE_BASE_URL) null else it.ifEmpty { null }
                }
            ),
            EditTextPreference(
                titleId = R.string.settings_weather_source_recosante_instance_geocoding,
                summary = { _, content ->
                    content.ifEmpty {
                        GEO_BASE_URL
                    }
                },
                content = if (geocodingInstance != GEO_BASE_URL) geocodingInstance else null,
                placeholder = GEO_BASE_URL,
                regex = EditTextPreference.URL_REGEX,
                regexError = context.getString(R.string.settings_source_instance_invalid),
                onValueChanged = {
                    geocodingInstance = if (it == GEO_BASE_URL) null else it.ifEmpty { null }
                }
            )
        )
    }

    override val pollenLabels = R.array.pollen_recosante_levels
    override val pollenColors = R.array.pollen_recosante_level_colors

    override val testingLocations = listOf(
        Location(
            city = "Marseille",
            latitude = 43.29695,
            longitude = 5.38107,
            timeZone = "Europe/Paris",
            countryCode = "FR",
            pollenSource = id
        )
    )

    companion object {
        private const val GEO_BASE_URL = "https://geo.api.gouv.fr/"
        private const val RECOSANTE_BASE_URL = "https://api.recosante.beta.gouv.fr/"
    }
}
