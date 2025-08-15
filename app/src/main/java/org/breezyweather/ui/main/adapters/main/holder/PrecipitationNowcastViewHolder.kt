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
import android.view.MotionEvent
import android.view.View
import android.view.View.OnTouchListener
import android.view.ViewGroup
import android.widget.TextView
import androidx.compose.ui.graphics.Color
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import breezyweather.domain.location.model.Location
import breezyweather.domain.weather.model.Precipitation
import com.patrykandpatrick.vico.compose.common.fill
import com.patrykandpatrick.vico.core.cartesian.CartesianChart
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
import com.patrykandpatrick.vico.core.common.Insets
import com.patrykandpatrick.vico.core.common.Position
import com.patrykandpatrick.vico.core.common.component.LineComponent
import com.patrykandpatrick.vico.core.common.component.Shadow
import com.patrykandpatrick.vico.core.common.component.ShapeComponent
import com.patrykandpatrick.vico.core.common.component.TextComponent
import com.patrykandpatrick.vico.core.common.shape.CorneredShape
import com.patrykandpatrick.vico.views.cartesian.CartesianChartView
import kotlinx.collections.immutable.ImmutableMap
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableMap
import kotlinx.coroutines.launch
import org.breezyweather.R
import org.breezyweather.common.activities.BreezyActivity
import org.breezyweather.common.extensions.formatMeasureIntensity
import org.breezyweather.common.extensions.getFormattedTime
import org.breezyweather.common.extensions.getThemeColor
import org.breezyweather.common.extensions.is12Hour
import org.breezyweather.common.extensions.isDarkMode
import org.breezyweather.common.extensions.pxToDp
import org.breezyweather.common.extensions.toDate
import org.breezyweather.domain.settings.SettingsManager
import org.breezyweather.domain.weather.model.getContentDescription
import org.breezyweather.domain.weather.model.getMinutelyDescription
import org.breezyweather.domain.weather.model.getMinutelyTitle
import org.breezyweather.ui.common.charts.SpecificHorizontalAxisItemPlacer
import org.breezyweather.ui.theme.ThemeManager
import org.breezyweather.ui.theme.resource.providers.ResourceProvider
import org.breezyweather.ui.theme.weatherView.WeatherView
import org.breezyweather.ui.theme.weatherView.WeatherViewController
import org.breezyweather.unit.precipitation.Precipitation.Companion.micrometers
import org.breezyweather.unit.precipitation.Precipitation.Companion.millimeters
import java.util.Date
import kotlin.math.abs
import kotlin.math.max
import kotlin.time.Duration.Companion.minutes

