/**
 * This file is part of Breezy Weather.
 *
 * Breezy Weather is free software: you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published by the
 * Free Software Foundation, either version 3 of the License, or (at your
 * option) any later version.
 *
 * Breezy Weather is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public
 * License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Breezy Weather. If not, see <https://www.gnu.org/licenses/>.
 */

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
                location.formattedId,
                publishDate,
                location.timeZone
            )
        )
    }

    fun selectYesterdayHistoryEntity(
        formattedId: String,
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
                        .and(HistoryEntity_.formattedId.equal(formattedId))
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
        formattedId: String,
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
                        .and(HistoryEntity_.formattedId.equal(formattedId))
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
        formattedId: String
    ): List<HistoryEntity> {
        val query = boxStore.boxFor(HistoryEntity::class.java)
            .query(HistoryEntity_.formattedId.equal(formattedId)).build()
        val results = query.find()
        query.close()
        return results
    }
}
