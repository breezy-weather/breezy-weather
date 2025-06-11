package org.breezyweather.ui.daily.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.ListItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import breezyweather.domain.location.model.Location
import breezyweather.domain.weather.model.Astro
import breezyweather.domain.weather.model.Daily
import breezyweather.domain.weather.model.MoonPhase
import org.breezyweather.BreezyWeather
import org.breezyweather.R
import org.breezyweather.common.basic.models.options.unit.DurationUnit
import org.breezyweather.common.extensions.getFormattedDate
import org.breezyweather.common.extensions.getFormattedTime
import org.breezyweather.common.extensions.is12Hour
import org.breezyweather.domain.weather.model.getDescription
import org.breezyweather.ui.common.widgets.Material3CardListItem
import org.breezyweather.ui.common.widgets.astro.MoonPhaseView
import org.breezyweather.ui.common.widgets.defaultCardListItemElevation
import org.breezyweather.ui.theme.ThemeManager
import org.breezyweather.ui.theme.compose.DayNightTheme

@Composable
fun DailySunMoon(
    location: Location,
    daily: Daily,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .fillMaxSize()
    ) {
        if (daily.sun?.isValid == true || daily.moon?.isValid == true || daily.moonPhase?.isValid == true) {
            Material3CardListItem(
                elevation = defaultCardListItemElevation
            ) {
                if (daily.sun?.isValid == true) {
                    DailySun(location, daily.sun!!)
                }
                if (daily.moon?.isValid == true) {
                    DailyMoon(location, daily.moon!!)
                }
                if (daily.moonPhase?.isValid == true) {
                    DailyMoonPhase(daily.moonPhase!!)
                }
            }
        } else {
            Text(
                text = stringResource(R.string.chart_no_daily_data),
                fontStyle = FontStyle.Italic,
                modifier = Modifier.padding(dimensionResource(R.dimen.normal_margin))
            )
        }
    }
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
                color = DayNightTheme.colors.titleColor
            )
        },
        tonalElevation = defaultCardListItemElevation,
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
                color = DayNightTheme.colors.titleColor
            )
        },
        tonalElevation = defaultCardListItemElevation,
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
                color = DayNightTheme.colors.titleColor
            )
        },
        tonalElevation = defaultCardListItemElevation,
        modifier = modifier
    )
}
