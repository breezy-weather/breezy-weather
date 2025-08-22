/*
 * This file is part of Breezy Weather.
 *
 * Breezy Weather is free software: you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published by the
 * Free Software Foundation, version 3 of the License.
 *
 * Breezy Weather is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public
 * License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Breezy Weather. If not, see <https://www.gnu.org/licenses/>.
 */

package org.breezyweather.ui.details.components

import androidx.annotation.ColorInt
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.ButtonGroup
import androidx.compose.material3.ButtonGroupDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.ToggleButton
import androidx.compose.material3.ToggleButtonDefaults
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
import androidx.compose.ui.platform.LocalResources
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import breezyweather.domain.location.model.Location
import breezyweather.domain.weather.model.AirQuality
import breezyweather.domain.weather.model.Daily
import breezyweather.domain.weather.model.Hourly
import com.patrykandpatrick.vico.core.cartesian.data.CartesianChartModelProducer
import com.patrykandpatrick.vico.core.cartesian.data.lineSeries
import com.patrykandpatrick.vico.core.cartesian.marker.CartesianMarker
import com.patrykandpatrick.vico.core.cartesian.marker.CartesianMarkerVisibilityListener
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.ImmutableMap
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.persistentMapOf
import kotlinx.collections.immutable.toImmutableList
import kotlinx.collections.immutable.toImmutableMap
import org.breezyweather.R
import org.breezyweather.common.extensions.currentLocale
import org.breezyweather.common.extensions.formatMeasure
import org.breezyweather.common.extensions.getFormattedTime
import org.breezyweather.common.extensions.is12Hour
import org.breezyweather.common.extensions.roundUpToNearestMultiplier
import org.breezyweather.common.extensions.toDate
import org.breezyweather.common.options.appearance.DetailScreen
import org.breezyweather.common.utils.UnitUtils
import org.breezyweather.domain.weather.index.PollutantIndex
import org.breezyweather.domain.weather.model.getColor
import org.breezyweather.domain.weather.model.getConcentration
import org.breezyweather.domain.weather.model.getDescription
import org.breezyweather.domain.weather.model.getIndex
import org.breezyweather.domain.weather.model.getName
import org.breezyweather.ui.common.charts.BreezyLineChart
import org.breezyweather.ui.common.charts.SpecificVerticalAxisItemPlacer
import org.breezyweather.ui.common.charts.TimeTopAxisItemPlacer
import org.breezyweather.ui.common.widgets.Material3ExpressiveCardListItem
import org.breezyweather.unit.formatting.UnitWidth
import java.util.Date
import kotlin.math.max
import kotlin.math.roundToInt

class AqiItem(
    val pollutantType: PollutantIndex,
    @field:ColorInt val color: Int,
    val progress: Float,
    val max: Float,
    val title: String,
    val content: String,
    val talkBack: String,
)

