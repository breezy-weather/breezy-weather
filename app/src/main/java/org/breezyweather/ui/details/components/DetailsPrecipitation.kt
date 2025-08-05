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
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.buildAnnotatedString
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
import org.breezyweather.common.basic.models.options.basic.UnitUtils
import org.breezyweather.common.basic.models.options.unit.DurationUnit
import org.breezyweather.common.extensions.getFormattedTime
import org.breezyweather.common.extensions.is12Hour
import org.breezyweather.common.extensions.roundUpToNearestMultiplier
import org.breezyweather.common.extensions.toDate
import org.breezyweather.domain.settings.SettingsManager
import org.breezyweather.ui.common.charts.BreezyBarChart
import org.breezyweather.ui.common.charts.BreezyLineChart
import org.breezyweather.ui.settings.preference.bottomInsetItem
import java.util.Date
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
    var activeQuantityItem: Pair<Date, Double>? by remember { mutableStateOf(null) }
    val quantityMarkerVisibilityListener = remember {
        object : CartesianMarkerVisibilityListener {
            override fun onShown(marker: CartesianMarker, targets: List<CartesianMarker.Target>) {
                activeQuantityItem = targets.firstOrNull()?.let { target ->
                    mappedQuantityValues.getOrElse(target.x.toLong()) { null }?.let {
                        Pair(target.x.toLong().toDate(), it)
                    }
                }
            }

            override fun onUpdated(marker: CartesianMarker, targets: List<CartesianMarker.Target>) {
                onShown(marker, targets)
            }

            override fun onHidden(marker: CartesianMarker) {
                activeQuantityItem = null
            }
        }
    }

    val mappedProbabilityValues = remember(hourlyList) {
        hourlyList
            .filter { it.precipitationProbability?.total != null }
            .associate { it.date.time to it.precipitationProbability!!.total!! }
            .toImmutableMap()
    }
    var activeProbabilityItem: Pair<Date, Double>? by remember { mutableStateOf(null) }
    val probabilityMarkerVisibilityListener = remember {
        object : CartesianMarkerVisibilityListener {
            override fun onShown(marker: CartesianMarker, targets: List<CartesianMarker.Target>) {
                activeProbabilityItem = targets.firstOrNull()?.let { target ->
                    mappedProbabilityValues.getOrElse(target.x.toLong()) { null }?.let {
                        Pair(target.x.toLong().toDate(), it)
                    }
                }
            }

            override fun onUpdated(marker: CartesianMarker, targets: List<CartesianMarker.Target>) {
                onShown(marker, targets)
            }

            override fun onHidden(marker: CartesianMarker) {
                activeProbabilityItem = null
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
            PrecipitationHeader(location, daily, activeQuantityItem)
        }
        item {
            Spacer(modifier = Modifier.height(dimensionResource(R.dimen.normal_margin)))
        }
        if (mappedQuantityValues.size >= DetailScreen.CHART_MIN_COUNT) {
            item {
                PrecipitationChart(location, mappedQuantityValues, daily, quantityMarkerVisibilityListener)
            }
        } else {
            item {
                UnavailableChart(mappedQuantityValues.size)
            }
        }
        item {
            PrecipitationDetails(daily.day?.precipitation, daily.night?.precipitation)
        }
        if (daily.day?.precipitationProbability != null ||
            daily.night?.precipitationProbability != null ||
            mappedProbabilityValues.isNotEmpty()
        ) {
            item {
                DetailsSectionDivider()
            }
            item {
                Text(
                    text = stringResource(R.string.precipitation_probability),
                    style = MaterialTheme.typography.headlineSmall
                )
            }
            item {
                Spacer(modifier = Modifier.height(dimensionResource(R.dimen.small_margin)))
            }
            item {
                PrecipitationProbabilityHeader(location, daily, activeProbabilityItem)
            }
            item {
                Spacer(modifier = Modifier.height(dimensionResource(R.dimen.normal_margin)))
            }
            if (mappedProbabilityValues.size >= DetailScreen.CHART_MIN_COUNT) {
                item {
                    PrecipitationProbabilityChart(
                        location,
                        mappedProbabilityValues,
                        daily,
                        probabilityMarkerVisibilityListener
                    )
                }
            } else {
                item {
                    UnavailableChart(mappedProbabilityValues.size)
                }
            }
            // TODO: Short explanation
            item {
                PrecipitationProbabilityDetails(
                    daily.day?.precipitationProbability,
                    daily.night?.precipitationProbability
                )
            }
        }
        if ((daily.day?.precipitationDuration?.total ?: 0.0) > 0.0 ||
            (daily.night?.precipitationDuration?.total ?: 0.0) > 0.0
        ) {
            item {
                DetailsSectionDivider()
            }
            item {
                Text(
                    text = stringResource(R.string.precipitation_duration),
                    style = MaterialTheme.typography.headlineSmall
                )
            }
            item {
                Spacer(modifier = Modifier.height(dimensionResource(R.dimen.small_margin)))
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
fun PrecipitationHeader(
    location: Location,
    daily: Daily,
    activeItem: Pair<Date, Double>?,
) {
    val context = LocalContext.current

    activeItem?.let {
        PrecipitationItem(
            header = {
                TextFixedHeight(
                    text = it.first.getFormattedTime(location, context, context.is12Hour),
                    style = MaterialTheme.typography.labelMedium
                )
            },
            precipitation = it.second
        )
    } ?: PrecipitationSummary(daily.day?.precipitation, daily.night?.precipitation)
}

@Composable
private fun PrecipitationItem(
    header: @Composable () -> Unit,
    precipitation: Double?,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val precipitationUnit = SettingsManager.getInstance(context).getPrecipitationUnit(context)

    Column(
        modifier = modifier.fillMaxWidth()
    ) {
        header()
        TextFixedHeight(
            text = precipitation?.let { prec ->
                UnitUtils.formatUnitsDifferentFontSize(
                    formattedMeasure = precipitationUnit.formatMeasure(context, prec),
                    fontSize = MaterialTheme.typography.headlineSmall.fontSize
                )
            } ?: AnnotatedString(""),
            style = MaterialTheme.typography.displaySmall,
            modifier = Modifier
                .clearAndSetSemantics {
                    precipitation?.let { prec ->
                        contentDescription = precipitationUnit.formatContentDescription(context, prec)
                    }
                }
        )
    }
}

@Composable
private fun PrecipitationSummary(
    daytimePrecipitation: Precipitation?,
    nighttimePrecipitation: Precipitation?,
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
            daytimePrecipitation?.total?.let {
                PrecipitationItem(
                    header = { DaytimeLabel() },
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
                    header = { NighttimeLabelWithInfo() },
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
    markerVisibilityListener: CartesianMarkerVisibilityListener,
) {
    val context = LocalContext.current

    val precipitationUnit = SettingsManager.getInstance(context).getPrecipitationUnit(context)
    val step = precipitationUnit.chartStep
    val maxY = remember(mappedValues) {
        max(
            Precipitation.PRECIPITATION_HOURLY_HEAVY,
            mappedValues.values.max()
        ).let {
            precipitationUnit.getConvertedUnit(it)
        }.roundUpToNearestMultiplier(step)
    }

    val modelProducer = remember { CartesianChartModelProducer() }

    LaunchedEffect(location) {
        modelProducer.runTransaction {
            columnSeries {
                series(
                    x = mappedValues.keys,
                    y = mappedValues.values.map { precipitationUnit.getConvertedUnit(it) }
                )
            }
        }
    }

    BreezyBarChart(
        location,
        modelProducer,
        daily.date,
        maxY,
        { _, value, _ -> precipitationUnit.formatMeasure(context, value, isValueInDefaultUnit = false) },
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
                precipitationUnit.getConvertedUnit(Precipitation.PRECIPITATION_HOURLY_HEAVY),
                context.getString(R.string.precipitation_intensity_heavy)
            )
            if (maxY < precipitationUnit.getConvertedUnit(Precipitation.PRECIPITATION_HOURLY_HEAVY.times(2.0))) {
                put(
                    precipitationUnit.getConvertedUnit(Precipitation.PRECIPITATION_HOURLY_MEDIUM),
                    context.getString(R.string.precipitation_intensity_medium)
                )
                put(
                    precipitationUnit.getConvertedUnit(Precipitation.PRECIPITATION_HOURLY_LIGHT),
                    context.getString(R.string.precipitation_intensity_light)
                )
            }
        }.toImmutableMap(),
        endAxisItemPlacer = remember {
            VerticalAxis.ItemPlacer.step({ step })
        },
        markerVisibilityListener = markerVisibilityListener
    )
}

@Composable
private fun PrecipitationDetails(
    daytimePrecipitation: Precipitation?,
    nighttimePrecipitation: Precipitation?,
) {
    val daytimePrecipitationItems = mutableListOf<Pair<Int, Double?>>()
    val nighttimePrecipitationItems = mutableListOf<Pair<Int, Double?>>()

    if ((daytimePrecipitation?.rain ?: 0.0) > 0 || (nighttimePrecipitation?.rain ?: 0.0) > 0) {
        daytimePrecipitationItems.add(Pair(R.string.precipitation_rain, daytimePrecipitation?.rain))
        nighttimePrecipitationItems.add(Pair(R.string.precipitation_rain, nighttimePrecipitation?.rain))
    }
    if ((daytimePrecipitation?.snow ?: 0.0) > 0 || (nighttimePrecipitation?.snow ?: 0.0) > 0) {
        daytimePrecipitationItems.add(Pair(R.string.precipitation_snow, daytimePrecipitation?.snow))
        nighttimePrecipitationItems.add(Pair(R.string.precipitation_snow, nighttimePrecipitation?.snow))
    }
    if ((daytimePrecipitation?.ice ?: 0.0) > 0 || (nighttimePrecipitation?.ice ?: 0.0) > 0) {
        daytimePrecipitationItems.add(Pair(R.string.precipitation_ice, daytimePrecipitation?.ice))
        nighttimePrecipitationItems.add(Pair(R.string.precipitation_ice, nighttimePrecipitation?.ice))
    }
    if ((daytimePrecipitation?.thunderstorm ?: 0.0) > 0 || (nighttimePrecipitation?.thunderstorm ?: 0.0) > 0) {
        daytimePrecipitationItems.add(Pair(R.string.precipitation_thunderstorm, daytimePrecipitation?.thunderstorm))
        nighttimePrecipitationItems.add(Pair(R.string.precipitation_thunderstorm, nighttimePrecipitation?.thunderstorm))
    }

    if (daytimePrecipitationItems.isNotEmpty()) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.small_margin)),
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
                DaytimeLabel()
                daytimePrecipitationItems.forEach {
                    DailyPrecipitationDetail(it)
                }
            }
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .semantics { isTraversalGroup = true }
            ) {
                NighttimeLabelWithInfo()
                nighttimePrecipitationItems.forEach {
                    DailyPrecipitationDetail(it)
                }
            }
        }
    }
}

