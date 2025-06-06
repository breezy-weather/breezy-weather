package org.breezyweather.ui.daily.components

import android.content.Context
import android.text.SpannableStringBuilder
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
import androidx.compose.material3.HorizontalDivider
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
import androidx.compose.ui.text.font.FontStyle
import breezyweather.domain.location.model.Location
import breezyweather.domain.weather.model.Daily
import breezyweather.domain.weather.model.Hourly
import breezyweather.domain.weather.model.Precipitation
import breezyweather.domain.weather.model.PrecipitationDuration
import breezyweather.domain.weather.model.PrecipitationProbability
import com.patrykandpatrick.vico.core.cartesian.CartesianDrawingContext
import com.patrykandpatrick.vico.core.cartesian.data.CartesianChartModelProducer
import com.patrykandpatrick.vico.core.cartesian.data.CartesianValueFormatter
import com.patrykandpatrick.vico.core.cartesian.data.columnSeries
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
import org.breezyweather.common.basic.models.options.unit.DurationUnit
import org.breezyweather.common.extensions.currentLocale
import org.breezyweather.common.extensions.getFormattedTime
import org.breezyweather.common.extensions.is12Hour
import org.breezyweather.domain.settings.SettingsManager
import org.breezyweather.ui.common.charts.BreezyBarChart
import org.breezyweather.ui.common.charts.BreezyLineChart
import java.text.NumberFormat
import java.util.Date
import kotlin.math.max

@Composable
fun DailyPrecipitation(
    location: Location,
    hourlyList: ImmutableList<Hourly>,
    daily: Daily,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val hourlyListWithProbability = hourlyList.filter { it.precipitationProbability?.total != null }.toImmutableList()

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(
            dimensionResource(R.dimen.normal_margin)
        )
    ) {
        item {
            PrecipitationSummary(
                daily.day?.precipitation,
                daily.night?.precipitation
            )
        }
        if (ChartDisplay.TAG_PRECIPITATION.isValidForChart(hourlyList)) {
            item {
                PrecipitationChart(
                    location,
                    hourlyList
                )
            }
        } else {
            item {
                Spacer(modifier = Modifier.height(dimensionResource(R.dimen.normal_margin)))
            }
            item {
                Text(
                    text = "Not enough hourly data available to display a chart for this day",
                    fontStyle = FontStyle.Italic
                )
            }
        }
        item {
            PrecipitationDetails(daily.day?.precipitation, daily.night?.precipitation)
        }
        item {
            Spacer(modifier = Modifier.height(dimensionResource(R.dimen.normal_margin)))
        }
        item {
            HorizontalDivider()
        }
        item {
            Spacer(modifier = Modifier.height(dimensionResource(R.dimen.normal_margin)))
        }
        item {
            Text(
                text = stringResource(R.string.precipitation_probability),
                style = MaterialTheme.typography.headlineSmall
            )
        }
        item {
            Spacer(modifier = Modifier.height(dimensionResource(R.dimen.little_margin)))
        }
        item {
            PrecipitationProbabilitySummary(
                daily.day?.precipitationProbability,
                daily.night?.precipitationProbability
            )
        }
        if (hourlyListWithProbability.size >= 4) {
            item {
                PrecipitationProbabilityChart(
                    location,
                    hourlyListWithProbability
                )
            }
        } else {
            item {
                Spacer(modifier = Modifier.height(dimensionResource(R.dimen.normal_margin)))
            }
            item {
                Text(
                    text = "Not enough hourly data available to display a chart for this day",
                    fontStyle = FontStyle.Italic
                )
            }
        }
        // TODO: Short explanation
        item {
            PrecipitationProbabilityDetails(daily.day?.precipitationProbability, daily.night?.precipitationProbability)
        }
        item {
            Spacer(modifier = Modifier.height(dimensionResource(R.dimen.normal_margin)))
        }
        item {
            HorizontalDivider()
        }
        item {
            Spacer(modifier = Modifier.height(dimensionResource(R.dimen.normal_margin)))
        }
        // TODO: Hide if no data
        item {
            Text(
                text = stringResource(R.string.precipitation_duration),
                style = MaterialTheme.typography.headlineSmall
            )
        }
        item {
            Spacer(modifier = Modifier.height(dimensionResource(R.dimen.little_margin)))
        }
        item {
            PrecipitationDurationSummary(daily.day?.precipitationDuration, daily.night?.precipitationDuration)
        }
        // TODO: Make a better design for sunshine duration
        daily.sunshineDuration?.let { sunshineDuration ->
            item {
                Spacer(modifier = Modifier.height(dimensionResource(R.dimen.normal_margin)))
            }
            item {
                HorizontalDivider()
            }
            item {
                DailyItem(
                    headlineText = stringResource(R.string.sunshine_duration),
                    supportingText = DurationUnit.H.getValueText(context, sunshineDuration),
                    icon = R.drawable.ic_sunshine_duration
                )
            }
        }
    }
}

