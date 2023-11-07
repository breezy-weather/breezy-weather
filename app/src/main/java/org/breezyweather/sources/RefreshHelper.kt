/**
 * This file is part of Breezy Weather.
 *
 * Breezy Weather is free software: you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published by the
 * Free Software Foundation, version 3 of the License.
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
import org.breezyweather.BreezyWeather
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
import org.breezyweather.common.exceptions.WeatherException
import org.breezyweather.common.extensions.getFormattedDate
import org.breezyweather.common.extensions.hasPermission
import org.breezyweather.common.extensions.isOnline
import org.breezyweather.common.extensions.toCalendarWithTimeZone
import org.breezyweather.common.extensions.toDateNoHour
import org.breezyweather.common.source.ConfigurableSource
import org.breezyweather.common.source.HttpSource
import org.breezyweather.common.source.LocationResult
import org.breezyweather.common.source.LocationSearchSource
import org.breezyweather.common.source.RefreshError
import org.breezyweather.common.source.SecondaryWeatherSourceFeature
import org.breezyweather.common.source.WeatherResult
import org.breezyweather.common.utils.helpers.LogHelper
import org.breezyweather.db.repositories.LocationEntityRepository
import org.breezyweather.db.repositories.WeatherEntityRepository
import org.breezyweather.main.utils.RefreshErrorType
import org.breezyweather.settings.SettingsManager
import retrofit2.HttpException
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import java.util.Calendar
import java.util.Date
import java.util.Locale
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
                if (location.isCurrentPosition || location.needsGeocodeRefresh) {
                    val currentLocationNoGeocodeRefresh = currentLocation.copy(needsGeocodeRefresh = false)
                    LocationEntityRepository.writeLocation(currentLocationNoGeocodeRefresh)
                    currentLocationNoGeocodeRefresh
                } else {
                    currentLocation
                }
            }
            return LocationResult(locationGeocoded, currentErrors)
        } else {
            return LocationResult(currentLocation, currentErrors)
        }
    }

    private suspend fun requestCurrentLocation(
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
                    location.weather,
                    listOf(RefreshError(RefreshErrorType.INVALID_LOCATION))
                )
            }

            val service = sourceManager.getMainWeatherSource(location.weatherSource)
            if (service == null) {
                return WeatherResult(
                    location.weather,
                    listOf(
                        RefreshError(RefreshErrorType.SOURCE_NOT_INSTALLED, location.weatherSource)
                    )
                )
            }

            // Debug source is not online
            if (service is HttpSource && !context.isOnline()) {
                return WeatherResult(
                    location.weather,
                    listOf(
                        RefreshError(RefreshErrorType.NETWORK_UNAVAILABLE)
                    )
                )
            }

            // Group data requested to secondary sources by source
            val mainFeaturesIgnored: MutableList<SecondaryWeatherSourceFeature> = mutableListOf()
            val secondarySources: MutableMap<String, MutableList<SecondaryWeatherSourceFeature>> =
                mutableMapOf()
            with(location) {
                listOf(
                    Pair(airQualitySource, SecondaryWeatherSourceFeature.FEATURE_AIR_QUALITY),
                    Pair(pollenSource, SecondaryWeatherSourceFeature.FEATURE_POLLEN),
                    Pair(minutelySource, SecondaryWeatherSourceFeature.FEATURE_MINUTELY),
                    Pair(alertSource, SecondaryWeatherSourceFeature.FEATURE_ALERT),
                    Pair(normalsSource, SecondaryWeatherSourceFeature.FEATURE_NORMALS)
                ).forEach {
                    if (!it.first.isNullOrEmpty() && it.first != weatherSource) {
                        if (secondarySources.containsKey(it.first)) {
                            secondarySources[it.first]!!.add(it.second)
                        } else {
                            secondarySources[it.first!!] = mutableListOf(it.second)
                        }
                        mainFeaturesIgnored.add(it.second)
                    }
                }
            }

            // MAIN SOURCE
            val base = location.weather?.base?.copy(
                refreshTime = Date()
            ) ?: Base(
                refreshTime = Date()
            )
            var isMainDataValid = false
            val languageUpdateTime = SettingsManager.getInstance(context).languageUpdateLastTimestamp
            if (location.weather?.base != null) {
                isMainDataValid = isWeatherDataStillValid(
                    location,
                    isRestricted = !BreezyWeather.instance.debugMode
                            && service is ConfigurableSource
                            && service.isRestricted,
                    minimumTime = languageUpdateTime
                )
                // If main data is still valid, let’s check if there are features inside main
                // that requires a refresh
                if (isMainDataValid) {
                    service.supportedFeaturesInMain.forEach {
                        // If the feature is not requested, nothing to process
                        if (!mainFeaturesIgnored.contains(it)) {
                            if (
                                !isWeatherDataStillValid(
                                    location,
                                    it,
                                    isRestricted = !BreezyWeather.instance.debugMode
                                            && service is ConfigurableSource
                                            && service.isRestricted,
                                    minimumTime = languageUpdateTime
                                )
                            ) {
                                isMainDataValid = false
                            } else {
                                LogHelper.log(msg = "${it.id} feature from main source is still valid")
                            }
                        }
                    }
                }

                // If there are no secondary sources to process, let’s just return the same weather
                if (isMainDataValid && mainFeaturesIgnored.isEmpty()) {
                    LogHelper.log(msg = "Main weather data is still valid")
                    val newWeather = location.weather.copy(
                        base = base
                    )
                    WeatherEntityRepository.writeWeather(location, newWeather)
                    return WeatherResult(newWeather)
                }
            }

            val mainWeather = if (isMainDataValid) {
                LogHelper.log(msg = "Main weather data is still valid")
                location.weather!!.toWeatherWrapper()
            } else {
                try {
                    service
                        .requestWeather(context, location.copy(), mainFeaturesIgnored)
                        .awaitFirstOrElse {
                            throw WeatherException()
                        }
                } catch (e: Throwable) {
                    return WeatherResult(
                        location.weather,
                        listOf(
                            RefreshError(getRequestErrorType(e, RefreshErrorType.WEATHER_REQ_FAILED), service.name)
                        )
                    )
                }
            }
            var airQualityUpdateTime = if (service.supportedFeaturesInMain.contains(SecondaryWeatherSourceFeature.FEATURE_AIR_QUALITY)
                && !mainFeaturesIgnored.contains(SecondaryWeatherSourceFeature.FEATURE_AIR_QUALITY)) {
                Date()
            } else base.airQualityUpdateTime
            var pollenUpdateTime = if (service.supportedFeaturesInMain.contains(SecondaryWeatherSourceFeature.FEATURE_POLLEN)
                && !mainFeaturesIgnored.contains(SecondaryWeatherSourceFeature.FEATURE_POLLEN)) {
                Date()
            } else base.pollenUpdateTime
            var minutelyUpdateTime = if (service.supportedFeaturesInMain.contains(SecondaryWeatherSourceFeature.FEATURE_MINUTELY)
                && !mainFeaturesIgnored.contains(SecondaryWeatherSourceFeature.FEATURE_MINUTELY)) {
                Date()
            } else base.minutelyUpdateTime
            var alertsUpdateTime = if (service.supportedFeaturesInMain.contains(SecondaryWeatherSourceFeature.FEATURE_ALERT)
                && !mainFeaturesIgnored.contains(SecondaryWeatherSourceFeature.FEATURE_ALERT)) {
                Date()
            } else base.alertsUpdateTime
            var normalsUpdateTime = if (service.supportedFeaturesInMain.contains(SecondaryWeatherSourceFeature.FEATURE_NORMALS)
                && !mainFeaturesIgnored.contains(SecondaryWeatherSourceFeature.FEATURE_NORMALS)) {
                Date()
            } else base.normalsUpdateTime

            // COMPLETE BACK TO YESTERDAY 00:00 MAX
            // TODO: Use Calendar to handle DST
            val yesterdayMidnight = Date(Date().time - 24 * 3600 * 1000)
                .getFormattedDate(location.timeZone, "yyyy-MM-dd", Locale.ENGLISH)
                .toDateNoHour(location.timeZone)!!
            val mainWeatherCompleted = completeMainWeatherWithPreviousData(
                mainWeather,
                location.weather,
                yesterdayMidnight
            )

            // SECONDARY SOURCES
            val errors = mutableListOf<RefreshError>()
            val secondaryWeatherWrapper = if (secondarySources.isNotEmpty()) {
                val secondarySourceCalls = mutableMapOf<String, SecondaryWeatherWrapper?>()
                secondarySources
                    .forEach { entry ->
                        val secondaryService = sourceManager.getSecondaryWeatherSource(entry.key)
                        if (secondaryService == null) {
                            errors.add(RefreshError(RefreshErrorType.SOURCE_NOT_INSTALLED, entry.key))
                        } else {
                            val featuresToUpdate = entry.value.filter {
                                !isWeatherDataStillValid(
                                    location,
                                    it,
                                    isRestricted = !BreezyWeather.instance.debugMode
                                            && secondaryService is ConfigurableSource
                                            && secondaryService.isRestricted,
                                    minimumTime = languageUpdateTime
                                )
                            }
                            if (featuresToUpdate.isEmpty()) {
                                // Setting to null will make it use previous data
                                secondarySourceCalls[entry.key] = null
                            } else {
                                //
                                entry.value.forEach {
                                    // We could also check for isFeatureSupportedForLocation
                                    // but it’s probably best to let the source decide if it wants
                                    // to throw the error itself
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
                                    e.printStackTrace()
                                    errors.add(
                                        RefreshError(
                                            getRequestErrorType(e, RefreshErrorType.SECONDARY_WEATHER_FAILED),
                                            secondaryService.name
                                        )
                                    )
                                    null
                                }
                            }
                        }
                    }

                /**
                 * Make sure we return data from the correct secondary source
                 */
                SecondaryWeatherWrapper(
                    airQuality = if (!location.airQualitySource.isNullOrEmpty() && location.airQualitySource != location.weatherSource) {
                        secondarySourceCalls.getOrElse(location.airQualitySource) { null }?.airQuality?.let {
                            airQualityUpdateTime = Date()
                            it
                        } ?: getAirQualityWrapperFromWeather(location.weather, yesterdayMidnight)
                    } else null,
                    pollen = if (!location.pollenSource.isNullOrEmpty() && location.pollenSource != location.weatherSource) {
                        secondarySourceCalls.getOrElse(location.pollenSource) { null }?.pollen?.let {
                            pollenUpdateTime = Date()
                            it
                        } ?: getPollenWrapperFromWeather(location.weather, yesterdayMidnight)
                    } else null,
                    minutelyForecast = if (!location.minutelySource.isNullOrEmpty() && location.minutelySource != location.weatherSource) {
                        secondarySourceCalls.getOrElse(location.minutelySource) { null }?.minutelyForecast?.let {
                            minutelyUpdateTime = Date()
                            it
                        } ?: getMinutelyFromWeather(location.weather)
                    } else null,
                    alertList = if (!location.alertSource.isNullOrEmpty() && location.alertSource != location.weatherSource) {
                        secondarySourceCalls.getOrElse(location.alertSource) { null }?.alertList?.let {
                            alertsUpdateTime = Date()
                            it
                        } ?: getAlertsFromWeather(location.weather)
                    } else null,
                    normals = if (!location.normalsSource.isNullOrEmpty() && location.normalsSource != location.weatherSource) {
                        secondarySourceCalls.getOrElse(location.normalsSource) { null }?.normals?.let {
                            normalsUpdateTime = Date()
                            it
                        } ?: getNormalsFromWeather(location)
                    } else null
                )
            } else null

            /**
             * Most sources starts hourly forecast at current time (13:00 for example)
             * while some complementary sources starts at 00:00.
             * Some others have a 3-hourly starting from day 3+
             * Only relying on the main source leads to missing hourly data that is used for daily
             * computation (for example, daily air quality and pollen)
             * For this reason, we complete missing data earlier for the secondary data
             */
            val secondaryWeatherWrapperCompleted = completeMissingSecondaryWeatherDailyData(
                secondaryWeatherWrapper, location.timeZone
            )

            val hourlyMissingComputed = computeMissingHourlyData(
                mergeSecondaryWeatherDataIntoHourlyWrapperList(
                    mainWeatherCompleted.hourlyForecast, secondaryWeatherWrapperCompleted
                )
            )
            val dailyForecast = completeDailyListFromHourlyList(
                mergeSecondaryWeatherDataIntoDailyList(
                    mainWeatherCompleted.dailyForecast, secondaryWeatherWrapperCompleted
                ),
                hourlyMissingComputed,
                location
            )
            val hourlyForecast = completeHourlyListFromDailyList(
                hourlyMissingComputed,
                dailyForecast,
                location.timeZone
            )

            // Example: 15:01 -> starts at 15:00, 15:59 -> starts at 15:00
            val currentHour = hourlyForecast.firstOrNull {
                it.date.time >= System.currentTimeMillis() - (3600 * 1000)
            }
            val currentDay = dailyForecast.firstOrNull {
                // Adding 23 hours just to be safe in case of DST
                it.date.time >= yesterdayMidnight.time + (23 * 3600 * 1000)
            }

            val weather = Weather(
                base = base.copy(
                    mainUpdateTime = if (isMainDataValid) base.mainUpdateTime else Date(),
                    airQualityUpdateTime = airQualityUpdateTime,
                    pollenUpdateTime = pollenUpdateTime,
                    minutelyUpdateTime = minutelyUpdateTime,
                    alertsUpdateTime = alertsUpdateTime,
                    normalsUpdateTime = normalsUpdateTime
                ),
                current = completeCurrentFromTodayDailyAndHourly(
                    mainWeatherCompleted.current,
                    currentHour,
                    currentDay,
                    location.timeZone
                ),
                normals = secondaryWeatherWrapper?.normals
                    ?: completeNormalsFromDaily(mainWeatherCompleted.normals, dailyForecast),
                dailyForecast = dailyForecast,
                hourlyForecast = hourlyForecast,
                minutelyForecast = secondaryWeatherWrapper?.minutelyForecast
                    ?: mainWeatherCompleted.minutelyForecast ?: emptyList(),
                alertList = (secondaryWeatherWrapper?.alertList ?: mainWeatherCompleted.alertList)?.filter {
                    // Don’t save past alerts in database
                    it.endDate == null || it.endDate.time > Date().time
                } ?: emptyList()
            )
            WeatherEntityRepository.writeWeather(location, weather)
            return WeatherResult(weather, errors)
        } catch (e: Throwable) {
            e.printStackTrace()
            return WeatherResult(
                location.weather,
                listOf(RefreshError(RefreshErrorType.WEATHER_REQ_FAILED))
            )
        }
    }

    private fun getRequestErrorType(
        e: Throwable,
        defaultRefreshError: RefreshErrorType
    ): RefreshErrorType {
        return when (e) {
            is NoNetworkException -> RefreshErrorType.NETWORK_UNAVAILABLE
            // Can mean different things but most of the time, it’s a network issue:
            is UnknownHostException -> RefreshErrorType.NETWORK_UNAVAILABLE
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

    /**
     * For a given location, will tell if data is still valid or needs a refresh
     * @param feature if null, will tell for main weather
     * @param isRestricted some sources will prefer a longer wait, make it true if that’s the case
     * @param minimumTime if the last update was before this minimum time, it will be forced refreshed (except for normals)
     */
    private fun isWeatherDataStillValid(
        location: Location,
        feature: SecondaryWeatherSourceFeature? = null,
        isRestricted: Boolean = false,
        minimumTime: Long = 0
    ): Boolean {
        if (location.weather?.base == null) return false

        when (feature) {
            SecondaryWeatherSourceFeature.FEATURE_AIR_QUALITY -> {
                return isUpdateStillValid(
                    location.weather.base.airQualityUpdateTime,
                    if (isRestricted) WAIT_AIR_QUALITY_RESTRICTED else WAIT_AIR_QUALITY,
                    minimumTime
                )
            }
            SecondaryWeatherSourceFeature.FEATURE_POLLEN -> {
                return isUpdateStillValid(
                    location.weather.base.pollenUpdateTime,
                    if (isRestricted) WAIT_POLLEN_RESTRICTED else WAIT_POLLEN,
                    minimumTime
                )
            }
            SecondaryWeatherSourceFeature.FEATURE_MINUTELY -> {
                return isUpdateStillValid(
                    location.weather.base.minutelyUpdateTime,
                    if (location.weather.minutelyForecast.none { it.precipitationIntensity != null && it.precipitationIntensity!! > 0 }) {
                        if (isRestricted) WAIT_MINUTELY_RESTRICTED else WAIT_MINUTELY
                    } else {
                        if (isRestricted) WAIT_MINUTELY_RESTRICTED_ONGOING else WAIT_MINUTELY_ONGOING
                    },
                    minimumTime
                )
            }
            SecondaryWeatherSourceFeature.FEATURE_ALERT -> {
                return isUpdateStillValid(
                    location.weather.base.alertsUpdateTime,
                    if (location.weather.currentAlertList.isEmpty()) {
                        if (isRestricted) WAIT_ALERTS_RESTRICTED else WAIT_ALERTS
                    } else {
                        if (isRestricted) WAIT_ALERTS_RESTRICTED_ONGOING else WAIT_ALERTS_ONGOING
                    },
                    minimumTime
                )
            }
            SecondaryWeatherSourceFeature.FEATURE_NORMALS -> {
                if (location.weather.base.normalsUpdateTime == null) return true

                if (location.isCurrentPosition) {
                    return isUpdateStillValid(
                        location.weather.base.normalsUpdateTime,
                        if (isRestricted) WAIT_NORMALS_CURRENT_RESTRICTED else WAIT_NORMALS_CURRENT
                    )
                } else {
                    if (location.weather.normals?.month == null) return false
                    val cal = Date().toCalendarWithTimeZone(location.timeZone)
                    return location.weather.normals.month == cal[Calendar.MONTH]
                }
            }
            else -> {
                return isUpdateStillValid(
                    location.weather.base.mainUpdateTime,
                    if (isRestricted) WAIT_MAIN_RESTRICTED else WAIT_MAIN,
                    minimumTime
                )
            }
        }
    }

    private fun isUpdateStillValid(
        updateTime: Date?,
        wait: Int,
        minimumTime: Long = 0
    ): Boolean {
        if (updateTime == null || updateTime.time < minimumTime) return false

        val currentTime = System.currentTimeMillis()

        return (currentTime >= updateTime.time
                && currentTime - updateTime.time < wait * 60 * 1000)
    }

    companion object {
        private const val WAIT_MINIMUM = 1
        private const val WAIT_REGULAR = 5
        private const val WAIT_RESTRICTED = 15
        private const val WAIT_ONE_HOUR = 60

        const val WAIT_MAIN = WAIT_REGULAR // 5 min
        const val WAIT_MAIN_RESTRICTED = WAIT_RESTRICTED // 15 min
        const val WAIT_AIR_QUALITY = WAIT_REGULAR // 5 min
        const val WAIT_AIR_QUALITY_RESTRICTED = WAIT_ONE_HOUR // 1 hour
        const val WAIT_POLLEN = WAIT_REGULAR // 5 min
        const val WAIT_POLLEN_RESTRICTED = WAIT_ONE_HOUR // 1 hour
        const val WAIT_MINUTELY = WAIT_REGULAR // 5 min
        const val WAIT_MINUTELY_ONGOING = WAIT_MINIMUM // 1 min
        const val WAIT_MINUTELY_RESTRICTED = WAIT_RESTRICTED // 15 min
        const val WAIT_MINUTELY_RESTRICTED_ONGOING = WAIT_REGULAR // 5 min
        const val WAIT_ALERTS = WAIT_REGULAR // 5 min
        const val WAIT_ALERTS_ONGOING = WAIT_MINIMUM // 1 min
        const val WAIT_ALERTS_RESTRICTED = WAIT_ONE_HOUR // 1 hour
        const val WAIT_ALERTS_RESTRICTED_ONGOING = WAIT_REGULAR // 5 min
        const val WAIT_NORMALS_CURRENT = WAIT_REGULAR // 5 min
        const val WAIT_NORMALS_CURRENT_RESTRICTED = WAIT_RESTRICTED // 15 min
    }
}
