package wangdaye.com.geometricweather.db.repositories

import wangdaye.com.geometricweather.common.basic.models.options.provider.WeatherSource
import wangdaye.com.geometricweather.db.ObjectBox.boxStore
import wangdaye.com.geometricweather.db.converters.WeatherSourceConverter
import wangdaye.com.geometricweather.db.entities.DailyEntity
import wangdaye.com.geometricweather.db.entities.DailyEntity_

object DailyEntityRepository {
    // insert.
    fun insertDailyList(entityList: List<DailyEntity>) {
        boxStore.boxFor(DailyEntity::class.java).put(entityList)
    }

    // delete.
    fun deleteDailyEntityList(entityList: List<DailyEntity>) {
        boxStore.boxFor(DailyEntity::class.java).remove(entityList)
    }

    // select.
    fun selectDailyEntityList(cityId: String, source: WeatherSource): List<DailyEntity> {
        val query = boxStore.boxFor(DailyEntity::class.java)
            .query(
                DailyEntity_.cityId.equal(cityId)
                    .and(
                        DailyEntity_.weatherSource.equal(
                            WeatherSourceConverter().convertToDatabaseValue(source)
                        )
                    )
            ).build()
        val results = query.find()
        query.close()
        return results
    }
}
