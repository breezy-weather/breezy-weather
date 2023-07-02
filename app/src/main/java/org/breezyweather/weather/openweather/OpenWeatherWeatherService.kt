package org.breezyweather.weather.openweather

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
import org.breezyweather.weather.openmeteo.OpenMeteoGeocodingApi
import org.breezyweather.weather.openmeteo.convert
import org.breezyweather.weather.openmeteo.json.OpenMeteoLocationResults
import org.breezyweather.weather.openweather.json.OpenWeatherAirPollutionResult
import org.breezyweather.weather.openweather.json.OpenWeatherOneCallResult
import javax.inject.Inject

class OpenWeatherWeatherService @Inject constructor(
    private val mApi: OpenWeatherApi,
    private val mGeocodingApi: OpenMeteoGeocodingApi,
    private val mCompositeDisposable: CompositeDisposable
) : WeatherService() {
    protected fun getApiKey(context: Context) = SettingsManager.getInstance(context).providerOpenWeatherKey

    override fun isConfigured(context: Context) = getApiKey(context).isNotEmpty()

    override fun requestWeather(context: Context, location: Location, callback: RequestWeatherCallback) {
        if (!isConfigured(context)) {
            callback.requestWeatherFailed(location, RequestErrorType.API_KEY_REQUIRED_MISSING)
            return
        }
        val apiKey = getApiKey(context)
        val languageCode = SettingsManager.getInstance(context).language.code
        val oneCall = mApi.getOneCall(
            SettingsManager.getInstance(context).customOpenWeatherOneCallVersion.id,
            apiKey,
            location.latitude.toDouble(),
            location.longitude.toDouble(),
            "metric",
            languageCode
        )
        val airPollution = mApi.getAirPollution(
            apiKey,
            location.latitude.toDouble(),
            location.longitude.toDouble()
        ).onErrorResumeNext {
            Observable.create { emitter ->
                emitter.onNext(OpenWeatherAirPollutionResult())
            }
        }
        Observable.zip(oneCall, airPollution) {
            openWeatherOneCallResult: OpenWeatherOneCallResult,
            openWeatherAirPollutionResult: OpenWeatherAirPollutionResult
            ->
            convert(
                context,
                location,
                openWeatherOneCallResult,
                openWeatherAirPollutionResult
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
                    if (isApiLimitReached) {
                        callback.requestWeatherFailed(location, RequestErrorType.API_LIMIT_REACHED)
                    } else if (isApiUnauthorized) {
                        callback.requestWeatherFailed(location, RequestErrorType.API_UNAUTHORIZED)
                    } else {
                        callback.requestWeatherFailed(location, RequestErrorType.WEATHER_REQ_FAILED)
                    }
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
            convert(null, it, WeatherSource.OPEN_WEATHER)
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