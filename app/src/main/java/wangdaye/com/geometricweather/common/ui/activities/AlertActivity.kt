package wangdaye.com.geometricweather.common.ui.activities

import android.os.Bundle
import android.text.TextUtils
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
import wangdaye.com.geometricweather.common.utils.helpers.AsyncHelper
import wangdaye.com.geometricweather.db.DatabaseHelper
import wangdaye.com.geometricweather.theme.compose.DayNightTheme
import wangdaye.com.geometricweather.theme.compose.GeometricWeatherTheme
import java.text.DateFormat

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

    @Composable
    private fun ContentView() {
        val alertList = remember { mutableStateOf(emptyList<Alert>()) }

        val formattedId = intent.getStringExtra(KEY_FORMATTED_ID)
        AsyncHelper.runOnIO({ emitter ->
            var location: Location? = null
            if (!TextUtils.isEmpty(formattedId)) {
                location = DatabaseHelper.getInstance(this).readLocation(formattedId!!)
            }
            if (location == null) {
                location = DatabaseHelper.getInstance(this).readLocationList()[0]
            }
            val weather = DatabaseHelper.getInstance(this).readWeather(
                location!!
            )
            if (weather != null) {
                emitter.send(weather.alertList, true)
            } else {
                emitter.send(ArrayList(), true)
            }
        }) { alerts: List<Alert>?, _ ->
            alerts?.let { alertList.value = it }
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
            LazyColumn(modifier = Modifier.fillMaxHeight()) {
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
                                text = DateFormat
                                    .getDateTimeInstance(DateFormat.LONG, DateFormat.DEFAULT)
                                    .format(alert.date),
                                color = DayNightTheme.colors.captionColor,
                                style = MaterialTheme.typography.labelMedium,
                            )
                            Spacer(modifier = Modifier.height(dimensionResource(R.dimen.little_margin)))
                            Text(
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