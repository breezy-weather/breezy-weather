package org.breezyweather.weather

import android.content.Context
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.disposables.CompositeDisposable
import org.breezyweather.common.basic.models.Location
import org.breezyweather.common.basic.models.options.provider.WeatherSource
import org.breezyweather.common.extensions.isOnline
import org.breezyweather.common.rxjava.ApiObserver
import org.breezyweather.common.rxjava.ObserverContainer
import org.breezyweather.common.rxjava.SchedulerTransformer
import org.breezyweather.common.utils.helpers.AsyncHelper
import org.breezyweather.db.repositories.HistoryEntityRepository
import org.breezyweather.db.repositories.WeatherEntityRepository
import org.breezyweather.main.utils.RequestErrorType
import javax.inject.Inject

class WeatherHelper @Inject constructor(
    private val mServiceSet: WeatherServiceSet,
    private val mCompositeDisposable: CompositeDisposable
) {
    interface OnRequestWeatherListener {
        fun requestWeatherSuccess(requestLocation: Location)
        fun requestWeatherFailed(requestLocation: Location, requestErrorType: RequestErrorType)
    }

    interface OnRequestLocationListener {
        fun requestLocationSuccess(query: String, locationList: List<Location>)
        fun requestLocationFailed(query: String, requestErrorType: RequestErrorType)
    }

    fun requestWeather(context: Context, location: Location, listener: OnRequestWeatherListener) {
        val service = mServiceSet[location.weatherSource]
        if (!context.isOnline()) {
            listener.requestWeatherFailed(location, RequestErrorType.NETWORK_UNAVAILABLE)
            return
        } else if (!location.isUsable) {
            listener.requestWeatherFailed(location, RequestErrorType.LOCATION_FAILED)
            return
        }
        service.requestWeather(context, location.copy(), object : WeatherService.RequestWeatherCallback {
            override fun requestWeatherSuccess(requestLocation: Location) {
                val weather = requestLocation.weather
                if (weather != null) {
                    WeatherEntityRepository.writeWeather(requestLocation, weather)
                    if (weather.yesterday == null) {
                        weather.yesterday = HistoryEntityRepository.readHistory(requestLocation, weather)
                    }
                    listener.requestWeatherSuccess(requestLocation)
                } else {
                    requestWeatherFailed(requestLocation, RequestErrorType.WEATHER_REQ_FAILED)
                }
            }

            override fun requestWeatherFailed(requestLocation: Location, requestErrorType: RequestErrorType) {
                listener.requestWeatherFailed(
                    requestLocation.copy(weather = WeatherEntityRepository.readWeather(requestLocation)),
                    requestErrorType
                )
            }
        })
    }

    fun requestSearchLocations(
        context: Context,
        query: String,
        enabledSource: WeatherSource?,
        listener: OnRequestLocationListener
    ) {
        if (enabledSource == null) {
            AsyncHelper.delayRunOnUI({ listener.requestLocationFailed(query, RequestErrorType.LOCATION_SEARCH_FAILED) }, 0)
            return
        }

        // generate weather services.
        val service = mServiceSet[enabledSource]
        if (!service.isConfigured(context)) {
            AsyncHelper.delayRunOnUI({ listener.requestLocationFailed(query, RequestErrorType.API_KEY_REQUIRED_MISSING) }, 0)
            return
        }

        // generate observable list.
        Observable.create { emitter ->
            emitter.onNext(service.requestLocationSearch(context, query))
        }.compose(SchedulerTransformer.create())
            .subscribe(ObserverContainer(mCompositeDisposable, object : ApiObserver<List<Location>>() {
                override fun onSucceed(t: List<Location>) {
                    listener.requestLocationSuccess(query, t)
                }

                override fun onFailed() {
                    if (isApiLimitReached) {
                        listener.requestLocationFailed(query, RequestErrorType.API_LIMIT_REACHED)
                    } else if (isApiUnauthorized) {
                        listener.requestLocationFailed(query, RequestErrorType.API_UNAUTHORIZED)
                    } else {
                        listener.requestLocationFailed(query, RequestErrorType.LOCATION_SEARCH_FAILED)
                    }
                }
            }))
    }

    fun cancel() {
        mServiceSet.all.forEach { it.cancel() }
        mCompositeDisposable.clear()
    }
}
