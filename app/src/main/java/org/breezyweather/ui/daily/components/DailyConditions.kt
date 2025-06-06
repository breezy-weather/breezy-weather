package org.breezyweather.ui.daily.components

import android.content.Context
import android.text.Spannable
import android.text.SpannableString
import android.text.SpannableStringBuilder
import android.text.style.ImageSpan
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
import androidx.compose.material3.Button
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import breezyweather.domain.location.model.Location
import breezyweather.domain.weather.model.Daily
import breezyweather.domain.weather.model.Hourly
import breezyweather.domain.weather.model.Normals
import breezyweather.domain.weather.model.Temperature
import com.patrykandpatrick.vico.core.cartesian.CartesianDrawingContext
import com.patrykandpatrick.vico.core.cartesian.data.CartesianChartModelProducer
import com.patrykandpatrick.vico.core.cartesian.data.CartesianValueFormatter
import com.patrykandpatrick.vico.core.cartesian.data.lineSeries
import com.patrykandpatrick.vico.core.cartesian.marker.CartesianMarker
import com.patrykandpatrick.vico.core.cartesian.marker.DefaultCartesianMarker
import com.patrykandpatrick.vico.core.common.Fill
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.persistentMapOf
import kotlinx.collections.immutable.toImmutableList
import org.breezyweather.R
import org.breezyweather.common.basic.models.options.appearance.ChartDisplay
import org.breezyweather.common.extensions.getFormattedTime
import org.breezyweather.common.extensions.is12Hour
import org.breezyweather.domain.settings.SettingsManager
import org.breezyweather.ui.common.charts.BreezyLineChart
import org.breezyweather.ui.theme.resource.ResourceHelper
import org.breezyweather.ui.theme.resource.ResourcesProviderFactory
import java.util.Date
import kotlin.math.max

@Composable
fun DailyConditions(
    location: Location,
    hourlyList: ImmutableList<Hourly>,
    daily: Daily,
    normals: Normals?,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val showRealTemp = rememberSaveable { mutableStateOf(true) }

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(
            dimensionResource(R.dimen.normal_margin)
        )
    ) {
        item {
            TemperatureSummary(
                daily.day?.temperature,
                daily.night?.temperature,
                showRealTemp.value
            )
        }
        if (ChartDisplay.TAG_CONDITIONS.isValidForChart(hourlyList)) {
            item {
                Spacer(modifier = Modifier.height(dimensionResource(R.dimen.normal_margin)))
            }
            item {
                TemperatureChart(
                    location,
                    hourlyList.filter {
                        // Skip missing values
                        if (showRealTemp.value) {
                            it.temperature?.temperature != null
                        } else {
                            it.temperature?.feelsLikeTemperature != null
                        }
                    }.toImmutableList(),
                    showRealTemp.value
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
                { showRealTemp.value = it },
                showRealTemp.value
            )
        }
        // TODO: Short explanation
        // Detailed feels like temperatures
        item {
            TemperatureDetails(daily.day?.temperature, daily.night?.temperature, normals)
        }
        // TODO: Make a better design for degree day
        if (daily.degreeDay?.isValid == true) {
            item {
                HorizontalDivider()
            }
            item {
                Spacer(modifier = Modifier.height(dimensionResource(R.dimen.normal_margin)))
            }
            daily.degreeDay?.let { degreeDay ->
                val temperatureUnit = SettingsManager.getInstance(context).temperatureUnit
                if ((degreeDay.heating ?: 0.0) > 0) {
                    item {
                        DailyItem(
                            headlineText = stringResource(R.string.temperature_degree_day_heating),
                            supportingText = temperatureUnit.getDegreeDayValueText(context, degreeDay.heating!!),
                            supportingContentDescription = temperatureUnit
                                .getDegreeDayValueVoice(context, degreeDay.heating!!),
                            icon = R.drawable.ic_mode_heat
                        )
                    }
                } else if ((degreeDay.cooling ?: 0.0) > 0) {
                    item {
                        DailyItem(
                            headlineText = stringResource(R.string.temperature_degree_day_cooling),
                            supportingText = temperatureUnit.getDegreeDayValueText(context, degreeDay.cooling!!),
                            supportingContentDescription = temperatureUnit
                                .getDegreeDayValueVoice(context, degreeDay.cooling!!),
                            icon = R.drawable.ic_mode_cool
                        )
                    }
                }
            }
        }
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
        horizontalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.little_margin)),
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
    val provider = ResourcesProviderFactory.newInstance
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
        SettingsManager.getInstance(context).temperatureUnit.getValueText(context, value)
    }

    val modelProducer = remember { CartesianChartModelProducer() }

    LaunchedEffect(location, showRealTemp) {
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
        persistentListOf(
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
        ),
        topAxisValueFormatter = { _, value, _ ->
            hourlyList.firstOrNull { it.date.time == value.toLong() }?.let { hourly ->
                hourly.weatherCode?.let {
                    val ss = SpannableString("abc")
                    val d = ResourceHelper.getWeatherIcon(provider, it, hourly.isDaylight)
                    d.setBounds(0, 0, 64, 64)
                    val span = ImageSpan(d, ImageSpan.ALIGN_BASELINE)
                    ss.setSpan(span, 0, 3, Spannable.SPAN_INCLUSIVE_EXCLUSIVE)
                    ss
                }
            } ?: "-"
        }
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

@Composable
private fun TemperatureDetails(
    daytimeTemperature: Temperature?,
    nighttimeTemperature: Temperature?,
    normals: Normals?,
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
        ) {
            Text(
                text = "Daytime max.",
                style = MaterialTheme.typography.labelSmall
            )
            DailyFeelsLikeTemperatureDetails(daytimeTemperature, normals?.daytimeTemperature)
        }
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
        ) {
            Text(
                text = "Overnight min.",
                style = MaterialTheme.typography.labelSmall
            )
            DailyFeelsLikeTemperatureDetails(nighttimeTemperature, normals?.nighttimeTemperature)
        }
    }
}

// TODO: Check accessibility
@Composable
fun DailyFeelsLikeTemperatureDetails(
    temperature: Temperature?,
    normalsTemperature: Double?,
) {
    val context = LocalContext.current
    val temperatureUnit = SettingsManager.getInstance(context).temperatureUnit
    val temperatureItems = buildList {
        temperature?.let { temp ->
            temp.realFeelTemperature?.let {
                add(Pair(R.string.temperature_real_feel, it))
            }
            temp.realFeelShaderTemperature?.let {
                add(Pair(R.string.temperature_real_feel_shade, it))
            }
            temp.apparentTemperature?.let {
                add(Pair(R.string.temperature_apparent, it))
            }
            temp.windChillTemperature?.let {
                add(Pair(R.string.temperature_wind_chill, it))
            }
            temp.wetBulbTemperature?.let {
                add(Pair(R.string.temperature_wet_bulb, it))
            }
        }
        normalsTemperature?.let {
            add(Pair(R.string.temperature_normal_short, it))
        }
    }
    temperatureItems.forEach { item ->
        DailyItem(
            headlineText = stringResource(item.first),
            supportingText = temperatureUnit.getValueText(context, item.second),
            supportingContentDescription = temperatureUnit.getValueVoice(context, item.second),
            modifier = Modifier.padding(top = dimensionResource(R.dimen.normal_margin))
        )
    }
}
