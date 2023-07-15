package org.breezyweather.main

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.rx3.awaitSingle
import kotlinx.coroutines.withContext
import org.breezyweather.common.basic.models.Location
import org.breezyweather.common.utils.helpers.AsyncHelper
import org.breezyweather.db.repositories.LocationEntityRepository
import org.breezyweather.db.repositories.WeatherEntityRepository
import org.breezyweather.location.LocationHelper
import org.breezyweather.main.utils.RequestErrorType
import org.breezyweather.weather.WeatherHelper
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

    fun destroy() {
        cancelWeatherRequest()
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

    suspend fun getWeather(
        context: Context,
        location: Location,
        locate: Boolean,
        callback: WeatherRequestCallback,
    ) {
        try {
            val locationToProcess = if (locate) {
                locationHelper.getCurrentLocationWithReverseGeocoding(
                    context,
                    location,
                    false
                )
            } else location

            try {
                val requestWeather = weatherHelper.requestWeather(
                    context,
                    locationToProcess
                ).awaitSingle()
                callback.onCompleted(locationToProcess.copy(weather = requestWeather), null)
            } catch (e: Throwable) {
                e.printStackTrace()
                callback.onCompleted(location, RequestErrorType.WEATHER_REQ_FAILED)
            }
        } catch (e: Throwable) {
            e.printStackTrace()
            callback.onCompleted(location, RequestErrorType.LOCATION_FAILED)
        }
    }

    fun getLocatePermissionList(context: Context) = locationHelper.getPermissions(context)

    fun cancelWeatherRequest() {
        locationHelper.cancel()
        weatherHelper.cancel()
    }
}