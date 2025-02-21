package org.breezyweather.ui.daily

import android.content.Context
import android.view.View
import androidx.annotation.DrawableRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import breezyweather.domain.location.model.Location
import breezyweather.domain.weather.model.AirQuality
import breezyweather.domain.weather.model.Astro
import breezyweather.domain.weather.model.HalfDay
import breezyweather.domain.weather.model.MoonPhase
import breezyweather.domain.weather.model.Normals
import breezyweather.domain.weather.model.Precipitation
import breezyweather.domain.weather.model.PrecipitationDuration
import breezyweather.domain.weather.model.PrecipitationProbability
import breezyweather.domain.weather.model.Temperature
import breezyweather.domain.weather.model.UV
import breezyweather.domain.weather.model.Wind
import org.breezyweather.BreezyWeather
import org.breezyweather.R
import org.breezyweather.common.basic.models.options.unit.DurationUnit
import org.breezyweather.common.basic.models.options.unit.TemperatureUnit
import org.breezyweather.common.extensions.currentLocale
import org.breezyweather.common.extensions.getFormattedDate
import org.breezyweather.common.extensions.getFormattedTime
import org.breezyweather.common.extensions.is12Hour
import org.breezyweather.domain.settings.SettingsManager
import org.breezyweather.domain.weather.model.getColor
import org.breezyweather.domain.weather.model.getContentDescription
import org.breezyweather.domain.weather.model.getDescription
import org.breezyweather.domain.weather.model.getDirection
import org.breezyweather.domain.weather.model.getIndex
import org.breezyweather.domain.weather.model.getName
import org.breezyweather.domain.weather.model.getShortDescription
import org.breezyweather.domain.weather.model.getStrength
import org.breezyweather.domain.weather.model.getUVColor
import org.breezyweather.ui.common.widgets.AnimatableIconView
import org.breezyweather.ui.common.widgets.astro.MoonPhaseView
import org.breezyweather.ui.theme.ThemeManager
import org.breezyweather.ui.theme.compose.DayNightTheme
import org.breezyweather.ui.theme.resource.ResourcesProviderFactory
import java.text.NumberFormat

@Composable
fun DailyTitle(
    text: String,
    modifier: Modifier = Modifier,
    @DrawableRes icon: Int? = null,
    style: TextStyle = MaterialTheme.typography.titleSmall,
) {
    if (icon != null) {
        ListItem(
            leadingContent = {
                Image(
                    painter = painterResource(icon),
                    contentDescription = null,
                    colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.primary)
                )
            },
            headlineContent = {
                Text(
                    text = text,
                    color = MaterialTheme.colorScheme.primary,
                    style = style,
                    modifier = modifier
                )
            }
        )
    } else {
        Text(
            text = text,
            color = MaterialTheme.colorScheme.primary,
            style = style,
            modifier = modifier
        )
    }
}

fun LazyListScope.dailyHalfDay(
    context: Context,
    halfDay: HalfDay,
    isDaytime: Boolean,
    thisDayNormals: Normals?,
) {
    item {
        DailyTitle(
            text = stringResource(if (isDaytime) R.string.daytime else R.string.nighttime),
            style = MaterialTheme.typography.titleMedium
        )
    }
    item {
        DailyOverview(
            halfDay = halfDay,
            isDaytime = isDaytime
        )
    }
    item {
        halfDay.wind?.let { wind ->
            if (wind.isValid) {
                DailyWind(wind)
            }
        }
    }
    if (halfDay.temperature?.feelsLikeTemperature != null) {
        item {
            DailyTitle(
                icon = R.drawable.ic_device_thermostat,
                text = stringResource(R.string.temperature)
            )
        }
        dailyFeelsLikeTemperatures(
            context,
            halfDay.temperature!!,
            if (isDaytime) {
                thisDayNormals?.daytimeTemperature
            } else {
                thisDayNormals?.nighttimeTemperature
            }
        )
    }
    if (halfDay.precipitation?.isValid == true) {
        item {
            DailyTitle(
                icon = R.drawable.ic_water,
                text = stringResource(R.string.precipitation)
            )
        }
        dailyPrecipitation(context, halfDay.precipitation!!)
    }
    if (halfDay.precipitationProbability?.isValid == true) {
        item {
            DailyTitle(
                icon = R.drawable.ic_water_percent,
                text = stringResource(R.string.precipitation_probability)
            )
        }
        dailyPrecipitationProbability(context, halfDay.precipitationProbability!!)
    }
    if ((halfDay.precipitationDuration?.total ?: 0.0) > 0) {
        item {
            DailyTitle(
                icon = R.drawable.ic_time,
                text = stringResource(R.string.precipitation_duration)
            )
        }
        dailyPrecipitationDuration(context, halfDay.precipitationDuration!!)
    }
    item {
        HorizontalDivider()
    }
}

