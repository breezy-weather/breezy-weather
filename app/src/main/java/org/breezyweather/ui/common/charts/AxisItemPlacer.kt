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

import com.patrykandpatrick.vico.core.cartesian.CartesianDrawingContext
import com.patrykandpatrick.vico.core.cartesian.CartesianMeasuringContext
import com.patrykandpatrick.vico.core.cartesian.axis.Axis
import com.patrykandpatrick.vico.core.cartesian.axis.HorizontalAxis
import com.patrykandpatrick.vico.core.cartesian.axis.VerticalAxis
import com.patrykandpatrick.vico.core.cartesian.layer.CartesianLayerDimensions
import kotlinx.collections.immutable.ImmutableList
import org.breezyweather.common.extensions.toTimezoneSpecificHour
import java.util.Date
import java.util.TimeZone
import kotlin.math.ceil
import kotlin.math.floor
import kotlin.time.Duration.Companion.hours
import kotlin.time.Duration.Companion.milliseconds

/**
 * Add steps every 6 hours, taking into account DST
 */
class TimeHorizontalAxisItemPlacer(
    theDay: Date,
    timeZone: TimeZone,
) : HorizontalAxis.ItemPlacer by HorizontalAxis.ItemPlacer.segmented(
    shiftExtremeLines = false
) {

    private val measuredValues = buildList {
        // add(theDay.time.toDouble())
        for (i in 6..18 step 6) {
            add(theDay.toTimezoneSpecificHour(timeZone, i).time.toDouble())
        }
    }

    override fun getLabelValues(
        context: CartesianDrawingContext,
        visibleXRange: ClosedFloatingPointRange<Double>,
        fullXRange: ClosedFloatingPointRange<Double>,
        maxLabelWidth: Float,
    ): List<Double> = measuredValues

    override fun getLineValues(
        context: CartesianDrawingContext,
        visibleXRange: ClosedFloatingPointRange<Double>,
        fullXRange: ClosedFloatingPointRange<Double>,
        maxLabelWidth: Float,
    ) = null
}

/**
 * Place ticks based on available space, and available points
 */
class TimeTopAxisItemPlacer(
    private val possibleValues: ImmutableList<Long>,
) : HorizontalAxis.ItemPlacer by HorizontalAxis.ItemPlacer.segmented(
    shiftExtremeLines = false
) {

    override fun getLabelValues(
        context: CartesianDrawingContext,
        visibleXRange: ClosedFloatingPointRange<Double>,
        fullXRange: ClosedFloatingPointRange<Double>,
        maxLabelWidth: Float,
    ): List<Double> {
        // Can be 23, 24 or 25 hours
        val nbHoursDisplayed = (context.ranges.maxX - context.ranges.minX).milliseconds.inWholeHours
        // Divide layer width by max label width to know how many labels can fit. For example 16
        val maxLabelsThatCanFit = floor(context.layerBounds.width().div(maxLabelWidth))
        // If there are 16 labels that can fit in a 24-hour chart, then a minimum of 2 hours is needed between hours
        val hoursToSkip = ceil(nbHoursDisplayed.div(maxLabelsThatCanFit)).coerceAtLeast(0f).toInt()

        val firstDisplayableLabel = (context.ranges.minX.milliseconds + (hoursToSkip - 1).coerceAtLeast(1).hours)
            .inWholeMilliseconds
        val lastDisplayableLabel = (context.ranges.maxX.milliseconds - (hoursToSkip - 1).coerceAtLeast(1).hours)
            .inWholeMilliseconds

        val labelsToDisplay = mutableListOf<Double>()

        var previousValue = 0L
        possibleValues
            .filter { it in firstDisplayableLabel..lastDisplayableLabel }
            .forEach { value ->
                if (value >= (previousValue + hoursToSkip.hours.inWholeMilliseconds)) {
                    labelsToDisplay.add(value.toDouble())
                    previousValue = value
                }
            }

        return labelsToDisplay
    }

    override fun getLineValues(
        context: CartesianDrawingContext,
        visibleXRange: ClosedFloatingPointRange<Double>,
        fullXRange: ClosedFloatingPointRange<Double>,
        maxLabelWidth: Float,
    ) = null

    override fun getWidthMeasurementLabelValues(
        context: CartesianMeasuringContext,
        layerDimensions: CartesianLayerDimensions,
        fullXRange: ClosedFloatingPointRange<Double>,
    ): List<Double> {
        return possibleValues.map { it.toDouble() }
    }
}

/**
 * @param measuredValues the values you would like to see on the X-axis
 */
class SpecificHorizontalAxisItemPlacer(
    private val measuredValues: List<Double>,
) : HorizontalAxis.ItemPlacer by HorizontalAxis.ItemPlacer.segmented(
    shiftExtremeLines = false
) {

    override fun getLabelValues(
        context: CartesianDrawingContext,
        visibleXRange: ClosedFloatingPointRange<Double>,
        fullXRange: ClosedFloatingPointRange<Double>,
        maxLabelWidth: Float,
    ): List<Double> = measuredValues

    override fun getLineValues(
        context: CartesianDrawingContext,
        visibleXRange: ClosedFloatingPointRange<Double>,
        fullXRange: ClosedFloatingPointRange<Double>,
        maxLabelWidth: Float,
    ) = null
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
