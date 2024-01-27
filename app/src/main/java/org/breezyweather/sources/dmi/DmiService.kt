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

package org.breezyweather.sources.dmi

import android.content.Context
import android.graphics.Color
import io.reactivex.rxjava3.core.Observable
import org.breezyweather.common.basic.models.Location
import org.breezyweather.common.basic.wrappers.WeatherWrapper
import org.breezyweather.common.source.HttpSource
import org.breezyweather.common.source.MainWeatherSource
import org.breezyweather.common.source.ReverseGeocodingSource
import org.breezyweather.common.source.SecondaryWeatherSourceFeature
import retrofit2.Retrofit
import javax.inject.Inject

class DmiService @Inject constructor(
    client: Retrofit.Builder
) : HttpSource(), MainWeatherSource, ReverseGeocodingSource {

    override val id = "dmi"
    override val name = "Danmarks Meteorologiske Institut (DMI)"
    override val privacyPolicyUrl = "https://www.dmi.dk/om-hjemmesiden/privatliv/"

    override val color = Color.rgb(12, 45, 131)
    override val weatherAttribution = "DMI (Creative Commons CC BY)"

    private val mApi by lazy {
        client
            .baseUrl(DMI_BASE_URL)
            .build()
            .create(DmiApi::class.java)
    }

    override val supportedFeaturesInMain = listOf<SecondaryWeatherSourceFeature>()

    override fun requestWeather(
        context: Context, location: Location,
        ignoreFeatures: List<SecondaryWeatherSourceFeature>
    ): Observable<WeatherWrapper> {
        val weatherResult = mApi.getWeather(
            location.latitude,
            location.longitude,
            "llj"
        )

        /*val alertsResult = if (location.countryCode == "DK" && !location.cityId.isNullOrEmpty()) {
            "TODO"
        } else "TODO"*/

        return weatherResult.map {
            convert(it, location.timeZone)
        }
    }

    override fun isUsable(location: Location): Boolean = true

    override fun requestReverseGeocodingLocation(
        context: Context,
        location: Location
    ): Observable<List<Location>> {
        val dmiResult = mApi.getWeather(
            location.latitude,
            location.longitude,
            "llj"
        )

        return dmiResult.map {
            val locationList: MutableList<Location> = ArrayList()
            locationList.add(convert(location, it))
            locationList
        }
    }

    companion object {
        private const val DMI_BASE_URL = "https://www.dmi.dk/"
    }
}