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

package org.breezyweather.ui.daily.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
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
import org.breezyweather.common.basic.models.options.appearance.ChartDisplay
import org.breezyweather.common.basic.models.options.unit.DurationUnit
import org.breezyweather.common.extensions.currentLocale
import org.breezyweather.common.extensions.getFormattedTime
import org.breezyweather.common.extensions.is12Hour
import org.breezyweather.common.extensions.toDate
import org.breezyweather.ui.common.charts.BreezyLineChart
import org.breezyweather.ui.settings.preference.bottomInsetItem
import java.text.NumberFormat
import java.util.Date

@Composable
fun DailyCloudCover(
    location: Location,
    hourlyList: ImmutableList<Hourly>,
    daily: Daily,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val mappedValues = remember(hourlyList) {
        hourlyList
            .filter { it.cloudCover != null }
            .associate { it.date.time to it.cloudCover!! }
            .toImmutableMap()
    }

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(
            horizontal = dimensionResource(R.dimen.normal_margin),
            vertical = dimensionResource(R.dimen.little_margin)
        )
    ) {
        if (mappedValues.size >= ChartDisplay.CHART_MIN_COUNT) {
            item {
                CloudCoverChart(location, mappedValues, daily.date)
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
                DailyItem(
                    headlineText = stringResource(R.string.sunshine_duration),
                    supportingText = DurationUnit.H.getValueText(context, sunshineDuration),
                    icon = R.drawable.ic_sunshine_duration,
                    modifier = Modifier
                        .semantics(mergeDescendants = true) {}
                        .clearAndSetSemantics {
                            contentDescription = context.getString(R.string.sunshine_duration) +
                                context.getString(R.string.colon_separator) +
                                DurationUnit.H.getValueVoice(context, sunshineDuration)
                        }
                )
            }
        }
        item {
            Spacer(modifier = Modifier.height(dimensionResource(R.dimen.normal_margin)))
        }
        // TODO: Daily summary
        item {
            DailySectionHeader(stringResource(R.string.cloud_cover_about))
        }
        item {
            DailyCardText(stringResource(R.string.cloud_cover_about_description))
        }
        bottomInsetItem()
    }
}

@Composable
private fun CloudCoverSummary(
    cloudCover: Int?,
) {
    CloudCoverItem(
        header = {
            Text(
                text = "",
                style = MaterialTheme.typography.labelMedium,
                modifier = Modifier.clearAndSetSemantics {}
            )
        },
        cloudCover = cloudCover
    )
}

@Composable
private fun CloudCoverItem(
    header: @Composable () -> Unit,
    cloudCover: Int?,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current

    Column(
        modifier = modifier.fillMaxWidth()
    ) {
        header()
        Text(
            text = cloudCover?.let {
                NumberFormat.getPercentInstance(context.currentLocale).apply {
                    maximumFractionDigits = 0
                }.format(it.div(100.0))
            } ?: "",
            style = MaterialTheme.typography.displaySmall
        )
    }
}

@Composable
private fun CloudCoverChart(
    location: Location,
    mappedValues: ImmutableMap<Long, Int>,
    theDay: Date,
) {
    val context = LocalContext.current
    val maxY = 100.0

    val endAxisValueFormatter = CartesianValueFormatter { _, value, _ ->
        NumberFormat.getPercentInstance(context.currentLocale).apply {
            maximumFractionDigits = 0
        }.format(value.div(100.0))
    }

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

    var activeMarkerTarget: CartesianMarker.Target? by remember { mutableStateOf(null) }
    val markerVisibilityListener = object : CartesianMarkerVisibilityListener {
        override fun onShown(marker: CartesianMarker, targets: List<CartesianMarker.Target>) {
            activeMarkerTarget = targets.firstOrNull()
        }

        override fun onUpdated(marker: CartesianMarker, targets: List<CartesianMarker.Target>) {
            activeMarkerTarget = targets.firstOrNull()
        }

        override fun onHidden(marker: CartesianMarker) {
            activeMarkerTarget = null
        }
    }

    activeMarkerTarget?.let {
        mappedValues.getOrElse(it.x.toLong()) { null }?.let { cloudCover ->
            CloudCoverItem(
                header = {
                    Text(
                        text = it.x.toLong().toDate().getFormattedTime(location, context, context.is12Hour),
                        style = MaterialTheme.typography.labelMedium
                    )
                },
                cloudCover = cloudCover
            )
        }
    } ?: CloudCoverSummary(null)

    Spacer(modifier = Modifier.height(dimensionResource(R.dimen.normal_margin)))

    BreezyLineChart(
        location,
        modelProducer,
        theDay,
        maxY,
        endAxisValueFormatter,
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
