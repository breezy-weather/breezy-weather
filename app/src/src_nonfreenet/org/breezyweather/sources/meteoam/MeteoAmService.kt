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
import android.graphics.Color
import breezyweather.domain.location.model.Location
import breezyweather.domain.weather.wrappers.WeatherWrapper
import io.reactivex.rxjava3.core.Observable
import org.breezyweather.common.source.HttpSource
import org.breezyweather.common.source.MainWeatherSource
import org.breezyweather.common.source.ReverseGeocodingSource
import org.breezyweather.common.source.SecondaryWeatherSourceFeature
import org.breezyweather.sources.meteoam.json.MeteoAmForecastResult
import org.breezyweather.sources.meteoam.json.MeteoAmObservationResult
import org.breezyweather.sources.meteoam.json.MeteoAmReverseLocationResult
import retrofit2.Retrofit
import javax.inject.Inject
import javax.inject.Named

class MeteoAmService @Inject constructor(
    @Named("JsonClient") client: Retrofit.Builder,
) : HttpSource(), MainWeatherSource, ReverseGeocodingSource {

    override val id = "meteoam"
    override val name = "Servizio Meteo AM"
    override val privacyPolicyUrl = "https://www.meteoam.it/it/privacy-policy"

    override val color = Color.rgb(20, 122, 179)

    // Required wording for third-party use taken from https://www.meteoam.it/it/condizioni-utilizzo
    override val weatherAttribution =
        "Servizio Meteorologico dell’Aeronautica Militare. Informazioni elaborate utilizzando, tra l’altro, dati e prodotti del Servizio Meteorologico dell’Aeronautica Militare pubblicati sul sito www.meteoam.it"

    private val mApi by lazy {
        client
            .baseUrl(METEOAM_BASE_URL)
            .build()
            .create(MeteoAmApi::class.java)
    }

    override val supportedFeaturesInMain = listOf(
        SecondaryWeatherSourceFeature.FEATURE_CURRENT
    )

    @SuppressLint("CheckResult")
    override fun requestWeather(
        context: Context,
        location: Location,
        ignoreFeatures: List<SecondaryWeatherSourceFeature>,
    ): Observable<WeatherWrapper> {
        val forecast = mApi.getForecast(
            location.latitude,
            location.longitude
        )
        val observation = if (!ignoreFeatures.contains(SecondaryWeatherSourceFeature.FEATURE_CURRENT)) {
            mApi.getCurrent(
                location.latitude,
                location.longitude
            )
        } else {
            Observable.create { emitter ->
                emitter.onNext(MeteoAmObservationResult())
            }
        }
        return Observable.zip(forecast, observation) {
                forecastResult: MeteoAmForecastResult,
                observationResult: MeteoAmObservationResult,
            ->
            convert(context, forecastResult, observationResult)
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

    companion object {
        private const val METEOAM_BASE_URL = "https://api.meteoam.it/"
    }
}
