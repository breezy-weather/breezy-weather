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

package org.breezyweather.main.adapters.main

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.widget.LinearLayout
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import org.breezyweather.R
import org.breezyweather.common.basic.GeoActivity
import breezyweather.domain.location.model.Location
import org.breezyweather.common.extensions.getFormattedDate
import org.breezyweather.common.extensions.getFormattedTime
import org.breezyweather.common.extensions.is12Hour
import org.breezyweather.common.utils.ColorUtils
import org.breezyweather.common.utils.helpers.IntentHelper
import org.breezyweather.main.utils.MainThemeColorProvider
import org.breezyweather.theme.compose.BreezyWeatherTheme
import org.breezyweather.theme.compose.DayNightTheme
import org.breezyweather.theme.compose.rememberThemeRipple
import java.util.Date

@SuppressLint("InflateParams")
class FirstCardHeaderController(
    private val mActivity: GeoActivity, location: Location
) : View.OnClickListener {
    private val mView: View = LayoutInflater.from(mActivity).inflate(R.layout.container_main_first_card_header, null)
    private val mFormattedId: String = location.formattedId
    private var mContainer: LinearLayout? = null

    init {
        // Don’t show if alertList only contains alerts in the past
        if (location.weather?.alertList?.any {
            (it.endDate?.time ?: 0L) > Date().time
        } == true) {
            mView.visibility = View.VISIBLE
            mView.setOnClickListener {
                IntentHelper.startAlertActivity(mActivity, mFormattedId)
            }
            mView.findViewById<ComposeView>(R.id.container_main_first_card_alert_list).setContent {
                BreezyWeatherTheme(lightTheme = MainThemeColorProvider.isLightTheme(mActivity, location)) {
                    ContentView(location)
                }
            }
        } else {
            mView.visibility = View.GONE
        }
    }

    @Composable
    fun ContentView(location: Location) {
        val weather = location.weather ?: return
        if (weather.currentAlertList.isEmpty()) {
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
                        color = DayNightTheme.colors.titleColor,
                        style = MaterialTheme.typography.titleMedium
                    )
                },
                leadingContent = {
                    Icon(
                        painterResource(R.drawable.ic_alert),
                        contentDescription = stringResource(R.string.alerts_to_follow),
                        tint = DayNightTheme.colors.titleColor
                    )
                }
            )
        } else {
            // TODO: Lazy
            Column {
                weather.currentAlertList.forEach { currentAlert ->
                    ListItem(
                        modifier = Modifier
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = rememberThemeRipple(),
                                onClick = {
                                    IntentHelper.startAlertActivity(mActivity, mFormattedId, currentAlert.alertId)
                                }
                            ),
                        colors = ListItemDefaults.colors(
                            containerColor = Color.Transparent
                        ),
                        headlineContent = {
                            Text(
                                currentAlert.description,
                                color = DayNightTheme.colors.titleColor,
                                style = MaterialTheme.typography.titleMedium
                            )
                        },
                        supportingContent = currentAlert.startDate?.let { startDate ->
                            {
                                val builder = StringBuilder()
                                val startDateDay = startDate.getFormattedDate(
                                    location.timeZone, mActivity.getString(R.string.date_format_long)
                                )
                                builder.append(startDateDay)
                                    .append(", ")
                                    .append(
                                        startDate.getFormattedTime(
                                            location.timeZone,
                                            mActivity.is12Hour
                                        )
                                    )
                                currentAlert.endDate?.let { endDate ->
                                    builder.append(" — ")
                                    val endDateDay = endDate.getFormattedDate(
                                        location.timeZone,
                                        mActivity.getString(R.string.date_format_long)
                                    )
                                    if (startDateDay != endDateDay) {
                                        builder.append(endDateDay)
                                            .append(", ")
                                    }
                                    builder.append(
                                        endDate.getFormattedTime(
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
                        },
                        leadingContent = {
                            Icon(
                                painterResource(R.drawable.ic_alert),
                                contentDescription = stringResource(R.string.alerts_to_follow),
                                tint = Color(ColorUtils.getDarkerColor(currentAlert.color))
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
