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

package org.breezyweather.sources.geosphereat

import android.content.Context
import android.graphics.Color
import breezyweather.domain.location.model.Location
import breezyweather.domain.weather.wrappers.WeatherWrapper
import io.reactivex.rxjava3.core.Observable
import org.breezyweather.common.source.HttpSource
import org.breezyweather.common.source.MainWeatherSource
import org.breezyweather.common.source.SecondaryWeatherSourceFeature
import retrofit2.Retrofit
import javax.inject.Inject

class GeoSphereAtService @Inject constructor(
    client: Retrofit.Builder
) : HttpSource(), MainWeatherSource {

    override val id = "geosphereat"
    override val name = "GeoSphere Austria"
    override val privacyPolicyUrl = "https://www.geosphere.at/de/legal"

    override val color = Color.rgb(191, 206, 64)
    override val weatherAttribution = "GeoSphere Austria (Creative Commons Attribution 4.0)"

    private val mApi by lazy {
        client
            .baseUrl(GEOSPHERE_AT_BASE_URL)
            .build()
            .create(GeoSphereAtApi::class.java)
    }

    override val supportedFeaturesInMain = emptyList<SecondaryWeatherSourceFeature>()

    override fun requestWeather(
        context: Context, location: Location, ignoreFeatures: List<SecondaryWeatherSourceFeature>
    ): Observable<WeatherWrapper> {
        val hourlyParameters = arrayOf(
            "sy", // Weather symbol
            "t2m", // Temperature at 2 meters
            "rr_acc", // Total precipitation amount
            "rain_acc", // Total rainfall amount
            "snow_acc", // Total surface snow amount
            "u10m", // 10 m wind speed in eastward direction
            "ugust", // u component of maximum wind gust
            "v10m", // 10 m wind speed in northward direction
            "vgust", // v component of maximum wind gust
            "rh2m", // Relative humidity 2 meters
            "tcc", // Total cloud cover
            "sp", // Surface pressure
        )

        val weather = mApi.getHourlyForecast(
            "${location.latitude},${location.longitude}",
            hourlyParameters.joinToString(",")
        )

        return weather.map { hourlyResult ->
            convert(hourlyResult, location)
        }
    }

    companion object {
        private const val GEOSPHERE_AT_BASE_URL = "https://dataset.api.hub.geosphere.at/"
        private const val GEOSPHERE_AT_WARNINGS_BASE_URL = "https://openapi.hub.geosphere.at/"
    }
}
