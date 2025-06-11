package org.breezyweather.ui.daily.components

import android.content.Context
import android.text.SpannableStringBuilder
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import breezyweather.domain.location.model.Location
import breezyweather.domain.weather.model.Hourly
import breezyweather.domain.weather.model.Wind
import com.patrykandpatrick.vico.core.cartesian.CartesianDrawingContext
import com.patrykandpatrick.vico.core.cartesian.data.CartesianChartModelProducer
import com.patrykandpatrick.vico.core.cartesian.data.lineSeries
import com.patrykandpatrick.vico.core.cartesian.marker.CartesianMarker
import com.patrykandpatrick.vico.core.cartesian.marker.DefaultCartesianMarker
import com.patrykandpatrick.vico.core.common.Fill
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.ImmutableMap
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.persistentMapOf
import kotlinx.collections.immutable.toImmutableMap
import org.breezyweather.R
import org.breezyweather.common.basic.models.options.appearance.ChartDisplay
import org.breezyweather.common.extensions.getFormattedTime
import org.breezyweather.common.extensions.is12Hour
import org.breezyweather.domain.settings.SettingsManager
import org.breezyweather.ui.common.charts.BreezyLineChart
import org.breezyweather.ui.settings.preference.bottomInsetItem
import java.util.Date
import kotlin.math.max

@Composable
fun DailyWind(
    location: Location,
    hourlyList: ImmutableList<Hourly>,
    daytimeWind: Wind?,
    nighttimeWind: Wind?,
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
            dimensionResource(R.dimen.normal_margin)
        )
    ) {
        if (daytimeWind?.speed != null || nighttimeWind?.speed != null) {
            item {
                WindSummary(daytimeWind, nighttimeWind)
            }
        }
        if (mappedValues.size >= ChartDisplay.CHART_MIN_COUNT) {
            item {
                WindChart(location, mappedValues)
            }
        } else {
            item {
                UnavailableChart(mappedValues.size)
            }
        }
        // TODO: Daily summary
        bottomInsetItem()
    }
}

