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
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.PrimaryScrollableTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import breezyweather.domain.location.model.Location
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.launch
import org.breezyweather.R
import org.breezyweather.common.basic.models.options.appearance.CalendarHelper
import org.breezyweather.common.basic.models.options.appearance.ChartDisplay
import org.breezyweather.common.extensions.getDayOfMonth
import org.breezyweather.common.extensions.getFormattedDate
import org.breezyweather.common.extensions.getFormattedFullDayAndMonth
import org.breezyweather.common.extensions.getFormattedMediumDayAndMonthInAdditionalCalendar
import org.breezyweather.common.extensions.getWeek
import org.breezyweather.common.extensions.toCalendar
import org.breezyweather.common.extensions.toCalendarWithTimeZone
import org.breezyweather.common.extensions.toTimezoneSpecificHour
import org.breezyweather.common.source.PollenIndexSource
import org.breezyweather.ui.common.widgets.Material3Scaffold
import org.breezyweather.ui.common.widgets.insets.BWCenterAlignedTopAppBar
import org.breezyweather.ui.daily.components.DailyAirQuality
import org.breezyweather.ui.daily.components.DailyCloudCover
import org.breezyweather.ui.daily.components.DailyConditions
import org.breezyweather.ui.daily.components.DailyHumidity
import org.breezyweather.ui.daily.components.DailyPollen
import org.breezyweather.ui.daily.components.DailyPrecipitation
import org.breezyweather.ui.daily.components.DailyPressure
import org.breezyweather.ui.daily.components.DailySunMoon
import org.breezyweather.ui.daily.components.DailyUV
import org.breezyweather.ui.daily.components.DailyVisibility
import org.breezyweather.ui.daily.components.DailyWind
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
                            selectedChart = dailyUiState.selectedChart,
                            setSelectedChart = { chart -> dailyViewModel.setSelectedChart(chart) },
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
    PrimaryScrollableTabRow(
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
fun DailyDropdownMenu(
    location: Location,
    selectedChart: ChartDisplay,
    setSelectedChart: (ChartDisplay) -> Unit,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    var expanded by remember { mutableStateOf(false) }
    val chartDisplayEntries = remember(location) {
        ChartDisplay.toChartDisplayList(location)
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
    ) {
        DropdownMenuItem(
            leadingIcon = {
                Icon(
                    painterResource(selectedChart.iconId),
                    contentDescription = null,
                    modifier = Modifier.size(24.dp)
                )
            },
            text = {
                Text(
                    selectedChart.getName(context),
                    style = MaterialTheme.typography.titleLarge
                )
            },
            trailingIcon = {
                Icon(
                    Icons.Filled.KeyboardArrowDown,
                    contentDescription = null
                )
            },
            onClick = {
                expanded = !expanded
            }
        )
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            chartDisplayEntries
                .forEach { option ->
                    DropdownMenuItem(
                        leadingIcon = {
                            Icon(
                                painterResource(option.iconId),
                                contentDescription = null,
                                modifier = Modifier.size(24.dp)
                            )
                        },
                        text = { Text(option.getName(context)) },
                        onClick = {
                            setSelectedChart(option)
                            expanded = false
                        }
                    )
                }
        }
    }
}

@Composable
fun DailyPagerContent(
    location: Location,
    selected: Int,
    selectedChart: ChartDisplay,
    setSelectedChart: (ChartDisplay) -> Unit,
    pollenIndexSource: PollenIndexSource?,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val daily = remember(selected) {
        location.weather!!.dailyForecast[selected]
    }
    val yesterday = remember(selected) {
        if (selected > 0) {
            location.weather!!.dailyForecast[selected - 1]
        } else {
            null
        }
    }
    val hourlyList = remember(selected) {
        val startingDate = daily.date.toTimezoneSpecificHour(location.javaTimeZone, 0)
        val endingDate = daily.date.toCalendarWithTimeZone(location.javaTimeZone).apply {
            add(Calendar.DAY_OF_MONTH, 1)
        }.time.toTimezoneSpecificHour(location.javaTimeZone, 0)

        val firstHourlyIndex = location.weather!!.hourlyForecast.indexOfFirst {
            it.date >= startingDate
        }.let {
            if (it > 0 && location.weather!!.hourlyForecast[it].date > startingDate) it - 1 else it
        }
        if (firstHourlyIndex == -1) return@remember persistentListOf()

        val lastHourlyIndex = location.weather!!.hourlyForecast.indexOfFirst {
            it.date >= endingDate
        }.let { if (it == -1) location.weather!!.hourlyForecast.size - 1 else it }

        // We are doing a subList to take into account 3-hourly/6-hourly
        // For example, a complete chart can be from 02:00 to 02:00 the next day
        location.weather!!.hourlyForecast.subList(firstHourlyIndex, lastHourlyIndex + 1).toImmutableList()
    }

    Column(
        modifier = modifier
    ) {
        if (CalendarHelper.getAlternateCalendarSetting(context) != null) {
            daily.date.getFormattedMediumDayAndMonthInAdditionalCalendar(location, context)?.let {
                Text(
                    text = it,
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier
                        .fillMaxWidth()
                        .wrapContentWidth(align = Alignment.CenterHorizontally)
                        .padding(dimensionResource(R.dimen.little_margin))
                )
            }
        }
        DailyDropdownMenu(location, selectedChart, setSelectedChart)
        when (selectedChart) {
            ChartDisplay.TAG_CONDITIONS -> {
                val cal = daily.date.toCalendarWithTimeZone(location.javaTimeZone)
                val thisDayNormals = if (location.weather?.normals?.month == cal[Calendar.MONTH]) {
                    location.weather!!.normals
                } else {
                    null
                }
                DailyConditions(location, hourlyList, daily, thisDayNormals)
            }
            ChartDisplay.TAG_PRECIPITATION -> DailyPrecipitation(location, hourlyList, daily)
            ChartDisplay.TAG_WIND -> DailyWind(location, hourlyList, daily)
            ChartDisplay.TAG_AIR_QUALITY -> DailyAirQuality(location, hourlyList, daily)
            ChartDisplay.TAG_POLLEN -> DailyPollen(daily.pollen, pollenIndexSource)
            ChartDisplay.TAG_UV_INDEX -> DailyUV(location, hourlyList, daily)
            ChartDisplay.TAG_HUMIDITY -> DailyHumidity(location, hourlyList, daily.date)
            ChartDisplay.TAG_PRESSURE -> DailyPressure(location, hourlyList, daily.date)
            ChartDisplay.TAG_CLOUD_COVER -> DailyCloudCover(location, hourlyList, daily)
            ChartDisplay.TAG_VISIBILITY -> DailyVisibility(location, hourlyList, daily.date)
            ChartDisplay.TAG_SUN_MOON -> DailySunMoon(location, daily, yesterday)
        }
    }
}
