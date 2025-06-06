package org.breezyweather.ui.daily.components

import android.content.Context
import android.text.SpannableStringBuilder
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
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
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList
import kotlinx.collections.immutable.toImmutableMap
import org.breezyweather.R
import org.breezyweather.common.basic.models.options.appearance.ChartDisplay
import org.breezyweather.common.basic.models.options.unit.AirQualityCOUnit
import org.breezyweather.common.basic.models.options.unit.AirQualityUnit
import org.breezyweather.common.extensions.getFormattedTime
import org.breezyweather.common.extensions.is12Hour
import org.breezyweather.domain.weather.index.PollutantIndex
import org.breezyweather.domain.weather.model.getColor
import org.breezyweather.domain.weather.model.getDescription
import org.breezyweather.domain.weather.model.getIndex
import org.breezyweather.domain.weather.model.getName
import org.breezyweather.ui.common.charts.BreezyLineChart
import org.breezyweather.ui.common.widgets.Material3CardListItem
import org.breezyweather.ui.main.adapters.AqiAdapter.AqiItem
import org.breezyweather.ui.theme.compose.DayNightTheme
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
                    hourlyList.filter {
                        // Skip missing values
                        it.airQuality?.isIndexValid == true
                    }.toImmutableList()
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
            item {
                AirQualityDetails(dayAirQuality)
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
        persistentListOf(
            (PollutantIndex.aqiThresholds.reversed() as List<Number>).zip(
                context.resources.getIntArray(PollutantIndex.colorsArrayId).reversed().map { Fill(it) }
            ).toMap().toImmutableMap()
        ),
        topAxisValueFormatter = { _, value, _ ->
            hourlyList.firstOrNull { it.date.time == value.toLong() }?.airQuality?.getIndex()?.toString() ?: "-"
        }
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

@Composable
fun AirQualityDetails(
    airQuality: AirQuality,
) {
    val context = LocalContext.current
    var pollutantTypeState: PollutantIndex? by remember { mutableStateOf(null) }
    val mItemList = mutableListOf<AqiItem>()

    // We use air quality index for the progress bar instead of concentration for more realistic bar
    airQuality.pM25?.let {
        mItemList.add(
            AqiItem(
                PollutantIndex.PM25,
                airQuality.getColor(context, PollutantIndex.PM25),
                airQuality.getIndex(PollutantIndex.PM25)!!.toFloat(),
                PollutantIndex.indexExcessivePollution.toFloat(),
                context.getString(R.string.air_quality_pm25),
                AirQualityUnit.MUGPCUM.getValueText(context, it),
                context.getString(R.string.air_quality_pm25_voice) +
                    context.getString(R.string.comma_separator) +
                    AirQualityUnit.MUGPCUM.getValueVoice(context, it),
                false
            )
        )
    }
    airQuality.pM10?.let {
        mItemList.add(
            AqiItem(
                PollutantIndex.PM10,
                airQuality.getColor(context, PollutantIndex.PM10),
                airQuality.getIndex(PollutantIndex.PM10)!!.toFloat(),
                PollutantIndex.indexExcessivePollution.toFloat(),
                context.getString(R.string.air_quality_pm10),
                AirQualityUnit.MUGPCUM.getValueText(context, it),
                context.getString(R.string.air_quality_pm10_voice) +
                    context.getString(R.string.comma_separator) +
                    AirQualityUnit.MUGPCUM.getValueVoice(context, it),
                false
            )
        )
    }
    airQuality.o3?.let {
        mItemList.add(
            AqiItem(
                PollutantIndex.O3,
                airQuality.getColor(context, PollutantIndex.O3),
                airQuality.getIndex(PollutantIndex.O3)!!.toFloat(),
                PollutantIndex.indexExcessivePollution.toFloat(),
                context.getString(R.string.air_quality_o3),
                AirQualityUnit.MUGPCUM.getValueText(context, it),
                context.getString(R.string.air_quality_o3_voice) +
                    context.getString(R.string.comma_separator) +
                    AirQualityUnit.MUGPCUM.getValueVoice(context, it),
                false
            )
        )
    }
    airQuality.nO2?.let {
        mItemList.add(
            AqiItem(
                PollutantIndex.NO2,
                airQuality.getColor(context, PollutantIndex.NO2),
                airQuality.getIndex(PollutantIndex.NO2)!!.toFloat(),
                PollutantIndex.indexExcessivePollution.toFloat(),
                context.getString(R.string.air_quality_no2),
                AirQualityUnit.MUGPCUM.getValueText(context, it),
                context.getString(R.string.air_quality_no2_voice) +
                    context.getString(R.string.comma_separator) +
                    AirQualityUnit.MUGPCUM.getValueVoice(context, it),
                false
            )
        )
    }
    if ((airQuality.sO2 ?: 0.0) > 0) {
        mItemList.add(
            AqiItem(
                PollutantIndex.SO2,
                airQuality.getColor(context, PollutantIndex.SO2),
                airQuality.getIndex(PollutantIndex.SO2)!!.toFloat(),
                PollutantIndex.indexExcessivePollution.toFloat(),
                context.getString(R.string.air_quality_so2),
                AirQualityUnit.MUGPCUM.getValueText(context, airQuality.sO2!!),
                context.getString(R.string.air_quality_so2_voice) +
                    context.getString(R.string.comma_separator) +
                    AirQualityUnit.MUGPCUM.getValueVoice(context, airQuality.sO2!!),
                false
            )
        )
    }
    if ((airQuality.cO ?: 0.0) > 0) {
        mItemList.add(
            AqiItem(
                PollutantIndex.CO,
                airQuality.getColor(context, PollutantIndex.CO),
                airQuality.getIndex(PollutantIndex.CO)!!.toFloat(),
                PollutantIndex.indexExcessivePollution.toFloat(),
                context.getString(R.string.air_quality_co),
                AirQualityCOUnit.MGPCUM.getValueText(context, airQuality.cO!!),
                context.getString(R.string.air_quality_co_voice) +
                    context.getString(R.string.comma_separator) +
                    AirQualityCOUnit.MGPCUM.getValueVoice(context, airQuality.cO!!),
                false
            )
        )
    }

    Material3CardListItem {
        FlowRow(
            maxItemsInEachRow = 2,
            modifier = Modifier
                .padding(dimensionResource(R.dimen.little_margin))
        ) {
            mItemList.forEach {
                DailyAirQualityItem(
                    it,
                    Modifier
                        .fillMaxWidth(0.5f)
                        .padding(dimensionResource(R.dimen.little_margin))
                        .clickable { pollutantTypeState = it.pollutantType }
                )
            }
        }
    }

    if (pollutantTypeState != null) {
        AlertDialog(
            onDismissRequest = { pollutantTypeState = null },
            title = {
                Text(
                    text = stringResource(airQualityInfo[pollutantTypeState]!![0]),
                    color = MaterialTheme.colorScheme.onSurface,
                    style = MaterialTheme.typography.headlineSmall
                )
            },
            text = {
                Column(
                    modifier = Modifier
                        .verticalScroll(rememberScrollState())
                ) {
                    Text(
                        text = stringResource(airQualityInfo[pollutantTypeState]!![1]),
                        color = DayNightTheme.colors.bodyColor,
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Spacer(modifier = Modifier.height(dimensionResource(R.dimen.little_margin)))
                    Text(
                        text = stringResource(airQualityInfo[pollutantTypeState]!![2]),
                        color = DayNightTheme.colors.bodyColor,
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Spacer(modifier = Modifier.height(dimensionResource(R.dimen.little_margin)))
                    Text(
                        text = stringResource(airQualityInfo[pollutantTypeState]!![3]),
                        color = DayNightTheme.colors.bodyColor,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        pollutantTypeState = null
                    }
                ) {
                    Text(
                        text = stringResource(R.string.action_close),
                        color = MaterialTheme.colorScheme.primary,
                        style = MaterialTheme.typography.labelLarge
                    )
                }
            }
        )
    }
}

private val airQualityInfo = mapOf(
    PollutantIndex.PM10 to arrayOf(
        R.string.air_quality_pm_info_title,
        R.string.air_quality_pm_explanations_introduction,
        R.string.air_quality_pm_explanations_origin,
        R.string.air_quality_pm_explanations_consequences
    ),
    PollutantIndex.PM25 to arrayOf(
        R.string.air_quality_pm_info_title,
        R.string.air_quality_pm_explanations_introduction,
        R.string.air_quality_pm_explanations_origin,
        R.string.air_quality_pm_explanations_consequences
    ),
    PollutantIndex.O3 to arrayOf(
        R.string.air_quality_o3_info_title,
        R.string.air_quality_o3_info_introduction,
        R.string.air_quality_o3_info_origin,
        R.string.air_quality_o3_info_consequences
    ),
    PollutantIndex.NO2 to arrayOf(
        R.string.air_quality_no2_info_title,
        R.string.air_quality_no2_info_introduction,
        R.string.air_quality_no2_info_origin,
        R.string.air_quality_no2_info_consequences
    ),
    PollutantIndex.SO2 to arrayOf(
        R.string.air_quality_so2_info_title,
        R.string.air_quality_so2_info_introduction,
        R.string.air_quality_so2_info_origin,
        R.string.air_quality_so2_info_consequences
    ),
    PollutantIndex.CO to arrayOf(
        R.string.air_quality_co_info_title,
        R.string.air_quality_co_info_introduction,
        R.string.air_quality_co_info_origin,
        R.string.air_quality_co_info_consequences
    )
)

@Composable
fun DailyAirQualityItem(
    aqiItem: AqiItem,
    modifier: Modifier = Modifier,
) {
    val color = Color(aqiItem.color)
    // Our maximum is max, while a value between 0.0 and 1.0 is expected
    val progress = (aqiItem.progress / aqiItem.max).coerceAtMost(1.0f)
    Column(modifier = modifier) {
        Text(
            text = aqiItem.title,
            fontWeight = FontWeight.Bold
        )
        LinearProgressIndicator(
            modifier = Modifier
                .height(10.dp)
                // We don't use trackColor cause it leaves an empty space between color and trackColor with round shape
                .background(color = color.copy(alpha = 0.1f), shape = RoundedCornerShape(5.dp))
                .fillMaxWidth(),
            progress = { progress },
            color = color,
            trackColor = Color.Transparent, // Uses the background color from Modifier
            strokeCap = StrokeCap.Round,
            drawStopIndicator = {}
        )
        Text(
            modifier = Modifier.align(Alignment.End),
            text = aqiItem.content,
            style = MaterialTheme.typography.bodyMedium
        )
    }
}
