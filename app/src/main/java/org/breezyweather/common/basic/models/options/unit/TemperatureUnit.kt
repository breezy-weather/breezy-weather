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
import android.icu.text.MeasureFormat
import android.icu.util.MeasureUnit
import android.os.Build
import android.text.BidiFormatter
import org.breezyweather.R
import org.breezyweather.common.basic.models.options.basic.UnitEnum
import org.breezyweather.common.basic.models.options.basic.Utils
import org.breezyweather.common.extensions.isRtl
import org.breezyweather.common.extensions.roundDecimals

enum class TemperatureUnit(
    override val id: String,
    override val convertUnit: (Double) -> Double,
    val convertDegreeDayUnit: (Double) -> Double,
) : UnitEnum<Double> {

    C("c", { valueInDefaultUnit -> valueInDefaultUnit }, { valueInDefaultUnit -> valueInDefaultUnit }),
    F("f", { valueInDefaultUnit ->
        32 + valueInDefaultUnit.times(1.8)
    }, { valueInDefaultUnit -> valueInDefaultUnit.times(1.8) }),
    K("k", { valueInDefaultUnit -> 273.15 + valueInDefaultUnit }, { valueInDefaultUnit -> valueInDefaultUnit }),
    ;

    companion object {
        fun getInstance(
            value: String,
        ) = TemperatureUnit.entries.firstOrNull {
            it.id == value
        } ?: C
    }

    override val valueArrayId = R.array.temperature_unit_values
    override val nameArrayId = R.array.temperature_units
    private val shortArrayId = R.array.temperature_units_short
    override val voiceArrayId = R.array.temperature_units

    override fun getName(context: Context) = Utils.getName(context, this)

    fun getShortName(
        context: Context,
    ) = Utils.getNameByValue(
        res = context.resources,
        value = id,
        nameArrayId = shortArrayId,
        valueArrayId = valueArrayId
    )!!

    override fun getValueTextWithoutUnit(
        valueInDefaultUnit: Double,
    ) = Utils.getValueTextWithoutUnit(this, valueInDefaultUnit, 0)!!

    override fun getVoice(context: Context) = Utils.getVoice(context, this)

    override fun getValueWithoutUnit(valueInDefaultUnit: Double) = convertUnit(valueInDefaultUnit)

    fun getDegreeDayValueWithoutUnit(valueInDefaultUnit: Double) = convertDegreeDayUnit(valueInDefaultUnit)

    override fun getValueText(
        context: Context,
        valueInDefaultUnit: Double,
    ) = getValueText(context, valueInDefaultUnit, context.isRtl)

    fun getValueText(
        context: Context,
        valueInDefaultUnit: Double,
        decimalNumber: Int = 1,
    ) = Utils.getValueText(
        context = context,
        enum = this,
        valueInDefaultUnit = valueInDefaultUnit,
        decimalNumber = decimalNumber,
        rtl = context.isRtl
    )

    override fun getValueText(
        context: Context,
        valueInDefaultUnit: Double,
        rtl: Boolean,
    ) = Utils.getValueText(
        context = context,
        enum = this,
        valueInDefaultUnit = valueInDefaultUnit,
        decimalNumber = 1,
        rtl = rtl
    )

    fun getDegreeDayValueText(
        context: Context,
        valueInDefaultUnit: Double,
    ) = if (context.isRtl) {
        BidiFormatter
            .getInstance()
            .unicodeWrap(
                Utils.formatDouble(getDegreeDayValueWithoutUnit(valueInDefaultUnit), 1)
            ) + "\u202f" + Utils.getName(context, this)
    } else {
        Utils.formatDouble(
            getDegreeDayValueWithoutUnit(valueInDefaultUnit),
            1
        ) + "\u202f" + Utils.getName(context, this)
    }

    fun getDegreeDayValueText(
        context: Context,
        valueInDefaultUnit: Double,
        rtl: Boolean,
    ) = Utils.getValueText(
        context = context,
        enum = this,
        valueInDefaultUnit = valueInDefaultUnit,
        decimalNumber = 1,
        rtl = rtl
    )

    fun getDegreeDayValueVoice(
        context: Context,
        valueInDefaultUnit: Double,
    ) = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
        UnitEnum.formatWithIcu(
            context,
            getDegreeDayValueWithoutUnit(valueInDefaultUnit).roundDecimals(0)!!,
            when (this) {
                C -> MeasureUnit.CELSIUS
                F -> MeasureUnit.FAHRENHEIT
                K -> MeasureUnit.KELVIN
            },
            MeasureFormat.FormatWidth.WIDE
        )
    } else {
        getDegreeDayValueText(context, valueInDefaultUnit)
    }

    fun getShortValueText(
        context: Context,
        valueInDefaultUnit: Double,
    ) = getShortValueText(context, valueInDefaultUnit, 0, context.isRtl)

    fun getShortValueText(
        context: Context,
        valueInDefaultUnit: Double,
        decimalNumber: Int,
        rtl: Boolean,
    ) = if (rtl) {
        BidiFormatter
            .getInstance()
            .unicodeWrap(
                Utils.formatDouble(
                    getValueWithoutUnit(valueInDefaultUnit),
                    decimalNumber
                )
            ) + getShortName(context)
    } else {
        Utils.formatDouble(
            getValueWithoutUnit(valueInDefaultUnit),
            decimalNumber
        ) + getShortName(context)
    }

    override fun getValueVoice(
        context: Context,
        valueInDefaultUnit: Double,
    ) = getValueVoice(context, valueInDefaultUnit, context.isRtl)

    override fun getValueVoice(
        context: Context,
        valueInDefaultUnit: Double,
        rtl: Boolean,
    ) = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
        UnitEnum.formatWithIcu(
            context,
            getValueWithoutUnit(valueInDefaultUnit).roundDecimals(0)!!,
            when (this) {
                C -> MeasureUnit.CELSIUS
                F -> MeasureUnit.FAHRENHEIT
                K -> MeasureUnit.KELVIN
            },
            MeasureFormat.FormatWidth.WIDE
        )
    } else {
        Utils.getVoiceText(
            context = context,
            enum = this,
            valueInDefaultUnit = valueInDefaultUnit,
            decimalNumber = 0,
            rtl = rtl
        )
    }
}
