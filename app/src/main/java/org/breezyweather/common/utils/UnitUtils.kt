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

package org.breezyweather.common.utils

import android.content.Context
import android.content.res.Resources
import android.text.SpannableString
import android.text.Spanned
import android.text.style.RelativeSizeSpan
import androidx.annotation.ArrayRes
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.BaselineShift
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.em
import org.breezyweather.common.extensions.currentLocale
import org.breezyweather.common.options.BaseEnum
import org.breezyweather.domain.settings.SettingsManager
import org.breezyweather.unit.formatting.format

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
            useNumberFormatter = SettingsManager.Companion.getInstance(context).useNumberFormatter,
            useMeasureFormat = SettingsManager.Companion.getInstance(context).useMeasureFormat
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
            useNumberFormatter = SettingsManager.Companion.getInstance(context).useNumberFormatter,
            useMeasureFormat = SettingsManager.Companion.getInstance(context).useMeasureFormat
        )
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
                        Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
                    )
                }
                if (lastIndexOfADigit < formattedMeasure.length) {
                    setSpan(
                        RelativeSizeSpan(0.5f),
                        lastIndexOfADigit + 1,
                        formattedMeasure.length,
                        Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
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
                withStyle(
                    style = SpanStyle(
                        baselineShift = BaselineShift.Companion.Subscript,
                        fontSize = 0.8.em
                    )
                ) {
                    append(formattedMeasure.substring(firstIndexOfADigit, lastIndexOfADigit + 1))
                }
                if (lastIndexOfADigit < formattedMeasure.length) {
                    append(formattedMeasure.substring(lastIndexOfADigit + 1))
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
