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
import breezyweather.domain.location.model.Location
import breezyweather.domain.source.SourceContinent
import breezyweather.domain.source.SourceFeature
import breezyweather.domain.weather.model.Alert
import breezyweather.domain.weather.model.AlertSeverity
import breezyweather.domain.weather.wrappers.WeatherWrapper
import dagger.hilt.android.qualifiers.ApplicationContext
import io.reactivex.rxjava3.core.Observable
import org.breezyweather.BreezyWeather
import org.breezyweather.common.extensions.capitalize
import org.breezyweather.common.extensions.code
import org.breezyweather.common.extensions.codeWithCountry
import org.breezyweather.common.extensions.currentLocale
import org.breezyweather.common.source.HttpSource
import org.breezyweather.common.source.WeatherSource
import org.breezyweather.sources.common.xml.CapAlert
import org.breezyweather.sources.wmosevereweather.json.WmoSevereWeatherAlertResult
import retrofit2.Retrofit
import java.util.Date
import javax.inject.Inject
import javax.inject.Named

/**
 * World Meteorological Organization Severe Weather Information Centre (WMO SWIC)
 * Supports severe weather from many issuing organizations
 *
 * Based on WFS from SWIC v3.0 that was released on 2024-03-29
 */
class WmoSevereWeatherService @Inject constructor(
    @ApplicationContext context: Context,
    @Named("JsonClient") jsonClient: Retrofit.Builder,
    @Named("XmlClient") xmlClient: Retrofit.Builder,
) : HttpSource(), WeatherSource {

    override val id = "wmosevereweather"
    override val name by lazy {
        with(context.currentLocale.code) {
            when {
                // Missing arabic abbreviation for WMO
                startsWith("ar") -> "WMO مركز معلومات الطقس القاسي"
                startsWith("eo") -> "MOM Severe Weather Information Centre"
                startsWith("es") -> "OMM Centro de Información de Tiempo Severo"
                startsWith("fr") -> "OMM Centre d’Information des Phénomènes Dangereux"
                startsWith("it") -> "OMM Eventi Meteorologici Estremi"
                startsWith("ko") -> "WMO 위험기상정보센터"
                startsWith("pl") -> "WMO Centrum Informacji o Groźnych Zjawiskach Pogodowych"
                startsWith("pt") -> "OMM Centro de Informação Tempo Severo"
                startsWith("ru") -> "ВМО Информационный центр неблагоприятных погодных условий"
                equals("zh-tw") || equals("zh-hk") || equals("zh-mo") -> "世界氣象組職惡劣天氣信息中心"
                startsWith("zh") -> "世界气象组织恶劣天气信息中心"
                else -> "WMO Severe Weather Information Centre"
            }
        }
    }
    override val continent = SourceContinent.WORLDWIDE
    override val privacyPolicyUrl = "https://wmo.int/privacy-policy"
    override val color = Color.rgb(31, 78, 149)

    private val mAlertsJsonApi by lazy {
        jsonClient
            .baseUrl(WMO_ALERTS_BASE_URL)
            .build()
            .create(WmoSevereWeatherJsonApi::class.java)
    }
    private val mAlertsXmlApi by lazy {
        xmlClient
            .baseUrl(WMO_ALERTS_BASE_URL)
            .build()
            .create(WmoSevereWeatherXmlApi::class.java)
    }

    override val supportedFeatures = mapOf(
        SourceFeature.ALERT to "Hong Kong Observatory on behalf of WMO + 137 issuing organizations " +
            "https://severeweather.wmo.int/sources.html"
    )

    override fun requestWeather(
        context: Context,
        location: Location,
        requestedFeatures: List<SourceFeature>,
    ): Observable<WeatherWrapper> {
        return mAlertsJsonApi.getAlerts(
            typeName = "local_postgis:postgis_geojsons",
            // cqlFilter = "INTERSECTS(wkb_geometry, POINT (${location.latitude} ${location.longitude})) AND " +
            //     "(row_type EQ 'POLYGON' OR row_type EQ 'MULTIPOLYGON' OR row_type EQ 'POINT')"
            cqlFilter = "INTERSECTS(wkb_geometry, POINT (${location.latitude} ${location.longitude})) AND " +
                "row_type NEQ 'BOUNDARY'"
        ).map {
            WeatherWrapper(
                alertList = getAlerts(it, mAlertsXmlApi, context)
            )
        }
    }

    private fun getAlerts(
        alertResult: WmoSevereWeatherAlertResult,
        client: WmoSevereWeatherXmlApi,
        context: Context,
    ): List<Alert>? {
        return alertResult.features
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
    }

    private fun alertFromInfo(
        xmlAlert: CapAlert?,
        selectedInfo: CapAlert.Info?,
        originalAlert: Alert,
    ): Alert {
        return selectedInfo?.let { info ->
            originalAlert.copy(
                alertId = xmlAlert!!.identifier?.value ?: originalAlert.alertId,
                startDate = info.onset?.value
                    ?: info.effective?.value
                    ?: originalAlert.startDate
                    ?: xmlAlert.sent?.value,
                endDate = info.expires?.value ?: originalAlert.endDate,
                headline = info.headline?.value ?: info.event?.value ?: originalAlert.headline,
                description = info.description?.value ?: originalAlert.description,
                instruction = info.instruction?.value ?: originalAlert.instruction,
                source = info.senderName?.value
            )
        } ?: originalAlert
    }

    /**
     * TODO: Probably needs a minimum of one location per country
     * Needs a way to mark tested countries as working as of <date of test>
     */
    override val testingLocations: List<Location> = emptyList()

    companion object {
        private const val WMO_ALERTS_BASE_URL = "https://severeweather.wmo.int/"
    }
}
