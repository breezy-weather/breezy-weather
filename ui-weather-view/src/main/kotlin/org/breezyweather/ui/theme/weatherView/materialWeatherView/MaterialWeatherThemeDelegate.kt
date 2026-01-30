/*
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

package org.breezyweather.ui.theme.weatherView.materialWeatherView

import android.content.Context
import android.content.res.Configuration
import android.graphics.Color
import androidx.core.graphics.ColorUtils
import org.breezyweather.ui.theme.weatherView.WeatherThemeDelegate
import org.breezyweather.ui.theme.weatherView.WeatherView
import org.breezyweather.ui.theme.weatherView.WeatherView.WeatherKindRule
import org.breezyweather.ui.theme.weatherView.materialWeatherView.implementor.CloudImplementor
import org.breezyweather.ui.theme.weatherView.materialWeatherView.implementor.HailImplementor
import org.breezyweather.ui.theme.weatherView.materialWeatherView.implementor.MeteorShowerImplementor
import org.breezyweather.ui.theme.weatherView.materialWeatherView.implementor.RainImplementor
import org.breezyweather.ui.theme.weatherView.materialWeatherView.implementor.SnowImplementor
import org.breezyweather.ui.theme.weatherView.materialWeatherView.implementor.SunImplementor
import org.breezyweather.ui.theme.weatherView.materialWeatherView.implementor.WindImplementor

class MaterialWeatherThemeDelegate : WeatherThemeDelegate {

    companion object {

        fun getBrighterColor(color: Int): Int {
            val hsv = FloatArray(3)
            Color.colorToHSV(color, hsv)
            hsv[1] = hsv[1] - 0.25f
            hsv[2] = hsv[2] + 0.25f
            return Color.HSVToColor(hsv)
        }

        private fun innerGetBackgroundColor(
            @WeatherKindRule weatherKind: Int,
            daytime: Boolean,
        ): Int = when (weatherKind) {
            WeatherView.WEATHER_KIND_CLEAR -> if (daytime) {
                SunImplementor.themeColor
            } else {
                MeteorShowerImplementor.themeColor
            }
            WeatherView.WEATHER_KIND_CLOUD -> CloudImplementor.getThemeColor(CloudImplementor.TYPE_CLOUD, daytime)
            WeatherView.WEATHER_KIND_CLOUDY -> CloudImplementor.getThemeColor(CloudImplementor.TYPE_CLOUDY, daytime)
            WeatherView.WEATHER_KIND_FOG -> CloudImplementor.getThemeColor(CloudImplementor.TYPE_FOG, daytime)
            WeatherView.WEATHER_KIND_HAIL -> HailImplementor.getThemeColor(daytime)
            WeatherView.WEATHER_KIND_HAZE -> CloudImplementor.getThemeColor(CloudImplementor.TYPE_HAZE, daytime)
            WeatherView.WEATHER_KIND_RAINY -> RainImplementor.getThemeColor(RainImplementor.TYPE_RAIN, daytime)
            WeatherView.WEATHER_KIND_SLEET -> RainImplementor.getThemeColor(RainImplementor.TYPE_SLEET, daytime)
            WeatherView.WEATHER_KIND_SNOW -> SnowImplementor.getThemeColor(daytime)
            WeatherView.WEATHER_KIND_THUNDERSTORM ->
                RainImplementor.getThemeColor(RainImplementor.TYPE_THUNDERSTORM, daytime)
            WeatherView.WEATHER_KIND_THUNDER -> CloudImplementor.getThemeColor(CloudImplementor.TYPE_THUNDER, daytime)
            WeatherView.WEATHER_KIND_WIND -> WindImplementor.getThemeColor(daytime)
            else -> Color.TRANSPARENT
        }
    }

    override fun getWeatherView(context: Context): WeatherView = MaterialWeatherView(context)

    override fun getThemeColors(
        context: Context,
        weatherKind: Int,
        daylight: Boolean,
    ): IntArray {
        var color = innerGetBackgroundColor(weatherKind, daylight)
        if (!daylight) {
            color = getBrighterColor(color)
        }
        return intArrayOf(
            color,
            color,
            ColorUtils.setAlphaComponent(color, (0.5 * 255).toInt())
        )
    }

    override fun isLightBackground(
        context: Context,
        weatherKind: Int,
        daylight: Boolean,
    ): Boolean {
        return daylight &&
            (context.resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK) !=
            Configuration.UI_MODE_NIGHT_YES
    }

    override fun getBackgroundColor(
        context: Context,
        weatherKind: Int,
        daylight: Boolean,
    ): Int {
        return innerGetBackgroundColor(weatherKind, daylight)
    }

    override fun getOnBackgroundColor(
        context: Context,
        weatherKind: Int,
        daylight: Boolean,
    ): Int {
        return if (isLightBackground(context, weatherKind, daylight)) Color.BLACK else Color.WHITE
    }
}
