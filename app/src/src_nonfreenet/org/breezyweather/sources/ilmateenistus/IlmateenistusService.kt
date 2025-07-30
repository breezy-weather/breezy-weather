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

package org.breezyweather.sources.ilmateenistus

import android.content.Context
import breezyweather.domain.location.model.Location
import breezyweather.domain.source.SourceContinent
import breezyweather.domain.source.SourceFeature
import breezyweather.domain.weather.wrappers.WeatherWrapper
import dagger.hilt.android.qualifiers.ApplicationContext
import io.reactivex.rxjava3.core.Observable
import org.breezyweather.common.extensions.code
import org.breezyweather.common.extensions.currentLocale
import org.breezyweather.common.source.HttpSource
import org.breezyweather.common.source.ReverseGeocodingSource
import org.breezyweather.common.source.WeatherSource
import org.breezyweather.common.source.WeatherSource.Companion.PRIORITY_HIGHEST
import org.breezyweather.common.source.WeatherSource.Companion.PRIORITY_NONE
import retrofit2.Retrofit
import java.util.Locale
import javax.inject.Inject
import javax.inject.Named

class IlmateenistusService @Inject constructor(
    @ApplicationContext context: Context,
    @Named("JsonClient") client: Retrofit.Builder,
) : HttpSource(), WeatherSource, ReverseGeocodingSource {
    override val id = "ilmateenistus"
    override val name = "Ilmateenistus (${Locale(context.currentLocale.code, "EE").displayCountry})"
    override val continent = SourceContinent.EUROPE
    override val privacyPolicyUrl by lazy {
        with(context.currentLocale.code) {
            when {
                startsWith("et") -> "https://www.ilmateenistus.ee/meist/kasutustingimused/"
                startsWith("ru") -> "https://www.ilmateenistus.ee/meist/kasutustingimused/?lang=ru"
                else -> "https://www.ilmateenistus.ee/meist/kasutustingimused/?lang=en"
            }
        }
    }

    private val mApi by lazy {
        client.baseUrl(ILMATEENISTUS_BASE_URL)
            .build()
            .create(IlmateenistusApi::class.java)
    }

    private val weatherAttribution by lazy {
        with(context.currentLocale.code) {
            when {
                startsWith("et") -> "Keskkonnaagentuur"
                startsWith("ru") -> "Агентство окружающей среды"
                else -> "Estonian Environment Agency"
            }
        }
    }
    override val reverseGeocodingAttribution = weatherAttribution
    override val supportedFeatures = mapOf(
        SourceFeature.FORECAST to weatherAttribution
    )
    override val attributionLinks = mapOf(
        weatherAttribution to "https://www.ilmateenistus.ee/"
    )

    override fun isFeatureSupportedForLocation(
        location: Location,
        feature: SourceFeature,
    ): Boolean {
        return isReverseGeocodingSupportedForLocation(location)
    }

    override fun isReverseGeocodingSupportedForLocation(location: Location): Boolean {
        return location.countryCode.equals("EE", ignoreCase = true)
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
        val coordinates = "${location.latitude};${location.longitude}"
        return mApi.getHourly(
            coordinates = coordinates
        ).map {
            val hourlyForecast = getHourlyForecast(context, it)
            WeatherWrapper(
                dailyForecast = getDailyForecast(hourlyForecast),
                hourlyForecast = hourlyForecast
            )
        }
    }

    override fun requestReverseGeocodingLocation(
        context: Context,
        location: Location,
    ): Observable<List<Location>> {
        val coordinates = "${location.latitude};${location.longitude}"
        return mApi.getHourly(
            coordinates = coordinates
        ).map {
            convert(context, location, it)
        }
    }

    override val testingLocations: List<Location> = emptyList()

    companion object {
        private const val ILMATEENISTUS_BASE_URL = "https://www.ilmateenistus.ee/"
    }
}
