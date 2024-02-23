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

// actual distance = distance(km) * factor.
enum class DistanceUnit(
    override val id: String,
    override val convertUnit: (Double) -> Double,
    val decimalNumbers: Int = 0
): UnitEnum<Double> {

    M("m", { valueInDefaultUnit -> valueInDefaultUnit }),
    KM("km", { valueInDefaultUnit -> valueInDefaultUnit.div(1000f) }, 1),
    MI("mi", { valueInDefaultUnit -> valueInDefaultUnit.div(1609.344f) }, 1),
    NMI("nmi", { valueInDefaultUnit -> valueInDefaultUnit.div(1852f) }, 1),
    FT("ft", { valueInDefaultUnit -> valueInDefaultUnit.times(3.28084f) });

    companion object {

        fun getInstance(
            value: String
        ) = when (value) {
            "km" -> KM
            "mi" -> MI
            "nmi" -> NMI
            "ft" -> FT
            else -> M
        }
    }

    override val valueArrayId = R.array.distance_unit_values
    override val nameArrayId = R.array.distance_units
    override val voiceArrayId = R.array.distance_unit_voices

    override fun getName(context: Context) = Utils.getName(context, this)

    override fun getVoice(context: Context) = Utils.getVoice(context, this)

    override fun getValueWithoutUnit(valueInDefaultUnit: Double) = convertUnit(valueInDefaultUnit)

    override fun getValueTextWithoutUnit(
        valueInDefaultUnit: Double
    ) = Utils.getValueTextWithoutUnit(this, valueInDefaultUnit, decimalNumbers)!!

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
        decimalNumber = decimalNumbers,
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
        decimalNumber = decimalNumbers,
        rtl = rtl
    )
}