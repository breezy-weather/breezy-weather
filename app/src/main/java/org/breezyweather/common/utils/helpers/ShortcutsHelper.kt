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

package org.breezyweather.common.utils.helpers

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.Drawable
import android.graphics.drawable.Icon
import android.os.Build
import androidx.annotation.RequiresApi
import breezyweather.domain.weather.model.WeatherCode
import org.breezyweather.theme.resource.ResourceHelper
import org.breezyweather.theme.resource.providers.ResourceProvider

/**
 * Shortcuts manager.
 */
@RequiresApi(api = Build.VERSION_CODES.N_MR1)
object ShortcutsHelper {

    private fun drawableToBitmap(drawable: Drawable): Bitmap {
        val bitmap = Bitmap.createBitmap(
            drawable.intrinsicWidth,
            drawable.intrinsicHeight,
            Bitmap.Config.ARGB_8888
        )
        val canvas = Canvas(bitmap)
        drawable.setBounds(0, 0, drawable.intrinsicWidth, drawable.intrinsicHeight)
        drawable.draw(canvas)
        return bitmap
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    fun getAdaptiveIcon(
        provider: ResourceProvider,
        code: WeatherCode,
        daytime: Boolean,
    ): Icon {
        return Icon.createWithAdaptiveBitmap(
            drawableToBitmap(
                ResourceHelper.getShortcutsForegroundIcon(provider, code, daytime)
            )
        )
    }

    fun getIcon(provider: ResourceProvider, code: WeatherCode, daytime: Boolean): Icon {
        return Icon.createWithBitmap(
            drawableToBitmap(
                ResourceHelper.getShortcutsIcon(provider, code, daytime)
            )
        )
    }
}
