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
import breezyweather.domain.weather.wrappers.SecondaryWeatherWrapper
import breezyweather.domain.weather.wrappers.WeatherWrapper
import dagger.hilt.android.qualifiers.ApplicationContext
import io.reactivex.rxjava3.core.Observable
import org.breezyweather.common.exceptions.SecondaryWeatherException
import org.breezyweather.common.extensions.code
import org.breezyweather.common.extensions.currentLocale
import org.breezyweather.common.source.HttpSource
import org.breezyweather.common.source.MainWeatherSource
import org.breezyweather.common.source.ReverseGeocodingSource
import org.breezyweather.common.source.SecondaryWeatherSource
import org.breezyweather.common.source.SecondaryWeatherSourceFeature
import retrofit2.Retrofit
import javax.inject.Inject
import javax.inject.Named

class MeteoLuxService @Inject constructor(
    @ApplicationContext context: Context,
    @Named("JsonClient") client: Retrofit.Builder,
) : HttpSource(), MainWeatherSource, SecondaryWeatherSource, ReverseGeocodingSource {

    override val id = "meteolux"
    override val name = "MeteoLux"
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
    override val weatherAttribution = "MeteoLux"

    private val mApi by lazy {
        client
            .baseUrl(METEOLUX_BASE_URL)
            .build()
            .create(MeteoLuxApi::class.java)
    }

    override val supportedFeaturesInMain = listOf(
        SecondaryWeatherSourceFeature.FEATURE_CURRENT,
        SecondaryWeatherSourceFeature.FEATURE_ALERT
    )

    override fun isFeatureSupportedInMainForLocation(
        location: Location,
        feature: SecondaryWeatherSourceFeature?,
    ): Boolean {
        return location.countryCode.equals("LU", ignoreCase = true)
    }

    override fun requestWeather(
        context: Context,
        location: Location,
        ignoreFeatures: List<SecondaryWeatherSourceFeature>,
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
            convert(
                context = context,
                weatherResult = it,
                ignoreFeatures = ignoreFeatures
            )
        }
    }

    // SECONDARY WEATHER SOURCE
    override val supportedFeaturesInSecondary = listOf(
        SecondaryWeatherSourceFeature.FEATURE_CURRENT,
        SecondaryWeatherSourceFeature.FEATURE_ALERT
    )
    override fun isFeatureSupportedInSecondaryForLocation(
        location: Location,
        feature: SecondaryWeatherSourceFeature,
    ): Boolean {
        return isFeatureSupportedInMainForLocation(location, feature)
    }
    override val currentAttribution = weatherAttribution
    override val airQualityAttribution = null
    override val pollenAttribution = null
    override val minutelyAttribution = null
    override val alertAttribution = weatherAttribution
    override val normalsAttribution = null

    override fun requestSecondaryWeather(
        context: Context,
        location: Location,
        requestedFeatures: List<SecondaryWeatherSourceFeature>,
    ): Observable<SecondaryWeatherWrapper> {
        if (!isFeatureSupportedInSecondaryForLocation(location, SecondaryWeatherSourceFeature.FEATURE_ALERT) ||
            !isFeatureSupportedInSecondaryForLocation(location, SecondaryWeatherSourceFeature.FEATURE_CURRENT)
        ) {
            // TODO: return Observable.error(UnsupportedFeatureForLocationException())
            return Observable.error(SecondaryWeatherException())
        }

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
            convertSecondary(
                context = context,
                weatherResult = it,
                requestedFeatures = requestedFeatures
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
