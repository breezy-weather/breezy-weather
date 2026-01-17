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

package org.breezyweather.ui.details

import androidx.activity.compose.BackHandler
import androidx.activity.compose.LocalActivity
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.FabPosition
import androidx.compose.material3.FloatingActionButtonMenu
import androidx.compose.material3.FloatingActionButtonMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.PrimaryScrollableTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.ToggleFloatingActionButton
import androidx.compose.material3.ToggleFloatingActionButtonDefaults.animateIcon
import androidx.compose.material3.animateFloatingActionButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.CustomAccessibilityAction
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.customActions
import androidx.compose.ui.semantics.isTraversalGroup
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.semantics.traversalIndex
import androidx.lifecycle.viewmodel.compose.viewModel
import breezyweather.domain.location.model.Location
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.launch
import org.breezyweather.R
import org.breezyweather.common.extensions.getCalendarMonth
import org.breezyweather.common.extensions.getFormattedDate
import org.breezyweather.common.extensions.getFormattedDayOfTheMonth
import org.breezyweather.common.extensions.getFormattedFullDayAndMonth
import org.breezyweather.common.extensions.getFormattedMediumDayAndMonthInAdditionalCalendar
import org.breezyweather.common.extensions.getWeek
import org.breezyweather.common.extensions.setSystemBarStyle
import org.breezyweather.common.extensions.toCalendarWithTimeZone
import org.breezyweather.common.extensions.toTimezoneSpecificHour
import org.breezyweather.common.options.appearance.CalendarHelper
import org.breezyweather.common.options.appearance.DetailScreen
import org.breezyweather.common.source.PollenIndexSource
import org.breezyweather.domain.weather.index.PollutantIndex
import org.breezyweather.domain.weather.model.getConcentration
import org.breezyweather.domain.weather.model.isToday
import org.breezyweather.ui.common.widgets.Material3Scaffold
import org.breezyweather.ui.common.widgets.insets.BWCenterAlignedTopAppBar
import org.breezyweather.ui.details.components.DetailsAirQuality
import org.breezyweather.ui.details.components.DetailsCloudCover
import org.breezyweather.ui.details.components.DetailsConditions
import org.breezyweather.ui.details.components.DetailsHumidity
import org.breezyweather.ui.details.components.DetailsPollen
import org.breezyweather.ui.details.components.DetailsPrecipitation
import org.breezyweather.ui.details.components.DetailsPressure
import org.breezyweather.ui.details.components.DetailsSunMoon
import org.breezyweather.ui.details.components.DetailsUV
import org.breezyweather.ui.details.components.DetailsVisibility
import org.breezyweather.ui.details.components.DetailsWind
import org.breezyweather.ui.theme.ThemeManager
import org.breezyweather.ui.theme.compose.BreezyWeatherTheme
import java.util.Calendar
import java.util.Date

@Composable
internal fun DailyWeatherScreen(
    onBackPressed: () -> Unit,
    detailsViewModel: DetailsViewModel = viewModel(),
) {
    val detailsUiState by detailsViewModel.uiState.collectAsState()

    val context = LocalContext.current
    val activity = LocalActivity.current

    val isLightTheme = ThemeManager.isLightTheme(context, detailsUiState.location)
    LaunchedEffect(detailsUiState.location) {
        // re-setting the status bar color once the location is fetched
        if (detailsUiState.location != null && activity != null) {
            activity.window.setSystemBarStyle(isLightTheme)
        }
    }

    BreezyWeatherTheme(!isLightTheme) {
        Material3Scaffold(
            topBar = {
                BWCenterAlignedTopAppBar(
                    title = detailsUiState.selectedChart.getName(context),
                    onBackPressed = onBackPressed
                )
            },
            floatingActionButton = {
                detailsUiState.location?.let { loc ->
                    DetailsDropdownMenu(
                        location = loc,
                        selectedChart = detailsUiState.selectedChart,
                        setSelectedChart = { chart -> detailsViewModel.setSelectedChart(chart) }
                    )
                }
            },
            floatingActionButtonPosition = FabPosition.End
        ) { paddings ->
            detailsUiState.location?.let { loc ->
                val scope = rememberCoroutineScope()
                val pages = remember(loc.weather!!.dailyForecast) {
                    loc.weather!!.dailyForecast.map { it.date }.toImmutableList()
                }
                val pagerState = rememberPagerState(initialPage = detailsUiState.initialIndex) {
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
                            selectedChart = detailsUiState.selectedChart,
                            setSelectedChart = { chart -> detailsViewModel.setSelectedChart(chart) },
                            selectedPollutant = detailsUiState.selectedPollutant,
                            setSelectedPollutant = { pollutant -> detailsViewModel.setSelectedPollutant(pollutant) },
                            pollenIndexSource = detailsViewModel.getPollenIndexSource(loc)
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
    val alternateCalendar = remember {
        CalendarHelper.getAlternateCalendarSetting(context)
    }

    PrimaryScrollableTabRow(
        selectedTabIndex = selected,
        modifier = modifier.fillMaxWidth()
    ) {
        pages.forEachIndexed { i, date ->
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
                                } +
                                    " " +
                                    date.getFormattedFullDayAndMonth(location, context)
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
                            text = date.getFormattedDayOfTheMonth(location, context),
                            style = MaterialTheme.typography.headlineMedium
                        )
                        Text(
                            text = alternateCalendar?.let {
                                date.getFormattedMediumDayAndMonthInAdditionalCalendar(location, context)
                            } ?: date.getFormattedDate("MMM", location, context, withBestPattern = true)
                        )
                    }
                }
            )
        }
    }
}

