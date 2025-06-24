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

import android.graphics.Paint
import android.graphics.Path
import android.graphics.RectF
import android.graphics.Shader
import android.text.Layout
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.LinearGradientShader
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.unit.dp
import breezyweather.domain.location.model.Location
import com.patrykandpatrick.vico.compose.cartesian.CartesianChartHost
import com.patrykandpatrick.vico.compose.cartesian.axis.rememberBottom
import com.patrykandpatrick.vico.compose.cartesian.axis.rememberEnd
import com.patrykandpatrick.vico.compose.cartesian.axis.rememberTop
import com.patrykandpatrick.vico.compose.cartesian.layer.continuous
import com.patrykandpatrick.vico.compose.cartesian.layer.rememberLine
import com.patrykandpatrick.vico.compose.cartesian.layer.rememberLineCartesianLayer
import com.patrykandpatrick.vico.compose.cartesian.marker.rememberDefaultCartesianMarker
import com.patrykandpatrick.vico.compose.cartesian.rememberCartesianChart
import com.patrykandpatrick.vico.compose.cartesian.rememberVicoScrollState
import com.patrykandpatrick.vico.compose.common.component.rememberLineComponent
import com.patrykandpatrick.vico.compose.common.component.rememberShapeComponent
import com.patrykandpatrick.vico.compose.common.component.rememberTextComponent
import com.patrykandpatrick.vico.compose.common.fill
import com.patrykandpatrick.vico.compose.common.insets
import com.patrykandpatrick.vico.core.cartesian.CartesianDrawingContext
import com.patrykandpatrick.vico.core.cartesian.axis.Axis
import com.patrykandpatrick.vico.core.cartesian.axis.HorizontalAxis
import com.patrykandpatrick.vico.core.cartesian.axis.VerticalAxis
import com.patrykandpatrick.vico.core.cartesian.data.CartesianChartModelProducer
import com.patrykandpatrick.vico.core.cartesian.data.CartesianLayerRangeProvider
import com.patrykandpatrick.vico.core.cartesian.data.CartesianValueFormatter
import com.patrykandpatrick.vico.core.cartesian.decoration.HorizontalLine
import com.patrykandpatrick.vico.core.cartesian.layer.LineCartesianLayer
import com.patrykandpatrick.vico.core.cartesian.marker.CartesianMarkerVisibilityListener
import com.patrykandpatrick.vico.core.cartesian.marker.DefaultCartesianMarker
import com.patrykandpatrick.vico.core.common.Fill
import com.patrykandpatrick.vico.core.common.Position
import com.patrykandpatrick.vico.core.common.component.Shadow
import com.patrykandpatrick.vico.core.common.component.TextComponent
import com.patrykandpatrick.vico.core.common.shape.CorneredShape
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.ImmutableMap
import kotlinx.collections.immutable.persistentMapOf
import org.breezyweather.R
import org.breezyweather.common.extensions.getFormattedTime
import org.breezyweather.common.extensions.handleNestedHorizontalDragGesture
import org.breezyweather.common.extensions.is12Hour
import org.breezyweather.common.extensions.toCalendarWithTimeZone
import org.breezyweather.common.extensions.toTimezoneSpecificHour
import org.breezyweather.common.extensions.windowHeightInDp
import org.breezyweather.domain.settings.SettingsManager
import org.breezyweather.ui.main.utils.MainThemeColorProvider
import java.util.Calendar
import java.util.Date
import kotlin.math.max

/**
 * A line chart with time as X-Axis and a formatted value on Y-Axis
 *
 * @param location The location
 * @param modelProducer Model producer containing the data
 * @param maxY max Y value for this chart
 * @param endAxisValueFormatter value formatter for the right side
 * @param markerFormatter value formatter for the marker. Leave empty to hide the marker
 */
