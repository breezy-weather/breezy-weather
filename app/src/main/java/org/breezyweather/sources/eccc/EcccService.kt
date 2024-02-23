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

package org.breezyweather.sources.eccc

import android.content.Context
import android.graphics.Color
import io.reactivex.rxjava3.core.Observable
import breezyweather.domain.location.model.Location
import breezyweather.domain.weather.wrappers.SecondaryWeatherWrapper
import breezyweather.domain.weather.wrappers.WeatherWrapper
import org.breezyweather.common.exceptions.InvalidLocationException
import org.breezyweather.common.source.HttpSource
import org.breezyweather.common.source.MainWeatherSource
import org.breezyweather.common.source.ReverseGeocodingSource
import org.breezyweather.common.source.SecondaryWeatherSource
import org.breezyweather.common.source.SecondaryWeatherSourceFeature
import org.breezyweather.settings.SettingsManager
import retrofit2.Retrofit
import javax.inject.Inject

class EcccService @Inject constructor(
    client: Retrofit.Builder
) : HttpSource(), MainWeatherSource, SecondaryWeatherSource, ReverseGeocodingSource {

    override val id = "eccc"
    override val name = "Environment and Climate Change Canada"
    override val privacyPolicyUrl = "https://app.weather.gc.ca/privacy-en.html"

    override val color = Color.rgb(255, 0, 0)
    override val weatherAttribution = "Environment and Climate Change Canada (Environment and Climate Change Canada Data Servers End-use Licence)"

    private val mApi by lazy {
        client
            .baseUrl(ECCC_BASE_URL)
            .build()
            .create(EcccApi::class.java)
    }

    override val supportedFeaturesInMain = listOf(
        SecondaryWeatherSourceFeature.FEATURE_ALERT,
        SecondaryWeatherSourceFeature.FEATURE_NORMALS
    )

    override fun isWeatherSupportedForLocation(location: Location): Boolean {
        return location.countryCode.equals("CA", ignoreCase = true)
    }

    override fun requestWeather(
        context: Context, location: Location,
        ignoreFeatures: List<SecondaryWeatherSourceFeature>
    ): Observable<WeatherWrapper> {
        val languageCode = SettingsManager.getInstance(context).language.code

        return mApi.getForecast(
            languageCode,
            location.latitude,
            location.longitude
        ).map {
            // Can’t do that because it is a List when it succeed
            //if (it.error == "OUT_OF_SERVICE_BOUNDARY") {
            if (it.isEmpty()) {
                throw InvalidLocationException()
            }
            convert(it[0], location.timeZone)
        }
    }

    // SECONDARY WEATHER SOURCE
    override val supportedFeatures = listOf(
        SecondaryWeatherSourceFeature.FEATURE_ALERT,
        SecondaryWeatherSourceFeature.FEATURE_NORMALS
    )
    override fun isFeatureSupportedForLocation(
        feature: SecondaryWeatherSourceFeature, location: Location
    ): Boolean {
        return isWeatherSupportedForLocation(location)
    }
    override val airQualityAttribution = null
    override val pollenAttribution = null
    override val minutelyAttribution = null
    override val alertAttribution = weatherAttribution
    override val normalsAttribution = weatherAttribution

    override fun requestSecondaryWeather(
        context: Context, location: Location,
        requestedFeatures: List<SecondaryWeatherSourceFeature>
    ): Observable<SecondaryWeatherWrapper> {
        val languageCode = SettingsManager.getInstance(context).language.code

        return mApi.getForecast(
            languageCode,
            location.latitude,
            location.longitude
        ).map {
            // Can’t do that because it is a List when it succeed
            //if (it.error == "OUT_OF_SERVICE_BOUNDARY") {
            if (it.isEmpty()) {
                throw InvalidLocationException()
            }
            convertSecondary(it[0], location.timeZone)
        }
    }

    override fun requestReverseGeocodingLocation(
        context: Context,
        location: Location
    ): Observable<List<Location>> {
        val languageCode = SettingsManager.getInstance(context).language.code

        return mApi.getForecast(
            languageCode,
            location.latitude,
            location.longitude
        ).map {
            if (it.isEmpty()) {
                throw InvalidLocationException()
            }
            val locationList: MutableList<Location> = ArrayList()
            locationList.add(convert(location, it[0]))
            locationList
        }
    }

    companion object {
        private const val ECCC_BASE_URL = "https://app.weather.gc.ca/"
    }
}