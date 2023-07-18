package org.breezyweather.sources.openmeteo

import android.content.Context
import io.reactivex.rxjava3.core.Observable
import org.breezyweather.common.basic.models.Location
import org.breezyweather.common.exceptions.LocationSearchException
import org.breezyweather.common.source.HttpSource
import org.breezyweather.common.source.LocationSearchSource
import org.breezyweather.common.basic.wrappers.WeatherResultWrapper
import org.breezyweather.common.source.WeatherSource
import org.breezyweather.settings.SettingsManager
import org.breezyweather.sources.openmeteo.json.OpenMeteoAirQualityResult
import org.breezyweather.sources.openmeteo.json.OpenMeteoWeatherResult
import retrofit2.Retrofit
import javax.inject.Inject

class OpenMeteoService @Inject constructor(
    client: Retrofit.Builder
) : HttpSource(), WeatherSource, LocationSearchSource {

    override val id = "openmeteo"
    override val name = "Open-Meteo"
    override val privacyPolicyUrl = "https://open-meteo.com/en/terms#privacy"

    override val color = -0x0077ff
    override val weatherAttribution = "Open-Meteo CC BY 4.0"
    override val locationSearchAttribution = "Open-Meteo CC BY 4.0 / GeoNames"

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

    override fun requestWeather(context: Context, location: Location): Observable<WeatherResultWrapper> {
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
            "uv_index",
            "is_day", // Used by current only
            "relativehumidity_2m",
            "dewpoint_2m",
            "surface_pressure",
            "cloudcover",
            "visibility"
        )
        val weather = mWeatherApi.getWeather(
            location.latitude.toDouble(),
            location.longitude.toDouble(),
            daily.joinToString(","),
            hourly.joinToString(","),
            forecastDays = 16,
            pastDays = 1,
            currentWeather = true
        )

        // TODO: pollen
        val airQualityHourly = arrayOf(
            "pm10",
            "pm2_5",
            "carbon_monoxide",
            "nitrogen_dioxide",
            "sulphur_dioxide",
            "ozone"
        )
        val aqi = mAirQualityApi.getAirQuality(
            location.latitude.toDouble(),
            location.longitude.toDouble(),
            airQualityHourly.joinToString(",")
        )
        return Observable.zip(weather, aqi) {
                openMeteoWeatherResult: OpenMeteoWeatherResult,
                openMeteoAirQualityResult: OpenMeteoAirQualityResult
            ->
            convert(
                context,
                location,
                openMeteoWeatherResult,
                openMeteoAirQualityResult
            )
        }
    }

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
                convert(null, it)
            }
        }
    }

    companion object {
        private const val OPEN_METEO_AIR_QUALITY_BASE_URL = "https://air-quality-api.open-meteo.com/"
        // TODO: make it private once it is modularized
        const val OPEN_METEO_GEOCODING_BASE_URL = "https://geocoding-api.open-meteo.com/"
        private const val OPEN_METEO_WEATHER_BASE_URL = "https://api.open-meteo.com/"
    }
}