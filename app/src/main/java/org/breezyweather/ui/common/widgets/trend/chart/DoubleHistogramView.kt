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

package org.breezyweather.ui.common.widgets.trend.chart

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.util.AttributeSet
import androidx.annotation.ColorInt
import androidx.annotation.Size
import org.breezyweather.R
import org.breezyweather.common.extensions.dpToPx
import org.breezyweather.common.extensions.getTypefaceFromTextAppearance

/**
 * Double histogram view.
 */
class DoubleHistogramView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
) : AbsChartItemView(context, attrs, defStyleAttr) {
    private val mPaint = Paint().apply {
        strokeCap = Paint.Cap.ROUND
        isAntiAlias = true
        isFilterBitmap = true
    }
    private var mHighHistogramValue: Float? = null
    private var mLowHistogramValue: Float? = null
    private var mHighHistogramValueStr: String? = null
    private var mLowHistogramValueStr: String? = null
    private var mHighestHistogramValue: Float? = null
    private var mHighHistogramY = 0
    private var mLowHistogramY = 0
    private val mMargins: Int
    private val mMarginCenter: Int
    override val marginTop: Int
        get() = mMargins
    override val marginBottom: Int
        get() = mMargins
    private val mHistogramWidth: Int
    private val mHistogramTextSize: Int
    private val mChartLineWith: Int
    private val mTextMargin: Int
    private val mLineColors = intArrayOf(Color.BLACK, Color.DKGRAY, Color.LTGRAY)
    private var mTextColor = 0
    private var mTextShadowColor = 0

    @Size(2)
    private var mHistogramAlphas: FloatArray

    init {
        setTextColors(Color.BLACK)
        mMargins = getContext().dpToPx(MARGIN_DIP).toInt()
        mMarginCenter = getContext().dpToPx(MARGIN_CENTER_DIP).toInt()
        mHistogramWidth = getContext().dpToPx(HISTOGRAM_WIDTH_DIP).toInt()
        mHistogramTextSize = getContext().dpToPx(HISTOGRAM_TEXT_SIZE_DIP).toInt()
        mChartLineWith = getContext().dpToPx(CHART_LINE_SIZE_DIP).toInt()
        mTextMargin = getContext().dpToPx(TEXT_MARGIN_DIP).toInt()
        mPaint.typeface = getContext().getTypefaceFromTextAppearance(R.style.title_text)
        mHistogramAlphas = floatArrayOf(1f, 1f)
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        computeCoordinates()
        drawTimeLine(canvas)
        if (mHighestHistogramValue != null) {
            if (mHighHistogramValue != null && mHighHistogramValue != 0f && mHighHistogramValueStr != null) {
                drawHighHistogram(canvas)
            }
            if (mLowHistogramValue != null && mLowHistogramValue != 0f && mLowHistogramValueStr != null) {
                drawLowHistogram(canvas)
            }
        }
    }

    private fun drawTimeLine(canvas: Canvas) {
        mPaint.style = Paint.Style.STROKE
        mPaint.strokeWidth = mChartLineWith.toFloat()
        mPaint.color = mLineColors[2]
        canvas.drawLine(
            measuredWidth / 2f,
            mMargins.toFloat(),
            measuredWidth / 2f,
            (measuredHeight - mMargins).toFloat(),
            mPaint
        )
    }

    private fun drawHighHistogram(canvas: Canvas) {
        require(mHighHistogramValue != null)
        require(mHighHistogramValueStr != null)
        val cx = measuredWidth / 2f
        val cy = measuredHeight / 2f - mMarginCenter / 2f

        // histogram.
        mPaint.apply {
            style = Paint.Style.FILL
            color = mLineColors[0]
            alpha = (255 * mHistogramAlphas[0]).toInt()
        }
        canvas.drawRoundRect(
            RectF(
                cx - mHistogramWidth / 2f,
                mHighHistogramY.toFloat(),
                cx + mHistogramWidth / 2f,
                cy
            ),
            mHistogramWidth / 2f,
            mHistogramWidth / 2f,
            mPaint
        )

        // text.
        mPaint.apply {
            color = mTextColor
            alpha = 255
            style = Paint.Style.FILL
            textAlign = Paint.Align.CENTER
            textSize = mHistogramTextSize.toFloat()
            setShadowLayer(2f, 0f, 1f, mTextShadowColor)
        }
        canvas.drawText(
            mHighHistogramValueStr ?: "",
            cx,
            mHighHistogramY - mPaint.fontMetrics.bottom - mTextMargin,
            mPaint
        )
        mPaint.setShadowLayer(0f, 0f, 0f, Color.TRANSPARENT)
    }

    private fun drawLowHistogram(canvas: Canvas) {
        require(mLowHistogramValue != null)
        require(mLowHistogramValueStr != null)
        val cx = measuredWidth / 2f
        val cy = measuredHeight / 2f + mMarginCenter / 2f

        // histogram.
        mPaint.apply {
            style = Paint.Style.FILL
            color = mLineColors[1]
            alpha = (255 * mHistogramAlphas[1]).toInt()
        }
        canvas.drawRoundRect(
            RectF(
                cx - mHistogramWidth / 2f,
                cy,
                cx + mHistogramWidth / 2f,
                mLowHistogramY.toFloat()
            ),
            mHistogramWidth / 2f,
            mHistogramWidth / 2f,
            mPaint
        )

        // text.
        mPaint.apply {
            color = mTextColor
            alpha = 255
            style = Paint.Style.FILL
            textAlign = Paint.Align.CENTER
            textSize = mHistogramTextSize.toFloat()
            setShadowLayer(2f, 0f, 1f, mTextShadowColor)
        }
        canvas.drawText(
            mLowHistogramValueStr ?: "",
            cx,
            mLowHistogramY - mPaint.fontMetrics.top + mTextMargin,
            mPaint
        )
        mPaint.setShadowLayer(0f, 0f, 0f, Color.TRANSPARENT)
    }

    // control.
    fun setData(
        highHistogramValues: Float?,
        lowHistogramValues: Float?,
        highHistogramValueStr: String?,
        lowHistogramValueStr: String?,
        highestHistogramValue: Float?,
    ) {
        mHighHistogramValue = highHistogramValues
        mLowHistogramValue = lowHistogramValues
        mHighHistogramValueStr = highHistogramValueStr
        mLowHistogramValueStr = lowHistogramValueStr
        mHighestHistogramValue = highestHistogramValue
        invalidate()
    }

    fun setLineColors(
        @ColorInt colorHigh: Int,
        @ColorInt colorLow: Int,
        @ColorInt colorSubLine: Int,
    ) {
        mLineColors[0] = colorHigh
        mLineColors[1] = colorLow
        mLineColors[2] = colorSubLine
        invalidate()
    }

    fun setTextColors(@ColorInt textColor: Int) {
        mTextColor = textColor
        mTextShadowColor = Color.argb((255 * 0.2).toInt(), 0, 0, 0)
        invalidate()
    }

    fun setHistogramAlphas(highAlpha: Float, lowAlpha: Float) {
        mHistogramAlphas = floatArrayOf(highAlpha, lowAlpha)
    }

    private fun computeCoordinates() {
        val canvasHeight = (measuredHeight - mMargins * 2 - mMarginCenter) / 2f
        val cy = measuredHeight / 2f
        if (mHighestHistogramValue != null) {
            if (mHighHistogramValue != null) {
                mHighHistogramY =
                    (cy - mMarginCenter / 2f - canvasHeight * mHighHistogramValue!! / mHighestHistogramValue!!).toInt()
            }
            if (mLowHistogramValue != null) {
                mLowHistogramY =
                    (cy + mMarginCenter / 2f + canvasHeight * mLowHistogramValue!! / mHighestHistogramValue!!).toInt()
            }
        }
    }

    companion object {
        private const val MARGIN_DIP = 24f
        private const val MARGIN_CENTER_DIP = 4f
        private const val HISTOGRAM_WIDTH_DIP = 8f
        private const val HISTOGRAM_TEXT_SIZE_DIP = 14f
        private const val CHART_LINE_SIZE_DIP = 1f
        private const val TEXT_MARGIN_DIP = 2f
    }
}
