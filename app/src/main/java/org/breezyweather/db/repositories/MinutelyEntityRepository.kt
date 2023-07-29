package org.breezyweather.db.repositories

import org.breezyweather.db.ObjectBox.boxStore
import org.breezyweather.db.entities.MinutelyEntity
import org.breezyweather.db.entities.MinutelyEntity_

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
    fun selectMinutelyEntityList(formattedId: String): List<MinutelyEntity> {
        val query = boxStore.boxFor(MinutelyEntity::class.java)
            .query(MinutelyEntity_.formattedId.equal(formattedId)).build()
        val results = query.find()
        query.close()
        return results
    }
}
