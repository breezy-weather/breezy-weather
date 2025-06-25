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

package org.breezyweather.ui.common.widgets.trend

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import androidx.annotation.ColorInt
import androidx.recyclerview.widget.RecyclerView
import org.breezyweather.R
import org.breezyweather.common.extensions.dpToPx
import org.breezyweather.common.extensions.getTypefaceFromTextAppearance
import org.breezyweather.ui.common.widgets.trend.item.AbsTrendItemView

/**
 * Trend recycler view.
 */
class TrendRecyclerView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyle: Int = 0,
) : RecyclerView(context, attrs, defStyle) {
    private val mPaint = Paint().apply {
        isAntiAlias = true
        strokeCap = Paint.Cap.ROUND
    }

    @ColorInt
    private var mLineColor = 0
    private var mTextColor = 0
    private var mDrawingBoundaryTop: Int
    private var mDrawingBoundaryBottom: Int
    private var mKeyLineList: List<KeyLine>? = null
    private var mKeyLineVisibility = true
    private var mHighestData: Float? = null
    private var mLowestData: Float? = null
    private val mTextSize: Int
    private val mTextMargin: Int
    private val mLineWidth: Int

    class KeyLine(
        var value: Float,
        var contentLeft: String?,
        var contentRight: String?,
        var contentPosition: ContentPosition,
    ) {
        enum class ContentPosition {
            ABOVE_LINE,
            BELOW_LINE,
        }
    }

    init {
        setWillNotDraw(false)
        mPaint.typeface = getContext().getTypefaceFromTextAppearance(R.style.subtitle_text)
        mTextSize = getContext().dpToPx(TEXT_SIZE_DIP.toFloat()).toInt()
        mTextMargin = getContext().dpToPx(TEXT_MARGIN_DIP.toFloat()).toInt()
        mLineWidth = getContext().dpToPx(LINE_WIDTH_DIP.toFloat()).toInt()
        mDrawingBoundaryTop = -1
        mDrawingBoundaryBottom = -1
        setLineColor(Color.GRAY)
        setTextColor(Color.GRAY)
        mKeyLineList = mutableListOf()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        drawKeyLines(canvas)
    }

    private fun drawKeyLines(canvas: Canvas) {
        if (!mKeyLineVisibility ||
            mKeyLineList == null ||
            mKeyLineList!!.isEmpty() ||
            mHighestData == null ||
            mLowestData == null
        ) {
            return
        }
        if (childCount > 0) {
            mDrawingBoundaryTop = (getChildAt(0) as AbsTrendItemView).chartTop
            mDrawingBoundaryBottom = (getChildAt(0) as AbsTrendItemView).chartBottom
        }
        if (mDrawingBoundaryTop < 0 || mDrawingBoundaryBottom < 0) {
            return
        }
        val dataRange = mHighestData!! - mLowestData!!
        val boundaryRange = (mDrawingBoundaryBottom - mDrawingBoundaryTop).toFloat()
        for (line in mKeyLineList!!) {
            if (line.value > mHighestData!! || line.value < mLowestData!!) {
                continue
            }
            val y = (mDrawingBoundaryBottom - (line.value - mLowestData!!) / dataRange * boundaryRange).toInt()
            mPaint.apply {
                style = Paint.Style.STROKE
                strokeWidth = mLineWidth.toFloat()
                color = mLineColor
            }
            canvas.drawLine(0f, y.toFloat(), measuredWidth.toFloat(), y.toFloat(), mPaint)
            mPaint.apply {
                style = Paint.Style.FILL
                textSize = mTextSize.toFloat()
                color = mTextColor
            }
            when (line.contentPosition) {
                TrendRecyclerView.KeyLine.ContentPosition.ABOVE_LINE -> {
                    if (!line.contentLeft.isNullOrEmpty()) {
                        mPaint.textAlign = Paint.Align.LEFT
                        canvas.drawText(
                            line.contentLeft!!,
                            (2 * mTextMargin).toFloat(),
                            y - mPaint.fontMetrics.bottom - mTextMargin,
                            mPaint
                        )
                    }
                    if (!line.contentRight.isNullOrEmpty()) {
                        mPaint.textAlign = Paint.Align.RIGHT
                        canvas.drawText(
                            line.contentRight!!,
                            (measuredWidth - 2 * mTextMargin).toFloat(),
                            y - mPaint.fontMetrics.bottom - mTextMargin,
                            mPaint
                        )
                    }
                }

                TrendRecyclerView.KeyLine.ContentPosition.BELOW_LINE -> {
                    if (!line.contentLeft.isNullOrEmpty()) {
                        mPaint.textAlign = Paint.Align.LEFT
                        canvas.drawText(
                            line.contentLeft!!,
                            (2 * mTextMargin).toFloat(),
                            y - mPaint.fontMetrics.top + mTextMargin,
                            mPaint
                        )
                    }
                    if (!line.contentRight.isNullOrEmpty()) {
                        mPaint.textAlign = Paint.Align.RIGHT
                        canvas.drawText(
                            line.contentRight!!,
                            (measuredWidth - 2 * mTextMargin).toFloat(),
                            y - mPaint.fontMetrics.top + mTextMargin,
                            mPaint
                        )
                    }
                }
            }
        }
    }

    // control.
    fun setData(keyLineList: List<KeyLine>?, highestData: Float, lowestData: Float) {
        mKeyLineList = keyLineList
        mHighestData = highestData
        mLowestData = lowestData
        invalidate()
    }

    fun setKeyLineVisibility(visibility: Boolean) {
        mKeyLineVisibility = visibility
        invalidate()
    }

    fun setLineColor(@ColorInt lineColor: Int) {
        mLineColor = lineColor
        invalidate()
    }

    fun setTextColor(@ColorInt textColor: Int) {
        mTextColor = textColor
        invalidate()
    }

    companion object {
        private const val LINE_WIDTH_DIP = 1
        private const val TEXT_SIZE_DIP = 12
        private const val TEXT_MARGIN_DIP = 2
        const val ITEM_MARGIN_BOTTOM_DIP = 16
    }
}
