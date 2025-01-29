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

package org.breezyweather.ui.common.widgets.astro

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.DashPathEffect
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.PathEffect
import android.graphics.PorterDuff
import android.graphics.PorterDuffXfermode
import android.graphics.RectF
import android.graphics.Shader
import android.graphics.Xfermode
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.view.View
import androidx.annotation.ColorInt
import androidx.annotation.Size
import androidx.core.graphics.ColorUtils
import org.breezyweather.common.extensions.dpToPx
import org.breezyweather.ui.common.widgets.DayNightShaderWrapper
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.max
import kotlin.math.min
import kotlin.math.sin
import kotlin.math.tan

class SunMoonView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
) : View(context, attrs, defStyleAttr) {
    // 0 - day / 1 - night.
    @Size(2)
    private val mIconDrawables = arrayOfNulls<Drawable>(2)
    private val mPaint = Paint().apply {
        isAntiAlias = true
        strokeCap = Paint.Cap.ROUND
    }
    private var mClearXfermode: Xfermode = PorterDuffXfermode(PorterDuff.Mode.CLEAR)
    private var mX1ShaderWrapper: DayNightShaderWrapper
    private var mX2ShaderWrapper: DayNightShaderWrapper
    private var mEffect: PathEffect? = null
    private val mRectF = RectF()

    @Size(2)
    private val mIconRotations = floatArrayOf(0f, 0f)

    @Size(2)
    private val mIconAlphas = floatArrayOf(0f, 0f)

    @Size(2)
    private val mIconPositions = arrayOf(floatArrayOf(0f, 0f), floatArrayOf(0f, 0f))

    @Size(2)
    private var mStartTimes = longArrayOf(1, 1)

    @Size(2)
    private var mCurrentTimes = longArrayOf(1, 1)

    @Size(2)
    private var mEndTimes = longArrayOf(0, 0)

    @Size(2)
    private val mProgresses = longArrayOf(-1, -1)

    @Size(2)
    private val mMaxes = longArrayOf(100, 100)

    @Size(3)
    private var mLineColors = intArrayOf(Color.BLACK, Color.GRAY, Color.LTGRAY)

    @Size(2)
    private val mX1ShaderColors = intArrayOf(Color.GRAY, Color.WHITE)

    @Size(2)
    private val mX2ShaderColors = intArrayOf(Color.BLACK, Color.WHITE)

    @ColorInt
    private var mRootColor = Color.WHITE
    private var mLineSize = 0f
    private var mDottedLineSize = 0f
    private var mMargin = 0f
    private var iconSize = 0

    init {
        mLineSize = context.dpToPx(LINE_SIZE_DIP)
        mDottedLineSize = context.dpToPx(DOTTED_LINE_SIZE_DIP)
        mMargin = context.dpToPx(MARGIN_DIP)
        iconSize = context.dpToPx(ICON_SIZE_DIP).toInt()
        mX1ShaderWrapper = DayNightShaderWrapper(measuredWidth, measuredHeight)
        mX2ShaderWrapper = DayNightShaderWrapper(measuredWidth, measuredHeight)
        mEffect = DashPathEffect(
            floatArrayOf(
                context.dpToPx(3f),
                2 * context.dpToPx(3f)
            ),
            0f
        )
    }

    fun setTime(
        @Size(2) startTimes: LongArray,
        @Size(2) endTimes: LongArray,
        @Size(2) currentTimes: LongArray,
    ) {
        mStartTimes = startTimes
        mEndTimes = endTimes
        mCurrentTimes = currentTimes
        setIndicatorPosition(0)
        setIndicatorPosition(1)
        postInvalidateOnAnimation()
    }

    fun setColors(
        @ColorInt sunLineColor: Int,
        @ColorInt moonLineColor: Int,
        @ColorInt backgroundLineColor: Int,
        @ColorInt rootColor: Int,
        lightTheme: Boolean,
    ) {
        mLineColors = intArrayOf(sunLineColor, moonLineColor, backgroundLineColor)
        ensureShader(rootColor, sunLineColor, moonLineColor, lightTheme)
        postInvalidateOnAnimation()
    }

    fun setDayIndicatorRotation(rotation: Float) {
        mIconRotations[0] = rotation
        postInvalidateOnAnimation()
    }

    fun setNightIndicatorRotation(rotation: Float) {
        mIconRotations[1] = rotation
        postInvalidateOnAnimation()
    }

    private fun ensureShader(
        @ColorInt rootColor: Int,
        @ColorInt sunLineColor: Int,
        @ColorInt moonLineColor: Int,
        lightTheme: Boolean,
    ) {
        val lineShadowShader = if (lightTheme) {
            ColorUtils.setAlphaComponent(sunLineColor, (255 * SHADOW_ALPHA_FACTOR_LIGHT).toInt())
        } else {
            ColorUtils.setAlphaComponent(moonLineColor, (255 * SHADOW_ALPHA_FACTOR_DARK).toInt())
        }
        mX1ShaderColors[0] = org.breezyweather.common.utils.ColorUtils.blendColor(lineShadowShader, rootColor)
        mX1ShaderColors[1] = rootColor
        mX2ShaderColors[0] = org.breezyweather.common.utils.ColorUtils.blendColor(lineShadowShader, mX1ShaderColors[0])
        mX2ShaderColors[1] = rootColor
        mRootColor = rootColor
        if (mX1ShaderWrapper.isDifferent(measuredWidth, measuredHeight, lightTheme, mX1ShaderColors)) {
            mX1ShaderWrapper.setShader(
                LinearGradient(
                    0f,
                    mRectF.top,
                    0f,
                    measuredHeight - mMargin,
                    mX1ShaderColors[0],
                    mX1ShaderColors[1],
                    Shader.TileMode.CLAMP
                ),
                measuredWidth,
                measuredHeight,
                lightTheme,
                mX1ShaderColors
            )
        }
        if (mX2ShaderWrapper.isDifferent(measuredWidth, measuredHeight, lightTheme, mX2ShaderColors)) {
            mX2ShaderWrapper.setShader(
                LinearGradient(
                    0f,
                    mRectF.top,
                    0f,
                    measuredHeight - mMargin,
                    mX2ShaderColors[0],
                    mX2ShaderColors[1],
                    Shader.TileMode.CLAMP
                ),
                measuredWidth,
                measuredHeight,
                lightTheme,
                mX2ShaderColors
            )
        }
    }

    private fun ensureProgress(index: Int) {
        mMaxes[index] = mEndTimes[index] - mStartTimes[index]
        mProgresses[index] = mCurrentTimes[index] - mStartTimes[index]
        mProgresses[index] = max(mProgresses[index], 0)
        mProgresses[index] = min(mProgresses[index], mMaxes[index])
    }

    private fun setIndicatorPosition(index: Int) {
        ensureProgress(index)
        val startAngle = 270 - ARC_ANGLE / 2f
        val progressSweepAngle = (1.0 * mProgresses[index] / mMaxes[index] * ARC_ANGLE).toFloat()
        val progressEndAngle = startAngle + progressSweepAngle
        val deltaAngle = progressEndAngle - 180
        val deltaWidth = abs(mRectF.width() / 2 * cos(Math.toRadians(deltaAngle.toDouble()))).toFloat()
        val deltaHeight = abs(mRectF.width() / 2 * sin(Math.toRadians(deltaAngle.toDouble()))).toFloat()
        if (progressSweepAngle == 0f && mIconAlphas[index] != 0f) {
            mIconAlphas[index] = 0f
        } else if (progressSweepAngle != 0f && mIconAlphas[index] == 0f) {
            mIconAlphas[index] = 1f
        }
        if (mIconDrawables[index] != null) {
            if (progressEndAngle < 270) {
                mIconPositions[index][0] = mRectF.centerX() - deltaWidth - iconSize / 2f
            } else {
                mIconPositions[index][0] = mRectF.centerX() + deltaWidth - iconSize / 2f
            }
            mIconPositions[index][1] = mRectF.centerY() - deltaHeight - iconSize / 2f
        }
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val width = (MeasureSpec.getSize(widthMeasureSpec) - 2 * mMargin).toInt()
        val deltaRadians = Math.toRadians((180 - ARC_ANGLE) / 2.0)
        val radius = (width / 2 / cos(deltaRadians)).toInt()
        val height = (radius - width / 2 * tan(deltaRadians)).toInt()
        setMeasuredDimension(
            MeasureSpec.makeMeasureSpec((width + 2 * mMargin).toInt(), MeasureSpec.EXACTLY),
            MeasureSpec.makeMeasureSpec((height + 2 * mMargin).toInt(), MeasureSpec.EXACTLY)
        )
        val centerX = measuredWidth / 2
        val centerY = (mMargin + radius).toInt()
        mRectF.set(
            (centerX - radius).toFloat(),
            (centerY - radius).toFloat(),
            (centerX + radius).toFloat(),
            (centerY + radius).toFloat()
        )
        ensureShader(mRootColor, mLineColors[0], mLineColors[1], mX1ShaderWrapper.isLightTheme)
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        // shadow.
        val startAngle = 270 - ARC_ANGLE / 2f
        val progressSweepAngleDay = (1.0 * mProgresses[0] / mMaxes[0] * ARC_ANGLE).toFloat()
        val progressEndAngleDay = startAngle + progressSweepAngleDay
        val progressSweepAngleNight = (1.0 * mProgresses[1] / mMaxes[1] * ARC_ANGLE).toFloat()
        val progressEndAngleNight = startAngle + progressSweepAngleNight
        if (progressEndAngleDay == progressEndAngleNight) {
            drawShadow(canvas, 0, progressEndAngleDay, mX2ShaderWrapper.shader)
        } else if (progressEndAngleDay > progressEndAngleNight) {
            drawShadow(canvas, 0, progressEndAngleDay, mX1ShaderWrapper.shader)
            drawShadow(canvas, 1, progressEndAngleNight, mX2ShaderWrapper.shader)
        } else { // progressEndAngleDay < progressEndAngleNight
            drawShadow(canvas, 1, progressEndAngleNight, mX1ShaderWrapper.shader)
            drawShadow(canvas, 0, progressEndAngleDay, mX2ShaderWrapper.shader)
        }

        // sub line.
        mPaint.color = mLineColors[2]
        mPaint.style = Paint.Style.STROKE
        mPaint.strokeWidth = mDottedLineSize
        mPaint.setPathEffect(mEffect)
        canvas.drawArc(mRectF, startAngle, ARC_ANGLE.toFloat(), false, mPaint)
        canvas.drawLine(
            mMargin,
            measuredHeight - mMargin,
            measuredWidth - mMargin,
            measuredHeight - mMargin,
            mPaint
        )

        // path.
        drawPathLine(
            canvas,
            1,
            startAngle,
            (1.0 * mProgresses[1] / mMaxes[1] * ARC_ANGLE).toFloat()
        )
        drawPathLine(
            canvas,
            0,
            startAngle,
            (1.0 * mProgresses[0] / mMaxes[0] * ARC_ANGLE).toFloat()
        )
        var restoreCount: Int

        // icon.
        for (i in 1 downTo 0) {
            if (mIconDrawables[i] == null || mProgresses[i] <= 0) {
                continue
            }
            restoreCount = canvas.save()
            canvas.translate(mIconPositions[i][0], mIconPositions[i][1])
            canvas.rotate(mIconRotations[i], iconSize / 2f, iconSize / 2f)
            mIconDrawables[i]!!.draw(canvas)
            canvas.restoreToCount(restoreCount)
        }
    }

    private fun drawShadow(canvas: Canvas, index: Int, progressEndAngle: Float, shader: Shader?) {
        if (mProgresses[index] > 0) {
            val layerId = canvas.saveLayer(
                mRectF.left,
                mRectF.top,
                mRectF.right,
                mRectF.top + mRectF.height() / 2,
                null
            )
            mPaint.style = Paint.Style.FILL
            mPaint.setShader(shader)
            canvas.drawArc(
                mRectF,
                270 - ARC_ANGLE / 2f,
                ARC_ANGLE.toFloat(),
                false,
                mPaint
            )
            mPaint.setShader(null)
            mPaint.setXfermode(mClearXfermode)
            canvas.drawRect(
                (mRectF.centerX() + mRectF.width() / 2 * cos((360 - progressEndAngle) * Math.PI / 180)).toFloat(),
                mRectF.top,
                mRectF.right,
                mRectF.top + mRectF.height() / 2,
                mPaint
            )
            mPaint.setXfermode(null)
            canvas.restoreToCount(layerId)
        }
    }

    private fun drawPathLine(
        canvas: Canvas,
        index: Int,
        startAngle: Float,
        progressSweepAngle: Float,
    ) {
        if (mProgresses[index] > 0) {
            mPaint.apply {
                color = mLineColors[index]
                strokeWidth = mLineSize
                pathEffect = null
            }
            canvas.drawArc(mRectF, startAngle, progressSweepAngle, false, mPaint)
        }
    }

    fun setSunDrawable(d: Drawable?) {
        if (d != null) {
            mIconDrawables[0] = d
            mIconDrawables[0]!!.setBounds(0, 0, iconSize, iconSize)
        }
    }

    fun setMoonDrawable(d: Drawable?) {
        if (d != null) {
            mIconDrawables[1] = d
            mIconDrawables[1]!!.setBounds(0, 0, iconSize, iconSize)
        }
    }

    companion object {
        private const val ICON_SIZE_DIP = 24f
        private const val LINE_SIZE_DIP = 5f
        private const val DOTTED_LINE_SIZE_DIP = 1f
        private const val MARGIN_DIP = 16f
        private const val ARC_ANGLE = 135
        private const val SHADOW_ALPHA_FACTOR_LIGHT = 0.1f
        private const val SHADOW_ALPHA_FACTOR_DARK = 0.2f
    }
}
