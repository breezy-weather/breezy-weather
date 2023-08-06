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

package org.breezyweather.db.converters

import io.objectbox.converter.PropertyConverter
import org.breezyweather.common.basic.models.weather.WeatherCode

class WeatherCodeConverter : PropertyConverter<WeatherCode?, String?> {
    override fun convertToEntityProperty(databaseValue: String?): WeatherCode? =
        if (databaseValue.isNullOrEmpty()) null else WeatherCode.getInstance(databaseValue)

    override fun convertToDatabaseValue(entityProperty: WeatherCode?): String? =
        entityProperty?.id
}