@Composable
fun DetailsAirQuality(
    location: Location,
    supportedPollutants: ImmutableList<PollutantIndex>,
    selectedPollutant: PollutantIndex?,
    setSelectedPollutant: (PollutantIndex?) -> Unit,
    hourlyList: ImmutableList<Hourly>,
    daily: Daily,
    defaultValue: Pair<Date, AirQuality>?,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val mappedValues = remember(hourlyList, selectedPollutant) {
        hourlyList
            .filter { hourly ->
                selectedPollutant?.let {
                    hourly.airQuality?.getConcentration(it) != null
                } ?: (hourly.airQuality?.isIndexValid == true)
            }
            .associate { it.date.time to it.airQuality!! }
            .toImmutableMap()
    }
    var activeItem: Pair<Date, AirQuality>? by remember { mutableStateOf(null) }
    val markerVisibilityListener = remember {
        object : CartesianMarkerVisibilityListener {
            override fun onShown(marker: CartesianMarker, targets: List<CartesianMarker.Target>) {
                activeItem = targets.firstOrNull()?.let { target ->
                    mappedValues.getOrElse(target.x.toLong()) { null }?.let {
                        Pair(target.x.toLong().toDate(), it)
                    }
                }
            }

            override fun onUpdated(marker: CartesianMarker, targets: List<CartesianMarker.Target>) {
                onShown(marker, targets)
            }

            override fun onHidden(marker: CartesianMarker) {
                activeItem = null
            }
        }
    }
    val selectedAirQuality = remember(daily, defaultValue) {
        if (daily.airQuality?.isValid == true) {
            daily.airQuality
        } else {
            defaultValue?.second
        }
    }

    val detailedPollutants = remember(selectedAirQuality) {
        buildList {
            selectedAirQuality?.let { airQuality ->
                // We use air quality index for the progress bar instead of concentration for more realistic bar
                listOf(PollutantIndex.PM25, PollutantIndex.PM10, PollutantIndex.O3, PollutantIndex.NO2)
                    .forEach { pollutantIndex ->
                        airQuality.getConcentration(pollutantIndex)?.let {
                            add(
                                AqiItem(
                                    pollutantIndex,
                                    airQuality.getColor(context, pollutantIndex),
                                    airQuality.getIndex(pollutantIndex)!!.toFloat(),
                                    PollutantIndex.indexExcessivePollution.toFloat(),
                                    context.getString(pollutantIndex.shortName),
                                    PollutantIndex.getUnit(pollutantIndex).formatMeasure(context, it),
                                    context.getString(pollutantIndex.voicedName) +
                                        context.getString(R.string.colon_separator) +
                                        PollutantIndex.getUnit(pollutantIndex)
                                            .formatMeasure(context, it, unitWidth = UnitWidth.LONG)
                                )
                            )
                        }
                    }
                listOf(PollutantIndex.SO2, PollutantIndex.CO)
                    .forEach { pollutantIndex ->
                        (airQuality.getConcentration(pollutantIndex) ?: 0.0).let {
                            if (it > 0) {
                                add(
                                    AqiItem(
                                        pollutantIndex,
                                        airQuality.getColor(context, pollutantIndex),
                                        airQuality.getIndex(pollutantIndex)!!.toFloat(),
                                        PollutantIndex.indexExcessivePollution.toFloat(),
                                        context.getString(pollutantIndex.shortName),
                                        PollutantIndex.getUnit(pollutantIndex).formatMeasure(context, it),
                                        context.getString(pollutantIndex.voicedName) +
                                            context.getString(R.string.colon_separator) +
                                            PollutantIndex.getUnit(pollutantIndex)
                                                .formatMeasure(context, it, unitWidth = UnitWidth.LONG)
                                    )
                                )
                            }
                        }
                    }
            }
        }.toImmutableList()
    }
    val primaryPollutant = remember(detailedPollutants) {
        detailedPollutants.maxByOrNull { it.progress }
    }

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(
            horizontal = dimensionResource(R.dimen.normal_margin),
            vertical = dimensionResource(R.dimen.small_margin)
        )
    ) {
        item {
            AirQualityHeader(location, daily, selectedPollutant, activeItem, defaultValue)
        }
        item {
            Spacer(modifier = Modifier.height(dimensionResource(R.dimen.normal_margin)))
        }
        if (mappedValues.size >= DetailScreen.CHART_MIN_COUNT) {
            // Force recomposition when switching charts
            item(key = "chart-$selectedPollutant") {
                AirQualityChart(location, selectedPollutant, mappedValues, daily, markerVisibilityListener)
            }
        } else {
            item {
                UnavailableChart(mappedValues.size)
            }
        }
        if (supportedPollutants.isNotEmpty()) {
            item {
                Spacer(modifier = Modifier.height(dimensionResource(R.dimen.normal_margin)))
            }
            item {
                AirQualitySwitcher(
                    supportedPollutants,
                    setSelectedPollutant,
                    selectedPollutant
                )
            }
        }
        selectedPollutant?.let { pollutant ->
            item {
                Spacer(modifier = Modifier.height(dimensionResource(R.dimen.normal_margin)))
            }
            item {
                DetailsSectionHeader(
                    UnitUtils.formatPollutantName(
                        stringResource(pollutant.aboutPollutant, stringResource(pollutant.shortName))
                    )
                )
            }
            item {
                DetailsCardText(stringResource(pollutant.sources))
            }
        } ?: run {
            selectedAirQuality?.let { airQuality ->
                item {
                    Spacer(modifier = Modifier.height(dimensionResource(R.dimen.normal_margin)))
                }
                item {
                    DetailsSectionHeader(stringResource(R.string.air_quality_health_information))
                }
                item {
                    DetailsCardText(airQuality.getDescription(context) ?: "")
                }
                primaryPollutant?.let {
                    item {
                        Spacer(modifier = Modifier.height(dimensionResource(R.dimen.normal_margin)))
                    }
                    item {
                        DetailsSectionHeader(
                            buildAnnotatedString { append(stringResource(R.string.air_quality_pollutant_primary)) },
                            if (it.pollutantType != PollutantIndex.PM10 && it.pollutantType != PollutantIndex.PM25) {
                                UnitUtils.formatPollutantName(it.pollutantType.getFullName(context))
                            } else {
                                buildAnnotatedString {
                                    append(it.pollutantType.getFullName(context))
                                }
                            }
                        )
                    }
                    item {
                        DetailsCardText(stringResource(it.pollutantType.sources))
                    }
                }
                item {
                    Spacer(modifier = Modifier.height(dimensionResource(R.dimen.normal_margin)))
                }
                item {
                    DetailsSectionHeader(stringResource(R.string.air_quality_pollutant_details))
                }
                item {
                    AirQualityDetails(detailedPollutants)
                }
            }
        }
        item {
            Spacer(modifier = Modifier.height(dimensionResource(R.dimen.normal_margin)))
        }
        item {
            DetailsSectionHeader(stringResource(R.string.air_quality_index_about))
        }
        item {
            DetailsCardText(
                stringResource(R.string.air_quality_index_about_description_1),
                stringResource(R.string.air_quality_index_about_description_2)
            )
        }
        item {
            Spacer(modifier = Modifier.height(dimensionResource(R.dimen.normal_margin)))
        }
        item {
            selectedPollutant?.aboutIndex?.let {
                DetailsSectionHeader(
                    UnitUtils.formatPollutantName(
                        stringResource(it, stringResource(selectedPollutant.shortName))
                    )
                )
            } ?: DetailsSectionHeader(
                stringResource(R.string.air_quality_index_scale)
            )
        }
        item {
            AirQualityScale(selectedPollutant)
        }
        bottomDetailsInset()
    }
}

