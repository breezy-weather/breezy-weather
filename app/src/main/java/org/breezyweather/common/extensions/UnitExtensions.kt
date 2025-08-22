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

package org.breezyweather.common.extensions

import android.content.Context
import android.graphics.Color
import androidx.annotation.ColorInt
import androidx.core.content.ContextCompat
import org.breezyweather.R
import org.breezyweather.domain.settings.SettingsManager
import org.breezyweather.unit.distance.Distance
import org.breezyweather.unit.distance.Distance.Companion.meters
import org.breezyweather.unit.duration.format
import org.breezyweather.unit.formatting.UnitWidth
import org.breezyweather.unit.pollen.PollenConcentrationUnit
import org.breezyweather.unit.pollutant.PollutantConcentrationUnit
import org.breezyweather.unit.precipitation.Precipitation
import org.breezyweather.unit.precipitation.PrecipitationUnit
import org.breezyweather.unit.pressure.Pressure
import org.breezyweather.unit.ratio.Ratio
import org.breezyweather.unit.ratio.RatioUnit
import org.breezyweather.unit.speed.Speed
import org.breezyweather.unit.temperature.Temperature
import org.breezyweather.unit.temperature.TemperatureUnit
import kotlin.time.Duration
import kotlin.time.DurationUnit

/**
 * TODO: Lot of duplicates code in this page
 *  Technically, we can do a <T : WeatherValue> extension, but we need to handle how we are getting the user-preferred
 *  unit
 */

/**
 * Convenient format function with parameters filled for our app
 * Getting the default unit from Settings is terribly slow, so it must be send as parameter
 */
fun Temperature.formatMeasure(
    context: Context,
    unit: TemperatureUnit,
    valueWidth: UnitWidth = UnitWidth.SHORT,
    unitWidth: UnitWidth = UnitWidth.SHORT,
    showSign: Boolean = false,
): String {
    val settings = SettingsManager.getInstance(context)
    return format(
        context = context,
        unit = unit,
        valueWidth = valueWidth,
        unitWidth = unitWidth,
        locale = context.currentLocale,
        showSign = showSign,
        useNumberFormatter = settings.useNumberFormatter,
        useMeasureFormat = settings.useMeasureFormat
    )
}

/**
 * Convenient format function with parameters filled for our app
 * Getting the default unit from Settings is terribly slow, so it must be send as parameter
 */
fun Temperature.formatValue(
    context: Context,
    unit: TemperatureUnit,
    width: UnitWidth = UnitWidth.SHORT,
): String {
    val settings = SettingsManager.getInstance(context)
    return formatValue(
        unit = unit,
        width = width,
        locale = context.currentLocale,
        useNumberFormatter = settings.useNumberFormatter,
        useMeasureFormat = settings.useMeasureFormat
    )
}

/**
 * Source: https://weather.metoffice.gov.uk/guides/what-does-this-forecast-mean
 */
const val VISIBILITY_VERY_POOR = 1000.0
const val VISIBILITY_POOR = 4000.0
const val VISIBILITY_MODERATE = 10000.0
const val VISIBILITY_GOOD = 20000.0
const val VISIBILITY_CLEAR = 40000.0

val visibilityScaleThresholds = listOf(
    0.meters,
    VISIBILITY_VERY_POOR.meters,
    VISIBILITY_POOR.meters,
    VISIBILITY_MODERATE.meters,
    VISIBILITY_GOOD.meters,
    VISIBILITY_CLEAR.meters
)

/**
 * @param context
 */
fun Distance.getVisibilityDescription(context: Context): String? {
    return when (inMeters) {
        in 0.0..<VISIBILITY_VERY_POOR -> context.getString(R.string.visibility_very_poor)
        in VISIBILITY_VERY_POOR..<VISIBILITY_POOR -> context.getString(R.string.visibility_poor)
        in VISIBILITY_POOR..<VISIBILITY_MODERATE -> context.getString(R.string.visibility_moderate)
        in VISIBILITY_MODERATE..<VISIBILITY_GOOD -> context.getString(R.string.visibility_good)
        in VISIBILITY_GOOD..<VISIBILITY_CLEAR -> context.getString(R.string.visibility_clear)
        in VISIBILITY_CLEAR..Double.MAX_VALUE -> context.getString(R.string.visibility_perfectly_clear)
        else -> null
    }
}

/**
 * Convenient format function with parameters filled for our app
 */
fun Distance.formatMeasure(
    context: Context,
    valueWidth: UnitWidth = UnitWidth.SHORT,
    unitWidth: UnitWidth = UnitWidth.SHORT,
): String {
    val settings = SettingsManager.getInstance(context)
    return format(
        context = context,
        unit = settings.getDistanceUnit(context),
        valueWidth = valueWidth,
        unitWidth = unitWidth,
        locale = context.currentLocale,
        useNumberFormatter = settings.useNumberFormatter,
        useMeasureFormat = settings.useMeasureFormat
    )
}

