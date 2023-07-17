package org.breezyweather.db.repositories

import org.breezyweather.db.ObjectBox.boxStore
import org.breezyweather.db.entities.DailyEntity
import org.breezyweather.db.entities.DailyEntity_

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
    fun selectDailyEntityList(cityId: String, source: String): List<DailyEntity> {
        val query = boxStore.boxFor(DailyEntity::class.java)
            .query(
                DailyEntity_.cityId.equal(cityId)
                    .and(DailyEntity_.weatherSource.equal(source))
            ).build()
        val results = query.find()
        query.close()
        return results
    }
}
