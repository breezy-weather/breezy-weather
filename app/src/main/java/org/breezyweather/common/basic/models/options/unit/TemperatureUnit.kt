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
import android.text.BidiFormatter
import androidx.core.text.util.LocalePreferences
import org.breezyweather.R
import org.breezyweather.common.basic.models.options.basic.UnitEnum
import org.breezyweather.common.basic.models.options.basic.UnitUtils
import org.breezyweather.common.extensions.currentLocale
import org.breezyweather.common.extensions.isRtl

enum class TemperatureUnit(
    override val id: String,
    override val measureUnit: MeasureUnit?,
    override val convertUnit: (Double) -> Double,
    val convertDegreeDayUnit: (Double) -> Double,
    val chartStep: Double,
    override val perMeasureUnit: MeasureUnit? = null,
) : UnitEnum<Double> {

    CELSIUS(
        "c",
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) MeasureUnit.CELSIUS else null,
        { valueInDefaultUnit -> valueInDefaultUnit },
        { valueInDefaultUnit -> valueInDefaultUnit },
        5.0
    ),
    FAHRENHEIT(
        "f",
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) MeasureUnit.FAHRENHEIT else null,
        { valueInDefaultUnit -> 32 + valueInDefaultUnit.times(1.8) },
        { valueInDefaultUnit -> valueInDefaultUnit.times(1.8) },
        10.0
    ),
    KELVIN(
        "k",
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) MeasureUnit.KELVIN else null,
        { valueInDefaultUnit -> 273.15 + valueInDefaultUnit },
        { valueInDefaultUnit -> valueInDefaultUnit },
        5.0
    ),
    ;

    companion object {
        /**
         * Resolve in the following order:
         * - System regional preference
         * - Current locale region preference
         */
        fun getDefaultUnit(
            context: Context,
        ) = when (LocalePreferences.getTemperatureUnit()) {
            LocalePreferences.TemperatureUnit.CELSIUS -> CELSIUS
            LocalePreferences.TemperatureUnit.FAHRENHEIT -> FAHRENHEIT
            LocalePreferences.TemperatureUnit.KELVIN -> KELVIN
            /**
             * Copyright © 1991-Present Unicode, Inc.
             * License: Unicode License v3 https://www.unicode.org/license.txt
             * Source: https://github.com/unicode-org/cldr/blob/3f3967f3cbadc56bbb44a9aed20784e82ac64c67/common/supplemental/units.xml#L579-L582
             */
            else -> when (context.currentLocale.country) {
                "BS", "BZ", "KY", "PR", "PW", "US" -> FAHRENHEIT
                else -> CELSIUS
            }
        }
    }

    override val valueArrayId = R.array.temperature_unit_values
    override val nameArrayId = R.array.temperature_units
    private val shortArrayId = R.array.temperature_units_short
    override val contentDescriptionArrayId = R.array.temperature_units

    override fun getName(context: Context) = UnitUtils.getName(context, this)

    fun getShortName(
        context: Context,
    ) = UnitUtils.getNameByValue(
        res = context.resources,
        value = id,
        nameArrayId = shortArrayId,
        valueArrayId = valueArrayId
    )!!

    override fun formatValue(
        context: Context,
        valueInDefaultUnit: Double,
    ) = UnitUtils.formatValue(
        context = context,
        enum = this,
        value = valueInDefaultUnit,
        precision = 0
    )

    override fun getMeasureContentDescription(context: Context) = UnitUtils.getMeasureContentDescription(context, this)

    override fun getConvertedUnit(valueInDefaultUnit: Double) = convertUnit(valueInDefaultUnit)

    fun getDegreeDayConvertedUnit(valueInDefaultUnit: Double) = convertDegreeDayUnit(valueInDefaultUnit)

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

    fun formatMeasure(
        context: Context,
        valueInDefaultUnit: Double,
        precision: Int = 1,
    ) = UnitUtils.formatMeasure(
        context = context,
        enum = this,
        value = valueInDefaultUnit,
        precision = precision
    )

    fun formatMeasureShort(
        context: Context,
        value: Double,
        precision: Int = 0,
        isValueInDefaultUnit: Boolean = true,
    ): String = UnitUtils.formatValue(context, this, value, precision, isValueInDefaultUnit).let { formattedValue ->
        if (context.isRtl) BidiFormatter.getInstance().unicodeWrap(formattedValue) else formattedValue
    } + getShortName(context)

    fun formatDegreeDay(
        context: Context,
        valueInDefaultUnit: Double,
        precision: Int = 1,
    ) = UnitUtils.formatMeasure(
        context = context,
        enum = this,
        value = getDegreeDayConvertedUnit(valueInDefaultUnit),
        precision = precision,
        isValueInDefaultUnit = false
    )

    fun formatDegreeDayContentDescription(
        context: Context,
        valueInDefaultUnit: Double,
        precision: Int = 0,
    ) = UnitUtils.formatMeasure(
        context = context,
        enum = this,
        value = getDegreeDayConvertedUnit(valueInDefaultUnit),
        precision = precision,
        isValueInDefaultUnit = false,
        unitWidth = UnitWidth.FULL
    )

    override fun formatContentDescription(
        context: Context,
        value: Double,
        isValueInDefaultUnit: Boolean,
    ) = UnitUtils.formatMeasure(
        context = context,
        enum = this,
        value = value,
        precision = 0,
        isValueInDefaultUnit = isValueInDefaultUnit,
        unitWidth = UnitWidth.FULL
    )
}
