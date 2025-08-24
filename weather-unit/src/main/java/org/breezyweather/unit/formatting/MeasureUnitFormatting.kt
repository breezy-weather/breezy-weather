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

import android.icu.number.LocalizedNumberFormatter
import android.icu.number.NumberFormatter
import android.icu.number.Precision
import android.icu.text.MeasureFormat
import android.icu.text.NumberFormat
import android.icu.util.Measure
import android.icu.util.MeasureUnit
import android.icu.util.TimeUnit
import android.os.Build
import androidx.annotation.RequiresApi
import org.breezyweather.unit.supportsMeasureFormatPerUnit
import org.breezyweather.unit.supportsNumberFormatterUsage
import java.text.FieldPosition
import java.util.Locale

/**
 * @param locale
 * @param value
 * @param perUnit an optional per unit
 * @param numberFormatterWidth
 */
@RequiresApi(Build.VERSION_CODES.R)
fun MeasureUnit.formatWithNumberFormatter(
    locale: Locale,
    value: Number,
    perUnit: MeasureUnit? = null,
    precision: Int,
    numberFormatterWidth: NumberFormatter.UnitWidth,
    showSign: Boolean = false,
): String {
    return (NumberFormatter.withLocale(locale) as LocalizedNumberFormatter)
        .precision(if (precision == 0) Precision.integer() else Precision.maxFraction(precision))
        .sign(if (showSign) NumberFormatter.SignDisplay.ALWAYS else NumberFormatter.SignDisplay.AUTO)
        .unit(this)
        .perUnit(perUnit)
        .unitWidth(numberFormatterWidth)
        .apply {
            if (supportsNumberFormatterUsage()) {
                usage(if (this is TimeUnit) "duration" else null)
            }
        }
        .format(value)
        .toString()
}

/**
 * @param locale
 * @param value
 * @param perUnit an optional per unit. /!\ Only supported on Android SDK >= 26
 */
@RequiresApi(Build.VERSION_CODES.N)
fun MeasureUnit.formatWithMeasureFormat(
    locale: Locale,
    value: Number,
    perUnit: MeasureUnit? = null,
    precision: Int,
    measureFormatWidth: MeasureFormat.FormatWidth,
): String {
    if (perUnit != null && !supportsMeasureFormatPerUnit()) {
        throw UnsupportedOperationException()
    }

    return MeasureFormat
        .getInstance(
            locale,
            measureFormatWidth,
            NumberFormat.getInstance().apply { maximumFractionDigits = precision }
        )
        .let {
            if (perUnit != null) {
                it.formatMeasurePerUnit(
                    Measure(value, this),
                    perUnit,
                    StringBuilder(),
                    FieldPosition(0)
                ).toString()
            } else {
                it.format(
                    Measure(value, this)
                )
            }
        }
}
