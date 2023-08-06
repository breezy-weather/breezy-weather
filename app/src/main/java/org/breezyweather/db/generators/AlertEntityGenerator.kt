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

package org.breezyweather.db.generators

import org.breezyweather.common.basic.models.weather.Alert
import org.breezyweather.db.entities.AlertEntity

object AlertEntityGenerator {
    fun generate(formattedId: String, alert: Alert): AlertEntity {
        return AlertEntity(
            formattedId = formattedId,
            alertId = alert.alertId,
            startDate = alert.startDate,
            endDate = alert.endDate,
            description = alert.description,
            content = alert.content,
            priority = alert.priority,
            color = alert.color
        )
    }

    fun generate(formattedId: String, alertList: List<Alert>): List<AlertEntity> {
        val entityList: MutableList<AlertEntity> = ArrayList(alertList.size)
        for (alert in alertList) {
            entityList.add(generate(formattedId, alert))
        }
        return entityList
    }

    fun generate(entity: AlertEntity): Alert {
        return Alert(
            entity.alertId,
            entity.startDate,
            entity.endDate,
            entity.description,
            entity.content,
            entity.priority,
            entity.color
        )
    }

    fun generate(entityList: List<AlertEntity>): List<Alert> {
        val dailyList: MutableList<Alert> = ArrayList(entityList.size)
        for (entity in entityList) {
            dailyList.add(generate(entity))
        }
        return dailyList
    }
}