@Composable
private fun PrecipitationSummary(
    daytimePrecipitation: Precipitation?,
    nighttimePrecipitation: Precipitation?,
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
            daytimePrecipitation?.total?.let {
                // TODO: Check accessibility
                Text(
                    text = "Daytime total",
                    style = MaterialTheme.typography.labelSmall
                )
                Text(
                    text = SettingsManager
                        .getInstance(context)
                        .precipitationUnit
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
            nighttimePrecipitation?.total?.let {
                // TODO: Check accessibility
                Text(
                    text = "Overnight total",
                    style = MaterialTheme.typography.labelSmall
                )
                Text(
                    text = SettingsManager
                        .getInstance(context)
                        .precipitationUnit
                        .getValueText(context, it),
                    style = MaterialTheme.typography.displaySmall
                )
            }
        }
    }
}

@Composable
private fun PrecipitationChart(
    location: Location,
    hourlyList: ImmutableList<Hourly>,
) {
    val context = LocalContext.current
    val maxY = max(
        Precipitation.PRECIPITATION_HOURLY_HEAVY,
        hourlyList.maxOfOrNull {
            it.precipitation?.total ?: 0.0
        } ?: 0.0
    )

    val endAxisValueFormatter = CartesianValueFormatter { _, value, _ ->
        SettingsManager
            .getInstance(context)
            .precipitationUnit
            .getValueText(context, value)
    }

    val modelProducer = remember { CartesianChartModelProducer() }

    LaunchedEffect(location) {
        modelProducer.runTransaction {
            columnSeries {
                series(
                    x = hourlyList.map {
                        it.date.time
                    },
                    y = hourlyList.map {
                        it.precipitation?.total ?: 0.0
                    }
                )
            }
        }
    }

    BreezyBarChart(
        location,
        modelProducer,
        maxY,
        endAxisValueFormatter,
        MarkerLabelFormatterPrecipitationDecorator(hourlyList, location, context),
        Fill(Color(60, 116, 160).toArgb())
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
    )
}

private class MarkerLabelFormatterPrecipitationDecorator(
    private val hourlyList: List<Hourly>,
    private val location: Location,
    private val aContext: Context,
) : DefaultCartesianMarker.ValueFormatter {

    override fun format(
        context: CartesianDrawingContext,
        targets: List<CartesianMarker.Target>,
    ): CharSequence {
        val model = targets.first()
        val startTime = Date(model.x.toLong()).getFormattedTime(location, aContext, aContext.is12Hour)
        val quantityFormatted = SettingsManager
            .getInstance(aContext)
            .precipitationUnit
            .getValueText(
                aContext,
                // TODO: A bit dirty, isn't there a better way to access the y value??
                hourlyList.firstOrNull { it.date.time == model.x.toLong() }?.precipitation?.total ?: 0.0
            )

        return SpannableStringBuilder().append(
            startTime,
            aContext.getString(R.string.colon_separator),
            quantityFormatted
        )
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
        ) {
            Text(
                text = "Daytime total",
                style = MaterialTheme.typography.labelSmall
            )
            DailyPrecipitationDetails(daytimePrecipitation)
        }
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
        ) {
            Text(
                text = "Overnight total",
                style = MaterialTheme.typography.labelSmall
            )
            DailyPrecipitationDetails(nighttimePrecipitation)
        }
    }
}

// TODO: Check accessibility
@Composable
fun DailyPrecipitationDetails(
    precipitation: Precipitation?,
) {
    val context = LocalContext.current
    val precipitationUnit = SettingsManager.getInstance(context).precipitationUnit
    val precipitationItems = buildList {
        precipitation?.let { prec ->
            prec.rain?.let {
                add(Pair(R.string.precipitation_rain, it))
            }
            prec.snow?.let {
                add(Pair(R.string.precipitation_snow, it))
            }
            prec.ice?.let {
                add(Pair(R.string.precipitation_ice, it))
            }
            prec.thunderstorm?.let {
                add(Pair(R.string.precipitation_thunderstorm, it))
            }
        }
    }
    precipitationItems.forEach { item ->
        DailyItem(
            headlineText = stringResource(item.first),
            supportingText = precipitationUnit.getValueText(context, item.second),
            supportingContentDescription = precipitationUnit.getValueVoice(context, item.second),
            modifier = Modifier.padding(top = dimensionResource(R.dimen.normal_margin))
        )
    }
}

@Composable
private fun PrecipitationProbabilitySummary(
    daytimePrecipitationProbability: PrecipitationProbability?,
    nighttimePrecipitationProbability: PrecipitationProbability?,
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
            daytimePrecipitationProbability?.total?.let {
                // TODO: Check accessibility
                Text(
                    text = "Daytime probability",
                    style = MaterialTheme.typography.labelSmall
                )
                Text(
                    text = NumberFormat.getPercentInstance(context.currentLocale).apply {
                        maximumFractionDigits = 0
                    }.format(it.div(100.0)),
                    style = MaterialTheme.typography.displaySmall
                )
            }
        }
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
        ) {
            nighttimePrecipitationProbability?.total?.let {
                // TODO: Check accessibility
                Text(
                    text = "Overnight probability",
                    style = MaterialTheme.typography.labelSmall
                )
                Text(
                    text = NumberFormat.getPercentInstance(context.currentLocale).apply {
                        maximumFractionDigits = 0
                    }.format(it.div(100.0)),
                    style = MaterialTheme.typography.displaySmall
                )
            }
        }
    }
}

