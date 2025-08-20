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

package org.breezyweather.ui.details.components

import android.text.SpannableString
import android.text.style.RelativeSizeSpan
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.isTraversalGroup
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import breezyweather.domain.location.model.Location
import breezyweather.domain.weather.model.Daily
import breezyweather.domain.weather.model.Hourly
import breezyweather.domain.weather.model.Wind
import com.patrykandpatrick.vico.core.cartesian.axis.VerticalAxis
import com.patrykandpatrick.vico.core.cartesian.data.CartesianChartModelProducer
import com.patrykandpatrick.vico.core.cartesian.data.lineSeries
import com.patrykandpatrick.vico.core.cartesian.marker.CartesianMarker
import com.patrykandpatrick.vico.core.cartesian.marker.CartesianMarkerVisibilityListener
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.ImmutableMap
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.persistentMapOf
import kotlinx.collections.immutable.toImmutableMap
import org.breezyweather.R
import org.breezyweather.common.extensions.currentLocale
import org.breezyweather.common.extensions.formatMeasure
import org.breezyweather.common.extensions.getBeaufortScaleColor
import org.breezyweather.common.extensions.getBeaufortScaleStrength
import org.breezyweather.common.extensions.getColorResource
import org.breezyweather.common.extensions.getFormattedTime
import org.breezyweather.common.extensions.is12Hour
import org.breezyweather.common.extensions.roundUpToNearestMultiplier
import org.breezyweather.common.extensions.toDate
import org.breezyweather.common.options.appearance.DetailScreen
import org.breezyweather.common.utils.UnitUtils
import org.breezyweather.domain.settings.SettingsManager
import org.breezyweather.domain.weather.model.getDirection
import org.breezyweather.ui.common.charts.BreezyLineChart
import org.breezyweather.ui.common.widgets.Material3ExpressiveCardListItem
import org.breezyweather.unit.formatting.UnitWidth
import org.breezyweather.unit.speed.Speed.Companion.beaufort
import org.breezyweather.unit.speed.Speed.Companion.metersPerSecond
import org.breezyweather.unit.speed.SpeedUnit
import org.breezyweather.unit.speed.toSpeed
import java.util.Date
import kotlin.math.max

@Composable
fun DetailsWind(
    location: Location,
    hourlyList: ImmutableList<Hourly>,
    daily: Daily,
    defaultValue: Pair<Date, Wind>?,
    modifier: Modifier = Modifier,
) {
    val mappedValues = remember(hourlyList) {
        hourlyList
            .filter { it.wind?.speed != null }
            .associate { it.date.time to it.wind!! }
            .toImmutableMap()
    }
    var activeItem: Pair<Date, Wind>? by remember { mutableStateOf(null) }
    val markerVisibilityListener = remember {
        object : CartesianMarkerVisibilityListener {
            override fun onShown(marker: CartesianMarker, targets: List<CartesianMarker.Target>) {
                activeItem = targets.firstOrNull()?.let { target ->
                    mappedValues.getOrElse(target.x.toLong()) { null }?.let {
                        Pair(target.x.toLong().toDate(), it)
                    }
                }
            }

            override fun onUpdated(marker: CartesianMarker, targets: List<CartesianMarker.Target>) {
                onShown(marker, targets)
            }

            override fun onHidden(marker: CartesianMarker) {
                activeItem = defaultValue
            }
        }
    }

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(
            horizontal = dimensionResource(R.dimen.normal_margin),
            vertical = dimensionResource(R.dimen.small_margin)
        )
    ) {
        item {
            WindHeader(location, daily, activeItem, defaultValue)
        }
        item {
            Spacer(modifier = Modifier.height(dimensionResource(R.dimen.normal_margin)))
        }
        if (mappedValues.size >= DetailScreen.CHART_MIN_COUNT) {
            item {
                WindChart(location, mappedValues, daily, markerVisibilityListener)
            }
        } else {
            item {
                UnavailableChart(mappedValues.size)
            }
        }
        item {
            Spacer(modifier = Modifier.height(dimensionResource(R.dimen.normal_margin)))
        }
        // TODO: Daily summary
        item {
            DetailsSectionHeader(stringResource(R.string.wind_speed_about))
        }
        item {
            DetailsCardText(stringResource(R.string.wind_speed_about_description))
        }
        item {
            Spacer(modifier = Modifier.height(dimensionResource(R.dimen.normal_margin)))
        }
        item {
            DetailsSectionHeader(stringResource(R.string.wind_strength_scale))
        }
        item {
            WindScale()
        }
        bottomDetailsInset()
    }
}

