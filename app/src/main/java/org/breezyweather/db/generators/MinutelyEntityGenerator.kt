package org.breezyweather.db.generators

import org.breezyweather.common.basic.models.weather.Minutely
import org.breezyweather.db.entities.MinutelyEntity

object MinutelyEntityGenerator {
    fun generate(formattedId: String, minutely: Minutely): MinutelyEntity {
        return MinutelyEntity(
            formattedId = formattedId,
            date = minutely.date,
            minuteInterval = minutely.minuteInterval,
            dbz = minutely.dbz
        )
    }

    fun generate(formattedId: String, minutelyList: List<Minutely>): List<MinutelyEntity> {
        val entityList: MutableList<MinutelyEntity> = ArrayList(minutelyList.size)
        for (minutely in minutelyList) {
            entityList.add(generate(formattedId, minutely))
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
