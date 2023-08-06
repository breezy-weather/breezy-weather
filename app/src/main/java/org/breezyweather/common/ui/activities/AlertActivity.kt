/**
 * This file is part of Breezy Weather.
 *
 * Breezy Weather is free software: you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published by the
 * Free Software Foundation, either version 3 of the License, or (at your
 * option) any later version.
 *
 * Breezy Weather is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public
 * License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Breezy Weather. If not, see <https://www.gnu.org/licenses/>.
 */

package org.breezyweather.common.ui.activities

import android.content.Context
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import org.breezyweather.common.basic.GeoActivity
import org.breezyweather.common.basic.models.Location
import org.breezyweather.common.ui.widgets.Material3CardListItem
import org.breezyweather.common.ui.widgets.Material3Scaffold
import org.breezyweather.common.ui.widgets.generateCollapsedScrollBehavior
import org.breezyweather.common.ui.widgets.getCardListItemMarginDp
import org.breezyweather.common.ui.widgets.insets.FitStatusBarTopAppBar
import org.breezyweather.common.ui.widgets.insets.bottomInsetItem
import org.breezyweather.db.repositories.LocationEntityRepository
import org.breezyweather.db.repositories.WeatherEntityRepository
import org.breezyweather.theme.compose.DayNightTheme
import org.breezyweather.theme.compose.BreezyWeatherTheme
import org.breezyweather.R
import org.breezyweather.common.basic.models.weather.Alert
import org.breezyweather.common.extensions.getFormattedDate
import org.breezyweather.common.extensions.getFormattedTime
import org.breezyweather.common.extensions.is12Hour
import org.breezyweather.common.utils.helpers.AsyncHelper
import java.util.Date
import java.util.TimeZone

class AlertActivity : GeoActivity() {

    companion object {
        const val KEY_FORMATTED_ID = "formatted_id"
        const val KEY_ALERT_ID = "alert_id"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            BreezyWeatherTheme(lightTheme = !isSystemInDarkTheme()) {
                ContentView()
            }
        }
    }

    private fun getAlertDate(context: Context, alert: Alert, timeZone: TimeZone): String {
        val builder = StringBuilder()
        if (alert.startDate != null) {
            val startDateDay = alert.startDate.getFormattedDate(
                timeZone, context.getString(R.string.date_format_long)
            )
            builder.append(startDateDay)
                .append(", ")
                .append(alert.startDate.getFormattedTime(timeZone, context.is12Hour))
            if (alert.endDate != null) {
                builder.append(" — ")
                val endDateDay = alert.endDate.getFormattedDate(
                    timeZone, context.getString(R.string.date_format_long)
                )
                if (startDateDay != endDateDay) {
                    builder.append(endDateDay).append(", ")
                }
                builder.append(alert.endDate.getFormattedTime(timeZone, context.is12Hour))
            }
        }
        return builder.toString()
    }

    @Composable
    private fun ContentView() {
        val alertList = remember { mutableStateOf(emptyList<Alert>()) }
        val timeZone = remember { mutableStateOf(TimeZone.getDefault()) }
        val listState = rememberLazyListState()
        val coroutineScope = rememberCoroutineScope()
        val context = LocalContext.current

        val formattedId = intent.getStringExtra(KEY_FORMATTED_ID)
        AsyncHelper.runOnIO({ emitter ->
            var location: Location? = null
            if (!formattedId.isNullOrEmpty()) {
                location = LocationEntityRepository.readLocation(formattedId)
            }
            if (location == null) {
                // FIXME: doesn’t display alerts for current position for China provider if not in first position
                location = LocationEntityRepository.readLocationList()[0]
            }
            val weather = WeatherEntityRepository.readWeather(location)

            // Don’t emit alerts from the past
            emitter.send(Pair(location.timeZone, weather?.alertList?.filter { it.endDate == null || it.endDate.time > Date().time } ?: emptyList()), true)
        }) { result: Pair<TimeZone, List<Alert>>?, _ ->
            result?.let {
                timeZone.value = it.first
                alertList.value = it.second

                if (it.second.isNotEmpty()) {
                    coroutineScope.launch {
                        val alertId = intent.getLongExtra(KEY_ALERT_ID, -1)
                        if (alertId != -1L) {
                            val alertIndex =
                                it.second.indexOfFirst { alert -> alert.alertId == alertId }
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
            }
        }

        val scrollBehavior = generateCollapsedScrollBehavior()

        Material3Scaffold(
            modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
            topBar = {
                FitStatusBarTopAppBar(
                    title = stringResource(R.string.alerts),
                    onBackPressed = { finish() },
                    scrollBehavior = scrollBehavior,
                )
            },
        ) {
            LazyColumn(
                modifier = Modifier.fillMaxHeight(),
                contentPadding = it,
                state = listState
            ) {
                items(alertList.value) { alert ->
                    Material3CardListItem {
                        Column(
                            modifier = Modifier.padding(dimensionResource(R.dimen.normal_margin)),
                        ) {
                            Text(
                                text = alert.description,
                                color = DayNightTheme.colors.titleColor,
                                fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.titleMedium,
                            )
                            Text(
                                text = getAlertDate(context, alert, timeZone.value),
                                color = DayNightTheme.colors.captionColor,
                                style = MaterialTheme.typography.labelMedium,
                            )
                            Spacer(modifier = Modifier.height(dimensionResource(R.dimen.little_margin)))
                            if (alert.content != null) Text(
                                text = alert.content,
                                color = DayNightTheme.colors.bodyColor,
                                style = MaterialTheme.typography.bodyMedium,
                            )
                        }
                    }
                }

                bottomInsetItem(
                    extraHeight = getCardListItemMarginDp(this@AlertActivity).dp
                )
            }
        }
    }
}