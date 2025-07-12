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
import org.breezyweather.common.basic.models.options.unit.UnitWidth
import org.breezyweather.common.extensions.currentLocale
import org.breezyweather.common.extensions.isRtl
import org.breezyweather.common.utils.helpers.LogHelper
import org.breezyweather.domain.settings.SettingsManager
import java.text.FieldPosition
import kotlin.math.pow
import kotlin.math.roundToInt

object UnitUtils {

    fun getName(
        context: Context,
        enum: BaseEnum,
    ) = getNameByValue(
        res = context.resources,
        value = enum.id,
        nameArrayId = enum.nameArrayId,
        valueArrayId = enum.valueArrayId
    )!!

    fun getMeasureContentDescription(
        context: Context,
        enum: ContentDescriptionEnum,
    ) = getNameByValue(
        res = context.resources,
        value = enum.id,
        nameArrayId = enum.contentDescriptionArrayId,
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

    fun formatValue(
        context: Context,
        enum: UnitEnum<Double>,
        value: Double,
        precision: Int,
        isValueInDefaultUnit: Boolean = true,
    ) = if (precision == 0) {
        formatInt(context, (if (isValueInDefaultUnit) enum.getConvertedUnit(value) else value).roundToInt())
    } else {
        formatDouble(context, if (isValueInDefaultUnit) enum.getConvertedUnit(value) else value, precision)
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

    fun formatMeasure(
        context: Context,
        enum: UnitEnum<Double>,
        value: Double,
        precision: Int,
        isValueInDefaultUnit: Boolean = true,
        unitWidth: UnitWidth = UnitWidth.SHORT,
    ) = if (enum.measureUnit != null &&
        (enum.perMeasureUnit == null || Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) &&
        SettingsManager.getInstance(context).let { it.useNumberFormatter || it.useMeasureFormat }
    ) {
        LogHelper.log(msg = "Formatting with ICU ${enum.id}: ${enum.measureUnit} per ${enum.perMeasureUnit}")
        formatWithIcu(
            context,
            if (isValueInDefaultUnit) enum.getConvertedUnit(value) else value,
            enum.measureUnit!!,
            enum.perMeasureUnit,
            precision,
            unitWidth
        )
    } else {
        LogHelper.log(msg = "Not formatting with ICU ${enum.id} because measureUnit=${enum.measureUnit}")
        formatWithoutIcu(
            context,
            enum,
            formatValue(context, enum, value, precision, isValueInDefaultUnit),
            unitWidth
        )
    }

    /**
     * @param context
     * @param value
     * @param unit
     * @param perUnit an optional per unit. /!\ Only supported on Android SDK >= 26
     * @param unitWidth
     *
     * Uses LocalizedNumberFormatter on Android SDK >= 30 (which is the recommended way)
     * Uses MeasureFormat on Android SDK < 30
     */
    @RequiresApi(api = Build.VERSION_CODES.N)
    fun formatWithIcu(
        context: Context,
        value: Number,
        unit: MeasureUnit,
        perUnit: MeasureUnit? = null,
        precision: Int,
        unitWidth: UnitWidth,
    ): String {
        if (perUnit != null && Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
            throw UnsupportedOperationException()
        }

        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R &&
            SettingsManager.getInstance(context).useNumberFormatter
        ) {
            (NumberFormatter.withLocale(context.currentLocale) as LocalizedNumberFormatter)
                .precision(if (precision == 0) Precision.integer() else Precision.maxFraction(precision))
                .unit(unit)
                .perUnit(perUnit)
                .unitWidth(unitWidth.numberFormatterWidth)
                .format(value)
                .toString()
        } else {
            MeasureFormat
                .getInstance(
                    context.currentLocale,
                    unitWidth.measureFormatWidth,
                    NumberFormat.getInstance().apply { maximumFractionDigits = precision }
                )
                .let {
                    if (perUnit != null) {
                        it.formatMeasurePerUnit(
                            Measure(value, unit),
                            perUnit,
                            StringBuilder(),
                            FieldPosition(0)
                        ).toString()
                    } else {
                        it.format(
                            Measure(value, unit)
                        )
                    }
                }
        }
    }

    fun formatWithoutIcu(
        context: Context,
        enum: UnitEnum<Double>,
        formattedValue: String,
        unitWidth: UnitWidth = UnitWidth.SHORT,
    ) = (if (context.isRtl) BidiFormatter.getInstance().unicodeWrap(formattedValue) else formattedValue) +
        (if (unitWidth != UnitWidth.NARROW) "\u202f" else "") +
        (if (unitWidth != UnitWidth.FULL) getName(context, enum) else getMeasureContentDescription(context, enum))

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
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R &&
            SettingsManager.getInstance(context).useNumberFormatter
        ) {
            (NumberFormatter.withLocale(context.currentLocale) as LocalizedNumberFormatter)
                .precision(if (precision == 0) Precision.integer() else Precision.maxFraction(precision))
                .unit(MeasureUnit.PERCENT)
                .format(value)
                .toString()
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            NumberFormat.getPercentInstance(context.currentLocale)
                .apply { maximumFractionDigits = precision }
                .format(if (value > 0) value.div(100.0) else 0)
        } else {
            java.text.NumberFormat.getPercentInstance(context.currentLocale)
                .apply { maximumFractionDigits = precision }
                .format(if (value > 0) value.div(100.0) else 0)
        }
    }
}
