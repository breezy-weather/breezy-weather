package wangdaye.com.geometricweather.db.repositories

import wangdaye.com.geometricweather.common.basic.models.options.provider.WeatherSource
import wangdaye.com.geometricweather.db.ObjectBox.boxStore
import wangdaye.com.geometricweather.db.converters.WeatherSourceConverter
import wangdaye.com.geometricweather.db.entities.MinutelyEntity
import wangdaye.com.geometricweather.db.entities.MinutelyEntity_

object MinutelyEntityRepository {
    // insert.
    fun insertMinutelyList(entityList: List<MinutelyEntity>) {
        boxStore.boxFor(MinutelyEntity::class.java).put(entityList)
    }

    // delete.
    fun deleteMinutelyEntityList(entityList: List<MinutelyEntity>) {
        boxStore.boxFor(MinutelyEntity::class.java).remove(entityList)
    }

    // select.
    fun selectMinutelyEntityList(cityId: String, source: WeatherSource): List<MinutelyEntity> {
        val query = boxStore.boxFor(MinutelyEntity::class.java)
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
