package org.breezyweather.db.generators

import org.breezyweather.common.basic.models.options.provider.WeatherSource
import org.breezyweather.common.basic.models.weather.Alert
import org.breezyweather.db.converters.WeatherSourceConverter
import org.breezyweather.db.entities.AlertEntity

object AlertEntityGenerator {
    fun generate(cityId: String, source: WeatherSource, alert: Alert): AlertEntity {
        return AlertEntity(
            cityId = cityId,
            weatherSource = WeatherSourceConverter().convertToDatabaseValue(source),
            alertId = alert.alertId,
            startDate = alert.startDate,
            endDate = alert.endDate,
            description = alert.description,
            content = alert.content,
            priority = alert.priority,
            color = alert.color
        )
    }

    fun generate(cityId: String, source: WeatherSource, alertList: List<Alert>): List<AlertEntity> {
        val entityList: MutableList<AlertEntity> = ArrayList(alertList.size)
        for (alert in alertList) {
            entityList.add(generate(cityId, source, alert))
        }
        return entityList
    }

    fun generate(entity: AlertEntity): Alert {
        return Alert(
            entity.alertId,
            entity.startDate,
            entity.endDate,
            entity.description,
            entity.content,
            entity.priority,
            entity.color
        )
    }

    fun generate(entityList: List<AlertEntity>): List<Alert> {
        val dailyList: MutableList<Alert> = ArrayList(entityList.size)
        for (entity in entityList) {
            dailyList.add(generate(entity))
        }
        return dailyList
    }
}
