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

package org.breezyweather.common.ui.activities

import android.content.Context
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import breezyweather.data.location.LocationRepository
import breezyweather.data.weather.WeatherRepository
import org.breezyweather.common.basic.GeoActivity
import breezyweather.domain.location.model.Location
import org.breezyweather.common.ui.widgets.Material3CardListItem
import org.breezyweather.common.ui.widgets.Material3Scaffold
import org.breezyweather.common.ui.widgets.generateCollapsedScrollBehavior
import org.breezyweather.common.ui.widgets.getCardListItemMarginDp
import org.breezyweather.common.ui.widgets.insets.FitStatusBarTopAppBar
import org.breezyweather.common.ui.widgets.insets.bottomInsetItem
import org.breezyweather.theme.compose.DayNightTheme
import org.breezyweather.theme.compose.BreezyWeatherTheme
import org.breezyweather.R
import breezyweather.domain.weather.model.Alert
import dagger.hilt.android.AndroidEntryPoint
import org.breezyweather.common.extensions.getFormattedDate
import org.breezyweather.common.extensions.getFormattedTime
import org.breezyweather.common.extensions.is12Hour
import org.breezyweather.common.utils.ColorUtils
import org.breezyweather.main.utils.MainThemeColorProvider
import java.util.TimeZone
import javax.inject.Inject

// TODO: Consider moving this activity as a fragment of MainActivity, so we don't have to query the database twice
@AndroidEntryPoint
class AlertActivity : GeoActivity() {

    @Inject
    lateinit var locationRepository: LocationRepository

    @Inject
    lateinit var weatherRepository: WeatherRepository

    companion object {
        const val KEY_FORMATTED_ID = "formatted_id"
        const val KEY_ALERT_ID = "alert_id"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            ContentView()
        }
    }

    private fun getAlertDate(context: Context, alert: Alert, timeZone: TimeZone): String {
        val builder = StringBuilder()
        alert.startDate?.let { startDate ->
            val startDateDay = startDate.getFormattedDate(
                timeZone, context.getString(R.string.date_format_long)
            )
            builder.append(startDateDay)
                .append(context.getString(R.string.comma_separator))
                .append(startDate.getFormattedTime(timeZone, context.is12Hour))
            alert.endDate?.let { endDate ->
                builder.append(" — ")
                val endDateDay = endDate.getFormattedDate(
                    timeZone, context.getString(R.string.date_format_long)
                )
                if (startDateDay != endDateDay) {
                    builder.append(endDateDay).append(context.getString(R.string.comma_separator))
                }
                builder.append(endDate.getFormattedTime(timeZone, context.is12Hour))
            }
        }
        return builder.toString()
    }

    @Composable
    private fun ContentView() {
        val formattedId = intent.getStringExtra(KEY_FORMATTED_ID)
        val alertList = remember { mutableStateOf(emptyList<Alert>()) }
        val location = remember { mutableStateOf<Location?>(null) }
        val listState = rememberLazyListState()
        val context = LocalContext.current

        LaunchedEffect(formattedId) {
            var locationC: Location? = null
            if (!formattedId.isNullOrEmpty()) {
                locationC = locationRepository.getLocation(formattedId, withParameters = false)
            }
            if (locationC == null) {
                locationC = locationRepository.getFirstLocation(withParameters = false)
            }
            if (locationC == null) {
                finish()
                return@LaunchedEffect
            }
            location.value = locationC

            val alerts = weatherRepository.getAlertListByLocationId(locationC.formattedId)
            alertList.value = alerts

            if (alerts.isNotEmpty()) {
                val alertId = intent.getStringExtra(KEY_ALERT_ID)
                if (!alertId.isNullOrEmpty()) {
                    val alertIndex = alerts.indexOfFirst { it.alertId == alertId }
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

        val scrollBehavior = generateCollapsedScrollBehavior()

        BreezyWeatherTheme(lightTheme = MainThemeColorProvider.isLightTheme(context, location.value)) {
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
                                modifier = Modifier
                                    .padding(dimensionResource(R.dimen.normal_margin))
                                    .fillMaxWidth(),
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        painterResource(R.drawable.ic_alert),
                                        contentDescription = alert.description,
                                        tint = Color(ColorUtils.getDarkerColor(alert.color))
                                    )
                                    Spacer(modifier = Modifier.width(dimensionResource(R.dimen.normal_margin)))
                                    Column {
                                        Text(
                                            text = alert.description,
                                            color = DayNightTheme.colors.titleColor,
                                            fontWeight = FontWeight.Bold,
                                            style = MaterialTheme.typography.titleMedium,
                                        )
                                        Text(
                                            text = getAlertDate(context, alert, location.value!!.javaTimeZone),
                                            color = DayNightTheme.colors.captionColor,
                                            style = MaterialTheme.typography.labelMedium,
                                        )
                                    }
                                }
                                alert.content?.let { content ->
                                    Spacer(modifier = Modifier.height(dimensionResource(R.dimen.little_margin)))
                                    Text(
                                        text = content,
                                        color = DayNightTheme.colors.bodyColor,
                                        style = MaterialTheme.typography.bodyMedium,
                                    )
                                }
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
}