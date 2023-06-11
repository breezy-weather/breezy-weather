package wangdaye.com.geometricweather.common.ui.activities

import android.content.Context
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.Text
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import wangdaye.com.geometricweather.R
import wangdaye.com.geometricweather.common.basic.GeoActivity
import wangdaye.com.geometricweather.common.basic.models.Location
import wangdaye.com.geometricweather.common.basic.models.weather.Alert
import wangdaye.com.geometricweather.common.ui.widgets.Material3CardListItem
import wangdaye.com.geometricweather.common.ui.widgets.Material3Scaffold
import wangdaye.com.geometricweather.common.ui.widgets.generateCollapsedScrollBehavior
import wangdaye.com.geometricweather.common.ui.widgets.getCardListItemMarginDp
import wangdaye.com.geometricweather.common.ui.widgets.insets.FitStatusBarTopAppBar
import wangdaye.com.geometricweather.common.ui.widgets.insets.bottomInsetItem
import wangdaye.com.geometricweather.common.utils.DisplayUtils
import wangdaye.com.geometricweather.common.utils.helpers.AsyncHelper
import wangdaye.com.geometricweather.db.repositories.LocationEntityRepository
import wangdaye.com.geometricweather.db.repositories.WeatherEntityRepository
import wangdaye.com.geometricweather.theme.compose.DayNightTheme
import wangdaye.com.geometricweather.theme.compose.GeometricWeatherTheme
import java.util.*

class AlertActivity : GeoActivity() {

    companion object {
        const val KEY_FORMATTED_ID = "formatted_id"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            GeometricWeatherTheme(lightTheme = !isSystemInDarkTheme()) {
                ContentView()
            }
        }
    }

    private fun getAlertDate(context: Context, alert: Alert, timeZone: TimeZone): String {
        val builder = StringBuilder()
        if (alert.startDate != null) {
            val startDateDay = DisplayUtils.getFormattedDate(
                alert.startDate,
                timeZone,
                context.getString(R.string.date_format_long)
            )
            builder.append(startDateDay)
                .append(", ")
                .append(
                    DisplayUtils.getFormattedDate(
                        alert.startDate,
                        timeZone,
                        if (DisplayUtils.is12Hour(context)) "h:mm aa" else "HH:mm"
                    )
                )
            if (alert.endDate != null) {
                builder.append(" — ")
                val endDateDay = DisplayUtils.getFormattedDate(
                    alert.endDate,
                    timeZone,
                    context.getString(R.string.date_format_long)
                )
                if (startDateDay != endDateDay) {
                    builder.append(endDateDay)
                        .append(", ")
                }
                builder.append(
                    DisplayUtils.getFormattedDate(
                        alert.endDate,
                        timeZone,
                        if (DisplayUtils.is12Hour(context)) "h:mm aa" else "HH:mm"
                    )
                )
            }
        }
        return builder.toString()
    }

    @Composable
    private fun ContentView() {
        val alertList = remember { mutableStateOf(emptyList<Alert>()) }
        val timeZone = remember { mutableStateOf(TimeZone.getDefault()) }
        val context = LocalContext.current

        val formattedId = intent.getStringExtra(KEY_FORMATTED_ID)
        AsyncHelper.runOnIO({ emitter ->
            var location: Location? = null
            if (!formattedId.isNullOrEmpty()) {
                location = LocationEntityRepository.readLocation(formattedId)
            }
            if (location == null) {
                location = LocationEntityRepository.readLocationList(context)[0]
            }
            val weather = WeatherEntityRepository.readWeather(location)

            emitter.send(Pair(location.timeZone, weather?.alertList ?: emptyList()), true)
        }) { result: Pair<TimeZone, List<Alert>>?, _ ->
            result?.let {
                timeZone.value = it.first
                alertList.value = it.second
            }
        }

        val scrollBehavior = generateCollapsedScrollBehavior()

        Material3Scaffold(
            modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
            topBar = {
                FitStatusBarTopAppBar(
                    title = stringResource(R.string.action_alert),
                    onBackPressed = { finish() },
                    scrollBehavior = scrollBehavior,
                )
            },
        ) {
            LazyColumn(
                modifier = Modifier.fillMaxHeight(),
                contentPadding = it,
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
                            if(alert.content != null) Text(
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