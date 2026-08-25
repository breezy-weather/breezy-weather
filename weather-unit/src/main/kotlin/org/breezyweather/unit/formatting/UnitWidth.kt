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

package org.breezyweather.unit.formatting

import android.icu.number.NumberFormatter
import android.icu.text.MeasureFormat
import org.breezyweather.unit.supportsNumberFormatter

enum class UnitWidth(
    val id: String,
    val measureFormatWidth: MeasureFormat.FormatWidth?,
    val numberFormatterWidth: NumberFormatter.UnitWidth?,
) {
    NARROW(
        "narrow",
        MeasureFormat.FormatWidth.NARROW,
        if (supportsNumberFormatter()) NumberFormatter.UnitWidth.NARROW else null
    ),
    SHORT(
        "short",
        MeasureFormat.FormatWidth.SHORT,
        if (supportsNumberFormatter()) NumberFormatter.UnitWidth.SHORT else null
    ),
    LONG(
        "full",
        MeasureFormat.FormatWidth.WIDE,
        if (supportsNumberFormatter()) NumberFormatter.UnitWidth.FULL_NAME else null
    ),
}
