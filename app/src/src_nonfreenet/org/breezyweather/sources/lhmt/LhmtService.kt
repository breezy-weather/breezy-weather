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

package org.breezyweather.sources.lhmt

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
import org.breezyweather.common.source.LocationParametersSource
import org.breezyweather.common.source.ReverseGeocodingSource
import org.breezyweather.common.source.WeatherSource
import org.breezyweather.sources.lhmt.json.LhmtAlertsResult
import org.breezyweather.sources.lhmt.json.LhmtLocationsResult
import org.breezyweather.sources.lhmt.json.LhmtWeatherResult
import retrofit2.Retrofit
import java.util.Locale
import javax.inject.Inject
import javax.inject.Named

class LhmtService @Inject constructor(
    @ApplicationContext context: Context,
    @Named("JsonClient") client: Retrofit.Builder,
) : HttpSource(), WeatherSource, ReverseGeocodingSource, LocationParametersSource {
    override val id = "lhmt"
    override val name = "LHMT (${Locale(context.currentLocale.code, "LT").displayCountry})"
    override val continent = SourceContinent.EUROPE
    override val privacyPolicyUrl = "https://www.meteo.lt/istaiga/asmens-duomenu-apsauga/privatumo-politika/"

    private val mApi by lazy {
        client.baseUrl(LHMT_BASE_URL)
            .build()
            .create(LhmtApi::class.java)
    }

    private val mWwwApi by lazy {
        client.baseUrl(LHMT_WWW_BASE_URL)
            .build()
            .create(LhmtWwwApi::class.java)
    }

    private val weatherAttribution = "Lietuvos hidrometeorologijos tarnyba"
    override val reverseGeocodingAttribution = weatherAttribution
    override val supportedFeatures = mapOf(
        SourceFeature.FORECAST to weatherAttribution,
        SourceFeature.CURRENT to weatherAttribution,
        SourceFeature.ALERT to weatherAttribution
    )

    override fun isFeatureSupportedForLocation(
        location: Location,
        feature: SourceFeature,
    ): Boolean {
        return isReverseGeocodingSupportedForLocation(location)
    }

    override fun isReverseGeocodingSupportedForLocation(location: Location): Boolean {
        return location.countryCode.equals("LT", ignoreCase = true)
    }

    override fun requestWeather(
        context: Context,
        location: Location,
        requestedFeatures: List<SourceFeature>,
    ): Observable<WeatherWrapper> {
        val forecastLocation = location.parameters.getOrElse(id) { null }?.getOrElse("forecastLocation") { null }
        val currentLocation = location.parameters.getOrElse(id) { null }?.getOrElse("currentLocation") { null }
        val municipality = location.parameters.getOrElse(id) { null }?.getOrElse("municipality") { null }
        val county = location.parameters.getOrElse(id) { null }?.getOrElse("county") { null }
        if (forecastLocation.isNullOrEmpty() ||
            currentLocation.isNullOrEmpty() ||
            municipality.isNullOrEmpty() ||
            county.isNullOrEmpty()
        ) {
            return Observable.error(InvalidLocationException())
        }

        val failedFeatures = mutableMapOf<SourceFeature, Throwable>()

        val forecast = if (SourceFeature.FORECAST in requestedFeatures) {
            mApi.getForecast(forecastLocation).onErrorResumeNext {
                failedFeatures[SourceFeature.FORECAST] = it
                Observable.just(LhmtWeatherResult())
            }
        } else {
            Observable.just(LhmtWeatherResult())
        }
        val current = if (SourceFeature.CURRENT in requestedFeatures) {
            mApi.getCurrent(currentLocation).onErrorResumeNext {
                failedFeatures[SourceFeature.CURRENT] = it
                Observable.just(LhmtWeatherResult())
            }
        } else {
            Observable.just(LhmtWeatherResult())
        }

        val alerts = if (SourceFeature.ALERT in requestedFeatures) {
            mWwwApi.getAlertList().map { list ->
                val path = list.first().substringAfter(LHMT_WWW_BASE_URL)
                mWwwApi.getAlerts(path).onErrorResumeNext {
                    failedFeatures[SourceFeature.ALERT] = it
                    Observable.just(LhmtAlertsResult())
                }.blockingFirst()
            }.onErrorResumeNext {
                failedFeatures[SourceFeature.ALERT] = it
                Observable.just(LhmtAlertsResult())
            }
        } else {
            Observable.just(LhmtAlertsResult())
        }

        return Observable.zip(current, forecast, alerts) {
                currentResult: LhmtWeatherResult,
                forecastResult: LhmtWeatherResult,
                alertsResult: LhmtAlertsResult,
            ->
            val hourlyForecast = if (SourceFeature.FORECAST in requestedFeatures) {
                getHourlyForecast(context, forecastResult)
            } else {
                null
            }
            WeatherWrapper(
                dailyForecast = if (SourceFeature.FORECAST in requestedFeatures) {
                    getDailyForecast(hourlyForecast)
                } else {
                    null
                },
                hourlyForecast = hourlyForecast,
                current = if (SourceFeature.CURRENT in requestedFeatures) {
                    getCurrent(context, currentResult)
                } else {
                    null
                },
                alertList = if (SourceFeature.ALERT in requestedFeatures) {
                    getAlertList(context, location, alertsResult)
                } else {
                    null
                },
                failedFeatures = failedFeatures
            )
        }
    }

    override fun requestReverseGeocodingLocation(
        context: Context,
        location: Location,
    ): Observable<List<Location>> {
        return mApi.getForecastLocations().map {
            convert(location, it)
        }
    }

    override fun needsLocationParametersRefresh(
        location: Location,
        coordinatesChanged: Boolean,
        features: List<SourceFeature>,
    ): Boolean {
        if (coordinatesChanged) return true

        val forecastLocation = location.parameters.getOrElse(id) { null }?.getOrElse("forecastLocation") { null }
        val currentLocation = location.parameters.getOrElse(id) { null }?.getOrElse("currentLocation") { null }
        val municipality = location.parameters.getOrElse(id) { null }?.getOrElse("municipality") { null }
        val county = location.parameters.getOrElse(id) { null }?.getOrElse("county") { null }

        return forecastLocation.isNullOrEmpty() ||
            currentLocation.isNullOrEmpty() ||
            municipality.isNullOrEmpty() ||
            county.isNullOrEmpty()
    }

    override fun requestLocationParameters(
        context: Context,
        location: Location,
    ): Observable<Map<String, String>> {
        val forecastLocations = mApi.getForecastLocations()
        val currentLocations = mApi.getCurrentLocations()
        return Observable.zip(forecastLocations, currentLocations) {
                forecastLocationsResult: List<LhmtLocationsResult>,
                currentLocationsResult: List<LhmtLocationsResult>,
            ->
            convert(location, forecastLocationsResult, currentLocationsResult)
        }
    }

    override val testingLocations: List<Location> = emptyList()

    companion object {
        private const val LHMT_BASE_URL = "https://api.meteo.lt/"
        private const val LHMT_WWW_BASE_URL = "https://www.meteo.lt/"
    }
}
