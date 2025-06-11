package org.breezyweather.ui.daily.components

import android.content.Context
import android.text.SpannableStringBuilder
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
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
import org.breezyweather.common.basic.models.options.unit.PressureUnit
import org.breezyweather.common.extensions.getFormattedTime
import org.breezyweather.common.extensions.is12Hour
import org.breezyweather.domain.settings.SettingsManager
import org.breezyweather.ui.common.charts.BreezyLineChart
import org.breezyweather.ui.settings.preference.bottomInsetItem
import java.util.Date
import kotlin.math.max
import kotlin.math.min

@Composable
fun DailyPressure(
    location: Location,
    hourlyList: ImmutableList<Hourly>,
    modifier: Modifier = Modifier,
) {
    val mappedValues = remember(hourlyList) {
        hourlyList
            .filter { it.pressure != null }
            .associate { it.date.time to it.pressure!! }
            .toImmutableMap()
    }

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(
            dimensionResource(R.dimen.normal_margin)
        )
    ) {
        if (mappedValues.size >= ChartDisplay.CHART_MIN_COUNT) {
            item {
                PressureChart(location, mappedValues)
            }
        } else {
            item {
                UnavailableChart(mappedValues.size)
            }
        }
        // TODO: Short explanation
        // TODO: About pressure
        bottomInsetItem()
    }
}

@Composable
private fun PressureChart(
    location: Location,
    mappedValues: ImmutableMap<Long, Double>,
) {
    val context = LocalContext.current
    val maxY = max(
        PressureUnit.NORMAL + 30.0, // TODO: Make this a const
        mappedValues.values.max()
    )
    val minY = min(
        PressureUnit.NORMAL - 30.0, // TODO: Make this a const
        mappedValues.values.min()
    )

    val endAxisValueFormatter = CartesianValueFormatter { _, value, _ ->
        SettingsManager.getInstance(context).pressureUnit.getValueText(context, value)
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
        MarkerLabelFormatterPressureDecorator(mappedValues, location, context),
        persistentListOf(
            persistentMapOf(
                1080 to Fill(Color(48, 8, 24).toArgb()),
                1046 to Fill(Color(111, 24, 64).toArgb()),
                1038 to Fill(Color(142, 47, 57).toArgb()),
                1030 to Fill(Color(159, 81, 44).toArgb()),
                1024 to Fill(Color(163, 116, 67).toArgb()),
                1019 to Fill(Color(167, 147, 107).toArgb()),
                1015.25 to Fill(Color(176, 174, 152).toArgb()),
                1013.25 to Fill(Color(182, 182, 182).toArgb()),
                1011.25 to Fill(Color(155, 183, 172).toArgb()),
                1007 to Fill(Color(103, 162, 155).toArgb()),
                1002 to Fill(Color(26, 140, 147).toArgb()),
                995 to Fill(Color(0, 117, 146).toArgb()),
                986 to Fill(Color(0, 90, 148).toArgb()),
                976 to Fill(Color(0, 52, 146).toArgb()),
                950 to Fill(Color(0, 32, 96).toArgb()),
                900 to Fill(Color(8, 16, 48).toArgb())
            )
        ),
        trendHorizontalLines = persistentMapOf(
            PressureUnit.NORMAL to stringResource(R.string.temperature_normal_short)
        ),
        minY = minY
    )
}

private class MarkerLabelFormatterPressureDecorator(
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
            SettingsManager.getInstance(aContext).pressureUnit.getValueText(aContext, it)
        } ?: "-"

        return SpannableStringBuilder().append(
            startTime,
            aContext.getString(R.string.colon_separator),
            quantityFormatted
        )
    }
}
