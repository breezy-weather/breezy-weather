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

// actual duration = duration(h) * factor.
enum class DurationUnit(
    override val id: String,
    override val measureUnit: TimeUnit?,
    override val convertUnit: (Double) -> Double,
    override val perMeasureUnit: MeasureUnit? = null,
) : UnitEnum<Double> {
    HOUR(
        "h",
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) MeasureUnit.HOUR else null,
        { valueInDefaultUnit -> valueInDefaultUnit }
    ),
    ;

    companion object {
        fun validateDailyValue(hours: Double?): Double? {
            return hours?.let { if (it in 0.0..24.0) it else null }
        }

        fun validateHalfDayValue(hours: Double?): Double? {
            return hours?.let { if (it in 0.0..12.0) it else null }
        }
    }

    override val valueArrayId = R.array.duration_unit_values
    override val nameArrayId = R.array.duration_units
    override val contentDescriptionArrayId = R.array.duration_unit_voices

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
        precision = 2
    )

    fun formatMeasureShort(
        context: Context,
        value: Double,
        isValueInDefaultUnit: Boolean = true,
    ) = UnitUtils.formatMeasure(
        context = context,
        enum = this,
        value = value,
        precision = 1,
        isValueInDefaultUnit = isValueInDefaultUnit,
        unitWidth = UnitWidth.NARROW
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
