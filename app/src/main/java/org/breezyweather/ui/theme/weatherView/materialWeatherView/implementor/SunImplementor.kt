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
            canvas.translate(canvasSizes[0] + deltaX, (0.0333 * canvasSizes[0] + deltaY).toFloat())
            mPaint.alpha = ((1 - scrollRate) * 255 * 0.40).toInt()
            canvas.rotate(mAngles[0])
            for (i in 0..3) {
                canvas.drawRect(
                    -mUnitSizes[0],
                    -mUnitSizes[0],
                    mUnitSizes[0],
                    mUnitSizes[0],
                    mPaint
                )
                canvas.rotate(22.5f)
            }
            canvas.rotate(-90 - mAngles[0])
            mPaint.alpha = ((1 - scrollRate) * 255 * 0.16).toInt()
            canvas.rotate(mAngles[1])
            for (i in 0..3) {
                canvas.drawRect(
                    -mUnitSizes[1],
                    -mUnitSizes[1],
                    mUnitSizes[1],
                    mUnitSizes[1],
                    mPaint
                )
                canvas.rotate(22.5f)
            }
            canvas.rotate(-90 - mAngles[1])
            mPaint.alpha = ((1 - scrollRate) * 255 * 0.08).toInt()
            canvas.rotate(mAngles[2])
            for (i in 0..3) {
                canvas.drawRect(
                    -mUnitSizes[2],
                    -mUnitSizes[2],
                    mUnitSizes[2],
                    mUnitSizes[2],
                    mPaint
                )
                canvas.rotate(22.5f)
            }
            canvas.rotate(-90 - mAngles[2])
        }
    }

    companion object {
        @get:ColorInt
        val themeColor: Int
            get() = Color.rgb(253, 188, 76)
    }
}
