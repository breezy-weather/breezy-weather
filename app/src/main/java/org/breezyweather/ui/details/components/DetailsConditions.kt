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

import android.text.Spannable
import android.text.SpannableString
import android.text.style.ImageSpan
import android.view.View
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.HelpOutline
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.ButtonGroupDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.PlainTooltip
import androidx.compose.material3.Text
import androidx.compose.material3.ToggleButton
import androidx.compose.material3.ToggleButtonDefaults
import androidx.compose.material3.TooltipAnchorPosition
import androidx.compose.material3.TooltipBox
import androidx.compose.material3.TooltipDefaults
import androidx.compose.material3.rememberTooltipState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.isTraversalGroup
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.viewinterop.AndroidView
import breezyweather.domain.location.model.Location
import breezyweather.domain.weather.model.Daily
import breezyweather.domain.weather.model.Hourly
import breezyweather.domain.weather.model.Normals
import breezyweather.domain.weather.reference.WeatherCode
import com.patrykandpatrick.vico.core.cartesian.axis.VerticalAxis
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
import kotlinx.coroutines.launch
import org.breezyweather.R
import org.breezyweather.common.extensions.currentLocale
import org.breezyweather.common.extensions.formatMeasure
import org.breezyweather.common.extensions.getCalendarMonth
import org.breezyweather.common.extensions.getFormattedTime
import org.breezyweather.common.extensions.is12Hour
import org.breezyweather.common.extensions.isLandscape
import org.breezyweather.common.extensions.roundDownToNearestMultiplier
import org.breezyweather.common.extensions.roundUpToNearestMultiplier
import org.breezyweather.common.extensions.toBitmap
import org.breezyweather.common.extensions.toDate
import org.breezyweather.common.options.appearance.DetailScreen
import org.breezyweather.domain.settings.SettingsManager
import org.breezyweather.ui.common.charts.BreezyLineChart
import org.breezyweather.ui.common.charts.TimeTopAxisItemPlacer
import org.breezyweather.ui.common.widgets.AnimatableIconView
import org.breezyweather.ui.theme.resource.ResourceHelper
import org.breezyweather.ui.theme.resource.ResourcesProviderFactory
import org.breezyweather.unit.formatting.UnitWidth
import org.breezyweather.unit.ratio.Ratio
import org.breezyweather.unit.temperature.Temperature
import org.breezyweather.unit.temperature.Temperature.Companion.celsius
import org.breezyweather.unit.temperature.Temperature.Companion.deciCelsius
import org.breezyweather.unit.temperature.TemperatureUnit
import org.breezyweather.unit.temperature.toTemperature
import java.util.Date
import kotlin.math.max
import kotlin.math.min

