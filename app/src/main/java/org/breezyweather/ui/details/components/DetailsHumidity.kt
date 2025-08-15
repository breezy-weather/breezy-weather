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
import org.breezyweather.common.extensions.formatMeasure
import org.breezyweather.common.extensions.getFormattedTime
import org.breezyweather.common.extensions.is12Hour
import org.breezyweather.common.extensions.roundDownToNearestMultiplier
import org.breezyweather.common.extensions.roundUpToNearestMultiplier
import org.breezyweather.common.extensions.toDate
import org.breezyweather.common.options.appearance.DetailScreen
import org.breezyweather.common.utils.UnitUtils
import org.breezyweather.domain.settings.SettingsManager
import org.breezyweather.domain.weather.model.getFullLabel
import org.breezyweather.domain.weather.model.getRangeContentDescriptionSummary
import org.breezyweather.domain.weather.model.getRangeSummary
import org.breezyweather.ui.common.charts.BreezyLineChart
import org.breezyweather.ui.settings.preference.bottomInsetItem
import org.breezyweather.unit.formatting.UnitWidth
import org.breezyweather.unit.temperature.Temperature
import org.breezyweather.unit.temperature.Temperature.Companion.celsius
import org.breezyweather.unit.temperature.toTemperature
import java.util.Date

@Composable
fun DetailsHumidity(
    location: Location,
    hourlyList: ImmutableList<Hourly>,
    daily: Daily,
    defaultHumidityValue: Pair<Date, Double>?,
    defaultDewPointValue: Pair<Date, Temperature>?,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val mappedHumidityValues = remember(hourlyList) {
        hourlyList
            .filter { it.relativeHumidity != null }
            .associate { it.date.time to it.relativeHumidity!! }
            .toImmutableMap()
    }
    var activeHumidityItem: Pair<Date, Double>? by remember { mutableStateOf(null) }
    val humidityMarkerVisibilityListener = remember {
        object : CartesianMarkerVisibilityListener {
            override fun onShown(marker: CartesianMarker, targets: List<CartesianMarker.Target>) {
                activeHumidityItem = targets.firstOrNull()?.let { target ->
                    mappedHumidityValues.getOrElse(target.x.toLong()) { null }?.let {
                        Pair(target.x.toLong().toDate(), it)
                    }
                }
            }

            override fun onUpdated(marker: CartesianMarker, targets: List<CartesianMarker.Target>) {
                onShown(marker, targets)
            }

            override fun onHidden(marker: CartesianMarker) {
                activeHumidityItem = null
            }
        }
    }

    val mappedDewPointValues = remember(hourlyList) {
        hourlyList
            .filter { it.dewPoint != null }
            .associate { it.date.time to it.dewPoint!! }
            .toImmutableMap()
    }
    var activeDewPointItem: Pair<Date, Temperature>? by remember { mutableStateOf(null) }
    val dewPointMarkerVisibilityListener = remember {
        object : CartesianMarkerVisibilityListener {
            override fun onShown(marker: CartesianMarker, targets: List<CartesianMarker.Target>) {
                activeDewPointItem = targets.firstOrNull()?.let { target ->
                    mappedDewPointValues.getOrElse(target.x.toLong()) { null }?.let {
                        Pair(target.x.toLong().toDate(), it)
                    }
                }
            }

            override fun onUpdated(marker: CartesianMarker, targets: List<CartesianMarker.Target>) {
                onShown(marker, targets)
            }

            override fun onHidden(marker: CartesianMarker) {
                activeDewPointItem = null
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
            HumidityHeader(location, daily, activeHumidityItem, defaultHumidityValue)
        }
        item {
            Spacer(modifier = Modifier.height(dimensionResource(R.dimen.normal_margin)))
        }
        if (mappedHumidityValues.size >= DetailScreen.CHART_MIN_COUNT) {
            item {
                HumidityChart(location, mappedHumidityValues, daily, humidityMarkerVisibilityListener)
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
            DetailsSectionHeader(stringResource(R.string.humidity_about))
        }
        item {
            DetailsCardText(stringResource(R.string.humidity_about_description))
        }
        item {
            DetailsSectionDivider()
        }
        item {
            Text(
                text = stringResource(R.string.dew_point),
                style = MaterialTheme.typography.headlineSmall
            )
        }
        item {
            Spacer(modifier = Modifier.height(dimensionResource(R.dimen.normal_margin)))
        }
        item {
            DewPointHeader(location, daily, activeDewPointItem, defaultDewPointValue)
        }
        item {
            Spacer(modifier = Modifier.height(dimensionResource(R.dimen.normal_margin)))
        }
        if (mappedDewPointValues.size >= DetailScreen.CHART_MIN_COUNT) {
            item {
                DewPointChart(location, mappedDewPointValues, daily, dewPointMarkerVisibilityListener)
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
            DetailsSectionHeader(stringResource(R.string.dew_point_about))
        }
        item {
            DetailsCardText(
                stringResource(R.string.dew_point_about_description, UnitUtils.formatPercent(context, 100.0))
            )
        }
        bottomInsetItem()
    }
}

@Composable
fun HumidityHeader(
    location: Location,
    daily: Daily,
    activeItem: Pair<Date, Double>?,
    defaultValue: Pair<Date, Double>?,
) {
    val context = LocalContext.current

    if (activeItem != null) {
        HumidityItem(
            header = {
                TextFixedHeight(
                    text = activeItem.first.getFormattedTime(location, context, context.is12Hour),
                    style = MaterialTheme.typography.labelMedium
                )
            },
            relativeHumidity = activeItem.second
        )
    } else if (daily.relativeHumidity?.min != null && daily.relativeHumidity!!.max != null) {
        HumiditySummary(location, daily)
    } else {
        HumidityItem(
            header = {
                TextFixedHeight(
                    text = defaultValue?.first?.getFormattedTime(location, context, context.is12Hour) ?: "",
                    style = MaterialTheme.typography.labelMedium
                )
            },
            relativeHumidity = defaultValue?.second
        )
    }
}

@Composable
private fun HumiditySummary(
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
            text = daily.relativeHumidity?.getRangeSummary(context) ?: "",
            style = MaterialTheme.typography.displaySmall
        )
    }
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
        TextFixedHeight(
            text = relativeHumidity?.let {
                UnitUtils.formatPercent(context, it)
            } ?: "",
            style = MaterialTheme.typography.displaySmall
        )
    }
}

