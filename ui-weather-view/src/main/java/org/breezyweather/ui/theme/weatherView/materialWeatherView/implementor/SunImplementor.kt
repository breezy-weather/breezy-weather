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

package org.breezyweather.ui.theme.weatherView.materialWeatherView.implementor

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import androidx.annotation.ColorInt
import androidx.annotation.Size
import org.breezyweather.ui.theme.weatherView.materialWeatherView.MaterialWeatherView.WeatherAnimationImplementor
import kotlin.math.sin

/**
 * Clear day implementor.
 */
class SunImplementor(
    @Size(2) canvasSizes: IntArray,
    animate: Boolean,
) : WeatherAnimationImplementor() {
    private val mAnimate = animate
    private val mPaint = Paint().apply {
        style = Paint.Style.FILL
        isAntiAlias = true
        color = Color.rgb(253, 84, 17)
    }
    private val mAngles = FloatArray(3)
    private val mUnitSizes: FloatArray = floatArrayOf(
        (0.5 * 0.47 * canvasSizes[0]).toFloat(),
        (1.7794 * 0.5 * 0.47 * canvasSizes[0]).toFloat(),
        (3.0594 * 0.5 * 0.47 * canvasSizes[0]).toFloat()
    )

    override fun updateData(
        @Size(2) canvasSizes: IntArray,
        interval: Long,
        rotation2D: Float,
        rotation3D: Float,
    ) {
        for (i in mAngles.indices) {
            mAngles[i] = ((mAngles[i] + 90.0 / (3000 + 1000 * i) * interval) % 90).toFloat()
        }
    }

    override fun draw(
        @Size(2) canvasSizes: IntArray,
        canvas: Canvas,
        scrollRate: Float,
        rotation2D: Float,
        rotation3D: Float,
    ) {
        if (scrollRate < 1) {
            val deltaX = (sin(rotation2D * Math.PI / 180.0) * 0.3 * canvasSizes[0]).toFloat()
            val deltaY = (sin(rotation3D * Math.PI / 180.0) * -0.3 * canvasSizes[0]).toFloat()
            canvas.translate(canvasSizes[0] + deltaX, (SUN_POSITION * canvasSizes[0] + deltaY).toFloat())

            arrayOf(SMALL_SUN_ALPHA, MEDIUM_SUN_ALPHA, LARGE_SUN_ALPHA).forEachIndexed { index, alpha ->
                mPaint.alpha = ((1 - scrollRate) * 255 * alpha).toInt()
                canvas.rotate(mAngles[index])
                for (i in 0..3) {
                    canvas.drawRect(
                        -mUnitSizes[index],
                        -mUnitSizes[index],
                        mUnitSizes[index],
                        mUnitSizes[index],
                        mPaint
                    )
                    canvas.rotate(22.5f)
                }
                canvas.rotate(-90 - mAngles[index])
            }
        }
    }

    companion object {
        const val SUN_POSITION = 0.0333
        const val SMALL_SUN_ALPHA = 0.40
        const val MEDIUM_SUN_ALPHA = 0.16
        const val LARGE_SUN_ALPHA = 0.08

        @get:ColorInt
        val themeColor: Int
            get() = Color.rgb(253, 188, 76)
    }
}
