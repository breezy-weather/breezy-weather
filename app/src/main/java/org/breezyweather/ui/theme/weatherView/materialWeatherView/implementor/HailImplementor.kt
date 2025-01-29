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
import android.graphics.RectF
import androidx.annotation.ColorInt
import androidx.annotation.Size
import org.breezyweather.ui.theme.weatherView.materialWeatherView.MaterialWeatherView.WeatherAnimationImplementor
import java.util.Random
import kotlin.math.pow
import kotlin.math.sin

/**
 * Hail implementor.
 */
class HailImplementor(
    @Size(2) canvasSizes: IntArray,
    animate: Boolean,
    daylight: Boolean,
) : WeatherAnimationImplementor() {
    private val mAnimate = animate
    private val mPaint = Paint().apply {
        style = Paint.Style.FILL
        isAntiAlias = true
    }
    private val mHails: Array<Hail>
    private var mLastRotation3D: Float

    private class Hail(
        private val mViewWidth: Int,
        private val mViewHeight: Int,
        @field:ColorInt @param:ColorInt val color: Int,
        val scale: Float,
    ) {
        var cx = 0f
        var cy = 0f
        var centerX = 0f
        var centerY = 0f
        var size: Float
        var rotation = 0f
        var speedY: Float
        var speedX = 0f
        var speedRotation = 0f
        var rectF = RectF()
        private val mCanvasSize: Int

        init {
            mCanvasSize = (mViewWidth * mViewWidth + mViewHeight * mViewHeight).toDouble().pow(0.5).toInt()
            size = (0.0324 * mViewWidth).toFloat() * 0.8f
            speedY = mViewWidth / 200f
            init(true)
        }

        private fun init(firstTime: Boolean) {
            val r = Random()
            cx = r.nextInt(mCanvasSize).toFloat()
            cy = if (firstTime) {
                (r.nextInt((mCanvasSize - size).toInt()) - mCanvasSize).toFloat()
            } else {
                -size
            }
            rotation = 360 * r.nextFloat()
            speedRotation = 360f / 500f * r.nextFloat()
            speedX = 0.75f * (r.nextFloat() * speedY * if (r.nextBoolean()) 1 else -1)
            computeCenterPosition()
        }

        private fun computeCenterPosition() {
            centerX = (cx - (mCanvasSize - mViewWidth) * 0.5).toFloat()
            centerY = (cy - (mCanvasSize - mViewHeight) * 0.5).toFloat()
        }

        fun move(interval: Long, deltaRotation3D: Float) {
            cx += (speedX * interval * scale.toDouble().pow(1.5)).toFloat()
            cy +=
                (speedY * interval * (scale.toDouble().pow(1.5) - 5 * sin(deltaRotation3D * Math.PI / 180.0))).toFloat()
            rotation = (rotation + speedRotation * interval) % 360
            if (cy - size >= mCanvasSize) {
                init(false)
            } else {
                computeCenterPosition()
            }
            rectF.set(cx - size * scale, cy - size * scale, cx + size * scale, cy + size * scale)
        }
    }

    init {
        val colors: IntArray = if (daylight) {
            intArrayOf(
                Color.rgb(128, 197, 255),
                Color.rgb(185, 222, 255),
                Color.rgb(255, 255, 255)
            )
        } else {
            intArrayOf(
                Color.rgb(40, 102, 155),
                Color.rgb(99, 144, 182),
                Color.rgb(255, 255, 255)
            )
        }
        val scales = floatArrayOf(0.6f, 0.8f, 1f)
        mHails = Array(HAIL_COUNT) { i ->
            Hail(
                canvasSizes[0],
                canvasSizes[1],
                colors[i * 3 / HAIL_COUNT],
                scales[i * 3 / HAIL_COUNT]
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
        for (h in mHails) {
            h.move(interval, if (mLastRotation3D == INITIAL_ROTATION_3D) 0f else rotation3D - mLastRotation3D)
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
        if (scrollRate < 1) {
            canvas.rotate(
                rotation2D,
                canvasSizes[0] * 0.5f,
                canvasSizes[1] * 0.5f
            )
            for (h in mHails) {
                mPaint.color = h.color
                mPaint.alpha = ((1 - scrollRate) * 255).toInt()
                canvas.rotate(h.rotation, h.cx, h.cy)
                canvas.drawRect(h.rectF, mPaint)
                canvas.rotate(-h.rotation, h.cx, h.cy)
            }
        }
    }

    companion object {
        private const val INITIAL_ROTATION_3D = 1000f
        private const val HAIL_COUNT = 51

        @ColorInt
        fun getThemeColor(daylight: Boolean): Int {
            return if (daylight) -0x974501 else -0xe5a46e
        }
    }
}
