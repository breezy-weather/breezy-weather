package org.breezyweather.weather.accu

import android.content.Context
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.disposables.CompositeDisposable
import org.breezyweather.BreezyWeather
import org.breezyweather.common.basic.models.Location
import org.breezyweather.common.rxjava.ApiObserver
import org.breezyweather.common.rxjava.ObserverContainer
import org.breezyweather.common.rxjava.SchedulerTransformer
import org.breezyweather.main.utils.RequestErrorType
import org.breezyweather.settings.SettingsManager
import org.breezyweather.weather.WeatherService
import org.breezyweather.weather.accu.json.*
import javax.inject.Inject

class AccuWeatherService @Inject constructor(
    private val mApi: AccuWeatherApi,
    private val mCompositeDisposable: CompositeDisposable
) : WeatherService() {
    protected fun getApiKey(context: Context) = SettingsManager.getInstance(context).providerAccuWeatherKey

    override fun isConfigured(context: Context) = getApiKey(context).isNotEmpty()

    override fun requestWeather(context: Context, location: Location, callback: RequestWeatherCallback) {
        if (!isConfigured(context)) {
            callback.requestWeatherFailed(location, RequestErrorType.API_KEY_REQUIRED_MISSING)
            return
        }
        val apiKey = getApiKey(context)
        val settings = SettingsManager.getInstance(context)
        val languageCode = settings.language.code
        val current = mApi.getCurrent(
            location.cityId,
            apiKey,
            languageCode,
            details = true
        )
        val daily = mApi.getDaily(
            settings.customAccuDays.id,
            location.cityId,
            apiKey,
            languageCode,
            details = true,
            metric = true // Converted later
        )
        val hourly = mApi.getHourly(
            settings.customAccuHours.id,
            location.cityId,
            apiKey,
            languageCode,
            details = true,
            metric = true // Converted later
        )
        val minute = mApi.getMinutely(
            apiKey,
            location.latitude.toString() + "," + location.longitude,
            languageCode,
            details = true
        ).onErrorResumeNext {
            Observable.create { emitter ->
                emitter.onNext(AccuMinutelyResult())
            }
        }
        val alert = mApi.getAlert(
            apiKey,
            location.latitude.toString() + "," + location.longitude,
            languageCode,
            details = true
        ).onErrorResumeNext {
            Observable.create { emitter ->
                emitter.onNext(ArrayList())
            }
        }
        val airQuality = mApi.getAirQuality(
            location.cityId, apiKey,
            pollutants = true,
            languageCode
        ).onErrorResumeNext {
            Observable.create { emitter ->
                emitter.onNext(AccuAirQualityResult())
            }
        }
        Observable.zip(current, daily, hourly, minute, alert, airQuality) {
            accuRealtimeResults: List<AccuCurrentResult>,
            accuDailyResult: AccuForecastDailyResult,
            accuHourlyResults: List<AccuForecastHourlyResult>,
            accuMinutelyResult: AccuMinutelyResult,
            accuAlertResults: List<AccuAlertResult>,
            accuAirQualityResult: AccuAirQualityResult
            ->
            convert(
                context,
                location,
                accuRealtimeResults[0],
                accuDailyResult,
                accuHourlyResults,
                accuMinutelyResult,
                accuAlertResults,
                accuAirQualityResult
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
        if (!isConfigured(context)) {
            return emptyList()
        }
        val apiKey = getApiKey(context)
        val languageCode = SettingsManager.getInstance(context).language.code
        var resultList: List<AccuLocationResult>? = null
        try {
            resultList = mApi.callWeatherLocation(
                apiKey,
                query,
                languageCode,
                details = false,
                alias = "Always"
            ).execute().body()
        } catch (e: Exception) {
            if (BreezyWeather.instance.debugMode) {
                e.printStackTrace()
            }
        }
        // TODO: Why? This will use searched terms as zip code even if the zip code is incomplete
        val zipCode = if (query.matches("[a-zA-Z0-9]*".toRegex())) query else null
        return resultList?.map {
            convert(null, it, zipCode)
        } ?: emptyList()
    }

    override fun requestReverseLocationSearch(
        context: Context,
        location: Location,
        callback: RequestLocationCallback
    ) {
        if (!isConfigured(context)) {
            callback.requestLocationFailed(
                location.latitude.toString() + "," + location.longitude,
                RequestErrorType.API_KEY_REQUIRED_MISSING
            )
            return
        }
        val apiKey = getApiKey(context)
        val languageCode = SettingsManager.getInstance(context).language.code
        mApi.getWeatherLocationByGeoPosition(
            apiKey,
            languageCode,
            details = false,
            location.latitude.toString() + "," + location.longitude
        ).compose(SchedulerTransformer.create())
            .subscribe(ObserverContainer(mCompositeDisposable, object : ApiObserver<AccuLocationResult>() {
                override fun onSucceed(t: AccuLocationResult) {
                    val locationList: MutableList<Location> = ArrayList()
                    locationList.add(convert(location, t, null))
                    callback.requestLocationSuccess(
                        location.latitude.toString() + "," + location.longitude,
                        locationList
                    )
                }

                override fun onFailed() {
                    if (isApiLimitReached) {
                        callback.requestLocationFailed(
                            location.latitude.toString() + "," + location.longitude,
                            RequestErrorType.API_LIMIT_REACHED
                        )
                    } else if (isApiUnauthorized) {
                        callback.requestLocationFailed(
                            location.latitude.toString() + "," + location.longitude,
                            RequestErrorType.API_UNAUTHORIZED
                        )
                    } else {
                        callback.requestLocationFailed(
                            location.latitude.toString() + "," + location.longitude,
                            RequestErrorType.REVERSE_GEOCODING_FAILED
                        )
                    }
                }
            }))
    }

    override fun cancel() {
        mCompositeDisposable.clear()
    }
}