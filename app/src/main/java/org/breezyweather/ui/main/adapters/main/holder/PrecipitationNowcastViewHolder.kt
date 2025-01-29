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
import android.text.Layout
import android.text.SpannableStringBuilder
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.unit.dp
import breezyweather.domain.location.model.Location
import breezyweather.domain.weather.model.Minutely
import breezyweather.domain.weather.model.Precipitation
import com.patrykandpatrick.vico.compose.cartesian.CartesianChartHost
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
import org.breezyweather.R
import org.breezyweather.common.basic.GeoActivity
import org.breezyweather.common.extensions.getFormattedTime
import org.breezyweather.common.extensions.is12Hour
import org.breezyweather.common.extensions.isDarkMode
import org.breezyweather.common.extensions.toDate
import org.breezyweather.domain.settings.SettingsManager
import org.breezyweather.domain.weather.model.getMinutelyDescription
import org.breezyweather.domain.weather.model.getMinutelyTitle
import org.breezyweather.ui.main.utils.MainThemeColorProvider
import org.breezyweather.ui.theme.ThemeManager
import org.breezyweather.ui.theme.compose.BreezyWeatherTheme
import org.breezyweather.ui.theme.resource.providers.ResourceProvider
import org.breezyweather.ui.theme.weatherView.WeatherViewController
import java.util.Date
import kotlin.math.absoluteValue
import kotlin.math.max
import kotlin.time.Duration.Companion.minutes

/**
 * TODO:
 * - Improve marker: make always showing, initialize the marker on "current time"
 * - Check what's the best way to make thick bars (currently we just put a very high random value)
 */
class PrecipitationNowcastViewHolder(parent: ViewGroup) : AbstractMainCardViewHolder(
    LayoutInflater.from(parent.context).inflate(R.layout.container_main_precipitation_nowcast_card, parent, false)
) {
    private val title = itemView.findViewById<TextView>(R.id.container_main_minutely_card_title)
    private val subtitle = itemView.findViewById<TextView>(R.id.container_main_minutely_card_subtitle)
    private val chartComposeView = itemView.findViewById<ComposeView>(R.id.container_main_minutely_chart_composeView)
    private val startText = itemView.findViewById<TextView>(R.id.container_main_minutely_card_minutelyStartText)
    private val centerText = itemView.findViewById<TextView>(R.id.container_main_minutely_card_minutelyCenterText)
    private val endText = itemView.findViewById<TextView>(R.id.container_main_minutely_card_minutelyEndText)
    private val startLine = itemView.findViewById<View>(R.id.container_main_minutely_card_minutelyStartLine)
    private val endLine = itemView.findViewById<View>(R.id.container_main_minutely_card_minutelyEndLine)

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

        title.setTextColor(colors[0])
        title.text = weather.getMinutelyTitle(context)
        subtitle.text = weather.getMinutelyDescription(context, location)

        val minutelyList = weather.minutelyForecast
        chartComposeView.setContent {
            BreezyWeatherTheme(
                lightTheme = MainThemeColorProvider.isLightTheme(context, location)
            ) {
                ContentView(location)
            }
        }
        chartComposeView.contentDescription =
            activity.getString(
                R.string.precipitation_between_time,
                minutelyList.first().date.getFormattedTime(location, context, context.is12Hour),
                minutelyList.last().date.getFormattedTime(location, context, context.is12Hour)
            )

        val firstTime = minutelyList.first().date
        val lastTime = Date(
            minutelyList.last().date.time + minutelyList.last().minuteInterval.minutes.inWholeMilliseconds
        )
        startText.text = firstTime.getFormattedTime(location, context, context.is12Hour)
        centerText.text = Date(firstTime.time + (lastTime.time - firstTime.time) / 2)
            .getFormattedTime(location, context, context.is12Hour)
        endText.text = lastTime.getFormattedTime(location, context, context.is12Hour)
        startText.setTextColor(MainThemeColorProvider.getColor(location, R.attr.colorBodyText))
        centerText.setTextColor(MainThemeColorProvider.getColor(location, R.attr.colorBodyText))
        endText.setTextColor(MainThemeColorProvider.getColor(location, R.attr.colorBodyText))

        startLine.setBackgroundColor(
            MainThemeColorProvider.getColor(location, com.google.android.material.R.attr.colorOutline)
        )
        endLine.setBackgroundColor(
            MainThemeColorProvider.getColor(location, com.google.android.material.R.attr.colorOutline)
        )
    }

    @Composable
    private fun ContentView(
        location: Location,
    ) {
        val view = LocalView.current
        val minutely = location.weather!!.minutelyForecastBy5Minutes
        val maxY = max(
            Precipitation.PRECIPITATION_HOURLY_HEAVY,
            minutely.maxOfOrNull { it.precipitationIntensity ?: 0.0 } ?: 0.0
        )
        val hasOnlyThresholdsValues = !minutely.any {
            it.precipitationIntensity !in arrayOf(
                null,
                0.0,
                Precipitation.PRECIPITATION_HOURLY_LIGHT,
                Precipitation.PRECIPITATION_HOURLY_MEDIUM,
                Precipitation.PRECIPITATION_HOURLY_HEAVY
            )
        }

        val modelProducer = remember { CartesianChartModelProducer() }

        val isTrendHorizontalLinesEnabled = SettingsManager.getInstance(context).isTrendHorizontalLinesEnabled
        val thresholdLineColor = if (context.isDarkMode) R.color.colorTextGrey else R.color.colorTextGrey2nd

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
            valueFormatter = MarkerLabelFormatterMinutelyDecorator(minutely, location, context, hasOnlyThresholdsValues)
        )

        LaunchedEffect(location) {
            modelProducer.runTransaction {
                columnSeries {
                    series(
                        x = minutely.indices.toList(),
                        y = minutely.map {
                            it.precipitationIntensity ?: 0
                        }
                    )
                }
            }
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
                    guideline = null,
                    tick = null, // Workaround: no custom ticks
                    label = null // Workaround: no custom ticks
                ),
                decorations = listOfNotNull(
                    /**
                     * Don’t show some thresholds when max precipitation is very high
                     * - Below heavy level: keep all 3 lines
                     * - Between heavy level and 2 * heavy level: keep light and heavy lines
                     * - Above 2 * heavy level: keep heavy line only
                     */
                    (maxY < Precipitation.PRECIPITATION_HOURLY_HEAVY * 2.0f).let {
                        if (it && SettingsManager.getInstance(context).isTrendHorizontalLinesEnabled) {
                            HorizontalLine(
                                y = { Precipitation.PRECIPITATION_HOURLY_LIGHT },
                                verticalLabelPosition = Position.Vertical.Bottom,
                                line = rememberLineComponent(
                                    fill = fill(colorResource(thresholdLineColor))
                                ),
                                labelComponent = rememberTextComponent(
                                    color = colorResource(thresholdLineColor)
                                ),
                                label = { context.getString(R.string.precipitation_intensity_light) }
                            )
                        } else {
                            null
                        }
                    },
                    (maxY <= Precipitation.PRECIPITATION_HOURLY_HEAVY).let {
                        if (it && isTrendHorizontalLinesEnabled) {
                            HorizontalLine(
                                y = { Precipitation.PRECIPITATION_HOURLY_MEDIUM },
                                verticalLabelPosition = Position.Vertical.Bottom,
                                line = rememberLineComponent(
                                    fill = fill(colorResource(thresholdLineColor))
                                ),
                                labelComponent = rememberTextComponent(
                                    color = colorResource(thresholdLineColor)
                                ),
                                label = { context.getString(R.string.precipitation_intensity_medium) }
                            )
                        } else {
                            null
                        }
                    },
                    if (SettingsManager.getInstance(context).isTrendHorizontalLinesEnabled) {
                        HorizontalLine(
                            y = { Precipitation.PRECIPITATION_HOURLY_HEAVY },
                            verticalLabelPosition = Position.Vertical.Bottom,
                            line = rememberLineComponent(
                                fill = fill(colorResource(thresholdLineColor))
                            ),
                            labelComponent = rememberTextComponent(
                                color = colorResource(thresholdLineColor)
                            ),
                            label = { context.getString(R.string.precipitation_intensity_heavy) }
                        )
                    } else {
                        null
                    }
                ),
                // TODO: Makes two markers instead of fading away when tapped somewhere else
                // persistentMarkers = mapOf(minutely.indexOfLast { it.date < Date() }.toFloat() to marker),
                marker = marker
            ),
            modelProducer = modelProducer,
            scrollState = rememberVicoScrollState(scrollEnabled = false),
            modifier = Modifier.handleNestedHorizontalDragGesture(view)
        )
    }
}