@Composable
fun DetailsDropdownMenu(
    location: Location,
    selectedChart: DetailScreen,
    setSelectedChart: (DetailScreen) -> Unit,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val detailScreenEntries = remember(location) {
        DetailScreen.toDetailScreenList(location)
    }

    val listState = rememberLazyListState()
    val fabVisible by remember { derivedStateOf { listState.firstVisibleItemIndex == 0 } }

    Box(
        modifier = modifier
    ) {
        var fabMenuExpanded by rememberSaveable { mutableStateOf(false) }

        BackHandler(fabMenuExpanded) { fabMenuExpanded = false }

        FloatingActionButtonMenu(
            modifier = Modifier.align(Alignment.BottomEnd),
            expanded = fabMenuExpanded,
            button = {
                ToggleFloatingActionButton(
                    modifier = Modifier
                        .semantics {
                            traversalIndex = -1f
                            stateDescription = context.getString(
                                if (fabMenuExpanded) R.string.label_expanded else R.string.label_collapsed
                            )
                            contentDescription = context.getString(R.string.action_toggle_data_type_menu)
                        }.animateFloatingActionButton(
                            visible = fabVisible || fabMenuExpanded,
                            alignment = Alignment.BottomEnd
                        ),
                    checked = fabMenuExpanded,
                    onCheckedChange = { fabMenuExpanded = !fabMenuExpanded }
                ) {
                    val imageVector by remember(selectedChart.iconId, checkedProgress) {
                        derivedStateOf {
                            if (checkedProgress > 0.5f) R.drawable.ic_close else selectedChart.iconId
                        }
                    }
                    Icon(
                        painter = painterResource(imageVector),
                        contentDescription = null,
                        modifier = Modifier.animateIcon({ checkedProgress })
                    )
                }
            }
        ) {
            // Workaround until the missing spacing is fixed upstream
            Spacer(modifier = Modifier.height(WindowInsets.statusBars.asPaddingValues().calculateTopPadding() * 2))
            detailScreenEntries.forEachIndexed { i, item ->
                FloatingActionButtonMenuItem(
                    modifier = Modifier
                        .semantics {
                            isTraversalGroup = true
                            // Add a custom a11y action to allow closing the menu when focusing
                            // the last menu item, since the close button comes before the first
                            // menu item in the traversal order.
                            if (i == detailScreenEntries.size - 1) {
                                customActions =
                                    listOf(
                                        CustomAccessibilityAction(
                                            label = context.getString(R.string.action_close_menu),
                                            action = {
                                                fabMenuExpanded = false
                                                true
                                            }
                                        )
                                    )
                            }
                        },
                    onClick = {
                        setSelectedChart(item)
                        fabMenuExpanded = false
                    },
                    icon = { Icon(painterResource(item.iconId), contentDescription = null) },
                    text = { Text(text = item.getName(context)) }
                )
            }
        }
    }
}

