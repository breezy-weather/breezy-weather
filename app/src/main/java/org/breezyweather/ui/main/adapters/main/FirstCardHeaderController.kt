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

package org.breezyweather.ui.main.adapters.main

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
import breezyweather.domain.location.model.Location
import org.breezyweather.R
import org.breezyweather.common.basic.GeoActivity
import org.breezyweather.common.extensions.getFormattedMediumDayAndMonth
import org.breezyweather.common.extensions.getFormattedTime
import org.breezyweather.common.extensions.is12Hour
import org.breezyweather.common.utils.ColorUtils
import org.breezyweather.common.utils.helpers.IntentHelper
import org.breezyweather.ui.main.utils.MainThemeColorProvider
import org.breezyweather.ui.theme.compose.BreezyWeatherTheme
import org.breezyweather.ui.theme.compose.DayNightTheme
import org.breezyweather.ui.theme.compose.themeRipple
import java.util.Date

@SuppressLint("InflateParams")
class FirstCardHeaderController(
    private val mActivity: GeoActivity,
    location: Location,
) : View.OnClickListener {
    private val mView: View = LayoutInflater.from(mActivity).inflate(R.layout.container_main_first_card_header, null)
    private val mFormattedId: String = location.formattedId
    private var mContainer: LinearLayout? = null

    init {
        // Don’t show if alertList only contains alerts in the past
        if (location.weather?.alertList?.any { it.endDate == null || it.endDate!!.time > Date().time } == true) {
            mView.visibility = View.VISIBLE
            mView.setOnClickListener {
                IntentHelper.startAlertActivity(mActivity, mFormattedId)
            }
            mView.findViewById<ComposeView>(R.id.container_main_first_card_alert_list).setContent {
                BreezyWeatherTheme(lightTheme = MainThemeColorProvider.isLightTheme(mActivity, location)) {
                    if (location.weather!!.currentAlertList.isEmpty()) {
                        UpcomingAlerts()
                    } else {
                        AlertList(location)
                    }
                }
            }
        } else {
            mView.visibility = View.GONE
        }
    }

    @Composable
    fun UpcomingAlerts(
        modifier: Modifier = Modifier,
    ) {
        ListItem(
            modifier = modifier
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = themeRipple(),
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
    }

    @Composable
    fun AlertList(
        location: Location,
        modifier: Modifier = Modifier,
    ) {
        Column {
            location.weather!!.currentAlertList.forEach { currentAlert ->
                ListItem(
                    modifier = modifier
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = themeRipple(),
                            onClick = {
                                IntentHelper.startAlertActivity(mActivity, mFormattedId, currentAlert.alertId)
                            }
                        ),
                    colors = ListItemDefaults.colors(
                        containerColor = Color.Transparent
                    ),
                    headlineContent = {
                        Text(
                            currentAlert.headline?.ifEmpty {
                                stringResource(R.string.alert)
                            } ?: stringResource(R.string.alert),
                            color = DayNightTheme.colors.titleColor,
                            style = MaterialTheme.typography.titleMedium
                        )
                    },
                    supportingContent = currentAlert.startDate?.let { startDate ->
                        {
                            val builder = StringBuilder()
                            val startDateDay = startDate.getFormattedMediumDayAndMonth(location, mActivity)
                            builder.append(startDateDay)
                                .append(stringResource(R.string.comma_separator))
                                .append(startDate.getFormattedTime(location, mActivity, mActivity.is12Hour))
                            currentAlert.endDate?.let { endDate ->
                                builder.append(" — ")
                                val endDateDay = endDate.getFormattedMediumDayAndMonth(location, mActivity)
                                if (startDateDay != endDateDay) {
                                    builder.append(endDateDay)
                                        .append(stringResource(R.string.comma_separator))
                                }
                                builder.append(endDate.getFormattedTime(location, mActivity, mActivity.is12Hour))
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
                            contentDescription = currentAlert.headline?.ifEmpty {
                                stringResource(R.string.alert)
                            } ?: stringResource(R.string.alert),
                            tint = Color(ColorUtils.getDarkerColor(currentAlert.color))
                        )
                    }
                )
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