@Composable
fun DetailsConditions(
    location: Location,
    hourlyList: ImmutableList<Hourly>,
    daily: Daily,
    normals: Normals?,
    selectedChart: DetailScreen,
    setShowRealTemp: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val temperatureUnit = remember {
        SettingsManager.getInstance(context).getTemperatureUnit(context)
    }
    val mappedValues = remember(hourlyList, selectedChart) {
        hourlyList
            .filter {
                it.temperature?.temperature != null &&
                    if (selectedChart != DetailScreen.TAG_FEELS_LIKE) {
                        true
                    } else {
                        it.temperature?.feelsLikeTemperature != null
                    }
            }
            .associateBy { it.date.time }
            .toImmutableMap()
    }
    var activeItem: Pair<Date, Hourly>? by remember { mutableStateOf(null) }
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

    val mappedProbabilityValues = remember(hourlyList) {
        hourlyList
            .filter { it.precipitationProbability?.total != null }
            .associate { it.date.time to it.precipitationProbability!!.total!! }
            .toImmutableMap()
    }
    var activeProbabilityItem: Pair<Date, Ratio>? by remember { mutableStateOf(null) }
    val probabilityMarkerVisibilityListener = remember {
        object : CartesianMarkerVisibilityListener {
            override fun onShown(marker: CartesianMarker, targets: List<CartesianMarker.Target>) {
                activeProbabilityItem = targets.firstOrNull()?.let { target ->
                    mappedProbabilityValues.getOrElse(target.x.toLong()) { null }?.let {
                        Pair(target.x.toLong().toDate(), it)
                    }
                }
            }

            override fun onUpdated(marker: CartesianMarker, targets: List<CartesianMarker.Target>) {
                onShown(marker, targets)
            }

            override fun onHidden(marker: CartesianMarker) {
                activeProbabilityItem = null
            }
        }
    }

    val tooltipState = rememberTooltipState(isPersistent = true)
    val coroutineScope = rememberCoroutineScope()

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(
            horizontal = dimensionResource(R.dimen.normal_margin),
            vertical = dimensionResource(R.dimen.small_margin)
        )
    ) {
        item {
            TemperatureHeader(
                location,
                daily,
                activeItem,
                selectedChart != DetailScreen.TAG_FEELS_LIKE,
                normals,
                temperatureUnit
            )
        }
        item {
            Spacer(modifier = Modifier.height(dimensionResource(R.dimen.normal_margin)))
        }
        if (mappedValues.size >= DetailScreen.CHART_MIN_COUNT) {
            item {
                TemperatureChart(
                    location,
                    mappedValues,
                    selectedChart != DetailScreen.TAG_FEELS_LIKE,
                    normals,
                    daily,
                    temperatureUnit,
                    markerVisibilityListener
                )
            }
        } else {
            item {
                UnavailableChart(mappedValues.size)
            }
        }
        item {
            TemperatureSwitcher(setShowRealTemp, selectedChart != DetailScreen.TAG_FEELS_LIKE)
        }
        item {
            Text(
                text = stringResource(
                    if (selectedChart != DetailScreen.TAG_FEELS_LIKE) {
                        R.string.temperature_real_details
                    } else {
                        R.string.temperature_feels_like_details
                    }
                ),
                style = MaterialTheme.typography.bodySmall
            )
        }
        // TODO: Short explanation
        if ((daily.day?.weatherSummary != null && daily.day!!.weatherText != daily.day!!.weatherSummary) ||
            (daily.night?.weatherSummary != null && daily.night!!.weatherText != daily.night!!.weatherSummary)
        ) {
            item {
                Spacer(modifier = Modifier.height(dimensionResource(R.dimen.small_margin)))
            }
            item {
                DetailsSectionHeader(stringResource(R.string.daily_summary))
            }
            item {
                DetailsCardText(
                    buildString {
                        if (daily.day?.weatherSummary == daily.night?.weatherSummary) {
                            append(daily.day!!.weatherSummary!!)
                        } else {
                            daily.day?.weatherSummary?.let {
                                append(stringResource(R.string.daytime))
                                append(stringResource(R.string.colon_separator))
                                append(it)
                            }
                            daily.night?.weatherSummary?.let {
                                if (it.isNotEmpty()) append("\n")
                                append(stringResource(R.string.nighttime))
                                append(stringResource(R.string.colon_separator))
                                append(it)
                            }
                        }
                    }
                )
            }
        }
        // TODO: Make a better design for degree day
        if (daily.degreeDay?.isValid == true) {
            item {
                Spacer(modifier = Modifier.height(dimensionResource(R.dimen.small_margin)))
            }
            if ((daily.degreeDay!!.heating?.value ?: 0) > 0) {
                item {
                    TooltipBox(
                        positionProvider = TooltipDefaults.rememberTooltipPositionProvider(TooltipAnchorPosition.Above),
                        tooltip = {
                            PlainTooltip {
                                Text(stringResource(R.string.temperature_degree_day_heating_explanation))
                            }
                        },
                        state = tooltipState
                    ) {
                        DetailsItem(
                            headlineText = stringResource(R.string.temperature_degree_day_heating),
                            supportingText = daily.degreeDay!!.heating!!.toDoubleDeviation(temperatureUnit)
                                .toTemperature(temperatureUnit)
                                .formatMeasure(context, temperatureUnit),
                            icon = R.drawable.ic_mode_heat,
                            modifier = Modifier
                                .semantics(mergeDescendants = true) {}
                                .clearAndSetSemantics {
                                    contentDescription = context.getString(R.string.temperature_degree_day_heating) +
                                        context.getString(R.string.colon_separator) +
                                        daily.degreeDay!!.heating!!.toDoubleDeviation(temperatureUnit)
                                            .toTemperature(temperatureUnit)
                                            .formatMeasure(context, temperatureUnit, unitWidth = UnitWidth.LONG)
                                }
                                .clickable {
                                    coroutineScope.launch {
                                        tooltipState.show()
                                    }
                                },
                            withHelp = true
                        )
                    }
                }
            } else if ((daily.degreeDay!!.cooling?.value ?: 0) > 0) {
                item {
                    TooltipBox(
                        positionProvider = TooltipDefaults.rememberTooltipPositionProvider(TooltipAnchorPosition.Above),
                        tooltip = {
                            PlainTooltip {
                                Text(stringResource(R.string.temperature_degree_day_cooling_explanation))
                            }
                        },
                        state = tooltipState
                    ) {
                        DetailsItem(
                            headlineText = stringResource(R.string.temperature_degree_day_cooling),
                            supportingText = daily.degreeDay!!.cooling!!.toDoubleDeviation(temperatureUnit)
                                .toTemperature(temperatureUnit)
                                .formatMeasure(context, temperatureUnit),
                            icon = R.drawable.ic_mode_cool,
                            modifier = Modifier
                                .semantics(mergeDescendants = true) {}
                                .clearAndSetSemantics {
                                    contentDescription = context.getString(R.string.temperature_degree_day_cooling) +
                                        context.getString(R.string.colon_separator) +
                                        daily.degreeDay!!.heating!!.toDoubleDeviation(temperatureUnit)
                                            .toTemperature(temperatureUnit)
                                            .formatMeasure(context, temperatureUnit, unitWidth = UnitWidth.LONG)
                                }
                                .clickable {
                                    coroutineScope.launch {
                                        tooltipState.show()
                                    }
                                },
                            withHelp = true
                        )
                    }
                }
            }
        }
        if (daily.day?.precipitationProbability != null ||
            daily.night?.precipitationProbability != null ||
            mappedProbabilityValues.isNotEmpty()
        ) {
            item {
                Spacer(modifier = Modifier.height(dimensionResource(R.dimen.small_margin)))
            }
            item {
                DetailsSectionDivider()
            }
            item {
                Text(
                    text = stringResource(R.string.precipitation_probability),
                    style = MaterialTheme.typography.headlineSmall
                )
            }
            item {
                Spacer(modifier = Modifier.height(dimensionResource(R.dimen.small_margin)))
            }
            item {
                PrecipitationProbabilityHeader(location, daily, activeProbabilityItem)
            }
            item {
                Spacer(modifier = Modifier.height(dimensionResource(R.dimen.normal_margin)))
            }
            if (mappedProbabilityValues.size >= DetailScreen.CHART_MIN_COUNT) {
                item {
                    PrecipitationProbabilityChart(
                        location,
                        mappedProbabilityValues,
                        daily,
                        probabilityMarkerVisibilityListener
                    )
                }
            } else {
                item {
                    UnavailableChart(mappedProbabilityValues.size)
                }
            }
            // TODO: Short explanation
            item {
                PrecipitationProbabilityDetails(
                    daily.day?.precipitationProbability,
                    daily.night?.precipitationProbability
                )
            }
        }
        bottomDetailsInset()
    }
}