/**
 * Convenient format function with parameters filled for our app
 */
fun Distance.formatValue(
    context: Context,
    width: UnitWidth = UnitWidth.SHORT,
): String {
    val settings = SettingsManager.getInstance(context)
    return formatValue(
        unit = settings.getDistanceUnit(context),
        width = width,
        locale = context.currentLocale,
        useNumberFormatter = settings.useNumberFormatter,
        useMeasureFormat = settings.useMeasureFormat
    )
}

fun Speed.getBeaufortScaleStrength(context: Context): String? {
    return context.resources.getStringArray(R.array.wind_strength_descriptions).getOrElse(inBeaufort) { null }
}

@ColorInt
fun Speed.getBeaufortScaleColor(context: Context): Int {
    return context.resources.getIntArray(R.array.wind_strength_colors).getOrNull(inBeaufort) ?: Color.TRANSPARENT
}

/**
 * Convenient format function with parameters filled for our app
 */
fun Speed.formatMeasure(
    context: Context,
    valueWidth: UnitWidth = UnitWidth.SHORT,
    unitWidth: UnitWidth = UnitWidth.SHORT,
): String {
    val settings = SettingsManager.getInstance(context)
    return format(
        context = context,
        unit = settings.getSpeedUnit(context),
        valueWidth = valueWidth,
        unitWidth = unitWidth,
        locale = context.currentLocale,
        useNumberFormatter = settings.useNumberFormatter,
        useMeasureFormat = settings.useMeasureFormat
    )
}

/**
 * Convenient format function with parameters filled for our app
 */
fun Speed.formatValue(
    context: Context,
    width: UnitWidth = UnitWidth.SHORT,
): String {
    val settings = SettingsManager.getInstance(context)
    return formatValue(
        unit = settings.getSpeedUnit(context),
        width = width,
        locale = context.currentLocale,
        useNumberFormatter = settings.useNumberFormatter,
        useMeasureFormat = settings.useMeasureFormat
    )
}

/**
 * Convenient format function with parameters filled for our app
 */
fun Precipitation.formatMeasure(
    context: Context,
    unit: PrecipitationUnit = SettingsManager.getInstance(context).getPrecipitationUnit(context),
    valueWidth: UnitWidth = UnitWidth.SHORT,
    unitWidth: UnitWidth = UnitWidth.SHORT,
): String {
    val settings = SettingsManager.getInstance(context)
    return format(
        context = context,
        unit = unit,
        valueWidth = valueWidth,
        unitWidth = unitWidth,
        locale = context.currentLocale,
        useNumberFormatter = settings.useNumberFormatter,
        useMeasureFormat = settings.useMeasureFormat
    )
}

/**
 * Convenient format function with parameters filled for our app
 */
fun Precipitation.formatValue(
    context: Context,
    unit: PrecipitationUnit = SettingsManager.getInstance(context).getPrecipitationUnit(context),
    width: UnitWidth = UnitWidth.SHORT,
): String {
    val settings = SettingsManager.getInstance(context)
    return formatValue(
        unit = unit,
        width = width,
        locale = context.currentLocale,
        useNumberFormatter = settings.useNumberFormatter,
        useMeasureFormat = settings.useMeasureFormat
    )
}

/**
 * Convenient format function with parameters filled for our app
 */
fun Precipitation.formatMeasureIntensity(
    context: Context,
    valueWidth: UnitWidth = UnitWidth.SHORT,
    unitWidth: UnitWidth = UnitWidth.SHORT,
): String {
    val settings = SettingsManager.getInstance(context)
    return formatIntensity(
        context = context,
        unit = settings.getPrecipitationUnit(context),
        valueWidth = valueWidth,
        unitWidth = unitWidth,
        locale = context.currentLocale,
        useNumberFormatter = settings.useNumberFormatter,
        useMeasureFormat = settings.useMeasureFormat
    )
}

/**
 * Convenient format function with parameters filled for our app
 */
fun Pressure.formatMeasure(
    context: Context,
    valueWidth: UnitWidth = UnitWidth.SHORT,
    unitWidth: UnitWidth = UnitWidth.SHORT,
): String {
    val settings = SettingsManager.getInstance(context)
    return format(
        context = context,
        unit = settings.getPressureUnit(context),
        valueWidth = valueWidth,
        unitWidth = unitWidth,
        locale = context.currentLocale,
        useNumberFormatter = settings.useNumberFormatter,
        useMeasureFormat = settings.useMeasureFormat
    )
}

/**
 * Convenient format function with parameters filled for our app
 */
fun Pressure.formatValue(
    context: Context,
    width: UnitWidth = UnitWidth.SHORT,
): String {
    val settings = SettingsManager.getInstance(context)
    return formatValue(
        unit = settings.getPressureUnit(context),
        width = width,
        locale = context.currentLocale,
        useNumberFormatter = settings.useNumberFormatter,
        useMeasureFormat = settings.useMeasureFormat
    )
}