@Composable
private fun HumidityChart(
    location: Location,
    mappedValues: ImmutableMap<Long, Double>,
    daily: Daily,
    markerVisibilityListener: CartesianMarkerVisibilityListener,
) {
    val context = LocalContext.current

    val maxY = 100.0

    val endAxisValueFormatter = CartesianValueFormatter { _, value, _ ->
        UnitUtils.formatPercent(context, value)
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

    BreezyLineChart(
        location,
        modelProducer,
        daily.date,
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
                UnitUtils.formatPercent(context, it)
            } ?: "-"
        },
        endAxisItemPlacer = remember {
            VerticalAxis.ItemPlacer.step({ 20.0 }) // Every 20 %
        },
        markerVisibilityListener = markerVisibilityListener
    )
}

@Composable
fun DewPointHeader(
    location: Location,
    daily: Daily,
    activeItem: Pair<Date, Temperature>?,
    defaultValue: Pair<Date, Temperature>?,
) {
    val context = LocalContext.current

    if (activeItem != null) {
        DewPointItem(
            header = {
                TextFixedHeight(
                    text = activeItem.first.getFormattedTime(location, context, context.is12Hour),
                    style = MaterialTheme.typography.labelMedium
                )
            },
            dewPoint = activeItem.second
        )
    } else if (daily.dewPoint?.min != null && daily.dewPoint!!.max != null) {
        DewPointSummary(location, daily)
    } else {
        DewPointItem(
            header = {
                TextFixedHeight(
                    text = defaultValue?.first?.getFormattedTime(location, context, context.is12Hour) ?: "",
                    style = MaterialTheme.typography.labelMedium
                )
            },
            dewPoint = defaultValue?.second
        )
    }
}

