package wangdaye.com.geometricweather.common.ui.activities

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import wangdaye.com.geometricweather.R
import wangdaye.com.geometricweather.common.basic.GeoActivity
import wangdaye.com.geometricweather.common.basic.models.options.unit.PollenUnit
import wangdaye.com.geometricweather.common.ui.widgets.Material3CardListItem
import wangdaye.com.geometricweather.common.ui.widgets.Material3Scaffold
import wangdaye.com.geometricweather.common.ui.widgets.generateCollapsedScrollBehavior
import wangdaye.com.geometricweather.common.ui.widgets.getCardListItemMarginDp
import wangdaye.com.geometricweather.common.ui.widgets.insets.FitStatusBarTopAppBar
import wangdaye.com.geometricweather.common.ui.widgets.insets.bottomInsetItem
import wangdaye.com.geometricweather.db.repositories.LocationEntityRepository
import wangdaye.com.geometricweather.db.repositories.WeatherEntityRepository
import wangdaye.com.geometricweather.theme.compose.DayNightTheme
import wangdaye.com.geometricweather.theme.compose.GeometricWeatherTheme

class AllergenActivity : GeoActivity() {

    companion object {
        const val KEY_ALLERGEN_ACTIVITY_LOCATION_FORMATTED_ID =
            "ALLERGEN_ACTIVITY_LOCATION_FORMATTED_ID"
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
        val formattedId = intent.getStringExtra(KEY_ALLERGEN_ACTIVITY_LOCATION_FORMATTED_ID) ?: ""
        var location = LocationEntityRepository.readLocation(formattedId)
            ?: LocationEntityRepository.readLocationList(LocalContext.current)[0]

        location = location.copy(weather = WeatherEntityRepository.readWeather(location))
        val weather = location.weather
        if (weather == null) {
            finish()
            return
        }

        val unit = PollenUnit.PPCM

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
                items(weather.dailyForecast) { daily ->
                    daily.pollen?.let {
                        Material3CardListItem {
                            Column {
                                Text(
                                    modifier = Modifier.padding(dimensionResource(R.dimen.normal_margin)),
                                    text = daily.getDate(stringResource(R.string.date_format_widget_long), location.timeZone),
                                    color = DayNightTheme.colors.titleColor,
                                    fontWeight = FontWeight.Bold,
                                    style = MaterialTheme.typography.titleMedium,
                                )
                                Row {
                                    PollenItem(
                                        modifier = Modifier
                                            .width(0.dp)
                                            .weight(1f),
                                        title = stringResource(R.string.grass),
                                        subtitle = unit.getValueText(
                                            this@AllergenActivity,
                                            it.grassIndex ?: 0
                                        ) + " - " + it.grassDescription,
                                        tintColor = Color(
                                            it.getGrassColor(this@AllergenActivity)
                                        )
                                    )
                                    PollenItem(
                                        modifier = Modifier
                                            .width(0.dp)
                                            .weight(1f),
                                        title = stringResource(R.string.ragweed),
                                        subtitle = unit.getValueText(
                                            this@AllergenActivity,
                                            it.ragweedIndex ?: 0
                                        ) + " - " + it.ragweedDescription,
                                        tintColor = Color(
                                            it.getRagweedColor(this@AllergenActivity)
                                        )
                                    )
                                }
                                Row {
                                    PollenItem(
                                        modifier = Modifier
                                            .width(0.dp)
                                            .weight(1f),
                                        title = stringResource(R.string.tree),
                                        subtitle = unit.getValueText(
                                            this@AllergenActivity,
                                            it.treeIndex ?: 0
                                        ) + " - " + it.treeDescription,
                                        tintColor = Color(
                                            it.getTreeColor(this@AllergenActivity)
                                        )
                                    )
                                    PollenItem(
                                        modifier = Modifier
                                            .width(0.dp)
                                            .weight(1f),
                                        title = stringResource(R.string.mold),
                                        subtitle = unit.getValueText(
                                            this@AllergenActivity,
                                            it.moldIndex ?: 0
                                        ) + " - " + it.moldDescription,
                                        tintColor = Color(
                                            it.getMoldColor(this@AllergenActivity)
                                        )
                                    )
                                }
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

    @Composable
    private fun PollenItem(
        modifier: Modifier,
        title: String,
        subtitle: String,
        tintColor: Color,
    ) = Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            modifier = Modifier
                .padding(dimensionResource(R.dimen.normal_margin))
                .size(dimensionResource(R.dimen.material_icon_size)),
            painter = painterResource(R.drawable.ic_circle_medium),
            contentDescription = null,
            tint = tintColor,
        )
        Column(
            Modifier.padding(
                end = dimensionResource(R.dimen.normal_margin),
                top = dimensionResource(R.dimen.normal_margin),
                bottom = dimensionResource(R.dimen.normal_margin),
            )
        ) {
            Text(
                text = title,
                color = DayNightTheme.colors.titleColor,
                style = MaterialTheme.typography.titleMedium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                text = subtitle,
                color = DayNightTheme.colors.bodyColor,
                style = MaterialTheme.typography.bodyMedium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}