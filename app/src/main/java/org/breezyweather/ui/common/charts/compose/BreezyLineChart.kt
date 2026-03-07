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

package org.breezyweather.ui.common.charts.compose

import android.graphics.RectF
import android.graphics.Shader
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.LinearGradientShader
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.asAndroidPath
import androidx.compose.ui.graphics.shadow.Shadow
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import breezyweather.domain.location.model.Location
import com.patrykandpatrick.vico.compose.cartesian.CartesianChartHost
import com.patrykandpatrick.vico.compose.cartesian.CartesianDrawingContext
import com.patrykandpatrick.vico.compose.cartesian.axis.Axis
import com.patrykandpatrick.vico.compose.cartesian.axis.BaseAxis
import com.patrykandpatrick.vico.compose.cartesian.axis.HorizontalAxis
import com.patrykandpatrick.vico.compose.cartesian.axis.VerticalAxis
import com.patrykandpatrick.vico.compose.cartesian.axis.rememberAxisGuidelineComponent
import com.patrykandpatrick.vico.compose.cartesian.axis.rememberAxisLabelComponent
import com.patrykandpatrick.vico.compose.cartesian.axis.rememberAxisLineComponent
import com.patrykandpatrick.vico.compose.cartesian.axis.rememberAxisTickComponent
import com.patrykandpatrick.vico.compose.cartesian.data.CartesianChartModelProducer
import com.patrykandpatrick.vico.compose.cartesian.data.CartesianLayerRangeProvider
import com.patrykandpatrick.vico.compose.cartesian.data.CartesianValueFormatter
import com.patrykandpatrick.vico.compose.cartesian.decoration.HorizontalLine
import com.patrykandpatrick.vico.compose.cartesian.layer.LineCartesianLayer
import com.patrykandpatrick.vico.compose.cartesian.layer.rememberLine
import com.patrykandpatrick.vico.compose.cartesian.layer.rememberLineCartesianLayer
import com.patrykandpatrick.vico.compose.cartesian.marker.CartesianMarkerController
import com.patrykandpatrick.vico.compose.cartesian.marker.CartesianMarkerVisibilityListener
import com.patrykandpatrick.vico.compose.cartesian.marker.DefaultCartesianMarker
import com.patrykandpatrick.vico.compose.cartesian.marker.rememberDefaultCartesianMarker
import com.patrykandpatrick.vico.compose.cartesian.rememberCartesianChart
import com.patrykandpatrick.vico.compose.cartesian.rememberVicoScrollState
import com.patrykandpatrick.vico.compose.common.Fill
import com.patrykandpatrick.vico.compose.common.Insets
import com.patrykandpatrick.vico.compose.common.Position
import com.patrykandpatrick.vico.compose.common.component.TextComponent
import com.patrykandpatrick.vico.compose.common.component.rememberLineComponent
import com.patrykandpatrick.vico.compose.common.component.rememberShapeComponent
import com.patrykandpatrick.vico.compose.common.component.rememberTextComponent
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.ImmutableMap
import kotlinx.collections.immutable.persistentMapOf
import org.breezyweather.R
import org.breezyweather.common.extensions.getFormattedTime
import org.breezyweather.common.extensions.getThemeColor
import org.breezyweather.common.extensions.is12Hour
import org.breezyweather.common.extensions.isRtl
import org.breezyweather.common.extensions.toCalendarWithTimeZone
import org.breezyweather.common.extensions.toTimezoneSpecificHour
import org.breezyweather.common.extensions.windowHeightInDp
import org.breezyweather.domain.settings.SettingsManager
import org.breezyweather.ui.theme.ThemeManager
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
    topAxisItemPlacer: HorizontalAxis.ItemPlacer = remember {
        HorizontalAxis.ItemPlacer.aligned(
            shiftExtremeLines = false,
            addExtremeLabelPadding = false
        )
    },
    topAxisSize: BaseAxis.Size = BaseAxis.Size.Auto(),
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
        theDay.toTimezoneSpecificHour(location.timeZone, 0)
    }
    val cartesianLayerRangeProvider = CartesianLayerRangeProvider.fixed(
        minX = startingDate.time.toDouble(),
        maxX = theDay.toCalendarWithTimeZone(location.timeZone).apply {
            add(Calendar.DAY_OF_MONTH, 1)
        }.time.toTimezoneSpecificHour(location.timeZone, 0).time.toDouble(),
        minY = minY,
        maxY = maxY
    )
    val lineColor = Color(context.getThemeColor(com.google.android.material.R.attr.colorOutline))
    val labelColor = colorResource(
        if (ThemeManager.isLightTheme(context, location)) R.color.colorTextGrey else R.color.colorTextGrey2nd
    )
    val marker = rememberDefaultCartesianMarker(
        label = if (markerFormatter == null) {
            // TODO: Report upstream to have a way to hide it
            rememberTextComponent(
                style = TextStyle(
                    color = Color.Transparent
                )
            )
        } else {
            rememberTextComponent(
                style = TextStyle(
                    color = Color(context.getThemeColor(com.google.android.material.R.attr.colorOnPrimary)),
                    textAlign = TextAlign.Center
                ),
                background = rememberShapeComponent(
                    fill = Fill(Color(context.getThemeColor(androidx.appcompat.R.attr.colorPrimary))),
                    shape = RoundedCornerShape(50),
                    shadows = listOf(
                        Shadow(
                            radius = LABEL_BACKGROUND_SHADOW_RADIUS_DP.dp,
                            spread = LABEL_BACKGROUND_SHADOW_SPREAD_DP.dp
                        )
                    )
                ),
                padding = Insets(
                    dimensionResource(R.dimen.normal_margin),
                    dimensionResource(R.dimen.small_margin)
                ),
                minWidth = TextComponent.MinWidth.fixed(40.dp)
            )
        },
        guideline = rememberLineComponent(fill = Fill(labelColor)),
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
                            areaFill = if (index == 0) ScaleAreaFill(colorList, context.isRtl) else null,
                            stroke = LineCartesianLayer.LineStroke.Continuous(LINE_THICKNESS_DP.dp)
                        )
                    }
                ),
                rangeProvider = cartesianLayerRangeProvider
            ),
            endAxis = VerticalAxis.rememberEnd(
                line = rememberAxisLineComponent(fill = Fill(lineColor)),
                label = rememberAxisLabelComponent(style = TextStyle(color = labelColor)),
                valueFormatter = endAxisValueFormatter,
                tick = rememberAxisTickComponent(fill = Fill(lineColor)),
                guideline = rememberAxisGuidelineComponent(fill = Fill(lineColor)),
                itemPlacer = endAxisItemPlacer
            ),
            bottomAxis = HorizontalAxis.rememberBottom(
                line = rememberAxisLineComponent(fill = Fill(lineColor)),
                label = rememberAxisLabelComponent(style = TextStyle(color = labelColor)),
                valueFormatter = timeValueFormatter,
                tick = rememberAxisTickComponent(fill = Fill(lineColor)),
                guideline = rememberAxisGuidelineComponent(fill = Fill(lineColor)),
                itemPlacer = remember {
                    TimeHorizontalAxisItemPlacer(startingDate, location.timeZone)
                }
            ),
            topAxis = topAxisValueFormatter?.let {
                HorizontalAxis.rememberTop(
                    line = rememberAxisLineComponent(fill = Fill(lineColor)),
                    label = rememberAxisLabelComponent(style = TextStyle(color = labelColor)),
                    valueFormatter = it,
                    tick = null,
                    guideline = null,
                    itemPlacer = topAxisItemPlacer,
                    size = topAxisSize
                )
            },
            decorations = if (isTrendHorizontalLinesEnabled) {
                trendHorizontalLines.entries.map { line ->
                    HorizontalLine(
                        y = { line.key },
                        verticalLabelPosition = Position.Vertical.Bottom,
                        line = rememberLineComponent(fill = Fill(lineColor)),
                        labelComponent = rememberTextComponent(style = TextStyle(color = labelColor)),
                        label = { line.value }
                    )
                }
            } else {
                emptyList()
            },
            marker = marker,
            markerVisibilityListener = markerVisibilityListener,
            markerController = CartesianMarkerController.rememberShowOnPress(consumeMoveEvents = true)
        ),
        modelProducer = modelProducer,
        modifier = modifier
            .height(max(LINE_CHART_HEIGHT_MIN_DP.toFloat(), context.windowHeightInDp.div(4)).dp),
        scrollState = rememberVicoScrollState(scrollEnabled = false),
        animateIn = SettingsManager.getInstance(context).isElementsAnimationEnabled
    )
}

