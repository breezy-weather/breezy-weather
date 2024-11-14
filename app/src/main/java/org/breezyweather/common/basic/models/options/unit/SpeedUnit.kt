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
import breezyweather.domain.weather.model.Wind
import org.breezyweather.R
import org.breezyweather.common.basic.models.options.basic.UnitEnum
import org.breezyweather.common.basic.models.options.basic.Utils
import org.breezyweather.common.extensions.isRtl

// actual speed = speed(km/h) * factor.
enum class SpeedUnit(
    override val id: String,
    override val convertUnit: (Double) -> Double,
) : UnitEnum<Double> {

    MPS("mps", { valueInDefaultUnit -> valueInDefaultUnit }),
    KPH("kph", { valueInDefaultUnit -> valueInDefaultUnit.times(3.6) }),
    KN("kn", { valueInDefaultUnit -> valueInDefaultUnit.times(1.94385) }),
    MPH("mph", { valueInDefaultUnit -> valueInDefaultUnit.times(2.23694) }),
    FTPS("ftps", { valueInDefaultUnit -> valueInDefaultUnit.times(3.28084) }),
    BF("bf", { valueInDefaultUnit ->
        when (valueInDefaultUnit) {
            in 0.0..Wind.WIND_SPEED_0 -> 0.0
            in Wind.WIND_SPEED_0..Wind.WIND_SPEED_1 -> 1.0
            in Wind.WIND_SPEED_1..Wind.WIND_SPEED_2 -> 2.0
            in Wind.WIND_SPEED_2..Wind.WIND_SPEED_3 -> 3.0
            in Wind.WIND_SPEED_3..Wind.WIND_SPEED_4 -> 4.0
            in Wind.WIND_SPEED_4..Wind.WIND_SPEED_5 -> 5.0
            in Wind.WIND_SPEED_5..Wind.WIND_SPEED_6 -> 6.0
            in Wind.WIND_SPEED_6..Wind.WIND_SPEED_7 -> 7.0
            in Wind.WIND_SPEED_7..Wind.WIND_SPEED_8 -> 8.0
            in Wind.WIND_SPEED_8..Wind.WIND_SPEED_9 -> 9.0
            in Wind.WIND_SPEED_9..Wind.WIND_SPEED_10 -> 10.0
            in Wind.WIND_SPEED_10..Wind.WIND_SPEED_11 -> 11.0
            in Wind.WIND_SPEED_11..Double.MAX_VALUE -> 12.0
            else -> 0.0
        }
    }),
    ;

    companion object {

        fun getInstance(
            value: String,
        ) = SpeedUnit.entries.firstOrNull {
            it.id == value
        } ?: MPS
    }

    override val valueArrayId = R.array.speed_unit_values
    override val nameArrayId = R.array.speed_units
    override val voiceArrayId = R.array.speed_unit_voices

    override fun getName(context: Context) = Utils.getName(context, this)

    override fun getVoice(context: Context) = Utils.getVoice(context, this)

    override fun getValueWithoutUnit(valueInDefaultUnit: Double) = convertUnit(valueInDefaultUnit)

    override fun getValueTextWithoutUnit(
        valueInDefaultUnit: Double,
    ) = Utils.getValueTextWithoutUnit(this, valueInDefaultUnit, 1)!!

    override fun getValueText(
        context: Context,
        valueInDefaultUnit: Double,
    ) = getValueText(context, valueInDefaultUnit, context.isRtl)

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
        decimalNumber = 1,
        rtl = rtl
    )
}
