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

package org.breezyweather.common.ui.widgets

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.util.AttributeSet
import android.view.View
import androidx.annotation.ColorInt
import org.breezyweather.common.extensions.dpToPx

/**
 * Round progress.
 */
class RoundProgress @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
) : View(context, attrs, defStyleAttr) {
    private val mProgressPaint = Paint().apply {
        isAntiAlias = true
        style = Paint.Style.FILL
        strokeCap = Paint.Cap.ROUND
    }
    private val mBackgroundRectF = RectF()
    private val mProgressRectF = RectF()
    private var mProgress = 0f
    private var mMax = 100f

    @ColorInt
    private var mProgressColor = Color.BLACK

    @ColorInt
    private var mBackgroundColor = Color.GRAY

    var progress: Float
        get() = mProgress
        set(progress) {
            mProgress = progress
            if (mProgress > max) {
                mProgress = max
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

    fun setProgressBackgroundColor(@ColorInt backgroundColor: Int) {
        mBackgroundColor = backgroundColor
        invalidate()
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        val padding = context.dpToPx(2f).toInt()
        mBackgroundRectF.set(
            padding.toFloat(),
            padding.toFloat(),
            (measuredWidth - padding).toFloat(),
            (measuredHeight - padding).toFloat()
        )
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        val radius = mBackgroundRectF.height() / 2f
        mProgressPaint.color = mBackgroundColor
        canvas.drawRoundRect(mBackgroundRectF, radius, radius, mProgressPaint)

        mProgressRectF.set(
            mBackgroundRectF.left,
            mBackgroundRectF.top,
            mBackgroundRectF.left + mBackgroundRectF.width() * mProgress / mMax,
            mBackgroundRectF.bottom
        )
        mProgressPaint.color = mProgressColor
        if (mProgressRectF.width() < 2 * radius) {
            canvas.drawCircle(
                mProgressRectF.left + radius,
                mProgressRectF.top + radius,
                radius,
                mProgressPaint
            )
        } else {
            canvas.drawRoundRect(mProgressRectF, radius, radius, mProgressPaint)
        }
    }
}
