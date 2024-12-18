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
import android.content.Intent
import android.content.pm.PackageManager
import android.content.pm.ShortcutInfo
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.core.location.LocationManagerCompat
import breezyweather.data.location.LocationRepository
import breezyweather.data.weather.WeatherRepository
import breezyweather.domain.location.model.Location
import breezyweather.domain.source.SourceFeature
import breezyweather.domain.weather.model.Base
import breezyweather.domain.weather.model.Weather
import breezyweather.domain.weather.model.WeatherCode
import breezyweather.domain.weather.wrappers.WeatherWrapper
import io.reactivex.rxjava3.core.Observable
import kotlinx.coroutines.rx3.awaitFirstOrElse
import kotlinx.serialization.MissingFieldException
import kotlinx.serialization.SerializationException
import org.breezyweather.BreezyWeather
import org.breezyweather.common.exceptions.ApiKeyMissingException
import org.breezyweather.common.exceptions.ApiLimitReachedException
import org.breezyweather.common.exceptions.ApiUnauthorizedException
import org.breezyweather.common.exceptions.InvalidLocationException
import org.breezyweather.common.exceptions.InvalidOrIncompleteDataException
import org.breezyweather.common.exceptions.LocationAccessOffException
import org.breezyweather.common.exceptions.LocationException
import org.breezyweather.common.exceptions.LocationSearchException
import org.breezyweather.common.exceptions.MissingPermissionLocationBackgroundException
import org.breezyweather.common.exceptions.MissingPermissionLocationException
import org.breezyweather.common.exceptions.NoNetworkException
import org.breezyweather.common.exceptions.ParsingException
import org.breezyweather.common.exceptions.ReverseGeocodingException
import org.breezyweather.common.exceptions.SourceNotInstalledException
import org.breezyweather.common.exceptions.WeatherException
import org.breezyweather.common.extensions.getFormattedDate
import org.breezyweather.common.extensions.getStringByLocale
import org.breezyweather.common.extensions.hasPermission
import org.breezyweather.common.extensions.isOnline
import org.breezyweather.common.extensions.locationManager
import org.breezyweather.common.extensions.shortcutManager
import org.breezyweather.common.extensions.toCalendarWithTimeZone
import org.breezyweather.common.extensions.toDateNoHour
import org.breezyweather.common.source.ConfigurableSource
import org.breezyweather.common.source.HttpSource
import org.breezyweather.common.source.LocationParametersSource
import org.breezyweather.common.source.LocationResult
import org.breezyweather.common.source.RefreshError
import org.breezyweather.common.source.WeatherResult
import org.breezyweather.common.utils.helpers.IntentHelper
import org.breezyweather.common.utils.helpers.LogHelper
import org.breezyweather.common.utils.helpers.ShortcutsHelper
import org.breezyweather.domain.location.model.getPlace
import org.breezyweather.domain.location.model.isDaylight
import org.breezyweather.main.utils.RefreshErrorType
import org.breezyweather.remoteviews.presenters.ClockDayDetailsWidgetIMP
import org.breezyweather.remoteviews.presenters.ClockDayHorizontalWidgetIMP
import org.breezyweather.remoteviews.presenters.ClockDayVerticalWidgetIMP
import org.breezyweather.remoteviews.presenters.ClockDayWeekWidgetIMP
import org.breezyweather.remoteviews.presenters.DailyTrendWidgetIMP
import org.breezyweather.remoteviews.presenters.DayWeekWidgetIMP
import org.breezyweather.remoteviews.presenters.DayWidgetIMP
import org.breezyweather.remoteviews.presenters.HourlyTrendWidgetIMP
import org.breezyweather.remoteviews.presenters.MaterialYouCurrentWidgetIMP
import org.breezyweather.remoteviews.presenters.MaterialYouForecastWidgetIMP
import org.breezyweather.remoteviews.presenters.MultiCityWidgetIMP
import org.breezyweather.remoteviews.presenters.TextWidgetIMP
import org.breezyweather.remoteviews.presenters.WeekWidgetIMP
import org.breezyweather.remoteviews.presenters.notification.WidgetNotificationIMP
import org.breezyweather.settings.SettingsManager
import org.breezyweather.settings.SourceConfigStore
import org.breezyweather.theme.resource.ResourcesProviderFactory
import retrofit2.HttpException
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import java.text.ParseException
import java.util.Calendar
import java.util.Date
import javax.inject.Inject
import kotlin.math.min
import kotlin.time.Duration.Companion.days
import kotlin.time.Duration.Companion.hours
import kotlin.time.Duration.Companion.minutes

