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

package org.breezyweather.wallpaper

import android.content.Context
import org.breezyweather.domain.settings.ConfigStore

class LiveWallpaperConfigManager(context: Context) {
    val weatherKind: String
    val dayNightType: String
    val animationsEnabled: Boolean

    init {
        val config = ConfigStore(context, SP_LIVE_WALLPAPER_CONFIG)
        weatherKind = config.getString(KEY_WEATHER_KIND, null) ?: "auto"
        dayNightType = config.getString(KEY_DAY_NIGHT_TYPE, null) ?: "auto"
        animationsEnabled = config.getBoolean(KEY_ANIMATIONS_ENABLED, false)
    }

    companion object {
        private const val SP_LIVE_WALLPAPER_CONFIG = "live_wallpaper_config"
        private const val KEY_WEATHER_KIND = "weather_kind"
        private const val KEY_DAY_NIGHT_TYPE = "day_night_type"
        private const val KEY_ANIMATIONS_ENABLED = "animations_enabled"

        fun update(context: Context, weatherKind: String?, dayNightType: String?, animationsEnabled: Boolean) {
            ConfigStore(context, SP_LIVE_WALLPAPER_CONFIG)
                .edit()
                .putString(KEY_WEATHER_KIND, weatherKind)
                .putString(KEY_DAY_NIGHT_TYPE, dayNightType)
                .putBoolean(KEY_ANIMATIONS_ENABLED, animationsEnabled)
                .apply()
        }
    }
}
