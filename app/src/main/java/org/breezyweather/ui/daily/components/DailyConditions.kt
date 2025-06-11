package org.breezyweather.ui.daily.components

import android.content.Context
import android.text.Spannable
import android.text.SpannableString
import android.text.SpannableStringBuilder
import android.text.style.ImageSpan
import android.view.View
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.ButtonGroupDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.ToggleButton
import androidx.compose.material3.ToggleButtonDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.viewinterop.AndroidView
import breezyweather.domain.location.model.Location
import breezyweather.domain.weather.model.Daily
import breezyweather.domain.weather.model.HalfDay
import breezyweather.domain.weather.model.Hourly
import breezyweather.domain.weather.model.Normals
import breezyweather.domain.weather.model.Temperature
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
import org.breezyweather.common.extensions.getFormattedTime
import org.breezyweather.common.extensions.is12Hour
import org.breezyweather.domain.settings.SettingsManager
import org.breezyweather.ui.common.charts.BreezyLineChart
import org.breezyweather.ui.common.widgets.AnimatableIconView
import org.breezyweather.ui.settings.preference.bottomInsetItem
import org.breezyweather.ui.theme.compose.DayNightTheme
import org.breezyweather.ui.theme.resource.ResourceHelper
import org.breezyweather.ui.theme.resource.ResourcesProviderFactory
import java.util.Date
import kotlin.math.max

@Composable
fun DailyConditions(
    location: Location,
    hourlyList: ImmutableList<Hourly>,
    daily: Daily,
    normals: Normals?,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val showRealTemp = rememberSaveable { mutableStateOf(true) }
    val mappedValues = remember(hourlyList, showRealTemp) {
        hourlyList
            .filter {
                if (showRealTemp.value) {
                    it.temperature?.temperature != null
                } else {
                    it.temperature?.feelsLikeTemperature != null
                }
            }
            .associateBy { it.date.time }
            .toImmutableMap()
    }

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(
            dimensionResource(R.dimen.normal_margin)
        )
    ) {
        item {
            TemperatureSummary(daily, showRealTemp.value)
        }
        item {
            Spacer(modifier = Modifier.height(dimensionResource(R.dimen.normal_margin)))
        }
        if (mappedValues.size >= ChartDisplay.CHART_MIN_COUNT) {
            item {
                TemperatureChart(location, mappedValues, showRealTemp.value)
            }
        } else {
            item {
                UnavailableChart(mappedValues.size)
            }
        }
        item {
            TemperatureSwitcher(
                { showRealTemp.value = it },
                showRealTemp.value
            )
        }
        // TODO: Short explanation
        // Detailed feels like temperatures
        item {
            TemperatureDetails(daily.day?.temperature, daily.night?.temperature, normals)
        }
        // TODO: Make a better design for degree day
        if (daily.degreeDay?.isValid == true) {
            item {
                HorizontalDivider()
            }
            item {
                Spacer(modifier = Modifier.height(dimensionResource(R.dimen.normal_margin)))
            }
            daily.degreeDay?.let { degreeDay ->
                val temperatureUnit = SettingsManager.getInstance(context).temperatureUnit
                if ((degreeDay.heating ?: 0.0) > 0) {
                    item {
                        DailyItem(
                            headlineText = stringResource(R.string.temperature_degree_day_heating),
                            supportingText = temperatureUnit.getDegreeDayValueText(context, degreeDay.heating!!),
                            supportingContentDescription = temperatureUnit
                                .getDegreeDayValueVoice(context, degreeDay.heating!!),
                            icon = R.drawable.ic_mode_heat
                        )
                    }
                } else if ((degreeDay.cooling ?: 0.0) > 0) {
                    item {
                        DailyItem(
                            headlineText = stringResource(R.string.temperature_degree_day_cooling),
                            supportingText = temperatureUnit.getDegreeDayValueText(context, degreeDay.cooling!!),
                            supportingContentDescription = temperatureUnit
                                .getDegreeDayValueVoice(context, degreeDay.cooling!!),
                            icon = R.drawable.ic_mode_cool
                        )
                    }
                }
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
private fun TemperatureSummary(
    daily: Daily,
    showRealTemp: Boolean,
) {
    val context = LocalContext.current

    Row(
        horizontalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.normal_margin)),
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
        ) {
            daily.day?.let { day ->
                (if (showRealTemp) day.temperature?.temperature else day.temperature?.feelsLikeTemperature)?.let {
                    // TODO: Check accessibility
                    Text(
                        text = stringResource(R.string.daytime),
                        style = MaterialTheme.typography.labelSmall
                    )
                    Text(
                        text = SettingsManager.getInstance(context).temperatureUnit.getValueText(context, it),
                        style = MaterialTheme.typography.displaySmall
                    )
                }
                DailyWeatherCondition(day, isDaytime = true)
            }
        }
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
        ) {
            daily.night?.let { night ->
                (if (showRealTemp) night.temperature?.temperature else night.temperature?.feelsLikeTemperature)?.let {
                    // TODO: Check accessibility
                    Text(
                        text = stringResource(R.string.nighttime),
                        style = MaterialTheme.typography.labelSmall
                    )
                    Text(
                        text = SettingsManager.getInstance(context).temperatureUnit.getValueText(context, it),
                        style = MaterialTheme.typography.displaySmall
                    )
                }
                DailyWeatherCondition(night, isDaytime = false)
            }
        }
    }
}

