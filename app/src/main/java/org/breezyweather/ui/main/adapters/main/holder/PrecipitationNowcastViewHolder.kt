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

package org.breezyweather.ui.main.adapters.main.holder

import android.content.Context
import android.os.Build
import android.text.Layout
import android.text.SpannableStringBuilder
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.TextView
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.unit.dp
import breezyweather.domain.location.model.Location
import breezyweather.domain.weather.model.Precipitation
import com.patrykandpatrick.vico.compose.cartesian.CartesianChartHost
import com.patrykandpatrick.vico.compose.cartesian.axis.rememberAxisLabelComponent
import com.patrykandpatrick.vico.compose.cartesian.axis.rememberAxisTickComponent
import com.patrykandpatrick.vico.compose.cartesian.axis.rememberBottom
import com.patrykandpatrick.vico.compose.cartesian.layer.rememberColumnCartesianLayer
import com.patrykandpatrick.vico.compose.cartesian.marker.rememberDefaultCartesianMarker
import com.patrykandpatrick.vico.compose.cartesian.rememberCartesianChart
import com.patrykandpatrick.vico.compose.cartesian.rememberVicoScrollState
import com.patrykandpatrick.vico.compose.common.component.rememberLineComponent
import com.patrykandpatrick.vico.compose.common.component.rememberShapeComponent
import com.patrykandpatrick.vico.compose.common.component.rememberTextComponent
import com.patrykandpatrick.vico.compose.common.fill
import com.patrykandpatrick.vico.compose.common.insets
import com.patrykandpatrick.vico.core.cartesian.CartesianDrawingContext
import com.patrykandpatrick.vico.core.cartesian.axis.HorizontalAxis
import com.patrykandpatrick.vico.core.cartesian.data.CartesianChartModelProducer
import com.patrykandpatrick.vico.core.cartesian.data.CartesianLayerRangeProvider
import com.patrykandpatrick.vico.core.cartesian.data.CartesianValueFormatter
import com.patrykandpatrick.vico.core.cartesian.data.columnSeries
import com.patrykandpatrick.vico.core.cartesian.decoration.HorizontalLine
import com.patrykandpatrick.vico.core.cartesian.layer.ColumnCartesianLayer
import com.patrykandpatrick.vico.core.cartesian.marker.CartesianMarker
import com.patrykandpatrick.vico.core.cartesian.marker.DefaultCartesianMarker
import com.patrykandpatrick.vico.core.common.Fill
import com.patrykandpatrick.vico.core.common.Position
import com.patrykandpatrick.vico.core.common.component.Shadow
import com.patrykandpatrick.vico.core.common.component.TextComponent
import com.patrykandpatrick.vico.core.common.shape.CorneredShape
import kotlinx.collections.immutable.ImmutableMap
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableMap
import org.breezyweather.R
import org.breezyweather.common.basic.GeoActivity
import org.breezyweather.common.extensions.getFormattedTime
import org.breezyweather.common.extensions.handleNestedHorizontalDragGesture
import org.breezyweather.common.extensions.is12Hour
import org.breezyweather.common.extensions.toDate
import org.breezyweather.domain.settings.SettingsManager
import org.breezyweather.domain.weather.model.getContentDescription
import org.breezyweather.domain.weather.model.getMinutelyDescription
import org.breezyweather.domain.weather.model.getMinutelyTitle
import org.breezyweather.ui.common.charts.SpecificHorizontalAxisItemPlacer
import org.breezyweather.ui.main.utils.MainThemeColorProvider
import org.breezyweather.ui.theme.ThemeManager
import org.breezyweather.ui.theme.compose.BreezyWeatherTheme
import org.breezyweather.ui.theme.resource.providers.ResourceProvider
import org.breezyweather.ui.theme.weatherView.WeatherViewController
import java.util.Date
import kotlin.math.max
import kotlin.time.Duration.Companion.minutes

