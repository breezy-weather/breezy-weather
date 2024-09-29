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

package org.breezyweather.theme.weatherView.materialWeatherView.implementor

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import androidx.annotation.ColorInt
import androidx.annotation.Size
import org.breezyweather.theme.weatherView.materialWeatherView.MaterialWeatherView.WeatherAnimationImplementor
import java.util.Random
import kotlin.math.cos
import kotlin.math.pow
import kotlin.math.sin
import kotlin.time.Duration.Companion.seconds

/**
 * Meteor shower implementor.
 */
class MeteorShowerImplementor(
    @Size(2) canvasSizes: IntArray,
    animate: Boolean
) : WeatherAnimationImplementor() {
    private val mAnimate = animate
    private val mPaint = Paint().apply {
        style = Paint.Style.STROKE
        strokeCap = Paint.Cap.ROUND
        isAntiAlias = true
    }
    private val mMeteors: Array<Meteor>
    private val mStars: Array<Star>
    private var mLastRotation3D: Float

    private class Meteor(
        private val mViewWidth: Int,
        private val mViewHeight: Int,
        @ColorInt val color: Int,
        val scale: Float
    ) {
        var x = 0f
        var y = 0f
        var width: Float
        var height = 0f
        var rectF: RectF = RectF()
        var speed: Float
        private var progress: Long = 0
        private var delay: Long = 0

        private val random: Random = Random()

        private val mCanvasSize: Int
        private val MAX_HEIGHT: Float
        private val MIN_HEIGHT: Float

        init { // 1, 0.7, 0.4
            mCanvasSize =
                (mViewWidth * mViewWidth + mViewHeight * mViewHeight).toDouble().pow(0.5).toInt()
            width = (mViewWidth * 0.005 * scale).toFloat()
            speed = mViewWidth / 200f
            MAX_HEIGHT = (mViewWidth / cos(60.0 * Math.PI / 180.0)).toFloat()
            MIN_HEIGHT = (MAX_HEIGHT * 0.7).toFloat()

            init(true)
        }

        private fun init(firstTime: Boolean) {
            progress = 0
            delay = (random.nextInt(
                METEOR_REVIVE_SECONDS_MAX - METEOR_REVIVE_SECONDS_MIN
            ) + METEOR_REVIVE_SECONDS_MIN).seconds.inWholeMilliseconds

            x = random.nextInt(mCanvasSize).toFloat()
            y = if (!firstTime) {
                random.nextInt(mCanvasSize) - MAX_HEIGHT - mCanvasSize
            } else mCanvasSize.toFloat() * 2 // prevents spawning all at once

            height = MIN_HEIGHT + random.nextFloat() * (MAX_HEIGHT - MIN_HEIGHT)
            buildRectF()
        }

        private fun buildRectF() {
            val x = (x - (mCanvasSize - mViewWidth) * 0.5).toFloat()
            val y = (y - (mCanvasSize - mViewHeight) * 0.5).toFloat()
            rectF.set(x, y, x + width, y + height)
        }

        fun update(interval: Long, deltaRotation3D: Float) {
            if (y > mCanvasSize) {
                progress += interval
                if (progress > delay) init(false)
                return
            }
            move(interval, deltaRotation3D)
            buildRectF()
        }

        private fun move(interval: Long, deltaRotation3D: Float) {
            x -= (speed * interval * 5
                * sin(deltaRotation3D * Math.PI / 180.0) * cos(60 * Math.PI / 180.0)).toFloat()
            y += (speed * interval
                * (scale.toDouble().pow(0.5)
                - 5 * sin(deltaRotation3D * Math.PI / 180.0) * sin(60 * Math.PI / 180.0))).toFloat()
        }
    }

    private class Star(
        var centerX: Float, var centerY: Float, radius: Float,
        @field:ColorInt @param:ColorInt var color: Int,
        var duration: Long
    ) {
        var radius: Float
        var alpha = 0f
        var progress: Long = 0

        init {
            this.radius = (radius * (0.6 + 0.3 * Random().nextFloat())).toFloat()
            computeAlpha(duration, progress)
        }

        fun shine(interval: Long) {
            progress = (progress + interval) % duration
            computeAlpha(duration, progress)
        }

        private fun computeAlpha(duration: Long, progress: Long) {
            alpha = if (progress < 0.5 * duration) {
                (progress / 0.5 / duration).toFloat()
            } else {
                (1 - (progress - 0.5 * duration) / 0.5 / duration).toFloat()
            }
            alpha = alpha * 0.66f + 0.33f
        }
    }

    init {
        val random = Random()
        val viewWidth = canvasSizes[0]
        val viewHeight = canvasSizes[1]
        val colors = intArrayOf(
            Color.rgb(210, 247, 255),
            Color.rgb(208, 233, 255),
            Color.rgb(175, 201, 228),
            Color.rgb(164, 194, 220),
            Color.rgb(97, 171, 220),
            Color.rgb(74, 141, 193),
            Color.rgb(54, 66, 119),
            Color.rgb(34, 48, 74),
            Color.rgb(236, 234, 213),
            Color.rgb(240, 220, 151)
        )

        mMeteors = Array(if(mAnimate) 10 else 0) {
            Meteor(
                viewWidth, viewHeight,
                colors[random.nextInt(colors.size)], random.nextFloat()
            )
        }
        val canvasSize =
            (viewWidth.toDouble().pow(2.0) + viewHeight.toDouble().pow(2.0)).pow(0.5).toInt()
        val width = (1.0 * canvasSize).toInt()
        val height = ((canvasSize - viewHeight) * 0.5 + viewWidth * 1.1111).toInt()
        val radius = (0.00125 * canvasSize * (0.5 + random.nextFloat())).toFloat()
        mStars = Array(80) { i ->
            val x = (random.nextInt(width) - 0.5 * (canvasSize - viewWidth)).toInt()
            val y = (random.nextInt(height) - 0.5 * (canvasSize - viewHeight)).toInt()
            val duration = (2500 + random.nextFloat() * 2500).toLong()
            Star(
                x.toFloat(),
                y.toFloat(),
                radius,
                colors[i % colors.size],
                duration
            )
        }
        mLastRotation3D = INITIAL_ROTATION_3D
    }

    override fun updateData(
        @Size(2) canvasSizes: IntArray, interval: Long,
        rotation2D: Float, rotation3D: Float
    ) {
        for (m in mMeteors) {
            m.update(
                interval,
                if (mLastRotation3D == INITIAL_ROTATION_3D) 0f else rotation3D - mLastRotation3D
            )
        }
        for (s in mStars) {
            s.shine(interval)
        }
        mLastRotation3D = rotation3D
    }

    override fun draw(
        @Size(2) canvasSizes: IntArray, canvas: Canvas,
        scrollRate: Float, rotation2D: Float, rotation3D: Float
    ) {
        if (scrollRate < 1) {
            canvas.rotate(
                rotation2D,
                canvasSizes[0] * 0.5f,
                canvasSizes[1] * 0.5f
            )
            for (s in mStars) {
                mPaint.apply {
                    color = s.color
                    alpha = ((1 - scrollRate) * s.alpha * 255).toInt()
                    strokeWidth = s.radius * 2
                }
                canvas.drawPoint(s.centerX, s.centerY, mPaint)
            }
            canvas.rotate(
                60f,
                canvasSizes[0] * 0.5f,
                canvasSizes[1] * 0.5f
            )
            for (m in mMeteors) {
                mPaint.apply {
                    color = m.color
                    strokeWidth = m.rectF.width()
                    alpha = ((1 - scrollRate) * 255).toInt()
                }
                canvas.drawLine(
                    m.rectF.centerX(), m.rectF.top,
                    m.rectF.centerX(), m.rectF.bottom,
                    mPaint
                )
            }
        }
    }

    companion object {
        private const val INITIAL_ROTATION_3D = 1000f
        private const val METEOR_REVIVE_SECONDS_MIN = 5
        private const val METEOR_REVIVE_SECONDS_MAX = 25

        @get:ColorInt
        val themeColor: Int
            get() = Color.rgb(20, 28, 44)
    }
}