@Composable
fun TemperatureHeader(
    location: Location,
    daily: Daily,
    activeItem: Pair<Date, Hourly>?,
    showRealTemp: Boolean,
    normals: Normals?,
    temperatureUnit: TemperatureUnit,
) {
    val context = LocalContext.current

    activeItem?.let {
        WeatherConditionItem(
            header = {
                TextFixedHeight(
                    text = it.first.getFormattedTime(location, context, context.is12Hour),
                    style = MaterialTheme.typography.labelMedium
                )
            },
            showRealTemp = showRealTemp,
            temperature = it.second.temperature,
            weatherCode = it.second.weatherCode,
            weatherText = it.second.weatherText,
            isDaytime = it.second.isDaylight,
            animated = false, // Doesn't redraw otherwise
            normals = null,
            monthFormatted = "",
            keepSpaceForSubtext = normals?.daytimeTemperature != null || normals?.nighttimeTemperature != null,
            temperatureUnit = temperatureUnit
        )
    } ?: WeatherConditionSummary(
        daily,
        showRealTemp,
        normals,
        daily.date.getCalendarMonth(location).getDisplayName(context.currentLocale),
        temperatureUnit
    )
}

@Composable
private fun TemperatureSwitcher(
    onRealTempSwitch: (Boolean) -> Unit,
    showRealTemp: Boolean,
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(ButtonGroupDefaults.ConnectedSpaceBetween)
    ) {
        ToggleButton(
            checked = showRealTemp,
            onCheckedChange = { onRealTempSwitch(true) },
            modifier = Modifier.weight(1f),
            shapes = ButtonGroupDefaults.connectedLeadingButtonShapes()
        ) {
            if (showRealTemp) {
                Icon(
                    Icons.Filled.Check,
                    contentDescription = stringResource(R.string.settings_enabled)
                )
                Spacer(Modifier.size(ToggleButtonDefaults.IconSpacing))
            }
            Text(stringResource(R.string.temperature_real))
        }
        ToggleButton(
            checked = !showRealTemp,
            onCheckedChange = { onRealTempSwitch(false) },
            modifier = Modifier.weight(1f),
            shapes = ButtonGroupDefaults.connectedTrailingButtonShapes()
        ) {
            if (!showRealTemp) {
                Icon(
                    Icons.Filled.Check,
                    contentDescription = stringResource(R.string.settings_enabled)
                )
                Spacer(Modifier.size(ToggleButtonDefaults.IconSpacing))
            }
            Text(stringResource(R.string.temperature_feels_like))
        }
    }
}

