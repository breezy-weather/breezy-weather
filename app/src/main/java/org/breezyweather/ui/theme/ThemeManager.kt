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
import android.util.TypedValue
import androidx.annotation.AttrRes
import breezyweather.domain.location.model.Location
import org.breezyweather.common.basic.models.options.DarkMode
import org.breezyweather.common.extensions.uiModeManager
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

        /**
         * Can be used in the future to support different themes
         */
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
         * - “Always light”: will use light theme, unless it’s nighttime in the location
         * - “Always dark”: will always use dark mode
         * - “Follow system”: will follow system, unless it’s light and nighttime in the location
         */
        fun isLightTheme(
            context: Context,
            daylight: Boolean? = null,
        ): Boolean = when (SettingsManager.getInstance(context).darkMode) {
            DarkMode.LIGHT -> daylight != false
            DarkMode.DARK -> false
            else -> context.uiModeManager?.nightMode != UiModeManager.MODE_NIGHT_YES && daylight != false
        }

        fun getColor(context: Context, @AttrRes id: Int): Int {
            val typedValue = TypedValue()
            context.theme.resolveAttribute(id, typedValue, true)
            return typedValue.data
        }
    }
}
