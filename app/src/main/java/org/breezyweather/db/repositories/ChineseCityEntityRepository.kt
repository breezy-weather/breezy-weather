package org.breezyweather.db.repositories

import android.content.Context
import io.objectbox.query.Query
import io.objectbox.query.QueryCondition
import org.breezyweather.common.basic.models.ChineseCity
import org.breezyweather.common.utils.FileUtils
import org.breezyweather.db.ObjectBox.boxStore
import org.breezyweather.db.entities.ChineseCityEntity_
import kotlin.math.pow

object ChineseCityEntityRepository {
    private val mWritingLock: Any = Any()

    // insert.
    fun insertChineseCityEntityList(entityList: List<org.breezyweather.db.entities.ChineseCityEntity>) {
        if (entityList.isNotEmpty()) {
            boxStore.boxFor(org.breezyweather.db.entities.ChineseCityEntity::class.java).put(entityList)
        }
    }
    fun ensureChineseCityList(context: Context) {
        if (countChineseCity() < 3216) {
            synchronized(mWritingLock) {
                if (countChineseCity() < 3216) {
                    val list = FileUtils.readCityList(context)
                    deleteChineseCityEntityList()
                    insertChineseCityEntityList(org.breezyweather.db.generators.ChineseCityEntityGenerator.generateEntityList(list))
                }
            }
        }
    }

    // delete.
    fun deleteChineseCityEntityList() {
        boxStore.boxFor(org.breezyweather.db.entities.ChineseCityEntity::class.java).removeAll()
    }

    // select.
    fun readChineseCity(name: String): ChineseCity? {
        val entity = selectChineseCityEntity(name)
        return if (entity != null) org.breezyweather.db.generators.ChineseCityEntityGenerator.generate(entity) else null
    }

    fun readChineseCity(province: String, city: String, district: String): ChineseCity? {
        val entity = selectChineseCityEntity(province, city, district)
        return if (entity != null) org.breezyweather.db.generators.ChineseCityEntityGenerator.generate(entity) else null
    }

    fun readChineseCity(latitude: Float, longitude: Float): ChineseCity? {
        val entity = selectChineseCityEntity(latitude, longitude)
        return if (entity != null) org.breezyweather.db.generators.ChineseCityEntityGenerator.generate(entity) else null
    }

    fun readChineseCityList(name: String): List<ChineseCity> {
        return org.breezyweather.db.generators.ChineseCityEntityGenerator.generateModuleList(selectChineseCityEntityList(name))
    }

    fun selectChineseCityEntity(name: String?): org.breezyweather.db.entities.ChineseCityEntity? {
        if (name.isNullOrEmpty()) return null
        val chineseCityEntityBox = boxStore.boxFor(org.breezyweather.db.entities.ChineseCityEntity::class.java)
        val query = chineseCityEntityBox.query(
            ChineseCityEntity_.district.equal(name)
                .or(ChineseCityEntity_.city.equal(name))
        ).build()
        val entityList = query.find()
        query.close()
        return if (entityList.size <= 0) null else entityList[0]
    }

    fun selectChineseCityEntity(province: String, city: String, district: String): org.breezyweather.db.entities.ChineseCityEntity? {
        val chineseCityEntityBox = boxStore.boxFor(org.breezyweather.db.entities.ChineseCityEntity::class.java)
        val conditionList: MutableList<QueryCondition<org.breezyweather.db.entities.ChineseCityEntity>> = ArrayList()
        conditionList.add(
            ChineseCityEntity_.district.equal(district)
                .and(ChineseCityEntity_.city.equal(city))
        )
        conditionList.add(
            ChineseCityEntity_.district.equal(district)
                .and(ChineseCityEntity_.province.equal(city))
        )
        conditionList.add(
            ChineseCityEntity_.city.equal(district)
                .and(ChineseCityEntity_.province.equal(city))
        )
        conditionList.add(ChineseCityEntity_.city.equal(city))
        conditionList.add(
            ChineseCityEntity_.district.equal(district)
                .and(ChineseCityEntity_.province.equal(city))
        )
        conditionList.add(
            ChineseCityEntity_.district.equal(district)
                .and(ChineseCityEntity_.city.equal(city))
        )
        conditionList.add(ChineseCityEntity_.district.equal(city))
        conditionList.add(ChineseCityEntity_.city.equal(district))
        var query: Query<org.breezyweather.db.entities.ChineseCityEntity>
        var entityList: List<org.breezyweather.db.entities.ChineseCityEntity>?
        for (c in conditionList) {
            try {
                query = chineseCityEntityBox.query(c).build()
                entityList = query.find()
                query.close()
            } catch (e: Exception) {
                entityList = null
            }
            if (!entityList.isNullOrEmpty()) {
                return entityList[0]
            }
        }
        return null
    }

    fun selectChineseCityEntity(latitude: Float, longitude: Float): org.breezyweather.db.entities.ChineseCityEntity? {
        val query = boxStore.boxFor(org.breezyweather.db.entities.ChineseCityEntity::class.java).query().build()
        val entityList = query.find()
        query.close()
        var minIndex = -1
        var minDistance = Double.MAX_VALUE
        for (i in entityList.indices) {
            val distance = ((latitude - entityList[i].latitude.toDouble()).pow(2.0)
                    + (longitude - entityList[i].longitude.toDouble()).pow(2.0))
            if (distance < minDistance) {
                minIndex = i
                minDistance = distance
            }
        }
        return if (0 <= minIndex && minIndex < entityList.size) entityList[minIndex] else null
    }

    fun selectChineseCityEntityList(name: String?): List<org.breezyweather.db.entities.ChineseCityEntity> {
        if (name.isNullOrEmpty()) return emptyList()
        val chineseCityEntityBox = boxStore.boxFor(
            org.breezyweather.db.entities.ChineseCityEntity::class.java
        )
        val query = chineseCityEntityBox.query(
            ChineseCityEntity_.district.contains(name)
                .or(ChineseCityEntity_.city.contains(name))
                .or(ChineseCityEntity_.province.contains(name))
        ).build()
        val results = query.find()
        query.close()
        return results
    }

    fun countChineseCity(): Int {
        return boxStore.boxFor(org.breezyweather.db.entities.ChineseCityEntity::class.java).count().toInt()
    }
}
