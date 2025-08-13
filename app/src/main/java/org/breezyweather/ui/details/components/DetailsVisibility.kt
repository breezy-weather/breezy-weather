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

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import breezyweather.domain.location.model.Location
import breezyweather.domain.weather.model.Daily
import breezyweather.domain.weather.model.Hourly
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
import org.breezyweather.common.basic.models.options.appearance.DetailScreen
import org.breezyweather.common.basic.models.options.basic.UnitUtils
import org.breezyweather.common.basic.models.options.unit.getVisibilityDescription
import org.breezyweather.common.basic.models.options.unit.visibilityScaleThresholds
import org.breezyweather.common.extensions.currentLocale
import org.breezyweather.common.extensions.formatMeasure
import org.breezyweather.common.extensions.formatValue
import org.breezyweather.common.extensions.getFormattedTime
import org.breezyweather.common.extensions.is12Hour
import org.breezyweather.common.extensions.roundUpToNearestMultiplier
import org.breezyweather.common.extensions.toDate
import org.breezyweather.domain.settings.SettingsManager
import org.breezyweather.domain.weather.model.getFullLabel
import org.breezyweather.domain.weather.model.getRangeContentDescriptionSummary
import org.breezyweather.domain.weather.model.getRangeDescriptionSummary
import org.breezyweather.domain.weather.model.getRangeSummary
import org.breezyweather.ui.common.charts.BreezyLineChart
import org.breezyweather.ui.common.widgets.Material3ExpressiveCardListItem
import org.breezyweather.ui.settings.preference.bottomInsetItem
import org.breezyweather.unit.distance.Distance
import org.breezyweather.unit.distance.Distance.Companion.meters
import org.breezyweather.unit.distance.toDistance
import org.breezyweather.unit.formatting.UnitWidth
import java.util.Date
import kotlin.math.max

@Composable
fun DetailsVisibility(
    location: Location,
    hourlyList: ImmutableList<Hourly>,
    daily: Daily,
    defaultValue: Pair<Date, Distance>?,
    modifier: Modifier = Modifier,
) {
    val mappedValues = remember(hourlyList) {
        hourlyList
            .filter { it.visibility != null }
            .associate { it.date.time to it.visibility!! }
            .toImmutableMap()
    }
    var activeItem: Pair<Date, Distance>? by remember { mutableStateOf(null) }
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
                activeItem = null
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
            VisibilityHeader(location, daily, activeItem, defaultValue)
        }
        item {
            Spacer(modifier = Modifier.height(dimensionResource(R.dimen.normal_margin)))
        }
        if (mappedValues.size >= DetailScreen.CHART_MIN_COUNT) {
            item {
                VisibilityChart(location, mappedValues, daily, markerVisibilityListener)
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
            DetailsSectionHeader(stringResource(R.string.visibility_about))
        }
        item {
            DetailsCardText(stringResource(R.string.visibility_about_description))
        }
        item {
            Spacer(modifier = Modifier.height(dimensionResource(R.dimen.normal_margin)))
        }
        item {
            DetailsSectionHeader(stringResource(R.string.visibility_scale))
        }
        item {
            VisibilityScale()
        }
        bottomInsetItem()
    }
}

@Composable
fun VisibilityHeader(
    location: Location,
    daily: Daily,
    activeItem: Pair<Date, Distance>?,
    defaultValue: Pair<Date, Distance>?,
) {
    val context = LocalContext.current

    if (activeItem != null) {
        VisibilityItem(
            header = {
                TextFixedHeight(
                    text = activeItem.first.getFormattedTime(location, context, context.is12Hour),
                    style = MaterialTheme.typography.labelMedium
                )
            },
            visibility = activeItem.second
        )
    } else if (daily.visibility?.min != null && daily.visibility!!.max != null) {
        VisibilitySummary(location, daily)
    } else {
        VisibilityItem(
            header = {
                TextFixedHeight(
                    text = defaultValue?.first?.getFormattedTime(location, context, context.is12Hour) ?: "",
                    style = MaterialTheme.typography.labelMedium
                )
            },
            visibility = defaultValue?.second
        )
    }
}

@Composable
private fun VisibilityItem(
    header: @Composable () -> Unit,
    visibility: Distance?,
) {
    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxWidth()
    ) {
        header()
        TextFixedHeight(
            text = visibility?.let { vis ->
                UnitUtils.formatUnitsDifferentFontSize(
                    formattedMeasure = vis.formatMeasure(context),
                    fontSize = MaterialTheme.typography.headlineSmall.fontSize
                )
            } ?: buildAnnotatedString {},
            style = MaterialTheme.typography.displaySmall,
            modifier = Modifier
                .clearAndSetSemantics {
                    visibility?.let {
                        contentDescription = it.formatMeasure(context, unitWidth = UnitWidth.LONG)
                    }
                }
        )
        TextFixedHeight(
            text = getVisibilityDescription(context, visibility) ?: "",
            style = MaterialTheme.typography.labelMedium
        )
    }
}

