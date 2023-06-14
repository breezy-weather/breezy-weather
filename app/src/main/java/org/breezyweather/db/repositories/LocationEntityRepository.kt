package org.breezyweather.db.repositories

import android.content.Context
import org.breezyweather.common.basic.models.Location
import org.breezyweather.db.ObjectBox.boxStore
import org.breezyweather.db.entities.LocationEntity_

object LocationEntityRepository {
    private val mWritingLock: Any = Any()

    // insert.
    fun insertLocationEntity(entity: org.breezyweather.db.entities.LocationEntity) {
        boxStore.boxFor(org.breezyweather.db.entities.LocationEntity::class.java).put(entity)
    }

    fun insertLocationEntityList(entityList: List<org.breezyweather.db.entities.LocationEntity>) {
        if (entityList.isNotEmpty()) {
            boxStore.boxFor(org.breezyweather.db.entities.LocationEntity::class.java).put(entityList)
        }
    }

    fun writeLocation(location: Location) {
        val entity = org.breezyweather.db.generators.LocationEntityGenerator.generate(location)
        boxStore.callInTxNoException {
            if (selectLocationEntity(location.formattedId) == null) {
                insertLocationEntity(entity)
            } else {
                updateLocationEntity(entity)
            }
            true
        }
    }

    fun writeLocationList(list: List<Location?>) {
        boxStore.callInTxNoException {
            deleteLocationEntityList()
            insertLocationEntityList(org.breezyweather.db.generators.LocationEntityGenerator.generateEntityList(list))
            true
        }
    }

    // delete.
    fun deleteLocation(entity: Location) {
        val query = boxStore.boxFor(org.breezyweather.db.entities.LocationEntity::class.java)
            .query(LocationEntity_.formattedId.equal(entity.formattedId))
            .build()
        val results = query.find()
        query.close()
        boxStore.boxFor(org.breezyweather.db.entities.LocationEntity::class.java).remove(results)
    }

    fun deleteLocationEntityList() {
        boxStore.boxFor(org.breezyweather.db.entities.LocationEntity::class.java).removeAll()
    }

    // update.
    fun updateLocationEntity(entity: org.breezyweather.db.entities.LocationEntity) {
        boxStore.boxFor(org.breezyweather.db.entities.LocationEntity::class.java).put(entity)
    }

    // select.
    fun readLocation(location: Location): Location? {
        return readLocation(location.formattedId)
    }

    fun readLocation(formattedId: String): Location? {
        val entity = selectLocationEntity(formattedId)
        return if (entity != null) org.breezyweather.db.generators.LocationEntityGenerator.generate(entity) else null
    }

    fun readLocationList(context: Context): MutableList<Location> {
        val entityList = selectLocationEntityList()
        if (entityList.size == 0) {
            synchronized(mWritingLock) {
                if (countLocation() == 0) {
                    val entity = org.breezyweather.db.generators.LocationEntityGenerator.generate(
                        Location.buildLocal(context)
                    )
                    entityList.add(entity)
                    insertLocationEntityList(entityList)
                    return org.breezyweather.db.generators.LocationEntityGenerator.generateModuleList(entityList)
                }
            }
        }
        return org.breezyweather.db.generators.LocationEntityGenerator.generateModuleList(entityList)
    }

    fun selectLocationEntity(formattedId: String): org.breezyweather.db.entities.LocationEntity? {
        val query = boxStore.boxFor(org.breezyweather.db.entities.LocationEntity::class.java)
            .query(LocationEntity_.formattedId.equal(formattedId))
            .build()
        val entityList = query.find()
        query.close()
        return if (entityList.size <= 0) null else entityList[0]
    }

    fun selectLocationEntityList(): MutableList<org.breezyweather.db.entities.LocationEntity> {
        val query = boxStore.boxFor(org.breezyweather.db.entities.LocationEntity::class.java).query().build()
        val results = query.find()
        query.close()
        return results
    }

    fun countLocation(): Int {
        val query = boxStore.boxFor(org.breezyweather.db.entities.LocationEntity::class.java).query().build()
        val count = query.count()
        query.close()
        return count.toInt()
    }
}
