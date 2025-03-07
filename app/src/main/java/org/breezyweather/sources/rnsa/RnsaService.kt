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

package org.breezyweather.sources.rnsa

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
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.csv.Csv
import okhttp3.OkHttpClient
import okhttp3.Request
import org.breezyweather.R
import org.breezyweather.common.exceptions.InvalidOrIncompleteDataException
import org.breezyweather.common.exceptions.UnsupportedFeatureException
import org.breezyweather.common.extensions.code
import org.breezyweather.common.extensions.currentLocale
import org.breezyweather.common.preference.EditTextPreference
import org.breezyweather.common.preference.Preference
import org.breezyweather.common.source.ConfigurableSource
import org.breezyweather.common.source.HttpSource
import org.breezyweather.common.source.PollenIndexSource
import org.breezyweather.common.source.WeatherSource
import org.breezyweather.domain.settings.SourceConfigStore
import org.breezyweather.sources.rnsa.csv.RnsaDepartement
import java.util.Locale
import javax.inject.Inject

/**
 * RNSA pollen service.
 */
class RnsaService @Inject constructor(
    @ApplicationContext context: Context,
    private val okHttpClient: OkHttpClient,
) : HttpSource(), WeatherSource, PollenIndexSource, ConfigurableSource {

    override val id = "rnsa"
    override val name = "Réseau National de Surveillance Aérobiologique (" +
        Locale(context.currentLocale.code, "FR").displayCountry +
        ")"
    override val continent = SourceContinent.EUROPE
    override val privacyPolicyUrl = "https://www.pollens.fr/informations-legales"

    private val csvSerializer: Csv
        get() = Csv {
            hasHeaderRecord = true
            delimiter = ';'
            recordSeparator = "\r\n"
            ignoreUnknownColumns = true
        }

    override val supportedFeatures = mapOf(
        SourceFeature.POLLEN to "Le Réseau National de Surveillance Aérobiologique (RNSA)"
    )
    override fun isFeatureSupportedForLocation(
        location: Location,
        feature: SourceFeature,
    ): Boolean {
        return !location.countryCode.isNullOrEmpty() &&
            location.countryCode.equals("FR", ignoreCase = true) &&
            !location.admin2Code.isNullOrEmpty()
    }

    override fun requestWeather(
        context: Context,
        location: Location,
        requestedFeatures: List<SourceFeature>,
    ): Observable<WeatherWrapper> {
        val request = Request.Builder().url(BASE_URL + "docs/ecosante.csv").build()
        return Observable.fromCallable {
            okHttpClient.newCall(request).execute()
        }.map { response ->
            val originalBody = response.body?.string()
            if (originalBody.isNullOrEmpty()) {
                throw InvalidOrIncompleteDataException()
            }

            // Remove variable date in first column, to be able to parse it
            val bodyWithHeaderCorrected = "number" + originalBody.substring(10)

            val departements = csvSerializer.decodeFromString(
                ListSerializer(RnsaDepartement.serializer()),
                bodyWithHeaderCorrected
            )

            val departementNumber = location.admin2Code.let {
                if (it.equals("2A", ignoreCase = true) || it.equals("2B", ignoreCase = true)) "20" else it
            }?.toIntOrNull()?.toString() // Remove padding 0 if present
            val departement = departements.firstOrNull {
                it.number == departementNumber
            }
            if (departement == null) {
                throw UnsupportedFeatureException()
            }

            WeatherWrapper(
                pollen = PollenWrapper(
                    current = getPollen(departement)
                )
            )
        }
    }

    private fun getPollen(departement: RnsaDepartement): Pollen {
        return Pollen(
            alder = departement.aulne,
            ash = departement.frene,
            birch = departement.bouleau,
            chestnut = departement.chataignier,
            cypress = departement.cypres,
            grass = departement.graminees,
            hazel = departement.noisetier,
            hornbeam = departement.charme,
            linden = departement.tilleul,
            mugwort = departement.armoises,
            oak = departement.chene,
            olive = departement.olivier,
            plane = departement.platane,
            plantain = departement.plantain,
            poplar = departement.peuplier,
            ragweed = departement.ambroisies,
            sorrel = departement.rumex,
            urticaceae = departement.urticacees,
            willow = departement.saule
        )
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
        get() = config.getString("instance", null) ?: BASE_URL

    override fun getPreferences(context: Context): List<Preference> {
        return listOf(
            EditTextPreference(
                titleId = R.string.settings_weather_source_rnsa_instance,
                summary = { _, content ->
                    content.ifEmpty {
                        BASE_URL
                    }
                },
                content = if (instance != BASE_URL) instance else null,
                placeholder = BASE_URL,
                regex = EditTextPreference.URL_REGEX,
                regexError = context.getString(R.string.settings_source_instance_invalid),
                keyboardType = KeyboardType.Uri,
                onValueChanged = {
                    instance = if (it == BASE_URL) null else it.ifEmpty { null }
                }
            )
        )
    }

    override val pollenLabels = R.array.pollen_recosante_levels
    override val pollenColors = R.array.pollen_recosante_level_colors

    override val testingLocations = listOf<Location>()

    companion object {
        private const val BASE_URL = "https://www.pollens.fr/"
    }
}
