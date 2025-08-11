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
import androidx.compose.ui.text.AnnotatedString
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
import org.breezyweather.common.extensions.formatMeasure
import org.breezyweather.common.extensions.getFormattedTime
import org.breezyweather.common.extensions.is12Hour
import org.breezyweather.common.extensions.roundDownToNearestMultiplier
import org.breezyweather.common.extensions.roundUpToNearestMultiplier
import org.breezyweather.common.extensions.toDate
import org.breezyweather.domain.settings.SettingsManager
import org.breezyweather.ui.common.charts.BreezyLineChart
import org.breezyweather.ui.settings.preference.bottomInsetItem
import org.breezyweather.unit.formatting.UnitWidth
import org.breezyweather.unit.pressure.Pressure
import org.breezyweather.unit.pressure.Pressure.Companion.hectopascals
import org.breezyweather.unit.pressure.Pressure.Companion.pascals
import org.breezyweather.unit.pressure.PressureUnit
import org.breezyweather.unit.pressure.toPressure
import java.util.Date
import kotlin.math.max
import kotlin.math.min

@Composable
fun DetailsPressure(
    location: Location,
    hourlyList: ImmutableList<Hourly>,
    daily: Daily,
    defaultValue: Pair<Date, Pressure>?,
    modifier: Modifier = Modifier,
) {
    val mappedValues = remember(hourlyList) {
        hourlyList
            .filter { it.pressure != null }
            .associate { it.date.time to it.pressure!! }
            .toImmutableMap()
    }
    var activeItem: Pair<Date, Pressure>? by remember { mutableStateOf(null) }
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
            PressureHeader(location, daily, activeItem, defaultValue)
        }
        item {
            Spacer(modifier = Modifier.height(dimensionResource(R.dimen.normal_margin)))
        }
        if (mappedValues.size >= DetailScreen.CHART_MIN_COUNT) {
            item {
                PressureChart(location, mappedValues, daily, markerVisibilityListener)
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
            DetailsSectionHeader(stringResource(R.string.pressure_about))
        }
        item {
            DetailsCardText(
                stringResource(R.string.pressure_about_description1),
                stringResource(R.string.pressure_about_description2)
            )
        }
        bottomInsetItem()
    }
}

@Composable
fun PressureHeader(
    location: Location,
    daily: Daily,
    activeItem: Pair<Date, Pressure>?,
    defaultValue: Pair<Date, Pressure>?,
) {
    val context = LocalContext.current

    if (activeItem != null) {
        PressureItem(
            header = activeItem.first.getFormattedTime(location, context, context.is12Hour),
            pressure = activeItem.second
        )
    } else if (daily.pressure?.average != null) {
        PressureItem(
            header = daily.pressure?.average?.let { stringResource(R.string.pressure_average) },
            pressure = daily.pressure?.average
        )
    } else {
        PressureItem(
            header = defaultValue?.first?.getFormattedTime(location, context, context.is12Hour),
            pressure = defaultValue?.second
        )
    }
}

@Composable
private fun PressureItem(
    header: String?,
    pressure: Pressure?,
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
            text = pressure?.let {
                UnitUtils.formatUnitsDifferentFontSize(
                    formattedMeasure = it.formatMeasure(context),
                    fontSize = MaterialTheme.typography.headlineSmall.fontSize
                )
            } ?: AnnotatedString(""),
            style = MaterialTheme.typography.displaySmall,
            modifier = Modifier
                .clearAndSetSemantics {
                    pressure?.let {
                        contentDescription = it.formatMeasure(context, unitWidth = UnitWidth.LONG)
                    }
                }
        )
    }
}

