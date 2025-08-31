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

package org.breezyweather.sources.ncdr

import android.content.Context
import android.graphics.Color
import breezyweather.domain.location.model.Location
import breezyweather.domain.source.SourceFeature
import breezyweather.domain.weather.model.Alert
import breezyweather.domain.weather.reference.AlertSeverity
import breezyweather.domain.weather.wrappers.WeatherWrapper
import com.google.maps.android.model.LatLng
import dagger.hilt.android.qualifiers.ApplicationContext
import io.reactivex.rxjava3.core.Observable
import org.breezyweather.common.exceptions.InvalidLocationException
import org.breezyweather.common.exceptions.InvalidOrIncompleteDataException
import org.breezyweather.common.exceptions.WeatherException
import org.breezyweather.sources.common.xml.CapAlert
import org.breezyweather.sources.nlsc.NlscApi
import retrofit2.Retrofit
import java.util.Objects
import javax.inject.Inject
import javax.inject.Named

class NcdrService @Inject constructor(
    @ApplicationContext context: Context,
    @Named("XmlClient") xmlClient: Retrofit.Builder,
) : NcdrServiceStub(context) {

    override val privacyPolicyUrl = "https://ncdr.nat.gov.tw/Page?itemid=40&mid=7"

    private val mNcdrApi by lazy {
        xmlClient
            .baseUrl(NCDR_BASE_URL)
            .build()
            .create(NcdrApi::class.java)
    }

    private val mNlscApi by lazy {
        xmlClient
            .baseUrl(NLSC_BASE_URL)
            .build()
            .create(NlscApi::class.java)
    }

    override val attributionLinks = mapOf(
        weatherAttribution to "https://ncdr.nat.gov.tw/"
    )

    override fun requestWeather(
        context: Context,
        location: Location,
        requestedFeatures: List<SourceFeature>,
    ): Observable<WeatherWrapper> {
        val legacyTownshipCode = location.parameters.getOrElse(id) { null }?.getOrElse("legacyTownshipCode") { null }
        if (legacyTownshipCode.isNullOrEmpty()) {
            return Observable.error(InvalidLocationException())
        }

        val alerts = mNcdrApi.getAlerts().execute().body() ?: return Observable.error(WeatherException())
        val cwaAlerts = alerts.entries?.filter { it.author.name.value == "中央氣象署" } ?: return Observable.empty()

        var someAlertsFailed = false
        return Observable.zip(
            cwaAlerts.map { cwaAlert ->
                mNcdrApi.getAlert(cwaAlert.link.href).onErrorResumeNext {
                    someAlertsFailed = true
                    Observable.just(CapAlert())
                }
            }
        ) { alertResultList ->
            val alertList = mutableListOf<Alert>()
            alertResultList.filterIsInstance<CapAlert>().forEach { alert ->
                alert.info?.forEach {
                    if (it.containsGeocode("Taiwan_Geocode_103", legacyTownshipCode) ||
                        it.containsPoint(LatLng(location.latitude, location.longitude))
                    ) {
                        val severity = when (it.severity?.value) {
                            "Extreme" -> AlertSeverity.EXTREME
                            "Severe" -> AlertSeverity.SEVERE
                            "Moderate" -> AlertSeverity.MODERATE
                            "Minor" -> AlertSeverity.MINOR
                            else -> AlertSeverity.UNKNOWN
                        }

                        // Use the color provided in the CAP Alert where available.
                        val websiteColor = it.parameters?.firstOrNull { parameter ->
                            parameter.valueName?.value.equals("website_color")
                        }?.value?.value
                        val color = if (!websiteColor.isNullOrEmpty()) {
                            val components = websiteColor.split(",")
                            if (components.size == 3) {
                                Color.rgb(components[0].toInt(), components[1].toInt(), components[2].toInt())
                            } else {
                                Alert.colorFromSeverity(severity)
                            }
                        } else {
                            Alert.colorFromSeverity(severity)
                        }

                        var headline = it.headline?.value?.trim()

                        // For Extremely Heavy Rain Advisories, replace the headline with severity level.
                        if (headline == "豪雨特報") {
                            val severityLevel = it.parameters?.firstOrNull { parameter ->
                                parameter.valueName?.value == "severity_level"
                            }?.value?.value
                            if (!severityLevel.isNullOrEmpty()) {
                                headline = severityLevel + "特報"
                            }
                        }

                        val startDate = it.onset?.value ?: it.effective?.value ?: alert.sent?.value
                        alertList.add(
                            Alert(
                                alertId = alert.identifier?.value
                                    ?: Objects.hash(headline, severity, startDate).toString(),
                                startDate = startDate,
                                endDate = it.expires?.value,
                                headline = headline,
                                description = it.description?.value?.trim(),
                                instruction = it.instruction?.value?.trim(),
                                source = it.senderName?.value?.trim(),
                                severity = severity,
                                color = color
                            )
                        )
                    }
                }
            }

            WeatherWrapper(
                alertList = alertList,
                failedFeatures = if (someAlertsFailed) {
                    mapOf(SourceFeature.ALERT to InvalidOrIncompleteDataException())
                } else {
                    null
                }
            )
        }
    }

    override fun needsLocationParametersRefresh(
        location: Location,
        coordinatesChanged: Boolean,
        features: List<SourceFeature>,
    ): Boolean {
        if (coordinatesChanged) return true

        val legacyTownshipCode = location.parameters.getOrElse(id) { null }?.getOrElse("legacyTownshipCode") { null }

        return legacyTownshipCode.isNullOrEmpty()
    }

    override fun requestLocationParameters(context: Context, location: Location): Observable<Map<String, String>> {
        return mNlscApi.getLocationCodes(
            lon = location.longitude,
            lat = location.latitude
        ).map { locationCodes ->
            if (locationCodes.townshipCode?.value.isNullOrEmpty()) {
                throw InvalidLocationException()
            }
            mapOf(
                // Currently not used. When used, update the throw InvalidLocationException condition above
                // "countyCode" to locationCodes.countyCode.value,
                // "townshipCode" to locationCodes.townshipCode.value,
                // "villageCode" to locationCodes.villageCode.value,
                "legacyTownshipCode" to getLegacyTownshipCode(locationCodes.townshipCode!!.value)
            )
        }
    }

    // This function converts the current standard 8-digit geocode
    // to the 7-digit legacy geocodes still used in CWA's CAP Alerts
    private fun getLegacyTownshipCode(
        code: String,
    ): String {
        var output = code
        val municipalityCodePattern = Regex("""(6\d)000(\d{2})0""")
        val townshipCodePattern = Regex("""(1\d{6})0""")
        val outlyingCodePattern = Regex("""(09\d{5})0""")
        if (municipalityCodePattern.matches(code)) {
            output = code.replace(municipalityCodePattern, "$10$200")
        } else if (townshipCodePattern.matches(code)) {
            output = code.replace(townshipCodePattern, "$1")
        } else if (outlyingCodePattern.matches(code)) {
            output = code.replace(outlyingCodePattern, "$1")
        }
        return output
    }

    companion object {
        private const val NCDR_BASE_URL = "https://alerts.ncdr.nat.gov.tw/"
        private const val NLSC_BASE_URL = "https://api.nlsc.gov.tw/"
    }
}
