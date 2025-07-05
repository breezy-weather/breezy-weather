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
import android.text.BidiFormatter
import androidx.annotation.ArrayRes
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
            UnitEnum.formatNumber(context, value.roundToInt(), 0)
        } else {
            UnitEnum.formatNumber(context, value, precision)
        }
    }

    fun formatInt(context: Context, value: Int): String {
        return UnitEnum.formatNumber(context, value, 0)
    }
}
