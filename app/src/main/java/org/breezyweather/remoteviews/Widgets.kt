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

package org.breezyweather.remoteviews

import android.content.Context
import android.text.TextPaint
import breezyweather.domain.weather.model.Weather
import org.breezyweather.common.extensions.formatMeasure
import org.breezyweather.unit.formatting.UnitWidth
import org.breezyweather.unit.temperature.TemperatureUnit

object Widgets {

    // day.
    const val DAY_PENDING_INTENT_CODE_WEATHER = 11
    const val DAY_PENDING_INTENT_CODE_CALENDAR = 13

    // week.
    const val WEEK_PENDING_INTENT_CODE_WEATHER = 21
    const val WEEK_PENDING_INTENT_CODE_DAILY_FORECAST_1 = 211
    const val WEEK_PENDING_INTENT_CODE_DAILY_FORECAST_2 = 212
    const val WEEK_PENDING_INTENT_CODE_DAILY_FORECAST_3 = 213
    const val WEEK_PENDING_INTENT_CODE_DAILY_FORECAST_4 = 214
    const val WEEK_PENDING_INTENT_CODE_DAILY_FORECAST_5 = 215

    // day + week.
    const val DAY_WEEK_PENDING_INTENT_CODE_WEATHER = 31
    const val DAY_WEEK_PENDING_INTENT_CODE_DAILY_FORECAST_1 = 311
    const val DAY_WEEK_PENDING_INTENT_CODE_DAILY_FORECAST_2 = 312
    const val DAY_WEEK_PENDING_INTENT_CODE_DAILY_FORECAST_3 = 313
    const val DAY_WEEK_PENDING_INTENT_CODE_DAILY_FORECAST_4 = 314
    const val DAY_WEEK_PENDING_INTENT_CODE_DAILY_FORECAST_5 = 315
    const val DAY_WEEK_PENDING_INTENT_CODE_CALENDAR = 33

    // clock + day (vertical).
    const val CLOCK_DAY_VERTICAL_PENDING_INTENT_CODE_WEATHER = 41
    const val CLOCK_DAY_VERTICAL_PENDING_INTENT_CODE_CLOCK_LIGHT = 43
    const val CLOCK_DAY_VERTICAL_PENDING_INTENT_CODE_CLOCK_NORMAL = 44
    const val CLOCK_DAY_VERTICAL_PENDING_INTENT_CODE_CLOCK_BLACK = 45
    const val CLOCK_DAY_VERTICAL_PENDING_INTENT_CODE_CLOCK_1_LIGHT = 46
    const val CLOCK_DAY_VERTICAL_PENDING_INTENT_CODE_CLOCK_2_LIGHT = 47
    const val CLOCK_DAY_VERTICAL_PENDING_INTENT_CODE_CLOCK_1_NORMAL = 48
    const val CLOCK_DAY_VERTICAL_PENDING_INTENT_CODE_CLOCK_2_NORMAL = 49
    const val CLOCK_DAY_VERTICAL_PENDING_INTENT_CODE_CLOCK_1_BLACK = 50
    const val CLOCK_DAY_VERTICAL_PENDING_INTENT_CODE_CLOCK_2_BLACK = 51

    // clock + day (horizontal).
    const val CLOCK_DAY_HORIZONTAL_PENDING_INTENT_CODE_WEATHER = 61
    const val CLOCK_DAY_HORIZONTAL_PENDING_INTENT_CODE_CALENDAR = 63
    const val CLOCK_DAY_HORIZONTAL_PENDING_INTENT_CODE_CLOCK_LIGHT = 64
    const val CLOCK_DAY_HORIZONTAL_PENDING_INTENT_CODE_CLOCK_NORMAL = 65
    const val CLOCK_DAY_HORIZONTAL_PENDING_INTENT_CODE_CLOCK_BLACK = 66

    // clock + day + details.
    const val CLOCK_DAY_DETAILS_PENDING_INTENT_CODE_WEATHER = 71
    const val CLOCK_DAY_DETAILS_PENDING_INTENT_CODE_CALENDAR = 73
    const val CLOCK_DAY_DETAILS_PENDING_INTENT_CODE_CLOCK_LIGHT = 74
    const val CLOCK_DAY_DETAILS_PENDING_INTENT_CODE_CLOCK_NORMAL = 75
    const val CLOCK_DAY_DETAILS_PENDING_INTENT_CODE_CLOCK_BLACK = 76

