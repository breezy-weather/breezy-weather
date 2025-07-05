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

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
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
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import breezyweather.domain.location.model.Location
import breezyweather.domain.weather.model.AirQuality
import breezyweather.domain.weather.model.Daily
import breezyweather.domain.weather.model.Hourly
import com.patrykandpatrick.vico.core.cartesian.data.CartesianChartModelProducer
import com.patrykandpatrick.vico.core.cartesian.data.lineSeries
import com.patrykandpatrick.vico.core.cartesian.marker.CartesianMarker
import com.patrykandpatrick.vico.core.cartesian.marker.CartesianMarkerVisibilityListener
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.ImmutableMap
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.persistentMapOf
import kotlinx.collections.immutable.toImmutableList
import kotlinx.collections.immutable.toImmutableMap
import org.breezyweather.R
import org.breezyweather.common.basic.models.options.appearance.DetailScreen
import org.breezyweather.common.basic.models.options.basic.Utils
import org.breezyweather.common.extensions.getFormattedTime
import org.breezyweather.common.extensions.is12Hour
import org.breezyweather.common.extensions.toDate
import org.breezyweather.domain.weather.index.PollutantIndex
import org.breezyweather.domain.weather.model.getColor
import org.breezyweather.domain.weather.model.getConcentration
import org.breezyweather.domain.weather.model.getDescription
import org.breezyweather.domain.weather.model.getIndex
import org.breezyweather.domain.weather.model.getName
import org.breezyweather.ui.common.charts.BreezyLineChart
import org.breezyweather.ui.common.charts.SpecificVerticalAxisItemPlacer
import org.breezyweather.ui.common.widgets.Material3CardListItem
import org.breezyweather.ui.main.adapters.AqiAdapter.AqiItem
import org.breezyweather.ui.settings.preference.bottomInsetItem
import org.breezyweather.ui.theme.compose.DayNightTheme
import kotlin.math.max
import kotlin.math.roundToInt

@Composable
fun DetailsAirQuality(
    location: Location,
    hourlyList: ImmutableList<Hourly>,
    daily: Daily,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val mappedValues = remember(hourlyList) {
        hourlyList
            .filter { it.airQuality?.isIndexValid == true }
            .associate { it.date.time to it.airQuality!! }
            .toImmutableMap()
    }

    val detailedPollutants = remember(daily) {
        buildList {
            daily.airQuality?.let { airQuality ->
                // We use air quality index for the progress bar instead of concentration for more realistic bar
                listOf(PollutantIndex.PM25, PollutantIndex.PM10, PollutantIndex.O3, PollutantIndex.NO2)
                    .forEach { pollutantIndex ->
                        airQuality.getConcentration(pollutantIndex)?.let {
                            add(
                                AqiItem(
                                    pollutantIndex,
                                    airQuality.getColor(context, pollutantIndex),
                                    airQuality.getIndex(pollutantIndex)!!.toFloat(),
                                    PollutantIndex.indexExcessivePollution.toFloat(),
                                    context.getString(pollutantIndex.shortName),
                                    PollutantIndex.getUnit(pollutantIndex).getValueText(context, it),
                                    context.getString(pollutantIndex.voicedName) +
                                        context.getString(R.string.colon_separator) +
                                        PollutantIndex.getUnit(pollutantIndex).getValueVoice(context, it),
                                    false
                                )
                            )
                        }
                    }
                listOf(PollutantIndex.SO2, PollutantIndex.CO)
                    .forEach { pollutantIndex ->
                        (airQuality.getConcentration(pollutantIndex) ?: 0.0).let {
                            if (it > 0) {
                                add(
                                    AqiItem(
                                        pollutantIndex,
                                        airQuality.getColor(context, pollutantIndex),
                                        airQuality.getIndex(pollutantIndex)!!.toFloat(),
                                        PollutantIndex.indexExcessivePollution.toFloat(),
                                        context.getString(pollutantIndex.shortName),
                                        PollutantIndex.getUnit(pollutantIndex).getValueText(context, it),
                                        context.getString(pollutantIndex.voicedName) +
                                            context.getString(R.string.colon_separator) +
                                            PollutantIndex.getUnit(pollutantIndex).getValueVoice(context, it),
                                        false
                                    )
                                )
                            }
                        }
                    }
            }
        }.toImmutableList()
    }
    val primaryPollutant = remember(detailedPollutants) {
        detailedPollutants.maxByOrNull { it.progress }
    }

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(
            horizontal = dimensionResource(R.dimen.normal_margin),
            vertical = dimensionResource(R.dimen.little_margin)
        )
    ) {
        item {
            AirQualityChart(location, mappedValues, daily)
        }
        if (daily.airQuality?.isValid == true) {
            item {
                Spacer(modifier = Modifier.height(dimensionResource(R.dimen.normal_margin)))
            }
            item {
                DetailsSectionHeader(stringResource(R.string.air_quality_health_information))
            }
            item {
                DetailsCardText(daily.airQuality!!.getDescription(context) ?: "")
            }
            primaryPollutant?.let {
                item {
                    Spacer(modifier = Modifier.height(dimensionResource(R.dimen.normal_margin)))
                }
                item {
                    DetailsSectionHeader(
                        stringResource(R.string.air_quality_pollutant_primary),
                        it.pollutantType.getFullName(context)
                    )
                }
                item {
                    DetailsCardText(stringResource(it.pollutantType.sources))
                }
            }
            item {
                Spacer(modifier = Modifier.height(dimensionResource(R.dimen.normal_margin)))
            }
            item {
                DetailsSectionHeader(stringResource(R.string.air_quality_pollutant_details))
            }
            item {
                AirQualityDetails(detailedPollutants)
            }
        }
        item {
            Spacer(modifier = Modifier.height(dimensionResource(R.dimen.normal_margin)))
        }
        item {
            DetailsSectionHeader(stringResource(R.string.air_quality_index_about))
        }
        item {
            DetailsCardText(
                stringResource(R.string.air_quality_index_about_description_1),
                stringResource(R.string.air_quality_index_about_description_2)
            )
        }
        item {
            Spacer(modifier = Modifier.height(dimensionResource(R.dimen.normal_margin)))
        }
        item {
            DetailsSectionHeader(stringResource(R.string.air_quality_index_scale))
        }
        item {
            AirQualityScale()
        }
        bottomInsetItem()
    }
}

