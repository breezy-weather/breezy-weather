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
import org.breezyweather.R
import org.breezyweather.common.basic.models.options.basic.UnitEnum
import org.breezyweather.common.basic.models.options.basic.Utils
import org.breezyweather.common.extensions.isRtl

// actual distance = distance(km) * factor.
enum class DistanceUnit(
    override val id: String,
    override val convertUnit: (Double) -> Double,
    val chartStep: (Double) -> Double,
    val decimalNumbers: Int = 0,
) : UnitEnum<Double> {

    M(
        "m",
        { valueInDefaultUnit -> valueInDefaultUnit },
        chartStep = { maxY -> if (maxY < 40000) 5000.0 else 10000.0 }
    ),
    KM(
        "km",
        { valueInDefaultUnit -> valueInDefaultUnit.div(1000f) },
        chartStep = { maxY -> if (maxY < 4) 5.0 else 10.0 },
        decimalNumbers = 1
    ),
    MI(
        "mi",
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
        decimalNumbers = 1
    ),
    NMI(
        "nmi",
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
        decimalNumbers = 1
    ),
    FT(
        "ft",
        { valueInDefaultUnit -> valueInDefaultUnit.times(3.28084f) },
        chartStep = { maxY -> if (maxY < 150000) 25000.0 else 50000.0 }
    ),
    ;

    companion object {

        fun getInstance(
            value: String,
        ) = DistanceUnit.entries.firstOrNull {
            it.id == value
        } ?: M

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
    override val voiceArrayId = R.array.distance_unit_voices

    override fun getName(context: Context) = Utils.getName(context, this)

    override fun getVoice(context: Context) = Utils.getVoice(context, this)

    override fun getValueWithoutUnit(valueInDefaultUnit: Double) = convertUnit(valueInDefaultUnit)

    override fun getValueTextWithoutUnit(
        valueInDefaultUnit: Double,
    ) = Utils.getValueTextWithoutUnit(this, valueInDefaultUnit, decimalNumbers)!!

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
        decimalNumber = decimalNumbers,
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
        decimalNumber = decimalNumbers,
        rtl = rtl
    )
}
