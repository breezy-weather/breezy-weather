package org.breezyweather.db.repositories

import org.breezyweather.common.basic.models.options.provider.WeatherSource
import org.breezyweather.db.ObjectBox.boxStore
import org.breezyweather.db.converters.WeatherSourceConverter
import org.breezyweather.db.entities.MinutelyEntity_

object MinutelyEntityRepository {
    // insert.
    fun insertMinutelyList(entityList: List<org.breezyweather.db.entities.MinutelyEntity>) {
        boxStore.boxFor(org.breezyweather.db.entities.MinutelyEntity::class.java).put(entityList)
    }

    // delete.
    fun deleteMinutelyEntityList(entityList: List<org.breezyweather.db.entities.MinutelyEntity>) {
        boxStore.boxFor(org.breezyweather.db.entities.MinutelyEntity::class.java).remove(entityList)
    }

    // select.
    fun selectMinutelyEntityList(cityId: String, source: WeatherSource): List<org.breezyweather.db.entities.MinutelyEntity> {
        val query = boxStore.boxFor(org.breezyweather.db.entities.MinutelyEntity::class.java)
            .query(
                MinutelyEntity_.cityId.equal(cityId)
                    .and(
                        MinutelyEntity_.weatherSource.equal(
                            WeatherSourceConverter().convertToDatabaseValue(source)
                        )
                    )
            ).build()
        val results = query.find()
        query.close()
        return results
    }
}