@Composable
fun WindHeader(
    location: Location,
    daily: Daily,
    activeItem: Pair<Date, Wind>?,
    defaultValue: Pair<Date, Wind>?,
) {
    val context = LocalContext.current

    if (activeItem != null) {
        WindItem(
            {
                TextFixedHeight(
                    text = activeItem.first.getFormattedTime(location, context, context.is12Hour),
                    style = MaterialTheme.typography.labelMedium
                )
            },
            activeItem.second
        )
    } else if (daily.day?.wind?.isValid == true || daily.night?.wind?.isValid == true) {
        WindSummary(daily.day?.wind, daily.night?.wind)
    } else {
        WindItem(
            {
                TextFixedHeight(
                    text = defaultValue?.first?.getFormattedTime(location, context, context.is12Hour) ?: "",
                    style = MaterialTheme.typography.labelMedium
                )
            },
            activeItem?.second
        )
    }
}

@Composable
private fun WindSummary(
    daytimeWind: Wind?,
    nighttimeWind: Wind?,
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.small_margin)),
        modifier = Modifier
            .fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .semantics { isTraversalGroup = true }
        ) {
            daytimeWind?.let {
                WindItem(
                    { DaytimeLabel() },
                    it
                )
            }
        }
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .semantics { isTraversalGroup = true }
        ) {
            nighttimeWind?.let {
                WindItem(
                    { NighttimeLabelWithInfo() },
                    it
                )
            }
        }
    }
}

@Composable
private fun WindItem(
    header: @Composable () -> Unit,
    wind: Wind?,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current

    Column(
        modifier = modifier.fillMaxWidth()
    ) {
        header()
        TextFixedHeight(
            text = buildAnnotatedString {
                wind?.speed?.let { speed ->
                    append(
                        UnitUtils.formatUnitsDifferentFontSize(
                            formattedMeasure = speed.formatMeasure(context),
                            fontSize = MaterialTheme.typography.headlineSmall.fontSize
                        )
                    )
                    wind.arrow?.let {
                        append(" ")
                        append(it)
                    }
                }
            },
            style = MaterialTheme.typography.displaySmall,
            modifier = Modifier
                .clearAndSetSemantics {
                    wind?.speed?.let { speed ->
                        contentDescription = speed.formatMeasure(context, unitWidth = UnitWidth.LONG) +
                            (
                                wind.arrow?.let {
                                    context.getString(org.breezyweather.unit.R.string.locale_separator) +
                                        wind.getDirection(context, short = false)!!
                                } ?: ""
                                )
                    }
                }
        )
        if (wind?.gusts != null && wind.speed != null && wind.gusts!! > wind.speed!!) {
            TextFixedHeight(
                text = stringResource(R.string.wind_gusts_short) +
                    stringResource(R.string.colon_separator) +
                    wind.gusts!!.formatMeasure(context),
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                overflow = TextOverflow.StartEllipsis,
                modifier = Modifier
                    .clearAndSetSemantics {
                        contentDescription = context.getString(R.string.wind_gusts_short) +
                            context.getString(R.string.colon_separator) +
                            wind.gusts!!.formatMeasure(context, unitWidth = UnitWidth.LONG)
                    }
            )
        } else {
            TextFixedHeight(
                text = "",
                style = MaterialTheme.typography.labelLarge,
                modifier = Modifier.clearAndSetSemantics {}
            )
        }
    }
}

