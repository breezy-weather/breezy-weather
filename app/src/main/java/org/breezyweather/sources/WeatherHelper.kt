package org.breezyweather.sources

import android.content.Context
import io.reactivex.rxjava3.core.Observable
import kotlinx.coroutines.rx3.awaitFirstOrElse
import org.breezyweather.common.basic.models.Location
import org.breezyweather.common.basic.models.weather.Base
import org.breezyweather.common.basic.models.weather.Weather
import org.breezyweather.common.basic.wrappers.SecondaryWeatherWrapper
import org.breezyweather.common.exceptions.LocationException
import org.breezyweather.common.exceptions.NoNetworkException
import org.breezyweather.common.exceptions.SecondaryWeatherException
import org.breezyweather.common.exceptions.SourceNotInstalledException
import org.breezyweather.common.exceptions.WeatherException
import org.breezyweather.common.extensions.isOnline
import org.breezyweather.common.source.HttpSource
import org.breezyweather.common.source.LocationSearchSource
import org.breezyweather.common.source.SecondaryWeatherSourceFeature
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

    suspend fun requestWeather(
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

        // Group data requested to secondary sources by source
        // TODO: Can probably be made more readable
        val mainFeaturesIgnored: MutableList<SecondaryWeatherSourceFeature> = mutableListOf()
        val secondarySources: MutableMap<String, MutableList<SecondaryWeatherSourceFeature>> = mutableMapOf()
        if (!location.airQualitySource.isNullOrEmpty()
            && location.airQualitySource != location.weatherSource) {
            secondarySources[location.airQualitySource] = mutableListOf(SecondaryWeatherSourceFeature.FEATURE_AIR_QUALITY)
            mainFeaturesIgnored.add(SecondaryWeatherSourceFeature.FEATURE_AIR_QUALITY)
        }
        if (!location.allergenSource.isNullOrEmpty()
            && location.allergenSource != location.weatherSource) {
            if (secondarySources.containsKey(location.allergenSource)) {
                secondarySources[location.allergenSource]!!.add(SecondaryWeatherSourceFeature.FEATURE_ALLERGEN)
            } else {
                secondarySources[location.allergenSource] = mutableListOf(SecondaryWeatherSourceFeature.FEATURE_ALLERGEN)
            }
            mainFeaturesIgnored.add(SecondaryWeatherSourceFeature.FEATURE_ALLERGEN)
        }
        if (!location.minutelySource.isNullOrEmpty()
            && location.minutelySource != location.weatherSource) {
            if (secondarySources.containsKey(location.minutelySource)) {
                secondarySources[location.minutelySource]!!.add(SecondaryWeatherSourceFeature.FEATURE_MINUTELY)
            } else {
                secondarySources[location.minutelySource] = mutableListOf(SecondaryWeatherSourceFeature.FEATURE_MINUTELY)
            }
            mainFeaturesIgnored.add(SecondaryWeatherSourceFeature.FEATURE_MINUTELY)
        }
        if (!location.alertSource.isNullOrEmpty()
            && location.alertSource != location.weatherSource) {
            if (secondarySources.containsKey(location.alertSource)) {
                secondarySources[location.alertSource]!!.add(SecondaryWeatherSourceFeature.FEATURE_ALERT)
            } else {
                secondarySources[location.alertSource] = mutableListOf(SecondaryWeatherSourceFeature.FEATURE_ALERT)
            }
            mainFeaturesIgnored.add(SecondaryWeatherSourceFeature.FEATURE_ALERT)
        }
        val secondaryWeatherWrapper = requestSecondaryWeather(
            context, location, secondarySources
        )

        /**
         * Most sources starts hourly forecast at current time (13:00 for example)
         * while some complementary sources starts at 00:00.
         * Some others have a 3-hourly starting from day 3+
         * Only relying on the main source leads to missing hourly data that is used for daily
         * computation (for example, daily air quality and allergen)
         * For this reason, we complete missing data earlier for the secondary data
         */
        val secondaryWeatherWrapperCompleted = completeMissingSecondaryWeatherDailyData(
            secondaryWeatherWrapper, location.timeZone
        )

        return service
            .requestWeather(context, location.copy(), mainFeaturesIgnored)
            .map { t ->
                val hourlyMissingComputed = computeMissingHourlyData(
                    mergeSecondaryWeatherDataIntoHourlyWrapperList(
                        t.hourlyForecast, secondaryWeatherWrapperCompleted
                    )
                )
                val dailyForecast = completeDailyListFromHourlyList(
                    mergeSecondaryWeatherDataIntoDailyList(
                        t.dailyForecast, secondaryWeatherWrapperCompleted
                    ),
                    hourlyMissingComputed,
                    location
                )
                val hourlyForecast = completeHourlyListFromDailyList(
                    hourlyMissingComputed,
                    dailyForecast,
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
                    minutelyForecast = secondaryWeatherWrapper?.minutelyForecast
                        ?: t.minutelyForecast ?: emptyList(),
                    alertList = (secondaryWeatherWrapper?.alertList ?: t.alertList)?.filter {
                        // Don’t save past alerts in database
                        it.endDate == null || it.endDate.time > Date().time
                    } ?: emptyList()
                )
                WeatherEntityRepository.writeWeather(location, weather)
                if (weather.yesterday == null) {
                    weather.copy(yesterday = HistoryEntityRepository.readHistory(location, t.base?.publishDate ?: Date()))
                } else weather
            }
    }

    /**
     * TODO: Can probably be optimized with coroutines
     */
    private suspend fun requestSecondaryWeather(
        context: Context, location: Location,
        secondarySources: MutableMap<String, MutableList<SecondaryWeatherSourceFeature>>
    ): SecondaryWeatherWrapper? {
        if (secondarySources.isEmpty()) return null
        
        val secondarySourceCalls = mutableMapOf<String, SecondaryWeatherWrapper>()
        secondarySources.forEach { entry ->
            val service = mSourceManager.getSecondaryWeatherSource(entry.key)
            if (service == null) {
                throw SourceNotInstalledException()
            }
            entry.value.forEach {
                // We could also check for isFeatureSupportedForLocation but it’s probably best to
                // let the source decide if it wants to throw the error itself
                if (!service.supportedFeatures.contains(it)) {
                    // TODO: throw UnsupportedSecondaryWeatherFeature()
                    throw SecondaryWeatherException()
                }
            }
            secondarySourceCalls[entry.key] =
                service.requestSecondaryWeather(context, location, entry.value).awaitFirstOrElse {
                    throw SecondaryWeatherException()
                }
        }

        /**
         * Make sure we return data from the correct secondary source
         */
        return SecondaryWeatherWrapper(
            airQuality = if (!location.airQualitySource.isNullOrEmpty() && location.airQualitySource != location.weatherSource) {
                secondarySourceCalls[location.airQualitySource]?.airQuality
            } else null,
            allergen = if (!location.allergenSource.isNullOrEmpty() && location.allergenSource != location.weatherSource) {
                secondarySourceCalls[location.allergenSource]?.allergen
            } else null,
            minutelyForecast = if (!location.minutelySource.isNullOrEmpty() && location.minutelySource != location.weatherSource) {
                secondarySourceCalls[location.minutelySource]?.minutelyForecast
            } else null,
            alertList = if (!location.alertSource.isNullOrEmpty() && location.alertSource != location.weatherSource) {
                secondarySourceCalls[location.alertSource]?.alertList
            } else null
        )
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