@Composable
fun DailyPrecipitationDetail(
    item: Pair<Int, Double?>,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val unit = if (item.first == R.string.precipitation_snow) {
        SettingsManager.getInstance(context).getSnowfallUnit(context)
    } else {
        SettingsManager.getInstance(context).getPrecipitationUnit(context)
    }
    DetailsItem(
        headlineText = stringResource(item.first),
        supportingText = item.second?.let { prec -> unit.formatMeasure(context, prec) }
            ?: stringResource(R.string.null_data_text),
        modifier = modifier
            .padding(top = dimensionResource(R.dimen.normal_margin))
            .semantics(mergeDescendants = true) {}
            .clearAndSetSemantics {
                item.second?.let { prec ->
                    contentDescription = context.getString(item.first) +
                        context.getString(R.string.colon_separator) +
                        unit.formatContentDescription(context, prec)
                }
            }
    )
}

@Composable
fun PrecipitationProbabilityHeader(
    location: Location,
    daily: Daily,
    activeItem: Pair<Date, Double>?,
) {
    val context = LocalContext.current

    activeItem?.let {
        PrecipitationProbabilityItem(
            header = {
                TextFixedHeight(
                    text = it.first.getFormattedTime(location, context, context.is12Hour),
                    style = MaterialTheme.typography.labelMedium
                )
            },
            precipitationProbability = it.second
        )
    } ?: PrecipitationProbabilitySummary(daily.day?.precipitationProbability, daily.night?.precipitationProbability)
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
        TextFixedHeight(
            text = precipitationProbability?.let { pp -> UnitUtils.formatPercent(context, pp) } ?: "",
            style = MaterialTheme.typography.displaySmall
        )
    }
}

