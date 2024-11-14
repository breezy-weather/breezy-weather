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

package org.breezyweather.common.ui.widgets

import android.graphics.Shader

class DayNightShaderWrapper @JvmOverloads constructor(
    targetWidth: Int,
    targetHeight: Int,
    lightTheme: Boolean = true,
    colors: IntArray = IntArray(0),
    shader: Shader? = null,
) {
    var shader: Shader? = null
        private set
    var targetWidth = 0
        private set
    var targetHeight = 0
        private set
    var isLightTheme = false
        private set
    private var mColors: IntArray = IntArray(0)

    init {
        setShader(shader, targetWidth, targetHeight, lightTheme, colors)
    }

    fun isDifferent(
        targetWidth: Int,
        targetHeight: Int,
        lightTheme: Boolean,
        colors: IntArray,
    ): Boolean {
        if (shader == null ||
            this.targetWidth != targetWidth ||
            this.targetHeight != targetHeight ||
            isLightTheme != lightTheme ||
            mColors.size != colors.size
        ) {
            return true
        }
        for (i in colors.indices) {
            if (mColors[i] != colors[i]) {
                return true
            }
        }
        return false
    }

    fun setShader(
        shader: Shader?,
        targetWidth: Int,
        targetHeight: Int,
        lightTheme: Boolean,
        colors: IntArray,
    ) {
        this.shader = shader
        this.targetWidth = targetWidth
        this.targetHeight = targetHeight
        isLightTheme = lightTheme
        mColors = colors.copyOf(colors.size)
    }
}