@Composable
private fun WeatherConditionSummary(
    daily: Daily,
    showRealTemp: Boolean,
    normals: Normals?,
    monthFormatted: String,
    temperatureUnit: TemperatureUnit,
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.normal_margin)),
        modifier = Modifier
            .fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .semantics { isTraversalGroup = true }
        ) {
            daily.day?.let { day ->
                WeatherConditionItem(
                    header = { DaytimeLabel() },
                    showRealTemp = showRealTemp,
                    temperature = day.temperature,
                    weatherCode = day.weatherCode,
                    weatherText = day.weatherText,
                    isDaytime = true,
                    animated = true,
                    normals = normals,
                    monthFormatted = monthFormatted,
                    keepSpaceForSubtext = normals?.daytimeTemperature != null || normals?.nighttimeTemperature != null,
                    temperatureUnit = temperatureUnit
                )
            }
        }
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .semantics { isTraversalGroup = true }
        ) {
            daily.night?.let { night ->
                WeatherConditionItem(
                    header = { NighttimeLabelWithInfo() },
                    showRealTemp = showRealTemp,
                    temperature = night.temperature,
                    weatherCode = night.weatherCode,
                    weatherText = night.weatherText,
                    isDaytime = false,
                    animated = true,
                    normals = normals,
                    monthFormatted = monthFormatted,
                    keepSpaceForSubtext = normals?.daytimeTemperature != null || normals?.nighttimeTemperature != null,
                    temperatureUnit = temperatureUnit
                )
            }
        }
    }
}