@Composable
fun DailyWeatherCondition(
    halfDay: HalfDay,
    isDaytime: Boolean,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    Row(
        horizontalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.little_margin)),
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier.fillMaxWidth()
    ) {
        if (halfDay.weatherCode != null) {
            val provider = ResourcesProviderFactory.newInstance
            AndroidView(
                factory = {
                    AnimatableIconView(context).apply {
                        importantForAccessibility = View.IMPORTANT_FOR_ACCESSIBILITY_NO
                        setAnimatableIcon(
                            provider.getWeatherIcons(halfDay.weatherCode, isDaytime),
                            provider.getWeatherAnimators(halfDay.weatherCode, isDaytime)
                        )
                        setOnClickListener {
                            startAnimators()
                        }
                    }
                },
                modifier = Modifier
                    .size(dimensionResource(R.dimen.little_weather_icon_size))
            )
        }
        if (!halfDay.weatherText.isNullOrEmpty()) {
            Text(
                text = halfDay.weatherText!!,
                color = DayNightTheme.colors.titleColor,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
private fun TemperatureChart(
    location: Location,
    mappedValues: ImmutableMap<Long, Hourly>,
    showRealTemp: Boolean,
) {
    val provider = ResourcesProviderFactory.newInstance
    val context = LocalContext.current
    val maxY = remember(mappedValues, showRealTemp) {
        max(
            50.0, // TODO: Make this a const
            mappedValues.values.maxOf {
                if (showRealTemp) {
                    it.temperature!!.temperature!!
                } else {
                    it.temperature!!.feelsLikeTemperature!!
                }
            }
        )
    }

    val endAxisValueFormatter = CartesianValueFormatter { _, value, _ ->
        SettingsManager.getInstance(context).temperatureUnit.getValueText(context, value)
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
                        }
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
        MarkerLabelFormatterTemperatureDecorator(mappedValues, location, context, showRealTemp),
        persistentListOf(
            persistentMapOf(
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
        }
    )
}

private class MarkerLabelFormatterTemperatureDecorator(
    private val mappedValues: ImmutableMap<Long, Hourly>,
    private val location: Location,
    private val aContext: Context,
    private val showRealTemp: Boolean,
) : DefaultCartesianMarker.ValueFormatter {

    override fun format(
        context: CartesianDrawingContext,
        targets: List<CartesianMarker.Target>,
    ): CharSequence {
        val model = targets.first()
        val startTime = Date(model.x.toLong()).getFormattedTime(location, aContext, aContext.is12Hour)
        val quantityFormatted = mappedValues.getOrElse(model.x.toLong()) { null }?.let {
            SettingsManager.getInstance(aContext).temperatureUnit.getValueText(
                aContext,
                if (showRealTemp) {
                    it.temperature?.temperature
                } else {
                    it.temperature?.feelsLikeTemperature
                } ?: 0.0
            )
        } ?: "-"

        return SpannableStringBuilder().append(
            startTime,
            aContext.getString(R.string.colon_separator),
            quantityFormatted
        )
    }
}

@Composable
private fun TemperatureDetails(
    daytimeTemperature: Temperature?,
    nighttimeTemperature: Temperature?,
    normals: Normals?,
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.little_margin)),
        modifier = Modifier
            .padding(top = dimensionResource(R.dimen.normal_margin))
            .fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
        ) {
            Text(
                text = stringResource(R.string.daytime),
                style = MaterialTheme.typography.labelSmall
            )
            DailyFeelsLikeTemperatureDetails(daytimeTemperature, normals?.daytimeTemperature)
        }
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
        ) {
            Text(
                text = stringResource(R.string.nighttime),
                style = MaterialTheme.typography.labelSmall
            )
            DailyFeelsLikeTemperatureDetails(nighttimeTemperature, normals?.nighttimeTemperature)
        }
    }
}

// TODO: Check accessibility
@Composable
fun DailyFeelsLikeTemperatureDetails(
    temperature: Temperature?,
    normalsTemperature: Double?,
) {
    val context = LocalContext.current
    val temperatureUnit = SettingsManager.getInstance(context).temperatureUnit
    val temperatureItems = buildList {
        temperature?.let { temp ->
            temp.realFeelTemperature?.let {
                add(Pair(R.string.temperature_real_feel, it))
            }
            temp.realFeelShaderTemperature?.let {
                add(Pair(R.string.temperature_real_feel_shade, it))
            }
            temp.apparentTemperature?.let {
                add(Pair(R.string.temperature_apparent, it))
            }
            temp.windChillTemperature?.let {
                add(Pair(R.string.temperature_wind_chill, it))
            }
            temp.wetBulbTemperature?.let {
                add(Pair(R.string.temperature_wet_bulb, it))
            }
        }
        normalsTemperature?.let {
            add(Pair(R.string.temperature_normal_short, it))
        }
    }
    temperatureItems.forEach { item ->
        DailyItem(
            headlineText = stringResource(item.first),
            supportingText = temperatureUnit.getValueText(context, item.second),
            supportingContentDescription = temperatureUnit.getValueVoice(context, item.second),
            modifier = Modifier.padding(top = dimensionResource(R.dimen.normal_margin))
        )
    }
}