@Composable
private fun AirQualityHeader(
    location: Location,
    daily: Daily,
    selectedPollutant: PollutantIndex?,
    activeItem: Pair<Date, AirQuality>?,
    defaultValue: Pair<Date, AirQuality>?,
) {
    val context = LocalContext.current

    if (activeItem != null) {
        AirQualityItem(
            header = activeItem.first.getFormattedTime(location, context, context.is12Hour),
            selectedPollutant = selectedPollutant,
            airQuality = activeItem.second
        )
    } else if (daily.airQuality?.isValid == true) {
        AirQualityItem(
            header = stringResource(R.string.air_quality_average),
            selectedPollutant = selectedPollutant,
            airQuality = daily.airQuality
        )
    } else {
        AirQualityItem(
            header = defaultValue?.first?.getFormattedTime(location, context, context.is12Hour),
            selectedPollutant = selectedPollutant,
            airQuality = defaultValue?.second
        )
    }
}

@Composable
private fun AirQualityItem(
    header: String?,
    selectedPollutant: PollutantIndex?,
    airQuality: AirQuality?,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    Row(
        horizontalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.small_margin)),
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
            .fillMaxWidth()
    ) {
        Icon(
            modifier = Modifier
                .size(dimensionResource(R.dimen.material_icon_size)),
            painter = painterResource(R.drawable.ic_circle),
            contentDescription = null,
            tint = airQuality?.getColor(context, selectedPollutant)?.let { Color(it) } ?: Color.Transparent
        )
        Column {
            TextFixedHeight(
                text = header ?: "",
                style = MaterialTheme.typography.labelMedium
            )
            TextFixedHeight(
                text = buildAnnotatedString {
                    if (selectedPollutant == null) {
                        airQuality?.getIndex()?.let {
                            append(UnitUtils.formatInt(context, it))
                            append(" ")
                        }
                    } else {
                        airQuality?.getConcentration(selectedPollutant)?.let {
                            append(
                                UnitUtils.formatUnitsDifferentFontSize(
                                    formattedMeasure = PollutantIndex.getUnit(selectedPollutant)
                                        .formatMeasure(context, it),
                                    fontSize = MaterialTheme.typography.headlineSmall.fontSize
                                )
                            )
                            append(" ")
                        }
                    }
                    airQuality?.getName(context, selectedPollutant)?.let {
                        withStyle(
                            style = SpanStyle(
                                fontSize = MaterialTheme.typography.headlineSmall.fontSize,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        ) {
                            append(it)
                        }
                    }
                },
                style = MaterialTheme.typography.displaySmall
            )
        }
    }
}

