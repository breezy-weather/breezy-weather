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

package org.breezyweather.common.ui.widgets.astro

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.util.AttributeSet
import android.view.View
import androidx.annotation.ColorInt
import org.breezyweather.common.extensions.dpToPx
import kotlin.math.cos
import kotlin.math.sin

class MoonPhaseView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
) : View(context, attrs, defStyleAttr) {
    private var mPaint = Paint().apply {
        isAntiAlias = true
        strokeCap = Paint.Cap.ROUND
    }
    private var mForegroundRectF = RectF()
    private var mBackgroundRectF = RectF()
    private var mSurfaceAngle = 0f // head of light surface, clockwise.

    @ColorInt
    private var mLightColor = 0

    @ColorInt
    private var mDarkColor = 0

    @ColorInt
    private var mStrokeColor = 0
    private var lineWidth = 1f

    init {
        setColor(Color.WHITE, Color.BLACK, Color.GRAY)
        setSurfaceAngle(0f) // from 0 -> phase : 🌑 (new)
        lineWidth = context.dpToPx(lineWidth.toInt().toFloat())
    }

    fun setColor(
        @ColorInt lightColor: Int,
        @ColorInt darkColor: Int,
        @ColorInt strokeColor: Int,
    ) {
        mLightColor = lightColor
        mDarkColor = darkColor
        mStrokeColor = strokeColor
    }

    fun setSurfaceAngle(surfaceAngle: Float) {
        mSurfaceAngle = surfaceAngle
        if (mSurfaceAngle >= 360) {
            mSurfaceAngle %= 360f
        }
        invalidate()
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        setMeasuredDimension(widthMeasureSpec, heightMeasureSpec)
        val padding = context.dpToPx(4f).toInt()
        mBackgroundRectF.set(
            padding.toFloat(),
            padding.toFloat(),
            (measuredWidth - padding).toFloat(),
            (measuredHeight - padding).toFloat()
        )
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        mPaint.style = Paint.Style.FILL
        if (mSurfaceAngle == 0f) { // 🌑
            drawDarkCircle(canvas)
        } else if (mSurfaceAngle < 90) { // 🌒
            drawLightCircle(canvas)
            mPaint.color = mDarkColor
            canvas.drawArc(mBackgroundRectF, 90f, 180f, true, mPaint)
            val halfWidth = (mBackgroundRectF.width() / 2 * cos(Math.toRadians(mSurfaceAngle.toDouble()))).toFloat()
            mForegroundRectF.set(
                mBackgroundRectF.centerX() - halfWidth,
                mBackgroundRectF.top,
                mBackgroundRectF.centerX() + halfWidth,
                mBackgroundRectF.bottom
            )
            canvas.drawArc(mForegroundRectF, 270f, 180f, true, mPaint)
        } else if (mSurfaceAngle == 90f) { // 🌓
            drawDarkCircle(canvas)
            mPaint.color = mLightColor
            canvas.drawArc(mBackgroundRectF, 270f, 180f, true, mPaint)
        } else if (mSurfaceAngle < 180) { // 🌔
            drawDarkCircle(canvas)
            mPaint.color = mLightColor
            canvas.drawArc(mBackgroundRectF, 270f, 180f, true, mPaint)
            val halfWidth = (mBackgroundRectF.width() / 2 * sin(Math.toRadians((mSurfaceAngle - 90).toDouble())))
                .toFloat()
            mForegroundRectF.set(
                mBackgroundRectF.centerX() - halfWidth,
                mBackgroundRectF.top,
                mBackgroundRectF.centerX() + halfWidth,
                mBackgroundRectF.bottom
            )
            canvas.drawArc(mForegroundRectF, 90f, 180f, true, mPaint)
        } else if (mSurfaceAngle == 180f) { // 🌕
            drawLightCircle(canvas)
        } else if (mSurfaceAngle < 270) { // 🌖
            drawDarkCircle(canvas)
            mPaint.color = mLightColor
            canvas.drawArc(mBackgroundRectF, 90f, 180f, true, mPaint)
            val halfWidth = (mBackgroundRectF.width() / 2 * cos(Math.toRadians((mSurfaceAngle - 180).toDouble())))
                .toFloat()
            mForegroundRectF.set(
                mBackgroundRectF.centerX() - halfWidth,
                mBackgroundRectF.top,
                mBackgroundRectF.centerX() + halfWidth,
                mBackgroundRectF.bottom
            )
            canvas.drawArc(mForegroundRectF, 270f, 180f, true, mPaint)
        } else if (mSurfaceAngle == 270f) { // 🌗
            drawDarkCircle(canvas)
            mPaint.color = mLightColor
            canvas.drawArc(mBackgroundRectF, 90f, 180f, true, mPaint)
        } else { // surface angle < 360. 🌘
            drawLightCircle(canvas)
            mPaint.color = mDarkColor
            canvas.drawArc(mBackgroundRectF, 270f, 180f, true, mPaint)
            val halfWidth = (mBackgroundRectF.width() / 2 * cos(Math.toRadians((360 - mSurfaceAngle).toDouble())))
                .toFloat()
            mForegroundRectF.set(
                mBackgroundRectF.centerX() - halfWidth,
                mBackgroundRectF.top,
                mBackgroundRectF.centerX() + halfWidth,
                mBackgroundRectF.bottom
            )
            canvas.drawArc(mForegroundRectF, 90f, 180f, true, mPaint)
        }
        mPaint.style = Paint.Style.STROKE
        mPaint.strokeWidth = lineWidth
        if (mSurfaceAngle < 90 || 270 < mSurfaceAngle) {
            mPaint.color = mDarkColor
            canvas.drawLine(
                mBackgroundRectF.centerX(),
                mBackgroundRectF.top,
                mBackgroundRectF.centerX(),
                mBackgroundRectF.bottom,
                mPaint
            )
        } else if (90 < mSurfaceAngle && mSurfaceAngle < 270) {
            mPaint.color = mLightColor
            canvas.drawLine(
                mBackgroundRectF.centerX(),
                mBackgroundRectF.top,
                mBackgroundRectF.centerX(),
                mBackgroundRectF.bottom,
                mPaint
            )
        }
        mPaint.color = mStrokeColor
        canvas.drawCircle(
            mBackgroundRectF.centerX(),
            mBackgroundRectF.centerY(),
            mBackgroundRectF.width() / 2,
            mPaint
        )
    }

    private fun drawLightCircle(canvas: Canvas) {
        mPaint.color = mLightColor
        canvas.drawCircle(
            mBackgroundRectF.centerX(),
            mBackgroundRectF.centerY(),
            mBackgroundRectF.width() / 2,
            mPaint
        )
    }

    private fun drawDarkCircle(canvas: Canvas) {
        mPaint.color = mDarkColor
        canvas.drawCircle(
            mBackgroundRectF.centerX(),
            mBackgroundRectF.centerY(),
            mBackgroundRectF.width() / 2,
            mPaint
        )
    }
}
