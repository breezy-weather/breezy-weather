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

package breezyweather.data.location

import android.util.Log
import breezyweather.data.DatabaseHandler
import breezyweather.domain.location.model.Location

class LocationRepository(
    private val handler: DatabaseHandler,
) {

    suspend fun getLocation(formattedId: String, withParameters: Boolean = true): Location? {
        val location = handler.awaitOneOrNull {
            locationsQueries.getLocationById(formattedId, LocationMapper::mapLocation)
        }
        return if (withParameters) {
            location?.copy(
                // I'm sure there must be a more efficient way
                parameters = handler.awaitList {
                    location_parametersQueries.getLocationParametersByLocationId(location.formattedId)
                }.groupBy(
                    { it.source },
                    { it.parameter to it.value_ }
                ).mapValues { parameterList ->
                    parameterList.value.associate { it.first to it.second }
                }
            )
        } else {
            location
        }
    }

    suspend fun getFirstLocation(withParameters: Boolean = true): Location? {
        val location = handler.awaitOneOrNull {
            locationsQueries.getFirstLocation(LocationMapper::mapLocation)
        }
        return if (withParameters) {
            location?.copy(
                // I'm sure there must be a more efficient way
                parameters = handler.awaitList {
                    location_parametersQueries.getLocationParametersByLocationId(location.formattedId)
                }.groupBy(
                    { it.source },
                    { it.parameter to it.value_ }
                ).mapValues { parameterList ->
                    parameterList.value.associate { it.first to it.second }
                }
            )
        } else {
            location
        }
    }

    suspend fun getXLocations(limit: Int, withParameters: Boolean = true): List<Location> {
        val locations = handler.awaitList {
            locationsQueries.getXLocations(limit.toLong(), LocationMapper::mapLocation)
        }
        return if (withParameters) {
            locations.map { location ->
                location.copy(
                    // I'm sure there must be a more efficient way
                    parameters = handler.awaitList {
                        location_parametersQueries.getLocationParametersByLocationId(location.formattedId)
                    }.groupBy(
                        { it.source },
                        { it.parameter to it.value_ }
                    ).mapValues { parameterList ->
                        parameterList.value.associate { it.first to it.second }
                    }
                )
            }
        } else {
            locations
        }
    }

    suspend fun getAllLocations(withParameters: Boolean = true): List<Location> {
        val locations = handler.awaitList {
            locationsQueries.getAllLocations(LocationMapper::mapLocation)
        }
        return if (withParameters) {
            locations.map { location ->
                location.copy(
                    // I'm sure there must be a more efficient way
                    parameters = handler.awaitList {
                        location_parametersQueries.getLocationParametersByLocationId(location.formattedId)
                    }.groupBy(
                        { it.source },
                        { it.parameter to it.value_ }
                    ).mapValues { parameterList ->
                        parameterList.value.associate { it.first to it.second }
                    }
                )
            }
        } else {
            locations
        }
    }

    suspend fun addAll(locations: List<Location>): List<Location> {
        return try {
            handler.await(inTransaction = true) {
                // 1. Delete every location that was removed
                locationsQueries.deleteAllNonMatchingLocations(locations.map { it.formattedId })

                // 2. Insert/Replace locations
                locations.mapIndexed { index, location ->
                    locationsQueries.insert( // Will do a replace if formattedId already exists
                        formattedId = location.formattedId,
                        listOrder = index + 1L,
                        cityId = location.cityId,
                        latitude = location.latitude,
                        longitude = location.longitude,
                        timezone = location.timeZone,
                        country = location.country,
                        countryCode = location.countryCode,
                        admin1 = location.admin1,
                        admin1Code = location.admin1Code,
                        admin2 = location.admin2,
                        admin2Code = location.admin2Code,
                        admin3 = location.admin3,
                        admin3Code = location.admin3Code,
                        admin4 = location.admin4,
                        admin4Code = location.admin4Code,
                        city = location.city,
                        district = location.district,
                        weatherSource = location.weatherSource,
                        currentSource = location.currentSource,
                        airQualitySource = location.airQualitySource,
                        pollenSource = location.pollenSource,
                        minutelySource = location.minutelySource,
                        alertSource = location.alertSource,
                        normalsSource = location.normalsSource,
                        currentPosition = location.isCurrentPosition,
                        needsGeocodeRefresh = location.needsGeocodeRefresh,
                        backgroundWeatherKind = location.backgroundWeatherKind,
                        backgroundDayNightType = location.backgroundDayNightType
                    )

                    // 3. Update location parameters
                    // 3a. Delete no longer existing parameters
                    location_parametersQueries.deleteAllNonMatchingParameters(
                        location.formattedId,
                        location.parameters.map { it.key }
                    )

                    // 3b. Insert/replace parameters
                    location.parameters.forEach { source ->
                        source.value.forEach {
                            // Will do a replace if formattedId/parameter already exists
                            location_parametersQueries.insert(
                                location.formattedId,
                                source.key,
                                it.key,
                                it.value
                            )
                        }
                    }

                    location
                }
            }
        } catch (e: Exception) {
            Log.e("BreezyWeather", e.toString())
            emptyList()
        }
    }

    suspend fun insertParameters(locationFormattedId: String, locationParameters: Map<String, Map<String, String>>) {
        handler.await(inTransaction = true) {
            // 3a. Delete no longer existing parameters
            location_parametersQueries.deleteAllNonMatchingParameters(
                locationFormattedId,
                locationParameters.map { it.key }
            )

            // 3b. Insert/replace parameters
            locationParameters.forEach { source ->
                source.value.forEach {
                    // Will do a replace if formattedId/parameter already exists
                    location_parametersQueries.insert(
                        locationFormattedId,
                        source.key,
                        it.key,
                        it.value
                    )
                }
            }
        }
    }

    suspend fun update(location: Location, oldFormattedId: String? = null): Boolean {
        return try {
            handler.await(inTransaction = true) {
                if (oldFormattedId != null && oldFormattedId != location.formattedId) {
                    locationsQueries.updateFormattedId(
                        oldFormattedId = oldFormattedId,
                        newFormattedId = location.formattedId
                    )
                }
                locationsQueries.update(
                    formattedId = location.formattedId,
                    cityId = location.cityId,
                    latitude = location.latitude,
                    longitude = location.longitude,
                    timezone = location.javaTimeZone.id,
                    country = location.country,
                    countryCode = location.countryCode,
                    admin1 = location.admin1,
                    admin1Code = location.admin1Code,
                    admin2 = location.admin2,
                    admin2Code = location.admin2Code,
                    admin3 = location.admin3,
                    admin3Code = location.admin3Code,
                    admin4 = location.admin4,
                    admin4Code = location.admin4Code,
                    city = location.city,
                    district = location.district,
                    weatherSource = location.weatherSource,
                    currentSource = location.currentSource,
                    airQualitySource = location.airQualitySource,
                    pollenSource = location.pollenSource,
                    minutelySource = location.minutelySource,
                    alertSource = location.alertSource,
                    normalsSource = location.normalsSource,
                    currentPosition = location.isCurrentPosition,
                    needsGeocodeRefresh = location.needsGeocodeRefresh,
                    backgroundWeatherKind = location.backgroundWeatherKind,
                    backgroundDayNightType = location.backgroundDayNightType
                )
            }
            true
        } catch (e: Exception) {
            Log.e("BreezyWeather", e.toString())
            false
        }
    }

    suspend fun delete(formattedId: String) {
        handler.await {
            locationsQueries.deleteLocation(formattedId)
        }
    }
}
