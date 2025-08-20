/*
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

package org.breezyweather.unit.ratio

import android.content.Context
import android.icu.text.NumberFormat
import android.icu.util.MeasureUnit
import android.os.Build
import org.breezyweather.unit.R
import org.breezyweather.unit.WeatherUnit
import org.breezyweather.unit.formatting.UnitDecimals
import org.breezyweather.unit.formatting.UnitTranslation
import org.breezyweather.unit.formatting.UnitWidth
import org.breezyweather.unit.formatting.format
import org.breezyweather.unit.formatting.formatWithNumberFormatter
import java.util.Locale

enum class RatioUnit(
    override val id: String,
    override val displayName: UnitTranslation,
    override val nominative: UnitTranslation,
    override val per: UnitTranslation? = null,
    override val measureUnit: MeasureUnit?,
    override val perMeasureUnit: MeasureUnit? = null,
    val convertFromReference: (Double) -> Double,
    val convertToReference: (Double) -> Double,
    override val decimals: UnitDecimals,
    val chartStep: Double,
) : WeatherUnit {

    PERMILLE(
        id = "permille",
        displayName = UnitTranslation(R.string.ratio_permille_display_name_short),
        nominative = UnitTranslation(R.string.ratio_permille_nominative_short),
        measureUnit = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) MeasureUnit.PERMILLE else null,
        convertFromReference = { valueInDefaultUnit -> valueInDefaultUnit },
        convertToReference = { valueInThisUnit -> valueInThisUnit },
        decimals = UnitDecimals(0),
        chartStep = 200.0
    ),
    PERCENT(
        id = "percent",
        displayName = UnitTranslation(R.string.ratio_percent_display_name_short),
        nominative = UnitTranslation(R.string.ratio_percent_nominative_short),
        measureUnit = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) MeasureUnit.PERCENT else null,
        convertFromReference = { valueInDefaultUnit -> valueInDefaultUnit.div(10.0) },
        convertToReference = { valueInThisUnit -> valueInThisUnit.times(10.0) },
        decimals = UnitDecimals(narrow = 0, short = 1, long = 1),
        chartStep = 20.0
    ),
    FRACTION(
        id = "fraction",
        displayName = UnitTranslation(R.string.ratio_fraction_display_name_short),
        nominative = UnitTranslation(R.string.ratio_fraction_nominative_short),
        measureUnit = null,
        convertFromReference = { valueInDefaultUnit -> valueInDefaultUnit.div(1000.0) },
        convertToReference = { valueInThisUnit -> valueInThisUnit.times(1000.0) },
        decimals = UnitDecimals(narrow = 1, short = 2, long = 3),
        chartStep = 0.2
    ),
    ;

    /**
     * @param useMeasureFormat ignored, never supported
     */
    override fun format(
        context: Context,
        value: Number,
        valueWidth: UnitWidth,
        unitWidth: UnitWidth,
        locale: Locale,
        showSign: Boolean,
        useNumberFormatter: Boolean,
        useMeasureFormat: Boolean,
    ): String {
        if (this == FRACTION) {
            return value.format(
                decimals = getPrecision(valueWidth),
                locale = locale,
                showSign = showSign,
                useNumberFormatter = useNumberFormatter,
                useNumberFormat = useMeasureFormat
            )
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R && useNumberFormatter) {
            return measureUnit!!.formatWithNumberFormatter(
                locale = locale,
                value = value,
                perUnit = null,
                precision = getPrecision(valueWidth),
                numberFormatterWidth = unitWidth.numberFormatterWidth!!,
                showSign = showSign
            )
        }

        if (this == PERCENT && !showSign) {
            return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                NumberFormat.getPercentInstance(locale)
                    .apply { maximumFractionDigits = getPrecision(valueWidth) }
                    .format(if (value.toDouble() > 0) value.toDouble().div(100.0) else 0)
            } else {
                java.text.NumberFormat.getPercentInstance(locale)
                    .apply { maximumFractionDigits = getPrecision(valueWidth) }
                    .format(if (value.toDouble() > 0) value.toDouble().div(100.0) else 0)
            }
        }

        return formatWithAndroidTranslations(
            context = context,
            value = value,
            valueWidth = valueWidth,
            unitWidth = unitWidth,
            locale = locale,
            showSign = showSign,
            useNumberFormatter = useNumberFormatter,
            useMeasureFormat = useMeasureFormat
        )
    }

    companion object {

        fun getUnit(id: String): RatioUnit? {
            return entries.firstOrNull { it.id == id }
        }
    }
}

/** Converts the given time ratio [value] expressed in the specified [sourceUnit] into the specified [targetUnit]. */
internal fun convertRatioUnit(
    value: Double,
    sourceUnit: RatioUnit,
    targetUnit: RatioUnit,
): Double {
    return if (sourceUnit == RatioUnit.PERMILLE) {
        targetUnit.convertFromReference(value)
    } else if (targetUnit == RatioUnit.PERMILLE) {
        sourceUnit.convertToReference(value)
    } else {
        targetUnit.convertFromReference(sourceUnit.convertToReference(value))
    }
}