@Composable
private fun PrecipitationProbabilityChart(
    location: Location,
    hourlyList: ImmutableList<Hourly>,
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
                    x = hourlyList.map {
                        it.date.time
                    },
                    y = hourlyList.map {
                        it.precipitationProbability?.total ?: 0.0
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
        MarkerLabelFormatterPrecipitationProbabilityDecorator(hourlyList, location, context),
        // TODO
        persistentListOf(
            persistentMapOf(
                100 to Fill(Color(60, 116, 160).toArgb()),
                0 to Fill(Color(60, 116, 160).toArgb())
            )
        )
    )
}

private class MarkerLabelFormatterPrecipitationProbabilityDecorator(
    private val hourlyList: List<Hourly>,
    private val location: Location,
    private val aContext: Context,
) : DefaultCartesianMarker.ValueFormatter {

    override fun format(
        context: CartesianDrawingContext,
        targets: List<CartesianMarker.Target>,
    ): CharSequence {
        val model = targets.first()
        val startTime = Date(model.x.toLong()).getFormattedTime(location, aContext, aContext.is12Hour)
        val quantityFormatted =
            NumberFormat.getPercentInstance(aContext.currentLocale).apply {
                maximumFractionDigits = 0
            }.format(
                (hourlyList.firstOrNull { it.date.time == model.x.toLong() }?.precipitationProbability?.total ?: 0.0)
                    .div(100.0)
            )

        return SpannableStringBuilder().append(
            startTime,
            aContext.getString(R.string.colon_separator),
            quantityFormatted
        )
    }
}

@Composable
private fun PrecipitationProbabilityDetails(
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
        ) {
            Text(
                text = "Daytime probability",
                style = MaterialTheme.typography.labelSmall
            )
            DailyPrecipitationProbabilityDetails(daytimePrecipitationProbability)
        }
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
        ) {
            Text(
                text = "Overnight probability",
                style = MaterialTheme.typography.labelSmall
            )
            DailyPrecipitationProbabilityDetails(nighttimePrecipitationProbability)
        }
    }
}

// TODO: Check accessibility
@Composable
fun DailyPrecipitationProbabilityDetails(
    precipitationProbability: PrecipitationProbability?,
) {
    val context = LocalContext.current
    val percentUnit = NumberFormat.getPercentInstance(context.currentLocale).apply {
        maximumFractionDigits = 0
    }
    val precipitationProbabilityItems = buildList {
        precipitationProbability?.let { pp ->
            pp.rain?.let {
                add(Pair(R.string.precipitation_rain, it))
            }
            pp.snow?.let {
                add(Pair(R.string.precipitation_snow, it))
            }
            pp.ice?.let {
                add(Pair(R.string.precipitation_ice, it))
            }
            pp.thunderstorm?.let {
                add(Pair(R.string.precipitation_thunderstorm, it))
            }
        }
    }
    precipitationProbabilityItems.forEach { item ->
        DailyItem(
            headlineText = stringResource(item.first),
            supportingText = percentUnit.format(item.second.div(100.0)),
            modifier = Modifier.padding(top = dimensionResource(R.dimen.normal_margin))
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
        ) {
            daytimePrecipitationDuration?.let { precDur ->
                precDur.total?.let {
                    // TODO: Check accessibility
                    Text(
                        text = "Daytime duration",
                        style = MaterialTheme.typography.labelSmall
                    )
                    Text(
                        text = DurationUnit.H.getValueText(context, it),
                        style = MaterialTheme.typography.displaySmall
                    )
                    DailyPrecipitationDurationDetails(precDur)
                }
            }
        }
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
        ) {
            nighttimePrecipitationDuration?.let { precDur ->
                precDur.total?.let {
                    // TODO: Check accessibility
                    Text(
                        text = "Overnight duration",
                        style = MaterialTheme.typography.labelSmall
                    )
                    Text(
                        text = DurationUnit.H.getValueText(context, it),
                        style = MaterialTheme.typography.displaySmall
                    )
                    DailyPrecipitationDurationDetails(precDur)
                }
            }
        }
    }
}

// TODO: Check accessibility
@Composable
fun DailyPrecipitationDurationDetails(
    precipitationDuration: PrecipitationDuration?,
) {
    val context = LocalContext.current
    val durationUnit = DurationUnit.H
    val precipitationProbabilityItems = buildList {
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
    precipitationProbabilityItems.forEach { item ->
        DailyItem(
            headlineText = stringResource(item.first),
            supportingText = durationUnit.getValueText(context, item.second),
            supportingContentDescription = durationUnit.getValueVoice(context, item.second),
            modifier = Modifier.padding(top = dimensionResource(R.dimen.normal_margin))
        )
    }
}