@Composable
private fun AirQualityChart(
    location: Location,
    selectedPollutant: PollutantIndex?,
    mappedValues: ImmutableMap<Long, AirQuality>,
    daily: Daily,
    markerVisibilityListener: CartesianMarkerVisibilityListener,
) {
    val context = LocalContext.current
    val resources = LocalResources.current

    val maxY = remember(mappedValues, selectedPollutant) {
        max(
            selectedPollutant?.maxY ?: PollutantIndex.aqiThresholds[4],
            mappedValues.values.maxOf {
                if (selectedPollutant == null) {
                    it.getIndex()!!
                } else {
                    it.getConcentration(selectedPollutant)?.roundUpToNearestMultiplier(1.0)?.roundToInt() ?: 0
                }
            }
        )
    }

    val modelProducer = remember { CartesianChartModelProducer() }

    LaunchedEffect(location) {
        modelProducer.runTransaction {
            lineSeries {
                series(
                    x = mappedValues.keys,
                    y = mappedValues.values.map {
                        if (selectedPollutant == null) {
                            it.getIndex()!!
                        } else {
                            it.getConcentration(selectedPollutant)!!
                        }
                    }
                )
            }
        }
    }

    BreezyLineChart(
        location = location,
        modelProducer = modelProducer,
        theDay = daily.date,
        maxY = maxY.toDouble(),
        topAxisItemPlacer = remember(mappedValues) {
            TimeTopAxisItemPlacer(mappedValues.keys.toImmutableList())
        },
        endAxisValueFormatter = remember(selectedPollutant) {
            { _, value, _ ->
                if (selectedPollutant == null) {
                    UnitUtils.formatInt(context, value.roundToInt())
                } else {
                    PollutantIndex.getUnit(selectedPollutant).formatMeasure(context, value)
                }
            }
        },
        colors = remember(selectedPollutant) {
            persistentListOf(
                ((selectedPollutant?.thresholds ?: PollutantIndex.aqiThresholds).reversed().map { it.toFloat() }).zip(
                    resources.getIntArray(PollutantIndex.colorsArrayId).reversed().map { Color(it) }
                ).toMap().toImmutableMap()
            )
        },
        trendHorizontalLines = persistentMapOf(
            (selectedPollutant?.let { it.thresholds[3] } ?: PollutantIndex.aqiThresholds[3]).toDouble() to
                resources.getStringArray(R.array.air_quality_levels)[3]
        ),
        topAxisValueFormatter = { _, value, _ ->
            mappedValues.getOrElse(value.toLong()) { null }
                ?.let {
                    UnitUtils.formatInt(
                        context,
                        if (selectedPollutant == null) {
                            it.getIndex()!!
                        } else {
                            it.getConcentration(selectedPollutant)!!.roundToInt()
                        }
                    )
                } ?: "-"
        },
        endAxisItemPlacer = remember(selectedPollutant) {
            SpecificVerticalAxisItemPlacer(
                (selectedPollutant?.thresholds ?: PollutantIndex.aqiThresholds)
                    .map { it.toDouble() }
                    .toMutableList()
                    .apply {
                        if (maxY > last()) {
                            add(maxY.toDouble())
                        }
                    }
            )
        },
        markerVisibilityListener = markerVisibilityListener
    )
}

