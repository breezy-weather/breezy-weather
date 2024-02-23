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
import io.reactivex.rxjava3.core.Observable
import breezyweather.domain.location.model.Location
import breezyweather.domain.weather.wrappers.WeatherWrapper
import org.breezyweather.common.source.HttpSource
import org.breezyweather.common.source.MainWeatherSource
import org.breezyweather.common.source.SecondaryWeatherSourceFeature
import retrofit2.Retrofit
import javax.inject.Inject

class MetIeService @Inject constructor(
    client: Retrofit.Builder
) : HttpSource(), MainWeatherSource {

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

    override val supportedFeaturesInMain = listOf<SecondaryWeatherSourceFeature>()

    override fun isWeatherSupportedForLocation(location: Location): Boolean {
        return location.countryCode.equals("IE", ignoreCase = true)
    }

    override fun requestWeather(
        context: Context, location: Location,
        ignoreFeatures: List<SecondaryWeatherSourceFeature>
    ): Observable<WeatherWrapper> {
        return mApi.getForecast(
            location.latitude,
            location.longitude
        ).map {
            convert(it, location.timeZone)
        }
    }

    companion object {
        private const val MET_IE_BASE_URL = "https://prodapi.metweb.ie/"
    }
}