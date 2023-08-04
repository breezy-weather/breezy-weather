package org.breezyweather.common.ui.images.pixel

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.ColorFilter
import android.graphics.Paint
import android.graphics.PixelFormat
import android.graphics.Rect
import android.graphics.drawable.Drawable
import androidx.annotation.ColorInt
import kotlin.math.min
import kotlin.math.sin

class PixelSunDrawable : Drawable() {
    private val mPaint = Paint().apply {
        isAntiAlias = true
    }

    @ColorInt
    private val mColor: Int = Color.rgb(255, 215, 5)
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
        mRadius = ((sin(Math.PI / 4) * boundSize / 2 + boundSize / 2) / 2 - 2).toFloat()
        mCX = (1.0 * bounds.width() / 2 + bounds.left).toFloat()
        mCY = (1.0 * bounds.height() / 2 + bounds.top).toFloat()
    }

    override fun onBoundsChange(bounds: Rect) {
        mBounds = bounds
        ensurePosition(bounds)
    }

    override fun draw(canvas: Canvas) {
        mPaint.alpha = (mAlpha * 255).toInt()
        mPaint.color = mColor
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
