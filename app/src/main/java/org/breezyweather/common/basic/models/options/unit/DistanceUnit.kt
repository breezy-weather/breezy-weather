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
import android.icu.util.MeasureUnit
import android.os.Build
import org.breezyweather.R
import org.breezyweather.common.basic.models.options.basic.UnitEnum
import org.breezyweather.common.basic.models.options.basic.UnitUtils
import org.breezyweather.common.extensions.currentLocale

// actual distance = distance(km) * factor.
enum class DistanceUnit(
    override val id: String,
    override val measureUnit: MeasureUnit?,
    override val convertUnit: (Double) -> Double,
    val chartStep: (Double) -> Double,
    val precision: Int = 0,
    override val perMeasureUnit: MeasureUnit? = null,
) : UnitEnum<Double> {

    METER(
        "m",
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) MeasureUnit.METER else null,
        { valueInDefaultUnit -> valueInDefaultUnit },
        chartStep = { maxY -> if (maxY < 40000) 5000.0 else 10000.0 }
    ),
    KILOMETER(
        "km",
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) MeasureUnit.KILOMETER else null,
        { valueInDefaultUnit -> valueInDefaultUnit.div(1000f) },
        chartStep = { maxY -> if (maxY < 4) 5.0 else 10.0 },
        precision = 1
    ),
    MILE(
        "mi",
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) MeasureUnit.MILE else null,
        { valueInDefaultUnit -> valueInDefaultUnit.div(1609.344f) },
        chartStep = { maxY ->
            with(maxY) {
                when {
                    this <= 15.0 -> 3.0
                    this in 15.0..30.0 -> 5.0
                    else -> 30.0
                }
            }
        },
        precision = 1
    ),
    NAUTICAL_MILE(
        "nmi",
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) MeasureUnit.NAUTICAL_MILE else null,
        { valueInDefaultUnit -> valueInDefaultUnit.div(1852f) },
        chartStep = { maxY ->
            with(maxY) {
                when {
                    this <= 15.0 -> 3.0
                    this in 15.0..30.0 -> 5.0
                    else -> 30.0
                }
            }
        },
        precision = 1
    ),
    FOOT(
        "ft",
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) MeasureUnit.FOOT else null,
        { valueInDefaultUnit -> valueInDefaultUnit.times(3.28084f) },
        chartStep = { maxY -> if (maxY < 150000) 25000.0 else 50000.0 }
    ),
    ;

    companion object {

        /**
         * Copyright © 1991-Present Unicode, Inc.
         * License: Unicode License v3 https://www.unicode.org/license.txt
         * Source (simplified): https://github.com/unicode-org/cldr/blob/3f3967f3cbadc56bbb44a9aed20784e82ac64c67/common/supplemental/units.xml#L506-L512
         */
        fun getDefaultUnit(
            context: Context,
        ) = when (context.currentLocale.country) {
            "DE", "ML" -> METER
            "GB", "US" -> MILE
            else -> KILOMETER
        }

        /**
         * Source: https://weather.metoffice.gov.uk/guides/what-does-this-forecast-mean
         */
        const val VISIBILITY_VERY_POOR = 1000.0
        const val VISIBILITY_POOR = 4000.0
        const val VISIBILITY_MODERATE = 10000.0
        const val VISIBILITY_GOOD = 20000.0
        const val VISIBILITY_CLEAR = 40000.0

        /**
         * @param context
         * @param visibility in meters (default [DistanceUnit] unit)
         */
        fun getVisibilityDescription(context: Context, visibility: Double?): String? {
            if (visibility == null) return null
            return when (visibility) {
                in 0.0..<VISIBILITY_VERY_POOR -> context.getString(R.string.visibility_very_poor)
                in VISIBILITY_VERY_POOR..<VISIBILITY_POOR -> context.getString(R.string.visibility_poor)
                in VISIBILITY_POOR..<VISIBILITY_MODERATE -> context.getString(R.string.visibility_moderate)
                in VISIBILITY_MODERATE..<VISIBILITY_GOOD -> context.getString(R.string.visibility_good)
                in VISIBILITY_GOOD..<VISIBILITY_CLEAR -> context.getString(R.string.visibility_clear)
                in VISIBILITY_CLEAR..Double.MAX_VALUE -> context.getString(R.string.visibility_perfectly_clear)
                else -> null
            }
        }
    }

    override val valueArrayId = R.array.distance_unit_values
    override val nameArrayId = R.array.distance_units
    override val contentDescriptionArrayId = R.array.distance_unit_voices

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
        precision = precision
    )

    override fun formatMeasure(
        context: Context,
        value: Double,
        isValueInDefaultUnit: Boolean,
    ) = UnitUtils.formatMeasure(
        context = context,
        enum = this,
        value = value,
        precision = precision,
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
        precision = precision,
        isValueInDefaultUnit = isValueInDefaultUnit,
        unitWidth = UnitWidth.FULL
    )
}
