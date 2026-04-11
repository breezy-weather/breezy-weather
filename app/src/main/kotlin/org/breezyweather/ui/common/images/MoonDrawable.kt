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

package org.breezyweather.ui.common.images

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.ColorFilter
import android.graphics.Paint
import android.graphics.PixelFormat
import android.graphics.PorterDuff
import android.graphics.PorterDuffXfermode
import android.graphics.Rect
import android.graphics.Xfermode
import android.graphics.drawable.Drawable
import androidx.annotation.ColorInt
import kotlin.math.min
import kotlin.math.sin

class MoonDrawable : Drawable() {
    private val mPaint = Paint().apply {
        isAntiAlias = true
    }
    private val mClearXfermode: Xfermode = PorterDuffXfermode(PorterDuff.Mode.CLEAR)

    @ColorInt
    private val mCoreColor: Int = Color.rgb(171, 202, 247)
    private var mAlpha: Float = 1f
    private var mBounds: Rect
    private var mCoreRadius = 0f
    private var mCoreCenterX = 0f
    private var mCoreCenterY = 0f
    private var mShaderRadius = 0f
    private var mShaderCenterX = 0f
    private var mShaderCenterY = 0f

    init {
        mBounds = bounds
        ensurePosition(mBounds)
    }

    private fun ensurePosition(bounds: Rect) {
        val boundSize = min(bounds.width(), bounds.height()).toFloat()
        mCoreRadius = ((sin(Math.PI / 4) * boundSize / 2 + boundSize / 2) / 2 - 2).toFloat()
        mCoreCenterX = (1.0 * bounds.width() / 2 + bounds.left).toFloat()
        mCoreCenterY = (1.0 * bounds.height() / 2 + bounds.top).toFloat()
        mShaderRadius = mCoreRadius * 0.9050f
        mShaderCenterX = mCoreCenterX + mCoreRadius * 0.5914f
        mShaderCenterY = mCoreCenterY - mCoreRadius * 0.5932f
    }

    override fun onBoundsChange(bounds: Rect) {
        mBounds = bounds
        ensurePosition(bounds)
    }

    override fun draw(canvas: Canvas) {
        mPaint.alpha = (mAlpha * 255).toInt()
        val layerId = canvas.saveLayer(
            mBounds.left.toFloat(),
            mBounds.top.toFloat(),
            mBounds.right.toFloat(),
            mBounds.bottom.toFloat(),
            null
        )
        mPaint.color = mCoreColor
        canvas.drawCircle(mCoreCenterX, mCoreCenterY, mCoreRadius, mPaint)
        mPaint.xfermode = mClearXfermode
        canvas.drawCircle(mShaderCenterX, mShaderCenterY, mShaderRadius, mPaint)
        mPaint.xfermode = null
        canvas.restoreToCount(layerId)
    }

    override fun setAlpha(alpha: Int) {
        mAlpha = alpha.toFloat()
    }

    override fun setColorFilter(colorFilter: ColorFilter?) {
        mPaint.setColorFilter(colorFilter)
    }

    @Deprecated(
        "Deprecated in Java",
        ReplaceWith("PixelFormat.OPAQUE", "android.graphics.PixelFormat")
    )
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
