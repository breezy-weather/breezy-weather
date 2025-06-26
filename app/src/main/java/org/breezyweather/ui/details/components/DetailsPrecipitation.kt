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
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.isTraversalGroup
import androidx.compose.ui.semantics.semantics
import breezyweather.domain.location.model.Location
import breezyweather.domain.weather.model.Daily
import breezyweather.domain.weather.model.Hourly
import breezyweather.domain.weather.model.Precipitation
import breezyweather.domain.weather.model.PrecipitationDuration
import breezyweather.domain.weather.model.PrecipitationProbability
import com.patrykandpatrick.vico.core.cartesian.axis.VerticalAxis
import com.patrykandpatrick.vico.core.cartesian.data.CartesianChartModelProducer
import com.patrykandpatrick.vico.core.cartesian.data.CartesianValueFormatter
import com.patrykandpatrick.vico.core.cartesian.data.columnSeries
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
import org.breezyweather.common.basic.models.options.appearance.DetailScreen
import org.breezyweather.common.basic.models.options.unit.DurationUnit
import org.breezyweather.common.extensions.currentLocale
import org.breezyweather.common.extensions.getFormattedTime
import org.breezyweather.common.extensions.is12Hour
import org.breezyweather.common.extensions.roundUpToNearestMultiplier
import org.breezyweather.common.extensions.toDate
import org.breezyweather.domain.settings.SettingsManager
import org.breezyweather.ui.common.charts.BreezyBarChart
import org.breezyweather.ui.common.charts.BreezyLineChart
import org.breezyweather.ui.settings.preference.bottomInsetItem
import java.text.NumberFormat
import kotlin.math.max

@Composable
fun DetailsPrecipitation(
    location: Location,
    hourlyList: ImmutableList<Hourly>,
    daily: Daily,
    modifier: Modifier = Modifier,
) {
    val mappedQuantityValues = remember(hourlyList) {
        hourlyList
            .filter { it.precipitation?.total != null }
            .associate { it.date.time to it.precipitation!!.total!! }
            .toImmutableMap()
    }
    val mappedProbabilityValues = remember(hourlyList) {
        hourlyList
            .filter { it.precipitationProbability?.total != null }
            .associate { it.date.time to it.precipitationProbability!!.total!! }
            .toImmutableMap()
    }

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(
            horizontal = dimensionResource(R.dimen.normal_margin),
            vertical = dimensionResource(R.dimen.little_margin)
        )
    ) {
        item {
            PrecipitationChart(
                location,
                mappedQuantityValues,
                daily
            )
        }
        item {
            PrecipitationDetails(daily.day?.precipitation, daily.night?.precipitation)
        }
        item {
            DetailsSectionDivider()
        }
        item {
            DetailsSectionHeader(stringResource(R.string.precipitation_probability))
        }
        item {
            Spacer(modifier = Modifier.height(dimensionResource(R.dimen.little_margin)))
        }
        item {
            PrecipitationProbabilityChart(
                location,
                mappedProbabilityValues,
                daily
            )
        }
        // TODO: Short explanation
        item {
            PrecipitationProbabilityDetails(
                daily.day?.precipitationProbability,
                daily.night?.precipitationProbability
            )
        }
        if ((daily.day?.precipitationDuration?.total ?: 0.0) > 0.0 ||
            (daily.night?.precipitationDuration?.total ?: 0.0) > 0.0
        ) {
            item {
                DetailsSectionDivider()
            }
            item {
                DetailsSectionHeader(stringResource(R.string.precipitation_duration))
            }
            item {
                Spacer(modifier = Modifier.height(dimensionResource(R.dimen.little_margin)))
            }
            item {
                PrecipitationDurationSummary(
                    daily.day?.precipitationDuration,
                    daily.night?.precipitationDuration
                )
            }
        }
        bottomInsetItem()
    }
}

@Composable
private fun PrecipitationItem(
    header: @Composable () -> Unit,
    precipitation: Double?,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val precipitationUnit = SettingsManager.getInstance(context).precipitationUnit

    Column(
        modifier = modifier.fillMaxWidth()
    ) {
        header()
        precipitation?.let { prec ->
            Text(
                text = precipitationUnit.getValueText(context, prec),
                style = MaterialTheme.typography.displaySmall,
                modifier = Modifier
                    .clearAndSetSemantics {
                        contentDescription = precipitationUnit.getValueVoice(context, prec)
                    }
            )
        }
    }
}

