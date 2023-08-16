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

package org.breezyweather.db.converters

import io.objectbox.converter.PropertyConverter
import java.util.TimeZone

class TimeZoneConverter : PropertyConverter<TimeZone?, String?> {
    override fun convertToEntityProperty(databaseValue: String?): TimeZone? =
        if (databaseValue.isNullOrEmpty()) null else TimeZone.getTimeZone(databaseValue)

    override fun convertToDatabaseValue(entityProperty: TimeZone?): String? =
        entityProperty?.id
}
