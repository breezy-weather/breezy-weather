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

package org.breezyweather.db.generators

import io.objectbox.BoxStore
import org.breezyweather.common.basic.models.Location
import org.breezyweather.db.entities.LocationEntity

object LocationEntityGenerator {
    fun generate(location: Location): LocationEntity {
        return LocationEntity(
            formattedId = location.formattedId,
            cityId = location.cityId,
            latitude = location.latitude,
            longitude = location.longitude,
            timeZone = location.timeZone,
            country = location.country,
            countryCode = location.countryCode,
            province = location.province,
            provinceCode = location.provinceCode,
            city = location.city,
            district = location.district,
            weatherSource = location.weatherSource,
            airQualitySource = location.airQualitySource,
            pollenSource = location.pollenSource,
            minutelySource = location.minutelySource,
            alertSource = location.alertSource,
            normalsSource = location.normalsSource,
            currentPosition = location.isCurrentPosition,
            residentPosition = location.isResidentPosition,
            needsGeocodeRefresh = location.needsGeocodeRefresh
        )
    }

    fun generateEntityList(locationList: List<Location>): List<LocationEntity> {
        val entityList: MutableList<LocationEntity> = ArrayList(locationList.size)
        for (location in locationList) {
            entityList.add(generate(location))
        }
        return entityList
    }

    fun generate(entity: LocationEntity, boxStore: BoxStore): Location {
        return Location(
            entity.cityId,
            entity.latitude,
            entity.longitude,
            entity.timeZone,
            entity.country,
            entity.countryCode,
            entity.province,
            entity.provinceCode,
            entity.city,
            entity.district,
            weather = null,
            entity.weatherSource,
            entity.airQualitySource,
            entity.pollenSource,
            entity.minutelySource,
            entity.alertSource,
            entity.normalsSource,
            entity.currentPosition,
            entity.residentPosition,
            entity.needsGeocodeRefresh,
            parameters = LocationParameterEntityGenerator.generate(entity.getParameterEntityList(boxStore))
        )
    }

    fun generateModuleList(entityList: List<LocationEntity>, boxStore: BoxStore): List<Location> {
        val locationList: MutableList<Location> = ArrayList(entityList.size)
        for (entity in entityList) {
            locationList.add(generate(entity, boxStore))
        }
        return locationList
    }
}
