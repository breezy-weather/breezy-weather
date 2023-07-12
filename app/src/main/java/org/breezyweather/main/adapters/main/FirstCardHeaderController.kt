package org.breezyweather.main.adapters.main

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.widget.LinearLayout
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import org.breezyweather.R
import org.breezyweather.common.basic.GeoActivity
import org.breezyweather.common.basic.models.Location
import org.breezyweather.common.extensions.getFormattedDate
import org.breezyweather.common.extensions.getFormattedTime
import org.breezyweather.common.extensions.is12Hour
import org.breezyweather.common.utils.helpers.IntentHelper
import org.breezyweather.main.utils.MainThemeColorProvider
import org.breezyweather.theme.compose.BreezyWeatherTheme
import org.breezyweather.theme.compose.DayNightTheme
import org.breezyweather.theme.compose.rememberThemeRipple

@SuppressLint("InflateParams")
class FirstCardHeaderController(
    private val mActivity: GeoActivity, location: Location
) : View.OnClickListener {
    private val mView: View = LayoutInflater.from(mActivity).inflate(R.layout.container_main_first_card_header, null)
    private val mFormattedId: String = location.formattedId
    private var mContainer: LinearLayout? = null

    init {
        if (location.weather != null && location.weather.alertList.isNotEmpty()) {
            mView.visibility = View.VISIBLE
            mView.setOnClickListener {
                IntentHelper.startAlertActivity(mActivity, mFormattedId)
            }
            mView.findViewById<ComposeView>(R.id.container_main_first_card_alert_list).apply {
                // Dispose of the Composition when the view's LifecycleOwner
                // is destroyed
                setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
                setContent {
                    BreezyWeatherTheme(lightTheme = !isSystemInDarkTheme()) {
                        ContentView(location)
                    }
                }
            }
        } else {
            mView.visibility = View.GONE
        }
    }

    @Composable
    fun ContentView(location: Location) {
        if (location.weather!!.currentAlertList.isEmpty()) {
            ListItem(
                modifier = Modifier
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = rememberThemeRipple(),
                        onClick = {
                            IntentHelper.startAlertActivity(mActivity, mFormattedId)
                        }
                    ),
                colors = ListItemDefaults.colors(
                    containerColor = Color.Transparent
                ),
                headlineContent = {
                    Text(
                        stringResource(R.string.alerts_to_follow),
                        color = Color(MainThemeColorProvider.getColor(location, R.attr.colorTitleText)),
                        style = MaterialTheme.typography.titleMedium
                    )
                },
                leadingContent = {
                    Icon(
                        painterResource(R.drawable.ic_alert),
                        contentDescription = stringResource(R.string.alerts_to_follow),
                        tint = Color(MainThemeColorProvider.getColor(location, R.attr.colorTitleText))
                    )
                }
            )
        } else {
            // TODO: Lazy
            Column {
                location.weather.currentAlertList.forEach { currentAlert ->
                    ListItem(
                        modifier = Modifier
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = rememberThemeRipple(),
                                onClick = {
                                    IntentHelper.startAlertActivity(mActivity, mFormattedId)
                                }
                            ),
                        colors = ListItemDefaults.colors(
                            containerColor = Color.Transparent
                        ),
                        headlineContent = {
                            Text(
                                currentAlert.description,
                                color = Color(MainThemeColorProvider.getColor(location, R.attr.colorTitleText)),
                                style = MaterialTheme.typography.titleMedium
                            )
                        },
                        supportingContent = if (currentAlert.startDate != null) {
                            {
                                val builder = StringBuilder()
                                val startDateDay = currentAlert.startDate.getFormattedDate(
                                    location.timeZone, mActivity.getString(R.string.date_format_long)
                                )
                                builder.append(startDateDay)
                                    .append(", ")
                                    .append(
                                        currentAlert.startDate.getFormattedTime(
                                            location.timeZone,
                                            mActivity.is12Hour
                                        )
                                    )
                                if (currentAlert.endDate != null) {
                                    builder.append(" — ")
                                    val endDateDay = currentAlert.endDate.getFormattedDate(
                                        location.timeZone,
                                        mActivity.getString(R.string.date_format_long)
                                    )
                                    if (startDateDay != endDateDay) {
                                        builder.append(endDateDay)
                                            .append(", ")
                                    }
                                    builder.append(
                                        currentAlert.endDate.getFormattedTime(
                                            location.timeZone,
                                            mActivity.is12Hour
                                        )
                                    )
                                }
                                Text(
                                    builder.toString(),
                                    color = DayNightTheme.colors.bodyColor
                                )
                            }
                        } else null,
                        leadingContent = {
                            Icon(
                                painterResource(R.drawable.ic_alert),
                                contentDescription = stringResource(R.string.alerts_to_follow),
                                tint = Color(currentAlert.color)
                            )
                        }
                    )
                }
            }
        }
    }

    fun bind(firstCardContainer: LinearLayout?) {
        mContainer = firstCardContainer
        mContainer!!.addView(mView, 0)
    }

    fun unbind() {
        mContainer?.let {
            it.removeViewAt(0)
            mContainer = null
        }
    }

    // interface.
    @SuppressLint("NonConstantResourceId")
    override fun onClick(v: View) {
        IntentHelper.startAlertActivity(mActivity, mFormattedId)
    }
}