@Composable
private fun WindChart(
    location: Location,
    mappedValues: ImmutableMap<Long, Wind>,
    daily: Daily,
    markerVisibilityListener: CartesianMarkerVisibilityListener,
) {
    val context = LocalContext.current

    val speedUnit = SettingsManager.getInstance(context).getSpeedUnit(context)
    val step = speedUnit.chartStep
    val maxY = remember(mappedValues) {
        max(
            15.0, // TODO: Make this a const
            max(
                mappedValues.values.maxOfOrNull { it.gusts?.inMetersPerSecond ?: 0.0 } ?: 0.0,
                mappedValues.values.maxOf { it.speed!!.inMetersPerSecond }
            )
        ).metersPerSecond.toDouble(speedUnit).roundUpToNearestMultiplier(step)
    }

    val modelProducer = remember { CartesianChartModelProducer() }

    LaunchedEffect(location) {
        modelProducer.runTransaction {
            lineSeries {
                series(
                    x = mappedValues.keys,
                    y = mappedValues.values.map { it.speed!!.toDouble(speedUnit) }
                )
                if (mappedValues.values.any { it.gusts != null }) {
                    series(
                        x = mappedValues.keys,
                        y = mappedValues.values.map {
                            (it.gusts?.takeIf { g -> g > it.speed!! } ?: it.speed!!).toDouble(speedUnit)
                        }
                    )
                }
            }
        }
    }

    BreezyLineChart(
        location = location,
        modelProducer = modelProducer,
        theDay = daily.date,
        maxY = maxY,
        endAxisValueFormatter = remember { { _, value, _ -> value.toSpeed(speedUnit).formatMeasure(context) } },
        colors = remember {
            persistentListOf(
                persistentMapOf(
                    104.0.metersPerSecond.toDouble(speedUnit).toFloat() to Color(128, 128, 128),
                    77.0.metersPerSecond.toDouble(speedUnit).toFloat() to Color(205, 202, 112),
                    51.0.metersPerSecond.toDouble(speedUnit).toFloat() to Color(219, 212, 135),
                    46.0.metersPerSecond.toDouble(speedUnit).toFloat() to Color(231, 215, 215),
                    36.0.metersPerSecond.toDouble(speedUnit).toFloat() to
                        context.getColorResource(R.color.windStrength_bf12),
                    30.5.metersPerSecond.toDouble(speedUnit).toFloat() to
                        context.getColorResource(R.color.windStrength_bf11),
                    26.4.metersPerSecond.toDouble(speedUnit).toFloat() to
                        context.getColorResource(R.color.windStrength_bf10),
                    24.475.metersPerSecond.toDouble(speedUnit).toFloat() to Color(109, 97, 163),
                    22.55.metersPerSecond.toDouble(speedUnit).toFloat() to
                        context.getColorResource(R.color.windStrength_bf9),
                    18.9.metersPerSecond.toDouble(speedUnit).toFloat() to
                        context.getColorResource(R.color.windStrength_bf8),
                    17.175.metersPerSecond.toDouble(speedUnit).toFloat() to Color(129, 58, 78),
                    15.45.metersPerSecond.toDouble(speedUnit).toFloat() to
                        context.getColorResource(R.color.windStrength_bf7),
                    13.85.metersPerSecond.toDouble(speedUnit).toFloat() to Color(159, 127, 58),
                    12.25.metersPerSecond.toDouble(speedUnit).toFloat() to
                        context.getColorResource(R.color.windStrength_bf6),
                    9.3.metersPerSecond.toDouble(speedUnit).toFloat() to
                        context.getColorResource(R.color.windStrength_bf5),
                    6.7.metersPerSecond.toDouble(speedUnit).toFloat() to
                        context.getColorResource(R.color.windStrength_bf4),
                    4.4.metersPerSecond.toDouble(speedUnit).toFloat() to
                        context.getColorResource(R.color.windStrength_bf3),
                    2.4.metersPerSecond.toDouble(speedUnit).toFloat() to
                        context.getColorResource(R.color.windStrength_bf2),
                    1.0.metersPerSecond.toDouble(speedUnit).toFloat() to
                        context.getColorResource(R.color.windStrength_bf1),
                    0.0.metersPerSecond.toDouble(speedUnit).toFloat() to
                        context.getColorResource(R.color.windStrength_bf0)
                ),
                persistentMapOf(
                    104.0.metersPerSecond.toDouble(speedUnit).toFloat() to Color(128, 128, 128, 160),
                    77.0.metersPerSecond.toDouble(speedUnit).toFloat() to Color(205, 202, 112, 160),
                    51.0.metersPerSecond.toDouble(speedUnit).toFloat() to Color(219, 212, 135, 160),
                    46.0.metersPerSecond.toDouble(speedUnit).toFloat() to Color(231, 215, 215, 160),
                    36.0.metersPerSecond.toDouble(speedUnit).toFloat() to
                        context.getColorResource(R.color.windStrength_bf12).copy(alpha = 160f.div(255f)),
                    30.5.metersPerSecond.toDouble(speedUnit).toFloat() to
                        context.getColorResource(R.color.windStrength_bf11).copy(alpha = 160f.div(255f)),
                    26.4.metersPerSecond.toDouble(speedUnit).toFloat() to
                        context.getColorResource(R.color.windStrength_bf10).copy(alpha = 160f.div(255f)),
                    24.475.metersPerSecond.toDouble(speedUnit).toFloat() to Color(109, 97, 163, 160),
                    22.55.metersPerSecond.toDouble(speedUnit).toFloat() to
                        context.getColorResource(R.color.windStrength_bf9).copy(alpha = 160f.div(255f)),
                    18.9.metersPerSecond.toDouble(speedUnit).toFloat() to
                        context.getColorResource(R.color.windStrength_bf8).copy(alpha = 160f.div(255f)),
                    17.175.metersPerSecond.toDouble(speedUnit).toFloat() to Color(129, 58, 78, 160),
                    15.45.metersPerSecond.toDouble(speedUnit).toFloat() to
                        context.getColorResource(R.color.windStrength_bf7).copy(alpha = 160f.div(255f)),
                    13.85.metersPerSecond.toDouble(speedUnit).toFloat() to Color(159, 127, 58, 160),
                    12.25.metersPerSecond.toDouble(speedUnit).toFloat() to
                        context.getColorResource(R.color.windStrength_bf6).copy(alpha = 160f.div(255f)),
                    9.3.metersPerSecond.toDouble(speedUnit).toFloat() to
                        context.getColorResource(R.color.windStrength_bf5).copy(alpha = 160f.div(255f)),
                    6.7.metersPerSecond.toDouble(speedUnit).toFloat() to
                        context.getColorResource(R.color.windStrength_bf4).copy(alpha = 160f.div(255f)),
                    4.4.metersPerSecond.toDouble(speedUnit).toFloat() to
                        context.getColorResource(R.color.windStrength_bf3).copy(alpha = 160f.div(255f)),
                    2.4.metersPerSecond.toDouble(speedUnit).toFloat() to
                        context.getColorResource(R.color.windStrength_bf2).copy(alpha = 160f.div(255f)),
                    1.0.metersPerSecond.toDouble(speedUnit).toFloat() to
                        context.getColorResource(R.color.windStrength_bf1).copy(alpha = 160f.div(255f)),
                    0.0.metersPerSecond.toDouble(speedUnit).toFloat() to
                        context.getColorResource(R.color.windStrength_bf0).copy(alpha = 160f.div(255f))
                )
            )
        },
        topAxisValueFormatter = remember(mappedValues) {
            { _, value, _ ->
                val arrow = mappedValues.getOrElse(value.toLong()) { null }?.arrow ?: "-"
                SpannableString(arrow).apply {
                    setSpan(RelativeSizeSpan(2f), 0, arrow.length, 0)
                }
            }
        },
        trendHorizontalLines = remember {
            buildMap {
                if (maxY > 7.beaufort.toDouble(speedUnit)) {
                    put(7.beaufort.toDouble(speedUnit), 7.beaufort.getBeaufortScaleStrength(context)!!)
                }
                // TODO: Make this a const:
                if (maxY < (7.beaufort.inMetersPerSecond + 5.0).metersPerSecond.toDouble(speedUnit)) {
                    put(3.beaufort.toDouble(speedUnit), 3.beaufort.getBeaufortScaleStrength(context)!!)
                }
            }.toImmutableMap()
        },
        endAxisItemPlacer = remember { VerticalAxis.ItemPlacer.step({ step }) },
        markerVisibilityListener = markerVisibilityListener
    )
}

