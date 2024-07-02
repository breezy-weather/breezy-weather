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

package org.breezyweather.settings.activities

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ContentTransform
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.stringResource
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import breezyweather.domain.location.model.Location
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import org.breezyweather.R
import org.breezyweather.common.basic.GeoActivity
import org.breezyweather.common.bus.EventBus
import org.breezyweather.common.extensions.hasPermission
import org.breezyweather.common.ui.widgets.Material3Scaffold
import org.breezyweather.common.ui.widgets.generateCollapsedScrollBehavior
import org.breezyweather.common.ui.widgets.insets.FitStatusBarTopAppBar
import org.breezyweather.settings.SettingsChangedMessage
import org.breezyweather.settings.SettingsManager
import org.breezyweather.settings.compose.AppBarState
import org.breezyweather.settings.compose.AppearanceSettingsScreen
import org.breezyweather.settings.compose.BackgroundSettingsScreen
import org.breezyweather.settings.compose.DebugSettingsScreen
import org.breezyweather.settings.compose.LocationSettingsScreen
import org.breezyweather.settings.compose.MainScreenSettingsScreen
import org.breezyweather.settings.compose.NotificationsSettingsScreen
import org.breezyweather.settings.compose.RootSettingsView
import org.breezyweather.settings.compose.SettingsScreenRouter
import org.breezyweather.settings.compose.UnitSettingsScreen
import org.breezyweather.settings.compose.WeatherSourcesSettingsScreen
import org.breezyweather.settings.compose.WidgetsSettingsScreen
import org.breezyweather.sources.RefreshHelper
import org.breezyweather.sources.SourceManager
import org.breezyweather.theme.compose.BreezyWeatherTheme
import javax.inject.Inject

private const val PERMISSION_CODE_POST_NOTIFICATION = 0
private const val ANIMATION_DURATION = 700

@AndroidEntryPoint
class SettingsActivity : GeoActivity() {

    @Inject lateinit var sourceManager: SourceManager
    @Inject lateinit var refreshHelper: RefreshHelper

    private val updateIntervalState = mutableStateOf(
        SettingsManager.getInstance(this).updateInterval
    )
    private val cardDisplayState = mutableStateOf(
        SettingsManager.getInstance(this).cardDisplayList
    )
    private val dailyTrendDisplayState = mutableStateOf(
        SettingsManager.getInstance(this).dailyTrendDisplayList
    )
    private val hourlyTrendDisplayState = mutableStateOf(
        SettingsManager.getInstance(this).hourlyTrendDisplayList
    )
    private val detailsDisplayState = mutableStateOf(
        SettingsManager.getInstance(this).detailDisplayList
    )
    private val notificationEnabledState = mutableStateOf(
        SettingsManager.getInstance(this).isWidgetNotificationEnabled
    )
    private val notificationTemperatureIconEnabledState = mutableStateOf(
        SettingsManager.getInstance(this).isWidgetNotificationTemperatureIconEnabled
    )
    private val todayForecastEnabledState = mutableStateOf(
        SettingsManager.getInstance(this).isTodayForecastEnabled
    )
    private val tomorrowForecastEnabledState = mutableStateOf(
        SettingsManager.getInstance(this).isTomorrowForecastEnabled
    )

