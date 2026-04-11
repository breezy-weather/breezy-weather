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
import breezyweather.domain.weather.model.DailyCloudCover
import org.breezyweather.R
import org.breezyweather.common.extensions.formatPercent
import org.breezyweather.common.extensions.formatValue
import org.breezyweather.common.extensions.getCloudCoverDescription

fun DailyCloudCover.getRangeSummary(context: Context): String? {
    return if (min == null || max == null) {
        null
    } else if (min == max) {
        max!!.formatPercent(context)
    } else {
        context.getString(
            R.string.cloud_cover_from_to_number,
            min!!.formatValue(context),
            max!!.formatPercent(context)
        )
    }
}

fun DailyCloudCover.getRangeDescriptionSummary(context: Context): String? {
    return if (min == null || max == null) {
        null
    } else {
        val minDescription = min!!.getCloudCoverDescription(context)
        val maxDescription = max!!.getCloudCoverDescription(context)

        if (minDescription == maxDescription) {
            maxDescription
        } else {
            context.getString(
                R.string.cloud_cover_from_to_description,
                minDescription,
                maxDescription
            )
        }
    }
}