/**
 * Convenient format function with parameters filled for our app
 */
fun PollutantConcentrationUnit.formatMeasure(
    context: Context,
    value: Number,
    valueWidth: UnitWidth = UnitWidth.SHORT,
    unitWidth: UnitWidth = UnitWidth.SHORT,
): String {
    val settings = SettingsManager.getInstance(context)
    return format(
        context = context,
        value = value,
        valueWidth = valueWidth,
        unitWidth = unitWidth,
        locale = context.currentLocale,
        useNumberFormatter = settings.useNumberFormatter,
        useMeasureFormat = settings.useMeasureFormat
    )
}

/**
 * Convenient format function with parameters filled for our app
 */
fun PollenConcentrationUnit.formatMeasure(
    context: Context,
    value: Number,
    valueWidth: UnitWidth = UnitWidth.SHORT,
    unitWidth: UnitWidth = UnitWidth.SHORT,
): String {
    val settings = SettingsManager.getInstance(context)
    return format(
        context = context,
        value = value,
        valueWidth = valueWidth,
        unitWidth = unitWidth,
        locale = context.currentLocale,
        useNumberFormatter = settings.useNumberFormatter,
        useMeasureFormat = settings.useMeasureFormat
    )
}

/**
 * Convenient format function with parameters filled for our app
 */
fun Duration.formatTime(
    context: Context,
    smallestUnit: DurationUnit = DurationUnit.HOURS,
    valueWidth: UnitWidth = UnitWidth.SHORT,
    unitWidth: UnitWidth = UnitWidth.SHORT,
): String {
    val settings = SettingsManager.getInstance(context)
    return format(
        context = context,
        unit = DurationUnit.HOURS,
        smallestUnit = smallestUnit,
        valueWidth = valueWidth,
        unitWidth = unitWidth,
        locale = context.currentLocale,
        useNumberFormatter = settings.useNumberFormatter,
        useMeasureFormat = settings.useMeasureFormat
    )
}

/**
 * Convenient format function with parameters filled for our app
 */
fun Ratio.formatPercent(
    context: Context,
    valueWidth: UnitWidth = UnitWidth.SHORT,
): String {
    val settings = SettingsManager.getInstance(context)
    return format(
        context = context,
        unit = RatioUnit.PERCENT,
        valueWidth = valueWidth,
        locale = context.currentLocale,
        useNumberFormatter = settings.useNumberFormatter,
        useMeasureFormat = settings.useMeasureFormat
    )
}

/**
 * Convenient format function with parameters filled for our app
 */
fun Ratio.formatValue(
    context: Context,
    width: UnitWidth = UnitWidth.SHORT,
): String {
    val settings = SettingsManager.getInstance(context)
    return formatValue(
        unit = RatioUnit.PERCENT,
        width = width,
        locale = context.currentLocale,
        useNumberFormatter = settings.useNumberFormatter,
        useMeasureFormat = settings.useMeasureFormat
    )
}

// We don't need any cloud cover unit, it's just a percent, but we need some helpers

/**
 * Source: WMO Cloud distribution for aviation
 */
const val CLOUD_COVER_SKC = 12.5 // 1 okta
const val CLOUD_COVER_FEW = 37.5 // 3 okta
const val CLOUD_COVER_SCT = 67.5 // 5 okta
const val CLOUD_COVER_BKN = 87.5 // 7 okta
const val CLOUD_COVER_OVC = 100.0 // 8 okta

fun Ratio.getCloudCoverColor(context: Context): Int {
    return when (inPercent) {
        in 0.0..<CLOUD_COVER_FEW -> ContextCompat.getColor(context, R.color.colorLevel_1)
        in CLOUD_COVER_FEW..CLOUD_COVER_SCT -> ContextCompat.getColor(context, R.color.colorLevel_2)
        in CLOUD_COVER_SCT..100.0 -> ContextCompat.getColor(context, R.color.colorLevel_3)
        else -> Color.TRANSPARENT
    }
}

/**
 * @param context
 */
fun Ratio.getCloudCoverDescription(context: Context): String? {
    return when (inPercent) {
        in 0.0..<CLOUD_COVER_SKC -> context.getString(R.string.common_weather_text_clear_sky)
        in CLOUD_COVER_SKC..<CLOUD_COVER_FEW -> context.getString(R.string.common_weather_text_mostly_clear)
        in CLOUD_COVER_FEW..<CLOUD_COVER_SCT -> context.getString(R.string.common_weather_text_partly_cloudy)
        in CLOUD_COVER_SCT..<CLOUD_COVER_BKN -> context.getString(R.string.common_weather_text_mostly_cloudy)
        in CLOUD_COVER_BKN..CLOUD_COVER_OVC -> context.getString(R.string.common_weather_text_cloudy)
        else -> null
    }
}
