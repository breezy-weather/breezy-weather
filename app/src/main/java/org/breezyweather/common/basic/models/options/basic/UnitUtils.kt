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

package org.breezyweather.common.basic.models.options.basic

import android.content.Context
import android.content.res.Resources
import android.icu.number.LocalizedNumberFormatter
import android.icu.number.NumberFormatter
import android.icu.number.Precision
import android.icu.text.MeasureFormat
import android.icu.text.NumberFormat
import android.icu.util.Measure
import android.icu.util.MeasureUnit
import android.icu.util.TimeUnit
import android.os.Build
import android.text.BidiFormatter
import android.text.SpannableString
import android.text.Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
import android.text.style.RelativeSizeSpan
import androidx.annotation.ArrayRes
import androidx.annotation.RequiresApi
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.BaselineShift
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.em
import org.breezyweather.common.basic.models.options.unit.UnitWidth
import org.breezyweather.common.extensions.code
import org.breezyweather.common.extensions.currentLocale
import org.breezyweather.common.extensions.isRtl
import org.breezyweather.common.extensions.isTraditionalChinese
import org.breezyweather.domain.settings.SettingsManager
import org.breezyweather.unit.formatting.format
import java.text.FieldPosition
import java.util.Locale

object UnitUtils {

    fun getName(
        context: Context,
        enum: BaseEnum,
    ) = getNameByValue(
        res = context.resources,
        value = enum.id,
        nameArrayId = enum.nameArrayId,
        valueArrayId = enum.valueArrayId
    )!!

    fun getMeasureContentDescription(
        context: Context,
        enum: ContentDescriptionEnum,
    ) = getNameByValue(
        res = context.resources,
        value = enum.id,
        nameArrayId = enum.contentDescriptionArrayId,
        valueArrayId = enum.valueArrayId
    )!!

    fun getNameByValue(
        res: Resources,
        value: String,
        @ArrayRes nameArrayId: Int,
        @ArrayRes valueArrayId: Int,
    ): String? {
        val names = res.getStringArray(nameArrayId)
        val values = res.getStringArray(valueArrayId)
        return getNameByValue(value, names, values)
    }

    private fun getNameByValue(
        value: String,
        names: Array<String>,
        values: Array<String>,
    ) = values.zip(names).firstOrNull { it.first == value }?.second

    @Deprecated("Use Number.format() extension")
    fun formatValue(
        context: Context,
        enum: UnitEnum<Double>,
        value: Double,
        precision: Int,
        isValueInDefaultUnit: Boolean = true,
        showSign: Boolean = false,
    ): String {
        return (if (isValueInDefaultUnit) enum.getConvertedUnit(value) else value)
            .format(
                decimals = precision,
                locale = context.currentLocale,
                showSign = showSign,
                useNumberFormatter = SettingsManager.getInstance(context).useNumberFormatter,
                useMeasureFormat = SettingsManager.getInstance(context).useMeasureFormat
            )
    }

    @Deprecated("Use Number.format() extension")
    fun formatDouble(
        context: Context,
        value: Double,
        precision: Int = 2,
        showSign: Boolean = false,
    ): String {
        return value.format(
            decimals = precision,
            locale = context.currentLocale,
            showSign = showSign,
            useNumberFormatter = SettingsManager.getInstance(context).useNumberFormatter,
            useMeasureFormat = SettingsManager.getInstance(context).useMeasureFormat
        )
    }

    @Deprecated("Use Number.format() extension")
    fun formatInt(
        context: Context,
        value: Int,
        showSign: Boolean = false,
    ): String {
        return value.format(
            decimals = 0,
            locale = context.currentLocale,
            showSign = showSign,
            useNumberFormatter = SettingsManager.getInstance(context).useNumberFormatter,
            useMeasureFormat = SettingsManager.getInstance(context).useMeasureFormat
        )
    }

    @Deprecated("Use Number.format() extension")
    fun formatNumber(
        context: Context,
        valueWithoutUnit: Number,
        precision: Int,
        showSign: Boolean = false,
    ): String {
        return valueWithoutUnit.format(
            decimals = precision,
            locale = context.currentLocale,
            showSign = showSign,
            useNumberFormatter = SettingsManager.getInstance(context).useNumberFormatter,
            useMeasureFormat = SettingsManager.getInstance(context).useMeasureFormat
        )
    }

