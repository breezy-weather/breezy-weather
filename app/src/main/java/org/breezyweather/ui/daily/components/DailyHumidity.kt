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
import breezyweather.domain.location.model.Location
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
import org.breezyweather.common.extensions.currentLocale
import org.breezyweather.common.extensions.getFormattedTime
import org.breezyweather.common.extensions.is12Hour
import org.breezyweather.common.extensions.roundDownToNearestMultiplier
import org.breezyweather.common.extensions.roundUpToNearestMultiplier
import org.breezyweather.common.extensions.toDate
import org.breezyweather.domain.settings.SettingsManager
import org.breezyweather.ui.common.charts.BreezyLineChart
import org.breezyweather.ui.settings.preference.bottomInsetItem
import java.text.NumberFormat
import java.util.Date
import kotlin.math.max

@Composable
fun DailyHumidity(
    location: Location,
    hourlyList: ImmutableList<Hourly>,
    theDay: Date,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val mappedHumidityValues = remember(hourlyList) {
        hourlyList
            .filter { it.relativeHumidity != null }
            .associate { it.date.time to it.relativeHumidity!! }
            .toImmutableMap()
    }
    val mappedDewPointValues = remember(hourlyList) {
        hourlyList
            .filter { it.dewPoint != null }
            .associate { it.date.time to it.dewPoint!! }
            .toImmutableMap()
    }

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(
            horizontal = dimensionResource(R.dimen.normal_margin),
            vertical = dimensionResource(R.dimen.little_margin)
        )
    ) {
        if (mappedHumidityValues.size >= ChartDisplay.CHART_MIN_COUNT) {
            item {
                HumidityChart(location, mappedHumidityValues, theDay)
            }
        } else {
            item {
                UnavailableChart(mappedHumidityValues.size)
            }
        }
        // TODO: Daily summary
        item {
            Spacer(modifier = Modifier.height(dimensionResource(R.dimen.normal_margin)))
        }
        item {
            DailySectionHeader(stringResource(R.string.humidity_about))
        }
        item {
            DailyCardText(stringResource(R.string.humidity_about_description))
        }
        item {
            DailySectionDivider()
        }
        item {
            DailySectionHeader(stringResource(R.string.dew_point))
        }
        item {
            Spacer(modifier = Modifier.height(dimensionResource(R.dimen.normal_margin)))
        }
        if (mappedDewPointValues.size >= ChartDisplay.CHART_MIN_COUNT) {
            item {
                DewPointChart(location, mappedDewPointValues, theDay)
            }
        } else {
            item {
                UnavailableChart(mappedDewPointValues.size)
            }
        }
        item {
            Spacer(modifier = Modifier.height(dimensionResource(R.dimen.normal_margin)))
        }
        item {
            DailySectionHeader(stringResource(R.string.dew_point_about))
        }
        item {
            DailyCardText(
                stringResource(
                    R.string.dew_point_about_description,
                    NumberFormat.getPercentInstance(context.currentLocale).apply {
                        maximumFractionDigits = 0
                    }.format(1)
                )
            )
        }
        bottomInsetItem()
    }
}

@Composable
private fun HumiditySummary(
    relativeHumidity: Double?,
) {
    HumidityItem(
        header = {
            Text(
                text = "",
                style = MaterialTheme.typography.labelMedium,
                modifier = Modifier.clearAndSetSemantics {}
            )
        },
        relativeHumidity = relativeHumidity
    )
}

@Composable
private fun HumidityItem(
    header: @Composable () -> Unit,
    relativeHumidity: Double?,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current

    Column(
        modifier = modifier.fillMaxWidth()
    ) {
        header()
        Text(
            text = relativeHumidity?.let {
                NumberFormat.getPercentInstance(context.currentLocale).apply {
                    maximumFractionDigits = 0
                }.format(it.div(100.0))
            } ?: "",
            style = MaterialTheme.typography.displaySmall
        )
    }
}

@Composable
private fun HumidityChart(
    location: Location,
    mappedValues: ImmutableMap<Long, Double>,
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
        mappedValues.getOrElse(it.x.toLong()) { null }?.let { relativeHumidity ->
            HumidityItem(
                header = {
                    Text(
                        text = it.x.toLong().toDate().getFormattedTime(location, context, context.is12Hour),
                        style = MaterialTheme.typography.labelMedium
                    )
                },
                relativeHumidity = relativeHumidity
            )
        }
    } ?: HumiditySummary(null)

    Spacer(modifier = Modifier.height(dimensionResource(R.dimen.normal_margin)))

    BreezyLineChart(
        location,
        modelProducer,
        theDay,
        maxY,
        endAxisValueFormatter,
        persistentListOf(
            persistentMapOf(
                100f to Color(56, 70, 114),
                97f to Color(56, 98, 157),
                93f to Color(56, 123, 173),
                90f to Color(56, 132, 173),
                87f to Color(56, 135, 173),
                83f to Color(56, 148, 173),
                80f to Color(56, 157, 173),
                75f to Color(56, 160, 173),
                70f to Color(56, 174, 173),
                60f to Color(56, 173, 121),
                50f to Color(105, 173, 56),
                40f to Color(173, 146, 56),
                30f to Color(173, 110, 56),
                0f to Color(173, 85, 56)
            )
        ),
        topAxisValueFormatter = { _, value, _ ->
            mappedValues.getOrElse(value.toLong()) { null }?.let {
                NumberFormat.getPercentInstance(context.currentLocale).apply {
                    maximumFractionDigits = 0
                }.format(it.div(100.0))
            } ?: "-"
        },
        endAxisItemPlacer = remember {
            VerticalAxis.ItemPlacer.step({ 20.0 }) // Every 20 %
        },
        markerVisibilityListener = markerVisibilityListener
    )
}

