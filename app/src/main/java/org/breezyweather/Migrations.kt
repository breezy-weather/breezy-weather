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

package org.breezyweather

import android.content.Context
import org.breezyweather.background.forecast.TodayForecastNotificationJob
import org.breezyweather.background.forecast.TomorrowForecastNotificationJob
import org.breezyweather.background.weather.WeatherUpdateJob
import org.breezyweather.common.basic.models.options.appearance.CardDisplay
import org.breezyweather.common.basic.models.options.appearance.DailyTrendDisplay
import org.breezyweather.common.basic.models.options.appearance.HourlyTrendDisplay
import org.breezyweather.settings.SettingsManager
import java.io.File

object Migrations {

    /**
     * Performs a migration when the application is updated.
     *
     * @return true if a migration is performed, false otherwise.
     */
    fun upgrade(context: Context): Boolean {
        val lastVersionCode = SettingsManager.getInstance(context).lastVersionCode
        val oldVersion = lastVersionCode
        if (oldVersion < BuildConfig.VERSION_CODE) {
            if (oldVersion > 0) { // Not fresh install
                if (oldVersion < 50000) {
                    // V5.0.0 adds many new charts
                    // Adding it to people who customized their hourly trends tabs so they don't miss
                    // this new feature. This can still be removed by user from settings
                    // as this code is only executed once, after migrating from a version < 5.0.0
                    try {
                        val curHourlyTrendDisplayList = HourlyTrendDisplay.toValue(
                            SettingsManager.getInstance(context).hourlyTrendDisplayList
                        )
                        if (curHourlyTrendDisplayList != SettingsManager.DEFAULT_HOURLY_TREND_DISPLAY) {
                            SettingsManager.getInstance(context).hourlyTrendDisplayList =
                                HourlyTrendDisplay.toHourlyTrendDisplayList(
                                    "$curHourlyTrendDisplayList&feels_like&humidity&pressure&cloud_cover&visibility"
                                )
                        }
                        val curDailyTrendDisplayList = DailyTrendDisplay.toValue(
                            SettingsManager.getInstance(context).dailyTrendDisplayList
                        )
                        if (curDailyTrendDisplayList != SettingsManager.DEFAULT_DAILY_TREND_DISPLAY) {
                            SettingsManager.getInstance(context).dailyTrendDisplayList =
                                DailyTrendDisplay.toDailyTrendDisplayList("$curDailyTrendDisplayList&feels_like")
                        }
                    } catch (ignored: Throwable) {
                        // ignored
                    }

                    // Delete old ObjectBox database
                    context.applicationInfo?.dataDir?.let {
                        val file = File("$it/files/objectbox/")
                        if (file.exists() && file.isDirectory) {
                            file.deleteRecursively()
                        }
                    }
                }
                if (oldVersion < 50102) {
                    // V5.1.2 adds daily sunshine chart
                    try {
                        val curDailyTrendDisplayList =
                            DailyTrendDisplay.toValue(SettingsManager.getInstance(context).dailyTrendDisplayList)
                        if (curDailyTrendDisplayList != SettingsManager.DEFAULT_DAILY_TREND_DISPLAY) {
                            SettingsManager.getInstance(context).dailyTrendDisplayList =
                                DailyTrendDisplay.toDailyTrendDisplayList("$curDailyTrendDisplayList&sunshine")
                        }
                    } catch (ignored: Throwable) {
                        // ignored
                    }
                }

                if (oldVersion < 50108) {
                    // V5.1.8 adds precipitation nowcast as a dedicated card
                    try {
                        val curCardDisplayList =
                            CardDisplay.toValue(SettingsManager.getInstance(context).cardDisplayList)
                        if (curCardDisplayList != SettingsManager.DEFAULT_CARD_DISPLAY) {
                            SettingsManager.getInstance(context).cardDisplayList =
                                CardDisplay.toCardDisplayList("precipitation_nowcast&$curCardDisplayList")
                        }
                    } catch (ignored: Throwable) {
                        // ignored
                    }
                }
            }

            SettingsManager.getInstance(context).lastVersionCode = BuildConfig.VERSION_CODE

            // Always set up background tasks to ensure they're running
            WeatherUpdateJob.setupTask(context) // This will also refresh data immediately
            TodayForecastNotificationJob.setupTask(context, false)
            TomorrowForecastNotificationJob.setupTask(context, false)

            return oldVersion != 0
        }

        return false
    }
}