    fun formatMeasure(
        context: Context,
        enum: UnitEnum<Double>,
        value: Double,
        precision: Int,
        isValueInDefaultUnit: Boolean = true,
        unitWidth: UnitWidth = UnitWidth.SHORT,
    ): String {
        if (enum.measureUnit != null &&
            (enum.perMeasureUnit == null || Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) &&
            SettingsManager.getInstance(context).let { it.useNumberFormatter || it.useMeasureFormat }
        ) {
            val convertedValue = if (isValueInDefaultUnit) enum.getConvertedUnit(value) else value

            // LogHelper.log(msg = "Formatting with ICU ${enum.id}: ${enum.measureUnit} per ${enum.perMeasureUnit}")

            // If result is null, it skips to the default non-ICU formatter
            val adjustedFormatting = getAdjustedFormatting(
                context.currentLocale,
                enum.measureUnit!!,
                unitWidth
            )
            if (adjustedFormatting != null) {
                return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R &&
                    SettingsManager.getInstance(context).useNumberFormatter
                ) {
                    formatWithNumberFormatter(
                        adjustedFormatting.first,
                        convertedValue,
                        enum.measureUnit!!,
                        enum.perMeasureUnit,
                        precision,
                        adjustedFormatting.second!!
                    )
                } else {
                    formatWithMeasureFormat(
                        adjustedFormatting.first,
                        convertedValue,
                        enum.measureUnit!!,
                        enum.perMeasureUnit,
                        precision,
                        adjustedFormatting.third!!
                    )
                }
            }
        }

