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
import breezyweather.domain.weather.model.DailyVisibility
import org.breezyweather.R
import org.breezyweather.common.basic.models.options.unit.getVisibilityDescription
import org.breezyweather.common.extensions.formatMeasure
import org.breezyweather.common.extensions.formatValue
import org.breezyweather.unit.formatting.UnitWidth

fun DailyVisibility.getRangeSummary(context: Context): String? {
    return if (min == null || max == null) {
        null
    } else if (min == max) {
        max!!.formatMeasure(context)
    } else {
        context.getString(
            R.string.visibility_from_to_number,
            min!!.formatValue(context),
            max!!.formatMeasure(context)
        )
    }
}

fun DailyVisibility.getRangeContentDescriptionSummary(context: Context): String? {
    return if (min == null || max == null) {
        null
    } else if (min == max) {
        max!!.formatMeasure(context, unitWidth = UnitWidth.LONG)
    } else {
        context.getString(
            R.string.visibility_from_to_number,
            min!!.formatValue(context),
            max!!.formatMeasure(context, unitWidth = UnitWidth.LONG)
        )
    }
}

fun DailyVisibility.getRangeDescriptionSummary(context: Context): String? {
    return if (min == null || max == null) {
        null
    } else {
        val minDescription = getVisibilityDescription(context, min)
        val maxDescription = getVisibilityDescription(context, max)

        if (minDescription == maxDescription) {
            maxDescription
        } else {
            context.getString(
                R.string.visibility_from_to_description,
                minDescription,
                maxDescription
            )
        }
    }
}
