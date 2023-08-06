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

package org.breezyweather.db.entities

import io.objectbox.annotation.Entity
import io.objectbox.annotation.Id
import java.util.*

/**
 * Minutely entity.
 *
 * [Minutely].
 */
@Entity
data class MinutelyEntity(
    @field:Id var id: Long = 0,
    var formattedId: String,
    var date: Date,
    var minuteInterval: Int,
    var dbz: Int? = null
)