@Composable
private fun WeatherConditionItem(
    header: @Composable () -> Unit,
    showRealTemp: Boolean,
    temperature: breezyweather.domain.weather.model.Temperature?,
    weatherCode: WeatherCode?,
    weatherText: String?,
    isDaytime: Boolean,
    animated: Boolean,
    normals: Normals?,
    monthFormatted: String,
    keepSpaceForSubtext: Boolean,
    temperatureUnit: TemperatureUnit,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current

    Column(
        modifier = modifier.fillMaxWidth()
    ) {
        header()
        Row {
            Column {
                (if (showRealTemp) temperature?.temperature else temperature?.feelsLikeTemperature).let { temp ->
                    TextFixedHeight(
                        text = temp?.formatMeasure(context, temperatureUnit, unitWidth = UnitWidth.NARROW) ?: "",
                        style = MaterialTheme.typography.displaySmall,
                        modifier = Modifier
                            .clearAndSetSemantics {
                                temp?.let {
                                    contentDescription = it.formatMeasure(
                                        context,
                                        temperatureUnit,
                                        unitWidth = UnitWidth.LONG
                                    )
                                }
                            }
                    )
                }
                if (!showRealTemp) {
                    TextFixedHeight(
                        text = temperature?.temperature?.let {
                            stringResource(R.string.temperature_real) +
                                stringResource(R.string.colon_separator) +
                                it.formatMeasure(context, temperatureUnit)
                        } ?: "",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier
                            .clearAndSetSemantics {
                                if (temperature?.temperature != null) {
                                    contentDescription = context.getString(R.string.temperature_real) +
                                        context.getString(R.string.colon_separator) +
                                        temperature.temperature!!.formatMeasure(
                                            context,
                                            temperatureUnit,
                                            unitWidth = UnitWidth.LONG
                                        )
                                }
                            }
                    )
                } else {
                    NormalsDepartureLabel(
                        temperature?.temperature,
                        normals,
                        monthFormatted,
                        isDaytime,
                        keepSpaceForSubtext,
                        temperatureUnit
                    )
                }
            }
            if (context.isLandscape) {
                Spacer(Modifier.width(dimensionResource(R.dimen.large_margin)))
                WeatherCondition(weatherCode, weatherText, isDaytime = isDaytime, animated = animated)
            }
        }
        if (!context.isLandscape) {
            Spacer(Modifier.height(dimensionResource(R.dimen.small_margin)))
            WeatherCondition(weatherCode, weatherText, isDaytime = isDaytime, animated = animated)
        }
    }
}

@Composable
fun NormalsDepartureLabel(
    halfDayTemperature: Temperature?,
    normals: Normals?,
    monthFormatted: String,
    isDaytime: Boolean,
    keepSpaceForSubtext: Boolean,
    temperatureUnit: TemperatureUnit,
    modifier: Modifier = Modifier,
) {
    val normal = if (isDaytime) normals?.daytimeTemperature else normals?.nighttimeTemperature

    if (halfDayTemperature != null && normal != null) {
        val context = LocalContext.current
        val tooltipState = rememberTooltipState(isPersistent = true)
        val coroutineScope = rememberCoroutineScope()
        val departure = remember(halfDayTemperature, normal) {
            halfDayTemperature.toDouble(temperatureUnit) - normal.toDouble(temperatureUnit)
        }

        TooltipBox(
            positionProvider = TooltipDefaults.rememberTooltipPositionProvider(TooltipAnchorPosition.Below),
            tooltip = {
                PlainTooltip {
                    Text(
                        stringResource(
                            if (isDaytime) {
                                R.string.temperature_normals_deviation_explanation_maximum
                            } else {
                                R.string.temperature_normals_deviation_explanation_minimum
                            },
                            monthFormatted
                        )
                    )
                }
            },
            state = tooltipState
        ) {
            Row(
                modifier = modifier
                    .clickable {
                        coroutineScope.launch {
                            tooltipState.show()
                        }
                    }
                    .height(
                        with(LocalDensity.current) {
                            MaterialTheme.typography.labelLarge.lineHeight.toDp()
                        }
                    ),
                horizontalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.small_margin)),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(R.string.temperature_normals_deviation) +
                        stringResource(R.string.colon_separator) +
                        "" +
                        departure.toTemperature(temperatureUnit).formatMeasure(
                            context,
                            temperatureUnit,
                            unitWidth = UnitWidth.NARROW,
                            showSign = true
                        ),
                    style = MaterialTheme.typography.labelLarge,
                    maxLines = 1
                )
                Icon(
                    imageVector = Icons.AutoMirrored.Outlined.HelpOutline,
                    // TooltipBox already takes care of adding the info that there is a tooltip:
                    contentDescription = null,
                    modifier = Modifier
                        .fillMaxHeight()
                )
            }
        }
    } else if (keepSpaceForSubtext) {
        TextFixedHeight(text = "", style = MaterialTheme.typography.labelLarge)
    }
}

