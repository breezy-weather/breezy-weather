package org.breezyweather.db.generators

import org.breezyweather.common.basic.models.ChineseCity
import org.breezyweather.db.entities.ChineseCityEntity

object ChineseCityEntityGenerator {
    fun generate(city: ChineseCity): ChineseCityEntity {
        return ChineseCityEntity(
            cityId = city.cityId,
            province = city.province,
            city = city.city,
            district = city.district,
            latitude = city.latitude,
            longitude = city.longitude
        )
    }

    fun generateEntityList(cityList: List<ChineseCity>): List<ChineseCityEntity> {
        val entityList: MutableList<ChineseCityEntity> = ArrayList(cityList.size)
        for (city in cityList) {
            entityList.add(generate(city))
        }
        return entityList
    }

    fun generate(entity: ChineseCityEntity): ChineseCity {
        return ChineseCity(
            entity.cityId,
            entity.province,
            entity.city,
            entity.district,
            entity.latitude,
            entity.longitude
        )
    }

    fun generateModuleList(entityList: List<ChineseCityEntity>): List<ChineseCity> {
        val cityList: MutableList<ChineseCity> = ArrayList(entityList.size)
        for (entity in entityList) {
            cityList.add(generate(entity))
        }
        return cityList
    }
}
