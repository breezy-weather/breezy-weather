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

    // Optional secondary bar drawn behind the main bar, and an optional small
    // rounded "pill" showing its value, drawn between the arrow icon and the speed text.
    private var mHighSecondaryHistogramValue: Float? = null
    private var mLowSecondaryHistogramValue: Float? = null
    private var mHighSecondaryHistogramValueStr: String? = null
    private var mLowSecondaryHistogramValueStr: String? = null
    private var mHighSecondaryHistogramY = 0
    private var mLowSecondaryHistogramY = 0
    private val mMarginCenter: Int
    override val marginTop: Int
        get() = getContext().dpToPx(
            if (mHighSecondaryHistogramValueStr != null) {
                MARGIN_DIP + SECONDARY_HISTOGRAM_PILL_RESERVED_SPACE_DIP
            } else {
                MARGIN_DIP
            }
        ).toInt()
    override val marginBottom: Int
        get() = getContext().dpToPx(
            if (mLowSecondaryHistogramValueStr != null) {
                MARGIN_DIP + SECONDARY_HISTOGRAM_PILL_RESERVED_SPACE_DIP
            } else {
                MARGIN_DIP
            }
        ).toInt()

    private val mHistogramWidth: Int
    private val mHistogramTextSize: Int
    private val mChartLineWith: Int
    private val mTextMargin: Int
    private val mSecondaryHistogramPillTextSize: Int
    private val mSecondaryHistogramPillPaddingHorizontal: Int
    private val mSecondaryHistogramPillPaddingVertical: Int
    private val mSecondaryHistogramPillMargin: Int

    // 0 = day/high, 1 = night/low, 2 = sub line, 3 = day/high gusts, 4 = night/low gusts.
    private val mLineColors = intArrayOf(Color.BLACK, Color.DKGRAY, Color.LTGRAY, Color.BLACK, Color.DKGRAY)
    private var mTextColor = 0
    private var mTextShadowColor = 0

    @Size(2)
    private var mHistogramAlphas: FloatArray

    init {
        setTextColors(Color.BLACK)
        mMarginCenter = getContext().dpToPx(MARGIN_CENTER_DIP).toInt()
        mHistogramWidth = getContext().dpToPx(HISTOGRAM_WIDTH_DIP).toInt()
        mHistogramTextSize = getContext().dpToPx(HISTOGRAM_TEXT_SIZE_DIP).toInt()
        mChartLineWith = getContext().dpToPx(CHART_LINE_SIZE_DIP).toInt()
        mTextMargin = getContext().dpToPx(TEXT_MARGIN_DIP).toInt()
        mSecondaryHistogramPillTextSize = getContext().dpToPx(SECONDARY_HISTOGRAM_PILL_TEXT_SIZE_DIP).toInt()
        mSecondaryHistogramPillPaddingHorizontal =
            getContext().dpToPx(SECONDARY_HISTOGRAM_PILL_PADDING_HORIZONTAL_DIP).toInt()
        mSecondaryHistogramPillPaddingVertical =
            getContext().dpToPx(SECONDARY_HISTOGRAM_PILL_PADDING_VERTICAL_DIP).toInt()
        mSecondaryHistogramPillMargin = getContext().dpToPx(SECONDARY_HISTOGRAM_PILL_MARGIN_DIP).toInt()

        mPaint.typeface = getContext().getTypefaceFromTextAppearance(R.style.title_text)
        mHistogramAlphas = floatArrayOf(1f, 1f)
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        computeCoordinates()
        drawTimeLine(canvas)
        if (mHighestHistogramValue != null) {
            if (mHighSecondaryHistogramValue != null &&
                mHighHistogramValue != null &&
                mHighSecondaryHistogramValue!! > mHighHistogramValue!! &&
                mHighSecondaryHistogramValueStr != null
            ) {
                drawHighSecondaryHistogram(canvas)
            }
            if (mLowSecondaryHistogramValue != null &&
                mLowHistogramValue != null &&
                mLowSecondaryHistogramValue!! > mLowHistogramValue!! &&
                mLowSecondaryHistogramValueStr != null
            ) {
                drawLowSecondaryHistogram(canvas)
            }
            if (mHighHistogramValue != null && mHighHistogramValue != 0f && mHighHistogramValueStr != null) {
                drawHighHistogram(canvas)
            }
            if (mLowHistogramValue != null && mLowHistogramValue != 0f && mLowHistogramValueStr != null) {
                drawLowHistogram(canvas)
            }
        }
        if (!mHighSecondaryHistogramValueStr.isNullOrEmpty()) {
            drawHighSecondaryHistogramPill(canvas)
        }
        if (!mLowSecondaryHistogramValueStr.isNullOrEmpty()) {
            drawLowSecondaryHistogramPill(canvas)
        }
    }

    private fun drawTimeLine(canvas: Canvas) {
        mPaint.style = Paint.Style.STROKE
        mPaint.strokeWidth = mChartLineWith.toFloat()
        mPaint.color = mLineColors[2]
        canvas.drawLine(
            measuredWidth / 2f,
            marginTop.toFloat(),
            measuredWidth / 2f,
            (measuredHeight - marginBottom).toFloat(),
            mPaint
        )
    }

    private fun drawHighSecondaryHistogram(canvas: Canvas) {
        val cx = measuredWidth / 2f
        val cy = measuredHeight / 2f - mMarginCenter / 2f

        mPaint.apply {
            style = Paint.Style.FILL
            color = mLineColors[3]
            alpha = (255 * SECONDARY_HISTOGRAM_ALPHA).toInt()
        }
        canvas.drawRoundRect(
            RectF(
                cx - mHistogramWidth / 2f,
                mHighSecondaryHistogramY.toFloat(),
                cx + mHistogramWidth / 2f,
                cy
            ),
            mHistogramWidth / 2f,
            mHistogramWidth / 2f,
            mPaint
        )
        mPaint.alpha = 255
    }

    private fun drawLowSecondaryHistogram(canvas: Canvas) {
        val cx = measuredWidth / 2f
        val cy = measuredHeight / 2f + mMarginCenter / 2f

        mPaint.apply {
            style = Paint.Style.FILL
            color = mLineColors[4]
            alpha = (255 * SECONDARY_HISTOGRAM_ALPHA).toInt()
        }
        canvas.drawRoundRect(
            RectF(
                cx - mHistogramWidth / 2f,
                cy,
                cx + mHistogramWidth / 2f,
                mLowSecondaryHistogramY.toFloat()
            ),
            mHistogramWidth / 2f,
            mHistogramWidth / 2f,
            mPaint
        )
        mPaint.alpha = 255
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
            marginTop - mPaint.fontMetrics.bottom - mTextMargin,
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
            (measuredHeight - marginBottom) - mPaint.fontMetrics.top + mTextMargin,
            mPaint
        )
        mPaint.setShadowLayer(0f, 0f, 0f, Color.TRANSPARENT)
    }

    private fun drawHighSecondaryHistogramPill(canvas: Canvas) {
        val text = mHighSecondaryHistogramValueStr ?: return

        mPaint.apply {
            shader = null
            style = Paint.Style.FILL
            textAlign = Paint.Align.CENTER
            textSize = mSecondaryHistogramPillTextSize.toFloat()
            setShadowLayer(0f, 0f, 0f, Color.TRANSPARENT)
        }
        val fontMetrics = mPaint.fontMetrics
        val textWidth = mPaint.measureText(text)
        val pillHeight = fontMetrics.bottom - fontMetrics.top + 2f * mSecondaryHistogramPillPaddingVertical
        val pillWidth = maxOf(pillHeight, textWidth + 2f * mSecondaryHistogramPillPaddingHorizontal)

        val centerX = measuredWidth / 2f
        val top = mSecondaryHistogramPillMargin.toFloat()
        val bottom = top + pillHeight

        mPaint.color = mLineColors[3]
        canvas.drawRoundRect(
            RectF(centerX - pillWidth / 2f, top, centerX + pillWidth / 2f, bottom),
            pillHeight / 2f,
            pillHeight / 2f,
            mPaint
        )

        mPaint.color = Color.BLACK
        canvas.drawText(
            text,
            centerX,
            bottom - mSecondaryHistogramPillPaddingVertical - fontMetrics.bottom,
            mPaint
        )
    }

    private fun drawLowSecondaryHistogramPill(canvas: Canvas) {
        val text = mLowSecondaryHistogramValueStr ?: return

        mPaint.apply {
            shader = null
            style = Paint.Style.FILL
            textAlign = Paint.Align.CENTER
            textSize = mSecondaryHistogramPillTextSize.toFloat()
            setShadowLayer(0f, 0f, 0f, Color.TRANSPARENT)
        }
        val fontMetrics = mPaint.fontMetrics
        val textWidth = mPaint.measureText(text)
        val pillHeight = fontMetrics.bottom - fontMetrics.top + 2f * mSecondaryHistogramPillPaddingVertical
        val pillWidth = maxOf(pillHeight, textWidth + 2f * mSecondaryHistogramPillPaddingHorizontal)

        val centerX = measuredWidth / 2f
        val bottom = (measuredHeight - mSecondaryHistogramPillMargin).toFloat()
        val top = bottom - pillHeight

        mPaint.color = mLineColors[4]
        canvas.drawRoundRect(
            RectF(centerX - pillWidth / 2f, top, centerX + pillWidth / 2f, bottom),
            pillHeight / 2f,
            pillHeight / 2f,
            mPaint
        )

        mPaint.color = Color.BLACK
        canvas.drawText(
            text,
            centerX,
            bottom - mSecondaryHistogramPillPaddingVertical - fontMetrics.bottom,
            mPaint
        )
    }

    // control.
    fun setData(
        highHistogramValues: Float?,
        lowHistogramValues: Float?,
        highHistogramValueStr: String?,
        lowHistogramValueStr: String?,
        highestHistogramValue: Float?,
        highSecondaryHistogramValue: Float? = null,
        lowSecondaryHistogramValue: Float? = null,
        highSecondaryHistogramValueStr: String? = null,
        lowSecondaryHistogramValueStr: String? = null,
    ) {
        mHighHistogramValue = highHistogramValues
        mLowHistogramValue = lowHistogramValues
        mHighHistogramValueStr = highHistogramValueStr
        mLowHistogramValueStr = lowHistogramValueStr
        mHighestHistogramValue = highestHistogramValue
        mHighSecondaryHistogramValue = highSecondaryHistogramValue
        mLowSecondaryHistogramValue = lowSecondaryHistogramValue
        mHighSecondaryHistogramValueStr = highSecondaryHistogramValueStr
        mLowSecondaryHistogramValueStr = lowSecondaryHistogramValueStr
        invalidate()
    }

    fun setLineColors(
        @ColorInt colorHigh: Int,
        @ColorInt colorLow: Int,
        @ColorInt colorSubLine: Int,
        @ColorInt secondaryColorHigh: Int? = null,
        @ColorInt secondaryColorLow: Int? = null,
    ) {
        mLineColors[0] = colorHigh
        mLineColors[1] = colorLow
        mLineColors[2] = colorSubLine
        mLineColors[3] = secondaryColorHigh ?: colorHigh
        mLineColors[4] = secondaryColorLow ?: colorLow
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
        val cy = measuredHeight / 2f
        val topCanvasHeight = cy - mMarginCenter / 2f - marginTop
        val bottomCanvasHeight = (measuredHeight - marginBottom) - (cy + mMarginCenter / 2f)
        if (mHighestHistogramValue != null) {
            if (mHighHistogramValue != null) {
                mHighHistogramY =
                    (cy - mMarginCenter / 2f - topCanvasHeight * mHighHistogramValue!! / mHighestHistogramValue!!)
                        .toInt()
            }
            if (mLowHistogramValue != null) {
                mLowHistogramY =
                    (cy + mMarginCenter / 2f + bottomCanvasHeight * mLowHistogramValue!! / mHighestHistogramValue!!)
                        .toInt()
            }
            if (mHighSecondaryHistogramValue != null) {
                mHighSecondaryHistogramY = (
                    cy - mMarginCenter / 2f -
                        topCanvasHeight * mHighSecondaryHistogramValue!! / mHighestHistogramValue!!
                    ).toInt()
            }
            if (mLowSecondaryHistogramValue != null) {
                mLowSecondaryHistogramY = (
                    cy + mMarginCenter / 2f +
                        bottomCanvasHeight * mLowSecondaryHistogramValue!! / mHighestHistogramValue!!
                    ).toInt()
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

        private const val SECONDARY_HISTOGRAM_PILL_TEXT_SIZE_DIP = 10f
        private const val SECONDARY_HISTOGRAM_PILL_PADDING_HORIZONTAL_DIP = 6f
        private const val SECONDARY_HISTOGRAM_PILL_PADDING_VERTICAL_DIP = 3f
        private const val SECONDARY_HISTOGRAM_PILL_MARGIN_DIP = 2f
        private const val SECONDARY_HISTOGRAM_PILL_RESERVED_SPACE_DIP =
            SECONDARY_HISTOGRAM_PILL_TEXT_SIZE_DIP + 2 * SECONDARY_HISTOGRAM_PILL_PADDING_VERTICAL_DIP +
                SECONDARY_HISTOGRAM_PILL_MARGIN_DIP + 4f
        private const val SECONDARY_HISTOGRAM_ALPHA = 0.3f
    }
}
