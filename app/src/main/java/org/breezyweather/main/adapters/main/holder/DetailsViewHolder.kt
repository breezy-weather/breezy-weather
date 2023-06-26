package org.breezyweather.main.adapters.main.holder

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.TextView
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import org.breezyweather.R
import org.breezyweather.common.basic.GeoActivity
import org.breezyweather.common.basic.models.Location
import org.breezyweather.common.basic.models.options.appearance.DetailDisplay
import org.breezyweather.common.basic.models.weather.Current
import org.breezyweather.settings.SettingsManager
import org.breezyweather.theme.ThemeManager.Companion.getInstance
import org.breezyweather.theme.compose.BreezyWeatherTheme
import org.breezyweather.theme.compose.DayNightTheme
import org.breezyweather.theme.resource.providers.ResourceProvider
import org.breezyweather.theme.weatherView.WeatherViewController

class DetailsViewHolder(parent: ViewGroup) : AbstractMainCardViewHolder(
    LayoutInflater
        .from(parent.context)
        .inflate(R.layout.container_main_details, parent, false)
) {
    private val mTitle: TextView = itemView.findViewById(R.id.container_main_details_title)
    private val mDetailsList: ComposeView = itemView.findViewById(R.id.container_main_details_list)

    override fun onBindView(
        activity: GeoActivity, location: Location, provider: ResourceProvider,
        listAnimationEnabled: Boolean, itemAnimationEnabled: Boolean, firstCard: Boolean
    ) {
        super.onBindView(
            activity, location, provider,
            listAnimationEnabled, itemAnimationEnabled, firstCard
        )
        if (location.weather?.current != null) {
            mTitle.setTextColor(
                getInstance(context)
                    .weatherThemeDelegate
                    .getThemeColors(
                        context,
                        WeatherViewController.getWeatherKind(location.weather),
                        location.isDaylight
                    )[0]
            )
            mDetailsList.setContent {
                BreezyWeatherTheme(lightTheme = !isSystemInDarkTheme()) {
                    ContentView(SettingsManager.getInstance(context).detailDisplayUnlisted, location.weather.current, location.isDaylight)
                }
            }
        }
    }

    @Composable
    private fun ContentView(detailDisplayList: List<DetailDisplay>, current: Current, isDaylight: Boolean = true) {
        Column {
            detailDisplayList.forEach { detailDisplay ->
                detailDisplay.getCurrentValue(LocalContext.current, current, isDaylight)?.let {
                    ListItem(
                        colors = ListItemDefaults.colors(
                            containerColor = Color.Transparent
                        ),
                        headlineContent = {
                            Text(
                                detailDisplay.getName(LocalContext.current),
                                fontWeight = FontWeight.Bold,
                                color = DayNightTheme.colors.titleColor
                            )
                        },
                        supportingContent = {
                            Text(
                                detailDisplay.getCurrentValue(LocalContext.current, current, isDaylight)!!,
                                color = DayNightTheme.colors.bodyColor
                            )
                        },
                        leadingContent = {
                            Icon(
                                painterResource(detailDisplay.iconId),
                                contentDescription = detailDisplay.getName(LocalContext.current),
                                tint = DayNightTheme.colors.titleColor
                            )
                        }
                    )
                }
            }
        }
    }
}
