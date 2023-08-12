/**
 * This file is part of Breezy Weather.
 *
 * Breezy Weather is free software: you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published by the
 * Free Software Foundation, either version 3 of the License, or (at your
 * option) any later version.
 *
 * Breezy Weather is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public
 * License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Breezy Weather. If not, see <https://www.gnu.org/licenses/>.
 */

package org.breezyweather.sources

import android.Manifest
import android.content.Context
import android.os.Build
import io.reactivex.rxjava3.core.Observable
import kotlinx.coroutines.rx3.awaitFirstOrElse
import kotlinx.serialization.MissingFieldException
import kotlinx.serialization.SerializationException
import org.breezyweather.common.basic.models.Location
import org.breezyweather.common.basic.models.weather.Base
import org.breezyweather.common.basic.models.weather.Weather
import org.breezyweather.common.basic.wrappers.SecondaryWeatherWrapper
import org.breezyweather.common.exceptions.ApiKeyMissingException
import org.breezyweather.common.exceptions.ApiLimitReachedException
import org.breezyweather.common.exceptions.InvalidLocationException
import org.breezyweather.common.exceptions.LocationException
import org.breezyweather.common.exceptions.LocationSearchException
import org.breezyweather.common.exceptions.MissingPermissionLocationBackgroundException
import org.breezyweather.common.exceptions.MissingPermissionLocationException
import org.breezyweather.common.exceptions.NoNetworkException
import org.breezyweather.common.exceptions.ParsingException
import org.breezyweather.common.exceptions.ReverseGeocodingException
import org.breezyweather.common.exceptions.SecondaryWeatherException
import org.breezyweather.common.exceptions.SourceNotInstalledException
import org.breezyweather.common.exceptions.UpdateNotAvailableYetException
import org.breezyweather.common.exceptions.WeatherException
import org.breezyweather.common.extensions.getFormattedDate
import org.breezyweather.common.extensions.hasPermission
import org.breezyweather.common.extensions.isOnline
import org.breezyweather.common.extensions.toDateNoHour
import org.breezyweather.common.source.HttpSource
import org.breezyweather.common.source.LocationResult
import org.breezyweather.common.source.LocationSearchSource
import org.breezyweather.common.source.RefreshError
import org.breezyweather.common.source.SecondaryWeatherSourceFeature
import org.breezyweather.common.source.WeatherResult
import org.breezyweather.db.repositories.HistoryEntityRepository
import org.breezyweather.db.repositories.LocationEntityRepository
import org.breezyweather.db.repositories.WeatherEntityRepository
import org.breezyweather.main.utils.RefreshErrorType
import org.breezyweather.settings.SettingsManager
import retrofit2.HttpException
import java.net.SocketTimeoutException
import java.util.Date
import java.util.TimeZone
import javax.inject.Inject

