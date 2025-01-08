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
import org.breezyweather.common.source.LocationParametersSource
import org.breezyweather.common.source.ReverseGeocodingSource
import org.breezyweather.common.source.WeatherSource
import org.breezyweather.sources.dmi.json.DmiResult
import org.breezyweather.sources.dmi.json.DmiWarningResult
import retrofit2.Retrofit
import java.util.Locale
import javax.inject.Inject
import javax.inject.Named

class DmiService @Inject constructor(
    @ApplicationContext context: Context,
    @Named("JsonClient") client: Retrofit.Builder,
) : HttpSource(), WeatherSource, ReverseGeocodingSource, LocationParametersSource {

    override val id = "dmi"
    override val name = "DMI (${Locale(context.currentLocale.code, "DK").displayCountry})"
    override val continent = SourceContinent.EUROPE
    override val privacyPolicyUrl = "https://www.dmi.dk/om-hjemmesiden/privatliv/"

    override val color = Color.rgb(12, 45, 131)

    private val mApi by lazy {
        client
            .baseUrl(DMI_BASE_URL)
            .build()
            .create(DmiApi::class.java)
    }

    private val weatherAttribution = "DMI (Creative Commons CC BY)"
    override val supportedFeatures = mapOf(
        SourceFeature.FORECAST to weatherAttribution,
        SourceFeature.ALERT to weatherAttribution
    )
    override fun isFeatureSupportedForLocation(
        location: Location,
        feature: SourceFeature,
    ): Boolean {
        return feature != SourceFeature.ALERT ||
            arrayOf("DK", "FO", "GL").any { it.equals(location.countryCode, ignoreCase = true) }
    }

    override fun requestWeather(
        context: Context,
        location: Location,
        requestedFeatures: List<SourceFeature>,
    ): Observable<WeatherWrapper> {
        val failedFeatures = mutableMapOf<SourceFeature, Throwable>()
        val weather = if (SourceFeature.FORECAST in requestedFeatures) {
            mApi.getWeather(
                location.latitude,
                location.longitude,
                DMI_WEATHER_CMD
            ).onErrorResumeNext {
                failedFeatures[SourceFeature.FORECAST] = it
                Observable.just(DmiResult())
            }
        } else {
            Observable.just(DmiResult())
        }
        val alerts = if (SourceFeature.ALERT in requestedFeatures) {
            val id = location.parameters.getOrElse(id) { null }?.getOrElse("id") { null }
            if (!id.isNullOrEmpty()) {
                mApi.getAlerts(id).onErrorResumeNext {
                    failedFeatures[SourceFeature.ALERT] = it
                    Observable.just(DmiWarningResult())
                }
            } else {
                failedFeatures[SourceFeature.ALERT] = InvalidLocationException()
                Observable.just(DmiWarningResult())
            }
        } else {
            Observable.just(DmiWarningResult())
        }

        return Observable.zip(
            weather,
            alerts
        ) { weatherResult: DmiResult, alertsResult: DmiWarningResult ->
            WeatherWrapper(
                dailyForecast = if (SourceFeature.FORECAST in requestedFeatures) {
                    getDailyForecast(location, weatherResult.timeserie)
                } else {
                    null
                },
                hourlyForecast = if (SourceFeature.FORECAST in requestedFeatures) {
                    getHourlyForecast(weatherResult.timeserie)
                } else {
                    null
                },
                alertList = if (SourceFeature.ALERT in requestedFeatures) {
                    getAlertList(alertsResult.locationWarnings)
                } else {
                    null
                },
                failedFeatures = failedFeatures
            )
        }
    }

    // Reverse geocoding
    override fun requestReverseGeocodingLocation(
        context: Context,
        location: Location,
    ): Observable<List<Location>> {
        return mApi.getWeather(
            location.latitude,
            location.longitude,
            DMI_WEATHER_CMD
        ).map {
            val locationList = mutableListOf<Location>()
            locationList.add(convert(location, it))
            locationList
        }
    }

    // Location parameters
    override fun needsLocationParametersRefresh(
        location: Location,
        coordinatesChanged: Boolean,
        features: List<SourceFeature>,
    ): Boolean {
        // If we are in Denmark, Faroe Islands, or Greenland, we need location parameters
        // (update it if coordinates changes, OR if we didn't have it yet)
        return SourceFeature.ALERT in features &&
            (coordinatesChanged || location.parameters.getOrElse(id) { null }?.getOrElse("id") { null }.isNullOrEmpty())
    }

    override fun requestLocationParameters(
        context: Context,
        location: Location,
    ): Observable<Map<String, String>> {
        return mApi.getWeather(
            location.latitude,
            location.longitude,
            DMI_WEATHER_CMD
        ).map {
            if (it.id.isNullOrEmpty()) {
                throw InvalidLocationException()
            }
            mapOf("id" to it.id)
        }
    }

    override val testingLocations: List<Location> = emptyList()

    companion object {
        private const val DMI_BASE_URL = "https://www.dmi.dk/"
        private const val DMI_WEATHER_CMD = "llj"
    }
}