@Composable
private fun PrecipitationSummary(
    daytimePrecipitation: Precipitation?,
    nighttimePrecipitation: Precipitation?,
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.little_margin)),
        modifier = Modifier
            .fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .semantics { isTraversalGroup = true }
        ) {
            daytimePrecipitation?.total?.let {
                PrecipitationItem(
                    header = {
                        Text(
                            text = stringResource(R.string.daytime),
                            style = MaterialTheme.typography.labelMedium
                        )
                    },
                    precipitation = it
                )
            }
        }
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .semantics { isTraversalGroup = true }
        ) {
            nighttimePrecipitation?.total?.let {
                PrecipitationItem(
                    header = { NighttimeWithInfo() },
                    precipitation = it
                )
            }
        }
    }
}

@Composable
internal fun PrecipitationChart(
    location: Location,
    mappedValues: ImmutableMap<Long, Double>,
    daily: Daily,
) {
    val context = LocalContext.current

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
        mappedValues.getOrElse(it.x.toLong()) { null }?.let { precipitation ->
            PrecipitationItem(
                header = {
                    Text(
                        text = it.x.toLong().toDate().getFormattedTime(location, context, context.is12Hour),
                        style = MaterialTheme.typography.labelMedium
                    )
                },
                precipitation = precipitation
            )
        }
    } ?: PrecipitationSummary(daily.day?.precipitation, daily.night?.precipitation)

    Spacer(modifier = Modifier.height(dimensionResource(R.dimen.normal_margin)))

    if (mappedValues.size >= DetailScreen.CHART_MIN_COUNT) {
        val precipitationUnit = SettingsManager.getInstance(context).precipitationUnit
        val step = precipitationUnit.chartStep
        val maxY = remember(mappedValues) {
            max(
                Precipitation.PRECIPITATION_HOURLY_HEAVY,
                mappedValues.values.max()
            ).let {
                precipitationUnit.getValueWithoutUnit(it)
            }.roundUpToNearestMultiplier(step)
        }

        val modelProducer = remember { CartesianChartModelProducer() }

        LaunchedEffect(location) {
            modelProducer.runTransaction {
                columnSeries {
                    series(
                        x = mappedValues.keys,
                        y = mappedValues.values.map { precipitationUnit.getValueWithoutUnit(it) }
                    )
                }
            }
        }

        BreezyBarChart(
            location,
            modelProducer,
            daily.date,
            maxY,
            { _, value, _ -> precipitationUnit.getValueText(context, value, isValueInDefaultUnit = false) },
            Fill(Color(60, 116, 160).toArgb()),
            /*persistentMapOf(
                50 to Fill(Color(168, 168, 168).toArgb()),
                31 to Fill(Color(161, 59, 161).toArgb()),
                20 to Fill(Color(161, 59, 59).toArgb()),
                15 to Fill(Color(161, 161, 59).toArgb()),
                10 to Fill(Color(130, 161, 59).toArgb()),
                8 to Fill(Color(59, 161, 61).toArgb()),
                6 to Fill(Color(59, 161, 161).toArgb()),
                0.6 to Fill(Color(60, 116, 160).toArgb()),
                0.0 to Fill(Color(111, 111, 111).toArgb())
            )*/
            trendHorizontalLines = buildMap {
                put(
                    precipitationUnit.getValueWithoutUnit(Precipitation.PRECIPITATION_HOURLY_HEAVY),
                    context.getString(R.string.precipitation_intensity_heavy)
                )
                if (maxY < precipitationUnit.getValueWithoutUnit(Precipitation.PRECIPITATION_HOURLY_HEAVY.times(2.0))) {
                    put(
                        precipitationUnit.getValueWithoutUnit(Precipitation.PRECIPITATION_HOURLY_MEDIUM),
                        context.getString(R.string.precipitation_intensity_medium)
                    )
                    put(
                        precipitationUnit.getValueWithoutUnit(Precipitation.PRECIPITATION_HOURLY_LIGHT),
                        context.getString(R.string.precipitation_intensity_light)
                    )
                }
            }.toImmutableMap(),
            endAxisItemPlacer = remember {
                VerticalAxis.ItemPlacer.step({ step })
            },
            markerVisibilityListener = markerVisibilityListener
        )
    } else {
        UnavailableChart(mappedValues.size)
    }
}

@Composable
private fun PrecipitationDetails(
    daytimePrecipitation: Precipitation?,
    nighttimePrecipitation: Precipitation?,
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.little_margin)),
        modifier = Modifier
            .padding(top = dimensionResource(R.dimen.normal_margin))
            .fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .semantics { isTraversalGroup = true }
        ) {
            DailyPrecipitationDetails(isDaytime = true, daytimePrecipitation)
        }
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .semantics { isTraversalGroup = true }
        ) {
            DailyPrecipitationDetails(isDaytime = false, nighttimePrecipitation)
        }
    }
}

