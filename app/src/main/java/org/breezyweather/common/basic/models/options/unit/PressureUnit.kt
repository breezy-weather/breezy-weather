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

package org.breezyweather.common.basic.models.options.unit

import android.content.Context
import org.breezyweather.R
import org.breezyweather.common.basic.models.options.basic.UnitEnum
import org.breezyweather.common.basic.models.options.basic.Utils
import org.breezyweather.common.extensions.isRtl

// actual pressure = pressure(mb) * factor.
enum class PressureUnit(
    override val id: String,
    override val convertUnit: (Double) -> Double,
    val chartStep: Double,
    val decimalNumbers: Int = 0,
) : UnitEnum<Double> {

    MB("mb", { valueInDefaultUnit -> valueInDefaultUnit }, chartStep = 15.0),
    KPA("kpa", { valueInDefaultUnit -> valueInDefaultUnit.div(10) }, chartStep = 1.5, 1),
    HPA("hpa", { valueInDefaultUnit -> valueInDefaultUnit }, chartStep = 15.0),
    ATM("atm", { valueInDefaultUnit -> valueInDefaultUnit.div(1013) }, chartStep = 0.015, 3),
    MMHG("mmhg", { valueInDefaultUnit -> valueInDefaultUnit.div(1.333) }, chartStep = 10.0),
    INHG("inhg", { valueInDefaultUnit -> valueInDefaultUnit.div(33.864) }, chartStep = 0.5, 2),
    KGFPSQCM("kgfpsqcm", { valueInDefaultUnit -> valueInDefaultUnit.div(980.7) }, chartStep = 0.015, 3),
    ;

    companion object {

        const val NORMAL = 1013.25

        fun getInstance(
            value: String,
        ) = PressureUnit.entries.firstOrNull {
            it.id == value
        } ?: MB
    }

    override val valueArrayId = R.array.pressure_unit_values
    override val nameArrayId = R.array.pressure_units
    override val voiceArrayId = R.array.pressure_unit_voices

    override fun getName(context: Context) = Utils.getName(context, this)

    override fun getVoice(context: Context) = Utils.getVoice(context, this)

    override fun getValueWithoutUnit(valueInDefaultUnit: Double) = convertUnit(valueInDefaultUnit)

    override fun getValueTextWithoutUnit(
        context: Context,
        valueInDefaultUnit: Double,
    ) = Utils.getValueTextWithoutUnit(context, this, valueInDefaultUnit, decimalNumbers)!!

    override fun getValueText(
        context: Context,
        value: Double,
        isValueInDefaultUnit: Boolean,
    ) = getValueText(context, value, context.isRtl, isValueInDefaultUnit)

    override fun getValueText(
        context: Context,
        value: Double,
        rtl: Boolean,
        isValueInDefaultUnit: Boolean,
    ) = Utils.getValueText(
        context = context,
        enum = this,
        value = value,
        precision = decimalNumbers,
        rtl = rtl,
        isValueInDefaultUnit = isValueInDefaultUnit
    )

    override fun getValueVoice(
        context: Context,
        valueInDefaultUnit: Double,
    ) = getValueVoice(context, valueInDefaultUnit, context.isRtl)

    override fun getValueVoice(
        context: Context,
        valueInDefaultUnit: Double,
        rtl: Boolean,
    ) = Utils.getVoiceText(
        context = context,
        enum = this,
        valueInDefaultUnit = valueInDefaultUnit,
        decimalNumber = decimalNumbers,
        rtl = rtl
    )
}
