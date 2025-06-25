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

package org.breezyweather.ui.common.charts

import breezyweather.domain.location.model.Location
import com.patrykandpatrick.vico.core.cartesian.CartesianDrawingContext
import com.patrykandpatrick.vico.core.cartesian.axis.Axis
import com.patrykandpatrick.vico.core.cartesian.axis.HorizontalAxis
import com.patrykandpatrick.vico.core.cartesian.axis.VerticalAxis
import org.breezyweather.common.extensions.toCalendarWithTimeZone
import org.breezyweather.common.extensions.toTimezoneSpecificHour
import java.util.Calendar
import java.util.Date

/**
 * Add steps every 6 hours, taking into account DST
 */
class TimeHorizontalAxisItemPlacer(
    location: Location,
    theDay: Date,
) : HorizontalAxis.ItemPlacer by HorizontalAxis.ItemPlacer.aligned() {

    private val measuredValues = buildList {
        add(theDay.time.toDouble())
        for (i in 6..18 step 6) {
            add(theDay.toTimezoneSpecificHour(location.javaTimeZone, i).time.toDouble())
        }
        add(
            theDay.toCalendarWithTimeZone(location.javaTimeZone).apply {
                add(Calendar.DAY_OF_MONTH, 1)
            }.time.toTimezoneSpecificHour(location.javaTimeZone, 0).time.toDouble()
        )
    }

    override fun getLabelValues(
        context: CartesianDrawingContext,
        visibleXRange: ClosedFloatingPointRange<Double>,
        fullXRange: ClosedFloatingPointRange<Double>,
        maxLabelWidth: Float,
    ): List<Double> = measuredValues
}

/**
 * @param measuredValues the values you would like to see on the X-axis
 */
class SpecificHorizontalAxisItemPlacer(
    private val measuredValues: List<Double>,
) : HorizontalAxis.ItemPlacer by HorizontalAxis.ItemPlacer.aligned() {

    override fun getLabelValues(
        context: CartesianDrawingContext,
        visibleXRange: ClosedFloatingPointRange<Double>,
        fullXRange: ClosedFloatingPointRange<Double>,
        maxLabelWidth: Float,
    ): List<Double> = measuredValues
}

/**
 * @param measuredValues the values you would like to see on the Y-axis
 */
class SpecificVerticalAxisItemPlacer(
    private val measuredValues: List<Double>,
) : VerticalAxis.ItemPlacer by VerticalAxis.ItemPlacer.step() {

    override fun getLabelValues(
        context: CartesianDrawingContext,
        axisHeight: Float,
        maxLabelHeight: Float,
        position: Axis.Position.Vertical,
    ): List<Double> = measuredValues
}
