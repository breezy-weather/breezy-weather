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

package org.breezyweather.ui.theme.weatherView.materialWeatherView

import android.content.Context
import android.content.res.Configuration
import androidx.annotation.DrawableRes
import androidx.annotation.Size
import org.breezyweather.ui.theme.weatherView.R
import org.breezyweather.ui.theme.weatherView.WeatherView
import org.breezyweather.ui.theme.weatherView.WeatherView.WeatherKindRule
import org.breezyweather.ui.theme.weatherView.materialWeatherView.MaterialWeatherView.WeatherAnimationImplementor
import org.breezyweather.ui.theme.weatherView.materialWeatherView.implementor.CloudImplementor
import org.breezyweather.ui.theme.weatherView.materialWeatherView.implementor.HailImplementor
import org.breezyweather.ui.theme.weatherView.materialWeatherView.implementor.MeteorShowerImplementor
import org.breezyweather.ui.theme.weatherView.materialWeatherView.implementor.RainImplementor
import org.breezyweather.ui.theme.weatherView.materialWeatherView.implementor.SnowImplementor
import org.breezyweather.ui.theme.weatherView.materialWeatherView.implementor.SunImplementor
import org.breezyweather.ui.theme.weatherView.materialWeatherView.implementor.WindImplementor

object WeatherImplementorFactory {

    fun getWeatherImplementor(
        context: Context,
        @WeatherKindRule weatherKind: Int,
        daytime: Boolean,
        @Size(2) sizes: IntArray,
        animate: Boolean,
    ): WeatherAnimationImplementor? {
        val darkMode = (context.resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK) ==
            Configuration.UI_MODE_NIGHT_YES
        return when (weatherKind) {
            WeatherView.WEATHER_KIND_CLEAR -> if (daytime) {
                SunImplementor(
                    sizes,
                    animate
                )
            } else {
                MeteorShowerImplementor(
                    sizes,
                    animate
                )
            }

            WeatherView.WEATHER_KIND_CLOUD ->
                CloudImplementor(
                    sizes,
                    animate,
                    CloudImplementor.TYPE_CLOUD,
                    daytime,
                    darkMode
                )

            WeatherView.WEATHER_KIND_CLOUDY ->
                CloudImplementor(
                    sizes,
                    animate,
                    CloudImplementor.TYPE_CLOUDY,
                    daytime,
                    darkMode
                )

            WeatherView.WEATHER_KIND_FOG ->
                CloudImplementor(
                    sizes,
                    animate,
                    CloudImplementor.TYPE_FOG,
                    daytime,
                    darkMode
                )

            WeatherView.WEATHER_KIND_HAZE ->
                CloudImplementor(
                    sizes,
                    animate,
                    CloudImplementor.TYPE_HAZE,
                    daytime,
                    darkMode
                )

            WeatherView.WEATHER_KIND_RAINY ->
                RainImplementor(
                    sizes,
                    animate,
                    RainImplementor.TYPE_RAIN,
                    daytime
                )

            WeatherView.WEATHER_KIND_SLEET ->
                RainImplementor(
                    sizes,
                    animate,
                    RainImplementor.TYPE_SLEET,
                    daytime
                )

            WeatherView.WEATHER_KIND_SNOW ->
                SnowImplementor(
                    sizes,
                    animate,
                    daytime
                )

            WeatherView.WEATHER_KIND_HAIL ->
                HailImplementor(
                    sizes,
                    animate,
                    daytime
                )

            WeatherView.WEATHER_KIND_THUNDERSTORM ->
                RainImplementor(
                    sizes,
                    animate,
                    RainImplementor.TYPE_THUNDERSTORM,
                    daytime
                )

            WeatherView.WEATHER_KIND_THUNDER ->
                CloudImplementor(
                    sizes,
                    animate,
                    CloudImplementor.TYPE_THUNDER,
                    daytime
                )

            WeatherView.WEATHER_KIND_WIND ->
                WindImplementor(
                    sizes,
                    animate,
                    daytime
                )

            else -> null
        }
    }

    @DrawableRes
    fun getBackgroundId(
        @WeatherKindRule weatherKind: Int,
        daylight: Boolean,
    ): Int = when (weatherKind) {
        WeatherView.WEATHER_KIND_CLEAR -> if (daylight) {
            R.drawable.weather_background_clear_day
        } else {
            R.drawable.weather_background_clear_night
        }
        WeatherView.WEATHER_KIND_CLOUD -> R.drawable.weather_background_partly_cloudy
        WeatherView.WEATHER_KIND_CLOUDY -> R.drawable.weather_background_cloudy
        WeatherView.WEATHER_KIND_FOG -> R.drawable.weather_background_fog
        WeatherView.WEATHER_KIND_HAIL -> R.drawable.weather_background_hail
        WeatherView.WEATHER_KIND_HAZE -> R.drawable.weather_background_haze
        WeatherView.WEATHER_KIND_RAINY -> R.drawable.weather_background_rain
        WeatherView.WEATHER_KIND_SLEET -> R.drawable.weather_background_sleet
        WeatherView.WEATHER_KIND_SNOW -> R.drawable.weather_background_snow
        WeatherView.WEATHER_KIND_THUNDER, WeatherView.WEATHER_KIND_THUNDERSTORM -> R.drawable.weather_background_thunder
        WeatherView.WEATHER_KIND_WIND -> R.drawable.weather_background_wind
        else -> R.drawable.weather_background_default
    }
}
