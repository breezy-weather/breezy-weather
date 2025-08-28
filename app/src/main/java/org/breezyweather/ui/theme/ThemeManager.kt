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

package org.breezyweather.ui.theme

import android.app.UiModeManager
import android.content.Context
import android.os.Build
import breezyweather.domain.location.model.Location
import org.breezyweather.common.extensions.uiModeManager
import org.breezyweather.common.options.DarkMode
import org.breezyweather.domain.location.model.isDaylight
import org.breezyweather.domain.settings.SettingsManager
import org.breezyweather.ui.theme.weatherView.WeatherThemeDelegate
import org.breezyweather.ui.theme.weatherView.materialWeatherView.MaterialWeatherThemeDelegate
import java.time.LocalTime

class ThemeManager private constructor(
    val weatherThemeDelegate: WeatherThemeDelegate,
) {

    companion object {

        @Volatile
        private var instance: ThemeManager? = null

        fun getInstance(context: Context): ThemeManager {
            if (instance == null) {
                synchronized(ThemeManager::class) {
                    if (instance == null) {
                        instance = ThemeManager(
                            weatherThemeDelegate = MaterialWeatherThemeDelegate()
                        )
                    }
                }
            }
            return instance!!
        }

        fun isLightTheme(
            context: Context,
            location: Location?,
        ) = isLightTheme(
            context = context,
            daylight = location?.isDaylight
        )

        /**
         * When dark mode is “Always light”, locations are light theme during the day, and dark theme during the night
         * When dark mode is “Always night”:
         * - day/night mode for locations disabled, dark theme is always used
         * - day/night mode for locations enabled, locations are light theme during the day, and dark theme during the night
         * When “follow system” is on, the logic above is applied based on current system dark mode
         */
        fun isLightTheme(
            context: Context,
            daylight: Boolean?,
        ): Boolean {
            val settings = SettingsManager.getInstance(context)
            return when (settings.darkMode) {
                DarkMode.LIGHT -> daylight == true
                DarkMode.DARK -> settings.dayNightModeForLocations && daylight == true
                else -> {
                    if (isSystemInDarkMode(context)) {
                        settings.dayNightModeForLocations && daylight == true
                    } else {
                        daylight == true
                    }
                }
            }
        }

        private fun isSystemInDarkMode(context: Context): Boolean {
            if (context.uiModeManager == null) return false
            return when (context.uiModeManager!!.nightMode) {
                UiModeManager.MODE_NIGHT_NO -> false
                UiModeManager.MODE_NIGHT_YES -> true
                UiModeManager.MODE_NIGHT_AUTO -> false // Makes the app follows location sunrise/sunset instead
                UiModeManager.MODE_NIGHT_CUSTOM -> if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                    // Note: This will not work if user manually disabled dark mode and manually enabled it,
                    //  as Android still says it's "custom"

                    val startTime = context.uiModeManager!!.customNightModeStart
                    val endTime = context.uiModeManager!!.customNightModeEnd
                    val now = LocalTime.now()

                    if (startTime.isBefore(endTime)) {
                        // 21:00 to 23:00
                        now.isAfter(startTime) && now.isBefore(endTime)
                    } else if (startTime.isAfter(endTime)) {
                        // 22:00 to 06:00
                        now.isBefore(endTime) || now.isAfter(startTime)
                    } else {
                        // Means the two values are equal or something weird happened
                        false
                    }
                } else {
                    false // Not available on Android < 11
                }
                else -> false // Unknown
            }
        }
    }
}
