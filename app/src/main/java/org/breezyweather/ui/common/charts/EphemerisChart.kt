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

import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.unit.dp
import androidx.core.graphics.ColorUtils
import breezyweather.domain.location.model.Location
import com.patrykandpatrick.vico.compose.cartesian.CartesianChartHost
import com.patrykandpatrick.vico.compose.cartesian.axis.rememberAxisGuidelineComponent
import com.patrykandpatrick.vico.compose.cartesian.axis.rememberAxisLabelComponent
import com.patrykandpatrick.vico.compose.cartesian.axis.rememberAxisLineComponent
import com.patrykandpatrick.vico.compose.cartesian.axis.rememberAxisTickComponent
import com.patrykandpatrick.vico.compose.cartesian.axis.rememberBottom
import com.patrykandpatrick.vico.compose.cartesian.axis.rememberEnd
import com.patrykandpatrick.vico.compose.cartesian.layer.continuous
import com.patrykandpatrick.vico.compose.cartesian.layer.rememberLine
import com.patrykandpatrick.vico.compose.cartesian.layer.rememberLineCartesianLayer
import com.patrykandpatrick.vico.compose.cartesian.rememberCartesianChart
import com.patrykandpatrick.vico.compose.cartesian.rememberVicoScrollState
import com.patrykandpatrick.vico.compose.common.fill
import com.patrykandpatrick.vico.core.cartesian.axis.HorizontalAxis
import com.patrykandpatrick.vico.core.cartesian.axis.VerticalAxis
import com.patrykandpatrick.vico.core.cartesian.data.CartesianChartModelProducer
import com.patrykandpatrick.vico.core.cartesian.data.CartesianLayerRangeProvider
import com.patrykandpatrick.vico.core.cartesian.data.CartesianValueFormatter
import com.patrykandpatrick.vico.core.cartesian.layer.LineCartesianLayer
import kotlinx.collections.immutable.ImmutableList
import org.breezyweather.R
import org.breezyweather.common.extensions.getFormattedTime
import org.breezyweather.common.extensions.getThemeColor
import org.breezyweather.common.extensions.handleNestedHorizontalDragGesture
import org.breezyweather.common.extensions.is12Hour
import org.breezyweather.common.extensions.isRtl
import org.breezyweather.common.extensions.toDate
import org.breezyweather.common.extensions.windowHeightInDp
import org.breezyweather.ui.theme.ThemeManager
import java.util.Date
import kotlin.math.PI
import kotlin.math.max
import kotlin.math.roundToInt

/**
 * A line chart with time as X-Axis and a formatted value on Y-Axis
 *
 * @param location The location
 * @param modelProducer Model producer containing the data
 * @param endAxisValueFormatter value formatter for the right side
 */
@Composable
fun EphemerisChart(
    location: Location,
    modelProducer: CartesianChartModelProducer,
    endAxisValueFormatter: CartesianValueFormatter,
    lineColors: ImmutableList<Int>,
    startingDate: Long,
    endingDate: Long,
    modifier: Modifier = Modifier,
) {
    val view = LocalView.current
    val context = LocalContext.current

    // 90 is the zenith, but add a safe-zone
    val cartesianLayerRangeProvider = CartesianLayerRangeProvider.fixed(
        minX = startingDate.toDouble(),
        maxX = endingDate.toDouble(),
        minY = -90.0,
        maxY = 90.0
    )

    val timeValueFormatter = CartesianValueFormatter { _, value, _ ->
        Date(value.toLong()).getFormattedTime(location, context, context.is12Hour)
    }

    val lineColor = Color(context.getThemeColor(com.google.android.material.R.attr.colorOutline))
    val labelColor = colorResource(
        if (ThemeManager.isLightTheme(context, location)) R.color.colorTextGrey else R.color.colorTextGrey2nd
    )

    CartesianChartHost(
        rememberCartesianChart(
            rememberLineCartesianLayer(
                LineCartesianLayer.LineProvider.series(
                    lineColors.map { fillColor ->
                        LineCartesianLayer.rememberLine(
                            fill = ScaleLineFill(
                                mapOf(
                                    0.1f to Color(fillColor),
                                    -0.1f to Color(
                                        ColorUtils.setAlphaComponent(fillColor, (255 * 0.5).roundToInt())
                                    )
                                )
                            ),
                            stroke = LineCartesianLayer.LineStroke.continuous(LINE_THICKNESS_DP.dp),
                            areaFill = ScaleAreaFill(
                                mapOf(
                                    0.1f to Color(fillColor),
                                    -0.1f to Color.Transparent
                                ),
                                context.isRtl
                            ),
                            pointConnector = LineCartesianLayer.PointConnector.cubic(
                                curvature = ((PI - 2.0) / PI).toFloat()
                            )
                        )
                    }
                ),
                rangeProvider = cartesianLayerRangeProvider
            ),
            endAxis = VerticalAxis.rememberEnd(
                line = rememberAxisLineComponent(fill = fill(lineColor)),
                label = rememberAxisLabelComponent(color = labelColor),
                valueFormatter = endAxisValueFormatter,
                tick = rememberAxisTickComponent(fill = fill(lineColor)),
                guideline = rememberAxisGuidelineComponent(fill = fill(lineColor)),
                itemPlacer = remember {
                    SpecificVerticalAxisItemPlacer(listOf(0.0, 90.0))
                }
            ),
            bottomAxis = HorizontalAxis.rememberBottom(
                line = rememberAxisLineComponent(fill = fill(lineColor)),
                label = rememberAxisLabelComponent(color = labelColor),
                valueFormatter = timeValueFormatter,
                tick = rememberAxisTickComponent(fill = fill(lineColor)),
                guideline = rememberAxisGuidelineComponent(fill = fill(lineColor)),
                itemPlacer = remember {
                    TimeHorizontalAxisItemPlacer(location, startingDate.toDate())
                }
            )
        ),
        modelProducer = modelProducer,
        scrollState = rememberVicoScrollState(scrollEnabled = false),
        modifier = modifier
            .height(max(LINE_CHART_HEIGHT_MIN_DP.toFloat(), context.windowHeightInDp.div(4)).dp)
            .handleNestedHorizontalDragGesture(view)
    )
}

private const val LINE_CHART_HEIGHT_MIN_DP = 100
private const val LINE_THICKNESS_DP = 5f
