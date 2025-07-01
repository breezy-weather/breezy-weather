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

package org.breezyweather.common.utils

import android.graphics.Bitmap
import android.graphics.Color
import android.media.ThumbnailUtils
import androidx.annotation.ColorInt
import androidx.core.graphics.ColorUtils
import kotlin.math.ln
import androidx.core.graphics.get

object ColorUtils {

    @ColorInt
    fun bitmapToColorInt(bitmap: Bitmap): Int {
        return ThumbnailUtils.extractThumbnail(bitmap, 1, 1)[0, 0]
    }

    fun isLightColor(@ColorInt color: Int): Boolean {
        val alpha = 0xFF shl 24
        var grey = color
        val red = grey and 0x00FF0000 shr 16
        val green = grey and 0x0000FF00 shr 8
        val blue = grey and 0x000000FF
        grey = (red * 0.3 + green * 0.59 + blue * 0.11).toInt()
        grey = alpha or (grey shl 16) or (grey shl 8) or grey
        return grey > -0x424243
    }

    fun getDarkerColor(@ColorInt color: Int): Int {
        val hsv = FloatArray(3)
        Color.colorToHSV(color, hsv)
        hsv[1] = hsv[1] + 0.15f
        hsv[2] = hsv[2] - 0.15f
        return Color.HSVToColor(hsv)
    }

    @ColorInt
    fun blendColor(@ColorInt foreground: Int, @ColorInt background: Int): Int {
        val scr = Color.red(foreground)
        val scg = Color.green(foreground)
        val scb = Color.blue(foreground)
        val sa = foreground ushr 24
        val dcr = Color.red(background)
        val dcg = Color.green(background)
        val dcb = Color.blue(background)
        val colorR = dcr * (0xff - sa) / 0xff + scr * sa / 0xff
        val colorG = dcg * (0xff - sa) / 0xff + scg * sa / 0xff
        val colorB = dcb * (0xff - sa) / 0xff + scb * sa / 0xff
        return (colorR shl 16) + (colorG shl 8) + colorB or -0x1000000
    }

    @ColorInt
    fun getWidgetSurfaceColor(
        elevationDp: Float,
        @ColorInt tintColor: Int,
        @ColorInt surfaceColor: Int,
    ): Int {
        if (elevationDp == 0f) return surfaceColor
        val foreground = ColorUtils.setAlphaComponent(
            tintColor,
            ((4.5f * ln((elevationDp + 1).toDouble()) + 2f) / 100f * 255).toInt()
        )
        return blendColor(foreground, surfaceColor)
    }
}
