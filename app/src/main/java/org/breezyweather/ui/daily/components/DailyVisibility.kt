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
import org.breezyweather.common.basic.models.options.unit.DistanceUnit
import org.breezyweather.common.extensions.getFormattedTime
import org.breezyweather.common.extensions.is12Hour
import org.breezyweather.domain.settings.SettingsManager
import org.breezyweather.ui.common.charts.BreezyLineChart
import org.breezyweather.ui.settings.preference.bottomInsetItem
import java.util.Date
import kotlin.math.max

@Composable
fun DailyVisibility(
    location: Location,
    hourlyList: ImmutableList<Hourly>,
    modifier: Modifier = Modifier,
) {
    val mappedValues = remember(hourlyList) {
        hourlyList
            .filter { it.visibility != null }
            .associate { it.date.time to it.visibility!! }
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
                VisibilitySummary(mappedValues)
            }
            item {
                VisibilityChart(location, mappedValues)
            }
        } else {
            item {
                UnavailableChart(mappedValues.size)
            }
        }
        // TODO: Short explanation
        // TODO: About visibility
        bottomInsetItem()
    }
}

@Composable
private fun VisibilitySummary(
    mappedValues: ImmutableMap<Long, Double>,
) {
    val context = LocalContext.current
    val minVisibility = mappedValues.values.min()
    val minVisibilityDescription = DistanceUnit.getVisibilityDescription(context, minVisibility)
    val maxVisibility = mappedValues.values.max()
    val maxVisibilityDescription = DistanceUnit.getVisibilityDescription(context, maxVisibility)
    val maxVisibilityFormatted =
        SettingsManager.getInstance(context).distanceUnit.getValueText(context, maxVisibility)

    Column(
        modifier = Modifier
            .fillMaxWidth()
    ) {
        // TODO: Check accessibility
        Text(
            text = if (minVisibility == maxVisibility) {
                maxVisibilityFormatted
            } else {
                SettingsManager
                    .getInstance(context)
                    .distanceUnit
                    .getValueTextWithoutUnit(minVisibility)
                    .let {
                        stringResource(
                            R.string.visibility_from_to_number,
                            it,
                            maxVisibilityFormatted
                        )
                    }
            },
            style = MaterialTheme.typography.displaySmall
        )
        if (!maxVisibilityDescription.isNullOrEmpty()) {
            Text(
                text = if (minVisibilityDescription == maxVisibilityDescription) {
                    maxVisibilityDescription
                } else {
                    stringResource(
                        R.string.visibility_from_to_description,
                        minVisibilityDescription!!,
                        maxVisibilityDescription
                    )
                },
                style = MaterialTheme.typography.labelMedium
            )
        }
    }
}

@Composable
private fun VisibilityChart(
    location: Location,
    mappedValues: ImmutableMap<Long, Double>,
) {
    val context = LocalContext.current
    val maxY = remember(mappedValues) {
        max(
            25000.0, // TODO: Make this a const
            mappedValues.values.max()
        )
    }

    val endAxisValueFormatter = CartesianValueFormatter { _, value, _ ->
        SettingsManager.getInstance(context).distanceUnit.getValueText(context, value)
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
        MarkerLabelFormatterVisibilityDecorator(mappedValues, location, context),
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
            mappedValues.getOrElse(value.toLong()) { null }?.let {
                SettingsManager.getInstance(context).distanceUnit.getValueTextWithoutUnit(it)
            } ?: "-"
        }
    )
}

private class MarkerLabelFormatterVisibilityDecorator(
    private val mappedValues: Map<Long, Double>,
    private val location: Location,
    private val aContext: Context,
) : DefaultCartesianMarker.ValueFormatter {

    override fun format(
        context: CartesianDrawingContext,
        targets: List<CartesianMarker.Target>,
    ): CharSequence {
        val model = targets.first()
        val startTime = Date(model.x.toLong())
            .getFormattedTime(location, aContext, aContext.is12Hour)
        val quantityFormatted = mappedValues.getOrElse(model.x.toLong()) { null }?.let {
            SettingsManager.getInstance(aContext).distanceUnit.getValueText(aContext, it)
        } ?: "-"

        return SpannableStringBuilder().append(
            startTime,
            aContext.getString(R.string.colon_separator),
            quantityFormatted
        )
    }
}
