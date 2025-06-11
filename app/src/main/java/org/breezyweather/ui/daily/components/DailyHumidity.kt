package org.breezyweather.ui.daily.components

import android.content.Context
import android.text.SpannableStringBuilder
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
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
import breezyweather.domain.location.model.Location
import breezyweather.domain.weather.model.Hourly
import com.patrykandpatrick.vico.core.cartesian.CartesianDrawingContext
import com.patrykandpatrick.vico.core.cartesian.data.CartesianChartModelProducer
import com.patrykandpatrick.vico.core.cartesian.data.CartesianValueFormatter
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
import org.breezyweather.common.extensions.currentLocale
import org.breezyweather.common.extensions.getFormattedTime
import org.breezyweather.common.extensions.is12Hour
import org.breezyweather.domain.settings.SettingsManager
import org.breezyweather.ui.common.charts.BreezyLineChart
import org.breezyweather.ui.settings.preference.bottomInsetItem
import java.text.NumberFormat
import java.util.Date
import kotlin.math.max

@Composable
fun DailyHumidity(
    location: Location,
    hourlyList: ImmutableList<Hourly>,
    modifier: Modifier = Modifier,
) {
    val mappedHumidityValues = remember(hourlyList) {
        hourlyList
            .filter { it.relativeHumidity != null }
            .associate { it.date.time to it.relativeHumidity!! }
            .toImmutableMap()
    }
    val mappedDewPointValues = remember(hourlyList) {
        hourlyList
            .filter { it.dewPoint != null }
            .associate { it.date.time to it.dewPoint!! }
            .toImmutableMap()
    }

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(
            dimensionResource(R.dimen.normal_margin)
        )
    ) {
        if (mappedHumidityValues.size >= ChartDisplay.CHART_MIN_COUNT) {
            item {
                HumidityChart(location, mappedHumidityValues)
            }
        } else {
            item {
                UnavailableChart(mappedHumidityValues.size)
            }
        }
        // TODO: Short explanation
        // Dew point chart
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
                text = stringResource(R.string.dew_point),
                style = MaterialTheme.typography.headlineSmall
            )
        }
        item {
            Spacer(modifier = Modifier.height(dimensionResource(R.dimen.normal_margin)))
        }
        if (mappedDewPointValues.size >= ChartDisplay.CHART_MIN_COUNT) {
            item {
                DewPointChart(location, mappedDewPointValues)
            }
        } else {
            item {
                UnavailableChart(mappedDewPointValues.size)
            }
        }
        // TODO: About humidity
        bottomInsetItem()
    }
}

@Composable
private fun HumidityChart(
    location: Location,
    mappedValues: ImmutableMap<Long, Double>,
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
                    x = mappedValues.keys,
                    y = mappedValues.values
                )
            }
        }
    }

    BreezyLineChart(
        location,
        modelProducer,
        maxY,
        endAxisValueFormatter,
        MarkerLabelFormatterHumidityDecorator(mappedValues, location, context),
        persistentListOf(
            persistentMapOf(
                100 to Fill(Color(56, 70, 114).toArgb()),
                97 to Fill(Color(56, 98, 157).toArgb()),
                93 to Fill(Color(56, 123, 173).toArgb()),
                90 to Fill(Color(56, 132, 173).toArgb()),
                87 to Fill(Color(56, 135, 173).toArgb()),
                83 to Fill(Color(56, 148, 173).toArgb()),
                80 to Fill(Color(56, 157, 173).toArgb()),
                75 to Fill(Color(56, 160, 173).toArgb()),
                70 to Fill(Color(56, 174, 173).toArgb()),
                60 to Fill(Color(56, 173, 121).toArgb()),
                50 to Fill(Color(105, 173, 56).toArgb()),
                40 to Fill(Color(173, 146, 56).toArgb()),
                30 to Fill(Color(173, 110, 56).toArgb()),
                0 to Fill(Color(173, 85, 56).toArgb())
            )
        ),
        topAxisValueFormatter = { _, value, _ ->
            mappedValues.getOrElse(value.toLong()) { null }?.let {
                NumberFormat.getPercentInstance(context.currentLocale).apply {
                    maximumFractionDigits = 0
                }.format(it.div(100.0))
            } ?: "-"
        }
    )
}

private class MarkerLabelFormatterHumidityDecorator(
    private val mappedValues: ImmutableMap<Long, Double>,
    private val location: Location,
    private val aContext: Context,
) : DefaultCartesianMarker.ValueFormatter {

    override fun format(
        context: CartesianDrawingContext,
        targets: List<CartesianMarker.Target>,
    ): CharSequence {
        val model = targets.first()
        val startTime = Date(model.x.toLong()).getFormattedTime(location, aContext, aContext.is12Hour)
        val quantityFormatted = mappedValues.getOrElse(model.x.toLong()) { null }?.let {
            NumberFormat.getPercentInstance(aContext.currentLocale).apply {
                maximumFractionDigits = 0
            }.format(it.div(100.0))
        } ?: "-"

        return SpannableStringBuilder().append(
            startTime,
            aContext.getString(R.string.colon_separator),
            quantityFormatted
        )
    }
}

@Composable
private fun DewPointChart(
    location: Location,
    mappedValues: ImmutableMap<Long, Double>,
) {
    val context = LocalContext.current
    val maxY = max(
        50.0, // TODO: Make this a const
        mappedValues.values.max()
    )

    val endAxisValueFormatter = CartesianValueFormatter { _, value, _ ->
        SettingsManager.getInstance(context).temperatureUnit.getValueText(context, value)
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
        maxY,
        endAxisValueFormatter,
        MarkerLabelFormatterDewPointDecorator(mappedValues, location, context),
        persistentListOf(
            persistentMapOf(
                // TODO: Duplicate of temperature colors
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
            mappedValues.getOrElse(value.toLong()) { null }?.let {
                SettingsManager.getInstance(context).temperatureUnit.getShortValueText(context, it)
            } ?: "-"
        }
    )
}

private class MarkerLabelFormatterDewPointDecorator(
    private val mappedValues: ImmutableMap<Long, Double>,
    private val location: Location,
    private val aContext: Context,
) : DefaultCartesianMarker.ValueFormatter {

    override fun format(
        context: CartesianDrawingContext,
        targets: List<CartesianMarker.Target>,
    ): CharSequence {
        val model = targets.first()
        val startTime = Date(model.x.toLong()).getFormattedTime(location, aContext, aContext.is12Hour)
        // TODO: A bit dirty, isn't there a better way to access the y value??
        val quantityFormatted = mappedValues.getOrElse(model.x.toLong()) { null }?.let {
            SettingsManager.getInstance(aContext).temperatureUnit.getValueText(aContext, it)
        } ?: "-"

        return SpannableStringBuilder().append(
            startTime,
            aContext.getString(R.string.colon_separator),
            quantityFormatted
        )
    }
}
