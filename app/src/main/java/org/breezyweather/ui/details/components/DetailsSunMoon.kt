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

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.ListItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
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
import com.patrykandpatrick.vico.core.cartesian.data.CartesianChartModelProducer
import com.patrykandpatrick.vico.core.cartesian.data.lineSeries
import kotlinx.collections.immutable.ImmutableMap
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableMap
import org.breezyweather.BreezyWeather
import org.breezyweather.R
import org.breezyweather.common.basic.models.options.unit.DurationUnit
import org.breezyweather.common.basic.models.options.unit.TemperatureUnit
import org.breezyweather.common.extensions.getFormattedDate
import org.breezyweather.common.extensions.getFormattedTime
import org.breezyweather.common.extensions.is12Hour
import org.breezyweather.common.extensions.toCalendarWithTimeZone
import org.breezyweather.common.extensions.toDate
import org.breezyweather.common.extensions.toTimezoneSpecificHour
import org.breezyweather.domain.weather.model.getDescription
import org.breezyweather.ui.common.charts.EphemerisChart
import org.breezyweather.ui.common.widgets.Material3CardListItem
import org.breezyweather.ui.common.widgets.astro.MoonPhaseView
import org.breezyweather.ui.common.widgets.defaultCardListItemElevation
import org.breezyweather.ui.settings.preference.bottomInsetItem
import org.breezyweather.ui.theme.ThemeManager
import org.breezyweather.ui.theme.compose.DayNightTheme
import org.breezyweather.ui.theme.weatherView.materialWeatherView.MaterialWeatherThemeDelegate
import org.breezyweather.ui.theme.weatherView.materialWeatherView.implementor.MeteorShowerImplementor
import org.breezyweather.ui.theme.weatherView.materialWeatherView.implementor.SunImplementor
import org.shredzone.commons.suncalc.MoonPosition
import org.shredzone.commons.suncalc.SunPosition
import java.util.Calendar
import java.util.Date
import kotlin.time.Duration.Companion.seconds

