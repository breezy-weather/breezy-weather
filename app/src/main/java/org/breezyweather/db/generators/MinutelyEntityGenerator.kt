package org.breezyweather.db.generators

import org.breezyweather.common.basic.models.options.provider.WeatherSource
import org.breezyweather.common.basic.models.weather.Minutely
import org.breezyweather.db.converters.WeatherSourceConverter
import org.breezyweather.db.entities.MinutelyEntity

object MinutelyEntityGenerator {
    fun generate(cityId: String, source: WeatherSource, minutely: Minutely): MinutelyEntity {
        return MinutelyEntity(
            cityId = cityId,
            weatherSource = WeatherSourceConverter().convertToDatabaseValue(source),
            date = minutely.date,
            minuteInterval = minutely.minuteInterval,
            dbz = minutely.dbz
        )
    }

    fun generate(cityId: String, source: WeatherSource, minutelyList: List<Minutely>): List<MinutelyEntity> {
        val entityList: MutableList<MinutelyEntity> = ArrayList(minutelyList.size)
        for (minutely in minutelyList) {
            entityList.add(generate(cityId, source, minutely))
        }
        return entityList
    }

    fun generate(entity: MinutelyEntity): Minutely {
        return Minutely(
            entity.date,
            entity.minuteInterval,
            entity.dbz
        )
    }

    fun generate(entityList: List<MinutelyEntity>): List<Minutely> {
        val dailyList: MutableList<Minutely> = ArrayList(entityList.size)
        for (entity in entityList) {
            dailyList.add(generate(entity))
        }
        return dailyList
    }

}