        // LogHelper.log(msg = "Not formatting with ICU ${enum.id} in ${context.currentLocale}")
        return formatWithoutIcu(
            context,
            enum,
            formatValue(context, enum, value, precision, isValueInDefaultUnit),
            unitWidth
        )
    }

    /**
     * @param locale
     * @param value
     * @param unit
     * @param perUnit an optional per unit
     * @param numberFormatterWidth
     */
    @RequiresApi(api = Build.VERSION_CODES.R)
    fun formatWithNumberFormatter(
        locale: Locale,
        value: Number,
        unit: MeasureUnit,
        perUnit: MeasureUnit? = null,
        precision: Int,
        numberFormatterWidth: NumberFormatter.UnitWidth,
    ): String {
        return (NumberFormatter.withLocale(locale) as LocalizedNumberFormatter)
            .precision(if (precision == 0) Precision.integer() else Precision.maxFraction(precision))
            .unit(unit)
            .perUnit(perUnit)
            .unitWidth(numberFormatterWidth)
            .format(value)
            .toString()
    }

    /**
     * @param locale
     * @param value
     * @param unit
     * @param perUnit an optional per unit. /!\ Only supported on Android SDK >= 26
     */
    @RequiresApi(api = Build.VERSION_CODES.N)
    fun formatWithMeasureFormat(
        locale: Locale,
        value: Number,
        unit: MeasureUnit,
        perUnit: MeasureUnit? = null,
        precision: Int,
        measureFormatWidth: MeasureFormat.FormatWidth,
    ): String {
        if (perUnit != null && Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
            throw UnsupportedOperationException()
        }

        return MeasureFormat
            .getInstance(
                locale,
                measureFormatWidth,
                NumberFormat.getInstance().apply { maximumFractionDigits = precision }
            )
            .let {
                if (perUnit != null) {
                    it.formatMeasurePerUnit(
                        Measure(value, unit),
                        perUnit,
                        StringBuilder(),
                        FieldPosition(0)
                    ).toString()
                } else {
                    it.format(
                        Measure(value, unit)
                    )
                }
            }
    }

    /**
     * CLDR is not always good, this function replaces some parameters with others
     * @returns null if ICU should not be used
     */
    @RequiresApi(Build.VERSION_CODES.N)
    private fun getAdjustedFormatting(
        currentLocale: Locale,
        unit: MeasureUnit,
        unitWidth: UnitWidth,
    ): Triple<Locale, NumberFormatter.UnitWidth?, MeasureFormat.FormatWidth?>? {
        val numberFormatterWidth = unitWidth.numberFormatterWidth
        val measureFormatWidth = unitWidth.measureFormatWidth
        if (unitWidth == UnitWidth.FULL) {
            return Triple(currentLocale, numberFormatterWidth, measureFormatWidth)
        }

        if (unit is TimeUnit && unitWidth == UnitWidth.NARROW) {
            return Triple(currentLocale, UnitWidth.SHORT.numberFormatterWidth, UnitWidth.SHORT.measureFormatWidth)
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.BAKLAVA && unit == MeasureUnit.BEAUFORT) {
            /*
             * Japanese: use 風力7 instead of “B 7”
             * Traditional Chinese: use 7級 rather than the verbose “蒲福風級 7 級”
             */
            if (currentLocale.code.startsWith("ja", ignoreCase = true) || currentLocale.isTraditionalChinese) {
                return null
            }
        }

        var newLocale = currentLocale
        /**
         * Use English units with Traditional Chinese
         * Except for durations
         *
         * Taiwan guidelines: https://www.bsmi.gov.tw/wSite/public/Attachment/f1736149048776.pdf
         * Ongoing issue: https://unicode-org.atlassian.net/jira/software/c/projects/CLDR/issues/CLDR-10604
         */
        if (currentLocale.isTraditionalChinese && unit !is TimeUnit) {
            newLocale = Locale.Builder().setLanguage("en").setRegion("001").build()
        }

        /**
         * Beaufort scale:
         * - fr_FR uses the incorrect unit (it should be "Bf"), replace with fr_CA
         */
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.BAKLAVA && unit == MeasureUnit.BEAUFORT) {
            newLocale = Locale.Builder().setLanguage("fr").setRegion("CA").build()
        }

        return Triple(newLocale, numberFormatterWidth, measureFormatWidth)
    }

    fun formatWithoutIcu(
        context: Context,
        enum: UnitEnum<Double>,
        formattedValue: String,
        unitWidth: UnitWidth = UnitWidth.SHORT,
    ) = (if (context.isRtl) BidiFormatter.getInstance().unicodeWrap(formattedValue) else formattedValue) +
        (if (unitWidth != UnitWidth.NARROW) "\u202f" else "") +
        (if (unitWidth != UnitWidth.FULL) getName(context, enum) else getMeasureContentDescription(context, enum))

    /**
     * Uses LocalizedNumberFormatter on Android SDK >= 30 (which is the recommended way)
     * Uses NumberFormat on Android SDK >= 24
     * Uses java.text.NumberFormat on Android SDK < 24
     *
     * @param context
     * @param value between 0.0 and 100.0
     * @param precision
     */
    fun formatPercent(
        context: Context,
        value: Double,
        precision: Int = 0,
    ): String {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R &&
            SettingsManager.getInstance(context).useNumberFormatter
        ) {
            (NumberFormatter.withLocale(context.currentLocale) as LocalizedNumberFormatter)
                .precision(if (precision == 0) Precision.integer() else Precision.maxFraction(precision))
                .unit(MeasureUnit.PERCENT)
                .format(value)
                .toString()
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            NumberFormat.getPercentInstance(context.currentLocale)
                .apply { maximumFractionDigits = precision }
                .format(if (value > 0) value.div(100.0) else 0)
        } else {
            java.text.NumberFormat.getPercentInstance(context.currentLocale)
                .apply { maximumFractionDigits = precision }
                .format(if (value > 0) value.div(100.0) else 0)
        }
    }

    fun formatPercent(
        context: Context,
        value: Int,
    ): String {
        return formatPercent(context, value.toDouble(), 0)
    }

    /**
     * Units will stay at the same size if it somehow fails to parse
     */
    fun formatUnitsHalfSize(formattedMeasure: String): CharSequence {
        val firstIndexOfADigit = formattedMeasure.indexOfAny(DIGITS, 0)
        val lastIndexOfADigit = formattedMeasure.lastIndexOfAny(DIGITS, formattedMeasure.length - 1)
        if (firstIndexOfADigit < 0 || lastIndexOfADigit < 0 || lastIndexOfADigit > formattedMeasure.length) {
            return formattedMeasure
        }
        return SpannableString(formattedMeasure)
            .apply {
                if (firstIndexOfADigit > 0) {
                    setSpan(
                        RelativeSizeSpan(0.5f),
                        0,
                        firstIndexOfADigit,
                        SPAN_EXCLUSIVE_EXCLUSIVE
                    )
                }
                if (lastIndexOfADigit < formattedMeasure.length) {
                    setSpan(
                        RelativeSizeSpan(0.5f),
                        lastIndexOfADigit + 1,
                        formattedMeasure.length,
                        SPAN_EXCLUSIVE_EXCLUSIVE
                    )
                }
            }
    }

    /**
     * Units will stay at the same size if it somehow fails to parse
     */
    @Composable
    fun formatUnitsDifferentFontSize(
        formattedMeasure: String,
        fontSize: TextUnit,
    ): AnnotatedString {
        val firstIndexOfADigit = formattedMeasure.indexOfAny(DIGITS, 0)
        val lastIndexOfADigit = formattedMeasure.lastIndexOfAny(DIGITS, formattedMeasure.length - 1)
        return buildAnnotatedString {
            if (firstIndexOfADigit < 0 || lastIndexOfADigit < 0 || lastIndexOfADigit > formattedMeasure.length) {
                append(formattedMeasure)
            } else {
                if (firstIndexOfADigit > 0) {
                    withStyle(style = SpanStyle(fontSize = fontSize)) {
                        append(formattedMeasure.substring(0, firstIndexOfADigit))
                    }
                }
                append(formattedMeasure.substring(firstIndexOfADigit, lastIndexOfADigit + 1))
                if (lastIndexOfADigit < formattedMeasure.length) {
                    withStyle(style = SpanStyle(fontSize = fontSize)) {
                        append(formattedMeasure.substring(lastIndexOfADigit + 1))
                    }
                }
            }
        }
    }

    /**
     * Format a pollutant name so that the number are subscript
     * Units will stay at the same size if it somehow fails to parse
     */
    @Composable
    fun formatPollutantName(
        formattedMeasure: String,
    ): AnnotatedString {
        val firstIndexOfADigit = formattedMeasure.indexOfAny(DIGITS, 0)
        val lastIndexOfADigit = formattedMeasure.lastIndexOfAny(DIGITS, formattedMeasure.length - 1)
        return buildAnnotatedString {
            if (firstIndexOfADigit < 0 || lastIndexOfADigit < 0 || lastIndexOfADigit > formattedMeasure.length) {
                append(formattedMeasure)
            } else {
                if (firstIndexOfADigit > 0) {
                    append(formattedMeasure.substring(0, firstIndexOfADigit))
                }
                withStyle(style = SpanStyle(baselineShift = BaselineShift.Subscript, fontSize = 0.8.em)) {
                    append(formattedMeasure.substring(firstIndexOfADigit, lastIndexOfADigit + 1))
                }
                if (lastIndexOfADigit < formattedMeasure.length) {
                    append(formattedMeasure.substring(lastIndexOfADigit + 1))
                }
            }
        }
    }

    fun validatePercent(percent: Double?): Double? {
        return percent?.let { if (it in 0.0..100.0) it else null }
    }

    fun validatePercent(percent: Int?): Int? {
        return percent?.let { if (it in 0..100) it else null }
    }

    private val ARABIC_DIGITS = charArrayOf('0', '1', '2', '3', '4', '5', '6', '7', '8', '9')
    private val ARABIC_INDIC_DIGITS = charArrayOf('٠', '١', '٢', '٣', '٤', '٥', '٦', '٧', '٨', '٩')
    private val BENGALI_DIGITS = charArrayOf('০', '১', '২', '৩', '৪', '৫', '৬', '৭', '৮', '৯')
    private val DEVANAGARI_DIGITS = charArrayOf('०', '१', '२', '३', '४', '५', '६', '७', '८', '९')
    private val PERSIAN_DIGITS = charArrayOf('۰', '۱', '۲', '۳', '۴', '۵', '۶', '۷', '۸', '۹')
    private val TAMIL_DIGITS = charArrayOf('௦', '௧', '௨', '௩', '௪', '௫', '௬', '௭', '௮', '௯')
    internal val DIGITS =
        ARABIC_DIGITS + ARABIC_INDIC_DIGITS + BENGALI_DIGITS + DEVANAGARI_DIGITS + PERSIAN_DIGITS + TAMIL_DIGITS
}
