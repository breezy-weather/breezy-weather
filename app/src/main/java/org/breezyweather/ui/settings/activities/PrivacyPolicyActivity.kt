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

package org.breezyweather.ui.settings.activities

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.stringResource
import dagger.hilt.android.AndroidEntryPoint
import org.breezyweather.R
import org.breezyweather.common.basic.GeoActivity
import org.breezyweather.common.extensions.currentLocale
import org.breezyweather.sources.SourceManager
import org.breezyweather.ui.common.widgets.Material3Scaffold
import org.breezyweather.ui.common.widgets.generateCollapsedScrollBehavior
import org.breezyweather.ui.common.widgets.insets.FitStatusBarTopAppBar
import org.breezyweather.ui.settings.preference.bottomInsetItem
import org.breezyweather.ui.settings.preference.clickablePreferenceItem
import org.breezyweather.ui.settings.preference.composables.PreferenceScreen
import org.breezyweather.ui.settings.preference.composables.PreferenceViewWithCard
import org.breezyweather.ui.theme.compose.BreezyWeatherTheme
import java.text.Collator
import javax.inject.Inject

@AndroidEntryPoint
class PrivacyPolicyActivity : GeoActivity() {

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
        val uriHandler = LocalUriHandler.current

        Material3Scaffold(
            modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
            topBar = {
                FitStatusBarTopAppBar(
                    title = stringResource(R.string.about_privacy_policy),
                    onBackPressed = { finish() },
                    scrollBehavior = scrollBehavior
                )
            }
        ) { paddingValues ->
            PreferenceScreen(paddingValues = paddingValues) {
                clickablePreferenceItem(R.string.breezy_weather) { id ->
                    val url = "https://github.com/breezy-weather/breezy-weather/blob/main/PRIVACY.md"
                    PreferenceViewWithCard(
                        title = stringResource(id),
                        summary = url
                    ) {
                        uriHandler.openUri(url)
                    }
                }

                items(
                    sourceManager.getHttpSources()
                        .filter { it.privacyPolicyUrl.startsWith("http") }
                        .sortedWith { s1, s2 ->
                            // Sort by name because there are now a lot of sources
                            Collator.getInstance(
                                this@PrivacyPolicyActivity.currentLocale
                            ).compare(s1.name, s2.name)
                        }
                ) { preferenceSource ->
                    PreferenceViewWithCard(
                        title = preferenceSource.name,
                        summary = preferenceSource.privacyPolicyUrl
                    ) {
                        uriHandler.openUri(preferenceSource.privacyPolicyUrl)
                    }
                }

                bottomInsetItem()
            }
        }
    }
}
