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

package org.breezyweather.sources.metie

import android.content.Context
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
import org.breezyweather.common.source.HttpSource
import org.breezyweather.common.source.LocationParametersSource
import org.breezyweather.common.source.ReverseGeocodingSource
import org.breezyweather.common.source.WeatherSource
import org.breezyweather.common.source.WeatherSource.Companion.PRIORITY_HIGHEST
import org.breezyweather.common.source.WeatherSource.Companion.PRIORITY_NONE
import org.breezyweather.sources.metie.json.MetIeHourly
import org.breezyweather.sources.metie.json.MetIeWarningResult
import retrofit2.Retrofit
import java.util.Locale
import javax.inject.Inject
import javax.inject.Named

/**
 * MET Éireann service
 */
class MetIeService @Inject constructor(
    @ApplicationContext context: Context,
    @Named("JsonClient") client: Retrofit.Builder,
) : HttpSource(), WeatherSource, ReverseGeocodingSource, LocationParametersSource {

    override val id = "metie"
    val countryName = Locale(context.currentLocale.code, "IE").displayCountry
    override val name = "MET Éireann".let {
        if (it.contains(countryName)) {
            it
        } else {
            "$it ($countryName)"
        }
    }
    override val continent = SourceContinent.EUROPE
    override val privacyPolicyUrl = "https://www.met.ie/about-us/privacy"

    private val mApi by lazy {
        client
            .baseUrl(MET_IE_BASE_URL)
            .build()
            .create(MetIeApi::class.java)
    }

    // Terms require: copyright + source + license (with link) + disclaimer + mention of modified data
    private val weatherAttribution = "Copyright Met Éireann. Source met.ie. This data is published under a " +
        "Commons Attribution 4.0 International (CC BY 4.0). Met Éireann does not accept any liability whatsoever " +
        "for any error or omission in the data, their availability, or for any loss or damage arising from their " +
        "use. ${context.getString(R.string.data_modified, context.getString(R.string.breezy_weather))}"
    override val supportedFeatures = mapOf(
        SourceFeature.FORECAST to weatherAttribution,
        SourceFeature.ALERT to weatherAttribution,
        SourceFeature.REVERSE_GEOCODING to weatherAttribution
    )
    override val attributionLinks = mapOf(
        "Met Éireann" to "https://www.met.ie/",
        "met.ie" to "https://www.met.ie/",
        "Creative Commons Attribution 4.0 International (CC BY 4.0)" to "https://creativecommons.org/licenses/by/4.0/"
    )

    override fun isFeatureSupportedForLocation(
        location: Location,
        feature: SourceFeature,
    ): Boolean {
        return location.countryCode.equals("IE", ignoreCase = true)
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
        val failedFeatures = mutableMapOf<SourceFeature, Throwable>()
        val forecast = if (SourceFeature.FORECAST in requestedFeatures) {
            mApi.getForecast(
                location.latitude,
                location.longitude
            ).onErrorResumeNext {
                failedFeatures[SourceFeature.FORECAST] = it
                Observable.just(emptyList())
            }
        } else {
            Observable.just(emptyList())
        }
        val alerts = if (SourceFeature.ALERT in requestedFeatures) {
            mApi.getWarnings().onErrorResumeNext {
                failedFeatures[SourceFeature.ALERT] = it
                Observable.just(MetIeWarningResult())
            }
        } else {
            Observable.just(MetIeWarningResult())
        }

        return Observable.zip(forecast, alerts) { forecastResult: List<MetIeHourly>, alertsResult: MetIeWarningResult ->
            WeatherWrapper(
                dailyForecast = if (SourceFeature.FORECAST in requestedFeatures) {
                    getDailyForecast(location, forecastResult)
                } else {
                    null
                },
                hourlyForecast = if (SourceFeature.FORECAST in requestedFeatures) {
                    getHourlyForecast(forecastResult)
                } else {
                    null
                },
                alertList = if (SourceFeature.ALERT in requestedFeatures) {
                    getAlertList(location, alertsResult.warnings?.national)
                } else {
                    null
                },
                failedFeatures = failedFeatures
            )
        }
    }

    override fun requestReverseGeocodingLocation(
        context: Context,
        location: Location,
    ): Observable<List<Location>> {
        return mApi.getReverseLocation(
            location.latitude,
            location.longitude
        ).map {
            val locationList = mutableListOf<Location>()
            if (it.city != "NO LOCATION SELECTED") {
                locationList.add(convert(context, location, it))
            }
            locationList
        }
    }

    // Location parameters
    override fun needsLocationParametersRefresh(
        location: Location,
        coordinatesChanged: Boolean,
        features: List<SourceFeature>,
    ): Boolean {
        if (regionsMapping.containsKey(location.admin2)) return false

        val currentRegion = location.parameters
            .getOrElse(id) { null }?.getOrElse("region") { null }

        return currentRegion.isNullOrEmpty()
    }

    override fun requestLocationParameters(
        context: Context,
        location: Location,
    ): Observable<Map<String, String>> {
        return mApi.getReverseLocation(
            location.latitude,
            location.longitude
        ).map {
            if (it.city != "NO LOCATION SELECTED" &&
                !it.county.isNullOrEmpty() &&
                regionsMapping.containsKey(it.county)
            ) {
                mapOf("region" to it.county)
            } else {
                throw InvalidLocationException()
            }
        }
    }

    override val testingLocations: List<Location> = emptyList()

    companion object {
        private const val MET_IE_BASE_URL = "https://prodapi.metweb.ie/"

        // Last checked: 2024-03-02 https://prodapi.metweb.ie/v2/warnings/regions
        val regionsMapping = mapOf(
            "All Counties" to "EI0",
            "All Sea Areas" to "EI8",
            "Carlow" to "EI01",
            "Cavan" to "EI02",
            "Clare" to "EI03",
            "Cork" to "EI04",
            "Donegal" to "EI06",
            "Dublin" to "EI07",
            "Galway" to "EI10",
            "Kerry" to "EI11",
            "Kildare" to "EI12",
            "Kilkenny" to "EI13",
            "Laois" to "EI15",
            "Leitrim" to "EI14",
            "Limerick" to "EI16",
            "Longford" to "EI18",
            "Louth" to "EI19",
            "Mayo" to "EI20",
            "Meath" to "EI21",
            "Monaghan" to "EI22",
            "Offaly" to "EI23",
            "Roscommon" to "EI24",
            "Sligo" to "EI25",
            "Tipperary" to "EI26",
            "Waterford" to "EI27",
            "Westmeath" to "EI29",
            "Wexford" to "EI30",
            "Wicklow" to "EI31"
        )
    }
}
