package org.breezyweather.db.repositories

import org.breezyweather.common.basic.models.Location
import org.breezyweather.db.ObjectBox.boxStore
import org.breezyweather.db.entities.LocationEntity
import org.breezyweather.db.entities.LocationEntity_
import org.breezyweather.db.generators.LocationEntityGenerator
import java.util.Locale

object LocationEntityRepository {
    private val mWritingLock: Any = Any()

    // insert.
    fun insertLocationEntity(entity: LocationEntity) {
        boxStore.boxFor(LocationEntity::class.java).put(entity)
    }

    fun insertLocationEntityList(entityList: List<LocationEntity>) {
        if (entityList.isNotEmpty()) {
            boxStore.boxFor(LocationEntity::class.java).put(entityList)
        }
    }

    fun writeLocation(location: Location, oldFormattedId: String? = null) {
        val entity = LocationEntityGenerator.generate(location)
        boxStore.callInTxNoException {
            val dbEntity = selectLocationEntity(oldFormattedId ?: location.formattedId)
            if (dbEntity == null) {
                insertLocationEntity(entity)
            } else {
                if (oldFormattedId != null && location.formattedId != oldFormattedId) {
                    // Clean up weather data from oldFormattedId if we changed formattedId
                    WeatherEntityRepository.deleteWeather(oldFormattedId)
                }

                entity.id = dbEntity.id
                updateLocationEntity(entity)
            }
            true
        }
    }

    fun writeLocationList(list: List<Location>) {
        boxStore.callInTxNoException {
            deleteLocationEntityList()
            insertLocationEntityList(LocationEntityGenerator.generateEntityList(list))
            true
        }
    }

    // delete.
    fun deleteLocation(entity: Location) {
        val query = boxStore.boxFor(LocationEntity::class.java)
            .query(LocationEntity_.formattedId.equal(entity.formattedId))
            .build()
        val results = query.find()
        query.close()
        boxStore.boxFor(LocationEntity::class.java).remove(results)
    }

    fun deleteLocationEntityList() {
        boxStore.boxFor(LocationEntity::class.java).removeAll()
    }

    // update.
    fun updateLocationEntity(entity: LocationEntity) {
        boxStore.boxFor(LocationEntity::class.java).put(entity)
    }

    // select.
    fun readLocation(location: Location): Location? {
        return readLocation(location.formattedId)
    }

    fun readLocation(formattedId: String): Location? {
        val entity = selectLocationEntity(formattedId)
        return if (entity != null) LocationEntityGenerator.generate(entity) else null
    }

    fun readLocationList(): List<Location> {
        return LocationEntityGenerator.generateModuleList(selectLocationEntityList())
    }

    fun selectLocationEntity(formattedId: String): LocationEntity? {
        val query = boxStore.boxFor(LocationEntity::class.java)
            .query(LocationEntity_.formattedId.equal(formattedId))
            .build()
        val entityList = query.find()
        query.close()
        return if (entityList.size <= 0) null else entityList[0]
    }

    fun selectLocationEntityList(): MutableList<LocationEntity> {
        val query = boxStore.boxFor(LocationEntity::class.java).query().build()
        val results = query.find()
        query.close()
        return results
    }

    fun countLocation(): Int {
        val query = boxStore.boxFor(LocationEntity::class.java).query().build()
        val count = query.count()
        query.close()
        return count.toInt()
    }

    fun regenerateAllFormattedId() {
        val locationEntityList = selectLocationEntityList()
        boxStore.callInTxNoException {
            locationEntityList.forEach { entity ->
                entity.formattedId = if (entity.currentPosition) {
                    Location.CURRENT_POSITION_ID
                } else {
                    String.format(Locale.US, "%f", entity.latitude) + "&" +
                            String.format(Locale.US, "%f", entity.longitude) + "&" +
                            entity.weatherSource
                }
                // Also clean up cityId containing "lon,lat"
                if (entity.cityId?.contains(",") == true) {
                    entity.cityId = null
                }
                updateLocationEntity(entity)
            }
            true
        }
    }
}
