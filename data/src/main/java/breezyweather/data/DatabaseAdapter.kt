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

package breezyweather.data

import app.cash.sqldelight.ColumnAdapter
import breezyweather.domain.weather.reference.AlertSeverity
import breezyweather.domain.weather.reference.WeatherCode
import java.util.Date
import java.util.TimeZone

object DateColumnAdapter : ColumnAdapter<Date, Long> {
    override fun decode(databaseValue: Long): Date = Date(databaseValue)
    override fun encode(value: Date): Long = value.time
}

private const val LIST_OF_STRINGS_SEPARATOR = ", "
object StringListColumnAdapter : ColumnAdapter<List<String>, String> {
    override fun decode(databaseValue: String) = if (databaseValue.isEmpty()) {
        emptyList()
    } else {
        databaseValue.split(LIST_OF_STRINGS_SEPARATOR)
    }
    override fun encode(value: List<String>) = value.joinToString(
        separator = LIST_OF_STRINGS_SEPARATOR
    )
}

object TimeZoneColumnAdapter : ColumnAdapter<TimeZone, String> {
    override fun decode(databaseValue: String): TimeZone = TimeZone.getTimeZone(databaseValue)

    override fun encode(value: TimeZone): String = value.id
}

object WeatherCodeColumnAdapter : ColumnAdapter<WeatherCode, String> {
    override fun decode(databaseValue: String): WeatherCode =
        WeatherCode.getInstance(databaseValue) ?: WeatherCode.CLEAR

    override fun encode(value: WeatherCode): String = value.id
}

object AlertSeverityColumnAdapter : ColumnAdapter<AlertSeverity, Long> {
    override fun decode(databaseValue: Long): AlertSeverity = AlertSeverity.getInstance(databaseValue.toInt())

    override fun encode(value: AlertSeverity): Long = value.id.toLong()
}
