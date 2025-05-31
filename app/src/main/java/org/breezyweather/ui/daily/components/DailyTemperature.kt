package org.breezyweather.ui.daily.components

import android.content.Context
import android.text.SpannableStringBuilder
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import breezyweather.domain.location.model.Location
import breezyweather.domain.weather.model.Hourly
import breezyweather.domain.weather.model.Temperature
import com.patrykandpatrick.vico.core.cartesian.CartesianDrawingContext
import com.patrykandpatrick.vico.core.cartesian.data.CartesianChartModelProducer
import com.patrykandpatrick.vico.core.cartesian.data.CartesianValueFormatter
import com.patrykandpatrick.vico.core.cartesian.data.lineSeries
import com.patrykandpatrick.vico.core.cartesian.marker.CartesianMarker
import com.patrykandpatrick.vico.core.cartesian.marker.DefaultCartesianMarker
import com.patrykandpatrick.vico.core.common.Fill
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentMapOf
import org.breezyweather.R
import org.breezyweather.common.basic.models.options.appearance.ChartDisplay
import org.breezyweather.common.extensions.getFormattedTime
import org.breezyweather.common.extensions.is12Hour
import org.breezyweather.domain.settings.SettingsManager
import org.breezyweather.ui.common.charts.BreezyLineChart
import java.util.Date
import kotlin.math.max

@Composable
fun DailyTemperature(
    location: Location,
    hourlyList: ImmutableList<Hourly>,
    daytimeTemperature: Temperature?,
    nighttimeTemperature: Temperature?,
    modifier: Modifier = Modifier,
) {
    var showRealTemp = rememberSaveable { true }

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(
            dimensionResource(R.dimen.normal_margin)
        )
    ) {
        item {
            TemperatureSummary(
                daytimeTemperature,
                nighttimeTemperature,
                showRealTemp
            )
        }
        if (ChartDisplay.TAG_TEMPERATURE.isValidForChart(hourlyList)) {
            item {
                TemperatureChart(
                    location,
                    hourlyList,
                    showRealTemp
                )
            }
        } else {
            item {
                Text(
                    text = "Not enough hourly data available to display a chart for this day",
                    fontStyle = FontStyle.Italic,
                    modifier = Modifier.padding(top = 20.dp)
                )
            }
        }
        item {
            // TODO: Not working
            TemperatureSwitcher(
                { showRealTemp = it },
                showRealTemp
            )
        }
        // TODO: Short explanation
    }
}

