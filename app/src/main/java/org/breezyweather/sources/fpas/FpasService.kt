/*
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

package org.breezyweather.sources.fpas

import android.content.Context
import androidx.compose.ui.text.input.KeyboardType
import breezyweather.domain.location.model.Location
import breezyweather.domain.source.SourceContinent
import breezyweather.domain.source.SourceFeature
import breezyweather.domain.weather.model.Alert
import breezyweather.domain.weather.reference.AlertSeverity
import breezyweather.domain.weather.wrappers.WeatherWrapper
import com.google.maps.android.model.LatLng
import dagger.hilt.android.qualifiers.ApplicationContext
import io.reactivex.rxjava3.core.Observable
import org.breezyweather.R
import org.breezyweather.common.exceptions.InvalidOrIncompleteDataException
import org.breezyweather.common.exceptions.WeatherException
import org.breezyweather.common.extensions.code
import org.breezyweather.common.extensions.currentLocale
import org.breezyweather.common.preference.EditTextPreference
import org.breezyweather.common.preference.Preference
import org.breezyweather.common.source.ConfigurableSource
import org.breezyweather.common.source.HttpSource
import org.breezyweather.common.source.WeatherSource
import org.breezyweather.domain.settings.SourceConfigStore
import org.breezyweather.sources.common.xml.CapAlert
import retrofit2.Retrofit
import java.util.Objects
import javax.inject.Inject
import javax.inject.Named

class FpasService @Inject constructor(
    @ApplicationContext context: Context,
    @Named("JsonClient") jsonClient: Retrofit.Builder,
    @Named("XmlClient") xmlClient: Retrofit.Builder,
) : HttpSource(), WeatherSource, ConfigurableSource {
    override val id = "fpas"
    override val name = "FOSS Public Alert Server (beta)"
    override val continent = SourceContinent.WORLDWIDE
    override val privacyPolicyUrl = "https://invent.kde.org/webapps/foss-public-alert-server/-/wikis/Terms-of-Service"

    private val mJsonApi by lazy {
        jsonClient
            .baseUrl(instance!!)
            .build()
            .create(FpasJsonApi::class.java)
    }

    private val mXmlApi by lazy {
        xmlClient
            .baseUrl(instance!!)
            .build()
            .create(FpasXmlApi::class.java)
    }

    override val supportedFeatures = mapOf(
        SourceFeature.ALERT to name
    )
    override val attributionLinks = mapOf(
        name to "https://invent.kde.org/webapps/foss-public-alert-server/"
    )

    override fun requestWeather(
        context: Context,
        location: Location,
        requestedFeatures: List<SourceFeature>,
    ): Observable<WeatherWrapper> {
        // bbox of approx. 111m in each cardinal direction near the equator
        val alertUuids = mJsonApi.getAlerts(
            minLat = location.latitude - 0.001,
            maxLat = location.latitude + 0.001,
            minLon = location.longitude - 0.001,
            maxLon = location.longitude + 0.001
        ).execute().body() ?: return Observable.error(WeatherException())

        if (alertUuids.isEmpty()) return Observable.just(WeatherWrapper())

        var someAlertsFailed = false
        return Observable.zip(
            alertUuids.map { uuid ->
                mXmlApi.getAlert(uuid).onErrorResumeNext {
                    someAlertsFailed = true
                    Observable.just(CapAlert())
                }
            }
        ) { alertResultList ->

            // SPECIAL CASE: Roshydromet alert IDs for pre-filtering.
            val roshydrometAlerts = alertResultList.filterIsInstance<CapAlert>()
                .filter { it.sender?.value == "web@mecom.ru" }
                .mapNotNull { it.identifier?.value }

            val alertList = alertResultList.filterIsInstance<CapAlert>().mapIndexedNotNull { index, capAlert ->

                // flag for pre-filtering alerts
                var eligible = true

                // SPECIAL CASE: Roshydromet
                // Multilingual alerts belonging to the same event has the same ID apart from the suffix.
                // Mark alerts as ineligible based on user language preference.
                if (capAlert.sender?.value == "web@mecom.ru") {
                    var id = capAlert.identifier?.value ?: ""
                    if (id.endsWith(".EN") || id.endsWith(".RU")) {
                        val suffix = id.substring(id.length - 3)
                        id = id.substring(0, id.length - 3)
                        if (context.currentLocale.code.startsWith("ru", ignoreCase = true)) {
                            if (suffix == ".EN" && roshydrometAlerts.contains("$id.RU")) {
                                eligible = false
                            }
                        } else {
                            if (suffix == ".RU" && roshydrometAlerts.contains("$id.EN")) {
                                eligible = false
                            }
                        }
                    }
                }

                // Filter out canceled and other ineligible alerts based on pre-filtering
                if (!capAlert.msgType?.value.equals("Cancel", ignoreCase = true) && eligible) {
                    capAlert.getInfoForContext(context)?.let {
                        // Filter out non-meteorological alerts, past alerts,
                        // and alert whose polygons do not cover the requested location
                        if (it.category?.value.equals("Met", ignoreCase = true) &&
                            !it.urgency?.value.equals("Past", ignoreCase = true) &&
                            it.containsPoint(LatLng(location.latitude, location.longitude))
                        ) {
                            val severity = when (it.severity?.value) {
                                "Extreme" -> AlertSeverity.EXTREME
                                "Severe" -> AlertSeverity.SEVERE
                                "Moderate" -> AlertSeverity.MODERATE
                                "Minor" -> AlertSeverity.MINOR
                                else -> AlertSeverity.UNKNOWN
                            }
                            val title = it.event?.value ?: it.headline?.value
                            val start = it.onset?.value ?: it.effective?.value ?: capAlert.sent?.value
                            Alert(
                                alertId = capAlert.identifier?.value
                                    ?: alertUuids.getOrElse(index) {
                                        Objects.hash(title, severity, start).toString()
                                    },
                                startDate = start,
                                endDate = it.expires?.value,
                                headline = title,
                                description = it.formatAlertText(text = it.description?.value),
                                instruction = it.formatAlertText(text = it.instruction?.value),
                                source = it.senderName?.value ?: capAlert.sender?.value,
                                severity = severity,
                                color = Alert.colorFromSeverity(severity)
                            )
                        } else {
                            null
                        }
                    }
                } else {
                    null
                }
            }

            val excludedAlerts = mutableListOf<Pair<String, String>>()

            // SPECIAL CASE: For agencies with transmit separate multilingual alerts for a single event,
            // the following algo filters out by matching the same alert ID against source names.
            // This works for BMKG (Indonesia) and NMC (Saudi Arabia).
            // It may be extended to other agencies.

            MULTILINGUAL_SOURCES.forEach { set ->
                // Each `set` in MULTILINGUAL_SOURCES is a list of Kotlin maps,
                // for an agency that transmits multilingual alerts as separate files,
                // with a language code mapped to source name used in the alert

                // This is for storing IDs of alerts transmitted in each language
                val multiAlertIds = mutableMapOf<String, List<String>>()

                // Fill up lists of IDs, one language at a time
                set.forEach { entry ->
                    val lang = entry.key
                    val source = entry.value
                    multiAlertIds[lang] = alertList
                        .filter { it.source == source }
                        .map { it.alertId }
                }

                // Go through all the alert languages again,
                // and see if any (lang1) matches the user's language setting.
                // If we've come to the last language in the set
                // and nothing has matched up until that point,
                // we will consider it a fallback match nonetheless.
                var matched = false
                set.forEach { entry1 ->
                    val lang1 = entry1.key
                    if (context.currentLocale.code.startsWith(lang1, ignoreCase = true) ||
                        (!matched && lang1 == set.keys.last())
                    ) {
                        matched = true

                        // Go through the alert ID lists of all the other languages (lang2)
                        // along with their source names (source2).
                        // If an ID overlaps with an ID in our matched language (lang1),
                        // we will add the source2-ID pair into our exclusion list.
                        set.filter { it.key != lang1 }.forEach { entry2 ->
                            val lang2 = entry2.key
                            val source2 = entry2.value
                            excludedAlerts.addAll(
                                (multiAlertIds[lang2] ?: emptyList())
                                    .filter { multiAlertIds[lang1]?.contains(it) == true }
                                    .map { Pair(source2, it) }
                            )
                        }
                    }
                }
            }

            WeatherWrapper(
                alertList = alertList.filter { Pair(it.source, it.alertId) !in excludedAlerts },
                failedFeatures = if (someAlertsFailed) {
                    mapOf(SourceFeature.ALERT to InvalidOrIncompleteDataException())
                } else {
                    null
                }
            )
        }
    }

    // CONFIG
    private val config = SourceConfigStore(context, id)
    private var instance: String?
        set(value) {
            value?.let {
                config.edit().putString("instance", it).apply()
            } ?: config.edit().remove("instance").apply()
        }
        get() = config.getString("instance", FPAS_BASE_URL)

    override val isConfigured = true
    override val isRestricted = false

    override fun getPreferences(context: Context): List<Preference> {
        return listOf(
            EditTextPreference(
                titleId = R.string.settings_weather_source_fpas_instance,
                summary = { _, content ->
                    content.ifEmpty { FPAS_BASE_URL }
                },
                content = if (instance != FPAS_BASE_URL) instance else null,
                placeholder = FPAS_BASE_URL,
                regex = EditTextPreference.URL_REGEX,
                regexError = context.getString(R.string.settings_source_instance_invalid),
                keyboardType = KeyboardType.Uri,
                onValueChanged = {
                    instance = if (it == FPAS_BASE_URL) null else it.ifEmpty { null }
                }
            )
        )
    }

    companion object {
        private const val FPAS_BASE_URL = "https://alerts.kde.org/"

        // Each item in this list is a map of language to source name.
        // This is for identifying agencies that transmit separate multilingual alerts
        // under different source names for the same event.
        // NOTE: The fallback language should be listed last in each map.
        private val MULTILINGUAL_SOURCES = listOf(
            mapOf( // Indonesia: BMKG
                "id" to "Badan Meteorologi Klimatologi dan Geofisika",
                "en" to "Indonesia Agency for Meteorology, Climatology, and Geophysics"
            ),
            mapOf( // Saudi Arabia: NCM
                "ar" to "المركز-الوطني-للأرصاد",
                "en" to "NCM"
            )
        )
    }
}