@Composable
fun BreezyLineChart(
    location: Location,
    modelProducer: CartesianChartModelProducer,
    theDay: Date,
    maxY: Double,
    endAxisValueFormatter: CartesianValueFormatter,
    colors: ImmutableList<ImmutableMap<Float, Color>>,
    modifier: Modifier = Modifier,
    topAxisValueFormatter: CartesianValueFormatter? = null,
    endAxisItemPlacer: VerticalAxis.ItemPlacer = remember {
        VerticalAxis.ItemPlacer.step()
    },
    trendHorizontalLines: ImmutableMap<Double, String> = persistentMapOf(),
    minY: Double? = null,
    markerFormatter: DefaultCartesianMarker.ValueFormatter? = null,
    markerVisibilityListener: CartesianMarkerVisibilityListener? = null,
) {
    val view = LocalView.current
    val context = LocalContext.current

    val isTrendHorizontalLinesEnabled = SettingsManager.getInstance(context).isTrendHorizontalLinesEnabled

    val startingDate = remember(theDay) {
        theDay.toTimezoneSpecificHour(location.javaTimeZone, 0)
    }
    val cartesianLayerRangeProvider = CartesianLayerRangeProvider.fixed(
        minX = startingDate.time.toDouble(),
        maxX = theDay.toCalendarWithTimeZone(location.javaTimeZone).apply {
            add(Calendar.DAY_OF_MONTH, 1)
        }.time.toTimezoneSpecificHour(location.javaTimeZone, 0).time.toDouble(),
        minY = minY,
        maxY = maxY
    )
    val marker = rememberDefaultCartesianMarker(
        label = if (markerFormatter == null) {
            // TODO: Report upstream to have a way to hide it
            rememberTextComponent(
                color = Color.Transparent
            )
        } else {
            rememberTextComponent(
                color = Color(
                    MainThemeColorProvider.getColor(location, com.google.android.material.R.attr.colorOnPrimary)
                ),
                background = rememberShapeComponent(
                    fill = Fill(MainThemeColorProvider.getColor(location, androidx.appcompat.R.attr.colorPrimary)),
                    CorneredShape.Pill,
                    shadow = Shadow(
                        radiusDp = LABEL_BACKGROUND_SHADOW_RADIUS_DP,
                        yDp = LABEL_BACKGROUND_SHADOW_DY_DP
                    )
                ),
                padding = insets(
                    dimensionResource(R.dimen.normal_margin),
                    dimensionResource(R.dimen.little_margin)
                ),
                textAlignment = Layout.Alignment.ALIGN_CENTER,
                minWidth = TextComponent.MinWidth.fixed(40f)
            )
        },
        guideline = rememberLineComponent(
            fill = fill(
                colorResource(R.color.colorTextContent)
            )
        ),
        valueFormatter = markerFormatter ?: remember {
            DefaultCartesianMarker.ValueFormatter.default(colorCode = false)
        }
    )

    val timeValueFormatter = CartesianValueFormatter { _, value, _ ->
        Date(value.toLong()).getFormattedTime(location, context, context.is12Hour)
    }

    CartesianChartHost(
        rememberCartesianChart(
            rememberLineCartesianLayer(
                LineCartesianLayer.LineProvider.series(
                    colors.mapIndexed { index, colorList ->
                        LineCartesianLayer.rememberLine(
                            fill = ScaleLineFill(colorList),
                            areaFill = if (index == 0) ScaleAreaFill(colorList) else null,
                            stroke = LineCartesianLayer.LineStroke.continuous(LINE_THICKNESS_DP.dp)
                        )
                    }
                ),
                rangeProvider = cartesianLayerRangeProvider
            ),
            endAxis = VerticalAxis.rememberEnd(
                valueFormatter = endAxisValueFormatter,
                itemPlacer = endAxisItemPlacer
            ),
            bottomAxis = HorizontalAxis.rememberBottom(
                valueFormatter = timeValueFormatter,
                itemPlacer = remember {
                    TimeHorizontalAxisItemPlacer(location, startingDate)
                }
            ),
            topAxis = topAxisValueFormatter?.let {
                HorizontalAxis.rememberTop(
                    valueFormatter = it,
                    guideline = null,
                    tick = null,
                    // TODO: Don't add ticks at places where there is no data
                    itemPlacer = HorizontalAxis.ItemPlacer.aligned()
                )
            },
            decorations = if (isTrendHorizontalLinesEnabled) {
                trendHorizontalLines.entries.map { line ->
                    HorizontalLine(
                        y = { line.key },
                        verticalLabelPosition = Position.Vertical.Bottom,
                        line = rememberLineComponent(
                            fill = fill(
                                colorResource(
                                    if (MainThemeColorProvider.isLightTheme(context, location)) {
                                        R.color.colorTextGrey2nd
                                    } else {
                                        R.color.colorTextGrey
                                    }
                                )
                            )
                        ),
                        labelComponent = rememberTextComponent(
                            color = colorResource(R.color.colorTextContent)
                        ),
                        label = { line.value }
                    )
                }
            } else {
                emptyList()
            },
            marker = marker,
            markerVisibilityListener = markerVisibilityListener
        ),
        modelProducer = modelProducer,
        scrollState = rememberVicoScrollState(scrollEnabled = false),
        modifier = modifier
            .height(max(LINE_CHART_HEIGHT_MIN_DP.toFloat(), context.windowHeightInDp.div(4)).dp)
            .handleNestedHorizontalDragGesture(view)
    )
}

