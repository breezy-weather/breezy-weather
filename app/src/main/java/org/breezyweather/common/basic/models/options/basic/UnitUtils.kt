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
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.TextUnit
import org.breezyweather.common.basic.models.options.unit.UnitWidth
import org.breezyweather.common.extensions.code
import org.breezyweather.common.extensions.currentLocale
import org.breezyweather.common.extensions.isChinese
import org.breezyweather.common.extensions.isRtl
import org.breezyweather.domain.settings.SettingsManager
import java.text.FieldPosition
import java.util.Locale
import kotlin.math.pow
import kotlin.math.roundToInt

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

    fun formatValue(
        context: Context,
        enum: UnitEnum<Double>,
        value: Double,
        precision: Int,
        isValueInDefaultUnit: Boolean = true,
    ) = if (precision == 0) {
        formatInt(context, (if (isValueInDefaultUnit) enum.getConvertedUnit(value) else value).roundToInt())
    } else {
        formatDouble(context, if (isValueInDefaultUnit) enum.getConvertedUnit(value) else value, precision)
    }

    fun formatDouble(context: Context, value: Double, precision: Int = 2): String {
        val factor = 10.0.pow(precision)
        return if (
            value.roundToInt() * factor == (value * factor).roundToInt().toDouble()
        ) {
            formatNumber(context, value.roundToInt(), 0)
        } else {
            formatNumber(context, value, precision)
        }
    }

    fun formatInt(context: Context, value: Int): String {
        return formatNumber(context, value, 0)
    }

    /**
     * Uses LocalizedNumberFormatter on Android SDK >= 30 (which is the recommended way)
     * Uses NumberFormat on Android SDK >= 24
     * Uses String.format() on Android SDK < 24
     */
    fun formatNumber(
        context: Context,
        valueWithoutUnit: Number,
        precision: Int,
    ): String {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            (NumberFormatter.withLocale(context.currentLocale) as LocalizedNumberFormatter)
                .precision(Precision.fixedFraction(precision))
                .format(valueWithoutUnit)
                .toString()
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            NumberFormat.getNumberInstance(context.currentLocale)
                .apply {
                    maximumFractionDigits = precision
                }
                .format(valueWithoutUnit)
                .toString()
        } else {
            if (precision == 0) {
                String.format(context.currentLocale, "%d", valueWithoutUnit)
            } else {
                String.format(context.currentLocale, "%." + precision + "f", valueWithoutUnit)
            }
        }
    }

    fun formatMeasure(
        context: Context,
        enum: UnitEnum<Double>,
        value: Double,
        precision: Int,
        isValueInDefaultUnit: Boolean = true,
        unitWidth: UnitWidth = UnitWidth.SHORT,
    ) = if (enum.measureUnit != null &&
        (enum.perMeasureUnit == null || Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) &&
        SettingsManager.getInstance(context).let { it.useNumberFormatter || it.useMeasureFormat }
    ) {
        // LogHelper.log(msg = "Formatting with ICU ${enum.id}: ${enum.measureUnit} per ${enum.perMeasureUnit}")
        formatWithIcu(
            context,
            if (isValueInDefaultUnit) enum.getConvertedUnit(value) else value,
            enum.measureUnit!!,
            enum.perMeasureUnit,
            precision,
            unitWidth
        )
    } else {
        // LogHelper.log(msg = "Not formatting with ICU ${enum.id} because measureUnit=${enum.measureUnit}")
        formatWithoutIcu(
            context,
            enum,
            formatValue(context, enum, value, precision, isValueInDefaultUnit),
            unitWidth
        )
    }

    /**
     * @param context
     * @param value
     * @param unit
     * @param perUnit an optional per unit. /!\ Only supported on Android SDK >= 26
     * @param unitWidth
     *
     * Uses LocalizedNumberFormatter on Android SDK >= 30 (which is the recommended way)
     * Uses MeasureFormat on Android SDK < 30
     */
    @RequiresApi(api = Build.VERSION_CODES.N)
    fun formatWithIcu(
        context: Context,
        value: Number,
        unit: MeasureUnit,
        perUnit: MeasureUnit? = null,
        precision: Int,
        unitWidth: UnitWidth,
    ): String {
        if (perUnit != null && Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
            throw UnsupportedOperationException()
        }

        val adjustedLocale = getAdjustedLocale(context.currentLocale, unit)

        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R &&
            SettingsManager.getInstance(context).useNumberFormatter
        ) {
            (NumberFormatter.withLocale(adjustedLocale) as LocalizedNumberFormatter)
                .precision(if (precision == 0) Precision.integer() else Precision.maxFraction(precision))
                .unit(unit)
                .perUnit(perUnit)
                .unitWidth(unitWidth.numberFormatterWidth)
                .format(value)
                .toString()
        } else {
            MeasureFormat
                .getInstance(
                    adjustedLocale,
                    unitWidth.measureFormatWidth,
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
    }

    /**
     * Some ICU translations are not really good, replacing with others locales
     */
    @RequiresApi(Build.VERSION_CODES.N)
    private fun getAdjustedLocale(
        currentLocale: Locale,
        unit: MeasureUnit,
    ): Locale {
        /**
         * Beaufort scale:
         * - Chinese (traditional) is verbose but not Chinese (simplified), so using it instead
         * - Ukrainian is verbose but not Russian, so using it instead
         */
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.BAKLAVA && unit == MeasureUnit.BEAUFORT) {
            if (currentLocale.isChinese) return Locale("zh", "CN")
            if (currentLocale.code.startsWith("uk")) return Locale("ru")
        }

        /**
         * Kelvin:
         * - Chinese (traditional) is spelled, while it’s just a K in Chinese (simplified), so using it instead
         */
        if (unit == MeasureUnit.KELVIN && currentLocale.isChinese) return Locale("zh", "CN")

        return currentLocale
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

    private val ARABIC_DIGITS = charArrayOf('0', '1', '2', '3', '4', '5', '6', '7', '8', '9')
    private val ARABIC_INDIC_DIGITS = charArrayOf('٠', '١', '٢', '٣', '٤', '٥', '٦', '٧', '٨', '٩')
    private val BENGALI_DIGITS = charArrayOf('০', '১', '২', '৩', '৪', '৫', '৬', '৭', '৮', '৯')
    private val DEVANAGARI_DIGITS = charArrayOf('०', '१', '२', '३', '४', '५', '६', '७', '८', '९')
    private val PERSIAN_DIGITS = charArrayOf('۰', '۱', '۲', '۳', '۴', '۵', '۶', '۷', '۸', '۹')
    private val TAMIL_DIGITS = charArrayOf('௦', '௧', '௨', '௩', '௪', '௫', '௬', '௭', '௮', '௯')
    internal val DIGITS =
        ARABIC_DIGITS + ARABIC_INDIC_DIGITS + BENGALI_DIGITS + DEVANAGARI_DIGITS + PERSIAN_DIGITS + TAMIL_DIGITS
}