// TODO: Redundancy
@Composable
private fun AirQualitySwitcher(
    supportedPollutants: ImmutableList<PollutantIndex>,
    setSelectedPollutant: (PollutantIndex?) -> Unit,
    selectedPollutant: PollutantIndex?,
) {
    ButtonGroup(
        horizontalArrangement = Arrangement.spacedBy(ButtonGroupDefaults.ConnectedSpaceBetween),
        overflowIndicator = { menuState ->
            ToggleButton(
                checked = false,
                onCheckedChange = {
                    if (menuState.isExpanded) {
                        menuState.dismiss()
                    } else {
                        menuState.show()
                    }
                },
                shapes = ButtonGroupDefaults.connectedTrailingButtonShapes()
            ) {
                Icon(
                    imageVector = Icons.Filled.MoreVert,
                    contentDescription = stringResource(R.string.action_more)
                )
            }
        }
    ) {
        customItem(
            buttonGroupContent = {
                ToggleButton(
                    checked = selectedPollutant == null,
                    onCheckedChange = { setSelectedPollutant(null) },
                    shapes = ButtonGroupDefaults.connectedLeadingButtonShapes()
                ) {
                    if (selectedPollutant == null) {
                        Icon(
                            Icons.Filled.Check,
                            contentDescription = stringResource(R.string.settings_enabled)
                        )
                        Spacer(Modifier.size(ToggleButtonDefaults.IconSpacing))
                    }
                    Text(
                        text = stringResource(R.string.air_quality_index_short),
                        modifier = Modifier.alignByBaseline()
                    )
                }
            },
            menuContent = { state ->
                DropdownMenuItem(
                    leadingIcon = if (selectedPollutant == null) {
                        {
                            Icon(
                                Icons.Filled.Check,
                                contentDescription = stringResource(R.string.settings_enabled)
                            )
                        }
                    } else {
                        null
                    },
                    text = { Text(stringResource(R.string.air_quality_index_short)) },
                    onClick = {
                        setSelectedPollutant(null)
                        state.dismiss()
                    }
                )
            }
        )
        supportedPollutants.forEach { pollutant ->
            customItem(
                buttonGroupContent = {
                    ToggleButton(
                        checked = selectedPollutant == pollutant,
                        onCheckedChange = { setSelectedPollutant(pollutant) },
                        shapes = ButtonGroupDefaults.connectedMiddleButtonShapes()
                    ) {
                        if (selectedPollutant == pollutant) {
                            Icon(
                                Icons.Filled.Check,
                                contentDescription = stringResource(R.string.settings_enabled)
                            )
                            Spacer(Modifier.size(ToggleButtonDefaults.IconSpacing))
                        }
                        Text(
                            text = UnitUtils.formatPollutantName(stringResource(pollutant.shortName))
                        )
                    }
                },
                menuContent = { state ->
                    DropdownMenuItem(
                        leadingIcon = if (selectedPollutant == pollutant) {
                            {
                                Icon(
                                    Icons.Filled.Check,
                                    contentDescription = stringResource(R.string.settings_enabled)
                                )
                            }
                        } else {
                            null
                        },
                        text = {
                            Text(
                                text = UnitUtils.formatPollutantName(stringResource(pollutant.shortName))
                            )
                        },
                        onClick = {
                            setSelectedPollutant(pollutant)
                            state.dismiss()
                        }
                    )
                }
            )
        }
    }
}