@Composable
fun WeatherCondition(
    weatherCode: WeatherCode?,
    weatherText: String?,
    isDaytime: Boolean,
    animated: Boolean,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    Row(
        horizontalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.small_margin)),
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier.fillMaxWidth()
    ) {
        if (weatherCode != null) {
            val provider = ResourcesProviderFactory.newInstance
            if (animated) {
                AndroidView(
                    factory = {
                        AnimatableIconView(context).apply {
                            importantForAccessibility = View.IMPORTANT_FOR_ACCESSIBILITY_NO
                            setAnimatableIcon(
                                provider.getWeatherIcons(weatherCode, isDaytime),
                                provider.getWeatherAnimators(weatherCode, isDaytime)
                            )
                            setOnClickListener {
                                startAnimators()
                            }
                        }
                    },
                    modifier = Modifier
                        .size(dimensionResource(R.dimen.small_weather_icon_size))
                )
            } else {
                Image(
                    bitmap = provider.getWeatherIcon(weatherCode, isDaytime).toBitmap().asImageBitmap(),
                    contentDescription = null,
                    modifier = Modifier
                        .size(dimensionResource(R.dimen.small_weather_icon_size))
                )
            }
        }
        TextFixedHeight(
            text = weatherText ?: "",
            color = MaterialTheme.colorScheme.onSurface,
            fontWeight = FontWeight.Bold,
            maxLines = 2
        )
    }
}

