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

package org.breezyweather.ui.common.widgets

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.util.AttributeSet
import android.view.View
import androidx.annotation.ColorInt
import org.breezyweather.R
import org.breezyweather.common.extensions.dpToPx
import kotlin.math.cos

class ArcProgress @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
) : View(context, attrs, defStyleAttr) {
    private val mProgressPaint: Paint
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
    private var mBackgroundColor: Int

    init {
        val attributes = context.theme
            .obtainStyledAttributes(attrs, R.styleable.ArcProgress, defStyleAttr, 0)
        mProgress = attributes.getInt(R.styleable.ArcProgress_progress, 0).toFloat()
        mProgressMaxed = attributes.getInt(R.styleable.ArcProgress_progress, 0).toFloat()
        mMax = attributes.getInt(R.styleable.ArcProgress_max, 100).toFloat()
        mArcAngle = attributes.getFloat(R.styleable.ArcProgress_arc_angle, 360 * 0.8f)
        mProgressWidth = attributes.getDimension(R.styleable.ArcProgress_progress_width, getContext().dpToPx(8f))
        mProgressColor = attributes.getColor(R.styleable.ArcProgress_progress_color, Color.BLACK)
        mBackgroundColor = attributes.getColor(R.styleable.ArcProgress_background_color, Color.GRAY)
        attributes.recycle()
        mProgressPaint = Paint().apply {
            isAntiAlias = true
            strokeWidth = mProgressWidth
            style = Paint.Style.STROKE
            strokeCap = Paint.Cap.ROUND
        }
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

    fun setProgressColor(@ColorInt progressColor: Int) {
        mProgressColor = progressColor
        invalidate()
    }

    fun setArcBackgroundColor(@ColorInt backgroundColor: Int) {
        mBackgroundColor = backgroundColor
        invalidate()
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
        // ensureShadowShader()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        if (layoutDirection == LAYOUT_DIRECTION_RTL) canvas.scale(-1f, 1f, width / 2f, height / 2f)

        val startAngle = 270 - mArcAngle / 2f
        val progressSweepAngle = (1.0 * mProgressMaxed / max * mArcAngle).toFloat()
        mProgressPaint.color = mBackgroundColor
        canvas.drawArc(mRectF, startAngle, mArcAngle, false, mProgressPaint)
        if (mProgressMaxed > 0) {
            mProgressPaint.color = mProgressColor
            canvas.drawArc(mRectF, startAngle, progressSweepAngle, false, mProgressPaint)
        }
        if (mArcBottomHeight == 0f) {
            val radius = width / 2f
            val angle = (360 - mArcAngle) / 2f
            mArcBottomHeight = radius * (1 - cos(angle / 180 * Math.PI)).toFloat()
        }
    }
}
