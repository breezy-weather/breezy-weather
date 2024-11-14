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

package org.breezyweather.sources.wmosevereweather

import android.content.Context
import android.graphics.Color
import breezyweather.domain.weather.model.Alert
import breezyweather.domain.weather.model.AlertSeverity
import breezyweather.domain.weather.wrappers.SecondaryWeatherWrapper
import org.breezyweather.BreezyWeather
import org.breezyweather.common.extensions.capitalize
import org.breezyweather.common.extensions.code
import org.breezyweather.common.extensions.codeWithCountry
import org.breezyweather.common.extensions.currentLocale
import org.breezyweather.sources.common.xml.CapAlert
import org.breezyweather.sources.wmosevereweather.json.WmoSevereWeatherAlertResult
import java.util.Date

fun convert(
    alertResult: WmoSevereWeatherAlertResult,
    client: WmoSevereWeatherXmlApi,
    context: Context,
): SecondaryWeatherWrapper {
    return SecondaryWeatherWrapper(
        alertList = alertResult.features
            ?.filter {
                it.properties != null &&
                    (it.properties.expires == null || it.properties.expires > Date())
            }?.map {
                val severity = AlertSeverity.getInstance(it.properties!!.s)
                val originalAlert = Alert(
                    alertId = (
                        it.id?.ifEmpty { null }
                            ?: it.properties.identifier?.ifEmpty { null }
                            ?: it.properties.capurl?.ifEmpty { null }
                            ?: it.properties.url
                        )!!,
                    startDate = it.properties.onset ?: it.properties.effective ?: it.properties.sent,
                    endDate = it.properties.expires,
                    headline = it.properties.event?.capitalize(),
                    description = it.properties.description,
                    severity = AlertSeverity.getInstance(it.properties.s),
                    color = when (severity) {
                        AlertSeverity.EXTREME -> Color.rgb(212, 45, 65)
                        AlertSeverity.SEVERE -> Color.rgb(240, 140, 17)
                        AlertSeverity.MODERATE -> Color.rgb(244, 207, 0)
                        AlertSeverity.MINOR -> Color.rgb(57, 156, 199)
                        else -> Color.rgb(130, 168, 223)
                    }
                )

                // Use URL to get more info (description, instruction, translations)
                try {
                    val urlToLoad = if (it.properties.rlink.isNullOrEmpty() && it.properties.capurl.isNullOrEmpty()) {
                        null
                    } else if (it.properties.capurl.isNullOrEmpty()) {
                        it.properties.rlink
                    } else if (it.properties.rlink.isNullOrEmpty()) {
                        it.properties.capurl
                    } else if (it.properties.rlink.contains("sa-ncm-")) {
                        // Source for this hack: https://severeweather.wmo.int/js/new-layout.js
                        if (context.currentLocale.code.startsWith("ar")) {
                            it.properties.rlink
                        } else {
                            it.properties.capurl
                        }
                    } else {
                        null
                    }

                    if (!urlToLoad.isNullOrEmpty()) {
                        val xmlAlert = client.getAlert("v2/cap-alerts/$urlToLoad").execute().body()

                        val selectedInfo = if (xmlAlert?.info.isNullOrEmpty()) {
                            null
                        } else if (xmlAlert!!.info!!.size == 1) {
                            xmlAlert.info!![0]
                        } else {
                            val notNullLanguageInfo = xmlAlert.info!!.filter { info ->
                                info.language?.value != null
                            }

                            if (notNullLanguageInfo.isEmpty()) {
                                xmlAlert.info.first() // Arbitrarily takes the first
                            } else {
                                notNullLanguageInfo.firstOrNull { info ->
                                    info.language!!.value!!.lowercase() ==
                                        context.currentLocale.codeWithCountry.lowercase()
                                } ?: xmlAlert.info.firstOrNull { info ->
                                    info.language!!.value!!.lowercase().startsWith(context.currentLocale.code)
                                } ?: xmlAlert.info.firstOrNull { info ->
                                    info.language!!.value!!.lowercase().startsWith("en")
                                } ?: xmlAlert.info.first() // Arbitrarily takes the first
                            }
                        }

                        alertFromInfo(xmlAlert, selectedInfo, originalAlert)
                    } else if (it.properties.rlink.isNullOrEmpty() && it.properties.capurl.isNullOrEmpty()) {
                        originalAlert
                    } else {
                        // "else" case from urlToLoad
                        // The only way to know which URL contains which language… is to load
                        // both URL. Source: https://severeweather.wmo.int/js/new-layout.js
                        val capurlAlert = client.getAlert("v2/cap-alerts/${it.properties.capurl}").execute().body()
                        val rlinkAlert = client.getAlert("v2/cap-alerts/${it.properties.rlink}").execute().body()

                        // There is only one Info block when there are two links
                        val capurlLanguage = capurlAlert?.info?.getOrNull(0)?.language?.value?.lowercase()
                        val rlinkLanguage = rlinkAlert?.info?.getOrNull(0)?.language?.value?.lowercase()

                        val selectedAlert = if (capurlLanguage.isNullOrEmpty() || rlinkLanguage.isNullOrEmpty()) {
                            capurlAlert
                        } else if (capurlLanguage == context.currentLocale.codeWithCountry.lowercase()) {
                            capurlAlert
                        } else if (rlinkLanguage == context.currentLocale.codeWithCountry.lowercase()) {
                            rlinkAlert
                        } else if (capurlLanguage.startsWith(context.currentLocale.code)) {
                            capurlAlert
                        } else if (rlinkLanguage.startsWith(context.currentLocale.code)) {
                            rlinkAlert
                        } else if (capurlLanguage.startsWith("en")) {
                            capurlAlert
                        } else if (rlinkLanguage.startsWith("en")) {
                            rlinkAlert
                        } else {
                            capurlAlert // Arbitrarily takes capurl
                        }

                        alertFromInfo(selectedAlert, selectedAlert?.info?.getOrNull(0), originalAlert)
                    }
                } catch (e: Throwable) {
                    if (BreezyWeather.instance.debugMode) {
                        throw e
                    } else {
                        e.printStackTrace()
                    }

                    originalAlert
                }
            }
    )
}

fun alertFromInfo(
    xmlAlert: CapAlert?,
    selectedInfo: CapAlert.Info?,
    originalAlert: Alert,
): Alert {
    return selectedInfo?.let { info ->
        originalAlert.copy(
            alertId = xmlAlert!!.identifier?.value ?: originalAlert.alertId,
            startDate = info.onset?.value ?: info.effective?.value ?: originalAlert.startDate ?: xmlAlert.sent?.value,
            endDate = info.expires?.value ?: originalAlert.endDate,
            headline = info.headline?.value ?: info.event?.value ?: originalAlert.headline,
            description = info.description?.value ?: originalAlert.description,
            instruction = info.instruction?.value ?: originalAlert.instruction,
            source = info.senderName?.value
        )
    } ?: originalAlert
}
