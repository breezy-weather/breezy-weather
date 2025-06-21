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

import android.text.BidiFormatter
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Icon
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
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.isTraversalGroup
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import breezyweather.domain.location.model.Location
import breezyweather.domain.weather.model.Daily
import breezyweather.domain.weather.model.Hourly
import breezyweather.domain.weather.model.Wind
import com.patrykandpatrick.vico.core.cartesian.data.CartesianChartModelProducer
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
import org.breezyweather.common.basic.models.options.basic.Utils.formatDouble
import org.breezyweather.common.basic.models.options.unit.SpeedUnit
import org.breezyweather.common.extensions.getFormattedTime
import org.breezyweather.common.extensions.is12Hour
import org.breezyweather.common.extensions.toDate
import org.breezyweather.domain.settings.SettingsManager
import org.breezyweather.domain.weather.model.getDirection
import org.breezyweather.ui.common.charts.BreezyLineChart
import org.breezyweather.ui.common.widgets.Material3CardListItem
import org.breezyweather.ui.settings.preference.bottomInsetItem
import org.breezyweather.ui.theme.compose.DayNightTheme
import kotlin.math.max
import kotlin.math.roundToInt

@Composable
fun DailyWind(
    location: Location,
    hourlyList: ImmutableList<Hourly>,
    daily: Daily,
    modifier: Modifier = Modifier,
) {
    val mappedValues = remember(hourlyList) {
        hourlyList
            .filter { it.wind?.speed != null }
            .associate { it.date.time to it.wind!! }
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
            WindChart(location, mappedValues, daily)
        }
        item {
            Spacer(modifier = Modifier.height(dimensionResource(R.dimen.normal_margin)))
        }
        // TODO: Daily summary
        item {
            DailySectionHeader(stringResource(R.string.wind_speed_about))
        }
        item {
            DailyCardText(stringResource(R.string.wind_speed_about_description))
        }
        item {
            Spacer(modifier = Modifier.height(dimensionResource(R.dimen.normal_margin)))
        }
        item {
            DailySectionHeader(stringResource(R.string.wind_strength_scale))
        }
        item {
            WindScale()
        }
        bottomInsetItem()
    }
}

@Composable
private fun WindSummary(
    daytimeWind: Wind?,
    nighttimeWind: Wind?,
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
            daytimeWind?.let {
                WindItem(
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
            nighttimeWind?.let {
                WindItem(
                    { NighttimeWithInfo() },
                    it
                )
            }
        }
    }
}

@Composable
private fun WindItem(
    header: @Composable () -> Unit,
    wind: Wind,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val speedUnit = SettingsManager.getInstance(context).speedUnit

    Column(
        modifier = modifier.fillMaxWidth()
    ) {
        header()
        wind.speed?.let { speed ->
            Row {
                Text(
                    text = speedUnit.getValueText(context, speed),
                    style = MaterialTheme.typography.headlineSmall,
                    modifier = Modifier
                        .clearAndSetSemantics {
                            contentDescription = speedUnit.getValueVoice(context, speed)
                        }
                )
                wind.arrow?.let {
                    Text(
                        text = it,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        style = MaterialTheme.typography.headlineSmall,
                        modifier = Modifier
                            .clearAndSetSemantics {
                                contentDescription = wind.getDirection(context, short = false)!!
                            }
                    )
                }
            }
            wind.gusts?.let { gusts ->
                if (gusts > speed) {
                    Text(
                        text = stringResource(R.string.wind_gusts_short) +
                            stringResource(R.string.colon_separator) +
                            speedUnit.getValueText(context, gusts),
                        style = MaterialTheme.typography.labelLarge,
                        color = DayNightTheme.colors.captionColor,
                        modifier = Modifier
                            .clearAndSetSemantics {
                                contentDescription = context.getString(R.string.wind_gusts_short) +
                                    context.getString(R.string.colon_separator) +
                                    speedUnit.getValueVoice(context, gusts)
                            }
                    )
                } else {
                    Text(
                        text = "",
                        style = MaterialTheme.typography.labelLarge,
                        modifier = Modifier.clearAndSetSemantics {}
                    )
                }
            } ?: Text(
                text = "",
                style = MaterialTheme.typography.labelLarge,
                modifier = Modifier.clearAndSetSemantics {}
            )
        }
    }
}

