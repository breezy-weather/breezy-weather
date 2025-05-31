package org.breezyweather.ui.common.charts

import android.graphics.Paint
import android.text.Layout
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.LinearGradientShader
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.unit.dp
import breezyweather.domain.location.model.Location
import com.patrykandpatrick.vico.compose.cartesian.CartesianChartHost
import com.patrykandpatrick.vico.compose.cartesian.axis.rememberBottom
import com.patrykandpatrick.vico.compose.cartesian.axis.rememberEnd
import com.patrykandpatrick.vico.compose.cartesian.layer.continuous
import com.patrykandpatrick.vico.compose.cartesian.layer.rememberLine
import com.patrykandpatrick.vico.compose.cartesian.layer.rememberLineCartesianLayer
import com.patrykandpatrick.vico.compose.cartesian.marker.rememberDefaultCartesianMarker
import com.patrykandpatrick.vico.compose.cartesian.rememberCartesianChart
import com.patrykandpatrick.vico.compose.cartesian.rememberVicoScrollState
import com.patrykandpatrick.vico.compose.common.component.rememberShapeComponent
import com.patrykandpatrick.vico.compose.common.component.rememberTextComponent
import com.patrykandpatrick.vico.compose.common.insets
import com.patrykandpatrick.vico.core.cartesian.CartesianDrawingContext
import com.patrykandpatrick.vico.core.cartesian.axis.Axis
import com.patrykandpatrick.vico.core.cartesian.axis.HorizontalAxis
import com.patrykandpatrick.vico.core.cartesian.axis.VerticalAxis
import com.patrykandpatrick.vico.core.cartesian.data.CartesianChartModelProducer
import com.patrykandpatrick.vico.core.cartesian.data.CartesianLayerRangeProvider
import com.patrykandpatrick.vico.core.cartesian.data.CartesianValueFormatter
import com.patrykandpatrick.vico.core.cartesian.layer.LineCartesianLayer
import com.patrykandpatrick.vico.core.cartesian.marker.DefaultCartesianMarker
import com.patrykandpatrick.vico.core.common.Fill
import com.patrykandpatrick.vico.core.common.component.Shadow
import com.patrykandpatrick.vico.core.common.component.TextComponent
import com.patrykandpatrick.vico.core.common.data.ExtraStore
import com.patrykandpatrick.vico.core.common.shape.CorneredShape
import kotlinx.collections.immutable.ImmutableMap
import org.breezyweather.R
import org.breezyweather.common.extensions.getFormattedTime
import org.breezyweather.common.extensions.handleNestedHorizontalDragGesture
import org.breezyweather.common.extensions.is12Hour
import org.breezyweather.ui.main.utils.MainThemeColorProvider
import java.util.Date
import kotlin.math.ceil

/**
 * A line chart with time as X-Axis and a formatted value on Y-Axis
 *
 * @param location The location
 * @param modelProducer Model producer containing the data
 * @param maxY max Y value for this chart
 * @param endAxisValueFormatter value formatter for the right side
 * @param valueFormatter value formatter for the marker
 */
@Composable
fun BreezyLineChart(
    location: Location,
    modelProducer: CartesianChartModelProducer,
    maxY: Double,
    endAxisValueFormatter: CartesianValueFormatter,
    valueFormatter: DefaultCartesianMarker.ValueFormatter,
    thresholdFills: ImmutableMap<Number, Fill>,
    modifier: Modifier = Modifier,
) {
    val view = LocalView.current
    val context = LocalContext.current

    val cartesianLayerRangeProvider = CartesianLayerRangeProvider.fixed(maxY = maxY)
    val marker = rememberDefaultCartesianMarker(
        label = rememberTextComponent(
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
        ),
        valueFormatter = valueFormatter
    )

    val timeValueFormatter = CartesianValueFormatter { _, value, _ ->
        Date(value.toLong()).getFormattedTime(location, context, context.is12Hour)
    }

    CartesianChartHost(
        rememberCartesianChart(
            rememberLineCartesianLayer(
                LineCartesianLayer.LineProvider.series(
                    LineCartesianLayer.rememberLine(
                        fill = GradientLineFill(thresholdFills),
                        stroke = LineCartesianLayer.LineStroke.continuous(LINE_THICKNESS_DP.dp)
                    )
                ),
                rangeProvider = cartesianLayerRangeProvider
            ),
            endAxis = VerticalAxis.rememberEnd(valueFormatter = endAxisValueFormatter),
            bottomAxis = HorizontalAxis.rememberBottom(
                valueFormatter = timeValueFormatter,
                itemPlacer = HorizontalAxis.ItemPlacer.aligned() // TODO: Custom steps
            ),
            // TODO: Makes two markers instead of fading away when tapped somewhere else
            // persistentMarkers = mapOf(minutely.indexOfLast { it.date < Date() }.toFloat() to marker),
            marker = marker
        ),
        modelProducer = modelProducer,
        scrollState = rememberVicoScrollState(scrollEnabled = false),
        modifier = modifier
            .height(300.dp)
            .handleNestedHorizontalDragGesture(view)
    )
}

