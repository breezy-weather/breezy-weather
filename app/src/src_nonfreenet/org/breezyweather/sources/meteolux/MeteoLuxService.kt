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

package org.breezyweather.sources.meteolux

import android.content.Context
import android.graphics.Color
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
import retrofit2.Retrofit
import java.util.Locale
import javax.inject.Inject
import javax.inject.Named

class MeteoLuxService @Inject constructor(
    @ApplicationContext context: Context,
    @Named("JsonClient") client: Retrofit.Builder,
) : HttpSource(), WeatherSource, ReverseGeocodingSource {

    override val id = "meteolux"
    override val name = "MeteoLux (${Locale(context.currentLocale.code, "LU").displayCountry})"
    override val continent = SourceContinent.EUROPE
    override val privacyPolicyUrl by lazy {
        with(context.currentLocale.code) {
            when {
                startsWith("fr") -> "https://ana.gouvernement.lu/fr/support/politique-confidentialite-meteolux.html"
                startsWith("de") -> "https://ana.gouvernement.lu/de/support/politique-confidentialite-meteolux.html"
                else -> "https://ana.gouvernement.lu/en/support/politique-confidentialite-meteolux.html"
            }
        }
    }

    override val color = Color.rgb(7, 84, 91)

    private val mApi by lazy {
        client
            .baseUrl(METEOLUX_BASE_URL)
            .build()
            .create(MeteoLuxApi::class.java)
    }

    private val weatherAttribution = "MeteoLux"
    override val supportedFeatures = mapOf(
        SourceFeature.FORECAST to weatherAttribution,
        SourceFeature.CURRENT to weatherAttribution,
        SourceFeature.ALERT to weatherAttribution
    )

    override fun isFeatureSupportedForLocation(
        location: Location,
        feature: SourceFeature,
    ): Boolean {
        return location.countryCode.equals("LU", ignoreCase = true)
    }

    override fun requestWeather(
        context: Context,
        location: Location,
        requestedFeatures: List<SourceFeature>,
    ): Observable<WeatherWrapper> {
        return mApi.getWeather(
            language = with(context.currentLocale.code) {
                when {
                    startsWith("de") -> "de"
                    startsWith("fr") -> "fr"
                    startsWith("lb") -> "lb"
                    else -> "en"
                }
            },
            lat = location.latitude,
            lon = location.longitude
        ).map {
            WeatherWrapper(
                dailyForecast = if (SourceFeature.FORECAST in requestedFeatures) {
                    getDailyForecast(context, it)
                } else {
                    null
                },
                hourlyForecast = if (SourceFeature.FORECAST in requestedFeatures) {
                    getHourlyForecast(context, it)
                } else {
                    null
                },
                current = if (SourceFeature.CURRENT in requestedFeatures) {
                    getCurrent(context, it)
                } else {
                    null
                },
                alertList = if (SourceFeature.ALERT in requestedFeatures) {
                    getAlertList(context, it)
                } else {
                    null
                }
            )
        }
    }

    override fun requestReverseGeocodingLocation(
        context: Context,
        location: Location,
    ): Observable<List<Location>> {
        return mApi.getWeather(
            language = with(context.currentLocale.code) {
                when {
                    startsWith("de") -> "de"
                    startsWith("fr") -> "fr"
                    startsWith("lb") -> "lb"
                    else -> "en"
                }
            },
            lat = location.latitude,
            lon = location.longitude
        ).map {
            val locationList = mutableListOf<Location>()
            locationList.add(convert(location, it))
            locationList
        }
    }

    companion object {
        private const val METEOLUX_BASE_URL = "https://metapi.ana.lu/"
    }
}
