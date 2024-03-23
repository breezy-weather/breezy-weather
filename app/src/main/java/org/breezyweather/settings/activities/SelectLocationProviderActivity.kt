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
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.google.accompanist.permissions.rememberPermissionState
import dagger.hilt.android.AndroidEntryPoint
import org.breezyweather.R
import org.breezyweather.common.basic.GeoActivity
import org.breezyweather.common.ui.widgets.Material3Scaffold
import org.breezyweather.common.ui.widgets.generateCollapsedScrollBehavior
import org.breezyweather.common.ui.widgets.insets.FitStatusBarTopAppBar
import org.breezyweather.settings.compose.LocationSettingsScreen
import org.breezyweather.settings.compose.SettingsScreenRouter
import org.breezyweather.sources.SourceManager
import org.breezyweather.theme.compose.BreezyWeatherTheme
import javax.inject.Inject

@AndroidEntryPoint
class SelectLocationProviderActivity : GeoActivity() {

    @Inject lateinit var sourceManager: SourceManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            BreezyWeatherTheme(lightTheme = !isSystemInDarkTheme()) {
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
                    title = stringResource(R.string.settings_location),
                    onBackPressed = { finish() },
                    scrollBehavior = scrollBehavior,
                )
            },
        ) { paddings ->
            val navController = rememberNavController()
            NavHost(
                navController = navController,
                startDestination = SettingsScreenRouter.Location.route
            ) {
                composable(SettingsScreenRouter.Location.route) {
                    LocationSettingsScreen(
                        context = this@SelectLocationProviderActivity,
                        locationSources = sourceManager.getLocationSources(),
                        accessCoarseLocationPermissionState = rememberPermissionState(permission = Manifest.permission.ACCESS_COARSE_LOCATION),
                        accessFineLocationPermissionState = rememberPermissionState(permission = Manifest.permission.ACCESS_FINE_LOCATION),
                        // TODO: What happens on Android < Q? Why is it not underlined when initializing from SettingsActivity??
                        accessBackgroundLocationPermissionState = rememberPermissionState(permission = Manifest.permission.ACCESS_BACKGROUND_LOCATION),
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