class PrecipitationNowcastViewHolder(parent: ViewGroup) : AbstractMainCardViewHolder(
    LayoutInflater.from(parent.context).inflate(R.layout.container_main_precipitation_nowcast_card, parent, false)
) {
    private val title = itemView.findViewById<TextView>(R.id.container_main_minutely_card_title)
    private val subtitle = itemView.findViewById<TextView>(R.id.container_main_minutely_card_subtitle)
    private val chartComposeView = itemView.findViewById<ComposeView>(R.id.container_main_minutely_chart_composeView)

    override fun onBindView(
        activity: GeoActivity,
        location: Location,
        provider: ResourceProvider,
        listAnimationEnabled: Boolean,
        itemAnimationEnabled: Boolean,
        firstCard: Boolean,
    ) {
        super.onBindView(activity, location, provider, listAnimationEnabled, itemAnimationEnabled, firstCard)

        val weather = location.weather ?: return
        val colors = ThemeManager
            .getInstance(context)
            .weatherThemeDelegate
            .getThemeColors(
                context,
                WeatherViewController.getWeatherKind(location),
                WeatherViewController.isDaylight(location)
            )

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            title.isAccessibilityHeading = true
        }
        title.setTextColor(colors[0])
        title.text = weather.getMinutelyTitle(context)
        subtitle.text = weather.getMinutelyDescription(context, location)

        val minutelyList = weather.minutelyForecast
        val firstTime = minutelyList.first().date.time
        val lastTime = minutelyList.last().date.time + minutelyList.last().minuteInterval.minutes.inWholeMilliseconds

        chartComposeView.setContent {
            BreezyWeatherTheme(
                lightTheme = MainThemeColorProvider.isLightTheme(context, location)
            ) {
                ContentView(location, firstTime, lastTime)
            }
        }
        chartComposeView.contentDescription = minutelyList.getContentDescription(context, location)
    }

    @Composable
    private fun ContentView(
        location: Location,
        minX: Long,
        maxX: Long,
    ) {
        val view = LocalView.current
        val minutely = location.weather!!.minutelyForecastBy5Minutes
            .associate { it.date.time to (it.precipitationIntensity ?: 0.0) }
            .toImmutableMap()
        val timeWithTicks = remember(minX, maxX) {
            persistentListOf(minX.toDouble(), (minX + (maxX - minX) / 2).toDouble(), maxX.toDouble())
        }
        val maxY = max(
            Precipitation.PRECIPITATION_HOURLY_HEAVY,
            minutely.values.max()
        )
        val trendHorizontalLines: ImmutableMap<Double, String> = remember(maxY) {
            buildMap {
                /**
                 * Don’t show some thresholds when max precipitation is very high
                 * - Below heavy level: keep all 3 lines
                 * - Between heavy level and 2 * heavy level: keep light and heavy lines
                 * - Above 2 * heavy level: keep heavy line only
                 */
                if (maxY < Precipitation.PRECIPITATION_HOURLY_HEAVY * 2.0f) {
                    put(
                        Precipitation.PRECIPITATION_HOURLY_LIGHT,
                        context.getString(R.string.precipitation_intensity_light)
                    )
                }
                if (maxY <= Precipitation.PRECIPITATION_HOURLY_HEAVY) {
                    put(
                        Precipitation.PRECIPITATION_HOURLY_MEDIUM,
                        context.getString(R.string.precipitation_intensity_medium)
                    )
                }
                put(
                    Precipitation.PRECIPITATION_HOURLY_HEAVY,
                    context.getString(R.string.precipitation_intensity_heavy)
                )
            }.toImmutableMap()
        }
        val hasOnlyThresholdsValues = !minutely.values.any {
            it !in arrayOf(
                null,
                0.0,
                Precipitation.PRECIPITATION_HOURLY_LIGHT,
                Precipitation.PRECIPITATION_HOURLY_MEDIUM,
                Precipitation.PRECIPITATION_HOURLY_HEAVY
            )
        }

        val modelProducer = remember { CartesianChartModelProducer() }

        val isTrendHorizontalLinesEnabled = SettingsManager.getInstance(context).isTrendHorizontalLinesEnabled
        val lineColor =
            Color(MainThemeColorProvider.getColor(location, com.google.android.material.R.attr.colorOutline))
        val labelColor = colorResource(
            if (MainThemeColorProvider.isLightTheme(context, location)) {
                R.color.colorTextGrey
            } else {
                R.color.colorTextGrey2nd
            }
        )

        val cartesianLayerRangeProvider = CartesianLayerRangeProvider.fixed(
            minX = minX.toDouble(),
            maxX = maxX.toDouble(),
            maxY = maxY
        )
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
            guideline = rememberLineComponent(fill = fill(labelColor)),
            valueFormatter = MarkerLabelFormatterMinutelyDecorator(minutely, location, context, hasOnlyThresholdsValues)
        )

        LaunchedEffect(location) {
            modelProducer.runTransaction {
                columnSeries {
                    series(
                        x = minutely.keys,
                        y = minutely.values
                    )
                }
            }
        }

        val timeValueFormatter = CartesianValueFormatter { _, value, _ ->
            Date(value.toLong()).getFormattedTime(location, context, context.is12Hour)
        }

        CartesianChartHost(
            rememberCartesianChart(
                rememberColumnCartesianLayer(
                    ColumnCartesianLayer.ColumnProvider.series(
                        rememberLineComponent(
                            fill = Fill(
                                ThemeManager
                                    .getInstance(context)
                                    .weatherThemeDelegate
                                    .getThemeColors(
                                        context,
                                        WeatherViewController.getWeatherKind(location),
                                        WeatherViewController.isDaylight(location)
                                    )[0]
                            ),
                            thickness = 500.dp,
                            shape = remember { CorneredShape.rounded(allPercent = 15) }
                        )
                    ),
                    rangeProvider = cartesianLayerRangeProvider
                ),
                bottomAxis = HorizontalAxis.rememberBottom(
                    line = rememberLineComponent(fill = fill(lineColor)),
                    label = rememberAxisLabelComponent(color = labelColor),
                    valueFormatter = timeValueFormatter,
                    tick = rememberAxisTickComponent(fill = fill(lineColor)),
                    guideline = null,
                    itemPlacer = remember {
                        SpecificHorizontalAxisItemPlacer(timeWithTicks)
                    }
                ),
                decorations = if (isTrendHorizontalLinesEnabled) {
                    trendHorizontalLines.entries.map { line ->
                        HorizontalLine(
                            y = { line.key },
                            verticalLabelPosition = Position.Vertical.Bottom,
                            line = rememberLineComponent(fill = fill(lineColor)),
                            labelComponent = rememberTextComponent(color = labelColor),
                            label = { line.value }
                        )
                    }
                } else {
                    emptyList()
                },
                marker = marker
            ),
            modelProducer = modelProducer,
            scrollState = rememberVicoScrollState(scrollEnabled = false),
            modifier = Modifier
                .handleNestedHorizontalDragGesture(view)
                .height(150.dp)
        )
    }
}