// TODO: Accessibility
@Composable
fun WindScale(
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val speedUnit = SettingsManager.getInstance(context).getSpeedUnit(context).let {
        if (it == SpeedUnit.BEAUFORT_SCALE) SpeedUnit.getDefaultUnit(context.currentLocale) else it
    }

    Material3ExpressiveCardListItem(
        modifier = modifier,
        isFirst = true,
        isLast = true
    ) {
        Column(
            modifier = Modifier.padding(
                horizontal = dimensionResource(R.dimen.normal_margin),
                vertical = dimensionResource(R.dimen.small_margin)
            )
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.small_margin)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    SpeedUnit.BEAUFORT_SCALE.getDisplayName(context, context.currentLocale),
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f)
                )
                Text(
                    stringResource(R.string.wind_strength_scale_description),
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1.5f)
                )
                Text(
                    speedUnit.getDisplayName(context, context.currentLocale),
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.End,
                    modifier = Modifier.weight(1.5f)
                )
            }
            (0..12).forEach { index ->
                val beaufort = index.beaufort
                val nextBeaufort = if (index < 12) (index + 1).beaufort else null
                val startingValueFormatted = beaufort.formatValue(
                    unit = speedUnit,
                    locale = context.currentLocale,
                    useNumberFormatter = SettingsManager.getInstance(context).useNumberFormatter,
                    useMeasureFormat = SettingsManager.getInstance(context).useMeasureFormat
                )
                val endingValueFormatted = nextBeaufort?.let {
                    " – ${
                        it.toDouble(speedUnit).minus(0.1).toSpeed(speedUnit).formatValue(
                            unit = speedUnit,
                            locale = context.currentLocale,
                            useNumberFormatter = SettingsManager.getInstance(context).useNumberFormatter,
                            useMeasureFormat = SettingsManager.getInstance(context).useMeasureFormat
                        )
                    }"
                } ?: "+"
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.small_margin)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = dimensionResource(R.dimen.small_margin))
                ) {
                    Row(
                        modifier = Modifier.weight(1f),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.small_margin))
                    ) {
                        Icon(
                            modifier = Modifier
                                .size(dimensionResource(R.dimen.material_icon_size)),
                            painter = painterResource(R.drawable.ic_circle),
                            contentDescription = null,
                            tint = Color(index.beaufort.getBeaufortScaleColor(context))
                        )
                        Text(
                            text = UnitUtils.formatInt(context, index)
                        )
                    }
                    Text(
                        text = beaufort.getBeaufortScaleStrength(context)!!,
                        modifier = Modifier.weight(1.5f)
                    )
                    Text(
                        text = "$startingValueFormatted$endingValueFormatted",
                        textAlign = TextAlign.End,
                        modifier = Modifier.weight(1.5f)
                    )
                }
            }
        }
    }
}
