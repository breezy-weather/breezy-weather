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
import android.graphics.Color
import breezyweather.domain.location.model.Location
import breezyweather.domain.weather.wrappers.SecondaryWeatherWrapper
import breezyweather.domain.weather.wrappers.WeatherWrapper
import io.reactivex.rxjava3.core.Observable
import org.breezyweather.common.exceptions.InvalidLocationException
import org.breezyweather.common.source.HttpSource
import org.breezyweather.common.source.LocationParametersSource
import org.breezyweather.common.source.MainWeatherSource
import org.breezyweather.common.source.ReverseGeocodingSource
import org.breezyweather.common.source.SecondaryWeatherSource
import org.breezyweather.common.source.SecondaryWeatherSourceFeature
import org.breezyweather.sources.metie.json.MetIeHourly
import org.breezyweather.sources.metie.json.MetIeWarningResult
import retrofit2.Retrofit
import javax.inject.Inject

/**
 * MET Éireann service
 */
class MetIeService @Inject constructor(
    client: Retrofit.Builder
) : HttpSource(), MainWeatherSource, SecondaryWeatherSource,
    ReverseGeocodingSource, LocationParametersSource {

    override val id = "metie"
    override val name = "MET Éireann"
    override val privacyPolicyUrl = "https://www.met.ie/about-us/privacy"

    override val color = Color.rgb(0, 48, 95)
    // Terms require: copyright + source + license (with link) + disclaimer + mention of modified data
    override val weatherAttribution = "Copyright Met Éireann. Source met.ie. This data is published under a Creative Commons Attribution 4.0 International (CC BY 4.0) https://creativecommons.org/licenses/by/4.0/. Met Éireann does not accept any liability whatsoever for any error or omission in the data, their availability, or for any loss or damage arising from their use. This material has been modified from the original by Breezy Weather, mainly to compute or extrapolate missing data."

    private val mApi by lazy {
        client
            .baseUrl(MET_IE_BASE_URL)
            .build()
            .create(MetIeApi::class.java)
    }

    override val supportedFeaturesInMain = listOf(
        SecondaryWeatherSourceFeature.FEATURE_ALERT
    )

    override fun isFeatureSupportedInMainForLocation(
        location: Location,
        feature: SecondaryWeatherSourceFeature?
    ): Boolean {
        return location.countryCode.equals("IE", ignoreCase = true)
    }

    override fun requestWeather(
        context: Context, location: Location, ignoreFeatures: List<SecondaryWeatherSourceFeature>
    ): Observable<WeatherWrapper> {
        val forecast = mApi.getForecast(
            location.latitude,
            location.longitude
        )

        val warnings = if (!ignoreFeatures.contains(SecondaryWeatherSourceFeature.FEATURE_ALERT)) {
            mApi.getWarnings()
        } else {
            Observable.create { emitter ->
                emitter.onNext(MetIeWarningResult())
            }
        }

        return Observable.zip(forecast, warnings) {
                forecastResult: List<MetIeHourly>,
                warningsResult: MetIeWarningResult
            ->
            convert(forecastResult, warningsResult, location)
        }
    }

    // SECONDARY WEATHER SOURCE
    override val supportedFeaturesInSecondary = listOf(
        SecondaryWeatherSourceFeature.FEATURE_ALERT
    )
    override fun isFeatureSupportedInSecondaryForLocation(
        location: Location,
        feature: SecondaryWeatherSourceFeature
    ): Boolean {
        return isFeatureSupportedInMainForLocation(location, feature)
    }
    override val airQualityAttribution = null
    override val pollenAttribution = null
    override val minutelyAttribution = null
    override val alertAttribution = weatherAttribution
    override val normalsAttribution = null

    override fun requestSecondaryWeather(
        context: Context, location: Location,
        requestedFeatures: List<SecondaryWeatherSourceFeature>
    ): Observable<SecondaryWeatherWrapper> {
        return mApi.getWarnings().map {
            convertSecondary(it, location)
        }
    }

    override fun requestReverseGeocodingLocation(
        context: Context,
        location: Location
    ): Observable<List<Location>> {
        return mApi.getReverseLocation(
            location.latitude,
            location.longitude
        )
            .map {
                val locationList = mutableListOf<Location>()
                if (it.city != "NO LOCATION SELECTED") {
                    locationList.add(convert(location, it))
                }
                locationList
            }
    }

    // Location parameters
    override fun needsLocationParametersRefresh(
        location: Location,
        coordinatesChanged: Boolean,
        features: List<SecondaryWeatherSourceFeature>
    ): Boolean {
        if (regionsMapping.containsKey(location.admin2)) return false

        val currentRegion = location.parameters
            .getOrElse(id) { null }?.getOrElse("region") { null }

        return currentRegion.isNullOrEmpty()
    }

    override fun requestLocationParameters(
        context: Context, location: Location
    ): Observable<Map<String, String>> {
        return mApi.getReverseLocation(
            location.latitude,
            location.longitude
        ).map {
            if (it.city != "NO LOCATION SELECTED" &&
                !it.county.isNullOrEmpty() &&
                regionsMapping.containsKey(it.county)) {
                mapOf("region" to it.county)
            } else {
                throw InvalidLocationException()
            }
        }
    }

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
