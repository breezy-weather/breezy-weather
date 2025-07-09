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
import android.graphics.Color
import androidx.annotation.ColorInt
import breezyweather.domain.weather.model.Wind
import org.breezyweather.R
import org.breezyweather.common.basic.models.options.basic.UnitEnum
import org.breezyweather.common.basic.models.options.basic.Utils
import org.breezyweather.common.extensions.isRtl

// actual speed = speed(km/h) * factor.
enum class SpeedUnit(
    override val id: String,
    override val convertUnit: (Double) -> Double,
    val chartStep: Double,
) : UnitEnum<Double> {

    MPS("mps", { valueInDefaultUnit -> valueInDefaultUnit }, 5.0),
    KPH("kph", { valueInDefaultUnit -> valueInDefaultUnit.times(3.6) }, 15.0),
    KN("kn", { valueInDefaultUnit -> valueInDefaultUnit.times(1.94385) }, 10.0),
    MPH("mph", { valueInDefaultUnit -> valueInDefaultUnit.times(2.23694) }, 10.0),
    FTPS("ftps", { valueInDefaultUnit -> valueInDefaultUnit.times(3.28084) }, 15.0),
    BF(
        "bf",
        { valueInDefaultUnit ->
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
        },
        2.0
    ),
    ;

    companion object {
        val beaufortScaleThresholds = listOf(
            0.0,
            Wind.WIND_SPEED_0,
            Wind.WIND_SPEED_1,
            Wind.WIND_SPEED_2,
            Wind.WIND_SPEED_3,
            Wind.WIND_SPEED_4,
            Wind.WIND_SPEED_5,
            Wind.WIND_SPEED_6,
            Wind.WIND_SPEED_7,
            Wind.WIND_SPEED_8,
            Wind.WIND_SPEED_9,
            Wind.WIND_SPEED_10,
            Wind.WIND_SPEED_11
        )

        fun getBeaufortScaleStrength(
            context: Context,
            windSpeedInDefaultUnit: Double?,
        ): String? {
            if (windSpeedInDefaultUnit == null) return null
            return when (windSpeedInDefaultUnit) {
                in 0.0..Wind.WIND_SPEED_0 -> context.getString(R.string.wind_strength_0)
                in Wind.WIND_SPEED_0..Wind.WIND_SPEED_1 -> context.getString(R.string.wind_strength_1)
                in Wind.WIND_SPEED_1..Wind.WIND_SPEED_2 -> context.getString(R.string.wind_strength_2)
                in Wind.WIND_SPEED_2..Wind.WIND_SPEED_3 -> context.getString(R.string.wind_strength_3)
                in Wind.WIND_SPEED_3..Wind.WIND_SPEED_4 -> context.getString(R.string.wind_strength_4)
                in Wind.WIND_SPEED_4..Wind.WIND_SPEED_5 -> context.getString(R.string.wind_strength_5)
                in Wind.WIND_SPEED_5..Wind.WIND_SPEED_6 -> context.getString(R.string.wind_strength_6)
                in Wind.WIND_SPEED_6..Wind.WIND_SPEED_7 -> context.getString(R.string.wind_strength_7)
                in Wind.WIND_SPEED_7..Wind.WIND_SPEED_8 -> context.getString(R.string.wind_strength_8)
                in Wind.WIND_SPEED_8..Wind.WIND_SPEED_9 -> context.getString(R.string.wind_strength_9)
                in Wind.WIND_SPEED_9..Wind.WIND_SPEED_10 -> context.getString(R.string.wind_strength_10)
                in Wind.WIND_SPEED_10..Wind.WIND_SPEED_11 -> context.getString(R.string.wind_strength_11)
                in Wind.WIND_SPEED_11..Double.MAX_VALUE -> context.getString(R.string.wind_strength_12)
                else -> null
            }
        }

        val colorsArrayId = R.array.wind_strength_colors

        @ColorInt
        fun getBeaufortScaleColor(context: Context, bf: Int): Int {
            return context.resources.getIntArray(colorsArrayId).getOrNull(bf) ?: Color.TRANSPARENT
        }

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
        context: Context,
        valueInDefaultUnit: Double,
    ) = Utils.getValueTextWithoutUnit(context, this, valueInDefaultUnit, 1)!!

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
        precision = 1,
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
        decimalNumber = 1,
        rtl = rtl
    )
}
