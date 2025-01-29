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

package org.breezyweather.ui.pollen

import androidx.activity.compose.LocalActivity
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import org.breezyweather.R
import org.breezyweather.common.extensions.capitalize
import org.breezyweather.common.extensions.currentLocale
import org.breezyweather.common.extensions.getFormattedDate
import org.breezyweather.common.extensions.getLongWeekdayDayMonth
import org.breezyweather.domain.weather.model.isIndexValid
import org.breezyweather.ui.common.composables.PollenGrid
import org.breezyweather.ui.common.widgets.Material3CardListItem
import org.breezyweather.ui.common.widgets.Material3Scaffold
import org.breezyweather.ui.common.widgets.generateCollapsedScrollBehavior
import org.breezyweather.ui.common.widgets.getCardListItemMarginDp
import org.breezyweather.ui.common.widgets.insets.FitStatusBarTopAppBar
import org.breezyweather.ui.common.widgets.insets.bottomInsetItem
import org.breezyweather.ui.main.utils.MainThemeColorProvider
import org.breezyweather.ui.theme.ThemeManager
import org.breezyweather.ui.theme.compose.BreezyWeatherTheme
import org.breezyweather.ui.theme.compose.DayNightTheme

@Composable
internal fun PollenScreen(
    onBackPressed: () -> Unit,
    pollenViewModel: PollenViewModel = viewModel(),
) {
    val pollenUiState by pollenViewModel.uiState.collectAsState()

    val context = LocalContext.current
    val activity = LocalActivity.current

    val scrollBehavior = generateCollapsedScrollBehavior()

    val isLightTheme = MainThemeColorProvider.isLightTheme(context, pollenUiState.location)
    LaunchedEffect(pollenUiState.location) {
        // re-setting the status bar color once the location is fetched
        if (pollenUiState.location != null && activity != null) {
            ThemeManager
                .getInstance(context)
                .weatherThemeDelegate
                .setSystemBarStyle(
                    context = context,
                    window = activity.window,
                    statusShader = false,
                    lightStatus = isLightTheme,
                    navigationShader = true,
                    lightNavigation = isLightTheme
                )
        }
    }

    BreezyWeatherTheme(lightTheme = isLightTheme) {
        Material3Scaffold(
            modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
            topBar = {
                FitStatusBarTopAppBar(
                    title = stringResource(R.string.pollen),
                    onBackPressed = onBackPressed,
                    scrollBehavior = scrollBehavior
                )
            }
        ) {
            pollenUiState.location?.weather?.let { weather ->
                LazyColumn(
                    modifier = Modifier.fillMaxHeight(),
                    contentPadding = it
                ) {
                    items(
                        weather.dailyForecastStartingToday.filter { d ->
                            d.pollen?.isIndexValid == true
                        }
                    ) { daily ->
                        daily.pollen?.let { pollen ->
                            Material3CardListItem(
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column {
                                    Text(
                                        modifier = Modifier.padding(dimensionResource(R.dimen.normal_margin)),
                                        text = daily.date.getFormattedDate(
                                            getLongWeekdayDayMonth(context),
                                            pollenUiState.location!!,
                                            context
                                        ).capitalize(context.currentLocale),
                                        color = DayNightTheme.colors.titleColor,
                                        fontWeight = FontWeight.Bold,
                                        style = MaterialTheme.typography.titleMedium
                                    )
                                    PollenGrid(
                                        pollen = pollen,
                                        pollenIndexSource = pollenUiState.pollenIndexSource
                                    )
                                }
                            }
                        }
                    }

                    bottomInsetItem(
                        extraHeight = getCardListItemMarginDp(context).dp
                    )
                }
            }
        }
    }
}
