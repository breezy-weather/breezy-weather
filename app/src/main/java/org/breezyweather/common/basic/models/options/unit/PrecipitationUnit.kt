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
import org.breezyweather.common.basic.models.options._basic.UnitEnum
import org.breezyweather.common.basic.models.options._basic.Utils
import org.breezyweather.common.extensions.isRtl

// actual precipitation = precipitation(mm) * factor.
enum class PrecipitationUnit(
    override val id: String,
    override val convertUnit: (Double) -> Double,
    val decimals: Int = 1
): UnitEnum<Double> {

    MM("mm", { valueInDefaultUnit -> valueInDefaultUnit }),
    CM("cm", { valueInDefaultUnit -> valueInDefaultUnit.div(10f) }),
    IN("in", { valueInDefaultUnit -> valueInDefaultUnit.div(25.4f) }, 2),
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

    override fun getValueWithoutUnit(valueInDefaultUnit: Double) = convertUnit(valueInDefaultUnit)

    override fun getValueTextWithoutUnit(
        valueInDefaultUnit: Double
    ) = Utils.getValueTextWithoutUnit(this, valueInDefaultUnit, 1)!!

    override fun getValueText(
        context: Context,
        valueInDefaultUnit: Double
    ) = getValueText(context, valueInDefaultUnit, context.isRtl)

    override fun getValueText(
        context: Context,
        valueInDefaultUnit: Double,
        rtl: Boolean
    ) = Utils.getValueText(
        context = context,
        enum = this,
        valueInDefaultUnit = valueInDefaultUnit,
        decimalNumber = decimals,
        rtl = rtl
    )

    override fun getValueVoice(
        context: Context,
        valueInDefaultUnit: Double
    ) = getValueVoice(context, valueInDefaultUnit, context.isRtl)

    override fun getValueVoice(
        context: Context,
        valueInDefaultUnit: Double,
        rtl: Boolean
    ) = Utils.getVoiceText(
        context = context,
        enum = this,
        valueInDefaultUnit = valueInDefaultUnit,
        decimalNumber = decimals,
        rtl = rtl
    )
}

// actual precipitation intensity = precipitation intensity(mm/h) * factor.
enum class PrecipitationIntensityUnit(
    override val id: String,
    override val convertUnit: (Double) -> Double,
    val decimals: Int = 1
): UnitEnum<Double> {

    MMPH("mmph", { valueInDefaultUnit -> valueInDefaultUnit }),
    CMPH("cmph", { valueInDefaultUnit -> valueInDefaultUnit.div(10f) }),
    INPH("inph", { valueInDefaultUnit -> valueInDefaultUnit.div(25.4f) }, 2),
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

    override fun getValueWithoutUnit(valueInDefaultUnit: Double) = convertUnit(valueInDefaultUnit)

    override fun getValueTextWithoutUnit(
        valueInDefaultUnit: Double
    ) = Utils.getValueTextWithoutUnit(this, valueInDefaultUnit, decimals)!!

    override fun getValueText(
        context: Context,
        valueInDefaultUnit: Double
    ) = getValueText(context, valueInDefaultUnit, context.isRtl)

    override fun getValueText(
        context: Context,
        valueInDefaultUnit: Double,
        rtl: Boolean
    ) = Utils.getValueText(
        context = context,
        enum = this,
        valueInDefaultUnit = valueInDefaultUnit,
        decimalNumber = decimals,
        rtl = rtl
    )

    override fun getValueVoice(
        context: Context,
        valueInDefaultUnit: Double
    ) = getValueVoice(context, valueInDefaultUnit, context.isRtl)

    override fun getValueVoice(
        context: Context,
        valueInDefaultUnit: Double,
        rtl: Boolean
    ) = Utils.getVoiceText(
        context = context,
        enum = this,
        valueInDefaultUnit = valueInDefaultUnit,
        decimalNumber = decimals,
        rtl = rtl
    )
}