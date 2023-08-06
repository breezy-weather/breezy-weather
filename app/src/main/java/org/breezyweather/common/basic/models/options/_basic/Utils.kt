/**
 * This file is part of Breezy Weather.
 *
 * Breezy Weather is free software: you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published by the
 * Free Software Foundation, either version 3 of the License, or (at your
 * option) any later version.
 *
 * Breezy Weather is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public
 * License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Breezy Weather. If not, see <https://www.gnu.org/licenses/>.
 */

package org.breezyweather.common.basic.models.options._basic

import android.content.Context
import android.content.res.Resources
import android.text.BidiFormatter
import androidx.annotation.ArrayRes
import kotlin.math.pow
import kotlin.math.roundToInt

object Utils {

    fun getName(
        context: Context,
        enum: BaseEnum
    ) = getNameByValue(
        res = context.resources,
        value = enum.id,
        nameArrayId = enum.nameArrayId,
        valueArrayId = enum.valueArrayId
    )!!

    fun getVoice(
        context: Context,
        enum: VoiceEnum
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
        @ArrayRes valueArrayId: Int
    ): String? {
        val names = res.getStringArray(nameArrayId)
        val values = res.getStringArray(valueArrayId)
        return getNameByValue(value, names, values)
    }

    private fun getNameByValue(
        value: String,
        names: Array<String>,
        values: Array<String>
    ) = values.zip(names).firstOrNull { it.first == value }?.second

    fun getValueTextWithoutUnit(
        enum: UnitEnum<Float>,
        valueInDefaultUnit: Float,
        decimalNumber: Int
    ) = BidiFormatter
        .getInstance()
        .unicodeWrap(
            formatFloat(
                enum.getValueWithoutUnit(valueInDefaultUnit),
                decimalNumber
            )
        )

    fun getValueTextWithoutUnit(
        enum: UnitEnum<Int>,
        valueInDefaultUnit: Int
    ) = BidiFormatter
        .getInstance()
        .unicodeWrap(
            formatInt(
                enum.getValueWithoutUnit(valueInDefaultUnit),
            )
        )

    fun getValueText(
        context: Context,
        enum: UnitEnum<Float>,
        valueInDefaultUnit: Float,
        decimalNumber: Int,
        rtl: Boolean
    ) = if (rtl) {
        (BidiFormatter
            .getInstance()
            .unicodeWrap(
                formatFloat(
                    enum.getValueWithoutUnit(valueInDefaultUnit),
                    decimalNumber
                )
            )
                + "\u202f"
                + getName(context, enum))
    } else {
        (formatFloat(
            enum.getValueWithoutUnit(valueInDefaultUnit),
            decimalNumber
        )
                + "\u202f"
                + getName(context, enum))
    }

    fun getValueText(
        context: Context,
        enum: UnitEnum<Int>,
        valueInDefaultUnit: Int,
        rtl: Boolean
    ) = if (rtl) {
        (BidiFormatter
            .getInstance()
            .unicodeWrap(
                formatInt(
                    enum.getValueWithoutUnit(valueInDefaultUnit),
                )
            )
                + "\u202f"
                + getName(context, enum))
    } else {
        (formatInt(
            enum.getValueWithoutUnit(valueInDefaultUnit),
        )
                + "\u202f"
                + getName(context, enum))
    }

    fun getVoiceText(
        context: Context,
        enum: UnitEnum<Float>,
        valueInDefaultUnit: Float,
        decimalNumber: Int,
        rtl: Boolean
    ) = if (rtl) {
        (BidiFormatter
            .getInstance()
            .unicodeWrap(
                formatFloat(
                    enum.getValueWithoutUnit(valueInDefaultUnit),
                    decimalNumber
                )
            )
                + "\u202f"
                + getVoice(context, enum))
    } else {
        (formatFloat(
            enum.getValueWithoutUnit(valueInDefaultUnit),
            decimalNumber
        )
                + "\u202f"
                + getVoice(context, enum))
    }

    fun getVoiceText(
        context: Context,
        enum: UnitEnum<Int>,
        valueInDefaultUnit: Int,
        rtl: Boolean
    ) = if (rtl) {
        (BidiFormatter
            .getInstance()
            .unicodeWrap(
                formatInt(
                    enum.getValueWithoutUnit(valueInDefaultUnit),
                )
            )
                + "\u202f"
                + getVoice(context, enum))
    } else {
        (formatInt(
            enum.getValueWithoutUnit(valueInDefaultUnit),
        )
                + "\u202f"
                + getVoice(context, enum))
    }

    fun formatFloat(value: Float, decimalNumber: Int = 2): String {
        val factor = 10.0.pow(decimalNumber.toDouble()).toFloat()
        return if (
            value.roundToInt() * factor == (value * factor).roundToInt().toFloat()
        ) {
            value.roundToInt().toString()
        } else {
            String.format("%." + decimalNumber + "f", value)
        }
    }

    fun formatInt(value: Int) = String.format("%d", value)
}