class RefreshHelper @Inject constructor(
    private val sourceManager: SourceManager,
    private val locationRepository: LocationRepository,
    private val weatherRepository: WeatherRepository,
) {
    suspend fun getLocation(
        context: Context,
        location: Location,
        background: Boolean,
    ): LocationResult {
        if (!location.isCurrentPosition) {
            return LocationResult(location)
        }

        var currentErrors = mutableListOf<RefreshError>()
        val currentLocation = try {
            val currentLocationResult = requestCurrentLocation(context, location, background)
            currentErrors = currentLocationResult.errors.toMutableList()
            currentLocationResult.location
        } catch (e: Throwable) {
            e.printStackTrace()
            currentErrors.add(RefreshError(RefreshErrorType.LOCATION_FAILED))
            location
        }

        if (currentLocation.isUsable) {
            val source = location.forecastSource
            val weatherService = sourceManager.getReverseGeocodingSourceOrDefault(source)
            val locationGeocoded = if (
                location.longitude != currentLocation.longitude ||
                location.latitude != currentLocation.latitude ||
                location.needsGeocodeRefresh
            ) {
                try {
                    val locationWithGeocodeInfo = if (weatherService is ConfigurableSource &&
                        !weatherService.isConfigured
                    ) {
                        throw ApiKeyMissingException()
                    } else {
                        weatherService
                            .requestReverseGeocodingLocation(context, currentLocation)
                            .map { locationList ->
                                if (locationList.isNotEmpty()) {
                                    val result = locationList[0]
                                    currentLocation.copy(
                                        cityId = result.cityId,
                                        timeZone = result.timeZone,
                                        country = result.country,
                                        countryCode = result.countryCode ?: "",
                                        admin1 = result.admin1 ?: "",
                                        admin1Code = result.admin1Code ?: "",
                                        admin2 = result.admin2 ?: "",
                                        admin2Code = result.admin2Code ?: "",
                                        admin3 = result.admin3 ?: "",
                                        admin3Code = result.admin3Code ?: "",
                                        admin4 = result.admin4 ?: "",
                                        admin4Code = result.admin4Code ?: "",
                                        city = result.city,
                                        district = result.district ?: "",
                                        needsGeocodeRefresh = false
                                    )
                                } else {
                                    throw ReverseGeocodingException()
                                }
                            }.awaitFirstOrElse {
                                throw ReverseGeocodingException()
                            }
                    }
                    locationRepository.update(locationWithGeocodeInfo)
                    locationWithGeocodeInfo
                } catch (e: Throwable) {
                    locationRepository.update(currentLocation)
                    return LocationResult(
                        currentLocation,
                        errors = currentErrors + listOf(
                            RefreshError(
                                getRequestErrorType(
                                    context,
                                    e,
                                    RefreshErrorType.REVERSE_GEOCODING_FAILED
                                ),
                                weatherService.name
                            )
                        )
                    )
                }
            } else {
                currentLocation
            }
            return LocationResult(locationGeocoded, currentErrors)
        } else {
            return LocationResult(currentLocation, currentErrors)
        }
    }

    private suspend fun requestCurrentLocation(
        context: Context,
        location: Location,
        background: Boolean,
    ): LocationResult {
        val locationSource = SettingsManager.getInstance(context).locationSource
        val locationService = sourceManager.getLocationSourceOrDefault(locationSource)
        val errors = mutableListOf<RefreshError>()
        if (!context.isOnline()) {
            errors.add(RefreshError(RefreshErrorType.NETWORK_UNAVAILABLE))
        }
        if (locationService.permissions.isNotEmpty()) {
            // if needs any location permission.
            if (!context.hasPermission(Manifest.permission.ACCESS_COARSE_LOCATION) &&
                !context.hasPermission(Manifest.permission.ACCESS_FINE_LOCATION)
            ) {
                errors.add(RefreshError(RefreshErrorType.ACCESS_LOCATION_PERMISSION_MISSING))
            }
            if (background) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q &&
                    !context.hasPermission(Manifest.permission.ACCESS_BACKGROUND_LOCATION)
                ) {
                    errors.add(RefreshError(RefreshErrorType.ACCESS_BACKGROUND_LOCATION_PERMISSION_MISSING))
                }
            }
        }
        if (!LocationManagerCompat.isLocationEnabled(context.locationManager)) {
            errors.add(RefreshError(RefreshErrorType.LOCATION_ACCESS_OFF))
        }
        if (errors.isNotEmpty()) {
            return LocationResult(location, errors)
        }

        return try {
            val result = if (locationService is ConfigurableSource && !locationService.isConfigured) {
                throw ApiKeyMissingException()
            } else {
                locationService
                    .requestLocation(context)
                    .awaitFirstOrElse {
                        throw LocationException()
                    }
            }
            return LocationResult(
                if (result.latitude != location.latitude || result.longitude != location.longitude) {
                    location.copy(
                        latitude = result.latitude,
                        longitude = result.longitude,
                        timeZone = result.timeZone ?: location.timeZone,
                        /*
                         * Don’t keep old data as the user can have changed position
                         * It avoids keeping old data from a reverse geocoding-compatible weather source
                         * onto a weather source without reverse geocoding
                         */
                        country = result.country ?: "",
                        countryCode = result.countryCode ?: "",
                        admin1 = result.admin1 ?: "",
                        admin1Code = result.admin1Code ?: "",
                        admin2 = result.admin2 ?: "",
                        admin2Code = result.admin2Code ?: "",
                        admin3 = result.admin3 ?: "",
                        admin3Code = result.admin3Code ?: "",
                        admin4 = result.admin4 ?: "",
                        admin4Code = result.admin4Code ?: "",
                        city = result.city ?: "",
                        district = result.district ?: ""
                    )
                } else {
                    // Return as-is without overwriting reverse geocoding info
                    location
                }
            )
        } catch (e: Throwable) {
            LocationResult(
                location,
                errors = listOf(
                    RefreshError(
                        getRequestErrorType(context, e, RefreshErrorType.LOCATION_FAILED),
                        locationService.name
                    )
                )
            )
        }
    }

    suspend fun updateLocation(location: Location, oldFormattedId: String? = null) {
        locationRepository.update(location, oldFormattedId)
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
        context: Context,
        location: Location,
        coordinatesChanged: Boolean,
    ): WeatherResult {
        try {
            if (!location.isUsable || location.needsGeocodeRefresh) {
                return WeatherResult(
                    location.weather,
                    listOf(RefreshError(RefreshErrorType.INVALID_LOCATION))
                )
            }

            // Group data requested to sources by source
            val featuresBySources: MutableMap<String, MutableList<SourceFeature>> = mutableMapOf()
            with(location) {
                listOf(
                    Pair(forecastSource, SourceFeature.FORECAST),
                    Pair(currentSource, SourceFeature.CURRENT),
                    Pair(airQualitySource, SourceFeature.AIR_QUALITY),
                    Pair(pollenSource, SourceFeature.POLLEN),
                    Pair(minutelySource, SourceFeature.MINUTELY),
                    Pair(alertSource, SourceFeature.ALERT),
                    Pair(normalsSource, SourceFeature.NORMALS)
                ).forEach {
                    if (!it.first.isNullOrEmpty()) {
                        if (featuresBySources.containsKey(it.first)) {
                            featuresBySources[it.first]!!.add(it.second)
                        } else {
                            featuresBySources[it.first!!] = mutableListOf(it.second)
                        }
                    }
                }
            }

            // Always update refresh time displayed to the user, even if just re-using cached data
            val base = location.weather?.base?.copy(
                refreshTime = Date()
            ) ?: Base(
                refreshTime = Date()
            )

            val languageUpdateTime = SettingsManager.getInstance(context).languageUpdateLastTimestamp
            val locationParameters = location.parameters.toMutableMap()
            val errors = mutableListOf<RefreshError>()

            // COMPLETE BACK TO YESTERDAY 00:00 MAX
            // TODO: Use Calendar to handle DST
            val yesterdayMidnight = Date(Date().time - 1.days.inWholeMilliseconds)
                .getFormattedDate("yyyy-MM-dd", location)
                .toDateNoHour(location.javaTimeZone)!!
            var forecastUpdateTime = base.forecastUpdateTime
            var currentUpdateTime = base.currentUpdateTime
            var airQualityUpdateTime = base.airQualityUpdateTime
            var pollenUpdateTime = base.pollenUpdateTime
            var minutelyUpdateTime = base.minutelyUpdateTime
            var alertsUpdateTime = base.alertsUpdateTime
            var normalsUpdateTime = base.normalsUpdateTime
            val weatherWrapper = if (featuresBySources.isNotEmpty()) {
                val sourceCalls = mutableMapOf<String, WeatherWrapper?>()
                featuresBySources
                    .forEach { entry ->
                        val service = sourceManager.getWeatherSource(entry.key)
                        if (service == null) {
                            errors.add(RefreshError(RefreshErrorType.SOURCE_NOT_INSTALLED, entry.key))
                        } else {
                            // Debug source is not online
                            if (service is HttpSource && !context.isOnline()) {
                                return WeatherResult(
                                    location.weather,
                                    listOf(
                                        RefreshError(RefreshErrorType.NETWORK_UNAVAILABLE)
                                    )
                                )
                            }

                            val featuresToUpdate = entry.value
                                .filter {
                                    // Remove sources that are not configured
                                    if (service is ConfigurableSource && !service.isConfigured) {
                                        errors.add(RefreshError(RefreshErrorType.API_KEY_REQUIRED_MISSING, entry.key))
                                        false
                                    } else {
                                        true
                                    }
                                }
                                .filter {
                                    // Remove sources that no longer supports the feature
                                    if (!service.supportedFeatures.containsKey(it)) {
                                        errors.add(RefreshError(RefreshErrorType.UNSUPPORTED_FEATURE, entry.key))
                                        false
                                    } else {
                                        true
                                    }
                                }
                                .filter {
                                    // Remove sources that no longer supports the feature for that location
                                    if (!service.isFeatureSupportedForLocation(location, it)) {
                                        errors.add(RefreshError(RefreshErrorType.UNSUPPORTED_FEATURE, entry.key))
                                        false
                                    } else {
                                        true
                                    }
                                }
                                .filter {
                                    !isWeatherDataStillValid(
                                        location,
                                        it,
                                        isRestricted = !BreezyWeather.instance.debugMode &&
                                            service is ConfigurableSource &&
                                            service.isRestricted,
                                        minimumTime = languageUpdateTime
                                    )
                                }
                            if (featuresToUpdate.isEmpty()) {
                                // Setting to null will make it use previous data
                                sourceCalls[entry.key] = null
                            } else {
                                sourceCalls[entry.key] = try {
                                    if (service is LocationParametersSource &&
                                        service.needsLocationParametersRefresh(
                                            location,
                                            coordinatesChanged,
                                            featuresToUpdate
                                        )
                                    ) {
                                        locationParameters[service.id] = buildMap {
                                            if (locationParameters.getOrElse(service.id) { null } != null) {
                                                putAll(locationParameters[service.id]!!)
                                            }
                                            putAll(
                                                service
                                                    .requestLocationParameters(context, location.copy())
                                                    .awaitFirstOrElse {
                                                        throw WeatherException()
                                                    }
                                            )
                                        }
                                    }
                                    service
                                        .requestWeather(
                                            context,
                                            location.copy(parameters = locationParameters),
                                            featuresToUpdate
                                        ).awaitFirstOrElse {
                                            featuresToUpdate.forEach {
                                                errors.add(
                                                    RefreshError(
                                                        RefreshErrorType.FAILED_FEATURE,
                                                        entry.key,
                                                        it
                                                    )
                                                )
                                            }
                                            null
                                        }
                                } catch (e: Throwable) {
                                    e.printStackTrace()
                                    featuresToUpdate.forEach {
                                        errors.add(
                                            RefreshError(
                                                RefreshErrorType.FAILED_FEATURE,
                                                entry.key,
                                                it
                                            )
                                        )
                                    }
                                    null
                                }
                            }
                        }
                    }

                for ((k, v) in sourceCalls) {
                    v?.failedFeatures?.forEach {
                        errors.add(
                            RefreshError(
                                RefreshErrorType.FAILED_FEATURE,
                                k,
                                it
                            )
                        )
                    }
                }

                /**
                 * Make sure we return data from the correct source
                 */
                WeatherWrapper(
                    dailyForecast = if (location.forecastSource.isNotEmpty()) {
                        if (errors.any {
                                it.feature == SourceFeature.FORECAST &&
                                    it.source == location.forecastSource
                            }
                        ) {
                            null
                        } else {
                            sourceCalls.getOrElse(location.forecastSource) { null }?.dailyForecast?.let {
                                if (it.isEmpty()) {
                                    errors.add(
                                        RefreshError(
                                            RefreshErrorType.FAILED_FEATURE,
                                            location.forecastSource,
                                            SourceFeature.FORECAST
                                        )
                                    )
                                    null
                                } else {
                                    forecastUpdateTime = Date()
                                    it
                                }
                            }
                        }
                    } else {
                        null
                    } ?: location.weather?.toDailyWrapperList(yesterdayMidnight),
                    hourlyForecast = if (location.forecastSource.isNotEmpty()) {
                        if (errors.any {
                                it.feature == SourceFeature.FORECAST &&
                                    it.source == location.forecastSource
                            }
                        ) {
                            null
                        } else {
                            sourceCalls.getOrElse(location.forecastSource) { null }?.hourlyForecast
                        }
                    } else {
                        null
                    } ?: location.weather?.toHourlyWrapperList(yesterdayMidnight),
                    current = if (!location.currentSource.isNullOrEmpty()) {
                        if (errors.any {
                                it.feature == SourceFeature.CURRENT &&
                                    it.source == location.currentSource!!
                            }
                        ) {
                            null
                        } else {
                            sourceCalls.getOrElse(location.currentSource!!) { null }?.current?.let {
                                currentUpdateTime = Date()
                                it
                            }
                        }
                    } else {
                        null
                    }, // Doesn't fallback to old current, as we will use forecast instead later
                    airQuality = if (!location.airQualitySource.isNullOrEmpty()) {
                        if (errors.any {
                                it.feature == SourceFeature.AIR_QUALITY &&
                                    it.source == location.airQualitySource!!
                            }
                        ) {
                            null
                        } else {
                            sourceCalls.getOrElse(location.airQualitySource!!) { null }?.airQuality?.let {
                                airQualityUpdateTime = Date()
                                it
                            }
                        } ?: location.weather?.toAirQualityWrapperList(yesterdayMidnight)
                    } else {
                        null
                    },
                    pollen = if (!location.pollenSource.isNullOrEmpty()) {
                        if (errors.any {
                                it.feature == SourceFeature.POLLEN &&
                                    it.source == location.pollenSource!!
                            }
                        ) {
                            null
                        } else {
                            sourceCalls.getOrElse(location.pollenSource!!) { null }?.pollen?.let {
                                pollenUpdateTime = Date()
                                it
                            }
                        } ?: location.weather?.toPollenWrapperList(yesterdayMidnight)
                    } else {
                        null
                    },
                    minutelyForecast = if (!location.minutelySource.isNullOrEmpty()) {
                        if (errors.any {
                                it.feature == SourceFeature.MINUTELY &&
                                    it.source == location.minutelySource!!
                            }
                        ) {
                            null
                        } else {
                            sourceCalls.getOrElse(location.minutelySource!!) { null }?.minutelyForecast?.let {
                                minutelyUpdateTime = Date()
                                it
                            }
                        } ?: location.weather?.toMinutelyWrapper()
                    } else {
                        null
                    },
                    alertList = if (!location.alertSource.isNullOrEmpty()) {
                        if (errors.any {
                                it.feature == SourceFeature.ALERT &&
                                    it.source == location.alertSource!!
                            }
                        ) {
                            null
                        } else {
                            sourceCalls.getOrElse(location.alertSource!!) { null }?.alertList?.let {
                                alertsUpdateTime = Date()
                                it
                            }
                        } ?: location.weather?.toAlertsWrapper()
                    } else {
                        null
                    },
                    normals = if (!location.normalsSource.isNullOrEmpty()) {
                        if (errors.any {
                                it.feature == SourceFeature.NORMALS &&
                                    it.source == location.normalsSource!!
                            }
                        ) {
                            null
                        } else {
                            sourceCalls.getOrElse(location.normalsSource!!) { null }?.normals?.let {
                                normalsUpdateTime = Date()
                                it
                            }
                        } ?: getNormalsFromWeather(location)
                    } else {
                        null
                    }
                )
            } else {
                return WeatherResult(
                    location.weather,
                    listOf(RefreshError(RefreshErrorType.INVALID_LOCATION))
                )
            }

            // COMPLETING DATA

            // 1) Creates hours/days back to yesterday 00:00 if they are missing from the new refresh
            val weatherWrapperCompleted = completeNewWeatherWithPreviousData(
                weatherWrapper,
                location.weather,
                yesterdayMidnight,
                location.airQualitySource,
                location.pollenSource
            )

            // 2) Computes as many data as possible (weather code, weather text, dew point, feels like temp., etc)
            val hourlyComputedMissingData = computeMissingHourlyData(
                weatherWrapperCompleted.hourlyForecast
            ) ?: emptyList()

            // 3) Create the daily object with air quality/pollen data + computes missing data
            val dailyForecast = completeDailyListFromHourlyList(
                convertDailyWrapperToDailyList(weatherWrapperCompleted),
                hourlyComputedMissingData,
                weatherWrapperCompleted.airQuality?.hourlyForecast ?: emptyMap(),
                weatherWrapperCompleted.pollen?.hourlyForecast ?: emptyMap(),
                location
            )

            // 4) Complete UV and isDaylight + air quality in hourly
            val hourlyForecast = completeHourlyListFromDailyList(
                hourlyComputedMissingData,
                dailyForecast,
                weatherWrapperCompleted.airQuality?.hourlyForecast ?: emptyMap(),
                location
            )

            // Example: 15:01 -> starts at 15:00, 15:59 -> starts at 15:00
            val currentHour = hourlyForecast.firstOrNull {
                it.date.time >= System.currentTimeMillis() - 1.hours.inWholeMilliseconds
            }
            val currentDay = dailyForecast.firstOrNull {
                // Adding 23 hours just to be safe in case of DST
                it.date.time >= yesterdayMidnight.time + 23.hours.inWholeMilliseconds
            }

            val weather = Weather(
                base = base.copy(
                    forecastUpdateTime = forecastUpdateTime,
                    currentUpdateTime = currentUpdateTime,
                    airQualityUpdateTime = airQualityUpdateTime,
                    pollenUpdateTime = pollenUpdateTime,
                    minutelyUpdateTime = minutelyUpdateTime,
                    alertsUpdateTime = alertsUpdateTime,
                    normalsUpdateTime = normalsUpdateTime
                ),
                current = completeCurrentFromHourlyData(
                    weatherWrapperCompleted.current,
                    currentHour,
                    currentDay,
                    weatherWrapperCompleted.airQuality?.current,
                    location
                ),
                normals = completeNormalsFromDaily(weatherWrapperCompleted.normals, dailyForecast),
                dailyForecast = dailyForecast,
                hourlyForecast = hourlyForecast,
                minutelyForecast = weatherWrapperCompleted.minutelyForecast ?: emptyList(),
                alertList = weatherWrapperCompleted.alertList ?: emptyList()
            )
            locationRepository.insertParameters(location.formattedId, locationParameters)
            weatherRepository.insert(location, weather)
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
        context: Context,
        e: Throwable,
        defaultRefreshError: RefreshErrorType,
    ): RefreshErrorType {
        val refreshErrorType = when (e) {
            is NoNetworkException -> RefreshErrorType.NETWORK_UNAVAILABLE
            // Can mean different things but most of the time, it’s a network issue:
            is UnknownHostException -> RefreshErrorType.NETWORK_UNAVAILABLE
            is HttpException -> {
                LogHelper.log(msg = "HttpException ${e.code()}")
                when (e.code()) {
                    401, 403 -> RefreshErrorType.API_UNAUTHORIZED
                    409, 429 -> RefreshErrorType.API_LIMIT_REACHED
                    in 500..599 -> RefreshErrorType.SERVER_UNAVAILABLE
                    else -> {
                        e.printStackTrace()
                        defaultRefreshError
                    }
                }
            }
            is SocketTimeoutException -> RefreshErrorType.SERVER_TIMEOUT
            is ApiLimitReachedException -> RefreshErrorType.API_LIMIT_REACHED
            is ApiKeyMissingException -> RefreshErrorType.API_KEY_REQUIRED_MISSING
            is ApiUnauthorizedException -> RefreshErrorType.API_UNAUTHORIZED
            is InvalidLocationException -> RefreshErrorType.INVALID_LOCATION
            is LocationException -> RefreshErrorType.LOCATION_FAILED
            is LocationAccessOffException -> RefreshErrorType.LOCATION_ACCESS_OFF
            is MissingPermissionLocationException -> RefreshErrorType.ACCESS_LOCATION_PERMISSION_MISSING
            is MissingPermissionLocationBackgroundException ->
                RefreshErrorType.ACCESS_BACKGROUND_LOCATION_PERMISSION_MISSING
            is ReverseGeocodingException -> RefreshErrorType.REVERSE_GEOCODING_FAILED
            is MissingFieldException, is SerializationException, is ParsingException, is ParseException -> {
                e.printStackTrace()
                RefreshErrorType.PARSING_ERROR
            }
            is SourceNotInstalledException -> RefreshErrorType.SOURCE_NOT_INSTALLED
            is LocationSearchException -> RefreshErrorType.LOCATION_SEARCH_FAILED
            is InvalidOrIncompleteDataException -> RefreshErrorType.INVALID_INCOMPLETE_DATA
            is WeatherException -> RefreshErrorType.WEATHER_REQ_FAILED
            else -> {
                e.printStackTrace()
                defaultRefreshError
            }
        }

        LogHelper.log(msg = "Refresh error: ${context.getStringByLocale(refreshErrorType.shortMessage)}")

        return refreshErrorType
    }

    fun requestSearchLocations(
        context: Context,
        query: String,
        locationSearchSource: String,
    ): Observable<List<Location>> {
        val searchService = sourceManager.getLocationSearchSourceOrDefault(locationSearchSource)

        // Debug source is not online
        if (searchService is HttpSource && !context.isOnline()) {
            return Observable.error(NoNetworkException())
        }

        return searchService.requestLocationSearch(context, query)
    }

    fun updateWidgetIfNecessary(context: Context, locationList: List<Location>) {
        if (DayWidgetIMP.isInUse(context)) {
            DayWidgetIMP.updateWidgetView(
                context,
                locationList[0],
                sourceManager.getPollenIndexSource(
                    (locationList[0].pollenSource ?: "").ifEmpty { locationList[0].forecastSource }
                )
            )
        }
        if (WeekWidgetIMP.isInUse(context)) {
            WeekWidgetIMP.updateWidgetView(context, locationList[0])
        }
        if (DayWeekWidgetIMP.isInUse(context)) {
            DayWeekWidgetIMP.updateWidgetView(
                context,
                locationList[0],
                sourceManager.getPollenIndexSource(
                    (locationList[0].pollenSource ?: "").ifEmpty { locationList[0].forecastSource }
                )
            )
        }
        if (ClockDayHorizontalWidgetIMP.isInUse(context)) {
            ClockDayHorizontalWidgetIMP.updateWidgetView(context, locationList[0])
        }
        if (ClockDayVerticalWidgetIMP.isInUse(context)) {
            ClockDayVerticalWidgetIMP.updateWidgetView(
                context,
                locationList[0],
                sourceManager.getPollenIndexSource(
                    (locationList[0].pollenSource ?: "").ifEmpty { locationList[0].forecastSource }
                )
            )
        }
        if (ClockDayWeekWidgetIMP.isInUse(context)) {
            ClockDayWeekWidgetIMP.updateWidgetView(context, locationList[0])
        }
        if (ClockDayDetailsWidgetIMP.isInUse(context)) {
            ClockDayDetailsWidgetIMP.updateWidgetView(context, locationList[0])
        }
        if (TextWidgetIMP.isInUse(context)) {
            TextWidgetIMP.updateWidgetView(
                context,
                locationList[0],
                sourceManager.getPollenIndexSource(
                    (locationList[0].pollenSource ?: "").ifEmpty { locationList[0].forecastSource }
                )
            )
        }
        if (DailyTrendWidgetIMP.isInUse(context)) {
            DailyTrendWidgetIMP.updateWidgetView(context, locationList[0])
        }
        if (HourlyTrendWidgetIMP.isInUse(context)) {
            HourlyTrendWidgetIMP.updateWidgetView(context, locationList[0])
        }
        if (MaterialYouForecastWidgetIMP.isEnabled(context)) {
            MaterialYouForecastWidgetIMP.updateWidgetView(context, locationList[0])
        }
        if (MaterialYouCurrentWidgetIMP.isEnabled(context)) {
            MaterialYouCurrentWidgetIMP.updateWidgetView(context, locationList[0])
        }
        if (MultiCityWidgetIMP.isInUse(context)) {
            MultiCityWidgetIMP.updateWidgetView(context, locationList)
        }
    }

    suspend fun updateWidgetIfNecessary(context: Context) {
        val locationList = locationRepository.getXLocations(3, withParameters = false).toMutableList()
        if (locationList.isNotEmpty()) {
            for (i in locationList.indices) {
                locationList[i] = locationList[i].copy(
                    weather = weatherRepository.getWeatherByLocationId(
                        locationList[i].formattedId,
                        withDaily = true,
                        withHourly = i == 0, // Not needed in multi city
                        withMinutely = false,
                        withAlerts = i == 0 // Not needed in multi city
                    )
                )
            }
            updateWidgetIfNecessary(context, locationList)
        }
    }

    fun updateNotificationIfNecessary(context: Context, locationList: List<Location>) {
        if (WidgetNotificationIMP.isEnabled(context)) {
            WidgetNotificationIMP.buildNotificationAndSendIt(context, locationList)
        }
    }

    suspend fun updateNotificationIfNecessary(context: Context) {
        if (WidgetNotificationIMP.isEnabled(context)) {
            val locationList = locationRepository.getXLocations(4, withParameters = false).toMutableList()
            for (i in locationList.indices) {
                locationList[i] = locationList[i].copy(
                    weather = weatherRepository.getWeatherByLocationId(
                        locationList[i].formattedId,
                        withDaily = true,
                        withHourly = i == 0, // Not needed in multi city
                        withMinutely = false,
                        withAlerts = i == 0 // Not needed in multi city
                    )
                )
            }
            updateNotificationIfNecessary(context, locationList)
        }
    }

    /**
     * @param context
     * @param sourceId if you only want to send data for a specific source
     */
    suspend fun broadcastDataIfNecessary(
        context: Context,
        sourceId: String? = null,
    ) {
        val locationList = locationRepository.getAllLocations(withParameters = false)
            .map {
                it.copy(
                    weather = weatherRepository.getWeatherByLocationId(
                        it.formattedId
                    )
                )
            }
        return broadcastDataIfNecessary(context, locationList, sourceId)
    }

    fun isBroadcastSourcesEnabled(context: Context): Boolean {
        return sourceManager.isBroadcastSourcesEnabled(context)
    }

    /**
     * @param context
     * @param locationList
     * @param sourceId if you only want to send data for a specific source
     */
    fun broadcastDataIfNecessary(
        context: Context,
        locationList: List<Location>,
        sourceId: String? = null,
    ) {
        sourceManager.getBroadcastSources()
            .filter { sourceId == null || sourceId == it.id }
            .forEach { source ->
                val config = SourceConfigStore(context, source.id)
                val enabledPackages = (config.getString("packages", null) ?: "").let {
                    if (it.isNotEmpty()) it.split(",") else emptyList()
                }

                if (enabledPackages.isNotEmpty()) {
                    val packageInfoList = context.packageManager.queryBroadcastReceivers(
                        Intent(source.intentAction),
                        PackageManager.GET_RESOLVED_FILTER
                    )
                    val enabledAndAvailablePackages = enabledPackages
                        .filter { enabledPackage ->
                            packageInfoList.any { it.activityInfo.applicationInfo.packageName == enabledPackage }
                        }
                    if (enabledPackages.size != enabledAndAvailablePackages.size) {
                        LogHelper.log(
                            msg = "[${source.name}] Updating packages setting as some packages are no longer available"
                        )
                        // Update to remove unavailable packages
                        config.edit().putString("packages", enabledAndAvailablePackages.joinToString(",")).apply()
                        // Don't notify settings changed, we are already sending data!
                    }

                    if (enabledAndAvailablePackages.isNotEmpty()) {
                        val data = source.getExtras(context, locationList)
                        if (data != null) {
                            enabledAndAvailablePackages.forEach {
                                if (BreezyWeather.instance.debugMode) {
                                    LogHelper.log(msg = "[${source.name}] Sending data to $it")
                                }
                                context.sendBroadcast(
                                    Intent(source.intentAction)
                                        .setPackage(it)
                                        .putExtras(data)
                                        .setFlags(Intent.FLAG_INCLUDE_STOPPED_PACKAGES)
                                )
                            }
                        }
                    }
                }
            }
    }

    @RequiresApi(api = Build.VERSION_CODES.N_MR1)
    suspend fun refreshShortcuts(context: Context, locationList: List<Location>) {
        val shortcutManager = context.shortcutManager ?: return
        val provider = ResourcesProviderFactory.newInstance
        val shortcutList = mutableListOf<ShortcutInfo>()

        // location list.
        val count = min(shortcutManager.maxShortcutCountPerActivity - 1, locationList.size)
        for (i in 0 until count) {
            val weather = locationList[i].weather
                ?: weatherRepository.getWeatherByLocationId(locationList[i].formattedId)
            val icon =
                weather?.current?.weatherCode?.let { weatherCode ->
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        ShortcutsHelper.getAdaptiveIcon(
                            provider,
                            weatherCode,
                            locationList[i].isDaylight
                        )
                    } else {
                        ShortcutsHelper.getIcon(
                            provider,
                            weatherCode,
                            locationList[i].isDaylight
                        )
                    }
                } ?: ShortcutsHelper.getIcon(provider, WeatherCode.CLEAR, true)
            val title = locationList[i].getPlace(context, true)
            shortcutList.add(
                ShortcutInfo.Builder(context, locationList[i].formattedId)
                    .setIcon(icon)
                    .setShortLabel(title)
                    .setLongLabel(title)
                    .setIntent(IntentHelper.buildMainActivityIntent(locationList[i]))
                    .build()
            )
        }
        try {
            shortcutManager.setDynamicShortcuts(shortcutList)
        } catch (ignore: Exception) {
            // do nothing.
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
        feature: SourceFeature? = null,
        isRestricted: Boolean = false,
        minimumTime: Long = 0,
    ): Boolean {
        if (location.weather?.base == null) return false

        when (feature) {
            SourceFeature.CURRENT -> {
                return isUpdateStillValid(
                    location.weather!!.base.currentUpdateTime,
                    if (isRestricted) WAIT_CURRENT_RESTRICTED else WAIT_CURRENT,
                    minimumTime
                )
            }
            SourceFeature.AIR_QUALITY -> {
                return isUpdateStillValid(
                    location.weather!!.base.airQualityUpdateTime,
                    if (isRestricted) WAIT_AIR_QUALITY_RESTRICTED else WAIT_AIR_QUALITY,
                    minimumTime
                )
            }
            SourceFeature.POLLEN -> {
                return isUpdateStillValid(
                    location.weather!!.base.pollenUpdateTime,
                    if (isRestricted) WAIT_POLLEN_RESTRICTED else WAIT_POLLEN,
                    minimumTime
                )
            }
            SourceFeature.MINUTELY -> {
                return isUpdateStillValid(
                    location.weather!!.base.minutelyUpdateTime,
                    if (location.weather!!.minutelyForecast.none { (it.precipitationIntensity ?: 0.0) > 0 }) {
                        if (isRestricted) WAIT_MINUTELY_RESTRICTED else WAIT_MINUTELY
                    } else {
                        if (isRestricted) WAIT_MINUTELY_RESTRICTED_ONGOING else WAIT_MINUTELY_ONGOING
                    },
                    minimumTime
                )
            }
            SourceFeature.ALERT -> {
                return isUpdateStillValid(
                    location.weather!!.base.alertsUpdateTime,
                    if (location.weather!!.currentAlertList.isEmpty()) {
                        if (isRestricted) WAIT_ALERTS_RESTRICTED else WAIT_ALERTS
                    } else {
                        if (isRestricted) WAIT_ALERTS_RESTRICTED_ONGOING else WAIT_ALERTS_ONGOING
                    },
                    minimumTime
                )
            }
            SourceFeature.NORMALS -> {
                if (location.weather!!.base.normalsUpdateTime == null) return false

                if (location.isCurrentPosition) {
                    return isUpdateStillValid(
                        location.weather!!.base.normalsUpdateTime,
                        if (isRestricted) WAIT_NORMALS_CURRENT_RESTRICTED else WAIT_NORMALS_CURRENT
                    )
                } else {
                    if (location.weather!!.normals?.month == null) return false
                    val cal = Date().toCalendarWithTimeZone(location.javaTimeZone)
                    return location.weather!!.normals!!.month == cal[Calendar.MONTH]
                }
            }
            else -> {
                return isUpdateStillValid(
                    location.weather!!.base.forecastUpdateTime,
                    if (isRestricted) WAIT_MAIN_RESTRICTED else WAIT_MAIN,
                    minimumTime
                )
            }
        }
    }

    private fun isUpdateStillValid(
        updateTime: Date?,
        wait: Int,
        minimumTime: Long = 0,
    ): Boolean {
        if (updateTime == null || updateTime.time < minimumTime) return false

        val currentTime = System.currentTimeMillis()

        return currentTime >= updateTime.time && currentTime - updateTime.time < wait.minutes.inWholeMilliseconds
    }

    companion object {
        private const val WAIT_MINIMUM = 1
        private const val WAIT_REGULAR = 5
        private const val WAIT_RESTRICTED = 15
        private const val WAIT_ONE_HOUR = 60

        const val WAIT_MAIN = WAIT_REGULAR // 5 min
        const val WAIT_MAIN_RESTRICTED = WAIT_RESTRICTED // 15 min
        const val WAIT_CURRENT = WAIT_MINIMUM // 1 min
        const val WAIT_CURRENT_RESTRICTED = WAIT_RESTRICTED // 15 min
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
