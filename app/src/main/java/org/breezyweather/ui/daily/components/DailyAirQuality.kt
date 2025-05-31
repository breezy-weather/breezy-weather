package org.breezyweather.ui.daily.components

import android.content.Context
import android.text.SpannableStringBuilder
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.unit.dp
import breezyweather.domain.location.model.Location
import breezyweather.domain.weather.model.AirQuality
import breezyweather.domain.weather.model.Hourly
import com.patrykandpatrick.vico.core.cartesian.CartesianDrawingContext
import com.patrykandpatrick.vico.core.cartesian.data.CartesianChartModelProducer
import com.patrykandpatrick.vico.core.cartesian.data.lineSeries
import com.patrykandpatrick.vico.core.cartesian.marker.CartesianMarker
import com.patrykandpatrick.vico.core.cartesian.marker.DefaultCartesianMarker
import com.patrykandpatrick.vico.core.common.Fill
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableMap
import org.breezyweather.R
import org.breezyweather.common.basic.models.options.appearance.ChartDisplay
import org.breezyweather.common.extensions.getFormattedTime
import org.breezyweather.common.extensions.is12Hour
import org.breezyweather.domain.weather.index.PollutantIndex
import org.breezyweather.domain.weather.model.getColor
import org.breezyweather.domain.weather.model.getDescription
import org.breezyweather.domain.weather.model.getIndex
import org.breezyweather.domain.weather.model.getName
import org.breezyweather.ui.common.charts.BreezyLineChart
import java.util.Date
import kotlin.math.max
import kotlin.math.roundToInt

@Composable
fun DailyAirQuality(
    location: Location,
    hourlyList: ImmutableList<Hourly>,
    dayAirQuality: AirQuality?,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(
            dimensionResource(R.dimen.normal_margin)
        )
    ) {
        if (dayAirQuality?.isValid == true) {
            item {
                AirQualitySummary(dayAirQuality)
            }
        }
        if (ChartDisplay.TAG_AIR_QUALITY.isValidForChart(hourlyList)) {
            item {
                AirQualityChart(
                    location,
                    hourlyList
                )
            }
        } else {
            item {
                Text(
                    text = "Not enough hourly data available to display a chart for this day",
                    fontStyle = FontStyle.Italic,
                    modifier = Modifier.padding(top = 15.dp)
                )
            }
        }
        if (dayAirQuality?.isValid == true) {
            item {
                Text(
                    text = dayAirQuality.getName(context) ?: "",
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier.padding(top = 10.dp)
                )
            }
            item {
                Text(
                    text = dayAirQuality.getDescription(context) ?: "",
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(top = 5.dp)
                )
            }
        }
    }
}

@Composable
private fun AirQualitySummary(
    dayAirQuality: AirQuality,
) {
    val context = LocalContext.current
    Column {
        // TODO: Check accessibility
        Text(
            text = "Air Quality Index",
            style = MaterialTheme.typography.labelSmall
        )
        Text(
            text = dayAirQuality.getIndex()?.toString() ?: "",
            style = MaterialTheme.typography.displaySmall,
            color = Color(dayAirQuality.getColor(context))
        )
    }
}

@Composable
private fun AirQualityChart(
    location: Location,
    hourlyList: ImmutableList<Hourly>,
) {
    val context = LocalContext.current
    val maxY = max(
        150, // TODO: Make this a const
        hourlyList.maxOfOrNull {
            it.airQuality?.getIndex() ?: 0
        } ?: 0
    )

    val modelProducer = remember { CartesianChartModelProducer() }

    LaunchedEffect(location) {
        modelProducer.runTransaction {
            lineSeries {
                series(
                    x = hourlyList.map {
                        it.date.time
                    },
                    y = hourlyList.map {
                        it.airQuality?.getIndex() ?: 0
                    }
                )
            }
        }
    }

    BreezyLineChart(
        location,
        modelProducer,
        maxY.toDouble(),
        { _, value, _ -> value.roundToInt().toString() },
        MarkerLabelFormatterAirQualityDecorator(hourlyList, location, context),
        (PollutantIndex.aqiThresholds.reversed() as List<Number>).zip(
            context.resources.getIntArray(PollutantIndex.colorsArrayId).reversed().map { Fill(it) }
        ).toMap().toImmutableMap()
    )
}

private class MarkerLabelFormatterAirQualityDecorator(
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

        return SpannableStringBuilder().append(
            startTime,
            aContext.getString(R.string.colon_separator),
            // TODO: A bit dirty, isn't there a better way to access the y value??
            hourlyList.firstOrNull { it.date.time == model.x.toLong() }?.airQuality?.getIndex()?.toString()
        )
    }
}