/**
 * Author: Gowsky
 */
private data class ScaleLineFill(val colors: Map<Float, Color>) : LineCartesianLayer.LineFill {
    private val paint = Paint(Paint.ANTI_ALIAS_FLAG)

    override fun draw(
        context: CartesianDrawingContext,
        halfLineThickness: Float,
        verticalAxisPosition: Axis.Position.Vertical?,
    ) {
        paint.shader = getShader(context, colors)

        context.canvas.drawRect(
            context.layerBounds.left,
            context.layerBounds.top - halfLineThickness,
            context.layerBounds.right,
            context.layerBounds.bottom + halfLineThickness,
            paint
        )
    }
}

/**
 * Author: Gowsky
 */
private data class ScaleAreaFill(val colors: Map<Float, Color>) : LineCartesianLayer.AreaFill {
    private val paint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val areaPath = Path()
    private val areaBounds = RectF()

    init {
        paint.alpha = 128 // For semi-transparent area fill.
    }

    override fun draw(
        context: CartesianDrawingContext,
        linePath: Path,
        halfLineThickness: Float,
        verticalAxisPosition: Axis.Position.Vertical?,
    ) {
        linePath.computeBounds(areaBounds, false)

        paint.shader = getShader(context, colors)

        with(areaPath) {
            set(linePath)
            lineTo(areaBounds.right, context.layerBounds.bottom)
            lineTo(areaBounds.left, context.layerBounds.bottom)
            close()
        }

        context.canvas.drawPath(areaPath, paint)
    }
}

/**
 * Author: Gowsky
 */
private fun getShader(context: CartesianDrawingContext, colors: Map<Float, Color>): Shader {
    val maxY = context.ranges.getYRange(null).maxY
    val minY = context.ranges.getYRange(null).minY

    return LinearGradientShader(
        from = Offset(0f, context.layerBounds.top),
        to = Offset(0f, context.layerBounds.bottom),
        colors = colors.values.toList(),
        colorStops = colors.keys.map { y -> 1f - ((y - minY) / (maxY - minY)).toFloat() }
    )
}

private const val LINE_CHART_HEIGHT_MIN_DP = 100
private const val LINE_THICKNESS_DP = 5f
private const val LABEL_BACKGROUND_SHADOW_RADIUS_DP = 4f
private const val LABEL_BACKGROUND_SHADOW_DY_DP = 2f