    // clock + day + week.
    const val CLOCK_DAY_WEEK_PENDING_INTENT_CODE_WEATHER = 81
    const val CLOCK_DAY_WEEK_PENDING_INTENT_CODE_DAILY_FORECAST_1 = 821
    const val CLOCK_DAY_WEEK_PENDING_INTENT_CODE_DAILY_FORECAST_2 = 822
    const val CLOCK_DAY_WEEK_PENDING_INTENT_CODE_DAILY_FORECAST_3 = 823
    const val CLOCK_DAY_WEEK_PENDING_INTENT_CODE_DAILY_FORECAST_4 = 824
    const val CLOCK_DAY_WEEK_PENDING_INTENT_CODE_DAILY_FORECAST_5 = 825
    const val CLOCK_DAY_WEEK_PENDING_INTENT_CODE_CALENDAR = 83
    const val CLOCK_DAY_WEEK_PENDING_INTENT_CODE_CLOCK_LIGHT = 84
    const val CLOCK_DAY_WEEK_PENDING_INTENT_CODE_CLOCK_NORMAL = 85
    const val CLOCK_DAY_WEEK_PENDING_INTENT_CODE_CLOCK_BLACK = 86

    // text.
    const val TEXT_PENDING_INTENT_CODE_WEATHER = 91
    const val TEXT_PENDING_INTENT_CODE_CALENDAR = 93

    // trend daily.
    const val TREND_DAILY_PENDING_INTENT_CODE_WEATHER = 101

    // trend hourly.
    const val TREND_HOURLY_PENDING_INTENT_CODE_WEATHER = 111

    // multi city.
    const val MULTI_CITY_PENDING_INTENT_CODE_WEATHER_1 = 121
    const val MULTI_CITY_PENDING_INTENT_CODE_WEATHER_2 = 123
    const val MULTI_CITY_PENDING_INTENT_CODE_WEATHER_3 = 125

    // material you.
    const val MATERIAL_YOU_FORECAST_PENDING_INTENT_CODE_WEATHER = 131
    const val MATERIAL_YOU_CURRENT_PENDING_INTENT_CODE_WEATHER = 132

    fun buildWidgetDayStyleText(context: Context, weather: Weather, temperatureUnit: TemperatureUnit): Array<String> {
        val texts = arrayOf(
            weather.current?.weatherText ?: "",
            weather.current?.temperature?.temperature?.formatMeasure(
                context,
                temperatureUnit,
                unitWidth = UnitWidth.NARROW
            ) ?: "",
            weather.today?.day?.temperature?.temperature?.formatMeasure(
                context,
                temperatureUnit,
                valueWidth = UnitWidth.NARROW,
                unitWidth = UnitWidth.NARROW
            ) ?: "",
            weather.today?.night?.temperature?.temperature?.formatMeasure(
                context,
                temperatureUnit,
                valueWidth = UnitWidth.NARROW,
                unitWidth = UnitWidth.NARROW
            ) ?: ""
        )
        val paint = TextPaint()
        val widths = FloatArray(4)
        for (i in widths.indices) {
            widths[i] = paint.measureText(texts[i])
        }
        var maxiWidth = widths[0]
        for (w in widths) {
            if (w > maxiWidth) {
                maxiWidth = w
            }
        }
        while (true) {
            val flags = booleanArrayOf(false, false, false, false)
            for (i in 0..1) {
                if (widths[i] < maxiWidth) {
                    texts[i] = texts[i] + " "
                    widths[i] = paint.measureText(texts[i])
                } else {
                    flags[i] = true
                }
            }
            for (i in 2..3) {
                if (widths[i] < maxiWidth) {
                    texts[i] = " " + texts[i]
                    widths[i] = paint.measureText(texts[i])
                } else {
                    flags[i] = true
                }
            }
            var n = 0
            for (flag in flags) {
                if (flag) {
                    n++
                }
            }
            if (n == 4) {
                break
            }
        }
        return arrayOf(
            texts[0] + "\n" + texts[1],
            texts[2] + "\n" + texts[3]
        )
    }
}
