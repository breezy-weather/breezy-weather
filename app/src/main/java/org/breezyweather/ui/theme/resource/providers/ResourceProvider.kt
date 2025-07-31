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

package org.breezyweather.ui.theme.resource.providers

import android.animation.Animator
import android.content.Context
import android.graphics.drawable.Drawable
import android.graphics.drawable.Icon
import android.net.Uri
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.annotation.Size
import breezyweather.domain.weather.reference.WeatherCode
import org.breezyweather.ui.theme.resource.utils.ResourceUtils

abstract class ResourceProvider {
    protected open fun getDrawableUri(resName: String): Uri {
        return ResourceUtils.getDrawableUri(packageName, "drawable", resName)
    }

    abstract val packageName: String
    abstract var providerName: String?
    abstract val providerIcon: Drawable?
    override fun equals(other: Any?): Boolean {
        return if (other is ResourceProvider) other.packageName == packageName else false
    }

    // weather icon.
    @Size(3)
    abstract fun getWeatherIcons(code: WeatherCode?, dayTime: Boolean): Array<Drawable?>
    abstract fun getWeatherIcon(code: WeatherCode?, dayTime: Boolean): Drawable
    abstract fun getWeatherIconUri(code: WeatherCode?, dayTime: Boolean): Uri

    // animator.
    @Size(3)
    abstract fun getWeatherAnimators(code: WeatherCode?, dayTime: Boolean): Array<Animator?>

    // minimal icon.
    @RequiresApi(api = Build.VERSION_CODES.M)
    abstract fun getMinimalIcon(code: WeatherCode?, dayTime: Boolean): Icon
    abstract fun getMinimalLightIcon(code: WeatherCode?, dayTime: Boolean): Drawable
    abstract fun getMinimalLightIconUri(code: WeatherCode?, dayTime: Boolean): Uri
    abstract fun getMinimalGreyIcon(code: WeatherCode?, dayTime: Boolean): Drawable
    abstract fun getMinimalGreyIconUri(code: WeatherCode?, dayTime: Boolean): Uri
    abstract fun getMinimalDarkIcon(code: WeatherCode?, dayTime: Boolean): Drawable
    abstract fun getMinimalDarkIconUri(code: WeatherCode?, dayTime: Boolean): Uri
    abstract fun getMinimalXmlIcon(code: WeatherCode?, dayTime: Boolean): Drawable

    // shortcut.
    abstract fun getShortcutsIcon(code: WeatherCode?, dayTime: Boolean): Drawable
    abstract fun getShortcutsForegroundIcon(code: WeatherCode?, dayTime: Boolean): Drawable

    // sun and moon.
    abstract val sunDrawable: Drawable

    abstract val moonDrawable: Drawable

    companion object {
        @JvmStatic
        protected fun getResId(context: Context, resName: String, type: String): Int {
            return ResourceUtils.getResId(context, resName, type)
        }
    }
}
