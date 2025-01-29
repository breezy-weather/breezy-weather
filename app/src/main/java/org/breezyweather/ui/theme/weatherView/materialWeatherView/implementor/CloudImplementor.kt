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

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import androidx.annotation.ColorInt
import androidx.annotation.IntDef
import androidx.annotation.Size
import androidx.core.content.ContextCompat
import org.breezyweather.R
import org.breezyweather.ui.theme.weatherView.materialWeatherView.MaterialWeatherView.WeatherAnimationImplementor
import java.util.Random
import kotlin.math.pow
import kotlin.math.sin
import kotlin.math.sqrt

@SuppressLint("SwitchIntDef")
class CloudImplementor(
    @Size(2) canvasSizes: IntArray,
    animate: Boolean,
    @TypeRule type: Int,
    daylight: Boolean,
) : WeatherAnimationImplementor() {
    private val mAnimate = animate
    private var mPaint = Paint().apply {
        style = Paint.Style.FILL
        isAntiAlias = true
    }
    private var mClouds: Array<Cloud> = emptyArray()
    private var mStars: Array<Star> = emptyArray()
    private var mThunder: Thunder? = null
    private val mRandom: Random

    @IntDef(TYPE_CLOUD, TYPE_CLOUDY, TYPE_THUNDER, TYPE_FOG, TYPE_HAZE)
    internal annotation class TypeRule
    private class Cloud(
        private val mInitCX: Float,
        private val mInitCY: Float,
        var radius: Float,
        val scaleRatio: Float,
        val moveFactor: Float,
        @ColorInt val color: Int,
        val alpha: Float,
        val duration: Long,
        initProgress: Long,
    ) {
        var centerX: Float = mInitCX
        var centerY: Float = mInitCY
        var initRadius: Float = radius
        var progress: Long = initProgress % duration

        init {
            computeRadius(duration, progress)
        }

        fun move(interval: Long, rotation2D: Float, rotation3D: Float) {
            centerX = (mInitCX + sin(rotation2D * Math.PI / 180.0) * 0.40 * radius * moveFactor).toFloat()
            centerY = (mInitCY - sin(rotation3D * Math.PI / 180.0) * 0.50 * radius * moveFactor).toFloat()
            progress = (progress + interval) % duration
            computeRadius(duration, progress)
        }

        private fun computeRadius(duration: Long, progress: Long) {
            radius = if (progress < 0.5 * duration) {
                (initRadius * (1 + (scaleRatio - 1) * progress / 0.5 / duration)).toFloat()
            } else {
                (initRadius * (scaleRatio - (scaleRatio - 1) * (progress - 0.5 * duration) / 0.5 / duration)).toFloat()
            }
        }
    }

    private class Star(
        val centerX: Float,
        val centerY: Float,
        radius: Float,
        @field:ColorInt @param:ColorInt val color: Int,
        val duration: Long,
        val animate: Boolean,
    ) {
        var radius: Float
        var alpha = 0f
        var progress: Long = 0

        init {
            this.radius = (radius * (0.7 + 0.3 * Random().nextFloat())).toFloat()
            if (!animate) {
                alpha = Random().nextFloat()
            } else {
                computeAlpha(duration, progress)
            }
        }

        fun shine(interval: Long) {
            if (!animate) return

            progress = (progress + interval) % duration
            computeAlpha(duration, progress)
        }

        private fun computeAlpha(duration: Long, progress: Long) {
            alpha = if (progress < 0.5 * duration) {
                (progress / 0.5 / duration).toFloat()
            } else {
                (1 - (progress - 0.5 * duration) / 0.5 / duration).toFloat()
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
            delay = (Random().nextInt(5000) + 2000).toLong()
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
        val viewWidth = canvasSizes[0]
        val viewHeight = canvasSizes[1]
        mRandom = Random()
        if (type == TYPE_FOG || type == TYPE_HAZE) {
            val cloudColors: IntArray
            val cloudAlphas: FloatArray
            if (type == TYPE_FOG) {
                cloudColors = if (daylight) {
                    intArrayOf(-0x8e8260, -0x8e8260, -0x8e8260)
                } else {
                    intArrayOf(
                        Color.rgb(85, 99, 110),
                        Color.rgb(91, 104, 114),
                        Color.rgb(99, 113, 123)
                    )
                }
                cloudAlphas = if (daylight) floatArrayOf(0.1f, 0.1f, 0.1f) else floatArrayOf(0.4f, 0.6f, 0.4f)
            } else {
                cloudColors = if (daylight) {
                    intArrayOf(-0x53627e, -0x53627e, -0x53627e)
                } else {
                    intArrayOf(
                        Color.rgb(179, 158, 132),
                        Color.rgb(179, 158, 132),
                        Color.rgb(179, 158, 132)
                    )
                }
                cloudAlphas = floatArrayOf(0.3f, 0.3f, 0.3f)
            }
            val clouds = arrayOf(
                Cloud(
                    viewWidth * 1.0699f,
                    viewWidth * (1.1900f * 0.2286f + 0.11f),
                    viewWidth * (0.4694f * 0.9f),
                    1.1f,
                    getRandomFactor(1.3f, 1.8f),
                    cloudColors[0],
                    cloudAlphas[0],
                    9000,
                    0
                ),
                Cloud(
                    viewWidth * 0.4866f,
                    viewWidth * (0.4866f * 0.6064f + 0.085f),
                    viewWidth * (0.3946f * 0.9f),
                    1.1f,
                    getRandomFactor(1.3f, 1.8f),
                    cloudColors[0],
                    cloudAlphas[0],
                    10500,
                    0
                ),
                Cloud(
                    viewWidth * 0.0351f,
                    viewWidth * (0.1701f * 1.4327f + 0.11f),
                    viewWidth * (0.4627f * 0.9f),
                    1.1f,
                    getRandomFactor(1.3f, 1.8f),
                    cloudColors[0],
                    cloudAlphas[0],
                    9000,
                    0
                ),
                Cloud(
                    viewWidth * 0.8831f,
                    viewWidth * (1.0270f * 0.1671f + 0.07f),
                    viewWidth * (0.3238f * 0.9f),
                    1.15f,
                    getRandomFactor(1.6f, 2f),
                    cloudColors[1],
                    cloudAlphas[1],
                    7000,
                    0
                ),
                Cloud(
                    viewWidth * 0.4663f,
                    viewWidth * (0.4663f * 0.3520f + 0.050f),
                    viewWidth * (0.2906f * 0.9f),
                    1.15f,
                    getRandomFactor(1.6f, 2f),
                    cloudColors[1],
                    cloudAlphas[1],
                    8500,
                    0
                ),
                Cloud(
                    viewWidth * 0.1229f,
                    viewWidth * (0.0234f * 5.7648f + 0.07f),
                    viewWidth * (0.2972f * 0.9f),
                    1.15f,
                    getRandomFactor(1.6f, 2f),
                    cloudColors[1],
                    cloudAlphas[1],
                    7000,
                    0
                ),
                Cloud(
                    viewWidth * 0.9250f,
                    viewWidth * (0.9250f * 0.0249f + 0.1500f),
                    viewWidth * 0.3166f,
                    1.15f,
                    getRandomFactor(1.8f, 2.2f),
                    cloudColors[2],
                    cloudAlphas[2],
                    7000,
                    0
                ),
                Cloud(
                    viewWidth * 0.4694f,
                    viewWidth * (0.4694f * 0.0489f + 0.1500f),
                    viewWidth * 0.3166f,
                    1.15f,
                    getRandomFactor(1.8f, 2.2f),
                    cloudColors[2],
                    cloudAlphas[2],
                    8200,
                    0
                ),
                Cloud(
                    viewWidth * 0.0250f,
                    viewWidth * (0.0250f * 0.6820f + 0.1500f),
                    viewWidth * 0.3166f,
                    1.15f,
                    getRandomFactor(1.8f, 2.2f),
                    cloudColors[2],
                    cloudAlphas[2],
                    7700,
                    0
                )
            )
            initialize(clouds)
        } else if (type == TYPE_CLOUDY || type == TYPE_THUNDER) {
            var cloudColors = IntArray(2)
            var cloudAlphas = FloatArray(2)
            when (type) {
                TYPE_CLOUDY -> {
                    cloudColors = if (daylight) {
                        intArrayOf(
                            Color.rgb(160, 179, 191),
                            Color.rgb(160, 179, 191)
                        )
                    } else {
                        intArrayOf(
                            Color.rgb(95, 104, 108),
                            Color.rgb(95, 104, 108)
                        )
                    }
                    cloudAlphas = floatArrayOf(0.3f, 0.3f)
                }
                TYPE_THUNDER -> {
                    cloudColors = if (daylight) {
                        intArrayOf(-0x43523f, -0x43523f)
                    } else {
                        intArrayOf(
                            Color.rgb(43, 30, 66),
                            Color.rgb(43, 30, 66)
                        )
                    }
                    cloudAlphas = if (daylight) floatArrayOf(0.2f, 0.3f) else floatArrayOf(0.8f, 0.8f)
                }
            }
            val clouds = arrayOf(
                Cloud(
                    viewWidth * 1.0699f,
                    viewWidth * (1.1900f * 0.2286f + 0.11f),
                    viewWidth * (0.4694f * 0.9f),
                    1.1f,
                    getRandomFactor(1.3f, 1.8f),
                    cloudColors[0],
                    cloudAlphas[0],
                    9000,
                    0
                ),
                Cloud(
                    viewWidth * 0.4866f,
                    viewWidth * (0.4866f * 0.6064f + 0.085f),
                    viewWidth * (0.3946f * 0.9f),
                    1.1f,
                    getRandomFactor(1.3f, 1.8f),
                    cloudColors[0],
                    cloudAlphas[0],
                    10500,
                    0
                ),
                Cloud(
                    viewWidth * 0.0351f,
                    viewWidth * (0.1701f * 1.4327f + 0.11f),
                    viewWidth * (0.4627f * 0.9f),
                    1.1f,
                    getRandomFactor(1.3f, 1.8f),
                    cloudColors[0],
                    cloudAlphas[0],
                    9000,
                    0
                ),
                Cloud(
                    viewWidth * 0.8831f,
                    viewWidth * (1.0270f * 0.1671f + 0.07f),
                    viewWidth * (0.3238f * 0.9f),
                    1.15f,
                    getRandomFactor(1.6f, 2f),
                    cloudColors[1],
                    cloudAlphas[1],
                    7000,
                    0
                ),
                Cloud(
                    viewWidth * 0.4663f,
                    viewWidth * (0.4663f * 0.3520f + 0.050f),
                    viewWidth * (0.2906f * 0.9f),
                    1.15f,
                    getRandomFactor(1.6f, 2f),
                    cloudColors[1],
                    cloudAlphas[1],
                    8500,
                    0
                ),
                Cloud(
                    viewWidth * 0.1229f,
                    viewWidth * (0.0234f * 5.7648f + 0.07f),
                    viewWidth * (0.2972f * 0.9f),
                    1.15f,
                    getRandomFactor(1.6f, 2f),
                    cloudColors[1],
                    cloudAlphas[1],
                    7000,
                    0
                )
            )
            initialize(clouds)
        } else {
            val cloudColor = if (daylight) {
                Color.rgb(203, 245, 255)
            } else {
                Color.rgb(151, 168, 202)
            }
            val cloudAlphas: FloatArray = floatArrayOf(0.40f, 0.10f)
            val clouds = arrayOf(
                Cloud(
                    (viewWidth * 0.1529).toFloat(),
                    (viewWidth * 0.1529 * 0.5568 + viewWidth * 0.050).toFloat(),
                    (viewWidth * 0.2649).toFloat(),
                    1.20f,
                    getRandomFactor(1.5f, 1.8f),
                    cloudColor,
                    cloudAlphas[0],
                    7000,
                    0
                ),
                Cloud(
                    (viewWidth * 0.4793).toFloat(),
                    (viewWidth * 0.4793 * 0.2185 + viewWidth * 0.050).toFloat(),
                    (viewWidth * 0.2426).toFloat(),
                    1.20f,
                    getRandomFactor(1.5f, 1.8f),
                    cloudColor,
                    cloudAlphas[0],
                    8500,
                    0
                ),
                Cloud(
                    (viewWidth * 0.8531).toFloat(),
                    (viewWidth * 0.8531 * 0.1286 + viewWidth * 0.050).toFloat(),
                    (viewWidth * 0.2970).toFloat(),
                    1.20f,
                    getRandomFactor(1.5f, 1.8f),
                    cloudColor,
                    cloudAlphas[0],
                    7050,
                    0
                ),
                Cloud(
                    (viewWidth * 0.0551).toFloat(),
                    (viewWidth * 0.0551 * 2.8600 + viewWidth * 0.050).toFloat(),
                    (viewWidth * 0.4125).toFloat(),
                    1.15f,
                    getRandomFactor(1.3f, 1.5f),
                    cloudColor,
                    cloudAlphas[1],
                    9500,
                    0
                ),
                Cloud(
                    (viewWidth * 0.4928).toFloat(),
                    (viewWidth * 0.4928 * 0.3897 + viewWidth * 0.050).toFloat(),
                    (viewWidth * 0.3521).toFloat(),
                    1.15f,
                    getRandomFactor(1.3f, 1.5f),
                    cloudColor,
                    cloudAlphas[1],
                    10500,
                    0
                ),
                Cloud(
                    (viewWidth * 1.0499).toFloat(),
                    (viewWidth * 1.0499 * 0.1875 + viewWidth * 0.050).toFloat(),
                    (viewWidth * 0.4186).toFloat(),
                    1.15f,
                    getRandomFactor(1.3f, 1.5f),
                    cloudColor,
                    cloudAlphas[1],
                    9000,
                    0
                )
            )
            if (daylight) {
                initialize(clouds)
            } else {
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
                val r = Random()
                val canvasSize = sqrt(viewWidth.toDouble().pow(2.0) + viewHeight.toDouble().pow(2.0)).toInt()
                val width = (1.0 * canvasSize).toInt()
                val height = ((canvasSize - viewHeight) * 0.5 + viewWidth * 1.1111).toInt()
                val radius = (0.00125 * canvasSize * (0.5 + r.nextFloat())).toFloat()
                val stars = Array(50) { i ->
                    val x = (r.nextInt(width) - 0.5 * (canvasSize - viewWidth)).toInt()
                    val y = (r.nextInt(height) - 0.5 * (canvasSize - viewHeight)).toInt()
                    val duration = (2500 + r.nextFloat() * 2500).toLong()
                    Star(
                        x.toFloat(),
                        y.toFloat(),
                        radius,
                        colors[i % colors.size],
                        duration,
                        mAnimate
                    )
                }
                initialize(clouds, stars)
            }
        }
        mThunder = if (type == TYPE_THUNDER) Thunder() else null
    }

    private fun initialize(clouds: Array<Cloud>, stars: Array<Star> = emptyArray()) {
        mPaint = Paint().apply {
            style = Paint.Style.FILL
            isAntiAlias = true
        }
        mClouds = clouds
        mStars = stars
    }

    private fun getRandomFactor(from: Float, to: Float): Float {
        return from + mRandom.nextFloat() % (to - from)
    }

    override fun updateData(
        @Size(2) canvasSizes: IntArray,
        interval: Long,
        rotation2D: Float,
        rotation3D: Float,
    ) {
        for (c in mClouds) {
            c.move(interval, rotation2D, rotation3D)
        }
        for (s in mStars) {
            s.shine(interval)
        }
        mThunder?.shine(interval)
    }

    override fun draw(
        @Size(2) canvasSizes: IntArray,
        canvas: Canvas,
        scrollRate: Float,
        rotation2D: Float,
        rotation3D: Float,
    ) {
        if (scrollRate < 1) {
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
            for (s in mStars) {
                mPaint.color = s.color
                mPaint.alpha = ((1 - scrollRate) * s.alpha * 255).toInt()
                canvas.drawCircle(s.centerX, s.centerY, s.radius, mPaint)
            }
            for (c in mClouds) {
                mPaint.color = c.color
                mPaint.alpha = ((1 - scrollRate) * c.alpha * 255).toInt()
                canvas.drawCircle(c.centerX, c.centerY, c.radius, mPaint)
            }
        }
    }

    companion object {
        const val TYPE_CLOUD = 1
        const val TYPE_CLOUDY = 3
        const val TYPE_THUNDER = 5
        const val TYPE_FOG = 6
        const val TYPE_HAZE = 7

        @ColorInt
        fun getThemeColor(context: Context, @TypeRule type: Int, daylight: Boolean): Int {
            when (type) {
                TYPE_CLOUDY -> return if (daylight) -0x62503f else -0xd9cdc8
                TYPE_CLOUD -> return if (daylight) -0xff5a27 else -0xddd2bd
                TYPE_THUNDER -> return if (daylight) -0x4d6943 else -0xdce8c7
                TYPE_FOG -> return if (daylight) -0x5c513e else -0xb0a298
                TYPE_HAZE -> return if (daylight) -0x1e3767 else -0x93a3b7
            }
            return ContextCompat.getColor(context, R.color.md_theme_primary)
        }
    }
}
