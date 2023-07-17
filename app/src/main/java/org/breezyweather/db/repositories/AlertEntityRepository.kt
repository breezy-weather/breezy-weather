package org.breezyweather.db.repositories

import org.breezyweather.db.ObjectBox.boxStore
import org.breezyweather.db.entities.AlertEntity
import org.breezyweather.db.entities.AlertEntity_

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
    fun selectLocationAlertEntity(cityId: String, source: String): List<AlertEntity> {
        val query = boxStore.boxFor(AlertEntity::class.java)
            .query(
                AlertEntity_.cityId.equal(cityId)
                    .and(AlertEntity_.weatherSource.equal(source))
            ).build()
        val results = query.find()
        query.close()
        return results
    }
}