@Composable
private fun WindChart(
    location: Location,
    mappedValues: ImmutableMap<Long, Wind>,
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
        mappedValues.getOrElse(it.x.toLong()) { null }?.let { wind ->
            WindItem(
                {
                    Text(
                        text = it.x.toLong().toDate().getFormattedTime(location, context, context.is12Hour),
                        style = MaterialTheme.typography.labelMedium
                    )
                },
                wind
            )
        }
    } ?: WindSummary(daily.day?.wind, daily.night?.wind)

    Spacer(modifier = Modifier.height(dimensionResource(R.dimen.normal_margin)))

    if (mappedValues.size >= ChartDisplay.CHART_MIN_COUNT) {
        val maxY = remember(mappedValues) {
            max(
                15.0, // TODO: Make this a const
                max(
                    mappedValues.values.maxOfOrNull { it.gusts ?: 0.0 } ?: 0.0,
                    mappedValues.values.maxOf { it.speed!! }
                ).plus(5.0).roundToInt().toDouble() // TODO: Make this a const
            )
        }

        val modelProducer = remember { CartesianChartModelProducer() }

        LaunchedEffect(location) {
            modelProducer.runTransaction {
                lineSeries {
                    series(
                        x = mappedValues.keys,
                        y = mappedValues.values.map { it.speed!! }
                    )
                    if (mappedValues.values.any { it.gusts != null }) {
                        series(
                            x = mappedValues.keys,
                            y = mappedValues.values.map {
                                it.gusts?.let { gusts ->
                                    if (gusts < it.speed!!) {
                                        it.speed!!
                                    } else {
                                        gusts
                                    }
                                } ?: it.speed!!
                            }
                        )
                    }
                }
            }
        }

        BreezyLineChart(
            location,
            modelProducer,
            daily.date,
            maxY,
            { _, value, _ -> SettingsManager.getInstance(context).speedUnit.getValueText(context, value) },
            persistentListOf(
                persistentMapOf(
                    104 to Fill(Color(128, 128, 128).toArgb()),
                    77 to Fill(Color(205, 202, 112).toArgb()),
                    51 to Fill(Color(219, 212, 135).toArgb()),
                    46 to Fill(Color(231, 215, 215).toArgb()),
                    36 to Fill(colorResource(R.color.windStrength_bf12).toArgb()),
                    30.5 to Fill(colorResource(R.color.windStrength_bf11).toArgb()),
                    26.4 to Fill(colorResource(R.color.windStrength_bf10).toArgb()),
                    24.475 to Fill(Color(109, 97, 163).toArgb()),
                    22.55 to Fill(colorResource(R.color.windStrength_bf9).toArgb()),
                    18.9 to Fill(colorResource(R.color.windStrength_bf8).toArgb()),
                    17.175 to Fill(Color(129, 58, 78).toArgb()),
                    15.45 to Fill(colorResource(R.color.windStrength_bf7).toArgb()),
                    13.85 to Fill(Color(159, 127, 58).toArgb()),
                    12.25 to Fill(colorResource(R.color.windStrength_bf6).toArgb()),
                    9.3 to Fill(colorResource(R.color.windStrength_bf5).toArgb()),
                    6.7 to Fill(colorResource(R.color.windStrength_bf4).toArgb()),
                    4.4 to Fill(colorResource(R.color.windStrength_bf3).toArgb()),
                    2.4 to Fill(colorResource(R.color.windStrength_bf2).toArgb()),
                    1.0 to Fill(colorResource(R.color.windStrength_bf1).toArgb()),
                    0.0 to Fill(colorResource(R.color.windStrength_bf0).toArgb())
                ),
                persistentMapOf(
                    104 to Fill(Color(128, 128, 128, 160).toArgb()),
                    77 to Fill(Color(205, 202, 112, 160).toArgb()),
                    51 to Fill(Color(219, 212, 135, 160).toArgb()),
                    46 to Fill(Color(231, 215, 215, 160).toArgb()),
                    36 to Fill(colorResource(R.color.windStrength_bf12).copy(alpha = 160f.div(255f)).toArgb()),
                    30.5 to Fill(colorResource(R.color.windStrength_bf11).copy(alpha = 160f.div(255f)).toArgb()),
                    26.4 to Fill(colorResource(R.color.windStrength_bf10).copy(alpha = 160f.div(255f)).toArgb()),
                    24.475 to Fill(Color(109, 97, 163, 160).toArgb()),
                    22.55 to Fill(colorResource(R.color.windStrength_bf9).copy(alpha = 160f.div(255f)).toArgb()),
                    18.9 to Fill(colorResource(R.color.windStrength_bf8).copy(alpha = 160f.div(255f)).toArgb()),
                    17.175 to Fill(Color(129, 58, 78, 160).toArgb()),
                    15.45 to Fill(colorResource(R.color.windStrength_bf7).copy(alpha = 160f.div(255f)).toArgb()),
                    13.85 to Fill(Color(159, 127, 58, 160).toArgb()),
                    12.25 to Fill(colorResource(R.color.windStrength_bf6).copy(alpha = 160f.div(255f)).toArgb()),
                    9.3 to Fill(colorResource(R.color.windStrength_bf5).copy(alpha = 160f.div(255f)).toArgb()),
                    6.7 to Fill(colorResource(R.color.windStrength_bf4).copy(alpha = 160f.div(255f)).toArgb()),
                    4.4 to Fill(colorResource(R.color.windStrength_bf3).copy(alpha = 160f.div(255f)).toArgb()),
                    2.4 to Fill(colorResource(R.color.windStrength_bf2).copy(alpha = 160f.div(255f)).toArgb()),
                    1.0 to Fill(colorResource(R.color.windStrength_bf1).copy(alpha = 160f.div(255f)).toArgb()),
                    0.0 to Fill(colorResource(R.color.windStrength_bf0).copy(alpha = 160f.div(255f)).toArgb())
                )
            ),
            topAxisValueFormatter = { _, value, _ ->
                mappedValues.getOrElse(value.toLong()) { null }?.arrow ?: "-"
            },
            trendHorizontalLines = buildMap {
                if (maxY > Wind.WIND_SPEED_7) {
                    put(Wind.WIND_SPEED_7, stringResource(R.string.wind_strength_7))
                }
                if (maxY < Wind.WIND_SPEED_7 + 5.0) { // TODO: Make this a const
                    put(Wind.WIND_SPEED_3, stringResource(R.string.wind_strength_3))
                }
            }.toImmutableMap(),
            markerVisibilityListener = markerVisibilityListener
        )
    } else {
        UnavailableChart(mappedValues.size)
    }
}

