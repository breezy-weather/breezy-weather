package wangdaye.com.geometricweather.db.repositories

import wangdaye.com.geometricweather.common.basic.models.options.provider.WeatherSource
import wangdaye.com.geometricweather.db.ObjectBox.boxStore
import wangdaye.com.geometricweather.db.converters.WeatherSourceConverter
import wangdaye.com.geometricweather.db.entities.HourlyEntity
import wangdaye.com.geometricweather.db.entities.HourlyEntity_

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
