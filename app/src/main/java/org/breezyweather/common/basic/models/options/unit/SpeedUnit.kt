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
import org.breezyweather.common.basic.models.weather.Wind
import org.breezyweather.common.basic.models.options._basic.UnitEnum
import org.breezyweather.common.basic.models.options._basic.Utils
import org.breezyweather.common.extensions.isRtl

// actual speed = speed(km/h) * factor.
enum class SpeedUnit(
    override val id: String,
    override val convertUnit: (Float) -> Float
): UnitEnum<Float> {

    MPS("mps", { valueInDefaultUnit -> valueInDefaultUnit }),
    KPH("kph", { valueInDefaultUnit -> valueInDefaultUnit.times(3.6f) }),
    KN("kn", { valueInDefaultUnit -> valueInDefaultUnit.times(1.94385f) }),
    MPH("mph", { valueInDefaultUnit -> valueInDefaultUnit.times(2.23694f) }),
    FTPS("ftps", { valueInDefaultUnit -> valueInDefaultUnit.times(3.28084f) }),
    BF("bf", { valueInDefaultUnit -> when (valueInDefaultUnit) {
        in 0f..Wind.WIND_SPEED_0 -> 0f
        in Wind.WIND_SPEED_0..Wind.WIND_SPEED_1 -> 1f
        in Wind.WIND_SPEED_1..Wind.WIND_SPEED_2 -> 2f
        in Wind.WIND_SPEED_2..Wind.WIND_SPEED_3 -> 3f
        in Wind.WIND_SPEED_3..Wind.WIND_SPEED_4 -> 4f
        in Wind.WIND_SPEED_4..Wind.WIND_SPEED_5 -> 5f
        in Wind.WIND_SPEED_5..Wind.WIND_SPEED_6 -> 6f
        in Wind.WIND_SPEED_6..Wind.WIND_SPEED_7 -> 7f
        in Wind.WIND_SPEED_7..Wind.WIND_SPEED_8 -> 8f
        in Wind.WIND_SPEED_8..Wind.WIND_SPEED_9 -> 9f
        in Wind.WIND_SPEED_9..Wind.WIND_SPEED_10 -> 10f
        in Wind.WIND_SPEED_10..Wind.WIND_SPEED_11 -> 11f
        in Wind.WIND_SPEED_11..Float.MAX_VALUE -> 12f
        else -> 0f
    } });

    companion object {

        fun getInstance(
            value: String
        ) = when (value) {
            "kph" -> KPH
            "kn" -> KN
            "mph" -> MPH
            "ftps" -> FTPS
            "bf" -> BF
            else -> MPS
        }
    }

    override val valueArrayId = R.array.speed_unit_values
    override val nameArrayId = R.array.speed_units
    override val voiceArrayId = R.array.speed_unit_voices

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