package org.breezyweather.theme.weatherView.materialWeatherView.implementor

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import androidx.annotation.ColorInt
import androidx.annotation.IntDef
import androidx.annotation.Size
import androidx.core.content.ContextCompat
import org.breezyweather.R
import org.breezyweather.theme.weatherView.materialWeatherView.MaterialWeatherView.WeatherAnimationImplementor
import java.util.*
import kotlin.math.cos
import kotlin.math.pow
import kotlin.math.sin

/**
 * Rain implementor.
 */
class RainImplementor(
    @Size(2) canvasSizes: IntArray,
    animate: Boolean,
    @TypeRule type: Int,
    daylight: Boolean
) : WeatherAnimationImplementor() {
    private val mAnimate = animate
    private val mPaint = Paint().apply {
        style = Paint.Style.FILL
        isAntiAlias = true
    }
    private val mRains: Array<Rain>
    private var mThunder: Thunder? = null
    private var mLastRotation3D: Float

    @IntDef(TYPE_RAIN, TYPE_THUNDERSTORM, TYPE_SLEET)
    internal annotation class TypeRule
    private class Rain(
        private val mViewWidth: Int,
        private val mViewHeight: Int,
        @ColorInt val color: Int,
        val scale: Float
    ) {
        var x = 0f
        var y = 0f
        var width = 0f
        var height = 0f
        var rectF: RectF = RectF()
        var speed: Float

        private val mCanvasSize: Int
        private val MAX_WIDTH: Float
        private val MIN_WIDTH: Float
        private val MAX_HEIGHT: Float
        private val MIN_HEIGHT: Float

        init {
            mCanvasSize = (mViewWidth * mViewWidth + mViewHeight * mViewHeight).toDouble().pow(0.5).toInt()
            speed = (mCanvasSize / (1000.0 * (1.75 + Random().nextDouble())) * 5.0).toFloat()
            MAX_WIDTH = (0.006 * mCanvasSize).toFloat()
            MIN_WIDTH = (0.003 * mCanvasSize).toFloat()
            MAX_HEIGHT = MAX_WIDTH * 10
            MIN_HEIGHT = MIN_WIDTH * 6
            init(true)
        }

        private fun init(firstTime: Boolean) {
            val r = Random()
            x = r.nextInt(mCanvasSize).toFloat()
            y = if (firstTime) {
                (r.nextInt((mCanvasSize - MAX_HEIGHT).toInt()) - mCanvasSize).toFloat()
            } else {
                -MAX_HEIGHT * (1 + 2 * r.nextFloat())
            }
            width = MIN_WIDTH + r.nextFloat() * (MAX_WIDTH - MIN_WIDTH)
            height = MIN_HEIGHT + r.nextFloat() * (MAX_HEIGHT - MIN_HEIGHT)
            buildRectF()
        }

        private fun buildRectF() {
            val x = (x - (mCanvasSize - mViewWidth) * 0.5).toFloat()
            val y = (y - (mCanvasSize - mViewHeight) * 0.5).toFloat()
            rectF.set(x, y, x + width * scale, y + height * scale)
        }

        fun move(interval: Long, deltaRotation3D: Float) {
            y += (speed * interval
                    * (scale.toDouble().pow(1.5)
                    - 5 * sin(deltaRotation3D * Math.PI / 180.0) * cos(8 * Math.PI / 180.0))).toFloat()
            x -= (speed * interval
                    * 5 * sin(deltaRotation3D * Math.PI / 180.0) * sin(8 * Math.PI / 180.0)).toFloat()
            if (y >= mCanvasSize) {
                init(false)
            } else {
                buildRectF()
            }
        }
    }

    private class Thunder {
        var r = 81
        var g = 67
        var b = 168
        var alpha = 0f
        private var progress: Long = 0
        private var duration: Long = 0
        private var delay: Long = 0

        init {
            init()
            computeFrame()
        }

        private fun init() {
            progress = 0
            duration = 300
            delay = (Random().nextInt(5000) + 3000).toLong()
        }

        private fun computeFrame() {
            alpha = if (progress < duration) {
                if (progress < 0.25 * duration) {
                    (progress / 0.25 / duration).toFloat()
                } else if (progress < 0.5 * duration) {
                    (1 - (progress - 0.25 * duration) / 0.25 / duration).toFloat()
                } else if (progress < 0.75 * duration) {
                    ((progress - 0.5 * duration) / 0.25 / duration).toFloat()
                } else {
                    (1 - (progress - 0.75 * duration) / 0.25 / duration).toFloat()
                }
            } else {
                0f
            }
        }

        fun shine(interval: Long) {
            progress += interval
            if (progress > duration + delay) {
                init()
            }
            computeFrame()
        }
    }

    init {
        var colors = IntArray(3)
        var rainCount = RAIN_COUNT
        when (type) {
            TYPE_RAIN -> if (daylight) {
                rainCount = RAIN_COUNT
                mThunder = null
                colors = intArrayOf(
                    Color.rgb(223, 179, 114),
                    Color.rgb(152, 175, 222),
                    Color.rgb(255, 255, 255)
                )
            } else {
                rainCount = RAIN_COUNT
                mThunder = null
                colors = intArrayOf(
                    Color.rgb(182, 142, 82),
                    Color.rgb(88, 92, 113),
                    Color.rgb(255, 255, 255)
                )
            }

            TYPE_THUNDERSTORM -> if (daylight) {
                rainCount = RAIN_COUNT
                mThunder = Thunder()
                colors = intArrayOf(
                    Color.rgb(182, 142, 82),
                    -0x93aa6e,
                    Color.rgb(255, 255, 255)
                )
            } else {
                rainCount = RAIN_COUNT
                mThunder = Thunder()
                colors = intArrayOf(
                    Color.rgb(182, 142, 82),
                    Color.rgb(88, 92, 113),
                    Color.rgb(255, 255, 255)
                )
            }

            TYPE_SLEET -> if (daylight) {
                rainCount = SLEET_COUNT
                mThunder = null
                colors = intArrayOf(
                    Color.rgb(128, 197, 255),
                    Color.rgb(185, 222, 255),
                    Color.rgb(255, 255, 255)
                )
            } else {
                rainCount = SLEET_COUNT
                mThunder = null
                colors = intArrayOf(
                    Color.rgb(40, 102, 155),
                    Color.rgb(99, 144, 182),
                    Color.rgb(255, 255, 255)
                )
            }
        }
        val scales = floatArrayOf(0.6f, 0.8f, 1f)
        mRains = Array(rainCount) { i ->
            Rain(
                canvasSizes[0],
                canvasSizes[1],
                colors[i * 3 / rainCount],
                scales[i * 3 / rainCount]
            )
        }
        mLastRotation3D = INITIAL_ROTATION_3D
    }

    override fun updateData(
        @Size(2) canvasSizes: IntArray, interval: Long,
        rotation2D: Float, rotation3D: Float
    ) {
        // do not display any rain effects if animations are turned off
        if (!mAnimate) return

        for (r in mRains) {
            r.move(
                interval,
                if (mLastRotation3D == INITIAL_ROTATION_3D) 0f else rotation3D - mLastRotation3D
            )
        }
        mThunder?.shine(interval)
        mLastRotation3D = rotation3D
    }

    override fun draw(
        @Size(2) canvasSizes: IntArray, canvas: Canvas,
        scrollRate: Float, rotation2D: Float, rotation3D: Float
    ) {
        var rotation2Dc = rotation2D
        if (scrollRate < 1) {
            rotation2Dc += 8f
            canvas.rotate(
                rotation2Dc,
                canvasSizes[0] * 0.5f,
                canvasSizes[1] * 0.5f
            )
            for (r in mRains) {
                mPaint.color = r.color
                mPaint.alpha = ((1 - scrollRate) * 255).toInt()
                canvas.drawRoundRect(r.rectF, r.width / 2f, r.width / 2f, mPaint)
            }
            mThunder?.let {
                canvas.drawColor(
                    Color.argb(
                        ((1 - scrollRate) * it.alpha * 255 * 0.66).toInt(),
                        it.r,
                        it.g,
                        it.b
                    )
                )
            }
        }
    }

    companion object {
        private const val INITIAL_ROTATION_3D = 1000f
        const val TYPE_RAIN = 1
        const val TYPE_THUNDERSTORM = 3
        const val TYPE_SLEET = 4
        private const val RAIN_COUNT = 75
        private const val SLEET_COUNT = 45
        @ColorInt
        fun getThemeColor(context: Context, @TypeRule type: Int, daylight: Boolean): Int {
            when (type) {
                TYPE_RAIN -> return if (daylight) -0xbd6819 else -0xd9b171
                TYPE_SLEET -> return if (daylight) -0x974501 else -0xe5a46e
                TYPE_THUNDERSTORM -> return if (daylight) -0x4d6943 else -0xdce8c7
            }
            return ContextCompat.getColor(context, R.color.md_theme_primary)
        }
    }
}