@Composable
fun DailyOverview(
    halfDay: HalfDay,
    isDaytime: Boolean,
    modifier: Modifier = Modifier,
) {
    val builder = StringBuilder()
    val contentDescription = StringBuilder()
    val context = LocalContext.current
    val temperatureUnit: TemperatureUnit = SettingsManager.getInstance(context).temperatureUnit
    if (!halfDay.weatherText.isNullOrEmpty()) {
        builder.append(halfDay.weatherText)
        contentDescription.append(halfDay.weatherText)
    }
    halfDay.temperature?.temperature?.let {
        if (builder.toString().isNotEmpty()) {
            builder.append(context.getString(R.string.comma_separator))
            contentDescription.append(context.getString(R.string.comma_separator))
        }
        builder.append(temperatureUnit.getValueText(context, it))
        contentDescription.append(temperatureUnit.getValueVoice(context, it))
    }
    ListItem(
        leadingContent = if (halfDay.weatherCode != null) {
            val provider = ResourcesProviderFactory.newInstance
            {
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
        } else {
            null
        },
        headlineContent = {
            Text(
                text = builder.toString(),
                color = DayNightTheme.colors.titleColor,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.clearAndSetSemantics {
                    this.contentDescription = contentDescription.toString()
                }
            )
        },
        modifier = modifier
    )
}

@Composable
fun DailyWind(
    wind: Wind,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val speedUnit = SettingsManager.getInstance(context).speedUnit
    ListItem(
        leadingContent = {
            Image(
                painter = painterResource(R.drawable.ic_navigation),
                contentDescription = null,
                colorFilter = ColorFilter.tint(Color(wind.getColor(context))),
                modifier = Modifier
                    .size(dimensionResource(R.dimen.material_icon_size))
                    .rotate(
                        if (wind.degree != null && wind.degree != -1.0) {
                            wind.degree!!.toFloat() + 180f
                        } else {
                            0f
                        }
                    )
            )
        },
        headlineContent = {
            Column {
                wind.degree?.let { degree ->
                    DailyItem(
                        headlineText = stringResource(R.string.wind_direction),
                        supportingText = if (degree == -1.0 || degree % 45 == 0.0) {
                            wind.getDirection(context)
                        } else {
                            wind.getDirection(context) + " (" + (degree % 360).toInt() + "°)"
                        }!!,
                        supportingContentDescription = wind.getDirection(context, short = false)
                    )
                }
                DailyItem(
                    headlineText = stringResource(R.string.wind_speed),
                    supportingText = speedUnit.getValueText(context, wind.speed!!),
                    supportingContentDescription = speedUnit.getValueVoice(context, wind.speed!!)
                )
                wind.getStrength(context)?.let {
                    DailyItem(
                        headlineText = stringResource(R.string.wind_strength),
                        supportingText = it
                    )
                }
                if ((wind.gusts ?: 0.0) > 0) {
                    DailyItem(
                        headlineText = stringResource(R.string.wind_gusts),
                        supportingText = speedUnit.getValueText(context, wind.gusts!!)
                    )
                }
            }
        },
        modifier = modifier
    )
}

fun LazyListScope.dailyFeelsLikeTemperatures(
    context: Context,
    temperature: Temperature,
    normalsTemperature: Double?,
) {
    val temperatureUnit = SettingsManager.getInstance(context).temperatureUnit
    val temperatureItems = buildList {
        temperature.realFeelTemperature?.let {
            add(Pair(R.string.temperature_real_feel, it))
        }
        temperature.realFeelShaderTemperature?.let {
            add(Pair(R.string.temperature_real_feel_shade, it))
        }
        temperature.apparentTemperature?.let {
            add(Pair(R.string.temperature_apparent, it))
        }
        temperature.windChillTemperature?.let {
            add(Pair(R.string.temperature_wind_chill, it))
        }
        temperature.wetBulbTemperature?.let {
            add(Pair(R.string.temperature_wet_bulb, it))
        }
        normalsTemperature?.let {
            add(Pair(R.string.temperature_normal_short, it))
        }
    }
    gridItems(temperatureItems, nColumns = 3) { item ->
        DailyItem(
            headlineText = stringResource(item.first),
            supportingText = temperatureUnit.getValueText(context, item.second),
            supportingContentDescription = temperatureUnit.getValueVoice(context, item.second)
        )
    }
}

fun LazyListScope.dailyPrecipitation(
    context: Context,
    precipitation: Precipitation,
) {
    val precipitationUnit = SettingsManager.getInstance(context).precipitationUnit
    val precipitationItems = buildList {
        add(Pair(R.string.precipitation_total, precipitation.total!!))
        if ((precipitation.rain ?: 0.0) > 0) {
            add(Pair(R.string.precipitation_rain, precipitation.rain!!))
        }
        if ((precipitation.snow ?: 0.0) > 0) {
            add(Pair(R.string.precipitation_snow, precipitation.snow!!))
        }
        if ((precipitation.ice ?: 0.0) > 0) {
            add(Pair(R.string.precipitation_ice, precipitation.ice!!))
        }
        if ((precipitation.thunderstorm ?: 0.0) > 0) {
            add(Pair(R.string.precipitation_thunderstorm, precipitation.thunderstorm!!))
        }
    }
    gridItems(precipitationItems, nColumns = 3) { item ->
        DailyItem(
            headlineText = stringResource(item.first),
            supportingText = precipitationUnit.getValueText(context, item.second)
        )
    }
}

fun LazyListScope.dailyPrecipitationProbability(
    context: Context,
    precipitationProbability: PrecipitationProbability,
) {
    val percentUnit = NumberFormat.getPercentInstance(context.currentLocale).apply {
        maximumFractionDigits = 0
    }
    val precipitationProbabilityItems = buildList {
        add(Pair(R.string.precipitation_total, precipitationProbability.total!!))
        if ((precipitationProbability.rain ?: 0.0) > 0) {
            add(Pair(R.string.precipitation_rain, precipitationProbability.rain!!))
        }
        if ((precipitationProbability.snow ?: 0.0) > 0) {
            add(Pair(R.string.precipitation_snow, precipitationProbability.snow!!))
        }
        if ((precipitationProbability.ice ?: 0.0) > 0) {
            add(Pair(R.string.precipitation_ice, precipitationProbability.ice!!))
        }
        if ((precipitationProbability.thunderstorm ?: 0.0) > 0) {
            add(Pair(R.string.precipitation_thunderstorm, precipitationProbability.thunderstorm!!))
        }
    }
    gridItems(precipitationProbabilityItems, nColumns = 3) { item ->
        DailyItem(
            headlineText = stringResource(item.first),
            supportingText = percentUnit.format(item.second.div(100.0))
        )
    }
}

fun LazyListScope.dailyPrecipitationDuration(
    context: Context,
    precipitationDuration: PrecipitationDuration,
) {
    val durationUnit = DurationUnit.H
    val precipitationDurationItems = buildList {
        add(Pair(R.string.precipitation_total, precipitationDuration.total!!))
        if ((precipitationDuration.rain ?: 0.0) > 0) {
            add(Pair(R.string.precipitation_rain, precipitationDuration.rain!!))
        }
        if ((precipitationDuration.snow ?: 0.0) > 0) {
            add(Pair(R.string.precipitation_snow, precipitationDuration.snow!!))
        }
        if ((precipitationDuration.ice ?: 0.0) > 0) {
            add(Pair(R.string.precipitation_ice, precipitationDuration.ice!!))
        }
        if ((precipitationDuration.thunderstorm ?: 0.0) > 0) {
            add(Pair(R.string.precipitation_thunderstorm, precipitationDuration.thunderstorm!!))
        }
    }
    gridItems(precipitationDurationItems, nColumns = 3) { item ->
        DailyItem(
            headlineText = stringResource(item.first),
            supportingText = durationUnit.getValueText(context, item.second)
        )
    }
}

@Composable
fun DailyItem(
    headlineText: String,
    supportingText: String,
    modifier: Modifier = Modifier,
    supportingContentDescription: String? = null,
    @DrawableRes icon: Int? = null,
) {
    ListItem(
        leadingContent = if (icon != null) {
            {
                Image(
                    painter = painterResource(icon),
                    contentDescription = null,
                    colorFilter = ColorFilter.tint(DayNightTheme.colors.titleColor)
                )
            }
        } else {
            null
        },
        headlineContent = {
            Text(
                text = headlineText
            )
        },
        supportingContent = {
            Text(
                text = supportingText,
                color = DayNightTheme.colors.titleColor,
                fontWeight = FontWeight.Black,
                modifier = if (supportingContentDescription != null) {
                    Modifier.clearAndSetSemantics {
                        contentDescription = supportingContentDescription
                    }
                } else {
                    Modifier
                }
            )
        },
        modifier = modifier
    )
}

// FIXME: Weird dot at the end
@Composable
fun DailyAirQuality(
    airQuality: AirQuality,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val color = Color(airQuality.getColor(context))
    val aqi = airQuality.getIndex()!!
    // Our maximum is 400, while a value between 0.0 and 1.0 is expected
    val progress = aqi.div(400.0).let {
        // If greater than 1.0, just fulfill the bar
        if (it > 1.0) 1.0 else it
    }.toFloat()
    Column(modifier = modifier) {
        Text(
            text = stringResource(R.string.air_quality_index),
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
            text = "$aqi / ${airQuality.getName(context)}",
            style = MaterialTheme.typography.bodyMedium
        )
    }
}

@Composable
fun DailyUV(
    uv: UV,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    ListItem(
        leadingContent = {
            Image(
                painter = painterResource(R.drawable.ic_circle_medium),
                contentDescription = null,
                colorFilter = ColorFilter.tint(Color(uv.getUVColor(context)))
            )
        },
        headlineContent = {
            Text(
                text = uv.getShortDescription(context),
                color = DayNightTheme.colors.titleColor,
                fontWeight = FontWeight.Black,
                modifier = Modifier
                    .clearAndSetSemantics {
                        contentDescription = uv.getContentDescription(context)
                    }
            )
        },
        modifier = modifier
    )
}

@Composable
fun DailySun(
    location: Location,
    sun: Astro,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val sunriseTime = sun.riseDate?.getFormattedTime(location, context, context.is12Hour)
    val sunsetTime = sun.setDate?.getFormattedTime(location, context, context.is12Hour)
    ListItem(
        leadingContent = {
            Image(
                painter = painterResource(R.drawable.weather_clear_day_mini_xml),
                contentDescription = null,
                colorFilter = ColorFilter.tint(colorResource(R.color.colorTextContent))
            )
        },
        headlineContent = {
            Text(
                text = if (BreezyWeather.instance.debugMode) {
                    (
                        sun.riseDate?.getFormattedDate("yyyy-MM-dd HH:mm", location, context)
                            ?: context.getString(R.string.null_data_text)
                        ) +
                        "↑ / " +
                        (
                            sun.setDate?.getFormattedDate("yyyy-MM-dd HH:mm", location, context)
                                ?: context.getString(R.string.null_data_text)
                            ) +
                        "↓" +
                        (sun.duration?.let { " / " + DurationUnit.H.getValueText(context, it) } ?: "")
                } else {
                    (
                        sunriseTime ?: context.getString(R.string.null_data_text)
                        ) +
                        "↑ / " +
                        (
                            sunsetTime ?: context.getString(R.string.null_data_text)
                            ) +
                        "↓" +
                        (sun.duration?.let { " / " + DurationUnit.H.getValueText(context, it) } ?: "")
                },
                color = DayNightTheme.colors.titleColor,
                fontWeight = FontWeight.Black
            )
        },
        modifier = modifier
            .clearAndSetSemantics {
                val talkBackBuilder = StringBuilder()
                if (sunriseTime != null) {
                    talkBackBuilder.append(context.getString(R.string.ephemeris_sunrise_at, sunriseTime))
                }
                if (sunsetTime != null) {
                    if (talkBackBuilder.toString().isNotEmpty()) {
                        talkBackBuilder.append(context.getString(R.string.comma_separator))
                    }
                    talkBackBuilder.append(context.getString(R.string.ephemeris_sunset_at, sunsetTime))
                }
                sun.duration?.let {
                    if (talkBackBuilder.toString().isNotEmpty()) {
                        talkBackBuilder.append(context.getString(R.string.comma_separator))
                    }
                    talkBackBuilder.append(context.getString(R.string.sunshine_duration))
                    talkBackBuilder.append(DurationUnit.H.getValueVoice(context, it))
                }
                contentDescription = talkBackBuilder.toString()
            }
    )
}

@Composable
fun DailyMoon(
    location: Location,
    moon: Astro,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val moonriseTime = moon.riseDate?.getFormattedTime(location, context, context.is12Hour)
    val moonsetTime = moon.setDate?.getFormattedTime(location, context, context.is12Hour)
    ListItem(
        leadingContent = {
            Image(
                painter = painterResource(R.drawable.weather_clear_night_mini_xml),
                contentDescription = null,
                colorFilter = ColorFilter.tint(colorResource(R.color.colorTextContent))
            )
        },
        headlineContent = {
            Text(
                text = if (BreezyWeather.instance.debugMode) {
                    (
                        moon.riseDate?.getFormattedDate("yyyy-MM-dd HH:mm", location, context)
                            ?: context.getString(R.string.null_data_text)
                        ) +
                        "↑ / " +
                        (
                            moon.setDate?.getFormattedDate("yyyy-MM-dd HH:mm", location, context)
                                ?: context.getString(R.string.null_data_text)
                            ) +
                        "↓"
                } else {
                    (
                        moonriseTime ?: context.getString(R.string.null_data_text)
                        ) +
                        "↑ / " +
                        (
                            moonsetTime ?: context.getString(R.string.null_data_text)
                            ) +
                        "↓"
                },
                color = DayNightTheme.colors.titleColor,
                fontWeight = FontWeight.Black
            )
        },
        modifier = modifier
            .clearAndSetSemantics {
                val talkBackBuilder = StringBuilder()
                if (moonriseTime != null) {
                    talkBackBuilder.append(context.getString(R.string.ephemeris_moonrise_at, moonriseTime))
                }
                if (moonsetTime != null) {
                    if (talkBackBuilder.toString().isNotEmpty()) {
                        talkBackBuilder.append(context.getString(R.string.comma_separator))
                    }
                    talkBackBuilder.append(context.getString(R.string.ephemeris_moonset_at, moonsetTime))
                }
                contentDescription = talkBackBuilder.toString()
            }
    )
}

@Composable
fun DailyMoonPhase(
    moonPhase: MoonPhase,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    ListItem(
        leadingContent = {
            AndroidView(
                modifier = Modifier.size(dimensionResource(R.dimen.material_icon_size)),
                factory = {
                    MoonPhaseView(context).apply {
                        setSurfaceAngle(moonPhase.angle!!.toFloat())
                        setColor(
                            ContextCompat.getColor(context, R.color.colorTextLight2nd),
                            ContextCompat.getColor(context, R.color.colorTextDark2nd),
                            ThemeManager.getInstance(context).getThemeColor(context, R.attr.colorBodyText)
                        )
                    }
                }
            )
        },
        headlineContent = {
            Text(
                text = moonPhase.getDescription(context) ?: "",
                color = DayNightTheme.colors.titleColor,
                fontWeight = FontWeight.Black
            )
        },
        modifier = modifier
    )
}

fun <T> LazyListScope.gridItems(
    data: List<T>,
    nColumns: Int,
    horizontalArrangement: Arrangement.Horizontal = Arrangement.Start,
    key: ((item: T) -> Any)? = null,
    itemContent: @Composable BoxScope.(T) -> Unit,
) {
    val rows = if (data.isEmpty()) 0 else 1 + (data.count() - 1) / nColumns
    items(rows) { rowIndex ->
        Row(horizontalArrangement = horizontalArrangement) {
            for (columnIndex in 0 until nColumns) {
                val itemIndex = rowIndex * nColumns + columnIndex
                if (itemIndex < data.count()) {
                    val item = data[itemIndex]
                    androidx.compose.runtime.key(key?.invoke(item)) {
                        Box(
                            modifier = Modifier.weight(1f, fill = true),
                            propagateMinConstraints = true
                        ) {
                            itemContent.invoke(this, item)
                        }
                    }
                } else {
                    Spacer(Modifier.weight(1f, fill = true))
                }
            }
        }
    }
}
