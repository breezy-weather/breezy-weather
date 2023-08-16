/**
 * This file is part of Breezy Weather.
 *
 * Breezy Weather is free software: you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published by the
 * Free Software Foundation, version 3 of the License.
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

import org.breezyweather.db.ObjectBox.boxStore
import org.breezyweather.db.entities.HourlyEntity
import org.breezyweather.db.entities.HourlyEntity_

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
    fun selectHourlyEntityList(formattedId: String): List<HourlyEntity> {
        val query = boxStore.boxFor(HourlyEntity::class.java)
            .query(HourlyEntity_.formattedId.equal(formattedId)).build()
        val results = query.find()
        query.close()
        return results
    }
}