// Simplified version of https://stackoverflow.com/a/77321467
private fun Modifier.handleNestedHorizontalDragGesture(
    view: View,
) = this.pointerInput(Unit) {
    var initialX = 0f
    var initialY = 0f

    awaitEachGesture {
        do {
            val event = awaitPointerEvent(PointerEventPass.Initial)
            when (event.type) {
                PointerEventType.Press -> {
                    view.parent.requestDisallowInterceptTouchEvent(true)
                    event.changes.firstOrNull()?.let {
                        initialX = it.position.x
                        initialY = it.position.y
                    }
                }
                PointerEventType.Move -> {
                    event.changes.firstOrNull()?.let {
                        val changedX = it.previousPosition.x - initialX
                        val changedY = it.previousPosition.y - initialY

                        if (changedY.absoluteValue > changedX.absoluteValue) {
                            view.parent.requestDisallowInterceptTouchEvent(false)
                        } else {
                            view.parent.requestDisallowInterceptTouchEvent(true)
                        }
                    }
                }
            }
        } while (event.changes.any { it.pressed })
    }
}

private class MarkerLabelFormatterMinutelyDecorator(
    private val minutely: List<Minutely>,
    private val location: Location,
    private val aContext: Context,
    private val hasOnlyThresholdValues: Boolean,
) : DefaultCartesianMarker.ValueFormatter {

    override fun format(
        context: CartesianDrawingContext,
        targets: List<CartesianMarker.Target>,
    ): CharSequence {
        val model = targets.first()
        val startTime = minutely[model.x.toInt()].date
            .getFormattedTime(location, aContext, aContext.is12Hour)
        val endTime = (minutely[model.x.toInt()].date.time + 5.minutes.inWholeMilliseconds).toDate()
            .getFormattedTime(location, aContext, aContext.is12Hour)
        val quantityFormatted = if (hasOnlyThresholdValues) {
            when (minutely[model.x.toInt()].precipitationIntensity!!) {
                0.0 -> aContext.getString(R.string.precipitation_none)
                Precipitation.PRECIPITATION_HOURLY_LIGHT -> aContext.getString(R.string.precipitation_intensity_light)
                Precipitation.PRECIPITATION_HOURLY_MEDIUM -> aContext.getString(R.string.precipitation_intensity_medium)
                Precipitation.PRECIPITATION_HOURLY_HEAVY -> aContext.getString(R.string.precipitation_intensity_heavy)
                else ->
                    SettingsManager
                        .getInstance(aContext)
                        .precipitationIntensityUnit
                        .getValueText(aContext, minutely[model.x.toInt()].precipitationIntensity!!)
            }
        } else {
            SettingsManager
                .getInstance(aContext)
                .precipitationIntensityUnit
                .getValueText(aContext, minutely[model.x.toInt()].precipitationIntensity!!)
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
