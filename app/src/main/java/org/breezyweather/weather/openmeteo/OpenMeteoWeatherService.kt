package org.breezyweather.weather.openmeteo

import android.content.Context
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.disposables.CompositeDisposable
import org.breezyweather.BreezyWeather
import org.breezyweather.common.basic.models.Location
import org.breezyweather.common.basic.models.options.provider.WeatherSource
import org.breezyweather.common.rxjava.ApiObserver
import org.breezyweather.common.rxjava.ObserverContainer
import org.breezyweather.common.rxjava.SchedulerTransformer
import org.breezyweather.main.utils.RequestErrorType
import org.breezyweather.settings.SettingsManager
import org.breezyweather.weather.WeatherService
import org.breezyweather.weather.openmeteo.json.OpenMeteoAirQualityResult
import org.breezyweather.weather.openmeteo.json.OpenMeteoLocationResults
import org.breezyweather.weather.openmeteo.json.OpenMeteoWeatherResult
import javax.inject.Inject

class OpenMeteoWeatherService @Inject constructor(
    private val mWeatherApi: OpenMeteoWeatherApi,
    private val mGeocodingApi: OpenMeteoGeocodingApi,
    private val mAirQualityApi: OpenMeteoAirQualityApi,
    private val mCompositeDisposable: CompositeDisposable
) : WeatherService() {
    override fun isConfigured(context: Context) = true

    override fun requestWeather(context: Context, location: Location, callback: RequestWeatherCallback) {
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
        Observable.zip(weather, aqi) {
            openMeteoWeatherResult: OpenMeteoWeatherResult,
            openMeteoAirQualityResult: OpenMeteoAirQualityResult
            ->
            convert(
                context,
                location,
                openMeteoWeatherResult,
                openMeteoAirQualityResult
            )
        }.compose(SchedulerTransformer.create())
            .subscribe(ObserverContainer(mCompositeDisposable, object : ApiObserver<WeatherResultWrapper>() {
                override fun onSucceed(t: WeatherResultWrapper) {
                    if (t.result != null) {
                        callback.requestWeatherSuccess(location.copy(weather = t.result))
                    } else {
                        onFailed()
                    }
                }

                override fun onFailed() {
                    callback.requestWeatherFailed(location, RequestErrorType.WEATHER_REQ_FAILED)
                }
            }))
    }

    override fun requestLocationSearch(
        context: Context,
        query: String
    ): List<Location> {
        val languageCode = SettingsManager.getInstance(context).language.code
        var apiResults: OpenMeteoLocationResults? = null
        try {
            apiResults = mGeocodingApi.callWeatherLocation(
                query,
                count = 20,
                languageCode
            ).execute().body()
        } catch (e: Exception) {
            if (BreezyWeather.instance.debugMode) {
                e.printStackTrace()
            }
        }
        return apiResults?.results?.map {
            convert(null, it, WeatherSource.OPEN_METEO)
        } ?: emptyList()
    }

    override fun requestReverseLocationSearch(
        context: Context,
        location: Location,
        callback: RequestLocationCallback
    ) {
        // Currently there is no reverse geocoding, so we just return the same location
        // TimeZone is initialized with the TimeZone from the phone (which is probably the same as the current position)
        // Hopefully, one day we will have a reverse geocoding API
        val locationList: MutableList<Location> = ArrayList()
        locationList.add(location.copy(cityId = location.latitude.toString() + "," + location.longitude))
        callback.requestLocationSuccess(
            location.latitude.toString() + "," + location.longitude,
            locationList
        )
    }

    override fun cancel() {
        mCompositeDisposable.clear()
    }
}