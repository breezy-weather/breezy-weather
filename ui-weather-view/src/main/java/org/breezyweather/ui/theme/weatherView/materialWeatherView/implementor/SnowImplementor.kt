/*
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
import java.util.Random
import kotlin.math.pow
import kotlin.math.sin

/**
 * Snow implementor.
 */
class SnowImplementor(
    @Size(2) canvasSizes: IntArray,
    animate: Boolean,
    daylight: Boolean,
) : WeatherAnimationImplementor() {
    private val mAnimate = animate
    private val mPaint = Paint().apply {
        style = Paint.Style.FILL
        isAntiAlias = true
    }
    private val mSnows: Array<Snow>
    private var mLastRotation3D: Float

    private class Snow(
        private val mViewWidth: Int,
        private val mViewHeight: Int,
        @field:ColorInt @param:ColorInt val color: Int,
        val scale: Float,
    ) {
        private var mCX = 0f
        private var mCY = 0f
        var centerX = 0f
        var centerY = 0f
        var radius: Float
        var speedX = 0f
        var speedY: Float
        private val mCanvasSize: Int

        init {
            mCanvasSize = (mViewWidth * mViewWidth + mViewHeight * mViewHeight).toDouble().pow(0.5).toInt()
            radius = (mCanvasSize * (0.005 + Random().nextDouble() * 0.007) * scale).toFloat()
            speedY = (mCanvasSize / (1000.0 * (2.5 + Random().nextDouble())) * SNOW_SPEED).toFloat()
            init(true)
        }

        private fun init(firstTime: Boolean) {
            val r = Random()
            mCX = r.nextInt(mCanvasSize).toFloat()
            mCY = if (firstTime) {
                (r.nextInt((mCanvasSize - radius).toInt()) - mCanvasSize).toFloat()
            } else {
                -radius
            }

            val bound = (2 * speedY).toInt()
            // Fix bound to at least two to prevent crashes on small screens (where bound would be 0) and provide
            // some variation on medium screens (where bound would always be 1 and the RNG would only give 0)
            speedX = r.nextInt(if (bound >= 2) bound else 2) - speedY
            computeCenterPosition()
        }

        private fun computeCenterPosition() {
            centerX = (mCX - (mCanvasSize - mViewWidth) * 0.5).toInt().toFloat()
            centerY = (mCY - (mCanvasSize - mViewHeight) * 0.5).toInt().toFloat()
        }

        fun move(interval: Long, deltaRotation3D: Float) {
            mCX += (speedX * interval * scale.toDouble().pow(1.5)).toFloat()
            mCY +=
                (speedY * interval * (scale.toDouble().pow(1.5) - 5 * sin(deltaRotation3D * Math.PI / 180.0))).toFloat()
            if (centerY >= mCanvasSize) {
                init(false)
            } else {
                computeCenterPosition()
            }
        }
    }

    init {
        val colors = if (daylight) {
            intArrayOf(
                Color.rgb(190, 225, 255),
                Color.rgb(211, 233, 255),
                Color.rgb(255, 255, 255)
            )
        } else {
            intArrayOf(
                Color.rgb(111, 133, 155),
                Color.rgb(140, 161, 182),
                Color.rgb(255, 255, 255)
            )
        }
        val scales = floatArrayOf(0.6f, 0.8f, 1f)
        mSnows = Array(SNOW_COUNT) { i ->
            Snow(
                canvasSizes[0],
                canvasSizes[1],
                colors[i * 3 / SNOW_COUNT],
                scales[i * 3 / SNOW_COUNT]
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
        for (s in mSnows) {
            s.move(interval, if (mLastRotation3D == INITIAL_ROTATION_3D) 0f else rotation3D - mLastRotation3D)
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
            for (s in mSnows) {
                mPaint.color = s.color
                mPaint.alpha = ((1 - scrollRate) * 255).toInt()
                canvas.drawCircle(s.centerX, s.centerY, s.radius, mPaint)
            }
        }
    }

    companion object {
        private const val INITIAL_ROTATION_3D = 1000f
        private const val SNOW_COUNT = 50
        private const val SNOW_SPEED = 1.5

        @ColorInt
        fun getThemeColor(daylight: Boolean): Int {
            return if (daylight) -0x974501 else -0xe5a46e
        }
    }
}