class PrecipitationNowcastViewHolder(parent: ViewGroup) : AbstractMainCardViewHolder(
    LayoutInflater.from(parent.context).inflate(R.layout.container_main_precipitation_nowcast_card, parent, false)
) {
    private val titleView: TextView = itemView.findViewById(R.id.nowcasting_block_title)
    private val subtitle: TextView = itemView.findViewById(R.id.nowcasting_block_subtitle)
    private val chartView: CartesianChartView = itemView.findViewById(R.id.container_main_minutely_chart)
    private val modelProducer = CartesianChartModelProducer()

    override fun onBindView(
        activity: BreezyActivity,
        location: Location,
        provider: ResourceProvider,
        listAnimationEnabled: Boolean,
        itemAnimationEnabled: Boolean,
    ) {
        super.onBindView(activity, location, provider, listAnimationEnabled, itemAnimationEnabled)

        val weather = location.weather ?: return
        titleView.text = weather.getMinutelyTitle(context)
        subtitle.text = weather.getMinutelyDescription(context, location)

        val minutelyList = weather.minutelyForecast
        val minX = minutelyList.first().date.time
        val maxX = minutelyList.last().endingDate.time

        val minutely = location.weather!!.minutelyForecastBy5Minutes
            .associate { it.date.time to (it.precipitationIntensity ?: 0.micrometers) }
            .toImmutableMap()
        val timeWithTicks = persistentListOf(minX.toDouble(), (minX + (maxX - minX) / 2).toDouble(), maxX.toDouble())

        val maxY = max(
            Precipitation.PRECIPITATION_HOURLY_HEAVY.millimeters.inMicrometers,
            minutely.values.max().inMicrometers
        )
        val trendHorizontalLines: ImmutableMap<Double, String> = buildMap {
            /**
             * Don’t show some thresholds when max precipitation is very high
             * - Below heavy level: keep all 3 lines
             * - Between heavy level and 2 * heavy level: keep light and heavy lines
             * - Above 2 * heavy level: keep heavy line only
             */
            if (maxY.micrometers.inMillimeters < Precipitation.PRECIPITATION_HOURLY_HEAVY * 2.0f) {
                put(
                    Precipitation.PRECIPITATION_HOURLY_LIGHT,
                    context.getString(R.string.precipitation_intensity_light)
                )
            }
            if (maxY.micrometers.inMillimeters <= Precipitation.PRECIPITATION_HOURLY_HEAVY) {
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
        val hasOnlyThresholdsValues = !minutely.values.any {
            it.inMillimeters !in arrayOf(
                null,
                0.0,
                Precipitation.PRECIPITATION_HOURLY_LIGHT,
                Precipitation.PRECIPITATION_HOURLY_MEDIUM,
                Precipitation.PRECIPITATION_HOURLY_HEAVY
            )
        }

        val isTrendHorizontalLinesEnabled = SettingsManager.getInstance(context).isTrendHorizontalLinesEnabled
        val lineColor = Color(context.getThemeColor(com.google.android.material.R.attr.colorOutline))
        val labelColor = ContextCompat.getColor(
            context,
            if (ThemeManager.isLightTheme(context, location)) {
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
        val marker = DefaultCartesianMarker(
            label = TextComponent(
                color = context.getThemeColor(com.google.android.material.R.attr.colorOnPrimary),
                background = ShapeComponent(
                    fill = Fill(context.getThemeColor(androidx.appcompat.R.attr.colorPrimary)),
                    CorneredShape.Pill,
                    shadow = Shadow(
                        radiusDp = LABEL_BACKGROUND_SHADOW_RADIUS_DP,
                        yDp = LABEL_BACKGROUND_SHADOW_DY_DP
                    )
                ),
                padding = Insets(
                    activity.pxToDp(activity.resources.getDimensionPixelSize(R.dimen.normal_margin)),
                    activity.pxToDp(activity.resources.getDimensionPixelSize(R.dimen.small_margin))
                ),
                textAlignment = Layout.Alignment.ALIGN_CENTER,
                minWidth = TextComponent.MinWidth.fixed(40f)
            ),
            guideline = LineComponent(fill = Fill(labelColor)),
            valueFormatter = MarkerLabelFormatterMinutelyDecorator(minutely, location, context, hasOnlyThresholdsValues)
        )

        val timeValueFormatter = CartesianValueFormatter { _, value, _ ->
            Date(value.toLong()).getFormattedTime(location, context, context.is12Hour)
        }

        chartView.chart = CartesianChart(
            ColumnCartesianLayer(
                ColumnCartesianLayer.ColumnProvider.series(
                    LineComponent(
                        fill = Fill(
                            ThemeManager
                                .getInstance(context)
                                .weatherThemeDelegate
                                .getThemeColors(
                                    context,
                                    WeatherView.WEATHER_KIND_RAINY,
                                    WeatherViewController.isDaylight(location) && !context.isDarkMode
                                )[0]
                        ),
                        thicknessDp = 500f,
                        shape = CorneredShape.rounded(allPercent = 15)
                    )
                ),
                rangeProvider = cartesianLayerRangeProvider
            ),
            bottomAxis = HorizontalAxis.bottom(
                line = LineComponent(fill = fill(lineColor)),
                label = TextComponent(
                    color = labelColor,
                    padding = Insets(4f, 2f)
                ),
                valueFormatter = timeValueFormatter,
                tick = LineComponent(fill = fill(lineColor)),
                guideline = null,
                itemPlacer = SpecificHorizontalAxisItemPlacer(timeWithTicks)
            ),
            decorations = if (isTrendHorizontalLinesEnabled) {
                trendHorizontalLines.entries.map { line ->
                    HorizontalLine(
                        y = { line.key.millimeters.inMicrometers },
                        verticalLabelPosition = Position.Vertical.Bottom,
                        line = LineComponent(fill = fill(lineColor)),
                        labelComponent = TextComponent(color = labelColor),
                        label = { line.value }
                    )
                }
            } else {
                emptyList()
            },
            marker = marker
        )
        chartView.animateIn = SettingsManager.getInstance(context).isElementsAnimationEnabled
        chartView.modelProducer = modelProducer
        chartView.contentDescription = minutelyList.getContentDescription(context, location)
        @Suppress("ClickableViewAccessibility")
        chartView.setOnTouchListener(
            object : OnTouchListener {
                private var mLastX = 0f
                private var mLastY = 0f

                @Suppress("ClickableViewAccessibility")
                override fun onTouch(v: View, event: MotionEvent): Boolean {
                    when (event.action) {
                        MotionEvent.ACTION_DOWN -> {
                            mLastX = event.x
                            mLastY = event.y
                        }
                        MotionEvent.ACTION_MOVE -> {
                            if (abs(event.x - mLastX) > abs(event.y - mLastY)) {
                                v.parent.requestDisallowInterceptTouchEvent(true)
                            }
                            mLastX = event.x
                            mLastY = event.y
                        }
                    }
                    return false
                }
            }
        )

        activity.lifecycleScope.launch {
            modelProducer.runTransaction {
                columnSeries {
                    series(
                        x = minutely.keys,
                        y = minutely.values.map { it.value }
                    )
                }
            }
        }
    }
}

private class MarkerLabelFormatterMinutelyDecorator(
    private val mappedValues: Map<Long, org.breezyweather.unit.precipitation.Precipitation>,
    private val location: Location,
    private val aContext: Context,
    private val hasOnlyThresholdValues: Boolean,
) : DefaultCartesianMarker.ValueFormatter {

    override fun format(
        context: CartesianDrawingContext,
        targets: List<CartesianMarker.Target>,
    ): CharSequence {
        val model = targets.first()
        val startTime = model.x.toLong().toDate()
            .getFormattedTime(location, aContext, aContext.is12Hour)
        val endTime = (model.x.toLong() + 5.minutes.inWholeMilliseconds).toDate()
            .getFormattedTime(location, aContext, aContext.is12Hour)
        val quantityFormatted = if (hasOnlyThresholdValues) {
            when (mappedValues.getOrElse(model.x.toLong()) { null }?.inMillimeters ?: 0.0) {
                0.0 -> aContext.getString(R.string.precipitation_none)
                Precipitation.PRECIPITATION_HOURLY_LIGHT -> aContext.getString(R.string.precipitation_intensity_light)
                Precipitation.PRECIPITATION_HOURLY_MEDIUM -> aContext.getString(R.string.precipitation_intensity_medium)
                Precipitation.PRECIPITATION_HOURLY_HEAVY -> aContext.getString(R.string.precipitation_intensity_heavy)
                else -> mappedValues.getOrElse(model.x.toLong()) { null }
                    ?.formatMeasureIntensity(aContext)
            }
        } else {
            mappedValues.getOrElse(model.x.toLong()) { null }
                ?.formatMeasureIntensity(aContext)
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
