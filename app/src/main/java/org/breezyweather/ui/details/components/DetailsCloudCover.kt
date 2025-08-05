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

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import breezyweather.domain.location.model.Location
import breezyweather.domain.weather.model.Daily
import breezyweather.domain.weather.model.Hourly
import com.patrykandpatrick.vico.core.cartesian.axis.VerticalAxis
import com.patrykandpatrick.vico.core.cartesian.data.CartesianChartModelProducer
import com.patrykandpatrick.vico.core.cartesian.data.CartesianValueFormatter
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
import org.breezyweather.common.basic.models.options.unit.DurationUnit
import org.breezyweather.common.basic.models.options.unit.getCloudCoverDescription
import org.breezyweather.common.extensions.getFormattedTime
import org.breezyweather.common.extensions.is12Hour
import org.breezyweather.common.extensions.toDate
import org.breezyweather.domain.weather.model.getFullLabel
import org.breezyweather.domain.weather.model.getRangeDescriptionSummary
import org.breezyweather.domain.weather.model.getRangeSummary
import org.breezyweather.ui.common.charts.BreezyLineChart
import org.breezyweather.ui.settings.preference.bottomInsetItem
import java.util.Date

@Composable
fun DetailsCloudCover(
    location: Location,
    hourlyList: ImmutableList<Hourly>,
    daily: Daily,
    defaultValue: Pair<Date, Int>?,
    modifier: Modifier = Modifier,
) {
    val mappedValues = remember(hourlyList) {
        hourlyList
            .filter { it.cloudCover != null }
            .associate { it.date.time to it.cloudCover!! }
            .toImmutableMap()
    }
    var activeItem: Pair<Date, Int>? by remember { mutableStateOf(null) }
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
            CloudCoverHeader(location, daily, activeItem, defaultValue)
        }
        item {
            Spacer(modifier = Modifier.height(dimensionResource(R.dimen.normal_margin)))
        }
        if (mappedValues.size >= DetailScreen.CHART_MIN_COUNT) {
            item {
                CloudCoverChart(location, mappedValues, daily, markerVisibilityListener)
            }
        } else {
            item {
                UnavailableChart(mappedValues.size)
            }
        }
        daily.sunshineDuration?.let { sunshineDuration ->
            item {
                Spacer(modifier = Modifier.height(dimensionResource(R.dimen.normal_margin)))
            }
            item {
                SunshineItem(sunshineDuration)
            }
        }
        item {
            Spacer(modifier = Modifier.height(dimensionResource(R.dimen.normal_margin)))
        }
        daily.cloudCover?.summary?.let {
            if (it.isNotEmpty()) {
                item {
                    DetailsSectionHeader(stringResource(R.string.daily_summary))
                }
                item {
                    DetailsCardText(it)
                }
            }
        }
        item {
            DetailsSectionHeader(stringResource(R.string.cloud_cover_about))
        }
        item {
            DetailsCardText(stringResource(R.string.cloud_cover_about_description))
        }
        bottomInsetItem()
    }
}

@Composable
private fun CloudCoverHeader(
    location: Location,
    daily: Daily,
    activeItem: Pair<Date, Int>?,
    defaultValue: Pair<Date, Int>?,
) {
    val context = LocalContext.current

    if (activeItem != null) {
        CloudCoverItem(
            header = activeItem.first.getFormattedTime(location, context, context.is12Hour),
            cloudCover = activeItem.second
        )
    } else if (daily.cloudCover?.min != null && daily.cloudCover!!.max != null) {
        CloudCoverSummary(location, daily)
    } else {
        CloudCoverItem(
            header = defaultValue?.first?.getFormattedTime(location, context, context.is12Hour),
            cloudCover = defaultValue?.second
        )
    }
}

@Composable
private fun CloudCoverItem(
    header: String?,
    cloudCover: Int?,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current

    Column(
        modifier = modifier.fillMaxWidth()
    ) {
        TextFixedHeight(
            text = header ?: "",
            style = MaterialTheme.typography.labelMedium
        )
        TextFixedHeight(
            text = cloudCover?.let {
                UnitUtils.formatPercent(context, it.toDouble())
            } ?: "",
            style = MaterialTheme.typography.displaySmall
        )
        TextFixedHeight(
            text = getCloudCoverDescription(context, cloudCover) ?: "",
            style = MaterialTheme.typography.labelMedium
        )
    }
}

@Composable
private fun CloudCoverSummary(
    location: Location,
    daily: Daily,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    Column(
        modifier = modifier.fillMaxWidth()
    ) {
        TextFixedHeight(
            text = daily.getFullLabel(location, context),
            style = MaterialTheme.typography.labelMedium
        )
        TextFixedHeight(
            text = daily.cloudCover?.getRangeSummary(context) ?: "",
            style = MaterialTheme.typography.displaySmall
        )
        TextFixedHeight(
            text = daily.cloudCover?.getRangeDescriptionSummary(context) ?: "",
            style = MaterialTheme.typography.labelMedium
        )
    }
}

@Composable
private fun CloudCoverChart(
    location: Location,
    mappedValues: ImmutableMap<Long, Int>,
    daily: Daily,
    markerVisibilityListener: CartesianMarkerVisibilityListener,
) {
    val context = LocalContext.current

    val modelProducer = remember { CartesianChartModelProducer() }
    LaunchedEffect(location) {
        modelProducer.runTransaction {
            lineSeries {
                series(
                    x = mappedValues.keys,
                    y = mappedValues.values
                )
            }
        }
    }

    BreezyLineChart(
        location,
        modelProducer,
        daily.date,
        maxY = 100.0,
        endAxisValueFormatter = remember {
            CartesianValueFormatter { _, value, _ ->
                UnitUtils.formatPercent(context, value)
            }
        },
        persistentListOf(
            persistentMapOf(
                100f to Color(213, 213, 205),
                98f to Color(198, 201, 201),
                95f to Color(171, 180, 179),
                50f to Color(116, 116, 116),
                10f to Color(132, 119, 70),
                0f to Color(146, 130, 70)
            )
        ),
        endAxisItemPlacer = remember {
            VerticalAxis.ItemPlacer.step({ 20.0 }) // Every 20 %
        },
        markerVisibilityListener = markerVisibilityListener
    )
}

@Composable
fun SunshineItem(
    sunshineDuration: Double,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current

    DetailsItem(
        headlineText = stringResource(R.string.sunshine_duration),
        supportingText = DurationUnit.HOUR.formatMeasure(context, sunshineDuration),
        icon = R.drawable.ic_sunshine_duration,
        modifier = modifier
            .semantics(mergeDescendants = true) {}
            .clearAndSetSemantics {
                contentDescription = context.getString(R.string.sunshine_duration) +
                    context.getString(R.string.colon_separator) +
                    DurationUnit.HOUR.formatContentDescription(context, sunshineDuration)
            }
    )
}
