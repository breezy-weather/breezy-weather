package org.breezyweather.db.generators

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
            allergenSource = location.allergenSource,
            minutelySource = location.minutelySource,
            alertSource = location.alertSource,
            currentPosition = location.isCurrentPosition,
            residentPosition = location.isResidentPosition
        )
    }

    fun generateEntityList(locationList: List<Location>): List<LocationEntity> {
        val entityList: MutableList<LocationEntity> = ArrayList(locationList.size)
        for (location in locationList) {
            entityList.add(generate(location))
        }
        return entityList
    }

    fun generate(entity: LocationEntity): Location {
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
            entity.allergenSource,
            entity.minutelySource,
            entity.alertSource,
            entity.currentPosition,
            entity.residentPosition
        )
    }

    fun generateModuleList(entityList: List<LocationEntity>): List<Location> {
        val locationList: MutableList<Location> = ArrayList(entityList.size)
        for (entity in entityList) {
            locationList.add(generate(entity))
        }
        return locationList
    }
}