@Composable
private fun DewPointSummary(
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
            text = daily.dewPoint?.getRangeSummary(context) ?: "",
            style = MaterialTheme.typography.displaySmall,
            modifier = Modifier
                .clearAndSetSemantics {
                    daily.dewPoint?.getRangeContentDescriptionSummary(context)?.let {
                        contentDescription = it
                    }
                }
        )
    }
}

@Composable
private fun DewPointItem(
    header: @Composable () -> Unit,
    dewPoint: Temperature?,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current

    Column(
        modifier = modifier.fillMaxWidth()
    ) {
        header()
        TextFixedHeight(
            text = dewPoint?.formatMeasure(context, unitWidth = UnitWidth.NARROW) ?: "",
            style = MaterialTheme.typography.displaySmall,
            modifier = Modifier
                .clearAndSetSemantics {
                    dewPoint?.let {
                        contentDescription = it.formatMeasure(context, unitWidth = UnitWidth.LONG)
                    }
                }
        )
    }
}

@Composable
private fun DewPointChart(
    location: Location,
    mappedValues: ImmutableMap<Long, Temperature>,
    daily: Daily,
    markerVisibilityListener: CartesianMarkerVisibilityListener,
) {
    val context = LocalContext.current

    val temperatureUnit = SettingsManager.getInstance(context).getTemperatureUnit(context)
    val step = temperatureUnit.chartStep
    val maxY = remember(mappedValues) {
        mappedValues.values.max().toDouble(temperatureUnit).roundUpToNearestMultiplier(step)
    }
    val minY = remember(mappedValues) {
        mappedValues.values.min().toDouble(temperatureUnit).roundDownToNearestMultiplier(step)
    }

    val modelProducer = remember { CartesianChartModelProducer() }

    LaunchedEffect(location) {
        modelProducer.runTransaction {
            lineSeries {
                series(
                    x = mappedValues.keys,
                    y = mappedValues.values.map { it.toDouble(temperatureUnit) }
                )
            }
        }
    }

    BreezyLineChart(
        location,
        modelProducer,
        daily.date,
        maxY,
        { _, value, _ ->
            value.toTemperature(temperatureUnit)
                .formatMeasure(context, valueWidth = UnitWidth.NARROW, unitWidth = UnitWidth.NARROW)
        },
        persistentListOf(
            persistentMapOf(
                // TODO: Duplicate of temperature colors
                47.celsius.toDouble(temperatureUnit).toFloat() to Color(71, 14, 0),
                30.celsius.toDouble(temperatureUnit).toFloat() to Color(232, 83, 25),
                21.celsius.toDouble(temperatureUnit).toFloat() to Color(243, 183, 4),
                10.celsius.toDouble(temperatureUnit).toFloat() to Color(128, 147, 24),
                1.celsius.toDouble(temperatureUnit).toFloat() to Color(68, 125, 99),
                0.celsius.toDouble(temperatureUnit).toFloat() to Color(93, 133, 198),
                -4.celsius.toDouble(temperatureUnit).toFloat() to Color(100, 166, 189),
                -8.celsius.toDouble(temperatureUnit).toFloat() to Color(106, 191, 181),
                -15.celsius.toDouble(temperatureUnit).toFloat() to Color(157, 219, 217),
                -25.celsius.toDouble(temperatureUnit).toFloat() to Color(143, 89, 169),
                -40.celsius.toDouble(temperatureUnit).toFloat() to Color(162, 70, 145),
                -55.celsius.toDouble(temperatureUnit).toFloat() to Color(202, 172, 195),
                -70.celsius.toDouble(temperatureUnit).toFloat() to Color(115, 70, 105)
            )
        ),
        topAxisValueFormatter = { _, value, _ ->
            mappedValues.getOrElse(value.toLong()) { null }?.formatMeasure(
                context,
                valueWidth = UnitWidth.NARROW,
                unitWidth = UnitWidth.NARROW
            ) ?: "-"
        },
        minY = minY,
        endAxisItemPlacer = remember {
            VerticalAxis.ItemPlacer.step({ step })
        },
        markerVisibilityListener = markerVisibilityListener
    )
}
