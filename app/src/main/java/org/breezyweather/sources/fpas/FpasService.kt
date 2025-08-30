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
            WeatherWrapper(
                alertList = alertResultList.filterIsInstance<CapAlert>().mapIndexedNotNull { index, capAlert ->
                    // Filter out alert cancellations
                    if (!capAlert.msgType?.value.equals("Cancel", ignoreCase = true)) {
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
                                    description = formatAlertText(
                                        it.senderName?.value,
                                        it.description?.value
                                    ),
                                    instruction = it.instruction?.value,
                                    source = it.senderName?.value,
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
                },
                failedFeatures = if (someAlertsFailed) {
                    mapOf(SourceFeature.ALERT to InvalidOrIncompleteDataException())
                } else {
                    null
                }
            )
        }
    }

    // apply formatting to alert text based on source
    private fun formatAlertText(
        source: String?,
        text: String?,
    ): String {
        var result: String
        if (text.isNullOrEmpty()) {
            return ""
        }
        result = text
        if (!source.isNullOrEmpty()) {
            if (source.startsWith("NWS ", ignoreCase = true) ||
                source.equals("National Weather Service", ignoreCase = true)
            ) {
                // Look for SINGLE line breaks surrounded by letters, numbers, and punctuation.
                val regex = Regex("""([0-9A-Za-z.,]) *\n([0-9A-Za-z])""")
                result = regex.replace(result, "$1 $2")
            }
        }
        return result.trim()
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
    }
}
