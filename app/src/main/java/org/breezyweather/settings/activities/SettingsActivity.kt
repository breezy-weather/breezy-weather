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
import androidx.compose.ui.tooling.preview.Preview
import androidx.core.content.ContextCompat
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import org.breezyweather.R
import org.breezyweather.common.basic.GeoActivity
import org.breezyweather.common.bus.EventBus
import org.breezyweather.common.ui.widgets.Material3Scaffold
import org.breezyweather.common.ui.widgets.generateCollapsedScrollBehavior
import org.breezyweather.common.ui.widgets.insets.FitStatusBarTopAppBar
import org.breezyweather.settings.SettingsChangedMessage
import org.breezyweather.settings.SettingsManager
import org.breezyweather.settings.compose.AppearanceSettingsScreen
import org.breezyweather.settings.compose.RootSettingsView
import org.breezyweather.settings.compose.ServiceProviderSettingsScreen
import org.breezyweather.settings.compose.SettingsProviderAdvancedSettingsScreen
import org.breezyweather.settings.compose.SettingsScreenRouter
import org.breezyweather.settings.compose.UnitSettingsScreen
import org.breezyweather.theme.compose.BreezyWeatherTheme

private const val PERMISSION_CODE_POST_NOTIFICATION = 0

class SettingsActivity : GeoActivity() {

    private val cardDisplayState = mutableStateOf(
        SettingsManager.getInstance(this).cardDisplayList
    )
    private val dailyTrendDisplayState = mutableStateOf(
        SettingsManager.getInstance(this).dailyTrendDisplayList
    )
    private val hourlyTrendDisplayState = mutableStateOf(
        SettingsManager.getInstance(this).hourlyTrendDisplayList
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
                                org.breezyweather.common.utils.helpers.IntentHelper.startAboutActivity(this@SettingsActivity)
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
                        context = this@SettingsActivity,
                        navController = navController,
                        paddingValues = paddings,
                        postNotificationPermissionEnsurer = { succeedCallback ->
                            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
                                succeedCallback()
                                return@RootSettingsView
                            }
                            if (ContextCompat.checkSelfPermission(
                                    this@SettingsActivity,
                                    Manifest.permission.POST_NOTIFICATIONS
                                ) == PackageManager.PERMISSION_GRANTED) {
                                return@RootSettingsView
                            }

                            requestPostNotificationPermissionSucceedCallback = succeedCallback
                            requestPermissions(
                                arrayOf(Manifest.permission.POST_NOTIFICATIONS),
                                PERMISSION_CODE_POST_NOTIFICATION
                            )
                        }
                    )
                }
                composable(SettingsScreenRouter.Appearance.route) {
                    AppearanceSettingsScreen(
                        context = this@SettingsActivity,
                        cardDisplayList = remember { cardDisplayState }.value,
                        dailyTrendDisplayList = remember { dailyTrendDisplayState }.value,
                        hourlyTrendDisplayList = remember { hourlyTrendDisplayState }.value,
                        paddingValues = paddings,
                    )
                }
                composable(SettingsScreenRouter.ServiceProvider.route) {
                    ServiceProviderSettingsScreen(
                        context = this@SettingsActivity,
                        navController = navController,
                        paddingValues = paddings,
                    )
                }
                composable(SettingsScreenRouter.ServiceProviderAdvanced.route) {
                    SettingsProviderAdvancedSettingsScreen(
                        context = this@SettingsActivity,
                        paddingValues = paddings,
                    )
                }
                composable(SettingsScreenRouter.Unit.route) {
                    UnitSettingsScreen(
                        context = this@SettingsActivity,
                        paddingValues = paddings,
                    )
                }
            }
        }
    }

    @Preview
    @Composable
    private fun DefaultPreview() {
        BreezyWeatherTheme(lightTheme = isSystemInDarkTheme()) {
            ContentView()
        }
    }
}