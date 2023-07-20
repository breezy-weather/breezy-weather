package org.breezyweather.main

import android.content.Context
import kotlinx.coroutines.rx3.awaitFirstOrElse
import kotlinx.serialization.MissingFieldException
import kotlinx.serialization.SerializationException
import org.breezyweather.common.basic.models.Location
import org.breezyweather.common.exceptions.ApiKeyMissingException
import org.breezyweather.common.exceptions.LocationException
import org.breezyweather.common.exceptions.MissingPermissionLocationBackgroundException
import org.breezyweather.common.exceptions.MissingPermissionLocationException
import org.breezyweather.common.exceptions.NoNetworkException
import org.breezyweather.common.exceptions.ParsingException
import org.breezyweather.common.exceptions.ReverseGeocodingException
import org.breezyweather.common.exceptions.SourceNotInstalledException
import org.breezyweather.common.exceptions.WeatherException
import org.breezyweather.common.utils.helpers.AsyncHelper
import org.breezyweather.db.repositories.LocationEntityRepository
import org.breezyweather.db.repositories.WeatherEntityRepository
import org.breezyweather.sources.LocationHelper
import org.breezyweather.main.utils.RequestErrorType
import org.breezyweather.sources.WeatherHelper
import retrofit2.HttpException
import java.net.SocketTimeoutException
import java.util.concurrent.Executors
import javax.inject.Inject

class MainActivityRepository @Inject constructor(
    private val locationHelper: LocationHelper,
    private val weatherHelper: WeatherHelper
) {
    private val singleThreadExecutor = Executors.newSingleThreadExecutor()

    interface WeatherRequestCallback {
        fun onCompleted(
            location: Location,
            requestErrorType: RequestErrorType?
        )
    }

    fun initLocations(formattedId: String?): List<Location> {
        val list = LocationEntityRepository.readLocationList().toMutableList()
        if (list.size == 0) return list

        if (formattedId != null) {
            for (i in list.indices) {
                if (list[i].formattedId == formattedId) {
                    list[i] = list[i].copy(weather = WeatherEntityRepository.readWeather(list[i]))
                    break
                }
            }
        } else {
            list[0] = list[0].copy(weather = WeatherEntityRepository.readWeather(list[0]))
        }

        return list
    }

    fun getWeatherCacheForLocations(
        oldList: List<Location>,
        ignoredFormattedId: String?,
        callback: (t: List<Location>, done: Boolean) -> Unit
    ) {
        AsyncHelper.runOnExecutor({ emitter ->
            emitter.send(
                oldList.map {
                    if (it.formattedId == ignoredFormattedId) {
                        it
                    } else {
                        it.copy(weather = WeatherEntityRepository.readWeather(it))
                    }
                },
                true
            )
        }, callback, singleThreadExecutor)
    }

    fun writeLocationList(locationList: List<Location>) {
        AsyncHelper.runOnExecutor({
            LocationEntityRepository.writeLocationList(locationList)
        }, singleThreadExecutor)
    }

    fun deleteLocation(location: Location) {
        AsyncHelper.runOnExecutor({
            LocationEntityRepository.deleteLocation(location)
            WeatherEntityRepository.deleteWeather(location)
        }, singleThreadExecutor)
    }

    /**
     * TODO: Optimize this function, too many duplicated code
     */
    suspend fun getWeather(
        context: Context,
        location: Location,
        locate: Boolean,
        callback: WeatherRequestCallback,
    ) {
        try {
            var locationThrowable: Throwable? = null
            val locationToProcess = if (locate) {
                try {
                    locationHelper.getCurrentLocationWithReverseGeocoding(
                        context,
                        location,
                        false
                    )
                } catch (e: Throwable) {
                    // If we failed to locate, throw an error later
                    // but still refresh with latest known position
                    if (location.isUsable) {
                        locationThrowable = e
                        location
                    } else throw e
                }
            } else location

            val requestWeather = weatherHelper.requestWeather(
                context,
                locationToProcess
            ).awaitFirstOrElse {
                throw WeatherException()
            }

            locationThrowable?.let { e ->
                val requestErrorType = when (e) {
                    is NoNetworkException -> RequestErrorType.NETWORK_UNAVAILABLE
                    is HttpException -> {
                        when (e.code()) {
                            401, 403 -> RequestErrorType.API_UNAUTHORIZED
                            409, 429 -> RequestErrorType.API_LIMIT_REACHED
                            else -> {
                                e.printStackTrace()
                                RequestErrorType.LOCATION_FAILED
                            }
                        }
                    }
                    is SocketTimeoutException -> RequestErrorType.SERVER_TIMEOUT
                    is ApiKeyMissingException -> RequestErrorType.API_KEY_REQUIRED_MISSING
                    is LocationException -> RequestErrorType.LOCATION_FAILED
                    is MissingPermissionLocationException -> RequestErrorType.ACCESS_LOCATION_PERMISSION_MISSING
                    // Should never happen, we are not in background, but just in case:
                    is MissingPermissionLocationBackgroundException -> RequestErrorType.ACCESS_BACKGROUND_LOCATION_PERMISSION_MISSING
                    is ReverseGeocodingException -> RequestErrorType.REVERSE_GEOCODING_FAILED
                    is MissingFieldException, is SerializationException, is ParsingException -> {
                        e.printStackTrace()
                        RequestErrorType.PARSING_ERROR
                    }
                    is SourceNotInstalledException -> RequestErrorType.SOURCE_NOT_INSTALLED
                    else -> {
                        e.printStackTrace()
                        RequestErrorType.LOCATION_FAILED
                    }
                }
                callback.onCompleted(locationToProcess.copy(weather = requestWeather), requestErrorType)
            } ?: run {
                callback.onCompleted(locationToProcess.copy(weather = requestWeather), null)
            }
        } catch (e: Throwable) {
            val requestErrorType = when (e) {
                is NoNetworkException -> RequestErrorType.NETWORK_UNAVAILABLE
                is HttpException -> {
                    when (e.code()) {
                        401, 403 -> RequestErrorType.API_UNAUTHORIZED
                        409, 429 -> RequestErrorType.API_LIMIT_REACHED
                        else -> {
                            e.printStackTrace()
                            RequestErrorType.WEATHER_REQ_FAILED
                        }
                    }
                }
                is SocketTimeoutException -> RequestErrorType.SERVER_TIMEOUT
                is ApiKeyMissingException -> RequestErrorType.API_KEY_REQUIRED_MISSING
                is LocationException -> RequestErrorType.LOCATION_FAILED
                is MissingPermissionLocationException -> RequestErrorType.ACCESS_LOCATION_PERMISSION_MISSING
                // Should never happen, we are not in background, but just in case:
                is MissingPermissionLocationBackgroundException -> RequestErrorType.ACCESS_BACKGROUND_LOCATION_PERMISSION_MISSING
                is ReverseGeocodingException -> RequestErrorType.REVERSE_GEOCODING_FAILED
                is MissingFieldException, is SerializationException, is ParsingException -> {
                    e.printStackTrace()
                    RequestErrorType.PARSING_ERROR
                }
                is SourceNotInstalledException -> RequestErrorType.SOURCE_NOT_INSTALLED
                else -> {
                    e.printStackTrace()
                    RequestErrorType.WEATHER_REQ_FAILED
                }
            }
            callback.onCompleted(location, requestErrorType)
        }
    }

    fun getLocatePermissionList(context: Context) = locationHelper.getPermissions(context)
}