@Composable
private fun PrecipitationProbabilitySummary(
    daytimePrecipitationProbability: PrecipitationProbability?,
    nighttimePrecipitationProbability: PrecipitationProbability?,
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
            PrecipitationProbabilityItem(
                { DaytimeLabel() },
                daytimePrecipitationProbability?.total
            )
        }
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .semantics { isTraversalGroup = true }
        ) {
            PrecipitationProbabilityItem(
                { NighttimeLabelWithInfo() },
                nighttimePrecipitationProbability?.total
            )
        }
    }
}

@Composable
internal fun PrecipitationProbabilityChart(
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
}

@Composable
internal fun PrecipitationProbabilityDetails(
    daytimePrecProb: PrecipitationProbability?,
    nighttimePrecProb: PrecipitationProbability?,
) {
    val daytimePrecProbItems = mutableListOf<Pair<Int, Double?>>()
    val nighttimePrecProbItems = mutableListOf<Pair<Int, Double?>>()

    if ((daytimePrecProb?.rain ?: 0.0) > 0 || (nighttimePrecProb?.rain ?: 0.0) > 0) {
        daytimePrecProbItems.add(Pair(R.string.precipitation_rain, daytimePrecProb?.rain))
        nighttimePrecProbItems.add(Pair(R.string.precipitation_rain, nighttimePrecProb?.rain))
    }
    if ((daytimePrecProb?.snow ?: 0.0) > 0 || (nighttimePrecProb?.snow ?: 0.0) > 0) {
        daytimePrecProbItems.add(Pair(R.string.precipitation_snow, daytimePrecProb?.snow))
        nighttimePrecProbItems.add(Pair(R.string.precipitation_snow, nighttimePrecProb?.snow))
    }
    if ((daytimePrecProb?.ice ?: 0.0) > 0 || (nighttimePrecProb?.ice ?: 0.0) > 0) {
        daytimePrecProbItems.add(Pair(R.string.precipitation_ice, daytimePrecProb?.ice))
        nighttimePrecProbItems.add(Pair(R.string.precipitation_ice, nighttimePrecProb?.ice))
    }
    if ((daytimePrecProb?.thunderstorm ?: 0.0) > 0 || (nighttimePrecProb?.thunderstorm ?: 0.0) > 0) {
        daytimePrecProbItems.add(Pair(R.string.precipitation_thunderstorm, daytimePrecProb?.thunderstorm))
        nighttimePrecProbItems.add(Pair(R.string.precipitation_thunderstorm, nighttimePrecProb?.thunderstorm))
    }

    if (daytimePrecProbItems.isNotEmpty()) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.small_margin)),
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
                DaytimeLabel()
                daytimePrecProbItems.forEach {
                    DailyPrecipitationProbabilityDetail(it)
                }
            }
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .semantics { isTraversalGroup = true }
            ) {
                NighttimeLabelWithInfo()
                nighttimePrecProbItems.forEach {
                    DailyPrecipitationProbabilityDetail(it)
                }
            }
        }
    }
}

