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
import android.icu.util.MeasureUnit
import android.icu.util.TimeUnit
import android.os.Build
import androidx.annotation.ColorInt
import breezyweather.domain.weather.model.Wind
import org.breezyweather.R
import org.breezyweather.common.basic.models.options.basic.UnitEnum
import org.breezyweather.common.basic.models.options.basic.UnitUtils
import org.breezyweather.common.extensions.currentLocale

// actual speed = speed(km/h) * factor.
enum class SpeedUnit(
    override val id: String,
    override val measureUnit: MeasureUnit?,
    override val perMeasureUnit: TimeUnit?,
    override val convertUnit: (Double) -> Double,
    val chartStep: Double,
) : UnitEnum<Double> {

    METER_PER_SECOND(
        "mps",
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) MeasureUnit.METER_PER_SECOND else null,
        null,
        { valueInDefaultUnit -> valueInDefaultUnit },
        5.0
    ),
    KILOMETER_PER_HOUR(
        "kph",
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) MeasureUnit.KILOMETER_PER_HOUR else null,
        null,
        { valueInDefaultUnit -> valueInDefaultUnit.times(3.6) },
        15.0
    ),
    KNOT(
        "kn",
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) MeasureUnit.KNOT else null,
        null,
        { valueInDefaultUnit -> valueInDefaultUnit.times(1.94385) },
        10.0
    ),
    MILE_PER_HOUR(
        "mph",
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) MeasureUnit.MILE_PER_HOUR else null,
        null,
        { valueInDefaultUnit -> valueInDefaultUnit.times(2.23694) },
        10.0
    ),
    FOOT_PER_SECOND(
        "ftps",
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) MeasureUnit.FOOT else null,
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) MeasureUnit.SECOND else null,
        { valueInDefaultUnit -> valueInDefaultUnit.times(3.28084) },
        15.0
    ),
    BEAUFORT(
        "bf",
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.BAKLAVA) MeasureUnit.BEAUFORT else null,
        null,
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

        /**
         * Copyright © 1991-Present Unicode, Inc.
         * License: Unicode License v3 https://www.unicode.org/license.txt
         * Source: https://github.com/unicode-org/cldr/blob/3f3967f3cbadc56bbb44a9aed20784e82ac64c67/common/supplemental/units.xml#L570-L574
         */
        fun getDefaultUnit(
            context: Context,
        ) = when (context.currentLocale.country) {
            "CN", "DK", "FI", "JP", "KR", "NO", "PL", "RU", "SE" -> METER_PER_SECOND
            "GB", "US" -> MILE_PER_HOUR
            else -> KILOMETER_PER_HOUR
        }
    }

    override val valueArrayId = R.array.speed_unit_values
    override val nameArrayId = R.array.speed_units
    override val contentDescriptionArrayId = R.array.speed_unit_voices

    override fun getName(context: Context) = UnitUtils.getName(context, this)

    override fun getMeasureContentDescription(context: Context) = UnitUtils.getMeasureContentDescription(context, this)

    override fun getConvertedUnit(valueInDefaultUnit: Double) = convertUnit(valueInDefaultUnit)

    override fun formatValue(
        context: Context,
        valueInDefaultUnit: Double,
    ) = UnitUtils.formatValue(
        context = context,
        enum = this,
        value = valueInDefaultUnit,
        precision = 1
    )

    override fun formatMeasure(
        context: Context,
        value: Double,
        isValueInDefaultUnit: Boolean,
    ) = UnitUtils.formatMeasure(
        context = context,
        enum = this,
        value = value,
        precision = 1,
        isValueInDefaultUnit = isValueInDefaultUnit
    )

    override fun formatContentDescription(
        context: Context,
        value: Double,
        isValueInDefaultUnit: Boolean,
    ) = UnitUtils.formatMeasure(
        context = context,
        enum = this,
        value = value,
        precision = 1,
        isValueInDefaultUnit = isValueInDefaultUnit,
        unitWidth = UnitWidth.FULL
    )
}
