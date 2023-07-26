package org.breezyweather.common.ui.activities

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import org.breezyweather.R
import org.breezyweather.common.basic.GeoActivity
import org.breezyweather.common.extensions.getFormattedDate
import org.breezyweather.common.ui.composables.AllergenGrid
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

class AllergenActivity : GeoActivity() {

    companion object {
        const val KEY_ALLERGEN_ACTIVITY_LOCATION_FORMATTED_ID =
            "ALLERGEN_ACTIVITY_LOCATION_FORMATTED_ID"
    }

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
        val formattedId = intent.getStringExtra(KEY_ALLERGEN_ACTIVITY_LOCATION_FORMATTED_ID) ?: ""
        var location = LocationEntityRepository.readLocation(formattedId)
            ?: LocationEntityRepository.readLocationList()[0]

        location = location.copy(weather = WeatherEntityRepository.readWeather(location))
        val weather = location.weather
        if (weather == null) {
            finish()
            return
        }


        val scrollBehavior = generateCollapsedScrollBehavior()

        Material3Scaffold(
            modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
            topBar = {
                FitStatusBarTopAppBar(
                    title = stringResource(R.string.allergen),
                    onBackPressed = { finish() },
                    scrollBehavior = scrollBehavior,
                )
            },
        ) {
            LazyColumn(
                modifier = Modifier.fillMaxHeight(),
                contentPadding = it,
            ) {
                items(weather.dailyForecast.filter { d -> d.allergen?.isValid == true }) { daily ->
                    daily.allergen?.let { allergen ->
                        Material3CardListItem(
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column {
                                Text(
                                    modifier = Modifier.padding(dimensionResource(R.dimen.normal_margin)),
                                    text = daily.date.getFormattedDate(location.timeZone, stringResource(R.string.date_format_widget_long)),
                                    color = DayNightTheme.colors.titleColor,
                                    fontWeight = FontWeight.Bold,
                                    style = MaterialTheme.typography.titleMedium,
                                )
                                AllergenGrid(allergen = allergen)
                            }
                        }
                    }
                }

                bottomInsetItem(
                    extraHeight = getCardListItemMarginDp(this@AllergenActivity).dp
                )
            }
        }
    }
}