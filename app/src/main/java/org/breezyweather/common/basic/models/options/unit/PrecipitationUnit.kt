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

package org.breezyweather.common.basic.models.options.unit

import android.content.Context
import org.breezyweather.R
import org.breezyweather.common.basic.models.options._basic.UnitEnum
import org.breezyweather.common.basic.models.options._basic.Utils
import org.breezyweather.common.extensions.isRtl

// actual precipitation = precipitation(mm) * factor.
enum class PrecipitationUnit(
    override val id: String,
    override val convertUnit: (Float) -> Float
): UnitEnum<Float> {

    MM("mm", { valueInDefaultUnit -> valueInDefaultUnit }),
    CM("cm", { valueInDefaultUnit -> valueInDefaultUnit.div(10f) }),
    IN("in", { valueInDefaultUnit -> valueInDefaultUnit.div(25.4f) }),
    LPSQM("lpsqm", { valueInDefaultUnit -> valueInDefaultUnit });

    companion object {

        fun getInstance(
            value: String
        ) = when (value) {
            "cm" -> CM
            "in" -> IN
            "lpsqm" -> LPSQM
            else -> MM
        }
    }

    override val valueArrayId = R.array.precipitation_unit_values
    override val nameArrayId = R.array.precipitation_units
    override val voiceArrayId = R.array.precipitation_unit_voices

    override fun getName(context: Context) = Utils.getName(context, this)

    override fun getVoice(context: Context) = Utils.getVoice(context, this)

    override fun getValueWithoutUnit(valueInDefaultUnit: Float) = convertUnit(valueInDefaultUnit)

    override fun getValueTextWithoutUnit(
        valueInDefaultUnit: Float
    ) = Utils.getValueTextWithoutUnit(this, valueInDefaultUnit, 1)!!

    override fun getValueText(
        context: Context,
        valueInDefaultUnit: Float
    ) = getValueText(context, valueInDefaultUnit, context.isRtl)

    override fun getValueText(
        context: Context,
        valueInDefaultUnit: Float,
        rtl: Boolean
    ) = Utils.getValueText(
        context = context,
        enum = this,
        valueInDefaultUnit = valueInDefaultUnit,
        decimalNumber = 1,
        rtl = rtl
    )

    override fun getValueVoice(
        context: Context,
        valueInDefaultUnit: Float
    ) = getValueVoice(context, valueInDefaultUnit, context.isRtl)

    override fun getValueVoice(
        context: Context,
        valueInDefaultUnit: Float,
        rtl: Boolean
    ) = Utils.getVoiceText(
        context = context,
        enum = this,
        valueInDefaultUnit = valueInDefaultUnit,
        decimalNumber = 1,
        rtl = rtl
    )
}

// actual precipitation intensity = precipitation intensity(mm/h) * factor.
enum class PrecipitationIntensityUnit(
    override val id: String,
    override val convertUnit: (Float) -> Float
): UnitEnum<Float> {

    MMPH("mmph", { valueInDefaultUnit -> valueInDefaultUnit }),
    CMPH("cmph", { valueInDefaultUnit -> valueInDefaultUnit.div(10f) }),
    INPH("inph", { valueInDefaultUnit -> valueInDefaultUnit.div(25.4f) }),
    LPSQMPH("lpsqmph", { valueInDefaultUnit -> valueInDefaultUnit });

    companion object {

        fun getInstance(
            value: String
        ) = when (value) {
            "cmph" -> CMPH
            "inph" -> INPH
            "lpsqmph" -> LPSQMPH
            else -> MMPH
        }
    }

    override val valueArrayId = R.array.precipitation_intensity_unit_values
    override val nameArrayId = R.array.precipitation_intensity_units
    override val voiceArrayId = R.array.precipitation_intensity_unit_voices

    override fun getName(context: Context) = Utils.getName(context, this)

    override fun getVoice(context: Context) = Utils.getVoice(context, this)

    override fun getValueWithoutUnit(valueInDefaultUnit: Float) = convertUnit(valueInDefaultUnit)

    override fun getValueTextWithoutUnit(
        valueInDefaultUnit: Float
    ) = Utils.getValueTextWithoutUnit(this, valueInDefaultUnit, 1)!!

    override fun getValueText(
        context: Context,
        valueInDefaultUnit: Float
    ) = getValueText(context, valueInDefaultUnit, context.isRtl)

    override fun getValueText(
        context: Context,
        valueInDefaultUnit: Float,
        rtl: Boolean
    ) = Utils.getValueText(
        context = context,
        enum = this,
        valueInDefaultUnit = valueInDefaultUnit,
        decimalNumber = 1,
        rtl = rtl
    )

    override fun getValueVoice(
        context: Context,
        valueInDefaultUnit: Float
    ) = getValueVoice(context, valueInDefaultUnit, context.isRtl)

    override fun getValueVoice(
        context: Context,
        valueInDefaultUnit: Float,
        rtl: Boolean
    ) = Utils.getVoiceText(
        context = context,
        enum = this,
        valueInDefaultUnit = valueInDefaultUnit,
        decimalNumber = 1,
        rtl = rtl
    )
}