/**
 * @param thresholdFills ordered from highest value (top of the chart) to lowest value (bottom). Must contain
 * at least 3 values, use DoubleLineFill otherwise
 */
internal data class GradientLineFill(
    val thresholdFills: ImmutableMap<Number, Fill>,
) : LineCartesianLayer.LineFill {
    private val paint = Paint(Paint.ANTI_ALIAS_FLAG)

    override fun draw(
        context: CartesianDrawingContext,
        halfLineThickness: Float,
        verticalAxisPosition: Axis.Position.Vertical?,
    ) {
        with(context) {
            // First and last colors are not gradient
            val canvasSplitYFirstColor =
                getCanvasSplitY({ thresholdFills.keys.first() }, halfLineThickness, verticalAxisPosition)
            val canvasSplitYLastColor =
                getCanvasSplitY({ thresholdFills.keys.last() }, halfLineThickness, verticalAxisPosition)

            paint.color = thresholdFills[thresholdFills.keys.first()]!!.color
            paint.shader =
                thresholdFills[thresholdFills.keys.first()]!!.shaderProvider?.getShader(
                    this,
                    layerBounds.left,
                    layerBounds.top - halfLineThickness,
                    layerBounds.right,
                    canvasSplitYFirstColor
                )
            canvas.drawRect(
                layerBounds.left,
                layerBounds.top - halfLineThickness,
                layerBounds.right,
                canvasSplitYFirstColor,
                paint
            )

            // Middle colors
            // First attempt: Gradients using the native gradient shader with color stops
            // FIXME: doesn't work when the first value is higher than maxY, or the last value is lower than minY

            // First value will be substracted from each value, so that the gradient starts at 0
            /*val gradientBeginValue = thresholdFills.keys.first().toDouble()
            val gradientCalculatedEndValue =
                thresholdFills.keys.last().toDouble() - thresholdFills.keys.first().toDouble()
            paint.shader = LinearGradientShader(
                Offset(0f, canvasSplitYFirstColor),
                Offset(0f, canvasSplitYLastColor),
                thresholdFills.values.map { Color(it.color) },
                thresholdFills.keys.map { key ->
                    // Divide by the last value, so that we have a value between 0.0 and 1.0, as expected
                    (key.toDouble() - gradientBeginValue).div(gradientCalculatedEndValue).toFloat()
                }
            )
            canvas.drawRect(
                layerBounds.left,
                canvasSplitYFirstColor,
                layerBounds.right,
                canvasSplitYLastColor,
                paint
            )*/

            // 2nd attempt, looping and creating the color stops manually
            var canvasSplitYTop: Float
            var canvasSplitYBottom = canvasSplitYFirstColor
            for (i in 1..<thresholdFills.size) {
                canvasSplitYTop = canvasSplitYBottom
                canvasSplitYBottom =
                    getCanvasSplitY({ thresholdFills.keys.elementAt(i) }, halfLineThickness, verticalAxisPosition)

                paint.shader = LinearGradientShader(
                    Offset(0f, canvasSplitYTop),
                    Offset(0f, canvasSplitYBottom),
                    listOf(
                        Color(thresholdFills.values.elementAt(i - 1).color),
                        Color(thresholdFills.values.elementAt(i).color)
                    ),
                    listOf(0f, 1f)
                )
                canvas.drawRect(
                    layerBounds.left,
                    canvasSplitYTop,
                    layerBounds.right,
                    canvasSplitYBottom,
                    paint
                )
            }

            // Last color
            paint.color = thresholdFills[thresholdFills.keys.last()]!!.color
            paint.shader =
                thresholdFills[thresholdFills.keys.last()]!!.shaderProvider?.getShader(
                    this,
                    layerBounds.left,
                    canvasSplitYLastColor,
                    layerBounds.right,
                    layerBounds.bottom + halfLineThickness
                )
            canvas.drawRect(
                layerBounds.left,
                canvasSplitYLastColor,
                layerBounds.right,
                layerBounds.bottom + halfLineThickness,
                paint
            )
        }
    }
}

internal fun CartesianDrawingContext.getCanvasSplitY(
    splitY: (ExtraStore) -> Number,
    halfLineThickness: Float,
    verticalAxisPosition: Axis.Position.Vertical?,
): Float {
    val yRange = ranges.getYRange(verticalAxisPosition)
    val base =
        layerBounds.bottom -
            ((splitY(model.extraStore).toDouble() - yRange.minY) / yRange.length).toFloat() *
            layerBounds.height()
    return ceil(base).coerceIn(layerBounds.top..layerBounds.bottom) + ceil(halfLineThickness)
}

private const val LINE_THICKNESS_DP: Float = 5f
private const val LABEL_BACKGROUND_SHADOW_RADIUS_DP = 4f
private const val LABEL_BACKGROUND_SHADOW_DY_DP = 2f
