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

package org.breezyweather.main.adapters.main.holder

import android.content.Context
import android.text.Layout
import android.text.Spannable
import android.text.SpannableStringBuilder
import android.text.style.ForegroundColorSpan
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.unit.dp
import breezyweather.domain.location.model.Location
import breezyweather.domain.weather.model.Minutely
import com.patrykandpatrick.vico.compose.axis.horizontal.rememberBottomAxis
import com.patrykandpatrick.vico.compose.chart.CartesianChartHost
import com.patrykandpatrick.vico.compose.chart.decoration.rememberHorizontalLine
import com.patrykandpatrick.vico.compose.chart.layer.rememberColumnCartesianLayer
import com.patrykandpatrick.vico.compose.chart.rememberCartesianChart
import com.patrykandpatrick.vico.compose.chart.scroll.rememberVicoScrollState
import com.patrykandpatrick.vico.compose.component.fixed
import com.patrykandpatrick.vico.compose.component.marker.rememberMarkerComponent
import com.patrykandpatrick.vico.compose.component.rememberLineComponent
import com.patrykandpatrick.vico.compose.component.rememberShapeComponent
import com.patrykandpatrick.vico.compose.component.rememberTextComponent
import com.patrykandpatrick.vico.compose.component.shape.markerCorneredShape
import com.patrykandpatrick.vico.compose.dimensions.dimensionsOf
import com.patrykandpatrick.vico.core.chart.layer.ColumnCartesianLayer
import com.patrykandpatrick.vico.core.chart.values.AxisValueOverrider
import com.patrykandpatrick.vico.core.chart.values.ChartValues
import com.patrykandpatrick.vico.core.component.shape.Shapes
import com.patrykandpatrick.vico.core.component.shape.cornered.Corner
import com.patrykandpatrick.vico.core.component.text.TextComponent
import com.patrykandpatrick.vico.core.component.text.VerticalPosition
import com.patrykandpatrick.vico.core.marker.Marker
import com.patrykandpatrick.vico.core.marker.MarkerLabelFormatter
import com.patrykandpatrick.vico.core.model.CartesianChartModelProducer
import com.patrykandpatrick.vico.core.model.columnSeries
import org.breezyweather.R
import org.breezyweather.common.basic.GeoActivity
import org.breezyweather.common.extensions.getFormattedTime
import org.breezyweather.common.extensions.is12Hour
import org.breezyweather.common.extensions.isDarkMode
import org.breezyweather.common.extensions.toDate
import org.breezyweather.domain.weather.model.getMinutelyDescription
import org.breezyweather.domain.weather.model.getMinutelyTitle
import org.breezyweather.main.utils.MainThemeColorProvider
import org.breezyweather.settings.SettingsManager
import org.breezyweather.theme.ThemeManager
import org.breezyweather.theme.compose.BreezyWeatherTheme
import org.breezyweather.theme.resource.providers.ResourceProvider
import org.breezyweather.theme.weatherView.WeatherViewController
import java.util.Date
import kotlin.math.max

/**
 * TODO:
 * - Improve marker: make always showing, initialize the marker on "current time"
 * - Define better values for light/medium/heavy precipitation
 * - Check what's the best way to make thick bars (currently we just put a very high random value)
 */
