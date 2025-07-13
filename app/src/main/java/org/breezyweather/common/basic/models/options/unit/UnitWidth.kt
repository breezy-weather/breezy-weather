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

package org.breezyweather.common.basic.models.options.unit

import android.icu.number.NumberFormatter
import android.icu.text.MeasureFormat
import android.os.Build

enum class UnitWidth(
    val id: String,
    val measureFormatWidth: MeasureFormat.FormatWidth?,
    val numberFormatterWidth: NumberFormatter.UnitWidth?,
) {
    NARROW(
        "narrow",
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) MeasureFormat.FormatWidth.NARROW else null,
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) NumberFormatter.UnitWidth.NARROW else null
    ),
    SHORT(
        "short",
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) MeasureFormat.FormatWidth.SHORT else null,
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) NumberFormatter.UnitWidth.SHORT else null
    ),
    FULL(
        "full",
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) MeasureFormat.FormatWidth.WIDE else null,
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) NumberFormatter.UnitWidth.FULL_NAME else null
    ),
}
