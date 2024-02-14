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
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.stringResource
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.google.accompanist.permissions.rememberPermissionState
import dagger.hilt.android.AndroidEntryPoint
import org.breezyweather.R
import org.breezyweather.common.basic.GeoActivity
import org.breezyweather.common.bus.EventBus
import org.breezyweather.common.extensions.hasPermission
import org.breezyweather.common.ui.widgets.Material3Scaffold
import org.breezyweather.common.ui.widgets.generateCollapsedScrollBehavior
import org.breezyweather.common.ui.widgets.insets.FitStatusBarTopAppBar
import org.breezyweather.common.utils.helpers.IntentHelper
import org.breezyweather.settings.SettingsChangedMessage
import org.breezyweather.settings.SettingsManager
import org.breezyweather.settings.compose.*
import org.breezyweather.sources.SourceManager
import org.breezyweather.theme.compose.BreezyWeatherTheme
import javax.inject.Inject

private const val PERMISSION_CODE_POST_NOTIFICATION = 0

@AndroidEntryPoint
class SettingsActivity : GeoActivity() {

    @Inject lateinit var sourceManager: SourceManager

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
        val scrollBehavior = generateCollapsedScrollBehavior()

        Material3Scaffold(
            modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
            topBar = {
                FitStatusBarTopAppBar(
                    title = stringResource(R.string.action_settings),
                    onBackPressed = { finish() },
                    actions = {
                        IconButton(
                            onClick = {
                                IntentHelper.startAboutActivity(this@SettingsActivity)
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Info,
                                contentDescription = stringResource(R.string.action_about),
                                tint = MaterialTheme.colorScheme.onPrimaryContainer,
                            )
                        }
                    },
                    scrollBehavior = scrollBehavior,
                )
            },
        ) { paddings ->
            val navController = rememberNavController()
            NavHost(
                navController = navController,
                startDestination = SettingsScreenRouter.Root.route
            ) {
                composable(SettingsScreenRouter.Root.route) {
                    RootSettingsView(
                        navController = navController,
                        paddingValues = paddings
                    )
                }
                composable(SettingsScreenRouter.BackgroundUpdates.route) {
                    BackgroundSettingsScreen(
                        context = this@SettingsActivity,
                        updateInterval = remember { updateIntervalState }.value,
                        paddingValues = paddings
                    )
                }
                composable(SettingsScreenRouter.Appearance.route) {
                    AppearanceSettingsScreen(
                        context = this@SettingsActivity,
                        navController = navController,
                        paddingValues = paddings,
                    )
                }
                composable(SettingsScreenRouter.Unit.route) {
                    UnitSettingsScreen(
                        context = this@SettingsActivity,
                        paddingValues = paddings,
                    )
                }
                composable(SettingsScreenRouter.MainScreen.route) {
                    MainScreenSettingsScreen(
                        context = this@SettingsActivity,
                        cardDisplayList = remember { cardDisplayState }.value,
                        dailyTrendDisplayList = remember { dailyTrendDisplayState }.value,
                        hourlyTrendDisplayList = remember { hourlyTrendDisplayState }.value,
                        detailDisplayList = remember { detailsDisplayState }.value,
                        paddingValues = paddings,
                    )
                }
                composable(SettingsScreenRouter.Notifications.route) {
                    NotificationsSettingsScreen(
                        context = this@SettingsActivity,
                        paddingValues = paddings,
                        todayForecastEnabled = remember { todayForecastEnabledState }.value,
                        tomorrowForecastEnabled = remember { tomorrowForecastEnabledState }.value,
                        postNotificationPermissionEnsurer = { succeedCallback ->
                            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
                                succeedCallback()
                                return@NotificationsSettingsScreen
                            }
                            if (this@SettingsActivity.hasPermission(Manifest.permission.POST_NOTIFICATIONS)) {
                                return@NotificationsSettingsScreen
                            }

                            requestPostNotificationPermissionSucceedCallback = succeedCallback
                            requestPermissions(
                                arrayOf(Manifest.permission.POST_NOTIFICATIONS),
                                PERMISSION_CODE_POST_NOTIFICATION
                            )
                        }
                    )
                }
                composable(SettingsScreenRouter.Widgets.route) {
                    WidgetsSettingsScreen(
                        context = this@SettingsActivity,
                        notificationEnabled = remember { notificationEnabledState }.value,
                        notificationTemperatureIconEnabled = remember { notificationTemperatureIconEnabledState }.value,
                        paddingValues = paddings,
                        postNotificationPermissionEnsurer = { succeedCallback ->
                            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
                                succeedCallback()
                                return@WidgetsSettingsScreen
                            }
                            if (this@SettingsActivity.hasPermission(Manifest.permission.POST_NOTIFICATIONS)) {
                                return@WidgetsSettingsScreen
                            }

                            requestPostNotificationPermissionSucceedCallback = succeedCallback
                            requestPermissions(
                                arrayOf(Manifest.permission.POST_NOTIFICATIONS),
                                PERMISSION_CODE_POST_NOTIFICATION
                            )
                        }
                    )
                }
                composable(SettingsScreenRouter.Location.route) {
                    LocationSettingsScreen(
                        context = this@SettingsActivity,
                        locationSources = sourceManager.getLocationSources(),
                        accessCoarseLocationPermissionState = rememberPermissionState(permission = Manifest.permission.ACCESS_COARSE_LOCATION),
                        accessFineLocationPermissionState = rememberPermissionState(permission = Manifest.permission.ACCESS_FINE_LOCATION),
                        accessBackgroundLocationPermissionState = rememberPermissionState(permission = Manifest.permission.ACCESS_BACKGROUND_LOCATION),
                        paddingValues = paddings,
                    )
                }
                composable(SettingsScreenRouter.WeatherProviders.route) {
                    WeatherSourcesSettingsScreen(
                        context = this@SettingsActivity,
                        configurableSources = sourceManager.getConfigurableSources(),
                        paddingValues = paddings,
                    )
                }
                composable(SettingsScreenRouter.Debug.route) {
                    DebugSettingsScreen(
                        context = this@SettingsActivity,
                        paddingValues = paddings,
                    )
                }
            }
        }
    }

    /*@Preview
    @Composable
    private fun DefaultPreview() {
        BreezyWeatherTheme(lightTheme = isSystemInDarkTheme()) {
            ContentView()
        }
    }*/
}