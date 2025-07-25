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
import android.icu.util.TimeUnit
import android.os.Build
import org.breezyweather.R
import org.breezyweather.common.basic.models.options.basic.UnitEnum
import org.breezyweather.common.basic.models.options.basic.UnitUtils
import org.breezyweather.common.extensions.currentLocale

// actual precipitation = precipitation(mm) * factor.
enum class PrecipitationUnit(
    override val id: String,
    override val measureUnit: MeasureUnit?,
    override val perMeasureUnit: MeasureUnit?,
    override val convertUnit: (Double) -> Double,
    val chartStep: Double,
    val precision: Int = 1,
) : UnitEnum<Double> {

    MILLIMETER(
        "mm",
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) MeasureUnit.MILLIMETER else null,
        null,
        { valueInDefaultUnit -> valueInDefaultUnit },
        chartStep = 5.0
    ),
    CENTIMETER(
        "cm",
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) MeasureUnit.CENTIMETER else null,
        null,
        { valueInDefaultUnit -> valueInDefaultUnit.div(10f) },
        chartStep = 0.5
    ),
    INCH(
        "in",
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) MeasureUnit.INCH else null,
        null,
        { valueInDefaultUnit -> valueInDefaultUnit.div(25.4f) },
        chartStep = 0.2,
        precision = 2
    ),
    LITER_PER_SQUARE_METER(
        "lpsqm",
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) MeasureUnit.LITER else null,
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) MeasureUnit.SQUARE_METER else null,
        { valueInDefaultUnit -> valueInDefaultUnit },
        chartStep = 5.0
    ),
    ;

    companion object {

        /**
         * Copyright © 1991-Present Unicode, Inc.
         * License: Unicode License v3 https://www.unicode.org/license.txt
         * Source (simplified): https://github.com/unicode-org/cldr/blob/3f3967f3cbadc56bbb44a9aed20784e82ac64c67/common/supplemental/units.xml#L474-L478
         */
        fun getDefaultUnit(
            context: Context,
        ) = when (context.currentLocale.country) {
            "BR" -> CENTIMETER
            "US" -> INCH
            else -> MILLIMETER
        }

        /**
         * Copyright © 1991-Present Unicode, Inc.
         * License: Unicode License v3 https://www.unicode.org/license.txt
         * Source (simplified): https://github.com/unicode-org/cldr/blob/3f3967f3cbadc56bbb44a9aed20784e82ac64c67/common/supplemental/units.xml#L498-L501
         */
        fun getDefaultSnowfallUnit(
            context: Context,
        ) = when (context.currentLocale.country) {
            "US" -> INCH
            else -> CENTIMETER
        }
    }

    override val valueArrayId = R.array.precipitation_unit_values
    override val nameArrayId = R.array.precipitation_units
    override val contentDescriptionArrayId = R.array.precipitation_unit_voices

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

// actual precipitation intensity = precipitation intensity(mm/h) * factor.
enum class PrecipitationIntensityUnit(
    override val id: String,
    override val measureUnit: MeasureUnit?,
    override val perMeasureUnit: TimeUnit?,
    override val convertUnit: (Double) -> Double,
    val precision: Int = 1,
) : UnitEnum<Double> {

    MILLIMETER_PER_HOUR(
        "mmph",
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) MeasureUnit.MILLIMETER else null,
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) MeasureUnit.HOUR else null,
        { valueInDefaultUnit -> valueInDefaultUnit }
    ),
    CENTIMETER_PER_HOUR(
        "cmph",
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) MeasureUnit.CENTIMETER else null,
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) MeasureUnit.HOUR else null,
        { valueInDefaultUnit -> valueInDefaultUnit.div(10f) }
    ),
    INCH_PER_HOUR(
        "inph",
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) MeasureUnit.INCH else null,
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) MeasureUnit.HOUR else null,
        { valueInDefaultUnit -> valueInDefaultUnit.div(25.4f) },
        2
    ),
    LITER_PER_SQUARE_METER_PER_HOUR(
        "lpsqmph",
        null, // TODO
        null, // TODO: Multiple perUnit
        { valueInDefaultUnit -> valueInDefaultUnit }
    ),
    ;

    companion object {

        /**
         * Copyright © 1991-Present Unicode, Inc.
         * License: Unicode License v3 https://www.unicode.org/license.txt
         * Source (simplified): https://github.com/unicode-org/cldr/blob/3f3967f3cbadc56bbb44a9aed20784e82ac64c67/common/supplemental/units.xml#L561-L565
         */
        fun getDefaultUnit(
            context: Context,
        ) = when (context.currentLocale.country) {
            "BR" -> CENTIMETER_PER_HOUR
            "US" -> INCH_PER_HOUR
            else -> MILLIMETER_PER_HOUR
        }

        /**
         * Copyright © 1991-Present Unicode, Inc.
         * License: Unicode License v3 https://www.unicode.org/license.txt
         * Source (simplified): https://github.com/unicode-org/cldr/blob/3f3967f3cbadc56bbb44a9aed20784e82ac64c67/common/supplemental/units.xml#L566-L569
         */
        fun getDefaultSnowfallUnit(
            context: Context,
        ) = when (context.currentLocale.country) {
            "US" -> INCH_PER_HOUR
            else -> CENTIMETER_PER_HOUR
        }
    }

    override val valueArrayId = R.array.precipitation_intensity_unit_values
    override val nameArrayId = R.array.precipitation_intensity_units
    override val contentDescriptionArrayId = R.array.precipitation_intensity_unit_voices

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
