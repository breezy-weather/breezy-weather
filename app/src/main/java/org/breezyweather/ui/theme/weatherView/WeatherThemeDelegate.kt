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

package org.breezyweather.ui.theme.weatherView

import android.content.Context
import android.view.Window
import androidx.annotation.ColorInt
import androidx.annotation.Px
import androidx.annotation.Size

interface WeatherThemeDelegate {

    fun getWeatherView(context: Context): WeatherView

    /**
     * @return colors[] {
     * theme color,
     * color of daytime chart line,
     * color of nighttime chart line
     * }
     *
     */
    @ColorInt
    @Size(3)
    fun getThemeColors(
        context: Context,
        weatherKind: Int,
        daylight: Boolean,
    ): IntArray

    @ColorInt
    fun getBackgroundColor(
        context: Context,
        weatherKind: Int,
        daylight: Boolean,
    ): Int

    @Px
    fun getHeaderTopMargin(context: Context): Int

    @ColorInt
    fun getHeaderTextColor(context: Context): Int

    fun setSystemBarStyle(
        window: Window,
        statusShader: Boolean,
        lightStatus: Boolean,
        lightNavigation: Boolean,
    )

    @Px
    fun getHomeCardRadius(context: Context): Float

    @Px
    fun getHomeCardElevation(context: Context): Float

    @Px
    fun getHomeCardMargins(context: Context): Int
}