@Composable
internal fun DailyPrecipitationProbabilityDetail(
    item: Pair<Int, Double?>,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current

    DetailsItem(
        headlineText = stringResource(item.first),
        supportingText = item.second?.let { pp -> UnitUtils.formatPercent(context, pp) }
            ?: stringResource(R.string.null_data_text),
        modifier = modifier
            .padding(top = dimensionResource(R.dimen.normal_margin))
            .semantics(mergeDescendants = true) {}
            .clearAndSetSemantics {
                item.second?.let { pp ->
                    contentDescription = context.getString(item.first) +
                        context.getString(R.string.colon_separator) +
                        UnitUtils.formatPercent(context, pp)
                }
            }
    )
}

@Composable
private fun PrecipitationDurationSummary(
    daytimePrecDur: PrecipitationDuration?,
    nighttimePrecDur: PrecipitationDuration?,
) {
    val context = LocalContext.current
    val daytimePrecDurItems = mutableListOf<Pair<Int, Double?>>()
    val nighttimePrecDurItems = mutableListOf<Pair<Int, Double?>>()

    if ((daytimePrecDur?.rain ?: 0.0) > 0 || (nighttimePrecDur?.rain ?: 0.0) > 0) {
        daytimePrecDurItems.add(Pair(R.string.precipitation_rain, daytimePrecDur?.rain))
        nighttimePrecDurItems.add(Pair(R.string.precipitation_rain, nighttimePrecDur?.rain))
    }
    if ((daytimePrecDur?.snow ?: 0.0) > 0 || (nighttimePrecDur?.snow ?: 0.0) > 0) {
        daytimePrecDurItems.add(Pair(R.string.precipitation_snow, daytimePrecDur?.snow))
        nighttimePrecDurItems.add(Pair(R.string.precipitation_snow, nighttimePrecDur?.snow))
    }
    if ((daytimePrecDur?.ice ?: 0.0) > 0 || (nighttimePrecDur?.ice ?: 0.0) > 0) {
        daytimePrecDurItems.add(Pair(R.string.precipitation_ice, daytimePrecDur?.ice))
        nighttimePrecDurItems.add(Pair(R.string.precipitation_ice, nighttimePrecDur?.ice))
    }
    if ((daytimePrecDur?.thunderstorm ?: 0.0) > 0 || (nighttimePrecDur?.thunderstorm ?: 0.0) > 0) {
        daytimePrecDurItems.add(Pair(R.string.precipitation_thunderstorm, daytimePrecDur?.thunderstorm))
        nighttimePrecDurItems.add(Pair(R.string.precipitation_thunderstorm, nighttimePrecDur?.thunderstorm))
    }

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
            DaytimeLabel()
            Text(
                text = buildAnnotatedString {
                    daytimePrecDur?.total?.let {
                        append(
                            UnitUtils.formatUnitsDifferentFontSize(
                                formattedMeasure = DurationUnit.HOUR.formatMeasureShort(context, it),
                                fontSize = MaterialTheme.typography.headlineSmall.fontSize
                            )
                        )
                    } ?: run {
                        append(stringResource(R.string.null_data_text))
                    }
                },
                style = MaterialTheme.typography.displaySmall,
                modifier = Modifier
                    .clearAndSetSemantics {
                        daytimePrecDur?.total?.let {
                            contentDescription = DurationUnit.HOUR.formatContentDescription(context, it)
                        }
                    }
            )
            daytimePrecDurItems.forEach {
                DailyPrecipitationDurationDetail(it)
            }
        }
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .semantics { isTraversalGroup = true }
        ) {
            NighttimeLabelWithInfo()
            Text(
                text = buildAnnotatedString {
                    nighttimePrecDur?.total?.let {
                        append(
                            UnitUtils.formatUnitsDifferentFontSize(
                                formattedMeasure = DurationUnit.HOUR.formatMeasureShort(context, it),
                                fontSize = MaterialTheme.typography.headlineSmall.fontSize
                            )
                        )
                    } ?: run {
                        append(stringResource(R.string.null_data_text))
                    }
                },
                style = MaterialTheme.typography.displaySmall,
                modifier = Modifier
                    .clearAndSetSemantics {
                        nighttimePrecDur?.total?.let {
                            contentDescription = DurationUnit.HOUR.formatContentDescription(context, it)
                        }
                    }
            )
            nighttimePrecDurItems.forEach {
                DailyPrecipitationDurationDetail(it)
            }
        }
    }
}

@Composable
fun DailyPrecipitationDurationDetail(
    item: Pair<Int, Double?>,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val durationUnit = DurationUnit.HOUR
    DetailsItem(
        headlineText = stringResource(item.first),
        supportingText = item.second?.let { dur -> durationUnit.formatMeasure(context, dur) },
        modifier = modifier
            .padding(top = dimensionResource(R.dimen.normal_margin))
            .semantics(mergeDescendants = true) {}
            .clearAndSetSemantics {
                item.second?.let { dur ->
                    contentDescription = context.getString(item.first) +
                        context.getString(R.string.colon_separator) +
                        durationUnit.formatContentDescription(context, dur)
                }
            }
    )
}
