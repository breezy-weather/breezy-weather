package wangdaye.com.geometricweather.settings.activities

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material.Scaffold
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import wangdaye.com.geometricweather.R
import wangdaye.com.geometricweather.common.basic.GeoActivity
import wangdaye.com.geometricweather.common.ui.widgets.Material3Scaffold
import wangdaye.com.geometricweather.common.ui.widgets.generateCollapsedScrollBehavior
import wangdaye.com.geometricweather.common.ui.widgets.insets.FitStatusBarTopAppBar
import wangdaye.com.geometricweather.settings.compose.ServiceProviderSettingsScreen
import wangdaye.com.geometricweather.settings.compose.SettingsProviderAdvancedSettingsScreen
import wangdaye.com.geometricweather.settings.compose.SettingsScreenRouter
import wangdaye.com.geometricweather.theme.compose.GeometricWeatherTheme

class SelectProviderActivity : GeoActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            GeometricWeatherTheme(lightTheme = !isSystemInDarkTheme()) {
                ContentView()
            }
        }
    }

    @Composable
    private fun ContentView() {
        val scrollBehavior = generateCollapsedScrollBehavior()

        Material3Scaffold(
            modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
            topBar = {
                FitStatusBarTopAppBar(
                    title = stringResource(R.string.settings_title_service_provider),
                    onBackPressed = { finish() },
                    scrollBehavior = scrollBehavior,
                )
            },
        ) {
            val navController = rememberNavController()
            NavHost(
                navController = navController,
                startDestination = SettingsScreenRouter.ServiceProvider.route
            ) {
                composable(SettingsScreenRouter.ServiceProvider.route) {
                    ServiceProviderSettingsScreen(
                        context = this@SelectProviderActivity,
                        navController = navController
                    )
                }
                composable(SettingsScreenRouter.ServiceProviderAdvanced.route) {
                    SettingsProviderAdvancedSettingsScreen(
                        context = this@SelectProviderActivity
                    )
                }
            }
        }
    }

    @Preview
    @Composable
    private fun DefaultPreview() {
        GeometricWeatherTheme(lightTheme = isSystemInDarkTheme()) {
            ContentView()
        }
    }
}