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
import androidx.compose.ui.text.input.KeyboardType
import breezyweather.domain.location.model.Location
import breezyweather.domain.source.SourceContinent
import breezyweather.domain.source.SourceFeature
import breezyweather.domain.weather.model.Pollen
import breezyweather.domain.weather.wrappers.PollenWrapper
import breezyweather.domain.weather.wrappers.WeatherWrapper
import dagger.hilt.android.qualifiers.ApplicationContext
import io.reactivex.rxjava3.core.Observable
import org.breezyweather.R
import org.breezyweather.common.exceptions.InvalidLocationException
import org.breezyweather.common.extensions.currentLocale
import org.breezyweather.common.extensions.getCountryName
import org.breezyweather.common.extensions.toCalendarWithTimeZone
import org.breezyweather.common.extensions.toDateNoHour
import org.breezyweather.common.extensions.toTimezoneNoHour
import org.breezyweather.common.preference.EditTextPreference
import org.breezyweather.common.preference.Preference
import org.breezyweather.common.source.ConfigurableSource
import org.breezyweather.common.source.HttpSource
import org.breezyweather.common.source.LocationParametersSource
import org.breezyweather.common.source.PollenIndexSource
import org.breezyweather.common.source.WeatherSource
import org.breezyweather.common.source.WeatherSource.Companion.PRIORITY_MEDIUM
import org.breezyweather.common.source.WeatherSource.Companion.PRIORITY_NONE
import org.breezyweather.domain.settings.SourceConfigStore
import org.breezyweather.sources.recosante.json.RecosanteRaepIndiceDetail
import org.breezyweather.sources.recosante.json.RecosanteResult
import retrofit2.Retrofit
import java.util.Calendar
import java.util.Date
import java.util.TimeZone
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
    override val name = "Recosanté (${context.currentLocale.getCountryName("FR")})"
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
        SourceFeature.POLLEN to "Recosanté • Atmo France"
    )
    override val attributionLinks = mapOf(
        "Recosanté" to "https://recosante.beta.gouv.fr/",
        "Atmo France" to "https://www.atmo-france.org/"
    )
    override fun isFeatureSupportedForLocation(
        location: Location,
        feature: SourceFeature,
    ): Boolean {
        return !location.countryCode.isNullOrEmpty() && location.countryCode.equals("FR", ignoreCase = true)
    }

    /**
     * Medium priority because it only has index, not concentrations
     */
    override fun getFeaturePriorityForLocation(
        location: Location,
        feature: SourceFeature,
    ): Int {
        return when {
            isFeatureSupportedForLocation(location, feature) -> PRIORITY_MEDIUM
            else -> PRIORITY_NONE
        }
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

    private fun getPollen(
        location: Location,
        result: RecosanteResult,
    ): PollenWrapper? {
        if (result.raep?.indice?.details.isNullOrEmpty()) {
            // Don’t throw an error if empty or null
            // This can happen when the weekly bulletin has not been emitted yet on Friday
            // See also bug #804
            return null
        }

        val dayList = mutableListOf<Date>()
        if (result.raep.validity?.start != null && result.raep.validity.end != null) {
            var startDate = result.raep.validity.start.toDateNoHour(location.timeZone)
            val endDate = result.raep.validity.end.toDateNoHour(location.timeZone)
            if (startDate != null && endDate != null) {
                var i = 0
                while (true) {
                    ++i
                    if (i > 10 || startDate == endDate) {
                        // End the loop if we ran for more than 10 days (means something went wrong)
                        break
                    } else {
                        dayList.add(startDate!!)
                        startDate = startDate.toCalendarWithTimeZone(location.timeZone).apply {
                            add(Calendar.DAY_OF_MONTH, 1)
                        }.time
                    }
                }
            } else {
                dayList.add(Date().toTimezoneNoHour(location.timeZone))
            }
        } else {
            dayList.add(Date().toTimezoneNoHour(location.timeZone))
        }

        return PollenWrapper(
            dailyForecast = getPollen(result.raep.indice.details).let { pollenData ->
                dayList.associateWith { pollenData }
            }
        )
    }

    private fun getPollen(details: List<RecosanteRaepIndiceDetail>): Pollen {
        var alder: Int? = null
        var ash: Int? = null
        var birch: Int? = null
        var chestnut: Int? = null
        var cypress: Int? = null
        var grass: Int? = null
        var hazel: Int? = null
        var hornbeam: Int? = null
        var linden: Int? = null
        var mugwort: Int? = null
        var oak: Int? = null
        var olive: Int? = null
        var plane: Int? = null
        var plantain: Int? = null
        var poplar: Int? = null
        var ragweed: Int? = null
        var sorrel: Int? = null
        var urticaceae: Int? = null
        var willow: Int? = null

        details
            .forEach { p ->
                when (p.label) {
                    "ambroisies" -> ragweed = p.indice.value
                    "armoises" -> mugwort = p.indice.value
                    "aulne" -> alder = p.indice.value
                    "bouleau" -> birch = p.indice.value
                    "charme" -> hornbeam = p.indice.value
                    "chataignier" -> chestnut = p.indice.value
                    "chene" -> oak = p.indice.value
                    "cypres" -> cypress = p.indice.value
                    "frene" -> ash = p.indice.value
                    "graminees" -> grass = p.indice.value
                    "noisetier" -> hazel = p.indice.value
                    "olivier" -> olive = p.indice.value
                    "peuplier" -> poplar = p.indice.value
                    "plantain" -> plantain = p.indice.value
                    "platane" -> plane = p.indice.value
                    "rumex" -> sorrel = p.indice.value
                    "saule" -> willow = p.indice.value
                    "tilleul" -> linden = p.indice.value
                    "urticacees" -> urticaceae = p.indice.value
                }
            }

        return Pollen(
            alder = alder,
            ash = ash,
            birch = birch,
            chestnut = chestnut,
            cypress = cypress,
            grass = grass,
            hazel = hazel,
            hornbeam = hornbeam,
            linden = linden,
            mugwort = mugwort,
            oak = oak,
            olive = olive,
            plane = plane,
            plantain = plantain,
            poplar = poplar,
            ragweed = ragweed,
            sorrel = sorrel,
            urticaceae = urticaceae,
            willow = willow
        )
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
                keyboardType = KeyboardType.Uri,
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
                keyboardType = KeyboardType.Uri,
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
            timeZone = TimeZone.getTimeZone("Europe/Paris"),
            countryCode = "FR",
            pollenSource = id
        )
    )

    companion object {
        private const val GEO_BASE_URL = "https://geo.api.gouv.fr/"
        private const val RECOSANTE_BASE_URL = "https://api.recosante.beta.gouv.fr/"
    }
}
