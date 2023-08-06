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

// actual pressure = pressure(mb) * factor.
enum class PressureUnit(
    override val id: String,
    override val unitFactor: Float
): UnitEnum<Float> {

    MB("mb", 1f),
    KPA("kpa", 0.1f),
    HPA("hpa", 1f),
    ATM("atm", 0.0009869f),
    MMHG("mmhg", 0.75006f),
    INHG("inhg", 0.02953f),
    KGFPSQCM("kgfpsqcm", 0.00102f);

    companion object {

        fun getInstance(
            value: String
        ) = when (value) {
            "kpa" -> KPA
            "hpa" -> HPA
            "atm" -> ATM
            "mmhg" -> MMHG
            "inhg" -> INHG
            "kgfpsqcm" -> KGFPSQCM
            else -> MB
        }
    }

    override val valueArrayId = R.array.pressure_unit_values
    override val nameArrayId = R.array.pressure_units
    override val voiceArrayId = R.array.pressure_unit_voices

    override fun getName(context: Context) = Utils.getName(context, this)

    override fun getVoice(context: Context) = Utils.getVoice(context, this)

    override fun getValueWithoutUnit(valueInDefaultUnit: Float) = valueInDefaultUnit * unitFactor

    override fun getValueInDefaultUnit(valueInCurrentUnit: Float) = valueInCurrentUnit / unitFactor

    override fun getValueTextWithoutUnit(
        valueInDefaultUnit: Float
    ) = Utils.getValueTextWithoutUnit(this, valueInDefaultUnit, 2)!!

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
        decimalNumber = 2,
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
        decimalNumber = 2,
        rtl = rtl
    )
}