class RefreshHelper @Inject constructor(
    private val sourceManager: SourceManager
) {
    suspend fun getLocation(
        context: Context, location: Location, background: Boolean
    ): LocationResult {
        var currentErrors = mutableListOf<RefreshError>()
        val currentLocation = if (location.isCurrentPosition) {
            try {
                val currentLocationResult = requestCurrentLocation(context, location, background)
                currentErrors = currentLocationResult.errors.toMutableList()
                currentLocationResult.location
            } catch (e: Throwable) {
                e.printStackTrace()
                currentErrors.add(RefreshError(RefreshErrorType.LOCATION_FAILED))
                location
            }
        } else location

        if (currentLocation.isUsable) {
            val source = location.weatherSource
            val weatherService = sourceManager.getReverseGeocodingSource(source)
            val locationGeocoded = if (weatherService != null) {
                if (currentLocation.isCurrentPosition
                    || currentLocation.needsGeocodeRefresh
                    || !weatherService.isUsable(currentLocation)
                ) {
                    try {
                        weatherService
                            .requestReverseGeocodingLocation(context, currentLocation)
                            .map { locationList ->
                                if (locationList.isNotEmpty()) {
                                    val result = locationList[0]
                                    val locationWithGeocodeInfo = currentLocation.copy(
                                        cityId = result.cityId,
                                        timeZone = result.timeZone,
                                        country = result.country,
                                        countryCode = result.countryCode ?: "",
                                        province = result.province ?: "",
                                        provinceCode = result.provinceCode ?: "",
                                        city = result.city,
                                        district = result.district ?: "",
                                        needsGeocodeRefresh = false
                                    )
                                    LocationEntityRepository.writeLocation(locationWithGeocodeInfo)
                                    locationWithGeocodeInfo
                                } else {
                                    throw ReverseGeocodingException()
                                }
                            }.awaitFirstOrElse {
                                throw ReverseGeocodingException()
                            }
                    } catch (e: Throwable) {
                        if (location.isCurrentPosition) {
                            LocationEntityRepository.writeLocation(currentLocation)
                        }
                        return LocationResult(
                            currentLocation,
                            errors = currentErrors + listOf(
                                RefreshError(getRequestErrorType(e, RefreshErrorType.REVERSE_GEOCODING_FAILED), weatherService.name)
                            )
                        )
                    }
                } else currentLocation
            } else {
                // Returned as-is if no reverse geocoding source
                // but write in case the location service has provided us information for current
                if (location.isCurrentPosition) {
                    LocationEntityRepository.writeLocation(currentLocation)
                } else if (location.needsGeocodeRefresh) {
                    LocationEntityRepository.writeLocation(
                        currentLocation.copy(needsGeocodeRefresh = false)
                    )
                }
                currentLocation
            }
            return LocationResult(locationGeocoded, currentErrors)
        } else {
            return LocationResult(currentLocation, currentErrors)
        }
    }

    suspend fun requestCurrentLocation(
        context: Context, location: Location, background: Boolean
    ): LocationResult {
        val locationSource = SettingsManager.getInstance(context).locationSource
        val locationService = sourceManager.getLocationSourceOrDefault(locationSource)
        val errors = mutableListOf<RefreshError>()
        if (!context.isOnline()) {
            errors.add(RefreshError(RefreshErrorType.NETWORK_UNAVAILABLE))
        }
        if (locationService.permissions.isNotEmpty()) {
            // if needs any location permission.
            if (!context.hasPermission(Manifest.permission.ACCESS_COARSE_LOCATION)
                && !context.hasPermission(Manifest.permission.ACCESS_FINE_LOCATION)
            ) {
                errors.add(RefreshError(RefreshErrorType.ACCESS_LOCATION_PERMISSION_MISSING))
            }
            if (background) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q
                    && !context.hasPermission(Manifest.permission.ACCESS_BACKGROUND_LOCATION)
                ) {
                    errors.add(RefreshError(RefreshErrorType.ACCESS_BACKGROUND_LOCATION_PERMISSION_MISSING))
                }
            }
        }
        if (errors.isNotEmpty()) {
            return LocationResult(location, errors)
        }

        return try {
            val result = locationService
                .requestLocation(context)
                .awaitFirstOrElse {
                    throw LocationException()
                }
            return LocationResult(
                location.copy(
                    latitude = result.latitude,
                    longitude = result.longitude,
                    /*
                     * Don’t keep old data as the user can have changed position
                     * It avoids keeping old data from a reverse geocoding-compatible weather source
                     * onto a weather source without reverse geocoding
                     */
                    timeZone = result.timeZone ?: TimeZone.getDefault(),
                    country = result.country ?: "",
                    countryCode = result.countryCode ?: "",
                    province = result.province ?: "",
                    provinceCode = result.provinceCode ?: "",
                    city = result.city ?: "",
                    district = result.district ?: ""
                )
            )
        } catch (e: Throwable) {
            LocationResult(
                location,
                errors = listOf(
                    RefreshError(getRequestErrorType(e, RefreshErrorType.LOCATION_FAILED), locationService.name)
                )
            )
        }
    }

    fun getPermissions(context: Context): List<String> {
        // if IP:    none.
        // else:
        //      R:   foreground location. (set background location enabled manually)
        //      Q:   foreground location + background location.
        //      K-P: foreground location.
        val locationSource = SettingsManager.getInstance(context).locationSource
        val service = sourceManager.getLocationSourceOrDefault(locationSource)
        val permissions: MutableList<String> = service.permissions.toMutableList()
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q || permissions.isEmpty()) {
            // device has no background location permission or locate by IP.
            return permissions
        }
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.R) {
            permissions.add(Manifest.permission.ACCESS_BACKGROUND_LOCATION)
        }
        return permissions
    }

    suspend fun getWeather(
        context: Context, location: Location
    ): WeatherResult {
        try {
            if (!location.isUsable
                || location.needsGeocodeRefresh) {
                return WeatherResult(
                    errors = listOf(
                        RefreshError(RefreshErrorType.INVALID_LOCATION)
                    )
                )
            }

            val service = sourceManager.getMainWeatherSource(location.weatherSource)
            if (service == null) {
                return WeatherResult(
                    errors = listOf(
                        RefreshError(RefreshErrorType.SOURCE_NOT_INSTALLED, location.weatherSource)
                    )
                )
            }

            // Debug source is not online
            if (service is HttpSource && !context.isOnline()) {
                return WeatherResult(
                    errors = listOf(
                        RefreshError(RefreshErrorType.NETWORK_UNAVAILABLE)
                    )
                )
            }

            // Group data requested to secondary sources by source
            // TODO: Can probably be made more readable
            val mainFeaturesIgnored: MutableList<SecondaryWeatherSourceFeature> = mutableListOf()
            val secondarySources: MutableMap<String, MutableList<SecondaryWeatherSourceFeature>> =
                mutableMapOf()
            with(location) {
                if (!airQualitySource.isNullOrEmpty() && airQualitySource != weatherSource) {
                    secondarySources[airQualitySource] =
                        mutableListOf(SecondaryWeatherSourceFeature.FEATURE_AIR_QUALITY)
                    mainFeaturesIgnored.add(SecondaryWeatherSourceFeature.FEATURE_AIR_QUALITY)
                }
                if (!allergenSource.isNullOrEmpty() && allergenSource != weatherSource) {
                    if (secondarySources.containsKey(allergenSource)) {
                        secondarySources[allergenSource]!!.add(SecondaryWeatherSourceFeature.FEATURE_ALLERGEN)
                    } else {
                        secondarySources[allergenSource] =
                            mutableListOf(SecondaryWeatherSourceFeature.FEATURE_ALLERGEN)
                    }
                    mainFeaturesIgnored.add(SecondaryWeatherSourceFeature.FEATURE_ALLERGEN)
                }
                if (!minutelySource.isNullOrEmpty() && minutelySource != weatherSource) {
                    if (secondarySources.containsKey(minutelySource)) {
                        secondarySources[minutelySource]!!.add(SecondaryWeatherSourceFeature.FEATURE_MINUTELY)
                    } else {
                        secondarySources[minutelySource] =
                            mutableListOf(SecondaryWeatherSourceFeature.FEATURE_MINUTELY)
                    }
                    mainFeaturesIgnored.add(SecondaryWeatherSourceFeature.FEATURE_MINUTELY)
                }
                if (!alertSource.isNullOrEmpty() && alertSource != weatherSource) {
                    if (secondarySources.containsKey(alertSource)) {
                        secondarySources[alertSource]!!.add(SecondaryWeatherSourceFeature.FEATURE_ALERT)
                    } else {
                        secondarySources[alertSource] =
                            mutableListOf(SecondaryWeatherSourceFeature.FEATURE_ALERT)
                    }
                    mainFeaturesIgnored.add(SecondaryWeatherSourceFeature.FEATURE_ALERT)
                }
            }

            // MAIN SOURCE
            val mainWeather = try {
                service
                    .requestWeather(context, location.copy(), mainFeaturesIgnored)
                    .awaitFirstOrElse {
                        throw WeatherException()
                    }
            } catch (e: Throwable) {
                return WeatherResult(
                    errors = listOf(
                        RefreshError(getRequestErrorType(e, RefreshErrorType.WEATHER_REQ_FAILED), service.name)
                    )
                )
            }

            // SECONDARY SOURCES
            val yesterdayMidnight = Date(Date().time - 24 * 3600 * 1000)
                .getFormattedDate(location.timeZone, "yyyy-MM-dd")
                .toDateNoHour(location.timeZone)!!
            val errors = mutableListOf<RefreshError>()
            val secondaryWeatherWrapper = if (secondarySources.isNotEmpty()) {
                val secondarySourceCalls = mutableMapOf<String, SecondaryWeatherWrapper?>()
                secondarySources.forEach { entry ->
                    val secondaryService = sourceManager.getSecondaryWeatherSource(entry.key)
                    if (secondaryService == null) {
                        errors.add(RefreshError(RefreshErrorType.SOURCE_NOT_INSTALLED, entry.key))
                    } else {
                        entry.value.forEach {
                            // We could also check for isFeatureSupportedForLocation but it’s probably best to
                            // let the source decide if it wants to throw the error itself
                            if (!secondaryService.supportedFeatures.contains(it)) {
                                errors.add(
                                    RefreshError(
                                        RefreshErrorType.UNSUPPORTED_FEATURE,
                                        secondaryService.name
                                    )
                                )
                            }
                        }
                        secondarySourceCalls[entry.key] = try {
                            secondaryService
                                .requestSecondaryWeather(context, location, entry.value)
                                .awaitFirstOrElse {
                                    throw SecondaryWeatherException()
                                }
                        } catch (e: Throwable) {
                            errors.add(RefreshError(getRequestErrorType(e, RefreshErrorType.SECONDARY_WEATHER_FAILED), secondaryService.name))
                            null
                        }
                    }
                }

                /**
                 * Make sure we return data from the correct secondary source
                 */
                SecondaryWeatherWrapper(
                    airQuality = if (!location.airQualitySource.isNullOrEmpty() && location.airQualitySource != location.weatherSource) {
                        secondarySourceCalls[location.airQualitySource]?.airQuality ?: getAirQualityWrapperFromWeather(location.weather, yesterdayMidnight)
                    } else null,
                    allergen = if (!location.allergenSource.isNullOrEmpty() && location.allergenSource != location.weatherSource) {
                        secondarySourceCalls[location.allergenSource]?.allergen ?: getAllergenWrapperFromWeather(location.weather, yesterdayMidnight)
                    } else null,
                    minutelyForecast = if (!location.minutelySource.isNullOrEmpty() && location.minutelySource != location.weatherSource) {
                        secondarySourceCalls[location.minutelySource]?.minutelyForecast ?: getMinutelyFromWeather(location.weather)
                    } else null,
                    alertList = if (!location.alertSource.isNullOrEmpty() && location.alertSource != location.weatherSource) {
                        secondarySourceCalls[location.alertSource]?.alertList ?: getAlertsFromWeather(location.weather)
                    } else null
                )
            } else null

            // TODO: Merge with existing weather data back to yesterday 00:00

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

            val hourlyMissingComputed = computeMissingHourlyData(
                mergeSecondaryWeatherDataIntoHourlyWrapperList(
                    mainWeather.hourlyForecast, secondaryWeatherWrapperCompleted
                )
            )
            val dailyForecast = completeDailyListFromHourlyList(
                mergeSecondaryWeatherDataIntoDailyList(
                    mainWeather.dailyForecast, secondaryWeatherWrapperCompleted
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
                base = mainWeather.base ?: Base(),
                current = completeCurrentFromTodayDailyAndHourly(
                    mainWeather.current,
                    hourlyForecast.getOrNull(0),
                    dailyForecast.getOrNull(0),
                    location.timeZone
                ),
                yesterday = mainWeather.yesterday,
                dailyForecast = dailyForecast,
                hourlyForecast = hourlyForecast,
                minutelyForecast = secondaryWeatherWrapper?.minutelyForecast
                    ?: mainWeather.minutelyForecast ?: emptyList(),
                alertList = (secondaryWeatherWrapper?.alertList ?: mainWeather.alertList)?.filter {
                    // Don’t save past alerts in database
                    it.endDate == null || it.endDate.time > Date().time
                } ?: emptyList()
            )
            WeatherEntityRepository.writeWeather(location, weather)
            return WeatherResult(
                if (weather.yesterday == null) {
                    weather.copy(
                        yesterday = HistoryEntityRepository.readHistory(
                            location,
                            mainWeather.base?.publishDate ?: Date()
                        )
                    )
                } else weather,
                errors
            )
        } catch (e: Throwable) {
            e.printStackTrace()
            return WeatherResult(
                errors = listOf(
                    RefreshError(RefreshErrorType.WEATHER_REQ_FAILED)
                )
            )
        }
    }

    private fun getRequestErrorType(
        e: Throwable,
        defaultRefreshError: RefreshErrorType
    ): RefreshErrorType {
        return when (e) {
            is NoNetworkException -> RefreshErrorType.NETWORK_UNAVAILABLE
            is HttpException -> {
                when (e.code()) {
                    401, 403 -> RefreshErrorType.API_UNAUTHORIZED
                    409, 429 -> RefreshErrorType.API_LIMIT_REACHED
                    else -> {
                        e.printStackTrace()
                        defaultRefreshError
                    }
                }
            }
            is SocketTimeoutException -> RefreshErrorType.SERVER_TIMEOUT
            is ApiLimitReachedException -> RefreshErrorType.API_LIMIT_REACHED
            is ApiKeyMissingException -> RefreshErrorType.API_KEY_REQUIRED_MISSING
            is UpdateNotAvailableYetException -> RefreshErrorType.UPDATE_NOT_YET_AVAILABLE
            is InvalidLocationException -> RefreshErrorType.INVALID_LOCATION
            is LocationException -> RefreshErrorType.LOCATION_FAILED
            is MissingPermissionLocationException -> RefreshErrorType.ACCESS_LOCATION_PERMISSION_MISSING
            is MissingPermissionLocationBackgroundException -> RefreshErrorType.ACCESS_BACKGROUND_LOCATION_PERMISSION_MISSING
            is ReverseGeocodingException -> RefreshErrorType.REVERSE_GEOCODING_FAILED
            is SecondaryWeatherException -> RefreshErrorType.SECONDARY_WEATHER_FAILED
            is MissingFieldException, is SerializationException, is ParsingException -> {
                e.printStackTrace()
                RefreshErrorType.PARSING_ERROR
            }
            is SourceNotInstalledException -> RefreshErrorType.SOURCE_NOT_INSTALLED
            is LocationSearchException -> RefreshErrorType.LOCATION_SEARCH_FAILED
            is WeatherException -> RefreshErrorType.WEATHER_REQ_FAILED
            else -> {
                e.printStackTrace()
                defaultRefreshError
            }
        }
    }

    fun requestSearchLocations(
        context: Context,
        query: String,
        enabledSource: String
    ): Observable<List<Location>> {
        val service = sourceManager.getMainWeatherSource(enabledSource)
        if (service == null) {
            return Observable.error(SourceNotInstalledException())
        }

        val searchService = if (service !is LocationSearchSource) {
            sourceManager.getLocationSearchSourceOrDefault(SettingsManager.getInstance(context).locationSearchSource)
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