@Composable
fun AirQualityDetails(
    detailedPollutants: ImmutableList<AqiItem>,
    modifier: Modifier = Modifier,
) {
    Material3ExpressiveCardListItem(
        modifier = modifier,
        isFirst = true,
        isLast = true
    ) {
        FlowRow(
            maxItemsInEachRow = 2,
            modifier = Modifier
                .padding(dimensionResource(R.dimen.small_margin))
        ) {
            detailedPollutants.forEach {
                DetailsAirQualityItem(
                    it,
                    Modifier
                        .fillMaxWidth(0.5f)
                        .padding(dimensionResource(R.dimen.small_margin))
                )
            }
        }
    }
}

@Composable
fun DetailsAirQualityItem(
    aqiItem: AqiItem,
    modifier: Modifier = Modifier,
) {
    val color = Color(aqiItem.color)
    // Our maximum is max, while a value between 0.0 and 1.0 is expected
    val progress = (aqiItem.progress / aqiItem.max).coerceAtMost(1.0f)
    Column(
        modifier = modifier
            .semantics(mergeDescendants = true) {}
            .clearAndSetSemantics {
                contentDescription = aqiItem.talkBack
            }
    ) {
        Text(
            text = UnitUtils.formatPollutantName(aqiItem.title),
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

// TODO: Accessibility
@Composable
fun AirQualityScale(
    selectedPollutant: PollutantIndex?,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current

    Material3ExpressiveCardListItem(
        modifier = modifier,
        isFirst = true,
        isLast = true
    ) {
        Column(
            modifier = Modifier.padding(
                horizontal = dimensionResource(R.dimen.normal_margin),
                vertical = dimensionResource(R.dimen.small_margin)
            )
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.small_margin)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    stringResource(R.string.air_quality_index_description),
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(2f)
                )
                Text(
                    stringResource(R.string.air_quality_index_harmless_exposure),
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1.5f)
                )
                Text(
                    selectedPollutant?.let {
                        PollutantIndex.getUnit(it).getDisplayName(context, context.currentLocale)
                    } ?: stringResource(R.string.air_quality_index_short),
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.End,
                    modifier = Modifier.weight(1.5f)
                )
            }
            (selectedPollutant?.thresholds ?: PollutantIndex.aqiThresholds).forEachIndexed { index, startingValue ->
                val aqi = if (selectedPollutant == null) {
                    startingValue
                } else {
                    selectedPollutant.getIndex(startingValue.toDouble())
                }
                val endingValue = (selectedPollutant?.thresholds ?: PollutantIndex.aqiThresholds)
                    .getOrElse(index + 1) { null }
                    ?.let { " – ${UnitUtils.formatInt(context, it - 1)}" }
                    ?: "+"
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.small_margin)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = dimensionResource(R.dimen.small_margin))
                ) {
                    Row(
                        modifier = Modifier.weight(2f),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.small_margin))
                    ) {
                        Icon(
                            modifier = Modifier
                                .size(dimensionResource(R.dimen.material_icon_size)),
                            painter = painterResource(R.drawable.ic_circle),
                            contentDescription = null,
                            tint = Color(PollutantIndex.getAqiToColor(context, aqi))
                        )
                        Text(
                            text = PollutantIndex.getAqiToName(context, aqi)!!
                        )
                    }
                    Text(
                        text = PollutantIndex.getAqiToHarmlessExposure(context, aqi)!!,
                        modifier = Modifier.weight(1.5f)
                    )
                    Text(
                        text = "${UnitUtils.formatInt(context, startingValue)}$endingValue",
                        textAlign = TextAlign.End,
                        modifier = Modifier.weight(1.5f)
                    )
                }
            }
        }
    }
}