private class MarkerLabelFormatterMinutelyDecorator(
    private val mappedValues: Map<Long, Double>,
    private val location: Location,
    private val aContext: Context,
    private val hasOnlyThresholdValues: Boolean,
) : DefaultCartesianMarker.ValueFormatter {

    override fun format(
        context: CartesianDrawingContext,
        targets: List<CartesianMarker.Target>,
    ): CharSequence {
        val model = targets.first()
        val precipitationIntensityUnit = SettingsManager.getInstance(aContext).getPrecipitationIntensityUnit(aContext)
        val startTime = model.x.toLong().toDate()
            .getFormattedTime(location, aContext, aContext.is12Hour)
        val endTime = (model.x.toLong() + 5.minutes.inWholeMilliseconds).toDate()
            .getFormattedTime(location, aContext, aContext.is12Hour)
        val quantityFormatted = if (hasOnlyThresholdValues) {
            when (mappedValues.getOrElse(model.x.toLong()) { null } ?: 0.0) {
                0.0 -> aContext.getString(R.string.precipitation_none)
                Precipitation.PRECIPITATION_HOURLY_LIGHT -> aContext.getString(R.string.precipitation_intensity_light)
                Precipitation.PRECIPITATION_HOURLY_MEDIUM -> aContext.getString(R.string.precipitation_intensity_medium)
                Precipitation.PRECIPITATION_HOURLY_HEAVY -> aContext.getString(R.string.precipitation_intensity_heavy)
                else -> precipitationIntensityUnit.getValueText(
                    aContext,
                    mappedValues.getOrElse(model.x.toLong()) { null } ?: 0.0
                )
            }
        } else {
            precipitationIntensityUnit.getValueText(
                aContext,
                mappedValues.getOrElse(model.x.toLong()) { null } ?: 0.0
            )
        }

        return SpannableStringBuilder().append(
            startTime,
            "-",
            endTime,
            aContext.getString(R.string.colon_separator),
            quantityFormatted
        )
    }
}

private const val LABEL_BACKGROUND_SHADOW_RADIUS_DP = 4f
private const val LABEL_BACKGROUND_SHADOW_DY_DP = 2f
