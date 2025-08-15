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
import breezyweather.domain.weather.model.DailyDewPoint
import org.breezyweather.R
import org.breezyweather.common.extensions.formatMeasure
import org.breezyweather.common.extensions.formatValue
import org.breezyweather.unit.formatting.UnitWidth

fun DailyDewPoint.getRangeSummary(context: Context): String? {
    return if (min == null || max == null) {
        null
    } else if (min == max) {
        max!!.formatMeasure(context, unitWidth = UnitWidth.NARROW)
    } else {
        context.getString(
            R.string.dew_point_from_to_number,
            min!!.formatValue(context),
            max!!.formatMeasure(context, unitWidth = UnitWidth.NARROW)
        )
    }
}

fun DailyDewPoint.getRangeContentDescriptionSummary(context: Context): String? {
    return if (min == null || max == null) {
        null
    } else if (min == max) {
        max!!.formatMeasure(context, unitWidth = UnitWidth.LONG)
    } else {
        context.getString(
            R.string.dew_point_from_to_number,
            min!!.formatValue(context),
            max!!.formatMeasure(context, unitWidth = UnitWidth.LONG)
        )
    }
}
