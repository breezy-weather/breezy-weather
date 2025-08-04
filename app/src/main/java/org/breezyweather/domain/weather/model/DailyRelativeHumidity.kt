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
import breezyweather.domain.weather.model.DailyRelativeHumidity
import org.breezyweather.R
import org.breezyweather.common.basic.models.options.basic.UnitUtils

fun DailyRelativeHumidity.getRangeSummary(context: Context): String? {
    return if (min == null || max == null) {
        null
    } else if (min == max) {
        UnitUtils.formatPercent(context, max!!)
    } else {
        context.getString(
            R.string.humidity_from_to_number,
            UnitUtils.formatDouble(context, min!!),
            UnitUtils.formatPercent(context, max!!)
        )
    }
}
