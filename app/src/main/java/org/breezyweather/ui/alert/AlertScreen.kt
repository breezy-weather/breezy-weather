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

package org.breezyweather.ui.alert

import androidx.activity.compose.LocalActivity
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import org.breezyweather.R
import org.breezyweather.common.utils.ColorUtils
import org.breezyweather.domain.weather.model.getFormattedDates
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
internal fun AlertScreen(
    onBackPressed: () -> Unit,
    alertViewModel: AlertViewModel = viewModel(),
) {
    val alertUiState by alertViewModel.uiState.collectAsState()

    val listState = rememberLazyListState()
    val context = LocalContext.current
    val activity = LocalActivity.current

    val scrollBehavior = generateCollapsedScrollBehavior()

    val isLightTheme = MainThemeColorProvider.isLightTheme(context, alertUiState.location)
    LaunchedEffect(alertUiState.location) {
        alertUiState.location?.weather?.alertList?.let { alerts ->
            if (alerts.isNotEmpty()) {
                if (!alertUiState.alertId.isNullOrEmpty()) {
                    val alertIndex = alerts.indexOfFirst { it.alertId == alertUiState.alertId }
                    if (alertIndex != -1) {
                        listState.scrollToItem(alertIndex)
                    } else {
                        listState.scrollToItem(0)
                    }
                } else {
                    listState.scrollToItem(0)
                }
            }
        }

        // re-setting the status bar color once the location is fetched
        if (alertUiState.location != null && activity != null) {
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
            modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
            topBar = {
                FitStatusBarTopAppBar(
                    title = stringResource(R.string.alerts),
                    onBackPressed = onBackPressed,
                    scrollBehavior = scrollBehavior
                )
            }
        ) {
            LazyColumn(
                modifier = Modifier.fillMaxHeight(),
                contentPadding = it,
                state = listState
            ) {
                items(alertUiState.location?.weather?.alertList ?: emptyList()) { alert ->
                    Material3CardListItem {
                        Column(
                            modifier = Modifier
                                .padding(dimensionResource(R.dimen.normal_margin))
                                .fillMaxWidth()
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    painterResource(R.drawable.ic_alert),
                                    contentDescription = null,
                                    tint = Color(ColorUtils.getDarkerColor(alert.color))
                                )
                                Spacer(modifier = Modifier.width(dimensionResource(R.dimen.normal_margin)))
                                Column {
                                    Text(
                                        text = alert.headline?.ifEmpty {
                                            stringResource(R.string.alert)
                                        } ?: stringResource(R.string.alert),
                                        color = DayNightTheme.colors.titleColor,
                                        fontWeight = FontWeight.Bold,
                                        style = MaterialTheme.typography.titleMedium
                                    )
                                    Text(
                                        text = alert.getFormattedDates(alertUiState.location!!, context),
                                        color = DayNightTheme.colors.captionColor,
                                        style = MaterialTheme.typography.labelMedium,
                                        modifier = Modifier
                                            .clearAndSetSemantics {
                                                contentDescription = alert.getFormattedDates(
                                                    alertUiState.location!!,
                                                    context,
                                                    full = true
                                                )
                                            }
                                    )
                                }
                            }
                            if (!alert.description.isNullOrBlank()) {
                                Spacer(modifier = Modifier.height(dimensionResource(R.dimen.little_margin)))
                                SelectionContainer {
                                    Text(
                                        text = alert.description!!,
                                        color = DayNightTheme.colors.bodyColor,
                                        style = MaterialTheme.typography.bodyMedium
                                    )
                                }
                            }
                            if (!alert.instruction.isNullOrBlank()) {
                                if (!alert.description.isNullOrBlank()) {
                                    Spacer(modifier = Modifier.height(dimensionResource(R.dimen.little_margin)))
                                    HorizontalDivider()
                                }
                                Spacer(modifier = Modifier.height(dimensionResource(R.dimen.little_margin)))
                                SelectionContainer {
                                    Text(
                                        text = alert.instruction!!,
                                        color = DayNightTheme.colors.bodyColor,
                                        style = MaterialTheme.typography.bodyMedium
                                    )
                                }
                            }
                            if (!alert.source.isNullOrBlank()) {
                                if (!alert.description.isNullOrBlank() || !alert.instruction.isNullOrBlank()) {
                                    Spacer(modifier = Modifier.height(dimensionResource(R.dimen.little_margin)))
                                    HorizontalDivider()
                                }
                                Spacer(modifier = Modifier.height(dimensionResource(R.dimen.little_margin)))
                                Text(
                                    text = stringResource(R.string.alert_source, alert.source!!),
                                    color = DayNightTheme.colors.bodyColor,
                                    style = MaterialTheme.typography.bodyMedium
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
