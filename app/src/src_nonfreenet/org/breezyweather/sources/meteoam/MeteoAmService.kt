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
import breezyweather.domain.source.SourceContinent
import breezyweather.domain.source.SourceFeature
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
) : HttpSource(), MainWeatherSource, SecondaryWeatherSource, ReverseGeocodingSource {

    override val id = "meteoam"
    override val name = "Servizio Meteo AM (${Locale(context.currentLocale.code, "IT").displayCountry})"
    override val continent = SourceContinent.EUROPE
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
        SourceFeature.FEATURE_CURRENT
    )

    @SuppressLint("CheckResult")
    override fun requestWeather(
        context: Context,
        location: Location,
        ignoreFeatures: List<SourceFeature>,
    ): Observable<WeatherWrapper> {
        val failedFeatures = mutableListOf<SourceFeature>()

        val forecast = mApi.getForecast(
            location.latitude,
            location.longitude
        )
        val observation = if (!ignoreFeatures.contains(SourceFeature.FEATURE_CURRENT)) {
            mApi.getCurrent(
                location.latitude,
                location.longitude
            ).onErrorResumeNext {
                failedFeatures.add(SourceFeature.FEATURE_CURRENT)
                Observable.just(MeteoAmObservationResult())
            }
        } else {
            Observable.just(MeteoAmObservationResult())
        }
        return Observable.zip(forecast, observation) {
                forecastResult: MeteoAmForecastResult,
                observationResult: MeteoAmObservationResult,
            ->
            convert(context, forecastResult, observationResult, failedFeatures)
        }
    }

    // SECONDARY WEATHER SOURCE
    override val supportedFeaturesInSecondary = listOf(
        SourceFeature.FEATURE_CURRENT
    )
    override fun isFeatureSupportedInSecondaryForLocation(
        location: Location,
        feature: SourceFeature,
    ): Boolean {
        return isFeatureSupportedInMainForLocation(location, feature)
    }
    override val currentAttribution = weatherAttribution
    override val airQualityAttribution = null
    override val pollenAttribution = null
    override val minutelyAttribution = null
    override val alertAttribution = null
    override val normalsAttribution = null

    override fun requestSecondaryWeather(
        context: Context,
        location: Location,
        requestedFeatures: List<SourceFeature>,
    ): Observable<SecondaryWeatherWrapper> {
        if (!isFeatureSupportedInSecondaryForLocation(location, SourceFeature.FEATURE_CURRENT)) {
            // TODO: return Observable.error(UnsupportedFeatureForLocationException())
            return Observable.error(SecondaryWeatherException())
        }
        val failedFeatures = mutableListOf<SourceFeature>()
        return if (requestedFeatures.contains(SourceFeature.FEATURE_CURRENT)) {
            mApi.getCurrent(
                location.latitude,
                location.longitude
            ).onErrorResumeNext {
                failedFeatures.add(SourceFeature.FEATURE_CURRENT)
                Observable.just(MeteoAmObservationResult())
            }.map {
                convertSecondary(context, it, failedFeatures)
            }
        } else {
            Observable.just(SecondaryWeatherWrapper())
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
