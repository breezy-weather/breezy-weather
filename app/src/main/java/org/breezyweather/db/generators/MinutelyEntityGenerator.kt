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

package org.breezyweather.db.generators

import org.breezyweather.common.basic.models.weather.Minutely
import org.breezyweather.db.entities.MinutelyEntity

object MinutelyEntityGenerator {
    fun generate(formattedId: String, minutely: Minutely): MinutelyEntity {
        return MinutelyEntity(
            formattedId = formattedId,
            date = minutely.date,
            minuteInterval = minutely.minuteInterval,
            dbz = minutely.dbz
        )
    }

    fun generate(formattedId: String, minutelyList: List<Minutely>): List<MinutelyEntity> {
        val entityList: MutableList<MinutelyEntity> = ArrayList(minutelyList.size)
        for (minutely in minutelyList) {
            entityList.add(generate(formattedId, minutely))
        }
        return entityList
    }

    fun generate(entity: MinutelyEntity): Minutely {
        return Minutely(
            entity.date,
            entity.minuteInterval,
            entity.dbz
        )
    }

    fun generate(entityList: List<MinutelyEntity>): List<Minutely> {
        val dailyList: MutableList<Minutely> = ArrayList(entityList.size)
        for (entity in entityList) {
            dailyList.add(generate(entity))
        }
        return dailyList
    }

}
