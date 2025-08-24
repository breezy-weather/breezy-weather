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

package org.breezyweather.unit.pollutant

import android.content.Context
import android.icu.util.MeasureUnit
import android.os.Build
import androidx.annotation.RequiresApi
import org.breezyweather.unit.R
import org.breezyweather.unit.WeatherUnit
import org.breezyweather.unit.formatting.UnitDecimals
import org.breezyweather.unit.formatting.UnitTranslation
import org.breezyweather.unit.formatting.UnitWidth
import java.util.Locale

enum class PollutantConcentrationUnit(
    override val id: String,
    override val displayName: UnitTranslation,
    override val nominative: UnitTranslation,
    override val per: UnitTranslation? = UnitTranslation(
        short = R.string.volume_m3_per_short,
        long = R.string.volume_m3_per_long
    ),
    val convertFromReference: (Double) -> Double,
    val convertToReference: (Double) -> Double,
    override val decimals: UnitDecimals,
    val chartStep: Double,
) : WeatherUnit {

    MICROGRAM_PER_CUBIC_METER(
        "microgpcum",
        displayName = UnitTranslation(
            short = R.string.weight_microg_display_name_short,
            long = R.string.weight_microg_display_name_long
        ),
        nominative = UnitTranslation(
            short = R.string.weight_microg_nominative_short,
            long = R.string.weight_microg_nominative_long
        ),
        convertFromReference = { valueInDefaultUnit -> valueInDefaultUnit },
        convertToReference = { valueInThisUnit -> valueInThisUnit },
        decimals = UnitDecimals(0),
        chartStep = 15.0 // TODO
    ),
    MILLIGRAM_PER_CUBIC_METER(
        "mgpcum",
        displayName = UnitTranslation(
            short = R.string.weight_mg_display_name_short,
            long = R.string.weight_mg_display_name_long
        ),
        nominative = UnitTranslation(
            short = R.string.weight_mg_nominative_short,
            long = R.string.weight_mg_nominative_long
        ),
        convertFromReference = { valueInDefaultUnit -> valueInDefaultUnit.div(1000.0) },
        convertToReference = { valueInThisUnit -> valueInThisUnit.times(1000.0) },
        decimals = UnitDecimals(short = 0, long = 1),
        chartStep = 15.0 // TODO
    ),
    ;

    @RequiresApi(Build.VERSION_CODES.N)
    override fun getMeasureUnit(): MeasureUnit? {
        return when (this) {
            MICROGRAM_PER_CUBIC_METER -> MeasureUnit.MICROGRAM
            MILLIGRAM_PER_CUBIC_METER -> MeasureUnit.MILLIGRAM
        }
    }

    @RequiresApi(Build.VERSION_CODES.N)
    override fun getPerMeasureUnit(): MeasureUnit? {
        return MeasureUnit.CUBIC_METER
    }

    /**
     * Override to:
     * - Use English units with Traditional Chinese
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
        // Translations missing for Esperanto in CLDR
        if (locale.language.equals("eo", ignoreCase = true)) {
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
        val correctedLocale = locale.let {
            /**
             * Taiwan guidelines: https://www.bsmi.gov.tw/wSite/public/Attachment/f1736149048776.pdf
             * Ongoing issue: https://unicode-org.atlassian.net/jira/software/c/projects/CLDR/issues/CLDR-10604
             */
            if (it.language.equals("zh", ignoreCase = true) &&
                arrayOf("TW", "HK", "MO").any { c -> it.country.equals(c, ignoreCase = true) } &&
                unitWidth != UnitWidth.LONG
            ) {
                Locale.Builder().setLanguage("en").setRegion("001").build()
            } else {
                it
            }
        }
        return super.format(
            context = context,
            value = value,
            valueWidth = valueWidth,
            unitWidth = unitWidth,
            locale = correctedLocale,
            showSign = showSign,
            useNumberFormatter = useNumberFormatter,
            useMeasureFormat = useNumberFormatter
        )
    }

    companion object {

        fun getUnit(id: String): PollutantConcentrationUnit? {
            return entries.firstOrNull { it.id == id }
        }
    }
}

/** Converts the given time pollutant concentration [value] expressed in the specified [sourceUnit] into the specified [targetUnit]. */
internal fun convertPollutantConcentrationUnit(
    value: Double,
    sourceUnit: PollutantConcentrationUnit,
    targetUnit: PollutantConcentrationUnit,
): Double {
    return if (sourceUnit == PollutantConcentrationUnit.MICROGRAM_PER_CUBIC_METER) {
        targetUnit.convertFromReference(value)
    } else if (targetUnit == PollutantConcentrationUnit.MICROGRAM_PER_CUBIC_METER) {
        sourceUnit.convertToReference(value)
    } else {
        targetUnit.convertFromReference(sourceUnit.convertToReference(value))
    }
}
