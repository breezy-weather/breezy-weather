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
import org.breezyweather.common.basic.models.options.unit.PressureUnit
import org.breezyweather.common.extensions.getFormattedTime
import org.breezyweather.common.extensions.is12Hour
import org.breezyweather.common.extensions.roundDownToNearestMultiplier
import org.breezyweather.common.extensions.roundUpToNearestMultiplier
import org.breezyweather.common.extensions.toDate
import org.breezyweather.domain.settings.SettingsManager
import org.breezyweather.ui.common.charts.BreezyLineChart
import org.breezyweather.ui.settings.preference.bottomInsetItem
import java.util.Date
import kotlin.math.max
import kotlin.math.min

@Composable
fun DetailsPressure(
    location: Location,
    hourlyList: ImmutableList<Hourly>,
    theDay: Date,
    modifier: Modifier = Modifier,
) {
    val mappedValues = remember(hourlyList) {
        hourlyList
            .filter { it.pressure != null }
            .associate { it.date.time to it.pressure!! }
            .toImmutableMap()
    }

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(
            horizontal = dimensionResource(R.dimen.normal_margin),
            vertical = dimensionResource(R.dimen.small_margin)
        )
    ) {
        if (mappedValues.size >= DetailScreen.CHART_MIN_COUNT) {
            item {
                PressureChart(location, mappedValues, theDay)
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
private fun PressureSummary(
    pressure: Double,
) {
    PressureItem(
        header = {
            TextFixedHeight(
                text = stringResource(R.string.pressure_average),
                style = MaterialTheme.typography.labelMedium
            )
        },
        pressure = pressure
    )
}

@Composable
private fun PressureItem(
    header: @Composable () -> Unit,
    pressure: Double?,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val pressureUnit = SettingsManager.getInstance(context).getPressureUnit(context)

    Column(
        modifier = modifier.fillMaxWidth()
    ) {
        header()
        TextFixedHeight(
            text = pressure?.let {
                UnitUtils.formatUnitsDifferentFontSize(
                    formattedMeasure = pressureUnit.formatMeasure(context, it),
                    fontSize = MaterialTheme.typography.headlineSmall.fontSize
                )
            } ?: AnnotatedString(""),
            style = MaterialTheme.typography.displaySmall,
            modifier = Modifier
                .clearAndSetSemantics {
                    pressure?.let {
                        contentDescription = pressureUnit.formatContentDescription(context, it)
                    }
                }
        )
    }
}

@Composable
private fun PressureChart(
    location: Location,
    mappedValues: ImmutableMap<Long, Double>,
    theDay: Date,
) {
    val context = LocalContext.current
    val pressureUnit = SettingsManager.getInstance(context).getPressureUnit(context)
    val chartStep = pressureUnit.chartStep
    val maxY = remember(mappedValues) {
        max(
            pressureUnit.getConvertedUnit(PressureUnit.NORMAL) + chartStep.times(1.6),
            pressureUnit.getConvertedUnit(mappedValues.values.max())
        ).roundUpToNearestMultiplier(chartStep)
    }
    val minY = remember(mappedValues) {
        min(
            pressureUnit.getConvertedUnit(PressureUnit.NORMAL) - chartStep.times(1.6),
            pressureUnit.getConvertedUnit(mappedValues.values.min())
        ).roundDownToNearestMultiplier(chartStep)
    }
    val averagePressure = remember(mappedValues) {
        mappedValues.values.average()
    }

    val modelProducer = remember { CartesianChartModelProducer() }

    LaunchedEffect(location) {
        modelProducer.runTransaction {
            lineSeries {
                series(
                    x = mappedValues.keys,
                    y = mappedValues.values.map { pressureUnit.getConvertedUnit(it) }
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
        mappedValues.getOrElse(it.x.toLong()) { null }?.let { pressure ->
            PressureItem(
                header = {
                    TextFixedHeight(
                        text = it.x.toLong().toDate().getFormattedTime(location, context, context.is12Hour),
                        style = MaterialTheme.typography.labelMedium
                    )
                },
                pressure = pressure
            )
        }
    } ?: PressureSummary(averagePressure)

    Spacer(modifier = Modifier.height(dimensionResource(R.dimen.normal_margin)))

    BreezyLineChart(
        location,
        modelProducer,
        theDay,
        maxY,
        { _, value, _ -> pressureUnit.formatMeasure(context, value, isValueInDefaultUnit = false) },
        persistentListOf(
            persistentMapOf(
                pressureUnit.getConvertedUnit(1080.0).toFloat() to Color(48, 8, 24),
                pressureUnit.getConvertedUnit(1046.0).toFloat() to Color(111, 24, 64),
                pressureUnit.getConvertedUnit(1038.0).toFloat() to Color(142, 47, 57),
                pressureUnit.getConvertedUnit(1030.0).toFloat() to Color(159, 81, 44),
                pressureUnit.getConvertedUnit(1024.0).toFloat() to Color(163, 116, 67),
                pressureUnit.getConvertedUnit(1019.0).toFloat() to Color(167, 147, 107),
                pressureUnit.getConvertedUnit(1015.25).toFloat() to Color(176, 174, 152),
                pressureUnit.getConvertedUnit(1013.25).toFloat() to Color(182, 182, 182),
                pressureUnit.getConvertedUnit(1011.25).toFloat() to Color(155, 183, 172),
                pressureUnit.getConvertedUnit(1007.0).toFloat() to Color(103, 162, 155),
                pressureUnit.getConvertedUnit(1002.0).toFloat() to Color(26, 140, 147),
                pressureUnit.getConvertedUnit(995.0).toFloat() to Color(0, 117, 146),
                pressureUnit.getConvertedUnit(986.0).toFloat() to Color(0, 90, 148),
                pressureUnit.getConvertedUnit(976.0).toFloat() to Color(0, 52, 146),
                pressureUnit.getConvertedUnit(950.0).toFloat() to Color(0, 32, 96),
                pressureUnit.getConvertedUnit(900.0).toFloat() to Color(8, 16, 48)
            )
        ),
        trendHorizontalLines = persistentMapOf(
            pressureUnit.getConvertedUnit(PressureUnit.NORMAL) to stringResource(R.string.temperature_normal_short)
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
            val trend = with(currentValue - previousValue) {
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