@Composable
fun DailyPagerContent(
    location: Location,
    selected: Int,
    selectedChart: DetailScreen,
    setSelectedChart: (DetailScreen) -> Unit,
    selectedPollutant: PollutantIndex?,
    setSelectedPollutant: (PollutantIndex?) -> Unit,
    pollenIndexSource: PollenIndexSource?,
    modifier: Modifier = Modifier,
) {
    val daily = remember(selected) {
        location.weather!!.dailyForecast[selected]
    }

    val hourlyList = remember(selected) {
        val startingDate = daily.date.toTimezoneSpecificHour(location.timeZone, 0)
        val endingDate = daily.date.toCalendarWithTimeZone(location.timeZone).apply {
            add(Calendar.DAY_OF_MONTH, 1)
        }.time.toTimezoneSpecificHour(location.timeZone, 0)

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

    val currentUpdateTime = remember(location) {
        location.weather!!.base.currentUpdateTime
            ?: location.weather!!.base.forecastUpdateTime
            ?: location.weather!!.base.refreshTime!!
    }

    val current = remember(selected) {
        if (daily.isToday(location) && currentUpdateTime > daily.date) location.weather!!.current else null
    }

    Column(
        modifier = modifier
    ) {
        when (selectedChart) {
            DetailScreen.TAG_CONDITIONS, DetailScreen.TAG_FEELS_LIKE -> {
                DetailsConditions(
                    location,
                    hourlyList,
                    daily,
                    location.weather?.normals?.getOrElse(daily.date.getCalendarMonth(location)) { null },
                    selectedChart,
                    { setSelectedChart(if (it) DetailScreen.TAG_CONDITIONS else DetailScreen.TAG_FEELS_LIKE) }
                )
            }
            DetailScreen.TAG_PRECIPITATION -> {
                DetailsPrecipitation(location, hourlyList, daily)
            }
            DetailScreen.TAG_WIND -> {
                DetailsWind(
                    location,
                    hourlyList,
                    daily,
                    current?.wind?.let {
                        if (it.isValid) Pair(currentUpdateTime, it) else null
                    }
                )
            }
            DetailScreen.TAG_AIR_QUALITY -> {
                val supportedPollutants = remember(location) {
                    PollutantIndex.entries
                        .filter { pollutant ->
                            location.weather!!.dailyForecast.any {
                                it.airQuality?.getConcentration(pollutant) != null
                            } ||
                                location.weather!!.hourlyForecast.any {
                                    it.airQuality?.getConcentration(pollutant) != null
                                } ||
                                location.weather!!.current?.airQuality?.getConcentration(pollutant) != null
                        }.toImmutableList()
                }

                DetailsAirQuality(
                    location,
                    supportedPollutants,
                    selectedPollutant,
                    setSelectedPollutant,
                    hourlyList,
                    daily,
                    current?.airQuality?.let {
                        if (it.isValid) Pair(currentUpdateTime, it) else null
                    }
                )
            }
            DetailScreen.TAG_POLLEN -> {
                DetailsPollen(daily.pollen, pollenIndexSource)
            }
            DetailScreen.TAG_UV_INDEX -> {
                DetailsUV(
                    location,
                    hourlyList,
                    daily,
                    current?.uV?.let {
                        if (it.isValid) Pair(currentUpdateTime, it) else null
                    }
                )
            }
            DetailScreen.TAG_HUMIDITY -> {
                DetailsHumidity(
                    location,
                    hourlyList,
                    daily,
                    current?.relativeHumidity?.let { relativeHumidity ->
                        Pair(currentUpdateTime, relativeHumidity)
                    },
                    current?.dewPoint?.let { dewPoint ->
                        Pair(currentUpdateTime, dewPoint)
                    }
                )
            }
            DetailScreen.TAG_PRESSURE -> {
                DetailsPressure(
                    location,
                    hourlyList,
                    daily,
                    current?.pressure?.let { pressure ->
                        Pair(currentUpdateTime, pressure)
                    }
                )
            }
            DetailScreen.TAG_CLOUD_COVER -> {
                DetailsCloudCover(
                    location,
                    hourlyList,
                    daily,
                    current?.cloudCover?.let { cloudCover ->
                        Pair(currentUpdateTime, cloudCover)
                    }
                )
            }
            DetailScreen.TAG_VISIBILITY -> {
                DetailsVisibility(
                    location,
                    hourlyList,
                    daily,
                    current?.visibility?.let { visibility ->
                        Pair(currentUpdateTime, visibility)
                    }
                )
            }
            DetailScreen.TAG_SUN_MOON -> {
                val sunTimes = remember(selected) {
                    location.weather!!.dailyForecast.mapNotNull { it.sun }
                }
                val moonTimes = remember(selected) {
                    location.weather!!.dailyForecast.mapNotNull { it.moon }
                }
                DetailsSunMoon(location, daily, sunTimes, moonTimes)
            }
        }
    }
}