@Composable
private fun DewPointSummary(
    dewPoint: Double?,
) {
    DewPointItem(
        header = {
            Text(
                text = "",
                style = MaterialTheme.typography.labelMedium,
                modifier = Modifier.clearAndSetSemantics {}
            )
        },
        dewPoint = dewPoint
    )
}

@Composable
private fun DewPointItem(
    header: @Composable () -> Unit,
    dewPoint: Double?,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current

    Column(
        modifier = modifier.fillMaxWidth()
    ) {
        header()
        Text(
            text = dewPoint?.let {
                SettingsManager.getInstance(context).temperatureUnit.getValueText(context, value = it)
            } ?: "",
            style = MaterialTheme.typography.displaySmall
        )
    }
}

@Composable
private fun DewPointChart(
    location: Location,
    mappedValues: ImmutableMap<Long, Double>,
    theDay: Date,
) {
    val context = LocalContext.current
    val temperatureUnit = SettingsManager.getInstance(context).temperatureUnit
    val step = temperatureUnit.chartStep
    val maxY = remember(mappedValues) {
        temperatureUnit.getValueWithoutUnit(mappedValues.values.max()).roundUpToNearestMultiplier(step)
    }
    val minY = remember(mappedValues) {
        temperatureUnit.getValueWithoutUnit(mappedValues.values.min()).roundDownToNearestMultiplier(step)
    }

    val modelProducer = remember { CartesianChartModelProducer() }

    LaunchedEffect(location) {
        modelProducer.runTransaction {
            lineSeries {
                series(
                    x = mappedValues.keys,
                    y = mappedValues.values.map { temperatureUnit.getValueWithoutUnit(it) }
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
        mappedValues.getOrElse(it.x.toLong()) { null }?.let { dewPoint ->
            DewPointItem(
                header = {
                    Text(
                        text = it.x.toLong().toDate().getFormattedTime(location, context, context.is12Hour),
                        style = MaterialTheme.typography.labelMedium
                    )
                },
                dewPoint = dewPoint
            )
        }
    } ?: DewPointSummary(null)

    Spacer(modifier = Modifier.height(dimensionResource(R.dimen.normal_margin)))

    BreezyLineChart(
        location,
        modelProducer,
        theDay,
        maxY,
        { _, value, _ -> temperatureUnit.getValueText(context, value, isValueInDefaultUnit = false) },
        persistentListOf(
            persistentMapOf(
                // TODO: Duplicate of temperature colors
                temperatureUnit.getValueWithoutUnit(47.0).toFloat() to Color(71, 14, 0),
                temperatureUnit.getValueWithoutUnit(30.0).toFloat() to Color(232, 83, 25),
                temperatureUnit.getValueWithoutUnit(21.0).toFloat() to Color(243, 183, 4),
                temperatureUnit.getValueWithoutUnit(10.0).toFloat() to Color(128, 147, 24),
                temperatureUnit.getValueWithoutUnit(1.0).toFloat() to Color(68, 125, 99),
                temperatureUnit.getValueWithoutUnit(0.0).toFloat() to Color(93, 133, 198),
                temperatureUnit.getValueWithoutUnit(-4.0).toFloat() to Color(100, 166, 189),
                temperatureUnit.getValueWithoutUnit(-8.0).toFloat() to Color(106, 191, 181),
                temperatureUnit.getValueWithoutUnit(-15.0).toFloat() to Color(157, 219, 217),
                temperatureUnit.getValueWithoutUnit(-25.0).toFloat() to Color(143, 89, 169),
                temperatureUnit.getValueWithoutUnit(-40.0).toFloat() to Color(162, 70, 145),
                temperatureUnit.getValueWithoutUnit(-55.0).toFloat() to Color(202, 172, 195),
                temperatureUnit.getValueWithoutUnit(-70.0).toFloat() to Color(115, 70, 105)
            )
        ),
        topAxisValueFormatter = { _, value, _ ->
            mappedValues.getOrElse(value.toLong()) { null }?.let {
                temperatureUnit.getShortValueText(context, it)
            } ?: "-"
        },
        minY = minY,
        endAxisItemPlacer = remember {
            VerticalAxis.ItemPlacer.step({ step })
        },
        markerVisibilityListener = markerVisibilityListener
    )
}
