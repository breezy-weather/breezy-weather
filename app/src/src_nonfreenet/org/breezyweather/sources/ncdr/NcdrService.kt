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
import breezyweather.domain.source.SourceContinent
import breezyweather.domain.source.SourceFeature
import breezyweather.domain.weather.model.Alert
import breezyweather.domain.weather.model.AlertSeverity
import breezyweather.domain.weather.wrappers.WeatherWrapper
import com.google.maps.android.model.LatLng
import com.google.maps.android.model.LatLngBounds
import dagger.hilt.android.qualifiers.ApplicationContext
import io.reactivex.rxjava3.core.Observable
import org.breezyweather.common.exceptions.InvalidLocationException
import org.breezyweather.common.extensions.code
import org.breezyweather.common.extensions.currentLocale
import org.breezyweather.common.source.HttpSource
import org.breezyweather.common.source.LocationParametersSource
import org.breezyweather.common.source.WeatherSource
import org.breezyweather.common.source.WeatherSource.Companion.PRIORITY_HIGHEST
import org.breezyweather.common.source.WeatherSource.Companion.PRIORITY_NONE
import org.breezyweather.sources.ncdr.xml.NcdrAlertsResult
import org.breezyweather.sources.nlsc.NlscApi
import retrofit2.Retrofit
import java.util.Locale
import javax.inject.Inject
import javax.inject.Named
import kotlin.lazy

class NcdrService @Inject constructor(
    @ApplicationContext context: Context,
    @Named("XmlClient") xmlClient: Retrofit.Builder,
) : HttpSource(), WeatherSource, LocationParametersSource {
    override val id = "ncdr"
    override val name by lazy {
        with(context.currentLocale.code) {
            when {
                startsWith("zh") -> "國家災害防救科技中心"
                else -> "NCDR"
            }
        } +
            " (${Locale(context.currentLocale.code, "TW").displayCountry})"
    }
    override val continent = SourceContinent.ASIA
    override val privacyPolicyUrl = "https://ncdr.nat.gov.tw/Page?itemid=40&mid=7"

    private val mNcdrApi by lazy {
        xmlClient
            .baseUrl(NCDR_BASE_URL)
            .build()
            .create(NcdrApi::class.java)
    }

    val mNlscApi by lazy {
        xmlClient
            .baseUrl(NLSC_BASE_URL)
            .build()
            .create(NlscApi::class.java)
    }

    private val weatherAttribution by lazy {
        with(context.currentLocale.code) {
            when {
                startsWith("zh") -> "國家災害防救科技中心"
                else -> "National Science and Technology Center for Disaster Reduction"
            }
        }
    }
    override val supportedFeatures = mapOf(
        SourceFeature.ALERT to weatherAttribution
    )
    override val attributionLinks = mapOf(
        weatherAttribution to "https://ncdr.nat.gov.tw/"
    )

    override fun isFeatureSupportedForLocation(location: Location, feature: SourceFeature): Boolean {
        val latLng = LatLng(location.latitude, location.longitude)
        return location.countryCode.equals("TW", ignoreCase = true) ||
            TAIWAN_BBOX.contains(latLng) ||
            PENGHU_BBOX.contains(latLng) ||
            KINMEN_BBOX.contains(latLng) ||
            WUQIU_BBOX.contains(latLng) ||
            MATSU_BBOX.contains(latLng)
    }

    override fun getFeaturePriorityForLocation(
        location: Location,
        feature: SourceFeature,
    ): Int {
        return when {
            isFeatureSupportedForLocation(location, feature) -> PRIORITY_HIGHEST
            else -> PRIORITY_NONE
        }
    }

    override fun requestWeather(
        context: Context,
        location: Location,
        requestedFeatures: List<SourceFeature>,
    ): Observable<WeatherWrapper> {
        val legacyTownshipCode = location.parameters.getOrElse(id) { null }?.getOrElse("legacyTownshipCode") { null }
        if (legacyTownshipCode.isNullOrEmpty()) {
            return Observable.error(InvalidLocationException())
        }

        val alerts = mNcdrApi.getAlerts().execute().body()
        val cwaAlerts = alerts?.entries?.filter {
            it.author.name.value == "中央氣象署"
        }

        return Observable.just(
            WeatherWrapper(
                alertList = convert(location, legacyTownshipCode, cwaAlerts)
            )
        )
    }

    internal fun convert(
        location: Location,
        legacyTownshipCode: String,
        cwaAlerts: List<NcdrAlertsResult.Entry>?,
    ): List<Alert> {
        val alertList = mutableListOf<Alert>()
        cwaAlerts?.forEach { cwaAlert ->
            val alert = mNcdrApi.getAlert(cwaAlert.link.href).execute().body()
            alert?.info?.forEach {
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

                    alertList.add(
                        Alert(
                            alertId = alert.identifier?.value ?: cwaAlert.link.href,
                            startDate = it.onset?.value ?: it.effective?.value ?: alert.sent?.value,
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
        return alertList
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
        val locationCodes = mNlscApi.getLocationCodes(
            lon = location.longitude,
            lat = location.latitude
        ).execute().body()

        if (locationCodes?.countyCode?.value.isNullOrEmpty() ||
            locationCodes.townshipCode?.value.isNullOrEmpty() ||
            locationCodes.villageCode?.value.isNullOrEmpty()
        ) {
            throw InvalidLocationException()
        }

        return Observable.just(
            mapOf(
                // Currently not used
                // "countyCode" to locationCodes.countyCode.value,
                // "townshipCode" to locationCodes.townshipCode.value,
                // "villageCode" to locationCodes.villageCode.value,
                "legacyTownshipCode" to getLegacyTownshipCode(locationCodes.townshipCode.value)
            )
        )
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

    override val testingLocations: List<Location> = emptyList()

    companion object {
        private const val NCDR_BASE_URL = "https://alerts.ncdr.nat.gov.tw/"
        private const val NLSC_BASE_URL = "https://api.nlsc.gov.tw/"

        private val TAIWAN_BBOX = LatLngBounds.parse(119.99690416, 21.756143532, 122.10915909, 25.633378776)
        private val PENGHU_BBOX = LatLngBounds.parse(119.314301816, 23.186561404, 119.726986388, 23.810692086)
        private val KINMEN_BBOX = LatLngBounds.parse(118.137979837, 24.160255444, 118.505977425, 24.534228163)
        private val WUQIU_BBOX = LatLngBounds.parse(119.443195363, 24.97760013, 119.479213453, 24.999614154)
        private val MATSU_BBOX = LatLngBounds.parse(119.908905081, 25.940995457, 120.511750672, 26.385275262)
    }
}
