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
import androidx.core.graphics.withTranslation
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
    private var mIconDrawable: Drawable? = null
    private val mPaint = Paint().apply {
        isAntiAlias = true
        strokeCap = Paint.Cap.ROUND
    }
    private var mClearXfermode: Xfermode = PorterDuffXfermode(PorterDuff.Mode.CLEAR)
    private var mX1ShaderWrapper: DayNightShaderWrapper
    private var mX2ShaderWrapper: DayNightShaderWrapper
    private var mEffect: PathEffect? = null
    private val mRectF = RectF()

    private var mIconRotation = 0f
    private var mIconAlpha = 0f
    private val mIconPositions = floatArrayOf(0f, 0f)
    private var mStartTime = 1L
    private var mCurrentTime = 1L
    private var mEndTime = 0L
    private var mProgress = -1L
    private var mMax = 100L

    @Size(2)
    private var mLineColors = intArrayOf(Color.BLACK, Color.LTGRAY)

    @Size(2)
    private val mX1ShaderColors = intArrayOf(Color.GRAY, Color.WHITE)

    @Size(2)
    private val mX2ShaderColors = intArrayOf(Color.BLACK, Color.WHITE)

    @ColorInt
    private var mRootColor = Color.WHITE
    private var mLineSize = 0f
    private var mDottedLineSize = 0f
    private var mHorizontalMargin = 0f
    private var mTopMargin = 0f
    private var mBottomMargin = 0f
    private var iconSize = 0

    init {
        mLineSize = context.dpToPx(LINE_SIZE_DIP)
        mDottedLineSize = context.dpToPx(DOTTED_LINE_SIZE_DIP)
        mHorizontalMargin = context.dpToPx(MARGIN_HORIZONTAL_DIP)
        mTopMargin = context.dpToPx(MARGIN_TOP_DIP)
        mBottomMargin = context.dpToPx(MARGIN_BOTTOM_DIP)
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
        startTime: Long,
        endTime: Long,
        currentTime: Long,
    ) {
        mStartTime = startTime
        mEndTime = endTime
        mCurrentTime = currentTime
        setIndicatorPosition()
        setIndicatorPosition()
        postInvalidateOnAnimation()
    }

    fun setColors(
        @ColorInt lineColor: Int,
        @ColorInt backgroundLineColor: Int,
        @ColorInt rootColor: Int,
        lightTheme: Boolean,
    ) {
        mLineColors = intArrayOf(lineColor, backgroundLineColor)
        ensureShader(rootColor, lineColor, lightTheme)
        postInvalidateOnAnimation()
    }

    fun setIndicatorRotation(rotation: Float) {
        mIconRotation = rotation
        postInvalidateOnAnimation()
    }

    private fun ensureShader(
        @ColorInt rootColor: Int,
        @ColorInt lineColor: Int,
        lightTheme: Boolean,
    ) {
        val lineShadowShader = if (lightTheme) {
            ColorUtils.setAlphaComponent(lineColor, (255 * SHADOW_ALPHA_FACTOR_LIGHT).toInt())
        } else {
            ColorUtils.setAlphaComponent(lineColor, (255 * SHADOW_ALPHA_FACTOR_DARK).toInt())
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
                    measuredHeight - mTopMargin - mBottomMargin,
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
                    measuredHeight - mTopMargin - mBottomMargin,
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

    private fun ensureProgress() {
        mMax = mEndTime - mStartTime
        mProgress = mCurrentTime - mStartTime
        mProgress = max(mProgress, 0)
        mProgress = min(mProgress, mMax)
    }

    private fun setIndicatorPosition() {
        ensureProgress()
        val startAngle = 270 - ARC_ANGLE / 2f
        val progressSweepAngle = (1.0 * mProgress / mMax * ARC_ANGLE).toFloat()
        val progressEndAngle = startAngle + progressSweepAngle
        val deltaAngle = progressEndAngle - 180
        val deltaWidth = abs(mRectF.width() / 2 * cos(Math.toRadians(deltaAngle.toDouble()))).toFloat()
        val deltaHeight = abs(mRectF.width() / 2 * sin(Math.toRadians(deltaAngle.toDouble()))).toFloat()
        if (progressSweepAngle == 0f && mIconAlpha != 0f) {
            mIconAlpha = 0f
        } else if (progressSweepAngle != 0f && mIconAlpha == 0f) {
            mIconAlpha = 1f
        }
        if (mIconDrawable != null) {
            if (progressEndAngle < 270) {
                mIconPositions[0] = mRectF.centerX() - deltaWidth - iconSize / 2f
            } else {
                mIconPositions[0] = mRectF.centerX() + deltaWidth - iconSize / 2f
            }
            mIconPositions[1] = mRectF.centerY() - deltaHeight - iconSize / 2f
        }
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val width = (MeasureSpec.getSize(widthMeasureSpec) - 2 * mHorizontalMargin).toInt()
        val deltaRadians = Math.toRadians((180 - ARC_ANGLE) / 2.0)
        val radius = (width / 2 / cos(deltaRadians)).toInt()
        val height = (radius - width / 2 * tan(deltaRadians)).toInt()
        setMeasuredDimension(
            MeasureSpec.makeMeasureSpec((width + 2 * mHorizontalMargin).toInt(), MeasureSpec.EXACTLY),
            MeasureSpec.makeMeasureSpec((height + mTopMargin + mBottomMargin).toInt(), MeasureSpec.EXACTLY)
        )
        val centerX = measuredWidth / 2
        val centerY = (mTopMargin + radius).toInt()
        mRectF.set(
            (centerX - radius).toFloat(),
            (centerY - radius).toFloat(),
            (centerX + radius).toFloat(),
            (centerY + radius).toFloat()
        )
        ensureShader(mRootColor, mLineColors[0], mX1ShaderWrapper.isLightTheme)
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        if (layoutDirection == LAYOUT_DIRECTION_RTL) canvas.scale(-1f, 1f, width / 2f, height / 2f)

        // shadow.
        val startAngle = 270 - ARC_ANGLE / 2f
        val progressSweepAngle = (1.0 * mProgress / mMax * ARC_ANGLE).toFloat()
        val progressEndAngle = startAngle + progressSweepAngle
        drawShadow(canvas, progressEndAngle, mX2ShaderWrapper.shader)

        // sub line.
        mPaint.color = mLineColors[1]
        mPaint.style = Paint.Style.STROKE
        mPaint.strokeWidth = mDottedLineSize
        mPaint.setPathEffect(mEffect)
        canvas.drawArc(mRectF, startAngle, ARC_ANGLE.toFloat(), false, mPaint)
        canvas.drawLine(
            mHorizontalMargin,
            measuredHeight - mBottomMargin,
            measuredWidth - mHorizontalMargin,
            measuredHeight - mBottomMargin,
            mPaint
        )

        // path.
        drawPathLine(
            canvas,
            startAngle,
            (1.0 * mProgress / mMax * ARC_ANGLE).toFloat()
        )

        // icon.
        if (mIconDrawable == null || mProgress <= 0) {
            return
        }
        canvas.withTranslation(mIconPositions[0], mIconPositions[1]) {
            canvas.rotate(mIconRotation, iconSize / 2f, iconSize / 2f)
            mIconDrawable!!.draw(canvas)
        }
    }

    private fun drawShadow(canvas: Canvas, progressEndAngle: Float, shader: Shader?) {
        if (mProgress > 0) {
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
        startAngle: Float,
        progressSweepAngle: Float,
    ) {
        if (mProgress > 0) {
            mPaint.apply {
                color = mLineColors[0]
                strokeWidth = mLineSize
                pathEffect = null
            }
            canvas.drawArc(mRectF, startAngle, progressSweepAngle, false, mPaint)
        }
    }

    fun setDrawable(d: Drawable?) {
        if (d != null) {
            mIconDrawable = d
            mIconDrawable!!.setBounds(0, 0, iconSize, iconSize)
        }
    }

    companion object {
        private const val ICON_SIZE_DIP = 24f
        private const val LINE_SIZE_DIP = 5f
        private const val DOTTED_LINE_SIZE_DIP = 1f
        private const val MARGIN_HORIZONTAL_DIP = 4f
        private const val MARGIN_TOP_DIP = 16f
        private const val MARGIN_BOTTOM_DIP = 8f
        private const val ARC_ANGLE = 135
        private const val SHADOW_ALPHA_FACTOR_LIGHT = 0.1f
        private const val SHADOW_ALPHA_FACTOR_DARK = 0.2f
    }
}
