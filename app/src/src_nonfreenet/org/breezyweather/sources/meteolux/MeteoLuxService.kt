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
import breezyweather.domain.location.model.Location
import breezyweather.domain.source.SourceContinent
import breezyweather.domain.source.SourceFeature
import breezyweather.domain.weather.wrappers.WeatherWrapper
import dagger.hilt.android.qualifiers.ApplicationContext
import io.reactivex.rxjava3.core.Observable
import org.breezyweather.R
import org.breezyweather.common.extensions.code
import org.breezyweather.common.extensions.currentLocale
import org.breezyweather.common.extensions.getCountryName
import org.breezyweather.common.source.HttpSource
import org.breezyweather.common.source.ReverseGeocodingSource
import org.breezyweather.common.source.WeatherSource
import org.breezyweather.common.source.WeatherSource.Companion.PRIORITY_HIGHEST
import org.breezyweather.common.source.WeatherSource.Companion.PRIORITY_NONE
import retrofit2.Retrofit
import javax.inject.Inject
import javax.inject.Named

class MeteoLuxService @Inject constructor(
    @ApplicationContext context: Context,
    @Named("JsonClient") client: Retrofit.Builder,
) : HttpSource(), WeatherSource, ReverseGeocodingSource {

    override val id = "meteolux"
    override val name = "MeteoLux (${context.currentLocale.getCountryName("LU")})"
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

    private val mApi by lazy {
        client
            .baseUrl(METEOLUX_BASE_URL)
            .build()
            .create(MeteoLuxApi::class.java)
    }

    private val weatherAttribution by lazy {
        with(context.currentLocale.code) {
            when {
                startsWith("fr") -> "MeteoLux (Transfert universel dans le Domaine Public Creative Commons CC0 1.0)"
                startsWith("de") ->
                    "MeteoLux (universellen Transfers in die Gemeinfreiheit (Public Domain) Creative Commons CC0 1.0)"
                else -> "MeteoLux (Universal transfer into the Public Domain Creative Commons CC0 1.0)."
            }
        } +
            " ${context.getString(R.string.data_modified, context.getString(R.string.breezy_weather))}"
    }
    override val supportedFeatures = mapOf(
        SourceFeature.FORECAST to weatherAttribution,
        SourceFeature.CURRENT to weatherAttribution,
        SourceFeature.ALERT to weatherAttribution,
        SourceFeature.REVERSE_GEOCODING to weatherAttribution
    )
    override val attributionLinks = mapOf(
        "MeteoLux" to "https://www.meteolux.lu/",
        "Transfert universel dans le Domaine Public Creative Commons CC0 1.0" to
            "https://creativecommons.org/publicdomain/zero/1.0/deed.fr",
        "universellen Transfers in die Gemeinfreiheit (Public Domain) Creative Commons CC0 1.0" to
            "https://creativecommons.org/publicdomain/zero/1.0/deed.de",
        "Universal transfer into the Public Domain Creative Commons CC0 1.0" to
            "https://creativecommons.org/publicdomain/zero/1.0/deed.en"
    )

    override fun isFeatureSupportedForLocation(
        location: Location,
        feature: SourceFeature,
    ): Boolean {
        return location.countryCode.equals("LU", ignoreCase = true)
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
            locationList.add(convert(context, location, it))
            locationList
        }
    }

    override val testingLocations: List<Location> = emptyList()

    companion object {
        private const val METEOLUX_BASE_URL = "https://metapi.ana.lu/"
    }
}
