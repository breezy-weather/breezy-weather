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
import android.text.BidiFormatter
import org.breezyweather.R
import org.breezyweather.common.basic.models.options._basic.UnitEnum
import org.breezyweather.common.basic.models.options._basic.Utils
import org.breezyweather.common.extensions.isRtl

enum class TemperatureUnit(
    override val id: String,
    override val convertUnit: (Float) -> Float,
    val convertDegreeDayUnit: (Float) -> Float
): UnitEnum<Float> {

    C("c", { valueInDefaultUnit -> valueInDefaultUnit }, { valueInDefaultUnit -> valueInDefaultUnit }),
    F("f", { valueInDefaultUnit -> 32 + valueInDefaultUnit.times(1.8f) }, { valueInDefaultUnit -> valueInDefaultUnit.times(1.8f) }),
    K("k", { valueInDefaultUnit -> 273.15f + valueInDefaultUnit }, { valueInDefaultUnit -> valueInDefaultUnit });

    companion object {
        fun getInstance(
            value: String
        ) = when (value) {
            "f" -> F
            "k" -> K
            else -> C
        }
    }

    override val valueArrayId = R.array.temperature_unit_values
    override val nameArrayId = R.array.temperature_units
    private val shortArrayId = R.array.temperature_units_short
    override val voiceArrayId = R.array.temperature_units

    override fun getName(context: Context) = Utils.getName(context, this)

    fun getShortName(
        context: Context
    ) = Utils.getNameByValue(
        res = context.resources,
        value = id,
        nameArrayId = shortArrayId,
        valueArrayId = valueArrayId
    )!!

    override fun getValueTextWithoutUnit(
        valueInDefaultUnit: Float
    ) = Utils.getValueTextWithoutUnit(this, valueInDefaultUnit, 0)!!

    override fun getVoice(context: Context) = Utils.getVoice(context, this)

    override fun getValueWithoutUnit(valueInDefaultUnit: Float) = convertUnit(valueInDefaultUnit)

    fun getDegreeDayValueWithoutUnit(valueInDefaultUnit: Float) = convertDegreeDayUnit(valueInDefaultUnit)

    override fun getValueText(
        context: Context,
        valueInDefaultUnit: Float,
    ) = getValueText(context, valueInDefaultUnit, context.isRtl)

    fun getValueText(
        context: Context,
        valueInDefaultUnit: Float,
        decimalNumber: Int = 1
    ) = Utils.getValueText(
        context = context,
        enum = this,
        valueInDefaultUnit = valueInDefaultUnit,
        decimalNumber = decimalNumber,
        rtl = context.isRtl
    )

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

    fun getDegreeDayValueText(
        context: Context,
        valueInDefaultUnit: Float,
    ) = if (context.isRtl) {
        (BidiFormatter
            .getInstance()
            .unicodeWrap(
                Utils.formatFloat(getDegreeDayValueWithoutUnit(valueInDefaultUnit), 1)
            )
                + "\u202f"
                + Utils.getName(context, this))
    } else {
        (Utils.formatFloat(getDegreeDayValueWithoutUnit(valueInDefaultUnit), 1)
                + "\u202f"
                + Utils.getName(context, this))
    }

    fun getDegreeDayValueText(
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

    fun getShortValueText(
        context: Context,
        valueInDefaultUnit: Float
    ) = getShortValueText(context, valueInDefaultUnit, 0, context.isRtl)

    fun getShortValueText(
        context: Context,
        valueInDefaultUnit: Float,
        decimalNumber: Int,
        rtl: Boolean
    ) = if (rtl) {
        (BidiFormatter
            .getInstance()
            .unicodeWrap(
                Utils.formatFloat(
                    getValueWithoutUnit(valueInDefaultUnit),
                    decimalNumber
                )
            )
                + getShortName(context))
    } else {
        (Utils.formatFloat(
            getValueWithoutUnit(valueInDefaultUnit),
            decimalNumber
        )
                + getShortName(context))
    }

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
        decimalNumber = 0,
        rtl = rtl
    )
}