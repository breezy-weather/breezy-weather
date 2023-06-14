package org.breezyweather.db.repositories

import org.breezyweather.common.basic.models.options.provider.WeatherSource
import org.breezyweather.db.ObjectBox.boxStore
import org.breezyweather.db.converters.WeatherSourceConverter
import org.breezyweather.db.entities.HourlyEntity
import org.breezyweather.db.entities.HourlyEntity_

object HourlyEntityRepository {
    // insert.
    fun insertHourlyList(entityList: List<HourlyEntity>) {
        boxStore.boxFor(HourlyEntity::class.java).put(entityList)
    }

    // delete.
    fun deleteHourlyEntityList(entityList: List<HourlyEntity>) {
        boxStore.boxFor(HourlyEntity::class.java).remove(entityList)
    }

    // select.
    fun selectHourlyEntityList(cityId: String, source: WeatherSource): List<HourlyEntity> {
        val query = boxStore.boxFor(HourlyEntity::class.java)
            .query(
                HourlyEntity_.cityId.equal(cityId)
                    .and(
                        HourlyEntity_.weatherSource.equal(
                            WeatherSourceConverter().convertToDatabaseValue(source)
                        )
                    )
            ).build()
        val results = query.find()
        query.close()
        return results
    }
}
