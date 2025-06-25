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

package org.breezyweather.sources.meteoam

import android.annotation.SuppressLint
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
import org.breezyweather.sources.meteoam.json.MeteoAmForecastResult
import org.breezyweather.sources.meteoam.json.MeteoAmObservationResult
import org.breezyweather.sources.meteoam.json.MeteoAmReverseLocationResult
import retrofit2.Retrofit
import java.util.Locale
import javax.inject.Inject
import javax.inject.Named

class MeteoAmService @Inject constructor(
    @ApplicationContext context: Context,
    @Named("JsonClient") client: Retrofit.Builder,
) : HttpSource(), WeatherSource, ReverseGeocodingSource {

    override val id = "meteoam"
    override val name = "Servizio Meteo AM (${Locale(context.currentLocale.code, "IT").displayCountry})"
    override val continent = SourceContinent.EUROPE
    override val privacyPolicyUrl = "https://www.meteoam.it/it/privacy-policy"

    private val mApi by lazy {
        client
            .baseUrl(METEOAM_BASE_URL)
            .build()
            .create(MeteoAmApi::class.java)
    }

    // Required wording for third-party use taken from https://www.meteoam.it/it/condizioni-utilizzo
    private val weatherAttribution = "Servizio Meteorologico dell’Aeronautica Militare. Informazioni elaborate " +
        "utilizzando, tra l’altro, dati e prodotti del Servizio Meteorologico dell’Aeronautica Militare pubblicati " +
        "sul sito www.meteoam.it"
    override val reverseGeocodingAttribution = weatherAttribution
    override val supportedFeatures = mapOf(
        SourceFeature.FORECAST to weatherAttribution,
        SourceFeature.CURRENT to weatherAttribution
    )
    override val attributionLinks = mapOf(
        "Servizio Meteorologico dell’Aeronautica Militare" to "https://www.meteoam.it/",
        "www.meteoam.it" to "https://www.meteoam.it/"
    )

    @SuppressLint("CheckResult")
    override fun requestWeather(
        context: Context,
        location: Location,
        requestedFeatures: List<SourceFeature>,
    ): Observable<WeatherWrapper> {
        val failedFeatures = mutableMapOf<SourceFeature, Throwable>()

        val forecast = if (SourceFeature.FORECAST in requestedFeatures) {
            mApi.getForecast(
                location.latitude,
                location.longitude
            ).onErrorResumeNext {
                failedFeatures[SourceFeature.FORECAST] = it
                Observable.just(MeteoAmForecastResult())
            }
        } else {
            Observable.just(MeteoAmForecastResult())
        }
        val observation = if (SourceFeature.CURRENT in requestedFeatures) {
            mApi.getCurrent(
                location.latitude,
                location.longitude
            ).onErrorResumeNext {
                failedFeatures[SourceFeature.CURRENT] = it
                Observable.just(MeteoAmObservationResult())
            }
        } else {
            Observable.just(MeteoAmObservationResult())
        }
        return Observable.zip(forecast, observation) {
                forecastResult: MeteoAmForecastResult,
                observationResult: MeteoAmObservationResult,
            ->
            WeatherWrapper(
                dailyForecast = if (SourceFeature.FORECAST in requestedFeatures) {
                    getDailyForecast(context, forecastResult.extrainfo?.stats)
                } else {
                    null
                },
                hourlyForecast = if (SourceFeature.FORECAST in requestedFeatures) {
                    getHourlyForecast(
                        context,
                        forecastResult.timeseries,
                        forecastResult.paramlist,
                        forecastResult.datasets?.data
                    )
                } else {
                    null
                },
                current = if (SourceFeature.CURRENT in requestedFeatures) {
                    val oParams = observationResult.paramlist
                    val observationData = observationResult.datasets?.getOrElse("0") { null }
                    observationData?.let { oParams?.let { getCurrent(context, oParams, observationData) } }
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
        val reverseLocation = mApi.getReverseLocation(location.latitude, location.longitude)
        val forecast = mApi.getForecast(location.latitude, location.longitude)
        val locationList = mutableListOf<Location>()
        return Observable.zip(reverseLocation, forecast) {
                reverseLocationResult: MeteoAmReverseLocationResult,
                forecastResult: MeteoAmForecastResult,
            ->
            if (!reverseLocationResult.results.isNullOrEmpty() && !forecastResult.extrainfo?.timezone.isNullOrEmpty()) {
                locationList.add(
                    convert(location, reverseLocationResult.results[0], forecastResult.extrainfo?.timezone!!)
                )
            }
            locationList
        }
    }

    override val testingLocations: List<Location> = emptyList()

    companion object {
        private const val METEOAM_BASE_URL = "https://api.meteoam.it/"
    }
}