    private var requestPostNotificationPermissionSucceedCallback: (() -> Unit)? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            BreezyWeatherTheme(lightTheme = !isSystemInDarkTheme()) {
                ContentView()
            }
        }

        EventBus.instance.with(SettingsChangedMessage::class.java).observe(this) {
            val updateInterval = SettingsManager.getInstance(this).updateInterval
            if (updateIntervalState.value != updateInterval) {
                updateIntervalState.value = updateInterval
            }

            val cardDisplayList = SettingsManager.getInstance(this).cardDisplayList
            if (cardDisplayState.value != cardDisplayList) {
                cardDisplayState.value = cardDisplayList
            }

            val dailyTrendDisplayList = SettingsManager.getInstance(this).dailyTrendDisplayList
            if (dailyTrendDisplayState.value != dailyTrendDisplayList) {
                dailyTrendDisplayState.value = dailyTrendDisplayList
            }

            val hourlyTrendDisplayList = SettingsManager.getInstance(this).hourlyTrendDisplayList
            if (hourlyTrendDisplayState.value != hourlyTrendDisplayList) {
                hourlyTrendDisplayState.value = hourlyTrendDisplayList
            }

            val detailsDisplayList = SettingsManager.getInstance(this).detailDisplayList
            if (detailsDisplayState.value != detailsDisplayList) {
                detailsDisplayState.value = detailsDisplayList
            }

            val notificationEnabled = SettingsManager.getInstance(this).isWidgetNotificationEnabled
            if (notificationEnabledState.value != notificationEnabled) {
                notificationEnabledState.value = notificationEnabled
            }

            val notificationTemperatureIconEnabled = SettingsManager.getInstance(this).isWidgetNotificationTemperatureIconEnabled
            if (notificationTemperatureIconEnabledState.value != notificationTemperatureIconEnabled) {
                notificationTemperatureIconEnabledState.value = notificationTemperatureIconEnabled
            }

            val todayForecastEnabled = SettingsManager.getInstance(this).isTodayForecastEnabled
            if (todayForecastEnabledState.value != todayForecastEnabled) {
                todayForecastEnabledState.value = todayForecastEnabled
            }

            val tomorrowForecastEnabled = SettingsManager.getInstance(this).isTomorrowForecastEnabled
            if (tomorrowForecastEnabledState.value != tomorrowForecastEnabled) {
                tomorrowForecastEnabledState.value = tomorrowForecastEnabled
            }
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (requestCode == PERMISSION_CODE_POST_NOTIFICATION) {
            if (grantResults.count { it == PackageManager.PERMISSION_GRANTED } == grantResults.size) {
                // all granted.
                requestPostNotificationPermissionSucceedCallback?.let { it() }
                requestPostNotificationPermissionSucceedCallback = null
            }
            return
        }
    }

    @Composable
    private fun ContentView() {
        val scope = rememberCoroutineScope()
        val scrollBehavior = generateCollapsedScrollBehavior()
        val navController = rememberNavController()

        var appBarState by remember { mutableStateOf(AppBarState()) }

        Material3Scaffold(
            modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
            topBar = {
                FitStatusBarTopAppBar(
                    title =  {
                        AnimatedContent(
                            targetState = appBarState.title,
                            transitionSpec = {
                                if (initialState.isNotEmpty()) {
                                    ContentTransform(
                                        defaultEnterTransition(),
                                        defaultExitTransition(),
                                        sizeTransform = null
                                    )
                                } else ContentTransform( // No animation when the title is initially set.
                                    EnterTransition.None, ExitTransition.None, sizeTransform = null
                                )
                            },
                            label = "Settings title"
                        ) {
                            Text(text = it)
                        }
                    },
                    navigationIcon = {
                        IconButton(onClick = { onBackPressedDispatcher.onBackPressed() }) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = stringResource(R.string.action_back),
                                tint = MaterialTheme.colorScheme.onSurface,
                            )
                        }
                    },
                    actions = appBarState.actions,
                    scrollBehavior = scrollBehavior,
                )
            },
        ) { paddings ->
            // Use the destination as key for the LaunchedEffect to ensure that it is correctly
            // triggered when quickly navigating back and forth between destinations.
            val navBackStackEntry by navController.currentBackStackEntryAsState()
            val currentDestination = navBackStackEntry?.destination?.id

            NavHost(
                navController = navController,
                startDestination = SettingsScreenRouter.Root.route,
                enterTransition = { defaultEnterTransition() },
                exitTransition = { defaultExitTransition() }
            ) {
                composable(SettingsScreenRouter.Root.route) {
                    LaunchedEffect(currentDestination) {
                        appBarState = AppBarState(
                            title = getString(R.string.action_settings)
                        )
                    }
                    RootSettingsView(
                        onNavigateTo = { navController.navigate(it) },
                        paddingValues = paddings
                    )
                }
                composable(SettingsScreenRouter.BackgroundUpdates.route) {
                    LaunchedEffect(currentDestination) {
                        appBarState = AppBarState(
                            title = getString(R.string.settings_background_updates)
                        )
                    }
                    BackgroundSettingsScreen(
                        context = this@SettingsActivity,
                        updateInterval = remember { updateIntervalState }.value,
                        paddingValues = paddings
                    )
                }
                composable(SettingsScreenRouter.Appearance.route) {
                    LaunchedEffect(currentDestination) {
                        appBarState = AppBarState(
                            title = getString(R.string.settings_appearance)
                        )
                    }
                    AppearanceSettingsScreen(
                        context = this@SettingsActivity,
                        onNavigateTo = { navController.navigate(it) },
                        paddingValues = paddings,
                    )
                }
                composable(SettingsScreenRouter.Unit.route) {
                    LaunchedEffect(currentDestination) {
                        appBarState = AppBarState(
                            title = getString(R.string.settings_units)
                        )
                    }
                    UnitSettingsScreen(
                        context = this@SettingsActivity,
                        paddingValues = paddings,
                    )
                }
                composable(SettingsScreenRouter.MainScreen.route) {
                    LaunchedEffect(currentDestination) {
                        appBarState = AppBarState(
                            title = getString(R.string.settings_main)
                        )
                    }
                    MainScreenSettingsScreen(
                        context = this@SettingsActivity,
                        cardDisplayList = remember { cardDisplayState }.value,
                        dailyTrendDisplayList = remember { dailyTrendDisplayState }.value,
                        hourlyTrendDisplayList = remember { hourlyTrendDisplayState }.value,
                        detailDisplayList = remember { detailsDisplayState }.value,
                        paddingValues = paddings,
                        updateWidgetIfNecessary = { context: Context ->
                            scope.launch {
                                refreshHelper.updateWidgetIfNecessary(context)
                            }
                        }
                    )
                }
                composable(SettingsScreenRouter.Notifications.route) {
                    LaunchedEffect(currentDestination) {
                        appBarState = AppBarState(
                            title = getString(R.string.settings_notifications)
                        )
                    }
                    NotificationsSettingsScreen(
                        context = this@SettingsActivity,
                        paddingValues = paddings,
                        todayForecastEnabled = remember { todayForecastEnabledState }.value,
                        tomorrowForecastEnabled = remember { tomorrowForecastEnabledState }.value,
                        postNotificationPermissionEnsurer = { succeedCallback ->
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
                                !this@SettingsActivity.hasPermission(Manifest.permission.POST_NOTIFICATIONS)) {

                                requestPostNotificationPermissionSucceedCallback = succeedCallback
                                requestPermissions(
                                    arrayOf(Manifest.permission.POST_NOTIFICATIONS),
                                    PERMISSION_CODE_POST_NOTIFICATION
                                )
                            } else {
                                succeedCallback()
                            }
                        }
                    )
                }
                composable(SettingsScreenRouter.Widgets.route) {
                    LaunchedEffect(currentDestination) {
                        appBarState = AppBarState(
                            title = getString(R.string.settings_widgets)
                        )
                    }
                    WidgetsSettingsScreen(
                        context = this@SettingsActivity,
                        notificationEnabled = remember { notificationEnabledState }.value,
                        notificationTemperatureIconEnabled = remember { notificationTemperatureIconEnabledState }.value,
                        paddingValues = paddings,
                        postNotificationPermissionEnsurer = { succeedCallback ->
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
                                !this@SettingsActivity.hasPermission(Manifest.permission.POST_NOTIFICATIONS)) {

                                requestPostNotificationPermissionSucceedCallback = succeedCallback
                                requestPermissions(
                                    arrayOf(Manifest.permission.POST_NOTIFICATIONS),
                                    PERMISSION_CODE_POST_NOTIFICATION
                                )
                            } else {
                                succeedCallback()
                            }
                        },
                        updateWidgetIfNecessary = { context: Context ->
                            scope.launch {
                                refreshHelper.updateWidgetIfNecessary(context)
                            }
                        },
                        updateNotificationIfNecessary = { context: Context ->
                            scope.launch {
                                refreshHelper.updateNotificationIfNecessary(context)
                            }
                        },
                        broadcastDataIfNecessary = { context: Context, sourceId: String ->
                            scope.launch {
                                refreshHelper.broadcastDataIfNecessary(context, sourceId)
                            }
                        },
                        broadcastSources = sourceManager.getBroadcastSources(),
                    )
                }
                composable(SettingsScreenRouter.Location.route) {
                    LaunchedEffect(currentDestination) {
                        appBarState = AppBarState(
                            title = getString(R.string.settings_location)
                        )
                    }
                    LocationSettingsScreen(
                        context = this@SettingsActivity,
                        locationSources = sourceManager.getConfiguredLocationSources(),
                        paddingValues = paddings,
                    )
                }
                composable(SettingsScreenRouter.WeatherProviders.route) {
                    LaunchedEffect(currentDestination) {
                        appBarState = AppBarState(
                            title = getString(R.string.settings_weather_sources)
                        )
                    }
                    WeatherSourcesSettingsScreen(
                        context = this@SettingsActivity,
                        configuredWorldwideSources = sourceManager.getConfiguredMainWeatherSources()
                            .filter {
                                it.isFeatureSupportedInMainForLocation(Location())
                            },
                        configurableSources = sourceManager.getConfigurableSources(),
                        paddingValues = paddings,
                    )
                }
                composable(SettingsScreenRouter.Debug.route) {
                    LaunchedEffect(currentDestination) {
                        appBarState = AppBarState(
                            title = getString(R.string.settings_debug)
                        )
                    }
                    DebugSettingsScreen(
                        context = this@SettingsActivity,
                        paddingValues = paddings,
                    )
                }
            }
        }
    }

    private fun defaultEnterTransition(): EnterTransition {
        return fadeIn((tween(ANIMATION_DURATION)))
    }

    private fun defaultExitTransition(): ExitTransition {
        return fadeOut((tween(ANIMATION_DURATION)))
    }

    /*@Preview
    @Composable
    private fun DefaultPreview() {
        BreezyWeatherTheme(lightTheme = isSystemInDarkTheme()) {
            ContentView()
        }
    }*/
}
