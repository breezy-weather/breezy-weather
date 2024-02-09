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

package org.breezyweather.db.entities

import io.objectbox.BoxStore
import io.objectbox.annotation.Convert
import io.objectbox.annotation.Entity
import io.objectbox.annotation.Id
import io.objectbox.annotation.Transient
import org.breezyweather.db.converters.TimeZoneConverter
import java.util.*

/**
 * Location entity.
 *
 * [Location].
 */
@Entity
data class LocationEntity(
    @field:Id var id: Long = 0,

    var formattedId: String,
    var cityId: String?,
    var latitude: Float,
    var longitude: Float,
    @field:Convert(
        converter = TimeZoneConverter::class,
        dbType = String::class
    ) var timeZone: TimeZone,
    var country: String,
    var countryCode: String? = null,
    var province: String? = null,
    var provinceCode: String? = null,
    var city: String,
    var district: String? = null,

    // Sources
    var weatherSource: String,
    var airQualitySource: String? = null,
    var pollenSource: String? = null,
    var minutelySource: String? = null,
    var alertSource: String? = null,
    var normalsSource: String? = null,

    var currentPosition: Boolean = false,
    var residentPosition: Boolean = false,

    var needsGeocodeRefresh: Boolean = false
) {
    @Transient
    protected var parametersEntityList: List<LocationParameterEntity>? = null

    /**
     * To-many relationship, resolved on first access (and after reset).
     * Changes to to-many relations are not persisted, make changes to the target entity.
     */
    fun getParameterEntityList(boxStore: BoxStore): List<LocationParameterEntity> {
        if (parametersEntityList == null) {
            val parametersEntityBox = boxStore.boxFor(LocationParameterEntity::class.java)
            val query = parametersEntityBox.query(LocationParameterEntity_.formattedId.equal(formattedId))
                .build()
            val parametersEntityListNew = query.find()
            query.close()
            synchronized(this) {
                if (parametersEntityList == null) {
                    parametersEntityList = parametersEntityListNew
                }
            }
        }
        return parametersEntityList ?: emptyList()
    }
}