@Composable
private fun AirQualitySummary(
    dayAirQuality: AirQuality,
) {
    AirQualityItem(
        airQuality = dayAirQuality,
        header = {
            Text(
                text = stringResource(R.string.air_quality_average),
                style = MaterialTheme.typography.labelMedium
            )
        }
    )
}

@Composable
private fun AirQualityItem(
    airQuality: AirQuality,
    header: @Composable () -> Unit,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    Row(
        horizontalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.little_margin)),
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
            .fillMaxWidth()
    ) {
        Icon(
            modifier = Modifier
                .size(dimensionResource(R.dimen.material_icon_size)),
            painter = painterResource(R.drawable.ic_circle),
            contentDescription = null,
            tint = Color(airQuality.getColor(context))
        )
        Column {
            header()
            Text(
                text = buildAnnotatedString {
                    airQuality.getIndex()?.let {
                        append(Utils.formatInt(it))
                        append(" ")
                    }
                    airQuality.getName(context)?.let {
                        withStyle(
                            style = SpanStyle(
                                fontSize = MaterialTheme.typography.headlineSmall.fontSize,
                                color = DayNightTheme.colors.captionColor
                            )
                        ) {
                            append(it)
                        }
                    }
                },
                style = MaterialTheme.typography.displaySmall
            )
        }
    }
}

@Composable
private fun AirQualityChart(
    location: Location,
    mappedValues: ImmutableMap<Long, AirQuality>,
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
        mappedValues.getOrElse(it.x.toLong()) { null }?.let { airQuality ->
            AirQualityItem(
                header = {
                    Text(
                        text = it.x.toLong().toDate().getFormattedTime(location, context, context.is12Hour),
                        style = MaterialTheme.typography.labelMedium
                    )
                },
                airQuality = airQuality
            )
        }
    } ?: daily.airQuality?.let { if (it.isValid) AirQualitySummary(it) }

    Spacer(modifier = Modifier.height(dimensionResource(R.dimen.normal_margin)))

    if (mappedValues.size >= DetailScreen.CHART_MIN_COUNT) {
        val maxY = remember(mappedValues) {
            max(
                PollutantIndex.aqiThresholds[4],
                mappedValues.values.maxOf { it.getIndex()!! }
            )
        }

        val modelProducer = remember { CartesianChartModelProducer() }

        LaunchedEffect(location) {
            modelProducer.runTransaction {
                lineSeries {
                    series(
                        x = mappedValues.keys,
                        y = mappedValues.values.map { it.getIndex()!! }
                    )
                }
            }
        }

        BreezyLineChart(
            location,
            modelProducer,
            daily.date,
            maxY.toDouble(),
            { _, value, _ -> value.roundToInt().toString() },
            persistentListOf(
                (PollutantIndex.aqiThresholds.reversed().map { it.toFloat() }).zip(
                    context.resources.getIntArray(PollutantIndex.colorsArrayId).reversed().map { Color(it) }
                ).toMap().toImmutableMap()
            ),
            trendHorizontalLines = persistentMapOf(
                PollutantIndex.indexHighPollution.toDouble() to
                    context.resources.getStringArray(R.array.air_quality_levels)[3]
            ),
            topAxisValueFormatter = { _, value, _ ->
                mappedValues.getOrElse(value.toLong()) { null }?.getIndex()?.toString() ?: "-"
            },
            endAxisItemPlacer = remember {
                SpecificVerticalAxisItemPlacer(
                    PollutantIndex
                        .aqiThresholds
                        .map { it.toDouble() }
                        .toMutableList()
                        .apply {
                            if (maxY > last()) {
                                add(maxY.toDouble())
                            }
                        }
                )
            },
            markerVisibilityListener = markerVisibilityListener
        )
    } else {
        UnavailableChart(mappedValues.size)
    }
}

