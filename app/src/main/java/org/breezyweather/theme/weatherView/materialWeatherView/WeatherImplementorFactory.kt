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

package org.breezyweather.theme.weatherView.materialWeatherView

import androidx.annotation.DrawableRes
import androidx.annotation.Size
import org.breezyweather.R
import org.breezyweather.theme.weatherView.WeatherView
import org.breezyweather.theme.weatherView.WeatherView.WeatherKindRule
import org.breezyweather.theme.weatherView.materialWeatherView.MaterialWeatherView.WeatherAnimationImplementor
import org.breezyweather.theme.weatherView.materialWeatherView.implementor.CloudImplementor
import org.breezyweather.theme.weatherView.materialWeatherView.implementor.HailImplementor
import org.breezyweather.theme.weatherView.materialWeatherView.implementor.MeteorShowerImplementor
import org.breezyweather.theme.weatherView.materialWeatherView.implementor.RainImplementor
import org.breezyweather.theme.weatherView.materialWeatherView.implementor.SnowImplementor
import org.breezyweather.theme.weatherView.materialWeatherView.implementor.SunImplementor
import org.breezyweather.theme.weatherView.materialWeatherView.implementor.WindImplementor

object WeatherImplementorFactory {

    fun getWeatherImplementor(
        @WeatherKindRule weatherKind: Int,
        daytime: Boolean,
        @Size(2) sizes: IntArray,
        animate: Boolean,
    ): WeatherAnimationImplementor? = when (weatherKind) {
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

        WeatherView.WEATHER_KIND_CLOUDY ->
            CloudImplementor(
                sizes,
                animate,
                CloudImplementor.TYPE_CLOUDY,
                daytime
            )

        WeatherView.WEATHER_KIND_CLOUD ->
            CloudImplementor(
                sizes,
                animate,
                CloudImplementor.TYPE_CLOUD,
                daytime
            )

        WeatherView.WEATHER_KIND_FOG ->
            CloudImplementor(
                sizes,
                animate,
                CloudImplementor.TYPE_FOG,
                daytime
            )

        WeatherView.WEATHER_KIND_HAIL ->
            HailImplementor(
                sizes,
                animate,
                daytime
            )

        WeatherView.WEATHER_KIND_HAZE ->
            CloudImplementor(
                sizes,
                animate,
                CloudImplementor.TYPE_HAZE,
                daytime
            )

        WeatherView.WEATHER_KIND_RAINY ->
            RainImplementor(
                sizes,
                animate,
                RainImplementor.TYPE_RAIN,
                daytime
            )

        WeatherView.WEATHER_KIND_SNOW ->
            SnowImplementor(
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

        WeatherView.WEATHER_KIND_SLEET ->
            RainImplementor(
                sizes,
                animate,
                RainImplementor.TYPE_SLEET,
                daytime
            )

        else -> null
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

        WeatherView.WEATHER_KIND_CLOUD -> if (daylight) {
            R.drawable.weather_background_partly_cloudy_day
        } else {
            R.drawable.weather_background_partly_cloudy_night
        }

        WeatherView.WEATHER_KIND_CLOUDY -> if (daylight) {
            R.drawable.weather_background_cloudy_day
        } else {
            R.drawable.weather_background_cloudy_night
        }

        WeatherView.WEATHER_KIND_FOG -> if (daylight) {
            R.drawable.weather_background_fog_day
        } else {
            R.drawable.weather_background_fog_night
        }

        WeatherView.WEATHER_KIND_HAIL -> if (daylight) {
            R.drawable.weather_background_hail_day
        } else {
            R.drawable.weather_background_hail_night
        }

        WeatherView.WEATHER_KIND_HAZE -> if (daylight) {
            R.drawable.weather_background_haze_day
        } else {
            R.drawable.weather_background_haze_night
        }

        WeatherView.WEATHER_KIND_RAINY -> if (daylight) {
            R.drawable.weather_background_rain_day
        } else {
            R.drawable.weather_background_rain_night
        }

        WeatherView.WEATHER_KIND_SLEET -> if (daylight) {
            R.drawable.weather_background_sleet_day
        } else {
            R.drawable.weather_background_sleet_night
        }

        WeatherView.WEATHER_KIND_SNOW -> if (daylight) {
            R.drawable.weather_background_snow_day
        } else {
            R.drawable.weather_background_snow_night
        }

        WeatherView.WEATHER_KIND_THUNDER, WeatherView.WEATHER_KIND_THUNDERSTORM -> if (daylight) {
            R.drawable.weather_background_thunder_day
        } else {
            R.drawable.weather_background_thunder_night
        }

        WeatherView.WEATHER_KIND_WIND -> if (daylight) {
            R.drawable.weather_background_wind_day
        } else {
            R.drawable.weather_background_wind_night
        }

        else ->
            R.drawable.weather_background_default
    }
}