/**
 * Author: Gowsky
 */
internal data class ScaleLineFill(val colors: Map<Float, Color>) : LineCartesianLayer.LineFill {
    private val paint = Paint()

    init {
        paint.isAntiAlias = true
    }

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
internal data class ScaleAreaFill(
    val colors: Map<Float, Color>,
    val rtl: Boolean = false,
) : LineCartesianLayer.AreaFill {
    private val paint = Paint()
    private val areaPath = Path()
    private val areaBounds = RectF()

    init {
        paint.isAntiAlias = true
        paint.alpha = 128f // For semi-transparent area fill.
    }

    override fun draw(
        context: CartesianDrawingContext,
        linePath: Path,
        halfLineThickness: Float,
        verticalAxisPosition: Axis.Position.Vertical?,
    ) {
        linePath.asAndroidPath().computeBounds(areaBounds, false)

        paint.shader = getShader(context, colors)

        with(areaPath.asAndroidPath()) {
            set(linePath.asAndroidPath())
            if (rtl) {
                lineTo(areaBounds.left, context.layerBounds.bottom)
                lineTo(areaBounds.right, context.layerBounds.bottom)
            } else {
                lineTo(areaBounds.right, context.layerBounds.bottom)
                lineTo(areaBounds.left, context.layerBounds.bottom)
            }
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
private const val LABEL_BACKGROUND_SHADOW_RADIUS_DP = 4
private const val LABEL_BACKGROUND_SHADOW_SPREAD_DP = 2
