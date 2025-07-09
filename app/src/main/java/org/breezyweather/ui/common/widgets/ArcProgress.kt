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

package org.breezyweather.ui.common.widgets

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Shader
import android.text.TextPaint
import android.util.AttributeSet
import android.view.View
import androidx.annotation.ColorInt
import androidx.annotation.Size
import androidx.core.graphics.ColorUtils
import org.breezyweather.R
import org.breezyweather.common.extensions.dpToPx
import org.breezyweather.common.extensions.getTypefaceFromTextAppearance
import kotlin.math.cos

class ArcProgress @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
) : View(context, attrs, defStyleAttr) {
    private val mProgressPaint: Paint
    private val mShadowPaint: Paint
    private val mCenterTextPaint: Paint
    private val mBottomTextPaint: Paint
    private val mShaderWrapper: DayNightShaderWrapper
    private val mRectF = RectF()
    private var mArcBottomHeight = 0f
    private var mProgress: Float
    private var mProgressMaxed: Float
    private var mMax: Float
    private val mArcAngle: Float
    private val mProgressWidth: Float

    @ColorInt
    private var mProgressColor: Int

    @ColorInt
    private var mShadowColor: Int

    @ColorInt
    private var mShaderColor: Int

    @ColorInt
    private var mBackgroundColor: Int
    private var mText: String?
    private val mTextSize: Float

    @ColorInt
    private var mTextColor: Int

    @Size(2)
    private val mShaderColors: IntArray
    private var mBottomText: String?
    private val mBottomTextSize: Float

    @ColorInt
    private var mBottomTextColor: Int

    init {
        val attributes = context.theme
            .obtainStyledAttributes(attrs, R.styleable.ArcProgress, defStyleAttr, 0)
        mProgress = attributes.getInt(R.styleable.ArcProgress_progress, 0).toFloat()
        mProgressMaxed = attributes.getInt(R.styleable.ArcProgress_progress, 0).toFloat()
        mMax = attributes.getInt(R.styleable.ArcProgress_max, 100).toFloat()
        mArcAngle = attributes.getFloat(R.styleable.ArcProgress_arc_angle, 360 * 0.8f)
        mProgressWidth = attributes.getDimension(R.styleable.ArcProgress_progress_width, getContext().dpToPx(8f))
        mProgressColor = attributes.getColor(R.styleable.ArcProgress_progress_color, Color.BLACK)
        mShadowColor = Color.argb((0.2 * 255).toInt(), 0, 0, 0)
        mShaderColor = Color.argb((0.2 * 255).toInt(), 0, 0, 0)
        mBackgroundColor = attributes.getColor(R.styleable.ArcProgress_background_color, Color.GRAY)
        mText = attributes.getString(R.styleable.ArcProgress_text)
        mTextSize = attributes.getDimension(R.styleable.ArcProgress_text_size, getContext().dpToPx(36f))
        mTextColor = attributes.getColor(R.styleable.ArcProgress_text_color, Color.DKGRAY)
        mBottomText = attributes.getString(R.styleable.ArcProgress_bottom_text)
        mBottomTextSize = attributes.getDimension(
            R.styleable.ArcProgress_bottom_text_size,
            getContext().dpToPx(14f)
        )
        mBottomTextColor = attributes.getColor(R.styleable.ArcProgress_bottom_text_color, Color.DKGRAY)
        attributes.recycle()
        mProgressPaint = Paint().apply {
            isAntiAlias = true
            strokeWidth = mProgressWidth
            style = Paint.Style.STROKE
            strokeCap = Paint.Cap.ROUND
        }
        mShadowPaint = Paint().apply {
            isAntiAlias = true
            style = Paint.Style.FILL
        }
        mCenterTextPaint = TextPaint().apply {
            textSize = mTextSize
            isAntiAlias = true
            typeface = getContext().getTypefaceFromTextAppearance(R.style.large_title_text)
        }
        mBottomTextPaint = TextPaint().apply {
            set(mCenterTextPaint)
            typeface = getContext().getTypefaceFromTextAppearance(R.style.content_text)
        }
        mShaderColors = intArrayOf(Color.BLACK, Color.WHITE)
        mShaderWrapper = DayNightShaderWrapper(measuredWidth, measuredHeight, lightTheme = true, mShaderColors)
    }

    var progress: Float
        get() = mProgress
        set(progress) {
            mProgress = progress
            mProgressMaxed = progress
            if (mProgressMaxed > max) {
                mProgressMaxed = max
            }
            invalidate()
        }

    var max: Float
        get() = mMax
        set(max) {
            if (max > 0) {
                mMax = max
                invalidate()
            }
        }

    fun setProgressColor(lightTheme: Boolean) {
        setProgressColor(mProgressColor, lightTheme)
    }

    fun setProgressColor(@ColorInt progressColor: Int, lightTheme: Boolean) {
        mProgressColor = progressColor
        mShadowColor = org.breezyweather.common.utils.ColorUtils.getDarkerColor(progressColor)
        mShaderColor = ColorUtils.setAlphaComponent(
            progressColor,
            (255 * if (lightTheme) SHADOW_ALPHA_FACTOR_LIGHT else SHADOW_ALPHA_FACTOR_DARK).toInt()
        )
        invalidate()
    }

    fun setArcBackgroundColor(@ColorInt backgroundColor: Int) {
        mBackgroundColor = backgroundColor
        invalidate()
    }

    fun setText(text: String?) {
        mText = text
        invalidate()
    }

    fun setTextColor(@ColorInt textColor: Int) {
        mTextColor = textColor
        invalidate()
    }

    fun setBottomText(bottomText: String?) {
        mBottomText = bottomText
        invalidate()
    }

    fun setBottomTextColor(@ColorInt bottomTextColor: Int) {
        mBottomTextColor = bottomTextColor
        invalidate()
    }

    private fun ensureShadowShader() {
        mShaderColors[0] = mShaderColor
        mShaderColors[1] = Color.TRANSPARENT
        if (mShaderWrapper.isDifferent(measuredWidth, measuredHeight, false, mShaderColors)) {
            mShaderWrapper.setShader(
                LinearGradient(
                    0f,
                    mRectF.top,
                    0f,
                    mRectF.bottom,
                    mShaderColors[0],
                    mShaderColors[1],
                    Shader.TileMode.CLAMP
                ),
                measuredWidth,
                measuredHeight,
                false,
                mShaderColors
            )
        }
    }

    override fun getSuggestedMinimumHeight(): Int {
        return context.dpToPx(100f).toInt()
    }

    override fun getSuggestedMinimumWidth(): Int {
        return context.dpToPx(100f).toInt()
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        setMeasuredDimension(widthMeasureSpec, heightMeasureSpec)
        val width = MeasureSpec.getSize(widthMeasureSpec)
        val arcPadding = context.dpToPx(4f).toInt()
        mRectF.set(
            mProgressWidth / 2f + arcPadding,
            mProgressWidth / 2f + arcPadding,
            width - mProgressWidth / 2f - arcPadding,
            MeasureSpec.getSize(heightMeasureSpec) - mProgressWidth / 2f - arcPadding
        )
        val radius = (width - 2 * arcPadding) / 2f
        val angle = (360 - mArcAngle) / 2f
        mArcBottomHeight = radius * (1 - cos(angle / 180 * Math.PI)).toFloat()
        ensureShadowShader()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        if (layoutDirection == LAYOUT_DIRECTION_RTL) canvas.scale(-1f, 1f, width / 2f, height / 2f)

        val startAngle = 270 - mArcAngle / 2f
        val progressSweepAngle = (1.0 * mProgressMaxed / max * mArcAngle).toFloat()
        val progressEndAngle = startAngle + progressSweepAngle
        val deltaAngle = (mProgressWidth / 2 / Math.PI / (mRectF.width() / 2) * 180).toFloat()
        if (mProgressMaxed > 0) {
            ensureShadowShader()
            mShadowPaint.setShader(mShaderWrapper.shader)
            if (progressEndAngle + deltaAngle >= 360) {
                canvas.drawCircle(
                    mRectF.centerX(),
                    mRectF.centerY(),
                    mRectF.width() / 2,
                    mShadowPaint
                )
            } else if (progressEndAngle + deltaAngle > 180) {
                canvas.drawArc(
                    mRectF,
                    360 - progressEndAngle - deltaAngle,
                    360 - 2 * (360 - progressEndAngle - deltaAngle),
                    false,
                    mShadowPaint
                )
            }
        }
        mProgressPaint.color = mBackgroundColor
        canvas.drawArc(mRectF, startAngle, mArcAngle, false, mProgressPaint)
        if (mProgressMaxed > 0) {
            mProgressPaint.color = mProgressColor
            canvas.drawArc(mRectF, startAngle, progressSweepAngle, false, mProgressPaint)
        }
        if (!mText.isNullOrEmpty()) {
            if (layoutDirection == LAYOUT_DIRECTION_RTL) canvas.scale(-1f, 1f, width / 2f, height / 2f)
            mCenterTextPaint.color = mTextColor
            mCenterTextPaint.textSize = mTextSize
            val textHeight = mCenterTextPaint.descent() + mCenterTextPaint.ascent()
            val textBaseline = (height - textHeight) / 2.0f
            canvas.drawText(
                mText!!,
                (width - mCenterTextPaint.measureText(mText)) / 2.0f,
                textBaseline,
                mCenterTextPaint
            )
            if (layoutDirection == LAYOUT_DIRECTION_RTL) canvas.scale(-1f, 1f, width / 2f, height / 2f)
        }
        if (mArcBottomHeight == 0f) {
            val radius = width / 2f
            val angle = (360 - mArcAngle) / 2f
            mArcBottomHeight = radius * (1 - cos(angle / 180 * Math.PI)).toFloat()
        }
        if (!mBottomText.isNullOrEmpty()) {
            if (layoutDirection == LAYOUT_DIRECTION_RTL) canvas.scale(-1f, 1f, width / 2f, height / 2f)
            mBottomTextPaint.color = mBottomTextColor
            mBottomTextPaint.textSize = mBottomTextSize
            val bottomTextBaseline = (
                height +
                    (mBottomTextPaint.descent() + mBottomTextPaint.ascent()) / 2 -
                    mProgressWidth * 0.33f
                )
            canvas.drawText(
                mBottomText!!,
                (width - mBottomTextPaint.measureText(mBottomText)) / 2.0f,
                bottomTextBaseline,
                mBottomTextPaint
            )
            if (layoutDirection == LAYOUT_DIRECTION_RTL) canvas.scale(-1f, 1f, width / 2f, height / 2f)
        }
    }

    companion object {
        private const val SHADOW_ALPHA_FACTOR_LIGHT = 0.1f
        private const val SHADOW_ALPHA_FACTOR_DARK = 0.1f
    }
}
