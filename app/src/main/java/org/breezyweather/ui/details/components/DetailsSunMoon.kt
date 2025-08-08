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
import androidx.compose.material3.MaterialTheme
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
import org.breezyweather.common.extensions.getThemeColor
import org.breezyweather.common.extensions.is12Hour
import org.breezyweather.common.extensions.toCalendarWithTimeZone
import org.breezyweather.common.extensions.toDate
import org.breezyweather.common.extensions.toTimezoneSpecificHour
import org.breezyweather.domain.weather.model.getDescription
import org.breezyweather.ui.common.charts.EphemerisChart
import org.breezyweather.ui.common.widgets.Material3ExpressiveCardListItem
import org.breezyweather.ui.common.widgets.astro.MoonPhaseView
import org.breezyweather.ui.common.widgets.defaultCardListItemElevation
import org.breezyweather.ui.settings.preference.bottomInsetItem
import org.breezyweather.ui.theme.weatherView.materialWeatherView.MaterialWeatherThemeDelegate
import org.breezyweather.ui.theme.weatherView.materialWeatherView.implementor.MeteorShowerImplementor
import org.breezyweather.ui.theme.weatherView.materialWeatherView.implementor.SunImplementor
import org.shredzone.commons.suncalc.MoonPosition
import org.shredzone.commons.suncalc.SunPosition
import java.util.Calendar
import java.util.Date

@Composable
fun DetailsSunMoon(
    location: Location,
    today: Daily,
    sunTimes: List<Astro>,
    moonTimes: List<Astro>,
    modifier: Modifier = Modifier,
) {
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(
            horizontal = dimensionResource(R.dimen.normal_margin),
            vertical = dimensionResource(R.dimen.small_margin)
        )
    ) {
        if (today.sun?.isValid == true || today.moon?.isValid == true) {
            item {
                EphemerisChart(location, today, sunTimes, moonTimes)
            }
        }
        item {
            if (today.sun?.isValid == true || today.moon?.isValid == true || today.moonPhase?.isValid == true) {
                Spacer(modifier = Modifier.height(dimensionResource(R.dimen.normal_margin)))
                Material3ExpressiveCardListItem(
                    elevation = defaultCardListItemElevation,
                    isFirst = true,
                    isLast = true
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
    sunTimes: List<Astro>,
    moonTimes: List<Astro>,
) {
    val context = LocalContext.current

    val startingDate = remember(today) {
        today.date.toTimezoneSpecificHour(location.timeZone)
    }
    val endingDate = remember(today) {
        today.date.toCalendarWithTimeZone(location.timeZone).apply {
            add(Calendar.DAY_OF_MONTH, 1)
        }.time.toTimezoneSpecificHour(location.timeZone, 0)
    }

    val mappedSunValues = remember(today) {
        getMappedAstroValues(location, sunTimes, isSun = true, startingDate, endingDate)
    }

    val mappedMoonValues = remember(today) {
        getMappedAstroValues(location, moonTimes, isSun = false, startingDate, endingDate)
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
        { _, value, _ -> TemperatureUnit.CELSIUS.formatMeasureShort(context, value) }, // Hack
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
    astro: List<Astro>,
    isSun: Boolean,
    startingDate: Date,
    endingDate: Date,
): ImmutableMap<Long, Double> {
    return buildMap {
        val riseTimes = mutableListOf<Long>()
        val setTimes = mutableListOf<Long>()
        var riseCounter = 0
        var setCounter = 0
        var noon: Long
        var nadir: Long
        var halfUp: Long
        var halfDown: Long
        var altitude: Double
        astro.forEach {
            if (it.riseDate != null) {
                riseTimes.add(it.riseDate!!.time)
            }
            if (it.setDate != null) {
                setTimes.add(it.setDate!!.time)
            }
        }

        // Bookend the riseTimes and setTimes with an extra cycle on each end
        // so the charts on the first and the last day look complete
        if (riseTimes.size > 1 && setTimes.size > 1) {
            riseTimes.add(0, riseTimes[0] - (riseTimes[1] - riseTimes[0]))
            setTimes.add(0, setTimes[0] - (setTimes[1] - setTimes[0]))
            riseTimes.add(
                riseTimes[riseTimes.lastIndex] + (riseTimes[riseTimes.lastIndex] - riseTimes[riseTimes.lastIndex - 1])
            )
            setTimes.add(
                setTimes[setTimes.lastIndex] + (setTimes[setTimes.lastIndex] - setTimes[setTimes.lastIndex - 1])
            )
        }

        // Seek last rise/set prior to startingDate to be the starting counter,
        // so we don't waste computation on noons and nadirs that won't show on the chart
        while (riseCounter < riseTimes.size && riseTimes[riseCounter] < startingDate.time) {
            riseCounter++
        }
        if (riseCounter > 0) {
            riseCounter--
        }
        while (setCounter < setTimes.size && setTimes[setCounter] < startingDate.time) {
            setCounter++
        }
        if (setCounter > 0) {
            setCounter--
        }

        // Seek the first rise/set after endingDate to be the ending counter,
        // so we don't waste computation on noons and nadirs that won't show on the chart
        var riseCounterLimit: Int = riseTimes.lastIndex
        var setCounterLimit: Int = setTimes.lastIndex
        while (riseCounterLimit >= 0 && riseTimes[riseCounterLimit] > endingDate.time) {
            riseCounterLimit--
        }
        if (riseCounterLimit < riseTimes.lastIndex) {
            riseCounterLimit++
        }
        while (setCounterLimit >= 0 && setTimes[setCounterLimit] > endingDate.time) {
            setCounterLimit--
        }
        if (setCounterLimit < setTimes.lastIndex) {
            setCounterLimit++
        }

        // loop within our set boundary of rise and set times
        while (riseCounter <= riseCounterLimit && setCounter <= setCounterLimit) {
            if (riseTimes[riseCounter] < setTimes[setCounter]) {
                // calculate noon
                halfUp = setTimes[setCounter].minus(riseTimes[riseCounter]).div(2)
                noon = riseTimes[riseCounter].plus(halfUp)
                altitude = getAltitude(location, noon.toDate(), isSun)
                put(noon, altitude)
                riseCounter++
            } else {
                // calculate nadir
                halfDown = riseTimes[riseCounter].minus(setTimes[setCounter]).div(2)
                nadir = setTimes[setCounter].plus(halfDown)
                altitude = getAltitude(location, nadir.toDate(), isSun)
                put(nadir, altitude)
                setCounter++
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
                        (sun.duration?.let { " / " + DurationUnit.HOUR.formatMeasure(context, it) } ?: "")
                } else {
                    (
                        sunriseTime ?: context.getString(R.string.null_data_text)
                        ) +
                        "↑ / " +
                        (
                            sunsetTime ?: context.getString(R.string.null_data_text)
                            ) +
                        "↓" +
                        (sun.duration?.let { " / " + DurationUnit.HOUR.formatMeasure(context, it) } ?: "")
                },
                color = MaterialTheme.colorScheme.onSurface
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
                    talkBackBuilder.append(DurationUnit.HOUR.formatContentDescription(context, it))
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
                color = MaterialTheme.colorScheme.onSurface
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
                color = MaterialTheme.colorScheme.onSurface
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
                            context.getThemeColor(R.attr.colorBodyText)
                        )
                    }
                }
            )
        },
        headlineContent = {
            Text(
                text = moonPhase.getDescription(context) ?: "",
                color = MaterialTheme.colorScheme.onSurface
            )
        },
        tonalElevation = defaultCardListItemElevation,
        modifier = modifier
    )
}
