package org.breezyweather.db.repositories

import org.breezyweather.common.basic.models.Location
import org.breezyweather.common.basic.models.weather.History
import org.breezyweather.common.extensions.toCalendarWithTimeZone
import org.breezyweather.db.ObjectBox.boxStore
import org.breezyweather.db.entities.HistoryEntity
import org.breezyweather.db.entities.HistoryEntity_
import org.breezyweather.db.generators.HistoryEntityGenerator
import java.util.*

object HistoryEntityRepository {
    // insert.
    fun insertHistoryEntity(entity: HistoryEntity) {
        boxStore.boxFor(HistoryEntity::class.java).put(entity)
    }

    // delete.
    fun deleteLocationHistoryEntity(entityList: List<HistoryEntity>) {
        boxStore.boxFor(HistoryEntity::class.java).remove(entityList)
    }

    // select.
    fun readHistory(location: Location, publishDate: Date): History? {
        return HistoryEntityGenerator.generate(
            selectYesterdayHistoryEntity(
                location.cityId,
                location.weatherSource,
                publishDate,
                location.timeZone
            )
        )
    }

    fun selectYesterdayHistoryEntity(
        cityId: String,
        source: String,
        currentDate: Date,
        timeZone: TimeZone
    ): HistoryEntity? {
        return try {
            val calendar = currentDate.toCalendarWithTimeZone(timeZone)
            val today = calendar.time
            calendar.add(Calendar.DATE, -1)
            val yesterday = calendar.time
            val query = boxStore.boxFor(HistoryEntity::class.java)
                .query(
                    HistoryEntity_.date.greaterOrEqual(yesterday)
                        .and(HistoryEntity_.date.less(today))
                        .and(HistoryEntity_.cityId.equal(cityId))
                        .and(HistoryEntity_.weatherSource.equal(source))
                ).build()
            val entityList = query.find()
            query.close()
            if (entityList.size == 0) null else entityList[0]
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    private fun selectTodayHistoryEntity(
        cityId: String,
        source: String,
        currentDate: Date,
        timeZone: TimeZone
    ): HistoryEntity? {
        return try {
            val calendar = currentDate.toCalendarWithTimeZone(timeZone)
            val today = calendar.time
            calendar.add(Calendar.DATE, 1)
            val tomorrow = calendar.time
            val query = boxStore.boxFor(HistoryEntity::class.java)
                .query(
                    HistoryEntity_.date.greaterOrEqual(today)
                        .and(HistoryEntity_.date.less(tomorrow))
                        .and(HistoryEntity_.cityId.equal(cityId))
                        .and(HistoryEntity_.weatherSource.equal(source))
                ).build()
            val entityList = query.find()
            query.close()
            if (entityList.size == 0) null else entityList[0]
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    fun selectHistoryEntityList(
        cityId: String,
        source: String
    ): List<HistoryEntity> {
        val query = boxStore.boxFor(HistoryEntity::class.java)
            .query(
                HistoryEntity_.cityId.equal(cityId)
                    .and(HistoryEntity_.weatherSource.equal(source))
            ).build()
        val results = query.find()
        query.close()
        return results
    }
}
