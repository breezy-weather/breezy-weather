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
import breezyweather.domain.location.model.Location
import breezyweather.domain.source.SourceContinent
import breezyweather.domain.source.SourceFeature
import breezyweather.domain.weather.wrappers.WeatherWrapper
import dagger.hilt.android.qualifiers.ApplicationContext
import io.reactivex.rxjava3.core.Observable
import org.breezyweather.common.exceptions.InvalidLocationException
import org.breezyweather.common.extensions.code
import org.breezyweather.common.extensions.currentLocale
import org.breezyweather.common.source.HttpSource
import org.breezyweather.common.source.ReverseGeocodingSource
import org.breezyweather.common.source.WeatherSource
import retrofit2.Retrofit
import java.util.Locale
import javax.inject.Inject
import javax.inject.Named

class EcccService @Inject constructor(
    @ApplicationContext context: Context,
    @Named("JsonClient") client: Retrofit.Builder,
) : HttpSource(), WeatherSource, ReverseGeocodingSource {

    override val id = "eccc"
    override val name = "ECCC (${Locale(context.currentLocale.code, "CA").displayCountry})"
    override val continent = SourceContinent.NORTH_AMERICA
    override val privacyPolicyUrl by lazy {
        with(context.currentLocale.code) {
            when {
                startsWith("fr") -> "https://app.weather.gc.ca/privacy-fr.html"
                else -> "https://app.weather.gc.ca/privacy-en.html"
            }
        }
    }

    private val mApi by lazy {
        client
            .baseUrl(ECCC_BASE_URL)
            .build()
            .create(EcccApi::class.java)
    }

    private val weatherAttribution by lazy {
        with(context.currentLocale.code) {
            when {
                startsWith("fr") ->
                    "Environnement et Changement Climatique Canada (Licence d’utilisation finale" +
                        " pour les serveurs de données d’Environnement et Changement Climatique Canada)"
                else ->
                    "Environment and Climate Change Canada" +
                        " (Environment and Climate Change Canada Data Servers End-use Licence)"
            }
        }
    }
    override val attributionLinks
        get() = mapOf(
            "Environnement et Changement Climatique Canada" to "https://meteo.gc.ca/",
            "Environment and Climate Change Canada" to "https://weather.gc.ca/"
        )
    override val reverseGeocodingAttribution = weatherAttribution
    override val supportedFeatures = mapOf(
        SourceFeature.FORECAST to weatherAttribution,
        SourceFeature.CURRENT to weatherAttribution,
        SourceFeature.ALERT to weatherAttribution,
        SourceFeature.NORMALS to weatherAttribution
    )

    override fun isFeatureSupportedForLocation(
        location: Location,
        feature: SourceFeature,
    ): Boolean {
        return isReverseGeocodingSupportedForLocation(location)
    }

    override fun isReverseGeocodingSupportedForLocation(location: Location): Boolean {
        return location.countryCode.equals("CA", ignoreCase = true)
    }

    override fun requestWeather(
        context: Context,
        location: Location,
        requestedFeatures: List<SourceFeature>,
    ): Observable<WeatherWrapper> {
        return mApi.getForecast(
            context.currentLocale.code,
            location.latitude,
            location.longitude
        ).map {
            // Can’t do that because it is a List when it succeed
            // if (it.error == "OUT_OF_SERVICE_BOUNDARY") {
            if (it.isEmpty()) {
                throw InvalidLocationException()
            }

            WeatherWrapper(
                dailyForecast = if (SourceFeature.FORECAST in requestedFeatures) {
                    getDailyForecast(location, it[0].dailyFcst)
                } else {
                    null
                },
                hourlyForecast = if (SourceFeature.FORECAST in requestedFeatures) {
                    getHourlyForecast(it[0].hourlyFcst?.hourly)
                } else {
                    null
                },
                current = if (SourceFeature.CURRENT in requestedFeatures) {
                    getCurrent(it[0].observation)
                } else {
                    null
                },
                alertList = if (SourceFeature.ALERT in requestedFeatures) {
                    getAlertList(it[0].alert?.alerts)
                } else {
                    null
                },
                normals = if (SourceFeature.NORMALS in requestedFeatures) {
                    getNormals(location, it[0].dailyFcst?.regionalNormals?.metric)
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
        return mApi.getForecast(
            context.currentLocale.code,
            location.latitude,
            location.longitude
        ).map {
            if (it.isEmpty()) {
                throw InvalidLocationException()
            }
            val locationList = mutableListOf<Location>()
            locationList.add(convert(location, it[0]))
            locationList
        }
    }

    override val testingLocations: List<Location> = emptyList()

    companion object {
        private const val ECCC_BASE_URL = "https://app.weather.gc.ca/"
    }
}
