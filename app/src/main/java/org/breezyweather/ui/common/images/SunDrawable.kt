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

package org.breezyweather.ui.common.images

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.ColorFilter
import android.graphics.Paint
import android.graphics.PixelFormat
import android.graphics.Rect
import android.graphics.drawable.Drawable
import androidx.annotation.ColorInt
import androidx.annotation.Size
import kotlin.math.min
import kotlin.math.sin

class SunDrawable : Drawable() {
    private val mPaint = Paint().apply {
        isAntiAlias = true
    }

    @ColorInt
    private val mCoreColor: Int = Color.rgb(254, 214, 117)

    @Size(2)
    @ColorInt
    private val mHaloColors: IntArray = intArrayOf(
        Color.rgb(249, 183, 93),
        Color.rgb(252, 198, 101)
    )
    private var mAlpha: Float = 1f
    private var mBounds: Rect
    private var mRadius = 0f
    private var mCX = 0f
    private var mCY = 0f

    init {
        mBounds = bounds
        ensurePosition(mBounds)
    }

    private fun ensurePosition(bounds: Rect) {
        val boundSize = min(bounds.width(), bounds.height()).toFloat()
        mRadius = (sin(Math.PI / 4) * boundSize / 2).toFloat() - 2
        mCX = (1.0 * bounds.width() / 2 + bounds.left).toFloat()
        mCY = (1.0 * bounds.height() / 2 + bounds.top).toFloat()
    }

    override fun onBoundsChange(bounds: Rect) {
        mBounds = bounds
        ensurePosition(bounds)
    }

    override fun draw(canvas: Canvas) {
        mPaint.alpha = (mAlpha * 255).toInt()
        mPaint.color = mHaloColors[0]
        canvas.drawRect(
            mCX - mRadius,
            mCY - mRadius,
            mCX + mRadius,
            mCY + mRadius,
            mPaint
        )
        mPaint.color = mHaloColors[0]
        val restoreCount = canvas.save()
        canvas.rotate(45f, mCX, mCY)
        canvas.drawRect(
            mCX - mRadius,
            mCY - mRadius,
            mCX + mRadius,
            mCY + mRadius,
            mPaint
        )
        canvas.restoreToCount(restoreCount)
        mPaint.color = mCoreColor
        canvas.drawCircle(mCX, mCY, mRadius, mPaint)
    }

    override fun setAlpha(alpha: Int) {
        mAlpha = alpha.toFloat()
    }

    override fun setColorFilter(colorFilter: ColorFilter?) {
        mPaint.setColorFilter(colorFilter)
    }

    @Deprecated("Deprecated in Java")
    override fun getOpacity(): Int {
        return PixelFormat.OPAQUE
    }

    override fun getIntrinsicWidth(): Int {
        return mBounds.width()
    }

    override fun getIntrinsicHeight(): Int {
        return mBounds.height()
    }
}
