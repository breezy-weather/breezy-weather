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

// actual pressure = pressure(mb) * factor.
enum class PressureUnit(
    override val id: String,
    override val measureUnit: MeasureUnit?,
    override val perMeasureUnit: MeasureUnit?,
    override val convertUnit: (Double) -> Double,
    val chartStep: Double,
    val precision: Int = 0,
) : UnitEnum<Double> {

    MILLIBAR(
        "mb",
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) MeasureUnit.MILLIBAR else null,
        null,
        { valueInDefaultUnit -> valueInDefaultUnit },
        chartStep = 15.0
    ),
    KILOPASCAL( // TODO: Consider deleting
        "kpa",
        null,
        null,
        { valueInDefaultUnit -> valueInDefaultUnit.div(10) },
        chartStep = 1.5,
        1
    ),
    HECTOPASCAL(
        "hpa",
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) MeasureUnit.HECTOPASCAL else null,
        null,
        { valueInDefaultUnit -> valueInDefaultUnit },
        chartStep = 15.0
    ),
    ATMOSPHERE(
        "atm",
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) MeasureUnit.ATMOSPHERE else null,
        null,
        { valueInDefaultUnit -> valueInDefaultUnit.div(1013) },
        chartStep = 0.015,
        3
    ),
    MILLIMETER_OF_MERCURY(
        "mmhg",
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) MeasureUnit.MILLIMETER_OF_MERCURY else null,
        null,
        { valueInDefaultUnit -> valueInDefaultUnit.div(1.333) },
        chartStep = 10.0
    ),
    INCH_OF_MERCURY(
        "inhg",
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) MeasureUnit.INCH_HG else null,
        null,
        { valueInDefaultUnit -> valueInDefaultUnit.div(33.864) },
        chartStep = 0.5,
        2
    ),
    KILOGRAM_FORCE_PER_SQUARE_CENTIMETER( // TODO: Consider deleting
        "kgfpsqcm",
        null,
        null,
        { valueInDefaultUnit -> valueInDefaultUnit.div(980.7) },
        chartStep = 0.015,
        3
    ),
    ;

    companion object {

        const val NORMAL = 1013.25

        /**
         * Copyright © 1991-Present Unicode, Inc.
         * License: Unicode License v3 https://www.unicode.org/license.txt
         * Source (simplified): https://github.com/unicode-org/cldr/blob/3f3967f3cbadc56bbb44a9aed20784e82ac64c67/common/supplemental/units.xml#L546-L551
         */
        fun getDefaultUnit(
            context: Context,
        ) = when (context.currentLocale.country) {
            "BR", "EG", "GB", "IL", "TH" -> MILLIBAR
            "MX", "RU" -> MILLIMETER_OF_MERCURY
            "US" -> INCH_OF_MERCURY
            else -> HECTOPASCAL
        }
    }

    override val valueArrayId = R.array.pressure_unit_values
    override val nameArrayId = R.array.pressure_units
    override val contentDescriptionArrayId = R.array.pressure_unit_voices

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