@Composable
fun DailyPrecipitationDetails(
    isDaytime: Boolean,
    precipitation: Precipitation?,
) {
    val context = LocalContext.current
    val precipitationUnit = SettingsManager.getInstance(context).precipitationUnit
    val precipitationItems = buildList {
        precipitation?.let { prec ->
            if ((prec.rain ?: 0.0) > 0) {
                add(Pair(R.string.precipitation_rain, prec.rain!!))
            }
            if ((prec.snow ?: 0.0) > 0) {
                add(Pair(R.string.precipitation_snow, prec.snow!!))
            }
            if ((prec.ice ?: 0.0) > 0) {
                add(Pair(R.string.precipitation_ice, prec.ice!!))
            }
            if ((prec.thunderstorm ?: 0.0) > 0) {
                add(Pair(R.string.precipitation_thunderstorm, prec.thunderstorm!!))
            }
        }
    }
    if (precipitationItems.isNotEmpty()) {
        if (isDaytime) {
            Text(
                text = stringResource(R.string.daytime),
                style = MaterialTheme.typography.labelMedium
            )
        } else {
            NighttimeWithInfo()
        }
    }
    precipitationItems.forEach { item ->
        DetailsItem(
            headlineText = stringResource(item.first),
            supportingText = precipitationUnit.getValueText(context, item.second),
            modifier = Modifier
                .padding(top = dimensionResource(R.dimen.normal_margin))
                .semantics(mergeDescendants = true) {}
                .clearAndSetSemantics {
                    contentDescription = context.getString(item.first) +
                        context.getString(R.string.colon_separator) +
                        precipitationUnit.getValueVoice(context, item.second)
                }
        )
    }
}

@Composable
private fun PrecipitationProbabilityItem(
    header: @Composable () -> Unit,
    precipitationProbability: Double?,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current

    Column(
        modifier = modifier.fillMaxWidth()
    ) {
        header()
        precipitationProbability?.let { pp ->
            Text(
                text = NumberFormat.getPercentInstance(context.currentLocale).apply {
                    maximumFractionDigits = 0
                }.format(pp.div(100.0)),
                style = MaterialTheme.typography.displaySmall
            )
        }
    }
}

@Composable
private fun PrecipitationProbabilitySummary(
    daytimePrecipitationProbability: PrecipitationProbability?,
    nighttimePrecipitationProbability: PrecipitationProbability?,
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.little_margin)),
        modifier = Modifier
            .fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .semantics { isTraversalGroup = true }
        ) {
            daytimePrecipitationProbability?.total?.let {
                PrecipitationProbabilityItem(
                    {
                        Text(
                            text = stringResource(R.string.daytime),
                            style = MaterialTheme.typography.labelMedium
                        )
                    },
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
            nighttimePrecipitationProbability?.total?.let {
                PrecipitationProbabilityItem(
                    { NighttimeWithInfo() },
                    it
                )
            }
        }
    }
}

@Composable
internal fun PrecipitationProbabilityChart(
    location: Location,
    mappedValues: ImmutableMap<Long, Double>,
    daily: Daily,
) {
    val context = LocalContext.current

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
        mappedValues.getOrElse(it.x.toLong()) { null }?.let { precipitationProbability ->
            PrecipitationProbabilityItem(
                header = {
                    Text(
                        text = it.x.toLong().toDate().getFormattedTime(location, context, context.is12Hour),
                        style = MaterialTheme.typography.labelMedium
                    )
                },
                precipitationProbability = precipitationProbability
            )
        }
    } ?: PrecipitationProbabilitySummary(daily.day?.precipitationProbability, daily.night?.precipitationProbability)

    Spacer(modifier = Modifier.height(dimensionResource(R.dimen.normal_margin)))

    if (mappedValues.size >= DetailScreen.CHART_MIN_COUNT) {
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

        BreezyLineChart(
            location,
            modelProducer,
            daily.date,
            maxY,
            endAxisValueFormatter,
            persistentListOf(
                persistentMapOf(
                    // TODO
                    100f to Color(60, 116, 160),
                    0f to Color(60, 116, 160)
                )
            ),
            endAxisItemPlacer = remember {
                VerticalAxis.ItemPlacer.step({ 20.0 }) // Every 20 %
            },
            markerVisibilityListener = markerVisibilityListener
        )
    } else {
        UnavailableChart(mappedValues.size)
    }
}

@Composable
internal fun PrecipitationProbabilityDetails(
    daytimePrecipitationProbability: PrecipitationProbability?,
    nighttimePrecipitationProbability: PrecipitationProbability?,
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.little_margin)),
        modifier = Modifier
            .padding(top = dimensionResource(R.dimen.normal_margin))
            .fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .semantics { isTraversalGroup = true }
        ) {
            DailyPrecipitationProbabilityDetails(
                isDaytime = true,
                daytimePrecipitationProbability
            )
        }
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .semantics { isTraversalGroup = true }
        ) {
            DailyPrecipitationProbabilityDetails(
                isDaytime = false,
                nighttimePrecipitationProbability
            )
        }
    }
}

