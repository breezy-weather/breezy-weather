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

package org.breezyweather.sources.bmd

import android.content.Context
import breezyweather.domain.location.model.Location
import breezyweather.domain.source.SourceContinent
import breezyweather.domain.source.SourceFeature
import breezyweather.domain.weather.wrappers.WeatherWrapper
import dagger.hilt.android.qualifiers.ApplicationContext
import io.reactivex.rxjava3.core.Observable
import okhttp3.OkHttpClient
import okhttp3.Request
import org.breezyweather.common.exceptions.InvalidLocationException
import org.breezyweather.common.extensions.code
import org.breezyweather.common.extensions.currentLocale
import org.breezyweather.common.source.HttpSource
import org.breezyweather.common.source.LocationParametersSource
import org.breezyweather.common.source.ReverseGeocodingSource
import org.breezyweather.common.source.WeatherSource
import org.breezyweather.common.source.WeatherSource.Companion.PRIORITY_HIGHEST
import org.breezyweather.common.source.WeatherSource.Companion.PRIORITY_NONE
import org.breezyweather.sources.bmd.json.BmdForecastResult
import retrofit2.Retrofit
import java.util.Locale
import javax.inject.Inject
import javax.inject.Named

class BmdService @Inject constructor(
    @ApplicationContext context: Context,
    @Named("JsonClient") client: Retrofit.Builder,
) : HttpSource(), WeatherSource, ReverseGeocodingSource, LocationParametersSource {

    override val id = "bmd"
    override val name by lazy {
        if (context.currentLocale.code.startsWith("bn")) {
            "বাংলাদেশ আবহাওয়া অধিদপ্তর"
        } else {
            "BMD (${Locale(context.currentLocale.code, "BD").displayCountry})"
        }
    }
    override val continent = SourceContinent.ASIA
    override val privacyPolicyUrl = ""

    private val mApi by lazy {
        client
            .baseUrl(BMD_API_BASE_URL)
            .build()
            .create(BmdApi::class.java)
    }

    private val okHttpClient = OkHttpClient()

    private val weatherAttribution = if (context.currentLocale.code.startsWith("bn")) {
        "বাংলাদেশ আবহাওয়া অধিদপ্তর"
    } else {
        "Bangladesh Meteorological Department"
    }
    override val attributionLinks = mapOf(
        weatherAttribution to "https://www.bmd.gov.bd/"
    )
    override val supportedFeatures = mapOf(
        SourceFeature.FORECAST to weatherAttribution,
        SourceFeature.REVERSE_GEOCODING to weatherAttribution
    )

    override fun isFeatureSupportedForLocation(
        location: Location,
        feature: SourceFeature,
    ): Boolean {
        return location.countryCode.equals("BD", ignoreCase = true)
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
        val upazila = location.parameters.getOrElse(id) { null }?.getOrElse("upazila") { null }
        if (upazila.isNullOrEmpty()) {
            return Observable.error(InvalidLocationException())
        }

        val daily = mApi.getDaily(
            pCode = upazila
        )
        val hourly = mApi.getHourly(
            pCode = upazila
        )

        return Observable.zip(daily, hourly) {
                dailyResult: BmdForecastResult,
                hourlyResult: BmdForecastResult,
            ->
            WeatherWrapper(
                dailyForecast = getDailyForecast(context, upazila, dailyResult),
                hourlyForecast = getHourlyForecast(context, upazila, hourlyResult)
            )
        }
    }

    override fun requestReverseGeocodingLocation(
        context: Context,
        location: Location,
    ): Observable<List<Location>> {
        val upazila = getUpazila(location)
        return mApi.getDaily(
            pCode = upazila
        ).map { dailyResult ->
            val locationList = mutableListOf<Location>()
            dailyResult.data?.getOrElse(upazila) { null }?.let {
                locationList.add(
                    convert(context, location, it)
                )
            }
            locationList
        }
    }

    override fun needsLocationParametersRefresh(
        location: Location,
        coordinatesChanged: Boolean,
        features: List<SourceFeature>,
    ): Boolean {
        if (coordinatesChanged) return true
        val upazila = location.parameters.getOrElse(id) { null }?.getOrElse("upazila") { null }

        return upazila.isNullOrEmpty()
    }

    override fun requestLocationParameters(
        context: Context,
        location: Location,
    ): Observable<Map<String, String>> {
        val upazila = getUpazila(location)
        return Observable.just(
            mapOf(
                "upazila" to upazila
            )
        )
    }

    private fun getUpazila(
        location: Location,
    ): String {
        val url = "https://bmd.bdservers.site/Dashboard/getUpazilaByLatLon/${location.latitude}/${location.longitude}"
        val request = Request.Builder().url(url).build()
        return okHttpClient.newCall(request).execute().use { call ->
            if (call.isSuccessful) {
                call.body.string()
            } else {
                throw InvalidLocationException()
            }
        }
    }

    override val testingLocations: List<Location> = emptyList()

    companion object {
        private const val BMD_API_BASE_URL = "https://api.bdservers.site/"
    }
}
