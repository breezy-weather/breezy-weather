package org.breezyweather.sources

import android.content.Context
import io.reactivex.rxjava3.core.Observable
import kotlinx.coroutines.rx3.awaitFirstOrElse
import org.breezyweather.common.basic.models.Location
import org.breezyweather.common.basic.models.weather.Base
import org.breezyweather.common.basic.models.weather.Weather
import org.breezyweather.common.exceptions.LocationException
import org.breezyweather.common.exceptions.NoNetworkException
import org.breezyweather.common.exceptions.SourceNotInstalledException
import org.breezyweather.common.exceptions.WeatherException
import org.breezyweather.common.extensions.isOnline
import org.breezyweather.common.source.HttpSource
import org.breezyweather.common.source.LocationSearchSource
import org.breezyweather.db.repositories.HistoryEntityRepository
import org.breezyweather.db.repositories.WeatherEntityRepository
import java.util.Date
import javax.inject.Inject

class WeatherHelper @Inject constructor(
    private val mSourceManager: SourceManager
) {
    suspend fun getWeather(context: Context, location: Location): Weather {
        return requestWeather(context, location).awaitFirstOrElse {
            throw WeatherException()
        }
    }

    fun requestWeather(
        context: Context, location: Location
    ): Observable<Weather> {
        if (!location.isUsable) {
            return Observable.error(LocationException())
        }

        val service = mSourceManager.getWeatherSource(location.weatherSource)
        if (service == null) {
            return Observable.error(SourceNotInstalledException())
        }

        // Debug source is not online
        if (service is HttpSource && !context.isOnline()) {
            return Observable.error(NoNetworkException())
        }

        return service
            .requestWeather(context, location.copy())
            .map { t ->
                val dailyForecast = completeDailyListFromHourlyList(
                    t.dailyForecast ?: emptyList(),
                    t.hourlyForecast ?: emptyList(),
                    location.timeZone
                )
                val hourlyForecast = completeHourlyListFromDailyList(
                    t.hourlyForecast ?: emptyList(),
                    t.dailyForecast ?: emptyList(),
                    location.timeZone
                )

                val weather = Weather(
                    base = t.base ?: Base(),
                    current = completeCurrentFromTodayDailyAndHourly(
                        t.current,
                        hourlyForecast.getOrNull(0),
                        dailyForecast.getOrNull(0),
                        location.timeZone
                    ),
                    yesterday = t.yesterday,
                    dailyForecast = dailyForecast,
                    hourlyForecast = hourlyForecast,
                    minutelyForecast = t.minutelyForecast ?: emptyList(),
                    alertList = t.alertList ?: emptyList()
                )
                WeatherEntityRepository.writeWeather(location, weather)
                if (weather.yesterday == null) {
                    weather.copy(yesterday = HistoryEntityRepository.readHistory(location, t.base?.publishDate ?: Date()))
                } else weather
            }
    }

    fun requestSearchLocations(
        context: Context,
        query: String,
        enabledSource: String
    ): Observable<List<Location>> {
        val service = mSourceManager.getWeatherSource(enabledSource)
        if (service == null) {
            return Observable.error(SourceNotInstalledException())
        }

        val searchService = if (service !is LocationSearchSource) {
            mSourceManager.getDefaultLocationSearchSource()
        } else service

        // Debug source is not online
        if (searchService is HttpSource && !context.isOnline()) {
            return Observable.error(NoNetworkException())
        }

        return searchService.requestLocationSearch(context, query).map { locationList ->
            // Rewrite all locations to point to selected weather source
            locationList.map {
                it.copy(weatherSource = service.id)
            }
        }
    }
}