class PrecipitationNowcastViewHolder(
    parent: ViewGroup
) : AbstractMainCardViewHolder(
    LayoutInflater
        .from(parent.context)
        .inflate(R.layout.container_main_precipitation_nowcast_card, parent, false)
) {
    private val minutelyTitle = itemView.findViewById<TextView>(R.id.container_main_minutely_card_title)
    private val minutelySubtitle = itemView.findViewById<TextView>(R.id.container_main_minutely_card_subtitle)
    private val minutelyChartComposeView = itemView.findViewById<ComposeView>(R.id.container_main_minutely_chart_composeView)
    private val minutelyStartText = itemView.findViewById<TextView>(R.id.container_main_minutely_card_minutelyStartText)
    private val minutelyCenterText = itemView.findViewById<TextView>(R.id.container_main_minutely_card_minutelyCenterText)
    private val minutelyEndText = itemView.findViewById<TextView>(R.id.container_main_minutely_card_minutelyEndText)
    private val minutelyStartLine = itemView.findViewById<View>(R.id.container_main_minutely_card_minutelyStartLine)
    private val minutelyEndLine = itemView.findViewById<View>(R.id.container_main_minutely_card_minutelyEndLine)

    override fun onBindView(
        activity: GeoActivity,
        location: Location,
        provider: ResourceProvider,
        listAnimationEnabled: Boolean,
        itemAnimationEnabled: Boolean,
        firstCard: Boolean
    ) {
        super.onBindView(
            activity,
            location,
            provider,
            listAnimationEnabled,
            itemAnimationEnabled,
            firstCard
        )

        val weather = location.weather ?: return
        val colors = ThemeManager
            .getInstance(context)
            .weatherThemeDelegate
            .getThemeColors(
                context,
                WeatherViewController.getWeatherKind(location),
                WeatherViewController.isDaylight(location)
            )

        minutelyTitle.setTextColor(colors[0])
        minutelyTitle.text = weather.getMinutelyTitle(context)
        minutelySubtitle.text = weather.getMinutelyDescription(context, location)

        val minutelyList = weather.minutelyForecast
        minutelyChartComposeView.setContent {
            BreezyWeatherTheme(
                lightTheme = MainThemeColorProvider.isLightTheme(context, location)
            ) {
                ContentView(location)
            }
        }
        minutelyChartComposeView.contentDescription =
            activity.getString(
                R.string.precipitation_between_time,
                minutelyList.first().date.getFormattedTime(location, context, context.is12Hour),
                minutelyList.last().date.getFormattedTime(location, context, context.is12Hour)
            )

        val firstTime = minutelyList.first().date
        val lastTime = Date(minutelyList.last().date.time +
            minutelyList.last().minuteInterval * 60 * 1000)
        minutelyStartText.text = firstTime.getFormattedTime(location, context, context.is12Hour)
        minutelyCenterText.text = Date(firstTime.time + (lastTime.time - firstTime.time) / 2)
            .getFormattedTime(location, context, context.is12Hour)
        minutelyEndText.text = lastTime.getFormattedTime(location, context, context.is12Hour)
        minutelyStartText.setTextColor(
            MainThemeColorProvider.getColor(location, R.attr.colorBodyText)
        )
        minutelyCenterText.setTextColor(
            MainThemeColorProvider.getColor(location, R.attr.colorBodyText)
        )
        minutelyEndText.setTextColor(
            MainThemeColorProvider.getColor(location, R.attr.colorBodyText)
        )

        minutelyStartLine.setBackgroundColor(
            MainThemeColorProvider.getColor(
                location, com.google.android.material.R.attr.colorOutline
            )
        )
        minutelyEndLine.setBackgroundColor(
            MainThemeColorProvider.getColor(
                location, com.google.android.material.R.attr.colorOutline
            )
        )
    }

    @Composable
    private fun ContentView(
        location: Location
    ) {
        val minutely = location.weather!!.minutelyForecastBy5Minutes

        val modelProducer = remember { CartesianChartModelProducer.build() }

        val thresholdLineColor = if (context.isDarkMode) {
            R.color.colorTextGrey
        } else R.color.colorTextGrey2nd

        val axisValueOverrider = AxisValueOverrider.fixed(
            maxY = max(
                Minutely.PRECIPITATION_HEAVY,
                minutely.maxOfOrNull { it.precipitationIntensity ?: 0.0 } ?: 0.0
            ).toFloat()
        )
        val marker = rememberMarkerComponent(
            label = rememberTextComponent(
                color = Color(
                    MainThemeColorProvider.getColor(
                        location, com.google.android.material.R.attr.colorOnPrimary
                    )
                ),
                background = rememberShapeComponent(
                    Shapes.markerCorneredShape(Corner.FullyRounded),
                    Color(
                        MainThemeColorProvider.getColor(
                            location, androidx.appcompat.R.attr.colorPrimary
                        )
                    )
                ).setShadow(
                    radius = LABEL_BACKGROUND_SHADOW_RADIUS_DP,
                    dy = LABEL_BACKGROUND_SHADOW_DY_DP,
                    applyElevationOverlay = true,
                ),
                padding = dimensionsOf(
                    dimensionResource(R.dimen.normal_margin),
                    dimensionResource(R.dimen.little_margin)
                ),
                textAlignment = Layout.Alignment.ALIGN_CENTER,
                minWidth = TextComponent.MinWidth.fixed(40.dp),
            ),
            labelFormatter = MarkerLabelFormatterMinutelyDecorator(
                minutely, location, context
            )
        )

        LaunchedEffect(location) {
            modelProducer.tryRunTransaction {
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
                            color = Color(
                                ThemeManager
                                    .getInstance(context)
                                    .weatherThemeDelegate
                                    .getThemeColors(
                                        context,
                                        WeatherViewController.getWeatherKind(location),
                                        WeatherViewController.isDaylight(location)
                                    )[0]
                            ),
                            thickness = 500.dp
                        )
                    ),
                    axisValueOverrider = axisValueOverrider
                ),
                bottomAxis = rememberBottomAxis(
                    guideline = null,
                    tick = null, // Workaround: no custom ticks
                    label = null, // Workaround: no custom ticks
                ),
                decorations = listOf(
                    rememberHorizontalLine(
                        y = { Minutely.PRECIPITATION_LIGHT.toFloat() },
                        verticalLabelPosition = VerticalPosition.Bottom,
                        line = rememberLineComponent(
                            color = colorResource(thresholdLineColor)
                        ),
                        labelComponent = rememberTextComponent(
                            color = colorResource(thresholdLineColor)
                        ),
                        label = { context.getString(R.string.precipitation_intensity_light) }
                    ),
                    rememberHorizontalLine(
                        y = { Minutely.PRECIPITATION_MEDIUM.toFloat() },
                        verticalLabelPosition = VerticalPosition.Bottom,
                        line = rememberLineComponent(
                            color = colorResource(thresholdLineColor)
                        ),
                        labelComponent = rememberTextComponent(
                            color = colorResource(thresholdLineColor)
                        ),
                        label = { context.getString(R.string.precipitation_intensity_medium) }
                    ),
                    rememberHorizontalLine(
                        y = { Minutely.PRECIPITATION_HEAVY.toFloat() },
                        verticalLabelPosition = VerticalPosition.Bottom,
                        line = rememberLineComponent(
                            color = colorResource(thresholdLineColor)
                        ),
                        labelComponent = rememberTextComponent(
                            color = colorResource(thresholdLineColor)
                        ),
                        label = { context.getString(R.string.precipitation_intensity_heavy) }
                    )
                ),
                // TODO: Makes two markers instead of fading away when tapped somewhere else
                //persistentMarkers = mapOf(minutely.indexOfLast { it.date < Date() }.toFloat() to marker),
            ),
            modelProducer,
            marker = marker,
            scrollState = rememberVicoScrollState(scrollEnabled = false)
        )
    }
}

private class MarkerLabelFormatterMinutelyDecorator(
    private val minutely: List<Minutely>,
    private val location: Location,
    private val context: Context
) : MarkerLabelFormatter {
    override fun getLabel(
        markedEntries: List<Marker.EntryModel>,
        chartValues: ChartValues
    ): CharSequence {
        val model = markedEntries.first()
        val startTime = minutely[model.entry.x.toInt()].date.getFormattedTime(location, context, context.is12Hour)
        val endTime = (minutely[model.entry.x.toInt()].date.time + 5.times(60).times(1000))
            .toDate().getFormattedTime(location, context, context.is12Hour)
        val quantityFormatted = SettingsManager
            .getInstance(context)
            .precipitationIntensityUnit
            .getValueText(context, minutely[model.entry.x.toInt()].precipitationIntensity!!.toDouble())

        return SpannableStringBuilder().append(
            startTime,
            "-",
            endTime,
            context.getString(R.string.colon_separator),
            quantityFormatted
        )
    }
}

private const val LABEL_BACKGROUND_SHADOW_RADIUS_DP = 4f
private const val LABEL_BACKGROUND_SHADOW_DY_DP = 2f
