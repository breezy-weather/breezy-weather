package org.breezyweather.weather

import android.content.Context
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.disposables.CompositeDisposable
import kotlinx.coroutines.rx3.awaitSingle
import org.breezyweather.R
import org.breezyweather.common.basic.models.Location
import org.breezyweather.common.basic.models.options.provider.WeatherSource
import org.breezyweather.common.basic.models.weather.Weather
import org.breezyweather.common.exceptions.LocationException
import org.breezyweather.common.exceptions.NoNetworkException
import org.breezyweather.common.exceptions.ParsingException
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
    // TODO: To be deprecated
    interface OnRequestSearchLocationsListener {
        fun requestLocationSuccess(query: String, locationList: List<Location>)
        fun requestLocationFailed(query: String, requestErrorType: RequestErrorType)
    }

    suspend fun getWeather(context: Context, location: Location): Weather {
        return requestWeather(context, location).awaitSingle()
    }

    fun requestWeather(
        context: Context, location: Location
    ): Observable<Weather> {
        val service = mServiceSet[location.weatherSource]
        if (!context.isOnline()) {
            return Observable.error(NoNetworkException())
        } else if (!location.isUsable) {
            return Observable.error(LocationException())
        }

        return service
            .requestWeather(context, location.copy())
            .map { t ->
                if (t.result != null) {
                    WeatherEntityRepository.writeWeather(location, t.result)
                    if (t.result.yesterday == null) {
                        t.result.yesterday = HistoryEntityRepository.readHistory(location, t.result)
                    }
                    t.result
                } else {
                    throw ParsingException()
                }
            }
    }

    fun requestSearchLocations(
        context: Context,
        query: String,
        enabledSource: WeatherSource?,
        listener: OnRequestSearchLocationsListener
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
