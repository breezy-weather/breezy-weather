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
import android.graphics.Paint
import android.graphics.RectF
import androidx.annotation.ColorInt
import androidx.annotation.Size
import androidx.core.graphics.toColorInt
import org.breezyweather.ui.theme.weatherView.materialWeatherView.MaterialWeatherView.WeatherAnimationImplementor
import java.util.Random
import kotlin.math.cos
import kotlin.math.pow
import kotlin.math.sin

class WindImplementor(
    @Size(2) canvasSizes: IntArray,
    animate: Boolean,
    daylight: Boolean,
) : WeatherAnimationImplementor() {
    private val mAnimate = animate
    private val mPaint = Paint().apply {
        style = Paint.Style.FILL
        isAntiAlias = true
        alpha = ((if (daylight) 1f else 0.33f) * 255).toInt()
    }
    private val mWinds: Array<Wind>
    private var mLastRotation3D: Float

    private class Wind(
        private val mViewWidth: Int,
        private val mViewHeight: Int,
        @ColorInt val color: Int,
        val scale: Float,
    ) {
        var x = 0f
        var y = 0f
        var width = 0f
        var height = 0f
        var rectF: RectF = RectF()
        var speed: Float

        private val mCanvasSize: Int
        private val maxWidth: Float
        private val minWidth: Float
        private val maxHeight: Float
        private val minHeight: Float

        init {
            mCanvasSize = (mViewWidth * mViewWidth + mViewHeight * mViewHeight).toDouble().pow(0.5).toInt()
            speed = (mCanvasSize / (1000.0 * (0.5 + Random().nextDouble())) * 6.0).toFloat()
            maxHeight = 0.007f * mCanvasSize
            minHeight = 0.005f * mCanvasSize
            maxWidth = maxHeight * 10
            minWidth = minHeight * 6
            init(true)
        }

        private fun init(firstTime: Boolean) {
            val r = Random()
            y = r.nextInt(mCanvasSize).toFloat()
            x = if (firstTime) {
                (r.nextInt((mCanvasSize - maxHeight).toInt()) - mCanvasSize).toFloat()
            } else {
                -maxHeight
            }
            width = minWidth + r.nextFloat() * (maxWidth - minWidth)
            height = minHeight + r.nextFloat() * (maxHeight - minHeight)
            buildRectF()
        }

        private fun buildRectF() {
            val x = (x - (mCanvasSize - mViewWidth) * 0.5).toFloat()
            val y = (y - (mCanvasSize - mViewHeight) * 0.5).toFloat()
            rectF.set(x, y, x + width * scale, y + height * scale)
        }

        fun move(interval: Long, deltaRotation3D: Float) {
            x += speed
                .times(interval)
                .times(
                    scale.toDouble().pow(1.5) + 5 * sin(deltaRotation3D * Math.PI / 180.0) * cos(16 * Math.PI / 180.0)
                ).toFloat()
            y -= (speed * interval * 5 * sin(deltaRotation3D * Math.PI / 180.0) * sin(16 * Math.PI / 180.0))
                .toFloat()
            if (x >= mCanvasSize) {
                init(false)
            } else {
                buildRectF()
            }
        }
    }

    init {
        val colors = if (daylight) {
            intArrayOf(
                "#C2E4CA".toColorInt(), // Color.rgb(240, 200, 148),
                "#B2E0BA".toColorInt(), // Color.rgb(237, 178, 100),
                "#D2F0DA".toColorInt() // Color.rgb(209, 142, 54)
            )
        } else {
            intArrayOf(
                "#313E3A".toColorInt(), // Color.rgb(240, 200, 148),
                "#529B73".toColorInt(), // Color.rgb(237, 178, 100),
                "#638170".toColorInt() // Color.rgb(209, 142, 54)
            )
        }
        val scales = floatArrayOf(0.6f, 0.8f, 1f)
        mWinds = Array(WIND_COUNT) { i ->
            Wind(
                canvasSizes[0],
                canvasSizes[1],
                colors[i * 3 / WIND_COUNT],
                scales[i * 3 / WIND_COUNT]
            )
        }
        mLastRotation3D = INITIAL_ROTATION_3D
    }

    override fun updateData(
        @Size(2) canvasSizes: IntArray,
        interval: Long,
        rotation2D: Float,
        rotation3D: Float,
    ) {
        for (w in mWinds) {
            w.move(interval, if (mLastRotation3D == INITIAL_ROTATION_3D) 0f else rotation3D - mLastRotation3D)
        }
        mLastRotation3D = rotation3D
    }

    override fun draw(
        @Size(2) canvasSizes: IntArray,
        canvas: Canvas,
        scrollRate: Float,
        rotation2D: Float,
        rotation3D: Float,
    ) {
        var rotation2Dc = rotation2D
        if (scrollRate < 1) {
            rotation2Dc -= 16f
            canvas.rotate(
                rotation2D,
                canvasSizes[0] * 0.5f,
                canvasSizes[1] * 0.5f
            )
            for (w in mWinds) {
                mPaint.color = w.color
                mPaint.alpha = ((1 - scrollRate) * 255).toInt()
                canvas.drawRect(w.rectF, mPaint)
            }
        }
    }

    companion object {
        private const val INITIAL_ROTATION_3D = 1000f
        private const val WIND_COUNT = 160

        @ColorInt
        fun getThemeColor(daylight: Boolean): Int {
            return if (daylight) -0x15325d else -0x6a798b
        }
    }
}
