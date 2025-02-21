/**
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

package org.breezyweather.common.basic.models.options.basic

import android.content.Context
import android.icu.number.LocalizedNumberFormatter
import android.icu.number.NumberFormatter
import android.icu.text.MeasureFormat
import android.icu.util.Measure
import android.icu.util.MeasureUnit
import android.os.Build
import androidx.annotation.RequiresApi
import org.breezyweather.common.extensions.currentLocale

interface BaseEnum {
    val id: String
    val nameArrayId: Int
    val valueArrayId: Int
    fun getName(context: Context): String
}

interface VoiceEnum : BaseEnum {
    val voiceArrayId: Int
    fun getVoice(context: Context): String
}

interface UnitEnum<T : Number> : VoiceEnum {
    val convertUnit: (T) -> Double
    fun getValueWithoutUnit(valueInDefaultUnit: T): T
    fun getValueTextWithoutUnit(valueInDefaultUnit: T): String
    fun getValueText(context: Context, valueInDefaultUnit: T): String
    fun getValueText(context: Context, valueInDefaultUnit: T, rtl: Boolean): String
    fun getValueVoice(context: Context, valueInDefaultUnit: T): String
    fun getValueVoice(context: Context, valueInDefaultUnit: T, rtl: Boolean): String

    companion object {
        /**
         * Uses LocalizedNumberFormatter on Android SDK >= 30 (which is the recommended way)
         * Uses MeasureFormat on Android SDK < 30
         */
        @RequiresApi(api = Build.VERSION_CODES.N)
        fun formatWithIcu(
            context: Context,
            valueWithoutUnit: Number,
            unit: MeasureUnit,
            unitWidth: MeasureFormat.FormatWidth,
        ): String {
            return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                (NumberFormatter.withLocale(context.currentLocale) as LocalizedNumberFormatter)
                    .unit(unit)
                    .unitWidth(
                        if (unitWidth == MeasureFormat.FormatWidth.WIDE) {
                            NumberFormatter.UnitWidth.FULL_NAME
                        } else {
                            NumberFormatter.UnitWidth.SHORT
                        }
                    )
                    .format(valueWithoutUnit)
                    .toString()
            } else {
                MeasureFormat
                    .getInstance(context.currentLocale, unitWidth)
                    .format(Measure(valueWithoutUnit, unit))
            }
        }
    }
}