@Composable
internal fun DailyPrecipitationProbabilityDetails(
    isDaytime: Boolean,
    precipitationProbability: PrecipitationProbability?,
) {
    val context = LocalContext.current
    val percentUnit = NumberFormat.getPercentInstance(context.currentLocale).apply {
        maximumFractionDigits = 0
    }
    val precipitationProbabilityItems = buildList {
        precipitationProbability?.let { pp ->
            if ((pp.rain ?: 0.0) > 0) {
                add(Pair(R.string.precipitation_rain, pp.rain!!))
            }
            if ((pp.snow ?: 0.0) > 0) {
                add(Pair(R.string.precipitation_snow, pp.snow!!))
            }
            if ((pp.ice ?: 0.0) > 0) {
                add(Pair(R.string.precipitation_ice, pp.ice!!))
            }
            if ((pp.thunderstorm ?: 0.0) > 0) {
                add(Pair(R.string.precipitation_thunderstorm, pp.thunderstorm!!))
            }
        }
    }
    if (precipitationProbabilityItems.isNotEmpty()) {
        if (isDaytime) {
            Text(
                text = stringResource(R.string.daytime),
                style = MaterialTheme.typography.labelMedium
            )
        } else {
            NighttimeWithInfo()
        }
    }
    precipitationProbabilityItems.forEach { item ->
        DetailsItem(
            headlineText = stringResource(item.first),
            supportingText = percentUnit.format(item.second.div(100.0)),
            modifier = Modifier
                .padding(top = dimensionResource(R.dimen.normal_margin))
                .semantics(mergeDescendants = true) {}
                .clearAndSetSemantics {
                    contentDescription = context.getString(item.first) +
                        context.getString(R.string.colon_separator) +
                        percentUnit.format(item.second.div(100.0))
                }
        )
    }
}

@Composable
private fun PrecipitationDurationSummary(
    daytimePrecipitationDuration: PrecipitationDuration?,
    nighttimePrecipitationDuration: PrecipitationDuration?,
) {
    val context = LocalContext.current

    Row(
        horizontalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.little_margin)),
        modifier = Modifier
            .fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .semantics { isTraversalGroup = true }
        ) {
            daytimePrecipitationDuration?.let { precDur ->
                precDur.total?.let {
                    Text(
                        text = stringResource(R.string.daytime),
                        style = MaterialTheme.typography.labelMedium
                    )
                    Text(
                        text = DurationUnit.H.getValueText(context, it),
                        style = MaterialTheme.typography.displaySmall,
                        modifier = Modifier
                            .clearAndSetSemantics {
                                contentDescription = DurationUnit.H.getValueVoice(context, it)
                            }
                    )
                    DailyPrecipitationDurationDetails(precDur)
                }
            }
        }
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .semantics { isTraversalGroup = true }
        ) {
            nighttimePrecipitationDuration?.let { precDur ->
                precDur.total?.let {
                    NighttimeWithInfo()
                    Text(
                        text = DurationUnit.H.getValueText(context, it),
                        style = MaterialTheme.typography.displaySmall,
                        modifier = Modifier
                            .clearAndSetSemantics {
                                contentDescription = DurationUnit.H.getValueVoice(context, it)
                            }
                    )
                    DailyPrecipitationDurationDetails(precDur)
                }
            }
        }
    }
}

@Composable
fun DailyPrecipitationDurationDetails(
    precipitationDuration: PrecipitationDuration?,
) {
    val context = LocalContext.current
    val durationUnit = DurationUnit.H
    val precipitationDurationItems = buildList {
        precipitationDuration?.let { precDur ->
            if ((precDur.rain ?: 0.0) > 0) {
                add(Pair(R.string.precipitation_rain, precDur.rain!!))
            }
            if ((precDur.snow ?: 0.0) > 0) {
                add(Pair(R.string.precipitation_snow, precDur.snow!!))
            }
            if ((precDur.ice ?: 0.0) > 0) {
                add(Pair(R.string.precipitation_ice, precDur.ice!!))
            }
            if ((precDur.thunderstorm ?: 0.0) > 0) {
                add(Pair(R.string.precipitation_thunderstorm, precDur.thunderstorm!!))
            }
        }
    }
    precipitationDurationItems.forEach { item ->
        DetailsItem(
            headlineText = stringResource(item.first),
            supportingText = durationUnit.getValueText(context, item.second),
            modifier = Modifier
                .padding(top = dimensionResource(R.dimen.normal_margin))
                .semantics(mergeDescendants = true) {}
                .clearAndSetSemantics {
                    contentDescription = context.getString(item.first) +
                        context.getString(R.string.colon_separator) +
                        durationUnit.getValueVoice(context, item.second)
                }
        )
    }
}
