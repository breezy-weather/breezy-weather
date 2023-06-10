package wangdaye.com.geometricweather.db.repositories

import wangdaye.com.geometricweather.common.basic.models.options.provider.WeatherSource
import wangdaye.com.geometricweather.db.ObjectBox.boxStore
import wangdaye.com.geometricweather.db.converters.WeatherSourceConverter
import wangdaye.com.geometricweather.db.entities.AlertEntity
import wangdaye.com.geometricweather.db.entities.AlertEntity_

object AlertEntityRepository {
    // insert.
    fun insertAlertList(entityList: List<AlertEntity>) {
        boxStore.boxFor(AlertEntity::class.java).put(entityList)
    }

    // delete.
    fun deleteAlertList(entityList: List<AlertEntity>) {
        boxStore.boxFor(AlertEntity::class.java).remove(entityList)
    }

    // search.
    fun selectLocationAlertEntity(cityId: String, source: WeatherSource): List<AlertEntity> {
        val query = boxStore.boxFor(AlertEntity::class.java)
            .query(
                AlertEntity_.cityId.equal(cityId)
                    .and(
                        AlertEntity_.weatherSource.equal(
                            WeatherSourceConverter().convertToDatabaseValue(source)
                        )
                    )
            ).build()
        val results = query.find()
        query.close()
        return results
    }
}