@Composable
fun DetailsSunMoon(
    location: Location,
    today: Daily,
    yesterday: Daily? = null,
    modifier: Modifier = Modifier,
) {
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(
            horizontal = dimensionResource(R.dimen.normal_margin),
            vertical = dimensionResource(R.dimen.little_margin)
        )
    ) {
        if (today.sun?.isValid == true || today.moon?.isValid == true) {
            item {
                EphemerisChart(location, today, yesterday)
            }
        }
        item {
            if (today.sun?.isValid == true || today.moon?.isValid == true || today.moonPhase?.isValid == true) {
                Spacer(modifier = Modifier.height(dimensionResource(R.dimen.normal_margin)))
                Material3CardListItem(
                    elevation = defaultCardListItemElevation,
                    withPadding = false
                ) {
                    if (today.sun?.isValid == true) {
                        DailySun(location, today.sun!!)
                    }
                    if (today.twilight?.isValid == true) {
                        DailyTwilight(location, today.twilight!!)
                    }
                    if (today.moon?.isValid == true) {
                        DailyMoon(location, today.moon!!)
                    }
                    if (today.moonPhase?.isValid == true) {
                        DailyMoonPhase(today.moonPhase!!)
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
        item {
            Spacer(modifier = Modifier.height(dimensionResource(R.dimen.normal_margin)))
        }
        item {
            DetailsSectionHeader(stringResource(R.string.ephemeris_about))
        }
        item {
            DetailsCardText(
                stringResource(R.string.ephemeris_about_rise) +
                    " " +
                    stringResource(R.string.ephemeris_about_set) +
                    "\n\n" +
                    stringResource(R.string.ephemeris_about_dawn) +
                    " " +
                    stringResource(R.string.ephemeris_about_dusk)
            )
        }
        bottomInsetItem()
    }
}

@Composable
fun EphemerisChart(
    location: Location,
    today: Daily,
    yesterday: Daily?,
) {
    val context = LocalContext.current

    val startingDate = remember(today) {
        today.date.toTimezoneSpecificHour(location.javaTimeZone)
    }
    val endingDate = remember(today) {
        today.date.toCalendarWithTimeZone(location.javaTimeZone).apply {
            add(Calendar.DAY_OF_MONTH, 1)
        }.time.toTimezoneSpecificHour(location.javaTimeZone, 0)
    }

    val mappedSunValues = remember(today) {
        getMappedAstroValues(location, today.sun, isSun = true, startingDate, endingDate)
    }

    val mappedMoonValues = remember(today) {
        getMappedAstroValues(
            location,
            today.moon,
            isSun = false,
            startingDate,
            endingDate,
            previousSetDate = yesterday?.moon?.setDate
        )
    }

    val modelProducer = remember { CartesianChartModelProducer() }

    LaunchedEffect(location) {
        modelProducer.runTransaction {
            lineSeries {
                if (mappedMoonValues.isNotEmpty()) {
                    series(
                        x = mappedMoonValues.keys,
                        y = mappedMoonValues.values
                    )
                }
                if (mappedSunValues.isNotEmpty()) {
                    series(
                        x = mappedSunValues.keys,
                        y = mappedSunValues.values
                    )
                }
            }
        }
    }

    EphemerisChart(
        location,
        modelProducer,
        { _, value, _ -> TemperatureUnit.C.getShortValueText(context, value) }, // Hack
        lineColors = if (mappedMoonValues.isNotEmpty()) {
            persistentListOf(
                MaterialWeatherThemeDelegate.getBrighterColor(MeteorShowerImplementor.themeColor), // Moon
                SunImplementor.themeColor // Sun
            )
        } else {
            persistentListOf(
                SunImplementor.themeColor // Sun
            )
        },
        startingDate = startingDate.time,
        endingDate = endingDate.time
    )
}

internal fun getMappedAstroValues(
    location: Location,
    astro: Astro?,
    isSun: Boolean,
    startingDate: Date,
    endingDate: Date,
    previousSetDate: Date? = null,
): ImmutableMap<Long, Double> {
    return buildMap {
        if (astro?.isValid == true) {
            val halfDaylightDuration = astro.setDate!!.time.minus(astro.riseDate!!.time).div(2)
            val maxAltitudeTime = astro.setDate!!.time.minus(halfDaylightDuration)
            val maxAltitude = getAltitude(location, maxAltitudeTime.toDate(), isSun)

            // 00:00 start of the day
            val altitudeAtStartDate = if (astro.riseDate == startingDate ||
                (previousSetDate != null && previousSetDate > startingDate)
            ) {
                getAltitude(location, startingDate, isSun)
            } else {
                val startingReversedMinAltitudeTime = astro.riseDate!!.time.minus(halfDaylightDuration)
                val slope = (maxAltitude - maxAltitude.times(-1))
                    .div(maxAltitudeTime - startingReversedMinAltitudeTime)

                slope * (startingDate.time - startingReversedMinAltitudeTime) + maxAltitude.times(-1)
                // Already on the line, not adding to avoid curving issues:
                /*put(
                    astro.riseDate!!.time,
                    0.0
                )*/
            }
            put(
                startingDate.time,
                altitudeAtStartDate
            )

            if (previousSetDate != null && previousSetDate > startingDate) {
                val interceptionCoordinates = interceptionPoint(
                    startingDate.time.toDouble(),
                    altitudeAtStartDate,
                    previousSetDate.time.toDouble(),
                    0.0,
                    astro.riseDate!!.time.toDouble(),
                    0.0,
                    maxAltitudeTime.toDouble(),
                    maxAltitude
                )
                put(
                    interceptionCoordinates.first.toLong(),
                    interceptionCoordinates.second
                )
            }

            // Max altitude
            put(
                maxAltitudeTime,
                maxAltitude
            )

            // 00:00 end of the day
            // 23:59:59 is used with our own converter, some other sources put 00:00 next day instead
            // TODO: Consider only using our own internal computing for consistency
            if (astro.setDate!!.time.plus(1.seconds.inWholeMilliseconds) == endingDate.time ||
                astro.setDate!!.time == endingDate.time
            ) {
                put(
                    endingDate.time,
                    getAltitude(location, endingDate, isSun)
                )
            } else {
                val endingReversedMinAltitudeTime = astro.setDate!!.time.plus(halfDaylightDuration)
                val slope = (maxAltitude - maxAltitude.times(-1))
                    .div(maxAltitudeTime - endingReversedMinAltitudeTime)
                put(
                    endingDate.time,
                    slope * (endingDate.time - endingReversedMinAltitudeTime) + maxAltitude.times(-1)
                )
                // Already on the line, not adding to avoid curving issues:
                /*put(
                    astro.setDate!!.time,
                    0.0
                )*/
            }
        }
    }.toImmutableMap()
}

private fun getAltitude(
    location: Location,
    date: Date,
    isSun: Boolean,
): Double {
    return if (isSun) {
        SunPosition.compute().on(date).at(location.latitude, location.longitude).execute().altitude
    } else {
        MoonPosition.compute().on(date).at(location.latitude, location.longitude).execute().altitude
    }
}

private fun interceptionPoint(
    s1x: Double,
    s1y: Double,
    s2x: Double,
    s2y: Double,
    d1x: Double,
    d1y: Double,
    d2x: Double,
    d2y: Double,
): Pair<Double, Double> {
    val a1 = s2y - s1y
    val b1 = s1x - s2x
    val c1 = a1 * s1x + b1 * s1y

    val a2 = d2y - d1y
    val b2 = d1x - d2x
    val c2 = a2 * d1x + b2 * d1y

    val delta = a1 * b2 - a2 * b1
    return Pair(
        (b2 * c1 - b1 * c2) / delta,
        (a1 * c2 - a2 * c1) / delta
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
fun DailyTwilight(
    location: Location,
    twilight: Astro,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val dawn = twilight.riseDate?.getFormattedTime(location, context, context.is12Hour)
    val dusk = twilight.setDate?.getFormattedTime(location, context, context.is12Hour)
    ListItem(
        leadingContent = {
            Image(
                painter = painterResource(R.drawable.ic_twilight),
                contentDescription = null,
                colorFilter = ColorFilter.tint(colorResource(R.color.colorTextContent))
            )
        },
        headlineContent = {
            Text(
                text = if (BreezyWeather.instance.debugMode) {
                    (
                        twilight.riseDate?.getFormattedDate("yyyy-MM-dd HH:mm", location, context)
                            ?: context.getString(R.string.null_data_text)
                        ) +
                        "↑ / " +
                        (
                            twilight.setDate?.getFormattedDate("yyyy-MM-dd HH:mm", location, context)
                                ?: context.getString(R.string.null_data_text)
                            ) +
                        "↓"
                } else {
                    (
                        dawn ?: context.getString(R.string.null_data_text)
                        ) +
                        "↑ / " +
                        (
                            dusk ?: context.getString(R.string.null_data_text)
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
                if (dawn != null) {
                    talkBackBuilder.append(context.getString(R.string.ephemeris_dawn_at, dawn))
                }
                if (dusk != null) {
                    if (talkBackBuilder.toString().isNotEmpty()) {
                        talkBackBuilder.append(context.getString(R.string.comma_separator))
                    }
                    talkBackBuilder.append(context.getString(R.string.ephemeris_dusk_at, dusk))
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
