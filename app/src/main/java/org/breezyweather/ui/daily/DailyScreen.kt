/**
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

package org.breezyweather.ui.daily

import androidx.activity.compose.LocalActivity
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.windowInsetsBottomHeight
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.lifecycle.viewmodel.compose.viewModel
import breezyweather.domain.location.model.Location
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.launch
import org.breezyweather.R
import org.breezyweather.common.basic.models.options.appearance.CalendarHelper
import org.breezyweather.common.basic.models.options.unit.DurationUnit
import org.breezyweather.common.extensions.getDayOfMonth
import org.breezyweather.common.extensions.getFormattedDate
import org.breezyweather.common.extensions.getFormattedFullDayAndMonth
import org.breezyweather.common.extensions.getFormattedMediumDayAndMonthInAdditionalCalendar
import org.breezyweather.common.extensions.getWeek
import org.breezyweather.common.extensions.toCalendar
import org.breezyweather.common.extensions.toCalendarWithTimeZone
import org.breezyweather.common.source.PollenIndexSource
import org.breezyweather.domain.settings.SettingsManager
import org.breezyweather.domain.weather.model.isIndexValid
import org.breezyweather.ui.common.composables.PollenGrid
import org.breezyweather.ui.common.widgets.Material3Scaffold
import org.breezyweather.ui.common.widgets.insets.BWCenterAlignedTopAppBar
import org.breezyweather.ui.main.utils.MainThemeColorProvider
import org.breezyweather.ui.theme.ThemeManager
import org.breezyweather.ui.theme.compose.BreezyWeatherTheme
import java.util.Calendar
import java.util.Date

@Composable
internal fun DailyWeatherScreen(
    onBackPressed: () -> Unit,
    dailyViewModel: DailyViewModel = viewModel(),
) {
    val dailyUiState by dailyViewModel.uiState.collectAsState()

    val context = LocalContext.current
    val activity = LocalActivity.current

    val isLightTheme = MainThemeColorProvider.isLightTheme(context, dailyUiState.location)
    LaunchedEffect(dailyUiState.location) {
        // re-setting the status bar color once the location is fetched
        if (dailyUiState.location != null && activity != null) {
            ThemeManager
                .getInstance(context)
                .weatherThemeDelegate
                .setSystemBarStyle(
                    window = activity.window,
                    statusShader = false,
                    lightStatus = isLightTheme,
                    lightNavigation = isLightTheme
                )
        }
    }

    BreezyWeatherTheme(lightTheme = isLightTheme) {
        Material3Scaffold(
            topBar = {
                BWCenterAlignedTopAppBar(
                    title = stringResource(R.string.daily_forecast),
                    onBackPressed = onBackPressed
                )
            }
        ) { paddings ->
            dailyUiState.location?.let { loc ->
                val scope = rememberCoroutineScope()
                val pages = remember(loc.weather!!.dailyForecast) {
                    loc.weather!!.dailyForecast.map { it.date }.toImmutableList()
                }
                val pagerState = rememberPagerState(initialPage = dailyUiState.initialIndex) {
                    loc.weather!!.dailyForecast.size
                }
                val pagerPage by remember {
                    derivedStateOf { pagerState.currentPage }
                }
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(
                            top = paddings.calculateTopPadding(),
                            start = paddings.calculateStartPadding(LocalLayoutDirection.current),
                            end = paddings.calculateEndPadding(LocalLayoutDirection.current)
                        )
                ) {
                    DailyPagerIndicator(
                        pages = pages,
                        selected = pagerPage,
                        location = loc,
                        onClick = {
                            scope.launch {
                                pagerState.animateScrollToPage(it)
                            }
                        },
                        todayIndex = loc.weather!!.todayIndex
                    )
                    HorizontalPager(state = pagerState) { page ->
                        DailyPagerContent(
                            location = loc,
                            selected = page,
                            pollenIndexSource = dailyViewModel.getPollenIndexSource(loc)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun DailyPagerIndicator(
    pages: ImmutableList<Date>,
    selected: Int,
    location: Location,
    onClick: (index: Int) -> Unit,
    modifier: Modifier = Modifier,
    todayIndex: Int? = null,
) {
    val context = LocalContext.current
    ScrollableTabRow(
        selectedTabIndex = selected,
        modifier = modifier.fillMaxWidth()
    ) {
        pages.forEachIndexed { i, date ->
            val cal = date.toCalendar(location)
            Tab(
                selected = (selected == i),
                unselectedContentColor = MaterialTheme.colorScheme.onSurfaceVariant,
                onClick = {
                    onClick(i)
                },
                text = {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .clearAndSetSemantics {
                                contentDescription = when {
                                    todayIndex == null -> date.getWeek(location, context, full = true)
                                    i == todayIndex - 1 -> context.getString(R.string.daily_yesterday)
                                    i == todayIndex -> context.getString(R.string.daily_today)
                                    i == todayIndex + 1 -> context.getString(R.string.daily_tomorrow)
                                    else -> date.getWeek(location, context, full = true)
                                } + " " + date.getFormattedFullDayAndMonth(location, context)
                            }
                    ) {
                        Text(
                            text = when {
                                todayIndex == null -> date.getWeek(location, context)
                                i == todayIndex - 1 -> stringResource(R.string.daily_yesterday_short)
                                i == todayIndex -> stringResource(R.string.daily_today_short)
                                i == todayIndex + 1 -> stringResource(R.string.daily_tomorrow_short)
                                else -> date.getWeek(location, context)
                            }
                        )
                        Text(
                            text = cal.getDayOfMonth(twoDigits = true),
                            style = MaterialTheme.typography.headlineMedium
                        )
                        Text(
                            text = date.getFormattedDate("MMM", location, context, withBestPattern = true)
                        )
                    }
                }
            )
        }
    }
}

@Composable
fun DailyPagerContent(
    location: Location,
    selected: Int,
    pollenIndexSource: PollenIndexSource?,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val cal = location.weather!!.dailyForecast[selected].date.toCalendarWithTimeZone(location.javaTimeZone)
    val thisDayNormals = if (location.weather?.normals?.month == cal[Calendar.MONTH]) {
        location.weather!!.normals
    } else {
        null
    }
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(
            dimensionResource(R.dimen.normal_margin)
        )
    ) {
        val daily = location.weather!!.dailyForecast[selected]

        if (CalendarHelper.getAlternateCalendarSetting(context) != null) {
            daily.date.getFormattedMediumDayAndMonthInAdditionalCalendar(location, context)?.let {
                item {
                    DailyTitle(
                        it,
                        modifier = Modifier
                            .fillMaxWidth()
                            .wrapContentWidth(align = Alignment.CenterHorizontally),
                        style = MaterialTheme.typography.titleLarge
                    )
                }
                item {
                    Spacer(modifier = Modifier.height(dimensionResource(R.dimen.normal_margin)))
                }
                item {
                    HorizontalDivider()
                }
                item {
                    Spacer(modifier = Modifier.height(dimensionResource(R.dimen.normal_margin)))
                }
            }
        }

        daily.day?.let { day ->
            dailyHalfDay(context, day, true, thisDayNormals)
        }
        daily.night?.let { night ->
            item {
                Spacer(modifier = Modifier.height(dimensionResource(R.dimen.normal_margin)))
            }
            dailyHalfDay(context, night, false, thisDayNormals)
        }
        daily.airQuality?.let { airQuality ->
            if (airQuality.isIndexValid) {
                item {
                    DailyTitle(
                        icon = R.drawable.weather_haze_mini_xml,
                        text = stringResource(R.string.air_quality)
                    )
                }
                item {
                    DailyAirQuality(airQuality)
                }
            }
        }
        daily.pollen?.let { pollen ->
            if (pollen.isIndexValid) {
                item {
                    DailyTitle(
                        icon = R.drawable.ic_allergy,
                        text = stringResource(if (pollen.isMoldValid) R.string.pollen_and_mold else R.string.pollen)
                    )
                }
                item {
                    PollenGrid(
                        pollen = pollen,
                        pollenIndexSource = pollenIndexSource
                    )
                }
            }
        }
        daily.uV?.let { uV ->
            if (uV.isValid) {
                item {
                    DailyTitle(
                        icon = R.drawable.ic_uv,
                        text = stringResource(R.string.uv_index)
                    )
                }
                item {
                    DailyUV(uV)
                }
            }
        }
        if (daily.sun?.isValid == true || daily.moon?.isValid == true || daily.moonPhase?.isValid == true) {
            item {
                DailyTitle(
                    text = stringResource(R.string.ephemeris),
                    style = MaterialTheme.typography.titleMedium
                )
            }
            if (daily.sun != null && daily.sun!!.isValid) {
                item {
                    DailySun(location, daily.sun!!)
                }
            }
            if (daily.moon != null && daily.moon!!.isValid) {
                item {
                    DailyMoon(location, daily.moon!!)
                }
            }
            if (daily.moonPhase != null && daily.moonPhase!!.isValid) {
                item {
                    DailyMoonPhase(daily.moonPhase!!)
                }
            }
        }
        if (daily.degreeDay?.isValid == true || daily.sunshineDuration != null) {
            item {
                HorizontalDivider()
            }
            item {
                Spacer(modifier = Modifier.height(dimensionResource(R.dimen.normal_margin)))
            }
            item {
                DailyTitle(
                    text = stringResource(R.string.details),
                    style = MaterialTheme.typography.titleMedium
                )
            }
            daily.degreeDay?.let { degreeDay ->
                if (degreeDay.isValid) {
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
            daily.sunshineDuration?.let { sunshineDuration ->
                item {
                    DailyItem(
                        headlineText = stringResource(R.string.sunshine_duration),
                        supportingText = DurationUnit.H.getValueText(context, sunshineDuration),
                        icon = R.drawable.ic_sunshine_duration
                    )
                }
            }
        }
        item {
            Spacer(modifier = Modifier.windowInsetsBottomHeight(WindowInsets.navigationBars))
        }
    }
}
