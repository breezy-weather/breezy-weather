/*
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

package org.breezyweather.domain.weather.model

import android.content.Context
import breezyweather.domain.location.model.Location
import breezyweather.domain.weather.model.Alert
import org.breezyweather.common.extensions.getFormattedFullDayAndMonth
import org.breezyweather.common.extensions.getFormattedMediumDayAndMonth
import org.breezyweather.common.extensions.getFormattedTime
import org.breezyweather.common.extensions.is12Hour

fun Alert.getFormattedDates(
    location: Location,
    context: Context,
    full: Boolean = false,
): String {
    val builder = StringBuilder()
    startDate?.let { startDate ->
        val startDateDay = if (full) {
            startDate.getFormattedFullDayAndMonth(location, context)
        } else {
            startDate.getFormattedMediumDayAndMonth(location, context)
        }
        builder.append(startDateDay)
            .append(context.getString(org.breezyweather.unit.R.string.locale_separator))
            .append(startDate.getFormattedTime(location, context, context.is12Hour))
        endDate?.let { endDate ->
            builder.append(" — ")
            val endDateDay = if (full) {
                startDate.getFormattedFullDayAndMonth(location, context)
            } else {
                endDate.getFormattedMediumDayAndMonth(location, context)
            }
            if (startDateDay != endDateDay) {
                builder.append(endDateDay).append(context.getString(org.breezyweather.unit.R.string.locale_separator))
            }
            builder.append(endDate.getFormattedTime(location, context, context.is12Hour))
        }
    }
    return builder.toString()
}
