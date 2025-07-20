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
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.ButtonGroupDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.PlainTooltip
import androidx.compose.material3.Text
import androidx.compose.material3.ToggleButton
import androidx.compose.material3.ToggleButtonDefaults
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
import breezyweather.domain.weather.model.Temperature
import breezyweather.domain.weather.model.WeatherCode
import com.patrykandpatrick.vico.core.cartesian.axis.VerticalAxis
import com.patrykandpatrick.vico.core.cartesian.data.CartesianChartModelProducer
import com.patrykandpatrick.vico.core.cartesian.data.CartesianValueFormatter
import com.patrykandpatrick.vico.core.cartesian.data.lineSeries
import com.patrykandpatrick.vico.core.cartesian.marker.CartesianMarker
import com.patrykandpatrick.vico.core.cartesian.marker.CartesianMarkerVisibilityListener
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.ImmutableMap
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.persistentMapOf
import kotlinx.collections.immutable.toImmutableMap
import kotlinx.coroutines.launch
import org.breezyweather.R
import org.breezyweather.common.basic.models.options.appearance.DetailScreen
import org.breezyweather.common.extensions.getFormattedTime
import org.breezyweather.common.extensions.is12Hour
import org.breezyweather.common.extensions.isLandscape
import org.breezyweather.common.extensions.roundDownToNearestMultiplier
import org.breezyweather.common.extensions.roundUpToNearestMultiplier
import org.breezyweather.common.extensions.toBitmap
import org.breezyweather.common.extensions.toDate
import org.breezyweather.domain.settings.SettingsManager
import org.breezyweather.ui.common.charts.BreezyLineChart
import org.breezyweather.ui.common.widgets.AnimatableIconView
import org.breezyweather.ui.settings.preference.bottomInsetItem
import org.breezyweather.ui.theme.resource.ResourceHelper
import org.breezyweather.ui.theme.resource.ResourcesProviderFactory
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
    val mappedProbabilityValues = remember(hourlyList) {
        hourlyList
            .filter { it.precipitationProbability?.total != null }
            .associate { it.date.time to it.precipitationProbability!!.total!! }
            .toImmutableMap()
    }
    val tooltipState = rememberTooltipState()
    val coroutineScope = rememberCoroutineScope()

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(
            horizontal = dimensionResource(R.dimen.normal_margin),
            vertical = dimensionResource(R.dimen.small_margin)
        )
    ) {
        item {
            TemperatureChart(location, mappedValues, selectedChart != DetailScreen.TAG_FEELS_LIKE, normals, daily)
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
        if ((daily.day?.weatherPhase != null && daily.day!!.weatherText != daily.day!!.weatherPhase) ||
            (daily.night?.weatherPhase != null && daily.night!!.weatherText != daily.night!!.weatherPhase)
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
                        if (daily.day?.weatherPhase == daily.night?.weatherPhase) {
                            append(daily.day!!.weatherPhase!!)
                        } else {
                            daily.day?.weatherPhase?.let {
                                append(stringResource(R.string.daytime))
                                append(stringResource(R.string.colon_separator))
                                append(it)
                            }
                            daily.night?.weatherPhase?.let {
                                if (it.isNotEmpty()) append("\n")
                                append(stringResource(R.string.nighttime))
                                append(stringResource(R.string.colon_separator))
                                append(it)
                            }
                        }
                    }
                )
            }
            item {
                Spacer(modifier = Modifier.height(dimensionResource(R.dimen.small_margin)))
            }
        }
        // Detailed feels like temperatures
        if (normals != null || selectedChart == DetailScreen.TAG_FEELS_LIKE) {
            item {
                TemperatureDetails(
                    if (selectedChart != DetailScreen.TAG_FEELS_LIKE) null else daily.day?.temperature,
                    if (selectedChart != DetailScreen.TAG_FEELS_LIKE) null else daily.night?.temperature,
                    normals
                )
            }
        }
        // TODO: Make a better design for degree day
        if (daily.degreeDay?.isValid == true) {
            val temperatureUnit = SettingsManager.getInstance(context).getTemperatureUnit(context)
            item {
                Spacer(modifier = Modifier.height(dimensionResource(R.dimen.small_margin)))
            }
            if ((daily.degreeDay!!.heating ?: 0.0) > 0) {
                item {
                    TooltipBox(
                        positionProvider = TooltipDefaults.rememberTooltipPositionProvider(),
                        tooltip = {
                            PlainTooltip {
                                Text(stringResource(R.string.temperature_degree_day_heating_explanation))
                            }
                        },
                        state = tooltipState
                    ) {
                        DetailsItem(
                            headlineText = stringResource(R.string.temperature_degree_day_heating),
                            supportingText = temperatureUnit.formatDegreeDay(
                                context,
                                daily.degreeDay!!.heating!!
                            ),
                            icon = R.drawable.ic_mode_heat,
                            modifier = Modifier
                                .semantics(mergeDescendants = true) {}
                                .clearAndSetSemantics {
                                    contentDescription = context.getString(R.string.temperature_degree_day_heating) +
                                        context.getString(R.string.colon_separator) +
                                        temperatureUnit.formatDegreeDayContentDescription(
                                            context,
                                            daily.degreeDay!!.heating!!
                                        )
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
            } else if ((daily.degreeDay!!.cooling ?: 0.0) > 0) {
                item {
                    TooltipBox(
                        positionProvider = TooltipDefaults.rememberTooltipPositionProvider(),
                        tooltip = {
                            PlainTooltip {
                                Text(stringResource(R.string.temperature_degree_day_cooling_explanation))
                            }
                        },
                        state = tooltipState
                    ) {
                        DetailsItem(
                            headlineText = stringResource(R.string.temperature_degree_day_cooling),
                            supportingText = temperatureUnit.formatDegreeDay(
                                context,
                                daily.degreeDay!!.cooling!!
                            ),
                            icon = R.drawable.ic_mode_cool,
                            modifier = Modifier
                                .semantics(mergeDescendants = true) {}
                                .clearAndSetSemantics {
                                    contentDescription = context.getString(R.string.temperature_degree_day_cooling) +
                                        context.getString(R.string.colon_separator) +
                                        temperatureUnit.formatDegreeDayContentDescription(
                                            context,
                                            daily.degreeDay!!.cooling!!
                                        )
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
                PrecipitationProbabilityChart(
                    location,
                    mappedProbabilityValues,
                    daily
                )
            }
            // TODO: Short explanation
            item {
                PrecipitationProbabilityDetails(
                    daily.day?.precipitationProbability,
                    daily.night?.precipitationProbability
                )
            }
        }
        bottomInsetItem()
    }
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
                    animated = true
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
                    animated = true
                )
            }
        }
    }
}

@Composable
private fun WeatherConditionItem(
    header: @Composable () -> Unit,
    showRealTemp: Boolean,
    temperature: Temperature?,
    weatherCode: WeatherCode?,
    weatherText: String?,
    isDaytime: Boolean,
    animated: Boolean,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val temperatureUnit = SettingsManager.getInstance(context).getTemperatureUnit(context)

    Column(
        modifier = modifier.fillMaxWidth()
    ) {
        header()
        Row {
            Column {
                (if (showRealTemp) temperature?.temperature else temperature?.feelsLikeTemperature).let { temp ->
                    TextFixedHeight(
                        text = temp?.let {
                            temperatureUnit.formatMeasureShort(context, value = it, 1)
                        } ?: "",
                        style = MaterialTheme.typography.displaySmall,
                        modifier = Modifier
                            .clearAndSetSemantics {
                                temp?.let {
                                    contentDescription = temperatureUnit.formatContentDescription(context, it)
                                }
                            }
                    )
                }
                if (!showRealTemp) {
                    TextFixedHeight(
                        text = temperature?.temperature?.let {
                            stringResource(R.string.temperature_real) +
                                stringResource(R.string.colon_separator) +
                                temperatureUnit.formatMeasure(context, value = it)
                        } ?: "",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier
                            .clearAndSetSemantics {
                                if (temperature?.temperature != null) {
                                    contentDescription = context.getString(R.string.temperature_real) +
                                        context.getString(R.string.colon_separator) +
                                        temperatureUnit.formatContentDescription(context, temperature.temperature!!)
                                }
                            }
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
) {
    val context = LocalContext.current

    var activeMarkerTarget: CartesianMarker.Target? by remember { mutableStateOf(null) }
    val markerVisibilityListener = object : CartesianMarkerVisibilityListener {
        override fun onShown(marker: CartesianMarker, targets: List<CartesianMarker.Target>) {
            activeMarkerTarget = targets.firstOrNull()
        }

        override fun onUpdated(marker: CartesianMarker, targets: List<CartesianMarker.Target>) {
            activeMarkerTarget = targets.firstOrNull()
        }

        override fun onHidden(marker: CartesianMarker) {
            activeMarkerTarget = null
        }
    }

    activeMarkerTarget?.let {
        mappedValues.getOrElse(it.x.toLong()) { null }?.let { hourly ->
            WeatherConditionItem(
                header = {
                    TextFixedHeight(
                        text = it.x.toLong().toDate().getFormattedTime(location, context, context.is12Hour),
                        style = MaterialTheme.typography.labelMedium
                    )
                },
                showRealTemp = showRealTemp,
                temperature = hourly.temperature,
                weatherCode = hourly.weatherCode,
                weatherText = hourly.weatherText,
                isDaytime = hourly.isDaylight,
                animated = false // Doesn't redraw otherwise
            )
        }
    } ?: WeatherConditionSummary(daily, showRealTemp)

    Spacer(modifier = Modifier.height(dimensionResource(R.dimen.normal_margin)))

    val hasEnoughValues = remember(mappedValues) {
        mappedValues.size >= DetailScreen.CHART_MIN_COUNT
    }

    if (hasEnoughValues) {
        val provider = ResourcesProviderFactory.newInstance
        val temperatureUnit = SettingsManager.getInstance(context).getTemperatureUnit(context)
        val step = temperatureUnit.chartStep
        val minY = remember(mappedValues, showRealTemp, normals) {
            if (showRealTemp) {
                mappedValues.values.minOf { it.temperature!!.temperature!! }
            } else {
                min(
                    mappedValues.values.minOf { it.temperature!!.temperature!! },
                    mappedValues.values.minOf { it.temperature!!.feelsLikeTemperature!! }
                )
            }.let {
                normals?.nighttimeTemperature?.let { normal ->
                    if (normal < it) normal else it
                } ?: it
            }.let {
                temperatureUnit.getConvertedUnit(it)
            }.roundDownToNearestMultiplier(step)
        }
        val maxY = remember(mappedValues, showRealTemp, normals) {
            if (showRealTemp) {
                mappedValues.values.maxOf { it.temperature!!.temperature!! }
            } else {
                max(
                    mappedValues.values.maxOf { it.temperature!!.temperature!! },
                    mappedValues.values.maxOf { it.temperature!!.feelsLikeTemperature!! }
                )
            }.let {
                normals?.daytimeTemperature?.let { normal ->
                    if (normal > it) normal else it
                } ?: it
            }.let {
                temperatureUnit.getConvertedUnit(it)
            }.roundUpToNearestMultiplier(step)
        }

        val endAxisValueFormatter = CartesianValueFormatter { _, value, _ ->
            temperatureUnit.formatMeasureShort(context, value, isValueInDefaultUnit = false)
        }

        val modelProducer = remember { CartesianChartModelProducer() }

        LaunchedEffect(location, showRealTemp) {
            modelProducer.runTransaction {
                lineSeries {
                    series(
                        x = mappedValues.keys,
                        y = mappedValues.values.map {
                            temperatureUnit.getConvertedUnit(
                                if (showRealTemp) {
                                    it.temperature!!.temperature!!
                                } else {
                                    it.temperature!!.feelsLikeTemperature!!
                                }
                            )
                        }
                    )
                    if (!showRealTemp) {
                        series(
                            x = mappedValues.keys,
                            y = mappedValues.values.map {
                                temperatureUnit.getConvertedUnit(it.temperature!!.temperature!!)
                            }
                        )
                    }
                }
            }
        }

        BreezyLineChart(
            location,
            modelProducer,
            daily.date,
            maxY,
            endAxisValueFormatter,
            persistentListOf(
                persistentMapOf(
                    temperatureUnit.getConvertedUnit(47.0).toFloat() to Color(71, 14, 0),
                    temperatureUnit.getConvertedUnit(30.0).toFloat() to Color(232, 83, 25),
                    temperatureUnit.getConvertedUnit(21.0).toFloat() to Color(243, 183, 4),
                    temperatureUnit.getConvertedUnit(10.0).toFloat() to Color(128, 147, 24),
                    temperatureUnit.getConvertedUnit(1.0).toFloat() to Color(68, 125, 99),
                    temperatureUnit.getConvertedUnit(0.0).toFloat() to Color(93, 133, 198),
                    temperatureUnit.getConvertedUnit(-4.0).toFloat() to Color(100, 166, 189),
                    temperatureUnit.getConvertedUnit(-8.0).toFloat() to Color(106, 191, 181),
                    temperatureUnit.getConvertedUnit(-15.0).toFloat() to Color(157, 219, 217),
                    temperatureUnit.getConvertedUnit(-25.0).toFloat() to Color(143, 89, 169),
                    temperatureUnit.getConvertedUnit(-40.0).toFloat() to Color(162, 70, 145),
                    temperatureUnit.getConvertedUnit(-55.0).toFloat() to Color(202, 172, 195),
                    temperatureUnit.getConvertedUnit(-70.0).toFloat() to Color(115, 70, 105)
                ),
                persistentMapOf(
                    50f to Color(128, 128, 128, 160),
                    0f to Color(128, 128, 128, 160)
                )
            ),
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
                            temperatureUnit.getConvertedUnit(normal),
                            context.getString(R.string.temperature_normal_short)
                        )
                    }
                    it.nighttimeTemperature?.let { normal ->
                        put(
                            temperatureUnit.getConvertedUnit(normal),
                            context.getString(R.string.temperature_normal_short)
                        )
                    }
                }
            }.toImmutableMap(),
            minY = minY,
            endAxisItemPlacer = remember {
                VerticalAxis.ItemPlacer.step({ step })
            },
            markerVisibilityListener = markerVisibilityListener
        )
    } else {
        UnavailableChart(mappedValues.size)
    }
}

@Composable
private fun TemperatureDetails(
    daytimeTemperature: Temperature?,
    nighttimeTemperature: Temperature?,
    normals: Normals?,
) {
    val daytimeTempItems = mutableListOf<Pair<Int, Double?>>()
    val nighttimeTempItems = mutableListOf<Pair<Int, Double?>>()

    if (daytimeTemperature?.realFeelTemperature != null || nighttimeTemperature?.realFeelTemperature != null) {
        daytimeTempItems.add(Pair(R.string.temperature_real_feel, daytimeTemperature?.realFeelTemperature))
        nighttimeTempItems.add(Pair(R.string.temperature_real_feel, nighttimeTemperature?.realFeelTemperature))
    }
    if (daytimeTemperature?.realFeelShaderTemperature != null ||
        nighttimeTemperature?.realFeelShaderTemperature != null
    ) {
        daytimeTempItems.add(
            Pair(R.string.temperature_real_feel_shade, daytimeTemperature?.realFeelShaderTemperature)
        )
        nighttimeTempItems.add(
            Pair(R.string.temperature_real_feel_shade, nighttimeTemperature?.realFeelShaderTemperature)
        )
    }
    if (daytimeTemperature?.apparentTemperature != null || nighttimeTemperature?.apparentTemperature != null) {
        daytimeTempItems.add(Pair(R.string.temperature_apparent, daytimeTemperature?.apparentTemperature))
        nighttimeTempItems.add(Pair(R.string.temperature_apparent, nighttimeTemperature?.apparentTemperature))
    }
    if (daytimeTemperature?.windChillTemperature != null || nighttimeTemperature?.windChillTemperature != null) {
        daytimeTempItems.add(Pair(R.string.temperature_wind_chill, daytimeTemperature?.windChillTemperature))
        nighttimeTempItems.add(Pair(R.string.temperature_wind_chill, nighttimeTemperature?.windChillTemperature))
    }
    if (daytimeTemperature?.wetBulbTemperature != null || nighttimeTemperature?.wetBulbTemperature != null) {
        daytimeTempItems.add(Pair(R.string.temperature_wet_bulb, daytimeTemperature?.wetBulbTemperature))
        nighttimeTempItems.add(Pair(R.string.temperature_wet_bulb, nighttimeTemperature?.wetBulbTemperature))
    }
    if (normals?.daytimeTemperature != null || normals?.nighttimeTemperature != null) {
        daytimeTempItems.add(Pair(R.string.temperature_normal_short, normals.daytimeTemperature))
        nighttimeTempItems.add(Pair(R.string.temperature_normal_short, normals.nighttimeTemperature))
    }

    Row(
        horizontalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.small_margin)),
        modifier = Modifier
            .padding(top = dimensionResource(R.dimen.normal_margin))
            .fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .semantics { isTraversalGroup = true }
        ) {
            DaytimeLabel()
            daytimeTempItems.forEach {
                DailyFeelsLikeTemperatureDetail(it)
            }
        }
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .semantics { isTraversalGroup = true }
        ) {
            NighttimeLabelWithInfo()
            nighttimeTempItems.forEach {
                DailyFeelsLikeTemperatureDetail(it)
            }
        }
    }
}

@Composable
fun DailyFeelsLikeTemperatureDetail(
    item: Pair<Int, Double?>,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val temperatureUnit = SettingsManager.getInstance(context).getTemperatureUnit(context)
    DetailsItem(
        headlineText = stringResource(item.first),
        supportingText = item.second?.let { temperatureUnit.formatMeasure(context, value = it) }
            ?: stringResource(R.string.null_data_text),
        modifier = modifier
            .padding(top = dimensionResource(R.dimen.normal_margin))
            .semantics(mergeDescendants = true) {}
            .clearAndSetSemantics {
                item.second?.let {
                    contentDescription = context.getString(item.first) +
                        context.getString(R.string.colon_separator) +
                        temperatureUnit.formatContentDescription(context, it)
                }
            }
    )
}
