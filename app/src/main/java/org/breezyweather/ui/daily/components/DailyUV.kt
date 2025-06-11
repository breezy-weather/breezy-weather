package org.breezyweather.ui.daily.components

import android.content.Context
import android.text.SpannableStringBuilder
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
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.unit.dp
import breezyweather.domain.location.model.Location
import breezyweather.domain.weather.model.Hourly
import breezyweather.domain.weather.model.UV
import com.patrykandpatrick.vico.core.cartesian.CartesianDrawingContext
import com.patrykandpatrick.vico.core.cartesian.data.CartesianChartModelProducer
import com.patrykandpatrick.vico.core.cartesian.data.lineSeries
import com.patrykandpatrick.vico.core.cartesian.marker.CartesianMarker
import com.patrykandpatrick.vico.core.cartesian.marker.DefaultCartesianMarker
import com.patrykandpatrick.vico.core.common.Fill
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.persistentMapOf
import org.breezyweather.R
import org.breezyweather.common.basic.models.options.appearance.ChartDisplay.Companion.CHART_MIN_COUNT
import org.breezyweather.common.extensions.getFormattedTime
import org.breezyweather.common.extensions.is12Hour
import org.breezyweather.common.extensions.roundDecimals
import org.breezyweather.domain.weather.model.getLevel
import org.breezyweather.domain.weather.model.getUVColor
import org.breezyweather.ui.common.charts.BreezyLineChart
import org.breezyweather.ui.settings.preference.bottomInsetItem
import java.util.Date
import kotlin.math.max
import kotlin.math.roundToInt

@Composable
fun DailyUV(
    location: Location,
    hourlyList: ImmutableList<Hourly>,
    dayUV: UV?,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val hourlyNoMissingValuesSize = remember(hourlyList) {
        hourlyList
            .filter { it.uV?.isValid == true }
            .size
    }

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(
            dimensionResource(R.dimen.normal_margin)
        )
    ) {
        if (dayUV?.isValid == true) {
            item {
                UVSummary(dayUV)
            }
        }
        if (hourlyNoMissingValuesSize >= CHART_MIN_COUNT) {
            item {
                UVChart(location, hourlyList)
            }
        } else {
            item {
                UnavailableChart(hourlyNoMissingValuesSize)
            }
        }
        if (dayUV?.isValid == true) {
            item {
                Text(
                    text = dayUV.getLevel(context) ?: "",
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier.padding(top = 10.dp)
                )
                // TODO: Add recommandations
            }
        }
        bottomInsetItem()
    }
}

@Composable
private fun UVSummary(
    dayUV: UV,
) {
    val context = LocalContext.current
    Text(
        text = dayUV.index?.toString() ?: "",
        style = MaterialTheme.typography.displaySmall,
        color = Color(dayUV.getUVColor(context))
    )
}

@Composable
private fun UVChart(
    location: Location,
    hourlyList: ImmutableList<Hourly>,
) {
    val context = LocalContext.current
    val maxY = max(
        UV.UV_INDEX_EXCESSIVE,
        hourlyList.maxOfOrNull { it.uV?.index ?: 0.0 } ?: 0.0
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
                        it.uV?.index ?: 0
                    }
                )
            }
        }
    }

    BreezyLineChart(
        location,
        modelProducer,
        maxY,
        { _, value, _ -> value.roundToInt().toString() },
        MarkerLabelFormatterUVDecorator(hourlyList, location, context),
        persistentListOf(
            persistentMapOf(
                19 to Fill(Color(255, 255, 255).toArgb()),
                11 to Fill(colorResource(R.color.colorLevel_5).toArgb()),
                10 to Fill(colorResource(R.color.colorLevel_4).toArgb()),
                7 to Fill(colorResource(R.color.colorLevel_3).toArgb()),
                5 to Fill(colorResource(R.color.colorLevel_2).toArgb()),
                2 to Fill(colorResource(R.color.colorLevel_1).toArgb()),
                0 to Fill(Color(110, 110, 110).toArgb())
            )
        ),
        topAxisValueFormatter = { _, value, _ ->
            hourlyList.firstOrNull { it.date.time == value.toLong() }?.uV?.index?.roundToInt()?.toString() ?: "-"
        }
    )
}

private class MarkerLabelFormatterUVDecorator(
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
            // TODO: Use NumberFormatter to use the correct decimal separator (dot, comma, etc)
            hourlyList.firstOrNull { it.date.time == model.x.toLong() }?.uV?.index?.roundDecimals(1)?.toString()
        )
    }
}
