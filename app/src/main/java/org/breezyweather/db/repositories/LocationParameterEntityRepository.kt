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

import org.breezyweather.db.ObjectBox
import org.breezyweather.db.entities.LocationParameterEntity
import org.breezyweather.db.entities.LocationParameterEntity_
import org.breezyweather.db.entities.WeatherEntity
import org.breezyweather.db.generators.LocationParameterEntityGenerator

object LocationParameterEntityRepository {
    fun updateFormattedId(oldFormattedId: String, formattedId: String) {
        val parametersWithOldId = selectLocationParameterListEntity(oldFormattedId)
        parametersWithOldId.forEach {
            it.formattedId = formattedId
        }
        updateLocationParameterListEntity(parametersWithOldId)
    }

    // update.
    fun updateLocationParameterListEntity(entityList: List<LocationParameterEntity>) {
        ObjectBox.boxStore.boxFor(LocationParameterEntity::class.java).put(entityList)
    }

    fun selectLocationParameterListEntity(formattedId: String): List<LocationParameterEntity> {
        val query = ObjectBox.boxStore.boxFor(LocationParameterEntity::class.java)
            .query(LocationParameterEntity_.formattedId.equal(formattedId))
            .build()
        val entityList = query.find()
        query.close()
        return entityList
    }

    fun writeLocationParameters(formattedId: String, updatedList: Map<String, Map<String, String>>) {
        ObjectBox.boxStore.callInTxNoException {
            deleteLocationParameters(formattedId)
            updateLocationParameterListEntity(
                LocationParameterEntityGenerator.generate(formattedId, updatedList)
            )
        }
    }

    fun deleteLocationParameters(formattedId: String) {
        ObjectBox.boxStore.boxFor(LocationParameterEntity::class.java).remove(
            selectLocationParameterListEntity(formattedId)
        )
    }
}
