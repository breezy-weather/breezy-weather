package org.breezyweather.weather

import android.content.Context
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.disposables.CompositeDisposable
import kotlinx.coroutines.rx3.awaitSingle
import org.breezyweather.common.basic.models.Location
import org.breezyweather.common.basic.models.options.provider.WeatherSource
import org.breezyweather.common.basic.models.weather.Weather
import org.breezyweather.common.exceptions.LocationException
import org.breezyweather.common.exceptions.LocationSearchException
import org.breezyweather.common.exceptions.NoNetworkException
import org.breezyweather.common.exceptions.ParsingException
import org.breezyweather.common.extensions.isOnline
import org.breezyweather.db.repositories.HistoryEntityRepository
import org.breezyweather.db.repositories.WeatherEntityRepository
import javax.inject.Inject

class WeatherHelper @Inject constructor(
    private val mServiceSet: WeatherServiceSet,
    private val mCompositeDisposable: CompositeDisposable
) {
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
        enabledSource: WeatherSource?
    ): Observable<List<Location>> {
        return if (enabledSource == null) {
            Observable.error(LocationSearchException())
        } else mServiceSet[enabledSource].requestLocationSearch(context, query)
    }

    fun cancel() {
        mServiceSet.all.forEach { it.cancel() }
        mCompositeDisposable.clear()
    }
}
