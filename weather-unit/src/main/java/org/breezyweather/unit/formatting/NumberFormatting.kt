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
import android.icu.text.NumberFormat
import android.os.Build
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.Locale

/**
 * Uses LocalizedNumberFormatter on Android SDK >= 30 (which is the recommended way)
 * Uses NumberFormat on Android SDK >= 24
 * Uses String.format() on Android SDK < 24
 */
fun Number.format(
    decimals: Int,
    locale: Locale = Locale.getDefault(),
    showSign: Boolean = false,
    useNumberFormatter: Boolean = true,
    useMeasureFormat: Boolean = true,
): String {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R && useNumberFormatter) {
        (NumberFormatter.withLocale(locale) as LocalizedNumberFormatter)
            .precision(Precision.fixedFraction(decimals))
            .sign(if (showSign) NumberFormatter.SignDisplay.ALWAYS else NumberFormatter.SignDisplay.AUTO)
            .format(this)
            .toString()
    } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N && useMeasureFormat && !showSign) {
        // showSign not supported by NumberFormat, skip
        NumberFormat.getNumberInstance(locale)
            .apply { maximumFractionDigits = decimals }
            .format(this)
            .toString()
    } else {
        return DecimalFormat(if (showSign) "+0" else "0", DecimalFormatSymbols.getInstance(locale))
            .apply { setMaximumFractionDigits(decimals) }
            .format(this)
    }
}
