package org.breezyweather.main

import android.content.Context
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

    fun initLocations(context: Context, formattedId: String): List<Location> {
        val list = LocationEntityRepository.readLocationList(context).toMutableList()

        var index = 0
        for (i in list.indices) {
            if (list[i].formattedId == formattedId) {
                index = i
                break
            }
        }

        list[index] = list[index].copy(weather = WeatherEntityRepository.readWeather(list[index]))
        return list
    }

    fun getWeatherCacheForLocations(
        oldList: List<Location>,
        ignoredFormattedId: String,
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

    fun getWeather(
        context: Context,
        location: Location,
        locate: Boolean,
        callback: WeatherRequestCallback,
    ) {
        if (locate) {
            ensureValidLocationInformation(context, location, callback)
        } else {
            getWeatherWithValidLocationInformation(context, location, null, callback)
        }
    }

    private fun ensureValidLocationInformation(
        context: Context,
        location: Location,
        callback: WeatherRequestCallback,
    ) = locationHelper.requestLocation(
        context,
        location,
        false,
        object : LocationHelper.OnRequestLocationListener {

            override fun requestLocationSuccess(requestLocation: Location) {
                if (requestLocation.formattedId != location.formattedId) {
                    return
                }
                getWeatherWithValidLocationInformation(
                    context,
                    requestLocation,
                    null,
                    callback
                )
            }

            override fun requestLocationFailed(requestLocation: Location, requestErrorType: RequestErrorType) {
                if (requestLocation.formattedId != location.formattedId) {
                    return
                }
                getWeatherWithValidLocationInformation(
                    context,
                    requestLocation,
                    requestErrorType,
                    callback
                )
            }
        }
    )

    private fun getWeatherWithValidLocationInformation(
        context: Context,
        location: Location,
        requestErrorType: RequestErrorType?,
        callback: WeatherRequestCallback,
    ) = weatherHelper.requestWeather(
        context,
        location,
        object : WeatherHelper.OnRequestWeatherListener {
            override fun requestWeatherSuccess(requestLocation: Location) {
                if (requestLocation.formattedId != location.formattedId) {
                    return
                }
                callback.onCompleted(requestLocation, requestErrorType)
            }

            override fun requestWeatherFailed(requestLocation: Location, requestErrorType: RequestErrorType) {
                if (requestLocation.formattedId != location.formattedId) {
                    return
                }
                callback.onCompleted(requestLocation, requestErrorType)
            }
        }
    )

    fun getLocatePermissionList(context: Context) = locationHelper
        .getPermissions(context)
        .toList()

    fun cancelWeatherRequest() {
        locationHelper.cancel()
        weatherHelper.cancel()
    }
}