@Composable
private fun VisibilitySummary(
    location: Location,
    daily: Daily,
) {
    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxWidth()
    ) {
        TextFixedHeight(
            text = daily.getFullLabel(location, context),
            style = MaterialTheme.typography.labelMedium
        )
        TextFixedHeight(
            text = buildAnnotatedString {
                daily.visibility?.getRangeSummary(context)?.let {
                    append(
                        UnitUtils.formatUnitsDifferentFontSize(
                            formattedMeasure = it,
                            fontSize = MaterialTheme.typography.headlineSmall.fontSize
                        )
                    )
                }
            },
            style = MaterialTheme.typography.displaySmall,
            modifier = Modifier
                .clearAndSetSemantics {
                    daily.visibility?.getRangeContentDescriptionSummary(context)?.let {
                        contentDescription = it
                    }
                }
        )
        TextFixedHeight(
            text = daily.visibility?.getRangeDescriptionSummary(context) ?: "",
            style = MaterialTheme.typography.labelMedium
        )
    }
}

@Composable
private fun VisibilityChart(
    location: Location,
    mappedValues: ImmutableMap<Long, Distance>,
    daily: Daily,
    markerVisibilityListener: CartesianMarkerVisibilityListener,
) {
    val context = LocalContext.current

    val distanceUnit = SettingsManager.getInstance(context).getDistanceUnit(context)
    val maxY = remember(mappedValues) {
        max(
            // This value makes it, once rounded, a minimum of 75000 ft
            22850.0, // TODO: Make this a const
            mappedValues.values.maxOf { it.inMeters }
        ).meters.toDouble(distanceUnit)
    }
    val step = remember(mappedValues) {
        distanceUnit.chartStep(maxY)
    }
    val maxYRounded = remember(mappedValues) {
        maxY.roundUpToNearestMultiplier(step)
    }

    val modelProducer = remember { CartesianChartModelProducer() }

    LaunchedEffect(location) {
        modelProducer.runTransaction {
            lineSeries {
                series(
                    x = mappedValues.keys,
                    y = mappedValues.values.map { it.toDouble(distanceUnit) }
                )
            }
        }
    }

    BreezyLineChart(
        location,
        modelProducer,
        daily.date,
        maxYRounded,
        { _, value, _ -> value.toDistance(distanceUnit).formatMeasure(context) },
        persistentListOf(
            persistentMapOf(
                20000.0.meters.toDouble(distanceUnit).toFloat() to Color(119, 141, 120),
                15000.0.meters.toDouble(distanceUnit).toFloat() to Color(91, 167, 99),
                9000.0.meters.toDouble(distanceUnit).toFloat() to Color(90, 169, 90),
                8000.0.meters.toDouble(distanceUnit).toFloat() to Color(98, 122, 160),
                6000.0.meters.toDouble(distanceUnit).toFloat() to Color(98, 122, 160),
                5000.0.meters.toDouble(distanceUnit).toFloat() to Color(167, 91, 91),
                2200.0.meters.toDouble(distanceUnit).toFloat() to Color(167, 91, 91),
                1600.0.meters.toDouble(distanceUnit).toFloat() to Color(162, 97, 160),
                0.0.meters.toDouble(distanceUnit).toFloat() to Color(166, 93, 165)
            )
        ),
        topAxisValueFormatter = { _, value, _ ->
            mappedValues.getOrElse(value.toLong()) { null }?.formatValue(context) ?: "-"
        },
        endAxisItemPlacer = remember {
            VerticalAxis.ItemPlacer.step({ step })
        },
        markerVisibilityListener = markerVisibilityListener
    )
}

// TODO: Accessibility
@Composable
fun VisibilityScale(
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val distanceUnit = SettingsManager.getInstance(context).getDistanceUnit(context)

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
                    stringResource(R.string.wind_strength_scale_description),
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1.5f)
                )
                Text(
                    distanceUnit.getDisplayName(context, context.currentLocale),
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.End,
                    modifier = Modifier.weight(1f)
                )
            }
            visibilityScaleThresholds.forEachIndexed { index, startingValue ->
                val startingValueFormatted = UnitUtils.formatDouble(
                    context,
                    startingValue.toDouble(distanceUnit),
                    distanceUnit.decimals.short
                )
                val endingValueFormatted = visibilityScaleThresholds.getOrElse(index + 1) { null }
                    ?.let {
                        " – ${
                            UnitUtils.formatDouble(
                                context,
                                it.toDouble(distanceUnit).minus(
                                    if (distanceUnit.decimals.short == 0) 1.0 else 0.1
                                ),
                                distanceUnit.decimals.short
                            )
                        }"
                    }
                    ?: "+"
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.small_margin)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = dimensionResource(R.dimen.small_margin))
                ) {
                    Text(
                        text = getVisibilityDescription(
                            context,
                            startingValue.toDouble(distanceUnit).plus(0.1).toDistance(distanceUnit)
                        )!!,
                        modifier = Modifier.weight(1.5f)
                    )
                    Text(
                        text = "$startingValueFormatted$endingValueFormatted",
                        textAlign = TextAlign.End,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}
