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

package org.breezyweather.ui.theme.resource.utils

import breezyweather.domain.weather.model.WeatherCode

object Constants {
    const val ACTION_ICON_PROVIDER = "org.breezyweather.ICON_PROVIDER"
    const val META_DATA_PROVIDER_CONFIG = "org.breezyweather.PROVIDER_CONFIG"
    const val META_DATA_DRAWABLE_FILTER = "org.breezyweather.DRAWABLE_FILTER"
    const val META_DATA_ANIMATOR_FILTER = "org.breezyweather.ANIMATOR_FILTER"
    const val META_DATA_SHORTCUT_FILTER = "org.breezyweather.SHORTCUT_FILTER"
    const val META_DATA_SUN_MOON_FILTER = "org.breezyweather.SUN_MOON_FILTER"

    const val GEOMETRIC_ACTION_ICON_PROVIDER = "com.wangdaye.geometricweather.ICON_PROVIDER"
    const val GEOMETRIC_META_DATA_PROVIDER_CONFIG = "com.wangdaye.geometricweather.PROVIDER_CONFIG"
    const val GEOMETRIC_META_DATA_DRAWABLE_FILTER = "com.wangdaye.geometricweather.DRAWABLE_FILTER"
    const val GEOMETRIC_META_DATA_ANIMATOR_FILTER = "com.wangdaye.geometricweather.ANIMATOR_FILTER"
    const val GEOMETRIC_META_DATA_SHORTCUT_FILTER = "com.wangdaye.geometricweather.SHORTCUT_FILTER"
    const val GEOMETRIC_META_DATA_SUN_MOON_FILTER = "com.wangdaye.geometricweather.SUN_MOON_FILTER"

    const val CATEGORY_CHRONUS_ICON_PACK = "com.dvtonder.chronus.ICON_PACK"

    private const val RESOURCES_CLEAR = "weather_clear"
    private const val RESOURCES_PARTLY_CLOUDY = "weather_partly_cloudy"
    private const val RESOURCES_CLOUDY = "weather_cloudy"
    private const val RESOURCES_RAIN = "weather_rain"
    private const val RESOURCES_SNOW = "weather_snow"
    private const val RESOURCES_WIND = "weather_wind"
    private const val RESOURCES_FOG = "weather_fog"
    private const val RESOURCES_HAZE = "weather_haze"
    private const val RESOURCES_SLEET = "weather_sleet"
    private const val RESOURCES_HAIL = "weather_hail"
    private const val RESOURCES_THUNDER = "weather_thunder"
    private const val RESOURCES_THUNDERSTORM = "weather_thunderstorm"

    const val RESOURCES_SUN = "sun"
    const val RESOURCES_MOON = "moon"

    private const val SHORTCUTS_CLEAR = "shortcuts_clear"
    private const val SHORTCUTS_PARTLY_CLOUDY = "shortcuts_partly_cloudy"
    private const val SHORTCUTS_CLOUDY = "shortcuts_cloudy"
    private const val SHORTCUTS_RAIN = "shortcuts_rain"
    private const val SHORTCUTS_SNOW = "shortcuts_snow"
    private const val SHORTCUTS_WIND = "shortcuts_wind"
    private const val SHORTCUTS_FOG = "shortcuts_fog"
    private const val SHORTCUTS_HAZE = "shortcuts_haze"
    private const val SHORTCUTS_SLEET = "shortcuts_sleet"
    private const val SHORTCUTS_HAIL = "shortcuts_hail"
    private const val SHORTCUTS_THUNDER = "shortcuts_thunder"
    private const val SHORTCUTS_THUNDERSTORM = "shortcuts_thunderstorm"

    const val DAY = "day"
    const val NIGHT = "night"
    const val MINI = "mini"
    const val LIGHT = "light"
    const val GREY = "grey"
    const val DARK = "dark"
    const val XML = "xml"
    const val FOREGROUND = "foreground"
    const val SEPARATOR = "_"
    const val FILTER_TAG_ITEM = "item"
    const val FILTER_TAG_NAME = "name"
    const val FILTER_TAG_VALUE = "value"
    const val FILTER_TAG_CONFIG = "config"
    const val CONFIG_HAS_WEATHER_ICONS = "hasWeatherIcons"
    const val CONFIG_HAS_WEATHER_ANIMATORS = "hasWeatherAnimators"
    const val CONFIG_HAS_MINIMAL_ICONS = "hasMinimalIcons"
    const val CONFIG_HAS_SHORTCUT_ICONS = "hasShortcutIcons"
    const val CONFIG_HAS_SUN_MOON_DRAWABLES = "hasSunMoonDrawables"

    fun getResourcesName(code: WeatherCode?): String = when (code) {
        WeatherCode.CLEAR -> RESOURCES_CLEAR
        WeatherCode.PARTLY_CLOUDY -> RESOURCES_PARTLY_CLOUDY
        WeatherCode.CLOUDY -> RESOURCES_CLOUDY
        WeatherCode.RAIN -> RESOURCES_RAIN
        WeatherCode.SNOW -> RESOURCES_SNOW
        WeatherCode.WIND -> RESOURCES_WIND
        WeatherCode.FOG -> RESOURCES_FOG
        WeatherCode.HAZE -> RESOURCES_HAZE
        WeatherCode.SLEET -> RESOURCES_SLEET
        WeatherCode.HAIL -> RESOURCES_HAIL
        WeatherCode.THUNDER -> RESOURCES_THUNDER
        WeatherCode.THUNDERSTORM -> RESOURCES_THUNDERSTORM
        else -> RESOURCES_CLOUDY
    }

    fun getShortcutsName(code: WeatherCode?): String = when (code) {
        WeatherCode.CLEAR -> SHORTCUTS_CLEAR
        WeatherCode.PARTLY_CLOUDY -> SHORTCUTS_PARTLY_CLOUDY
        WeatherCode.CLOUDY -> SHORTCUTS_CLOUDY
        WeatherCode.RAIN -> SHORTCUTS_RAIN
        WeatherCode.SNOW -> SHORTCUTS_SNOW
        WeatherCode.WIND -> SHORTCUTS_WIND
        WeatherCode.FOG -> SHORTCUTS_FOG
        WeatherCode.HAZE -> SHORTCUTS_HAZE
        WeatherCode.SLEET -> SHORTCUTS_SLEET
        WeatherCode.HAIL -> SHORTCUTS_HAIL
        WeatherCode.THUNDER -> SHORTCUTS_THUNDER
        WeatherCode.THUNDERSTORM -> SHORTCUTS_THUNDERSTORM
        else -> SHORTCUTS_CLOUDY
    }
}
