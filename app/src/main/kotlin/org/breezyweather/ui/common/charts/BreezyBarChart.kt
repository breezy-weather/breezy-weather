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

import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
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
import com.patrykandpatrick.vico.compose.cartesian.layer.ColumnCartesianLayer
import com.patrykandpatrick.vico.compose.cartesian.layer.rememberColumnCartesianLayer
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
import kotlinx.collections.immutable.ImmutableMap
import kotlinx.collections.immutable.persistentMapOf
import org.breezyweather.R
import org.breezyweather.common.extensions.getFormattedTime
import org.breezyweather.common.extensions.getThemeColor
import org.breezyweather.common.extensions.is12Hour
import org.breezyweather.common.extensions.toCalendarWithTimeZone
import org.breezyweather.common.extensions.toTimezoneSpecificHour
import org.breezyweather.common.extensions.windowHeightInDp
import org.breezyweather.domain.settings.SettingsManager
import org.breezyweather.ui.theme.ThemeManager
import java.util.Calendar
import java.util.Date
import kotlin.math.max

/**
 * A bar chart with time as X-Axis and a formatted value on Y-Axis
 *
 * @param location The location
 * @param modelProducer Model producer containing the data
 * @param maxY max Y value for this chart
 * @param endAxisValueFormatter value formatter for the right side
 * @param markerFormatter value formatter for the marker. Leave empty to hide the marker
 */
@Composable
fun BreezyBarChart(
    location: Location,
    modelProducer: CartesianChartModelProducer,
    theDay: Date,
    maxY: Double,
    endAxisValueFormatter: CartesianValueFormatter,
    barColorFill: Fill,
    modifier: Modifier = Modifier,
    endAxisItemPlacer: VerticalAxis.ItemPlacer = remember {
        VerticalAxis.ItemPlacer.step()
    },
    trendHorizontalLines: ImmutableMap<Double, String> = persistentMapOf(),
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
        maxY = maxY
    )

    val lineColor = Color(context.getThemeColor(com.google.android.material.R.attr.colorOutline))
    val labelColor = colorResource(
        if (ThemeManager.isLightTheme(context, location)) {
            R.color.colorTextGrey
        } else {
            R.color.colorTextGrey2nd
        }
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
                    color = Color(
                        context.getThemeColor(com.google.android.material.R.attr.colorOnPrimary)
                    ),
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
            rememberColumnCartesianLayer(
                ColumnCartesianLayer.ColumnProvider.series(
                    rememberLineComponent(
                        fill = barColorFill,
                        thickness = BAR_THICKNESS_DP.dp,
                        shape = remember { RoundedCornerShape(15) }
                    )
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
            .height(max(BAR_CHART_HEIGHT_MIN_DP.toFloat(), context.windowHeightInDp.div(4)).dp),
        scrollState = rememberVicoScrollState(scrollEnabled = false),
        animateIn = SettingsManager.getInstance(context).isElementsAnimationEnabled
    )
}

private const val BAR_CHART_HEIGHT_MIN_DP = 100
private const val BAR_THICKNESS_DP = 500f
private const val LABEL_BACKGROUND_SHADOW_RADIUS_DP = 4
private const val LABEL_BACKGROUND_SHADOW_SPREAD_DP = 2