// TODO: Accessibility
@Composable
fun WindScale(
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val speedUnit = SettingsManager.getInstance(context).speedUnit.let {
        if (it == SpeedUnit.BF) SpeedUnit.MPS else it
    }

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
                    stringResource(R.string.unit_bf),
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f)
                )
                Text(
                    stringResource(R.string.wind_strength_scale_description),
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1.5f)
                )
                Text(
                    speedUnit.getName(context),
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.End,
                    modifier = Modifier.weight(1.5f)
                )
            }
            SpeedUnit.beaufortScaleThresholds.forEachIndexed { index, startingValue ->
                val startingValueFormatted = BidiFormatter
                    .getInstance()
                    .unicodeWrap(
                        formatDouble(
                            speedUnit.getValueWithoutUnit(startingValue),
                            1
                        )
                    )
                val endingValueFormatted = SpeedUnit.beaufortScaleThresholds.getOrElse(index + 1) { null }
                    ?.let {
                        " – ${
                            BidiFormatter
                                .getInstance()
                                .unicodeWrap(
                                    formatDouble(speedUnit.getValueWithoutUnit(it) - 0.1, 1)
                                )
                        }"
                    }
                    ?: "+"
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.little_margin)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = dimensionResource(R.dimen.little_margin))
                ) {
                    Row(
                        modifier = Modifier.weight(1f),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.little_margin))
                    ) {
                        Icon(
                            modifier = Modifier
                                .size(dimensionResource(R.dimen.material_icon_size)),
                            painter = painterResource(R.drawable.ic_circle),
                            contentDescription = null,
                            tint = Color(SpeedUnit.getBeaufortScaleColor(context, index))
                        )
                        Text(
                            text = index.toString()
                        )
                    }
                    Text(
                        text = SpeedUnit.getBeaufortScaleStrength(context, startingValue + 0.1)!!,
                        modifier = Modifier.weight(1.5f)
                    )
                    Text(
                        text = "$startingValueFormatted$endingValueFormatted",
                        textAlign = TextAlign.End,
                        modifier = Modifier.weight(1.5f)
                    )
                }
            }
        }
    }
}
