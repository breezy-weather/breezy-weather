package org.breezyweather.ui.daily.components

import android.content.Context
import android.text.SpannableStringBuilder
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.ui.text.font.FontStyle
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
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.persistentMapOf
import kotlinx.collections.immutable.toImmutableList
import org.breezyweather.R
import org.breezyweather.common.basic.models.options.appearance.ChartDisplay
import org.breezyweather.common.basic.models.options.unit.DistanceUnit
import org.breezyweather.common.extensions.getFormattedTime
import org.breezyweather.common.extensions.is12Hour
import org.breezyweather.domain.settings.SettingsManager
import org.breezyweather.ui.common.charts.BreezyLineChart
import java.util.Date
import kotlin.math.max

@Composable
fun DailyVisibility(
    location: Location,
    hourlyList: ImmutableList<Hourly>,
    modifier: Modifier = Modifier,
) {
    val hourlyNoMissingValues = hourlyList.filter {
        // Skip missing values
        it.visibility != null
    }.toImmutableList()

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(
            dimensionResource(R.dimen.normal_margin)
        )
    ) {
        if (ChartDisplay.TAG_VISIBILITY.isValidForChart(hourlyList)) {
            item {
                VisibilitySummary(hourlyNoMissingValues)
            }
            item {
                VisibilityChart(
                    location,
                    hourlyNoMissingValues
                )
            }
        } else {
            item {
                Text(
                    text = "No data available",
                    fontStyle = FontStyle.Italic
                )
            }
        }
        // TODO: Short explanation
        // TODO: About visibility
    }
}

@Composable
private fun VisibilitySummary(
    hourlyNoMissingValues: ImmutableList<Hourly>,
) {
    val context = LocalContext.current
    val minVisibility = hourlyNoMissingValues.minOf { it.visibility!! }
    val minVisibilityDescription = DistanceUnit.getVisibilityDescription(context, minVisibility)
    val maxVisibility = hourlyNoMissingValues.maxOf { it.visibility!! }
    val maxVisibilityDescription = DistanceUnit.getVisibilityDescription(context, maxVisibility)
    val maxVisibilityFormatted = SettingsManager
        .getInstance(context)
        .distanceUnit
        .getValueText(context, maxVisibility)

    Column(
        modifier = Modifier
            .fillMaxWidth()
    ) {
        // TODO: Check accessibility
        Text(
            text = if (minVisibility == maxVisibility) {
                maxVisibilityFormatted
            } else {
                SettingsManager.getInstance(context).distanceUnit.getValueWithoutUnit(minVisibility).let {
                    "$it to $maxVisibilityFormatted"
                }
            },
            style = MaterialTheme.typography.displaySmall
        )
        if (!maxVisibilityDescription.isNullOrEmpty()) {
            Text(
                text = if (minVisibilityDescription == maxVisibilityDescription) {
                    maxVisibilityDescription
                } else {
                    "$minVisibilityDescription to $maxVisibilityDescription"
                },
                style = MaterialTheme.typography.labelMedium
            )
        }
    }
}

@Composable
private fun VisibilityChart(
    location: Location,
    hourlyList: ImmutableList<Hourly>,
) {
    val context = LocalContext.current
    val maxY = max(
        25000.0, // TODO: Make this a const
        hourlyList.maxOfOrNull {
            it.visibility!!
        } ?: 0.0
    )

    val endAxisValueFormatter = CartesianValueFormatter { _, value, _ ->
        SettingsManager.getInstance(context).distanceUnit.getValueText(context, value)
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
                        it.visibility!!
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
        MarkerLabelFormatterVisibilityDecorator(hourlyList, location, context),
        persistentListOf(
            persistentMapOf(
                20000 to Fill(Color(119, 141, 120).toArgb()),
                15000 to Fill(Color(91, 167, 99).toArgb()),
                9000 to Fill(Color(90, 169, 90).toArgb()),
                8000 to Fill(Color(98, 122, 160).toArgb()),
                6000 to Fill(Color(98, 122, 160).toArgb()),
                5000 to Fill(Color(167, 91, 91).toArgb()),
                2200 to Fill(Color(167, 91, 91).toArgb()),
                1600 to Fill(Color(162, 97, 160).toArgb()),
                0 to Fill(Color(166, 93, 165).toArgb())
            )
        ),
        topAxisValueFormatter = { _, value, _ ->
            hourlyList.firstOrNull { it.date.time == value.toLong() }?.visibility?.let {
                SettingsManager.getInstance(context).distanceUnit.getValueWithoutUnit(it).toString()
            } ?: "-"
        }
    )
}

private class MarkerLabelFormatterVisibilityDecorator(
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
        // TODO: A bit dirty, isn't there a better way to access the y value??
        val quantityFormatted = hourlyList.firstOrNull { it.date.time == model.x.toLong() }?.visibility?.let {
            SettingsManager.getInstance(aContext).distanceUnit.getValueText(aContext, it)
        } ?: "-"

        return SpannableStringBuilder().append(
            startTime,
            aContext.getString(R.string.colon_separator),
            quantityFormatted
        )
    }
}
