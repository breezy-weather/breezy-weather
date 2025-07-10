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
import android.content.res.Resources
import android.icu.number.LocalizedNumberFormatter
import android.icu.number.NumberFormatter
import android.icu.number.Precision
import android.icu.text.MeasureFormat
import android.icu.text.NumberFormat
import android.icu.util.Measure
import android.icu.util.MeasureUnit
import android.os.Build
import android.text.BidiFormatter
import androidx.annotation.ArrayRes
import androidx.annotation.RequiresApi
import org.breezyweather.common.extensions.currentLocale
import kotlin.math.pow
import kotlin.math.roundToInt

object Utils {

    fun getName(
        context: Context,
        enum: BaseEnum,
    ) = getNameByValue(
        res = context.resources,
        value = enum.id,
        nameArrayId = enum.nameArrayId,
        valueArrayId = enum.valueArrayId
    )!!

    fun getVoice(
        context: Context,
        enum: VoiceEnum,
    ) = getNameByValue(
        res = context.resources,
        value = enum.id,
        nameArrayId = enum.voiceArrayId,
        valueArrayId = enum.valueArrayId
    )!!

    fun getNameByValue(
        res: Resources,
        value: String,
        @ArrayRes nameArrayId: Int,
        @ArrayRes valueArrayId: Int,
    ): String? {
        val names = res.getStringArray(nameArrayId)
        val values = res.getStringArray(valueArrayId)
        return getNameByValue(value, names, values)
    }

    private fun getNameByValue(
        value: String,
        names: Array<String>,
        values: Array<String>,
    ) = values.zip(names).firstOrNull { it.first == value }?.second

    fun getValueTextWithoutUnit(
        context: Context,
        enum: UnitEnum<Double>,
        valueInDefaultUnit: Double,
        decimalNumber: Int,
    ) = BidiFormatter
        .getInstance()
        .unicodeWrap(
            formatDouble(context, enum.getValueWithoutUnit(valueInDefaultUnit), decimalNumber)
        )

    fun getValueTextWithoutUnit(
        context: Context,
        enum: UnitEnum<Int>,
        valueInDefaultUnit: Int,
    ) = BidiFormatter
        .getInstance()
        .unicodeWrap(
            formatInt(context, enum.getValueWithoutUnit(valueInDefaultUnit))
        )

    fun getValueText(
        context: Context,
        enum: UnitEnum<Double>,
        value: Double,
        precision: Int,
        rtl: Boolean,
        isValueInDefaultUnit: Boolean = true,
    ) = if (rtl) {
        BidiFormatter
            .getInstance()
            .unicodeWrap(
                formatDouble(context, if (isValueInDefaultUnit) enum.getValueWithoutUnit(value) else value, precision)
            ) + "\u202f" + getName(context, enum)
    } else {
        formatDouble(
            context,
            if (isValueInDefaultUnit) enum.getValueWithoutUnit(value) else value,
            precision
        ) + "\u202f" + getName(context, enum)
    }

    fun getValueText(
        context: Context,
        enum: UnitEnum<Int>,
        value: Int,
        rtl: Boolean,
        isValueInDefaultUnit: Boolean = true,
    ) = if (rtl) {
        BidiFormatter
            .getInstance()
            .unicodeWrap(
                formatInt(context, if (isValueInDefaultUnit) enum.getValueWithoutUnit(value) else value)
            ) + "\u202f" + getName(context, enum)
    } else {
        formatInt(context, if (isValueInDefaultUnit) enum.getValueWithoutUnit(value) else value) +
            "\u202f" + getName(context, enum)
    }

    fun getVoiceText(
        context: Context,
        enum: UnitEnum<Double>,
        valueInDefaultUnit: Double,
        decimalNumber: Int,
        rtl: Boolean,
    ) = if (rtl) {
        BidiFormatter
            .getInstance()
            .unicodeWrap(
                formatDouble(
                    context,
                    enum.getValueWithoutUnit(valueInDefaultUnit),
                    decimalNumber
                )
            ) + "\u202f" + getVoice(context, enum)
    } else {
        formatDouble(context, enum.getValueWithoutUnit(valueInDefaultUnit), decimalNumber) +
            "\u202f" +
            getVoice(context, enum)
    }

    fun getVoiceText(
        context: Context,
        enum: UnitEnum<Int>,
        valueInDefaultUnit: Int,
        rtl: Boolean,
    ) = if (rtl) {
        BidiFormatter
            .getInstance()
            .unicodeWrap(
                formatInt(context, enum.getValueWithoutUnit(valueInDefaultUnit))
            ) + "\u202f" + getVoice(context, enum)
    } else {
        formatInt(context, enum.getValueWithoutUnit(valueInDefaultUnit)) + "\u202f" + getVoice(context, enum)
    }

    fun formatDouble(context: Context, value: Double, precision: Int = 2): String {
        val factor = 10.0.pow(precision)
        return if (
            value.roundToInt() * factor == (value * factor).roundToInt().toDouble()
        ) {
            formatNumber(context, value.roundToInt(), 0)
        } else {
            formatNumber(context, value, precision)
        }
    }

    fun formatInt(context: Context, value: Int): String {
        return formatNumber(context, value, 0)
    }

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

    /**
     * Uses LocalizedNumberFormatter on Android SDK >= 30 (which is the recommended way)
     * Uses NumberFormat on Android SDK >= 24
     * Uses String.format() on Android SDK < 24
     */
    fun formatNumber(
        context: Context,
        valueWithoutUnit: Number,
        precision: Int,
    ): String {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            (NumberFormatter.withLocale(context.currentLocale) as LocalizedNumberFormatter)
                .precision(Precision.fixedFraction(precision))
                .format(valueWithoutUnit)
                .toString()
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            NumberFormat.getNumberInstance(context.currentLocale)
                .apply {
                    maximumFractionDigits = precision
                }
                .format(valueWithoutUnit)
                .toString()
        } else {
            if (precision == 0) {
                String.format(context.currentLocale, "%d", valueWithoutUnit)
            } else {
                String.format(context.currentLocale, "%." + precision + "f", valueWithoutUnit)
            }
        }
    }

    /**
     * Uses LocalizedNumberFormatter on Android SDK >= 30 (which is the recommended way)
     * Uses NumberFormat on Android SDK >= 24
     * Uses java.text.NumberFormat on Android SDK < 24
     *
     * @param context
     * @param value between 0.0 and 100.0
     * @param precision
     */
    fun formatPercent(
        context: Context,
        value: Double,
        precision: Int = 0,
    ): String {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            (NumberFormatter.withLocale(context.currentLocale) as LocalizedNumberFormatter)
                .precision(Precision.fixedFraction(precision))
                .unit(MeasureUnit.PERCENT)
                .format(value)
                .toString()
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            NumberFormat.getPercentInstance(context.currentLocale).apply {
                maximumFractionDigits = precision
            }.format(if (value > 0) value.div(100.0) else 0)
        } else {
            java.text.NumberFormat.getPercentInstance(context.currentLocale).apply {
                maximumFractionDigits = precision
            }.format(if (value > 0) value.div(100.0) else 0)
        }
    }
}
