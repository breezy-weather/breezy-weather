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

package org.breezyweather.sources.pagasa

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Color
import breezyweather.domain.location.model.Location
import breezyweather.domain.weather.wrappers.WeatherWrapper
import dagger.hilt.android.qualifiers.ApplicationContext
import io.reactivex.rxjava3.core.Observable
import org.breezyweather.common.exceptions.InvalidLocationException
import org.breezyweather.common.source.HttpSource
import org.breezyweather.common.source.LocationParametersSource
import org.breezyweather.common.source.MainWeatherSource
import org.breezyweather.common.source.SecondaryWeatherSourceFeature
import org.breezyweather.sources.pagasa.json.PagasaCurrentResult
import org.breezyweather.sources.pagasa.json.PagasaHourlyResult
import retrofit2.Retrofit
import javax.inject.Inject
import javax.inject.Named

class PagasaService @Inject constructor(
    @ApplicationContext context: Context,
    @Named("JsonClient") client: Retrofit.Builder,
) : HttpSource(), MainWeatherSource, LocationParametersSource {

    override val id = "pagasa"
    override val name = "PAGASA"
    override val privacyPolicyUrl = ""
    override val color = Color.rgb(75, 196, 211)
    override val weatherAttribution = "Philippine Atmospheric, Geophysical and Astronomical Services Administration"

    private val mApi by lazy {
        client
            .baseUrl(PAGASA_BASE_URL)
            .build()
            .create(PagasaApi::class.java)
    }

    override val supportedFeaturesInMain = listOf<SecondaryWeatherSourceFeature>()

    override fun isFeatureSupportedInMainForLocation(
        location: Location,
        feature: SecondaryWeatherSourceFeature?,
    ): Boolean {
        return location.countryCode.equals("PH", ignoreCase = true)
    }

    override fun requestWeather(
        context: Context,
        location: Location,
        ignoreFeatures: List<SecondaryWeatherSourceFeature>,
    ): Observable<WeatherWrapper> {
        val station = location.parameters.getOrElse(id) { null }?.getOrElse("station") { null }
        val key = location.parameters.getOrElse(id) { null }?.getOrElse("key") { null }
        if (station.isNullOrEmpty() || key.isNullOrEmpty()) {
            return Observable.error(InvalidLocationException())
        }

        val current = mApi.getCurrent()
        val hourly = List(5) { day ->
            mApi.getHourly(
                site = station,
                day = day
            )
        }

        return Observable.zip(current, hourly[0], hourly[1], hourly[2], hourly[3], hourly[4]) {
                currentResult: Map<String, List<PagasaCurrentResult>>,
                hourlyResult0: PagasaHourlyResult,
                hourlyResult1: PagasaHourlyResult,
                hourlyResult2: PagasaHourlyResult,
                hourlyResult3: PagasaHourlyResult,
                hourlyResult4: PagasaHourlyResult,
            ->
            convert(
                context = context,
                location = location,
                currentResult = currentResult.getOrElse(key) { null },
                hourlyResult = listOf(hourlyResult0, hourlyResult1, hourlyResult2, hourlyResult3, hourlyResult4)
            )
        }
    }

    // Location parameters
    override fun needsLocationParametersRefresh(
        location: Location,
        coordinatesChanged: Boolean,
        features: List<SecondaryWeatherSourceFeature>,
    ): Boolean {
        if (coordinatesChanged) return true

        val station = location.parameters.getOrElse(id) { null }?.getOrElse("station") { null }
        val key = location.parameters.getOrElse(id) { null }?.getOrElse("key") { null }

        return station.isNullOrEmpty() || key.isNullOrEmpty()
    }

    @SuppressLint("NewApi")
    override fun requestLocationParameters(
        context: Context,
        location: Location,
    ): Observable<Map<String, String>> {
        return mApi.getLocations().map {
            convert(location, it)
        }
    }

    companion object {
        private const val PAGASA_BASE_URL = "https://www.pagasa.dost.gov.ph/"
    }
}