@Composable
private fun WindSummary(
    daytimeWind: Wind?,
    nighttimeWind: Wind?,
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
        ) {
            daytimeWind?.speed?.let { speed ->
                // TODO: Check accessibility
                Text(
                    text = stringResource(R.string.daytime),
                    style = MaterialTheme.typography.labelSmall
                )
                Row {
                    Text(
                        text = SettingsManager.getInstance(context).speedUnit.getValueText(context, speed),
                        style = MaterialTheme.typography.headlineSmall
                    )
                    Text(
                        text = daytimeWind.arrow!!,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        style = MaterialTheme.typography.headlineSmall
                    )
                }
                daytimeWind.gusts?.let { gusts ->
                    if (gusts > speed) {
                        Text(
                            text = stringResource(R.string.wind_gusts_short) +
                                stringResource(R.string.colon_separator) +
                                SettingsManager.getInstance(context).speedUnit.getValueText(context, gusts),
                            style = MaterialTheme.typography.labelLarge
                        )
                    }
                }
            }
        }
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
        ) {
            nighttimeWind?.speed?.let { speed ->
                // TODO: Check accessibility
                Text(
                    text = stringResource(R.string.nighttime),
                    style = MaterialTheme.typography.labelSmall
                )
                Row {
                    Text(
                        text = SettingsManager.getInstance(context).speedUnit.getValueText(context, speed),
                        style = MaterialTheme.typography.headlineSmall
                    )
                    Text(
                        text = nighttimeWind.arrow!!,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        style = MaterialTheme.typography.headlineSmall
                    )
                }
                nighttimeWind.gusts?.let { gusts ->
                    if (gusts > speed) {
                        Text(
                            text = stringResource(R.string.wind_gusts_short) +
                                stringResource(R.string.colon_separator) +
                                SettingsManager.getInstance(context).speedUnit.getValueText(context, gusts),
                            style = MaterialTheme.typography.labelLarge
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun WindChart(
    location: Location,
    mappedValues: ImmutableMap<Long, Wind>,
) {
    val context = LocalContext.current
    val maxY = max(
        30.0, // TODO: Make this a const
        max(
            mappedValues.values.maxOfOrNull { it.gusts ?: 0.0 } ?: 0.0,
            mappedValues.values.maxOf { it.speed!! }
        )
    )

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
        maxY,
        { _, value, _ -> SettingsManager.getInstance(context).speedUnit.getValueText(context, value) },
        MarkerLabelFormatterWindDecorator(mappedValues, location, context),
        persistentListOf(
            persistentMapOf(
                104 to Fill(Color(128, 128, 128).toArgb()),
                77 to Fill(Color(205, 202, 112).toArgb()),
                51 to Fill(Color(219, 212, 135).toArgb()),
                46 to Fill(Color(231, 215, 215).toArgb()),
                36 to Fill(Color(125, 68, 165).toArgb()),
                29 to Fill(Color(92, 144, 152).toArgb()),
                27 to Fill(Color(68, 105, 141).toArgb()),
                24 to Fill(Color(109, 97, 163).toArgb()),
                21 to Fill(Color(117, 74, 147).toArgb()),
                19 to Fill(Color(175, 80, 136).toArgb()),
                17 to Fill(Color(129, 58, 78).toArgb()),
                15 to Fill(Color(161, 100, 92).toArgb()),
                13 to Fill(Color(159, 127, 58).toArgb()),
                11 to Fill(Color(167, 157, 81).toArgb()),
                9 to Fill(Color(53, 159, 53).toArgb()),
                7 to Fill(Color(83, 165, 83).toArgb()),
                5 to Fill(Color(77, 141, 123).toArgb()),
                3 to Fill(Color(74, 148, 169).toArgb()),
                1 to Fill(Color(57, 97, 159).toArgb()),
                0 to Fill(Color(98, 113, 183).toArgb())
            ),
            persistentMapOf(
                104 to Fill(Color(128, 128, 128, 160).toArgb()),
                77 to Fill(Color(205, 202, 112, 160).toArgb()),
                51 to Fill(Color(219, 212, 135, 160).toArgb()),
                46 to Fill(Color(231, 215, 215, 160).toArgb()),
                36 to Fill(Color(125, 68, 165, 160).toArgb()),
                29 to Fill(Color(92, 144, 152, 160).toArgb()),
                27 to Fill(Color(68, 105, 141, 160).toArgb()),
                24 to Fill(Color(109, 97, 163, 160).toArgb()),
                21 to Fill(Color(117, 74, 147, 160).toArgb()),
                19 to Fill(Color(175, 80, 136, 160).toArgb()),
                17 to Fill(Color(129, 58, 78, 160).toArgb()),
                15 to Fill(Color(161, 100, 92, 160).toArgb()),
                13 to Fill(Color(159, 127, 58, 160).toArgb()),
                11 to Fill(Color(167, 157, 81, 160).toArgb()),
                9 to Fill(Color(53, 159, 53, 160).toArgb()),
                7 to Fill(Color(83, 165, 83, 160).toArgb()),
                5 to Fill(Color(77, 141, 123, 160).toArgb()),
                3 to Fill(Color(74, 148, 169, 160).toArgb()),
                1 to Fill(Color(57, 97, 159, 160).toArgb()),
                0 to Fill(Color(98, 113, 183, 160).toArgb())
            )
        ),
        topAxisValueFormatter = { _, value, _ ->
            mappedValues.getOrElse(value.toLong()) { null }?.arrow ?: "-"
        }
    )
}

private class MarkerLabelFormatterWindDecorator(
    private val mappedValues: ImmutableMap<Long, Wind>,
    private val location: Location,
    private val aContext: Context,
) : DefaultCartesianMarker.ValueFormatter {

    override fun format(
        context: CartesianDrawingContext,
        targets: List<CartesianMarker.Target>,
    ): CharSequence {
        val model = targets.first()
        val startTime = Date(model.x.toLong()).getFormattedTime(location, aContext, aContext.is12Hour)
        val quantityFormatted = mappedValues.getOrElse(model.x.toLong()) { null }?.let { wind ->
            wind.speed?.let { speed ->
                val str = StringBuilder()
                str.append(SettingsManager.getInstance(aContext).speedUnit.getValueText(aContext, speed))
                // TODO: Multiple lines doesn't work
                /*wind.gusts?.let { gusts ->
                    if (speed != gusts) {
                        str.append("\n")
                        str.append(aContext.getString(R.string.wind_gusts))
                        str.append(aContext.getString(R.string.colon_separator))
                        str.append(SettingsManager.getInstance(aContext).speedUnit.getValueText(aContext, gusts))
                    }
                }*/
                str
            }
        } ?: "-"

        return SpannableStringBuilder().append(
            startTime,
            aContext.getString(R.string.colon_separator),
            quantityFormatted
        )
    }
}
