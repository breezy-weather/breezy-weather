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
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import breezyweather.domain.location.model.Location
import breezyweather.domain.weather.model.Hourly
import com.patrykandpatrick.vico.core.cartesian.data.CartesianChartModelProducer
import com.patrykandpatrick.vico.core.cartesian.data.CartesianValueFormatter
import com.patrykandpatrick.vico.core.cartesian.data.lineSeries
import com.patrykandpatrick.vico.core.cartesian.marker.CartesianMarker
import com.patrykandpatrick.vico.core.cartesian.marker.CartesianMarkerVisibilityListener
import com.patrykandpatrick.vico.core.common.Fill
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.ImmutableMap
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.persistentMapOf
import kotlinx.collections.immutable.toImmutableMap
import org.breezyweather.R
import org.breezyweather.common.basic.models.options.appearance.ChartDisplay
import org.breezyweather.common.basic.models.options.unit.DistanceUnit
import org.breezyweather.common.extensions.getFormattedTime
import org.breezyweather.common.extensions.is12Hour
import org.breezyweather.common.extensions.toDate
import org.breezyweather.domain.settings.SettingsManager
import org.breezyweather.ui.common.charts.BreezyLineChart
import org.breezyweather.ui.settings.preference.bottomInsetItem
import java.util.Date
import kotlin.math.max

@Composable
fun DailyVisibility(
    location: Location,
    hourlyList: ImmutableList<Hourly>,
    theDay: Date,
    modifier: Modifier = Modifier,
) {
    val mappedValues = remember(hourlyList) {
        hourlyList
            .filter { it.visibility != null }
            .associate { it.date.time to it.visibility!! }
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
                VisibilityChart(location, mappedValues, theDay)
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
            DailySectionHeader(stringResource(R.string.visibility_about))
        }
        item {
            DailyCardText(stringResource(R.string.visibility_about_description))
        }
        bottomInsetItem()
    }
}

@Composable
private fun VisibilityItem(
    header: @Composable () -> Unit,
    visibility: Double,
) {
    val context = LocalContext.current
    val distanceUnit = SettingsManager.getInstance(context).distanceUnit
    val visibilityDescription = DistanceUnit.getVisibilityDescription(context, visibility)
    val visibilityFormatted = distanceUnit.getValueText(context, visibility)
    val visibilityContentDescription = distanceUnit.getValueVoice(context, visibility)

    Column(
        modifier = Modifier
            .fillMaxWidth()
    ) {
        header()
        Text(
            text = visibilityFormatted,
            style = MaterialTheme.typography.displaySmall,
            modifier = Modifier
                .clearAndSetSemantics {
                    contentDescription = visibilityContentDescription
                }
        )
        if (!visibilityDescription.isNullOrEmpty()) {
            Text(
                text = visibilityDescription,
                style = MaterialTheme.typography.labelMedium
            )
        }
    }
}

@Composable
private fun VisibilitySummary(
    mappedValues: ImmutableMap<Long, Double>,
) {
    val context = LocalContext.current
    val distanceUnit = SettingsManager.getInstance(context).distanceUnit
    val minVisibility = mappedValues.values.min()
    val minVisibilityDescription = DistanceUnit.getVisibilityDescription(context, minVisibility)
    val maxVisibility = mappedValues.values.max()
    val maxVisibilityDescription = DistanceUnit.getVisibilityDescription(context, maxVisibility)
    val maxVisibilityFormatted = distanceUnit.getValueText(context, maxVisibility)
    val maxVisibilityContentDescription = distanceUnit.getValueVoice(context, maxVisibility)

    Column(
        modifier = Modifier
            .fillMaxWidth()
    ) {
        // Make room for time when switching to the marker
        Text(
            text = "",
            style = MaterialTheme.typography.labelMedium,
            modifier = Modifier.clearAndSetSemantics {}
        )
        Text(
            text = if (minVisibility == maxVisibility) {
                maxVisibilityFormatted
            } else {
                stringResource(
                    R.string.visibility_from_to_number,
                    distanceUnit.getValueTextWithoutUnit(minVisibility),
                    maxVisibilityFormatted
                )
            },
            style = MaterialTheme.typography.displaySmall,
            modifier = Modifier
                .clearAndSetSemantics {
                    contentDescription = if (minVisibility == maxVisibility) {
                        maxVisibilityContentDescription
                    } else {
                        distanceUnit.getValueTextWithoutUnit(minVisibility).let {
                            context.getString(
                                R.string.visibility_from_to_number,
                                it,
                                maxVisibilityContentDescription
                            )
                        }
                    }
                }
        )
        if (!maxVisibilityDescription.isNullOrEmpty()) {
            Text(
                text = if (minVisibilityDescription == maxVisibilityDescription) {
                    maxVisibilityDescription
                } else {
                    stringResource(
                        R.string.visibility_from_to_description,
                        minVisibilityDescription!!,
                        maxVisibilityDescription
                    )
                },
                style = MaterialTheme.typography.labelMedium
            )
        }
    }
}

@Composable
private fun VisibilityChart(
    location: Location,
    mappedValues: ImmutableMap<Long, Double>,
    theDay: Date,
) {
    val context = LocalContext.current
    val maxY = remember(mappedValues) {
        max(
            25000.0, // TODO: Make this a const
            mappedValues.values.max()
        )
    }

    val endAxisValueFormatter = CartesianValueFormatter { _, value, _ ->
        SettingsManager.getInstance(context).distanceUnit.getValueText(context, value)
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
        mappedValues.getOrElse(it.x.toLong()) { null }?.let { visibility ->
            VisibilityItem(
                header = {
                    Text(
                        text = it.x.toLong().toDate().getFormattedTime(location, context, context.is12Hour),
                        style = MaterialTheme.typography.labelMedium
                    )
                },
                visibility = visibility
            )
        }
    } ?: VisibilitySummary(mappedValues)

    Spacer(modifier = Modifier.height(dimensionResource(R.dimen.normal_margin)))

    BreezyLineChart(
        location,
        modelProducer,
        theDay,
        maxY,
        endAxisValueFormatter,
        persistentListOf(
            persistentMapOf(
                20000 to Fill(Color(119, 141, 120).toArgb()),
                15000 to Fill(Color(91, 167, 99).toArgb()),
                9000 to Fill(Color(90, 169, 90).toArgb()),
                8000 to Fill(Color(98, 122, 160).toArgb()),
                6000 to Fill(Color(98, 122, 160).toArgb()),
                5000 to Fill(Color(167, 91, 91).toArgb()),
                2200 to Fill(Color(167, 91, 91).toArgb()),
                1600 to Fill(Color(162, 97, 160).toArgb()),
                0 to Fill(Color(166, 93, 165).toArgb())
            )
        ),
        topAxisValueFormatter = { _, value, _ ->
            mappedValues.getOrElse(value.toLong()) { null }?.let {
                SettingsManager.getInstance(context).distanceUnit.getValueTextWithoutUnit(it)
            } ?: "-"
        },
        markerVisibilityListener = markerVisibilityListener
    )
}