@Composable
private fun PressureChart(
    location: Location,
    mappedValues: ImmutableMap<Long, Pressure>,
    daily: Daily,
    markerVisibilityListener: CartesianMarkerVisibilityListener,
) {
    val context = LocalContext.current

    val pressureUnit = SettingsManager.getInstance(context).getPressureUnit(context)
    val chartStep = pressureUnit.chartStep
    val maxY = remember(mappedValues) {
        max(
            PressureUnit.NORMAL.pascals.toDouble(pressureUnit) + chartStep.times(1.6),
            mappedValues.values.maxOf { it.toDouble(pressureUnit) }
        ).roundUpToNearestMultiplier(chartStep)
    }
    val minY = remember(mappedValues) {
        min(
            PressureUnit.NORMAL.pascals.toDouble(pressureUnit) - chartStep.times(1.6),
            mappedValues.values.minOf { it.toDouble(pressureUnit) }
        ).roundDownToNearestMultiplier(chartStep)
    }

    val modelProducer = remember { CartesianChartModelProducer() }

    LaunchedEffect(location) {
        modelProducer.runTransaction {
            lineSeries {
                series(
                    x = mappedValues.keys,
                    y = mappedValues.values.map { it.toDouble(pressureUnit) }
                )
            }
        }
    }

    BreezyLineChart(
        location,
        modelProducer,
        daily.date,
        maxY,
        { _, value, _ -> value.toPressure(pressureUnit).formatMeasure(context) },
        persistentListOf(
            persistentMapOf(
                1080.0.hectopascals.toDouble(pressureUnit).toFloat() to Color(48, 8, 24),
                1046.0.hectopascals.toDouble(pressureUnit).toFloat() to Color(111, 24, 64),
                1038.0.hectopascals.toDouble(pressureUnit).toFloat() to Color(142, 47, 57),
                1030.0.hectopascals.toDouble(pressureUnit).toFloat() to Color(159, 81, 44),
                1024.0.hectopascals.toDouble(pressureUnit).toFloat() to Color(163, 116, 67),
                1019.0.hectopascals.toDouble(pressureUnit).toFloat() to Color(167, 147, 107),
                1015.25.hectopascals.toDouble(pressureUnit).toFloat() to Color(176, 174, 152),
                1013.25.hectopascals.toDouble(pressureUnit).toFloat() to Color(182, 182, 182),
                1011.25.hectopascals.toDouble(pressureUnit).toFloat() to Color(155, 183, 172),
                1007.0.hectopascals.toDouble(pressureUnit).toFloat() to Color(103, 162, 155),
                1002.0.hectopascals.toDouble(pressureUnit).toFloat() to Color(26, 140, 147),
                995.0.hectopascals.toDouble(pressureUnit).toFloat() to Color(0, 117, 146),
                986.0.hectopascals.toDouble(pressureUnit).toFloat() to Color(0, 90, 148),
                976.0.hectopascals.toDouble(pressureUnit).toFloat() to Color(0, 52, 146),
                950.0.hectopascals.toDouble(pressureUnit).toFloat() to Color(0, 32, 96),
                900.0.hectopascals.toDouble(pressureUnit).toFloat() to Color(8, 16, 48)
            )
        ),
        trendHorizontalLines = persistentMapOf(
            PressureUnit.NORMAL.pascals.toDouble(pressureUnit) to stringResource(R.string.temperature_normal_short)
        ),
        minY = minY,
        topAxisValueFormatter = { _, value, _ ->
            val currentIndex = mappedValues.keys.indexOfFirst { it == value.toLong() }.let {
                if (it == 0) 1 else it
            }
            val previousValue = if (currentIndex > 0) {
                mappedValues.values.elementAt(currentIndex - 1)
            } else {
                return@BreezyLineChart "-"
            }
            val currentValue = mappedValues.values.elementAt(currentIndex)
            val trend = with(currentValue.value - previousValue.value) {
                when {
                    // Take into account the trend if the difference is of at least 0.5
                    this >= 0.5 -> "↑"
                    this <= -0.5 -> "↓"
                    else -> "="
                }
            }
            SpannableString(trend).apply {
                setSpan(RelativeSizeSpan(2f), 0, trend.length, 0)
            }
        },
        endAxisItemPlacer = remember {
            VerticalAxis.ItemPlacer.step({ chartStep })
        },
        markerVisibilityListener = markerVisibilityListener
    )
}