@Composable
private fun TemperatureChart(
    location: Location,
    mappedValues: ImmutableMap<Long, Hourly>,
    showRealTemp: Boolean,
    normals: Normals?,
    daily: Daily,
    temperatureUnit: TemperatureUnit,
    markerVisibilityListener: CartesianMarkerVisibilityListener,
) {
    val context = LocalContext.current

    val provider = ResourcesProviderFactory.newInstance
    val step = temperatureUnit.chartStep
    val minY = remember(mappedValues, showRealTemp, normals) {
        if (showRealTemp) {
            mappedValues.values.minOf { it.temperature!!.temperature!!.inDeciCelsius }
        } else {
            min(
                mappedValues.values.minOf { it.temperature!!.temperature!!.inDeciCelsius },
                mappedValues.values.minOf { it.temperature!!.feelsLikeTemperature!!.inDeciCelsius }
            )
        }.let {
            normals?.nighttimeTemperature?.let { normal ->
                if (normal.inDeciCelsius < it) normal.inDeciCelsius else it
            } ?: it
        }.deciCelsius.toDouble(temperatureUnit).roundDownToNearestMultiplier(step)
    }
    val maxY = remember(mappedValues, showRealTemp, normals) {
        if (showRealTemp) {
            mappedValues.values.maxOf { it.temperature!!.temperature!!.inDeciCelsius }
        } else {
            max(
                mappedValues.values.maxOf { it.temperature!!.temperature!!.inDeciCelsius },
                mappedValues.values.maxOf { it.temperature!!.feelsLikeTemperature!!.inDeciCelsius }
            )
        }.let {
            normals?.daytimeTemperature?.let { normal ->
                if (normal.inDeciCelsius > it) normal.inDeciCelsius else it
            } ?: it
        }.deciCelsius.toDouble(temperatureUnit).roundUpToNearestMultiplier(step)
    }

    val modelProducer = remember { CartesianChartModelProducer() }

    LaunchedEffect(location, showRealTemp) {
        modelProducer.runTransaction {
            lineSeries {
                series(
                    x = mappedValues.keys,
                    y = mappedValues.values.map {
                        if (showRealTemp) {
                            it.temperature!!.temperature!!
                        } else {
                            it.temperature!!.feelsLikeTemperature!!
                        }.toDouble(temperatureUnit)
                    }
                )
                if (!showRealTemp) {
                    series(
                        x = mappedValues.keys,
                        y = mappedValues.values.map {
                            it.temperature!!.temperature!!.toDouble(temperatureUnit)
                        }
                    )
                }
            }
        }
    }

    BreezyLineChart(
        location = location,
        modelProducer = modelProducer,
        theDay = daily.date,
        maxY = maxY,
        topAxisItemPlacer = remember(mappedValues) {
            TimeTopAxisItemPlacer(mappedValues.keys.toImmutableList())
        },
        endAxisValueFormatter = { _, value, _ ->
            value.toTemperature(temperatureUnit)
                .formatMeasure(context, temperatureUnit, valueWidth = UnitWidth.NARROW, unitWidth = UnitWidth.NARROW)
        },
        colors = remember {
            persistentListOf(
                persistentMapOf(
                    47.celsius.toDouble(temperatureUnit).toFloat() to Color(71, 14, 0),
                    30.celsius.toDouble(temperatureUnit).toFloat() to Color(232, 83, 25),
                    21.celsius.toDouble(temperatureUnit).toFloat() to Color(243, 183, 4),
                    10.celsius.toDouble(temperatureUnit).toFloat() to Color(128, 147, 24),
                    1.celsius.toDouble(temperatureUnit).toFloat() to Color(68, 125, 99),
                    0.celsius.toDouble(temperatureUnit).toFloat() to Color(93, 133, 198),
                    -4.celsius.toDouble(temperatureUnit).toFloat() to Color(100, 166, 189),
                    -8.celsius.toDouble(temperatureUnit).toFloat() to Color(106, 191, 181),
                    -15.celsius.toDouble(temperatureUnit).toFloat() to Color(157, 219, 217),
                    -25.celsius.toDouble(temperatureUnit).toFloat() to Color(143, 89, 169),
                    -40.celsius.toDouble(temperatureUnit).toFloat() to Color(162, 70, 145),
                    -55.celsius.toDouble(temperatureUnit).toFloat() to Color(202, 172, 195),
                    -70.celsius.toDouble(temperatureUnit).toFloat() to Color(115, 70, 105)
                ),
                persistentMapOf(
                    50f to Color(128, 128, 128, 160),
                    0f to Color(128, 128, 128, 160)
                )
            )
        },
        topAxisValueFormatter = { _, value, _ ->
            mappedValues.getOrElse(value.toLong()) { null }?.let { hourly ->
                hourly.weatherCode?.let {
                    val ss = SpannableString("abc")
                    val d = ResourceHelper.getWeatherIcon(provider, it, hourly.isDaylight)
                    d.setBounds(0, 0, 64, 64)
                    val span = ImageSpan(d, ImageSpan.ALIGN_BASELINE)
                    ss.setSpan(span, 0, 3, Spannable.SPAN_INCLUSIVE_EXCLUSIVE)
                    ss
                }
            } ?: "-"
        },
        trendHorizontalLines = buildMap {
            normals?.let {
                it.daytimeTemperature?.let { normal ->
                    put(
                        normal.toDouble(temperatureUnit),
                        context.getString(R.string.temperature_normal_short)
                    )
                }
                it.nighttimeTemperature?.let { normal ->
                    put(
                        normal.toDouble(temperatureUnit),
                        context.getString(R.string.temperature_normal_short)
                    )
                }
            }
        }.toImmutableMap(),
        minY = minY,
        endAxisItemPlacer = remember { VerticalAxis.ItemPlacer.step({ step }) },
        markerVisibilityListener = markerVisibilityListener
    )
}