@Composable
fun AirQualityDetails(
    detailedPollutants: ImmutableList<AqiItem>,
    modifier: Modifier = Modifier,
) {
    Material3CardListItem(
        modifier = modifier,
        withPadding = false
    ) {
        FlowRow(
            maxItemsInEachRow = 2,
            modifier = Modifier
                .padding(dimensionResource(R.dimen.little_margin))
        ) {
            detailedPollutants.forEach {
                DetailsAirQualityItem(
                    it,
                    Modifier
                        .fillMaxWidth(0.5f)
                        .padding(dimensionResource(R.dimen.little_margin))
                )
            }
        }
    }
}

@Composable
fun DetailsAirQualityItem(
    aqiItem: AqiItem,
    modifier: Modifier = Modifier,
) {
    val color = Color(aqiItem.color)
    // Our maximum is max, while a value between 0.0 and 1.0 is expected
    val progress = (aqiItem.progress / aqiItem.max).coerceAtMost(1.0f)
    Column(
        modifier = modifier
            .semantics(mergeDescendants = true) {}
            .clearAndSetSemantics {
                contentDescription = aqiItem.talkBack
            }
    ) {
        Text(
            text = aqiItem.title,
            fontWeight = FontWeight.Bold
        )
        LinearProgressIndicator(
            modifier = Modifier
                .height(10.dp)
                // We don't use trackColor cause it leaves an empty space between color and trackColor with round shape
                .background(color = color.copy(alpha = 0.1f), shape = RoundedCornerShape(5.dp))
                .fillMaxWidth(),
            progress = { progress },
            color = color,
            trackColor = Color.Transparent, // Uses the background color from Modifier
            strokeCap = StrokeCap.Round,
            drawStopIndicator = {}
        )
        Text(
            modifier = Modifier.align(Alignment.End),
            text = aqiItem.content,
            style = MaterialTheme.typography.bodyMedium
        )
    }
}

// TODO: Accessibility
@Composable
fun AirQualityScale(
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current

    Material3CardListItem(
        modifier = modifier,
        withPadding = false
    ) {
        Column(
            modifier = Modifier.padding(
                horizontal = dimensionResource(R.dimen.normal_margin),
                vertical = dimensionResource(R.dimen.little_margin)
            )
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.little_margin)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    stringResource(R.string.air_quality_index_description),
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(2f)
                )
                Text(
                    stringResource(R.string.air_quality_index_harmless_exposure),
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1.5f)
                )
                Text(
                    stringResource(R.string.air_quality_index_short),
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.End,
                    modifier = Modifier.weight(1.5f)
                )
            }
            PollutantIndex.aqiThresholds.forEachIndexed { index, startingValue ->
                val endingValue = PollutantIndex.aqiThresholds.getOrElse(index + 1) { null }
                    ?.let { " – ${it - 1}" }
                    ?: "+"
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.little_margin)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = dimensionResource(R.dimen.little_margin))
                ) {
                    Row(
                        modifier = Modifier.weight(2f),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.little_margin))
                    ) {
                        Icon(
                            modifier = Modifier
                                .size(dimensionResource(R.dimen.material_icon_size)),
                            painter = painterResource(R.drawable.ic_circle),
                            contentDescription = null,
                            tint = Color(PollutantIndex.getAqiToColor(context, startingValue))
                        )
                        Text(
                            text = PollutantIndex.getAqiToName(context, startingValue)!!
                        )
                    }
                    Text(
                        text = PollutantIndex.getAqiToHarmlessExposure(context, startingValue)!!,
                        modifier = Modifier.weight(1.5f)
                    )
                    Text(
                        text = "$startingValue$endingValue",
                        textAlign = TextAlign.End,
                        modifier = Modifier.weight(1.5f)
                    )
                }
            }
        }
    }
}
