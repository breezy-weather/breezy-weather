package org.breezyweather.ui.common.charts

import android.text.Layout
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.unit.dp
import breezyweather.domain.location.model.Location
import com.patrykandpatrick.vico.compose.cartesian.CartesianChartHost
import com.patrykandpatrick.vico.compose.cartesian.axis.rememberBottom
import com.patrykandpatrick.vico.compose.cartesian.axis.rememberEnd
import com.patrykandpatrick.vico.compose.cartesian.layer.rememberColumnCartesianLayer
import com.patrykandpatrick.vico.compose.cartesian.marker.rememberDefaultCartesianMarker
import com.patrykandpatrick.vico.compose.cartesian.rememberCartesianChart
import com.patrykandpatrick.vico.compose.cartesian.rememberVicoScrollState
import com.patrykandpatrick.vico.compose.common.component.rememberLineComponent
import com.patrykandpatrick.vico.compose.common.component.rememberShapeComponent
import com.patrykandpatrick.vico.compose.common.component.rememberTextComponent
import com.patrykandpatrick.vico.compose.common.insets
import com.patrykandpatrick.vico.core.cartesian.axis.HorizontalAxis
import com.patrykandpatrick.vico.core.cartesian.axis.VerticalAxis
import com.patrykandpatrick.vico.core.cartesian.data.CartesianChartModelProducer
import com.patrykandpatrick.vico.core.cartesian.data.CartesianLayerRangeProvider
import com.patrykandpatrick.vico.core.cartesian.data.CartesianValueFormatter
import com.patrykandpatrick.vico.core.cartesian.layer.ColumnCartesianLayer
import com.patrykandpatrick.vico.core.cartesian.marker.DefaultCartesianMarker
import com.patrykandpatrick.vico.core.common.Fill
import com.patrykandpatrick.vico.core.common.component.Shadow
import com.patrykandpatrick.vico.core.common.component.TextComponent
import com.patrykandpatrick.vico.core.common.shape.CorneredShape
import org.breezyweather.R
import org.breezyweather.common.extensions.getFormattedTime
import org.breezyweather.common.extensions.handleNestedHorizontalDragGesture
import org.breezyweather.common.extensions.is12Hour
import org.breezyweather.ui.main.utils.MainThemeColorProvider
import java.util.Date

/**
 * A bar chart with time as X-Axis and a formatted value on Y-Axis
 *
 * @param location The location
 * @param modelProducer Model producer containing the data
 * @param maxY max Y value for this chart
 * @param endAxisValueFormatter value formatter for the right side
 * @param valueFormatter value formatter for the marker
 */
@Composable
fun BreezyBarChart(
    location: Location,
    modelProducer: CartesianChartModelProducer,
    maxY: Double,
    endAxisValueFormatter: CartesianValueFormatter,
    valueFormatter: DefaultCartesianMarker.ValueFormatter,
    barColorFill: Fill,
    // thresholdFills: ImmutableMap<Number, Fill>,
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
            rememberColumnCartesianLayer(
                ColumnCartesianLayer.ColumnProvider.series(
                    rememberLineComponent(
                        fill = barColorFill,
                        thickness = BAR_THICKNESS_DP.dp,
                        shape = remember { CorneredShape.rounded(allPercent = 15) }
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

private const val BAR_THICKNESS_DP: Float = 500f
private const val LABEL_BACKGROUND_SHADOW_RADIUS_DP = 4f
private const val LABEL_BACKGROUND_SHADOW_DY_DP = 2f