@Composable
private fun TemperatureSwitcher(
    onRealTempSwitch: (Boolean) -> Unit,
    showRealTemp: Boolean,
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
        ) {
            Button(
                onClick = { onRealTempSwitch(true) },
                enabled = !showRealTemp,
                modifier = Modifier
                    .fillMaxWidth()
            ) {
                Text(
                    text = "Real",
                    textAlign = TextAlign.Center
                )
            }
        }
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
        ) {
            Button(
                onClick = { onRealTempSwitch(false) },
                enabled = showRealTemp,
                modifier = Modifier
                    .fillMaxWidth()
            ) {
                Text(
                    text = "Feels like",
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

@Composable
private fun TemperatureSummary(
    daytimeTemperature: Temperature?,
    nighttimeTemperature: Temperature?,
    showRealTemp: Boolean,
) {
    val context = LocalContext.current

    Row(
        modifier = Modifier
            .fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
        ) {
            (if (showRealTemp) daytimeTemperature?.temperature else daytimeTemperature?.feelsLikeTemperature)?.let {
                // TODO: Check accessibility
                Text(
                    text = "Daytime max.",
                    style = MaterialTheme.typography.labelSmall
                )
                Text(
                    text = SettingsManager
                        .getInstance(context)
                        .temperatureUnit
                        .getValueText(context, it),
                    style = MaterialTheme.typography.displaySmall
                )
            }
        }
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
        ) {
            (if (showRealTemp) nighttimeTemperature?.temperature else nighttimeTemperature?.feelsLikeTemperature)?.let {
                // TODO: Check accessibility
                Text(
                    text = "Overnight min.",
                    style = MaterialTheme.typography.labelSmall
                )
                Text(
                    text = SettingsManager
                        .getInstance(context)
                        .temperatureUnit
                        .getValueText(context, it),
                    style = MaterialTheme.typography.displaySmall
                )
            }
        }
    }
}

@Composable
private fun TemperatureChart(
    location: Location,
    hourlyList: ImmutableList<Hourly>,
    showRealTemp: Boolean,
) {
    val context = LocalContext.current
    val maxY = max(
        50.0, // TODO: Make this a const
        hourlyList.maxOfOrNull {
            if (showRealTemp) {
                it.temperature?.temperature
            } else {
                it.temperature?.feelsLikeTemperature
            } ?: 0.0
        } ?: 0.0
    )

    val endAxisValueFormatter = CartesianValueFormatter { _, value, _ ->
        SettingsManager
            .getInstance(context)
            .temperatureUnit
            .getValueText(context, value)
    }

    val modelProducer = remember { CartesianChartModelProducer() }

    LaunchedEffect(location) {
        modelProducer.runTransaction {
            lineSeries {
                series(
                    x = hourlyList.map {
                        it.date.time
                    },
                    y = hourlyList.map {
                        if (showRealTemp) {
                            it.temperature?.temperature
                        } else {
                            it.temperature?.feelsLikeTemperature
                        } ?: 0
                    }
                )
            }
        }
    }

    BreezyLineChart(
        location,
        modelProducer,
        maxY,
        endAxisValueFormatter,
        MarkerLabelFormatterTemperatureDecorator(hourlyList, location, context, showRealTemp),
        persistentMapOf(
            47.0 to Fill(Color(71, 14, 0).toArgb()),
            30.0 to Fill(Color(232, 83, 25).toArgb()),
            21.0 to Fill(Color(243, 183, 4).toArgb()),
            10.0 to Fill(Color(128, 147, 24).toArgb()),
            1.0 to Fill(Color(68, 125, 99).toArgb()),
            0.0 to Fill(Color(93, 133, 198).toArgb()),
            -4.0 to Fill(Color(100, 166, 189).toArgb()),
            -8.0 to Fill(Color(106, 191, 181).toArgb()),
            -15.0 to Fill(Color(157, 219, 217).toArgb()),
            -25.0 to Fill(Color(143, 89, 169).toArgb()),
            -40.0 to Fill(Color(162, 70, 145).toArgb()),
            -55.0 to Fill(Color(202, 172, 195).toArgb()),
            -70.0 to Fill(Color(115, 70, 105).toArgb())
        )
    )
}

private class MarkerLabelFormatterTemperatureDecorator(
    private val hourlyList: List<Hourly>,
    private val location: Location,
    private val aContext: Context,
    private val showRealTemp: Boolean,
) : DefaultCartesianMarker.ValueFormatter {

    override fun format(
        context: CartesianDrawingContext,
        targets: List<CartesianMarker.Target>,
    ): CharSequence {
        val model = targets.first()
        val startTime = Date(model.x.toLong()).getFormattedTime(location, aContext, aContext.is12Hour)
        val quantityFormatted = SettingsManager
            .getInstance(aContext)
            .temperatureUnit
            .getValueText(
                aContext,
                if (showRealTemp) {
                    // TODO: A bit dirty, isn't there a better way to access the y value??
                    hourlyList.firstOrNull { it.date.time == model.x.toLong() }?.temperature?.temperature
                } else {
                    hourlyList.firstOrNull { it.date.time == model.x.toLong() }?.temperature?.feelsLikeTemperature
                } ?: 0.0
            )

        return SpannableStringBuilder().append(
            startTime,
            aContext.getString(R.string.colon_separator),
            quantityFormatted
        )
    }
}
