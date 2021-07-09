package wangdaye.com.geometricweather.main

import android.content.Context
import kotlinx.coroutines.*
import wangdaye.com.geometricweather.common.basic.models.Location
import wangdaye.com.geometricweather.common.basic.models.Response
import wangdaye.com.geometricweather.common.basic.models.weather.Weather
import wangdaye.com.geometricweather.db.DatabaseHelper
import wangdaye.com.geometricweather.location.LocationHelper
import wangdaye.com.geometricweather.weather.WeatherHelper
import java.util.*
import java.util.concurrent.Executors
import javax.inject.Inject
import kotlin.collections.ArrayList

class MainActivityRepository(private val locationHelper: LocationHelper,
                             private val weatherHelper: WeatherHelper,
                             private val ioDispatcher: CoroutineDispatcher,
                             private val exclusiveDispatcher: ExecutorCoroutineDispatcher) {

    @Inject constructor(
            locationHelper: LocationHelper,
            weatherHelper: WeatherHelper
    ): this(
            locationHelper,
            weatherHelper,
            ioDispatcher = Dispatchers.IO,
            exclusiveDispatcher = Executors.newSingleThreadExecutor().asCoroutineDispatcher()
    )

    fun destroy() {
        exclusiveDispatcher.close()
    }

    suspend fun getLocationList(context: Context, oldList: List<Location>): List<Location> {
        return withContext(exclusiveDispatcher) {
            val list = DatabaseHelper.getInstance(context).readLocationList()
            for (oldOne in oldList) {
                for (newOne in list) {
                    if (newOne.equals(oldOne)) {
                        newOne.weather = oldOne.weather
                        break
                    }
                }
            }
            return@withContext list
        }
    }

    suspend fun getWeatherCaches(context: Context, list: List<Location>): List<Location> {
        return withContext(exclusiveDispatcher) {
            for (location in list) {
                location.weather = DatabaseHelper.getInstance(context).readWeather(location)
            }
            return@withContext list
        }
    }

    suspend fun writeLocation(context: Context, location: Location) {
        withContext(exclusiveDispatcher) {
            DatabaseHelper.getInstance(context).writeLocation(location)
            location.weather?.let {
                DatabaseHelper.getInstance(context).writeWeather(location, it)
            }
        }
    }

    suspend fun writeLocationList(context: Context, locationList: List<Location>) {
        withContext(exclusiveDispatcher) {
            DatabaseHelper.getInstance(context).writeLocationList(locationList)
        }
    }

    suspend fun writeLocationList(context: Context, locationList: List<Location>, newIndex: Int) {
        withContext(exclusiveDispatcher) {
            DatabaseHelper.getInstance(context).writeLocationList(locationList)
            locationList[newIndex].weather?.let {
                DatabaseHelper.getInstance(context).writeWeather(locationList[newIndex], it)
            }
        }
    }

    suspend fun deleteLocation(context: Context, location: Location) {
        withContext(exclusiveDispatcher) {
            DatabaseHelper.getInstance(context).deleteLocation(location)
            DatabaseHelper.getInstance(context).deleteWeather(location)
        }
    }

    suspend fun getLocation(context: Context, location: Location): Response<Location?> {
        return withContext(ioDispatcher) {
            locationHelper.getLocation(context, location, false)
        }
    }

    suspend fun getWeather(context: Context, location: Location): Response<Weather?> {
        return withContext(ioDispatcher) {
            weatherHelper.getWeather(context, location)
        }
    }

    fun getLocatePermissionList(context: Context): List<String> {
        val list = ArrayList<String>()
        locationHelper.getPermissions(context).forEach {
            list.add(it)
        }
        return list
    }
}