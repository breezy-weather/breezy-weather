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

import android.os.Bundle
import androidx.activity.compose.setContent
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import breezyweather.data.location.LocationRepository
import breezyweather.data.weather.WeatherRepository
import breezyweather.domain.location.model.Location
import dagger.hilt.android.AndroidEntryPoint
import org.breezyweather.R
import org.breezyweather.common.basic.GeoActivity
import org.breezyweather.common.extensions.capitalize
import org.breezyweather.common.extensions.currentLocale
import org.breezyweather.common.extensions.getFormattedDate
import org.breezyweather.common.extensions.getLongWeekdayDayMonth
import org.breezyweather.common.ui.composables.PollenGrid
import org.breezyweather.common.ui.widgets.Material3CardListItem
import org.breezyweather.common.ui.widgets.Material3Scaffold
import org.breezyweather.common.ui.widgets.generateCollapsedScrollBehavior
import org.breezyweather.common.ui.widgets.getCardListItemMarginDp
import org.breezyweather.common.ui.widgets.insets.FitStatusBarTopAppBar
import org.breezyweather.common.ui.widgets.insets.bottomInsetItem
import org.breezyweather.domain.weather.model.isIndexValid
import org.breezyweather.main.utils.MainThemeColorProvider
import org.breezyweather.sources.SourceManager
import org.breezyweather.theme.ThemeManager
import org.breezyweather.theme.compose.BreezyWeatherTheme
import org.breezyweather.theme.compose.DayNightTheme
import javax.inject.Inject

// TODO: Consider moving this activity as a fragment of MainActivity, so we don't have to query the database twice
@AndroidEntryPoint
class PollenActivity : GeoActivity() {

    @Inject lateinit var sourceManager: SourceManager

    @Inject lateinit var locationRepository: LocationRepository

    @Inject lateinit var weatherRepository: WeatherRepository

    companion object {
        const val KEY_POLLEN_ACTIVITY_LOCATION_FORMATTED_ID =
            "POLLEN_ACTIVITY_LOCATION_FORMATTED_ID"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            ContentView()
        }
    }

    @Composable
    private fun ContentView() {
        val formattedId = intent.getStringExtra(KEY_POLLEN_ACTIVITY_LOCATION_FORMATTED_ID)
        val location = remember { mutableStateOf<Location?>(null) }
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

            val weather = weatherRepository.getWeatherByLocationId(
                locationC.formattedId,
                withDaily = true,
                withHourly = false,
                withMinutely = false,
                withAlerts = false
            )
            if (weather == null) {
                finish()
                return@LaunchedEffect
            }

            location.value = locationC.copy(weather = weather)
        }

        val scrollBehavior = generateCollapsedScrollBehavior()

        val isLightTheme = MainThemeColorProvider.isLightTheme(context, location.value)
        BreezyWeatherTheme(lightTheme = isLightTheme) {
            // re-setting the status bar color once the location is fetched above in the launched effect
            ThemeManager
                .getInstance(this)
                .weatherThemeDelegate
                .setSystemBarStyle(
                    context = this,
                    window = this.window,
                    statusShader = false,
                    lightStatus = isLightTheme,
                    navigationShader = false,
                    lightNavigation = isLightTheme
                )

            Material3Scaffold(
                modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
                topBar = {
                    FitStatusBarTopAppBar(
                        title = stringResource(R.string.pollen),
                        onBackPressed = { finish() },
                        scrollBehavior = scrollBehavior
                    )
                }
            ) {
                location.value?.weather?.let { weather ->
                    val pollenIndexSource = sourceManager.getPollenIndexSource(
                        if (!location.value!!.pollenSource.isNullOrEmpty()) {
                            location.value!!.pollenSource!!
                        } else {
                            location.value!!.weatherSource
                        }
                    )

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
                                                getLongWeekdayDayMonth(this@PollenActivity),
                                                location.value!!,
                                                this@PollenActivity
                                            ).capitalize(this@PollenActivity.currentLocale),
                                            color = DayNightTheme.colors.titleColor,
                                            fontWeight = FontWeight.Bold,
                                            style = MaterialTheme.typography.titleMedium
                                        )
                                        PollenGrid(
                                            pollen = pollen,
                                            pollenIndexSource = pollenIndexSource
                                        )
                                    }
                                }
                            }
                        }

                        bottomInsetItem(
                            extraHeight = getCardListItemMarginDp(this@PollenActivity).dp
                        )
                    }
                }
            }
        }
    }
}
