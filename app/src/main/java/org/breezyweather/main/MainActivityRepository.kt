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

package org.breezyweather.main

import android.content.Context
import org.breezyweather.common.basic.models.Location
import org.breezyweather.common.source.RefreshError
import org.breezyweather.common.utils.helpers.AsyncHelper
import org.breezyweather.db.repositories.LocationEntityRepository
import org.breezyweather.db.repositories.LocationParameterEntityRepository
import org.breezyweather.db.repositories.WeatherEntityRepository
import org.breezyweather.main.utils.RefreshErrorType
import org.breezyweather.sources.RefreshHelper
import java.util.concurrent.Executors
import javax.inject.Inject

class MainActivityRepository @Inject constructor(
    private val refreshHelper: RefreshHelper
) {
    private val singleThreadExecutor = Executors.newSingleThreadExecutor()

    interface WeatherRequestCallback {
        fun onCompleted(
            location: Location,
            errors: List<RefreshError> = emptyList()
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
            LocationParameterEntityRepository.deleteLocationParameters(location.formattedId)
            WeatherEntityRepository.deleteWeather(location)
        }, singleThreadExecutor)
    }

    suspend fun getWeather(
        context: Context,
        location: Location,
        callback: WeatherRequestCallback,
    ) {
        try {
            val locationResult = refreshHelper.getLocation(
                context, location, false
            )

            if (locationResult.location.isUsable
                && !locationResult.location.needsGeocodeRefresh) {
                val weatherResult = refreshHelper.getWeather(context, locationResult.location)
                callback.onCompleted(
                    locationResult.location.copy(weather = weatherResult.weather),
                    locationResult.errors + weatherResult.errors
                )
            } else {
                callback.onCompleted(
                    locationResult.location,
                    locationResult.errors
                )
            }
        } catch (e: Throwable) {
            // Should never happen
            e.printStackTrace()
            callback.onCompleted(
                location,
                listOf(RefreshError(RefreshErrorType.WEATHER_REQ_FAILED))
            )
        }
    }

    fun getLocatePermissionList(context: Context) = refreshHelper.getPermissions(context)
}