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

package org.breezyweather.sources.openmeteo

import android.content.Context
import android.graphics.Color
import io.reactivex.rxjava3.core.Observable
import org.breezyweather.common.basic.models.Location
import org.breezyweather.common.basic.wrappers.SecondaryWeatherWrapper
import org.breezyweather.common.basic.wrappers.WeatherWrapper
import org.breezyweather.common.exceptions.LocationSearchException
import org.breezyweather.common.source.HttpSource
import org.breezyweather.common.source.LocationSearchSource
import org.breezyweather.common.source.MainWeatherSource
import org.breezyweather.common.source.SecondaryWeatherSource
import org.breezyweather.common.source.SecondaryWeatherSourceFeature
import org.breezyweather.settings.SettingsManager
import org.breezyweather.sources.openmeteo.json.OpenMeteoAirQualityResult
import org.breezyweather.sources.openmeteo.json.OpenMeteoWeatherResult
import retrofit2.Retrofit
import javax.inject.Inject

class OpenMeteoService @Inject constructor(
    client: Retrofit.Builder
) : HttpSource(), MainWeatherSource, LocationSearchSource, SecondaryWeatherSource {

    override val id = "openmeteo"
    override val name = "Open-Meteo"
    override val privacyPolicyUrl = "https://open-meteo.com/en/terms#privacy"

    override val color = Color.rgb(255, 136, 0)
    override val weatherAttribution = "Open-Meteo (CC BY 4.0)"
    override val locationSearchAttribution = "Open-Meteo (CC BY 4.0) / GeoNames"

    private val mWeatherApi by lazy {
        client
            .baseUrl(OPEN_METEO_WEATHER_BASE_URL)
            .build()
            .create(OpenMeteoWeatherApi::class.java)
    }
    private val mGeocodingApi by lazy {
        client
            .baseUrl(OPEN_METEO_GEOCODING_BASE_URL)
            .build()
            .create(OpenMeteoGeocodingApi::class.java)
    }
    private val mAirQualityApi by lazy {
        client
            .baseUrl(OPEN_METEO_AIR_QUALITY_BASE_URL)
            .build()
            .create(OpenMeteoAirQualityApi::class.java)
    }

    val airQualityHourly = arrayOf(
        "pm10",
        "pm2_5",
        "carbon_monoxide",
        "nitrogen_dioxide",
        "sulphur_dioxide",
        "ozone"
    )
    val pollenHourly = arrayOf(
        "alder_pollen",
        "birch_pollen",
        "grass_pollen",
        "mugwort_pollen",
        "olive_pollen",
        "ragweed_pollen"
    )
    val minutely = arrayOf(
        //"precipitation_probability",
        "precipitation"
    )

    override val supportedFeaturesInMain = listOf(
        SecondaryWeatherSourceFeature.FEATURE_AIR_QUALITY,
        SecondaryWeatherSourceFeature.FEATURE_POLLEN,
        SecondaryWeatherSourceFeature.FEATURE_MINUTELY
    )
    override fun requestWeather(
        context: Context, location: Location,
        ignoreFeatures: List<SecondaryWeatherSourceFeature>
    ): Observable<WeatherWrapper> {
        val daily = arrayOf(
            "temperature_2m_max",
            "temperature_2m_min",
            "apparent_temperature_max",
            "apparent_temperature_min",
            "sunrise",
            "sunset",
            "uv_index_max"
        )
        val hourly = arrayOf(
            "temperature_2m",
            "apparent_temperature",
            "precipitation_probability",
            "precipitation",
            "rain",
            "showers",
            "snowfall",
            "weathercode",
            "windspeed_10m",
            "winddirection_10m",
            "windgusts_10m",
            "uv_index",
            "is_day",
            "relativehumidity_2m",
            "dewpoint_2m",
            "pressure_msl",
            "cloudcover",
            "visibility"
        )
        val current = arrayOf(
            "temperature_2m",
            "apparent_temperature",
            "weathercode",
            "windspeed_10m",
            "winddirection_10m",
            "windgusts_10m",
            "uv_index",
            "relativehumidity_2m",
            "dewpoint_2m",
            "pressure_msl",
            "cloudcover",
            "visibility"
        )
        val weather = mWeatherApi.getWeather(
            location.latitude.toDouble(),
            location.longitude.toDouble(),
            daily.joinToString(","),
            hourly.joinToString(","),
            if (!ignoreFeatures.contains(SecondaryWeatherSourceFeature.FEATURE_MINUTELY)) {
                minutely.joinToString(",")
            } else "",
            current.joinToString(","),
            forecastDays = 16,
            pastDays = 1,
            windspeedUnit = "ms"
        )

        val aqi = if (!ignoreFeatures.contains(SecondaryWeatherSourceFeature.FEATURE_AIR_QUALITY)
            || !ignoreFeatures.contains(SecondaryWeatherSourceFeature.FEATURE_POLLEN)
        ) {
            val airQualityPollenHourly =
                (if (!ignoreFeatures.contains(SecondaryWeatherSourceFeature.FEATURE_AIR_QUALITY)) {
                    airQualityHourly
                } else arrayOf()) +
                (if (!ignoreFeatures.contains(SecondaryWeatherSourceFeature.FEATURE_POLLEN)) {
                    pollenHourly
                } else arrayOf())
            mAirQualityApi.getAirQuality(
                location.latitude.toDouble(),
                location.longitude.toDouble(),
                airQualityPollenHourly.joinToString(","),
                forecastDays = 7,
                pastDays = 1,
            )
        } else {
            Observable.create { emitter ->
                emitter.onNext(OpenMeteoAirQualityResult())
            }
        }
        return Observable.zip(weather, aqi) { openMeteoWeatherResult: OpenMeteoWeatherResult,
                                              openMeteoAirQualityResult: OpenMeteoAirQualityResult
            ->
            convert(
                context,
                openMeteoWeatherResult,
                openMeteoAirQualityResult
            )
        }
    }

    // SECONDARY WEATHER SOURCE
    override val supportedFeatures = listOf(
        SecondaryWeatherSourceFeature.FEATURE_AIR_QUALITY,
        SecondaryWeatherSourceFeature.FEATURE_POLLEN,
        SecondaryWeatherSourceFeature.FEATURE_MINUTELY
    )
    override val airQualityAttribution =
        "Open-Meteo (CC BY 4.0) / METEO FRANCE, Institut national de l'environnement industriel et des risques (Ineris), Aarhus University, Norwegian Meteorological Institute (MET Norway), Jülich Institut für Energie- und Klimaforschung (IEK), Institute of Environmental Protection – National Research Institute (IEP-NRI), Koninklijk Nederlands Meteorologisch Instituut (KNMI), Nederlandse Organisatie voor toegepast-natuurwetenschappelijk onderzoek (TNO), Swedish Meteorological and Hydrological Institute (SMHI), Finnish Meteorological Institute (FMI), Italian National Agency for New Technologies, Energy and Sustainable Economic Development (ENEA) and Barcelona Supercomputing Center (BSC) (2022): CAMS European air quality forecasts, ENSEMBLE data. Copernicus Atmosphere Monitoring Service (CAMS) Atmosphere Data Store (ADS). (Updated twice daily)."
    override val pollenAttribution = airQualityAttribution
    override val minutelyAttribution = weatherAttribution
    override val alertAttribution = null
    override val normalsAttribution = null

    override fun requestSecondaryWeather(
        context: Context, location: Location,
        requestedFeatures: List<SecondaryWeatherSourceFeature>
    ): Observable<SecondaryWeatherWrapper> {
        val weather = if (requestedFeatures.contains(SecondaryWeatherSourceFeature.FEATURE_MINUTELY)) {
            mWeatherApi.getWeather(
                location.latitude.toDouble(),
                location.longitude.toDouble(),
                "",
                "",
                "",
                minutely.joinToString(","),
                forecastDays = 2, // In case current + 2 hours overlap two days
                pastDays = 0,
                windspeedUnit = "ms"
            )
        } else {
            Observable.create { emitter ->
                emitter.onNext(OpenMeteoWeatherResult())
            }
        }

        val aqi = if (requestedFeatures.contains(SecondaryWeatherSourceFeature.FEATURE_AIR_QUALITY)
            || requestedFeatures.contains(SecondaryWeatherSourceFeature.FEATURE_POLLEN)) {
            val airQualityPollenHourly =
                (if (requestedFeatures.contains(SecondaryWeatherSourceFeature.FEATURE_AIR_QUALITY)) airQualityHourly else emptyArray()) +
                        (if (requestedFeatures.contains(SecondaryWeatherSourceFeature.FEATURE_POLLEN)) pollenHourly else emptyArray())
            mAirQualityApi.getAirQuality(
                location.latitude.toDouble(),
                location.longitude.toDouble(),
                airQualityPollenHourly.joinToString(","),
                forecastDays = 7,
                pastDays = 1,
            )
        } else {
            Observable.create { emitter ->
                emitter.onNext(OpenMeteoAirQualityResult())
            }
        }

        return Observable.zip(weather, aqi) { openMeteoWeatherResult: OpenMeteoWeatherResult,
                                              openMeteoAirQualityResult: OpenMeteoAirQualityResult
            ->
            convertSecondary(
                openMeteoWeatherResult.minutelyFifteen,
                openMeteoAirQualityResult.hourly,
                requestedFeatures
            )
        }
    }

    // Location
    override fun requestLocationSearch(
        context: Context,
        query: String
    ): Observable<List<Location>> {
        val languageCode = SettingsManager.getInstance(context).language.code

        return mGeocodingApi.getWeatherLocation(
            query,
            count = 20,
            languageCode
        ).map { results ->
            if (results.results == null) {
                throw LocationSearchException()
            }

            results.results.map {
                convert(it)
            }
        }
    }

    companion object {
        private const val OPEN_METEO_AIR_QUALITY_BASE_URL =
            "https://air-quality-api.open-meteo.com/"
        private const val OPEN_METEO_GEOCODING_BASE_URL = "https://geocoding-api.open-meteo.com/"
        private const val OPEN_METEO_WEATHER_BASE_URL = "https://api.open-meteo.com/"
    }
}