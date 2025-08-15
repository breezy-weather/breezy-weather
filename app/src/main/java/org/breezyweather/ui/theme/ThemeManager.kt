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
import breezyweather.domain.location.model.Location
import org.breezyweather.common.extensions.uiModeManager
import org.breezyweather.common.options.DarkMode
import org.breezyweather.domain.location.model.isDaylight
import org.breezyweather.domain.settings.SettingsManager
import org.breezyweather.ui.theme.weatherView.WeatherThemeDelegate
import org.breezyweather.ui.theme.weatherView.materialWeatherView.MaterialWeatherThemeDelegate

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
        ): Boolean = when (SettingsManager.getInstance(context).darkMode) {
            DarkMode.LIGHT -> daylight == true
            DarkMode.DARK -> SettingsManager.getInstance(context).dayNightModeForLocations && daylight == true
            else -> if (context.uiModeManager?.nightMode != UiModeManager.MODE_NIGHT_YES) {
                daylight == true
            } else {
                SettingsManager.getInstance(context).dayNightModeForLocations && daylight == true
            }
        }
    }
}
