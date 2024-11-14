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

package org.breezyweather.common.ui.widgets.trend.chart

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RectF
import android.graphics.Shader
import android.util.AttributeSet
import androidx.annotation.ColorInt
import androidx.annotation.FloatRange
import androidx.annotation.Size
import androidx.core.graphics.ColorUtils
import org.breezyweather.R
import org.breezyweather.common.extensions.dpToPx
import org.breezyweather.common.extensions.getTypefaceFromTextAppearance
import org.breezyweather.common.ui.widgets.DayNightShaderWrapper

/**
 * Polyline and histogram view.
 */
class PolylineAndHistogramView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
) : AbsChartItemView(context, attrs, defStyleAttr) {
    private val mPaint = Paint().apply {
        strokeCap = Paint.Cap.ROUND
        isAntiAlias = true
        isFilterBitmap = true
    }
    private val mPath = Path()
    private val mShaderWrapper: DayNightShaderWrapper

    @Size(3)
    private var mHighPolylineValues: Array<Float?>? = arrayOfNulls(3)

    @Size(3)
    private var mLowPolylineValues: Array<Float?>? = arrayOfNulls(3)
    private var mHighPolylineValueStr: String? = null
    private var mLowPolylineValueStr: String? = null
    private var mHighestPolylineValue: Float? = null
    private var mLowestPolylineValue: Float? = null
    private var mHistogramValue: Float? = null
    private var mHistogramValueStr: String? = null
    private var mHighestHistogramValue: Float? = null
    private var mLowestHistogramValue: Float? = null
    private val mHighPolylineY = IntArray(3)
    private val mLowPolylineY = IntArray(3)
    private var mHistogramY = 0
    override val marginTop: Int
    override val marginBottom: Int
    private val mPolylineWidth: Int
    private val mPolylineTextSize: Int
    private val mHistogramWidth: Int
    private val mHistogramTextSize: Int
    private val mChartLineWith: Int
    private val mTextMargin: Int
    private val mLineColors: IntArray = intArrayOf(Color.BLACK, Color.DKGRAY, Color.LTGRAY)
    private val mShadowColors: IntArray = intArrayOf(Color.BLACK, Color.WHITE)
    private var mHighTextColor = 0
    private var mLowTextColor = 0
    private var mTextShadowColor = 0
    private var mHistogramTextColor = 0
    private var mHistogramAlpha = 0f

    init {
        setTextColors(Color.BLACK, Color.DKGRAY, Color.GRAY)
        setHistogramAlpha(0.33f)
        marginTop = getContext().dpToPx(MARGIN_TOP_DIP).toInt()
        marginBottom = getContext().dpToPx(MARGIN_BOTTOM_DIP).toInt()
        mPolylineTextSize = getContext().dpToPx(POLYLINE_TEXT_SIZE_DIP).toInt()
        mHistogramTextSize = getContext().dpToPx(HISTOGRAM_TEXT_SIZE_DIP).toInt()
        mPolylineWidth = getContext().dpToPx(POLYLINE_SIZE_DIP).toInt()
        mHistogramWidth = getContext().dpToPx(HISTOGRAM_WIDTH_DIP).toInt()
        mChartLineWith = getContext().dpToPx(CHART_LINE_SIZE_DIP).toInt()
        mTextMargin = getContext().dpToPx(TEXT_MARGIN_DIP).toInt()
        mPaint.typeface = getContext().getTypefaceFromTextAppearance(R.style.title_text)
        mShaderWrapper = DayNightShaderWrapper(measuredWidth, measuredHeight)
        setShadowColors(Color.BLACK, Color.GRAY, true)
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        ensureShader(mShaderWrapper.isLightTheme)
        computeCoordinates()
        drawTimeLine(canvas)
        if (mHistogramValue != null &&
            (mHistogramValue != 0f || mHighestPolylineValue == null && mLowestPolylineValue == null) &&
            mHistogramValueStr != null &&
            mHighestHistogramValue != null &&
            mLowestHistogramValue != null
        ) {
            drawHistogram(canvas)
        }
        if (mHighestPolylineValue != null && mLowestPolylineValue != null) {
            if (mHighPolylineValues != null && mHighPolylineValueStr != null) {
                drawHighPolyLine(canvas)
            }
            if (mLowPolylineValues != null && mLowPolylineValueStr != null) {
                drawLowPolyline(canvas)
            }
        }
    }

    private fun drawTimeLine(canvas: Canvas) {
        mPaint.apply {
            style = Paint.Style.STROKE
            strokeWidth = mChartLineWith.toFloat()
            color = mLineColors[2]
        }
        canvas.drawLine(
            measuredWidth / 2f,
            marginTop.toFloat(),
            measuredWidth / 2f,
            (measuredHeight - marginBottom).toFloat(),
            mPaint
        )
    }

    private fun drawHighPolyLine(canvas: Canvas) {
        assert(mHighPolylineValues != null)
        assert(mHighPolylineValueStr != null)
        if (mHighPolylineValues!![0] != null && mHighPolylineValues!![2] != null) {
            // shadow.
            mPaint.apply {
                color = Color.BLACK
                shader = mShaderWrapper.shader
                style = Paint.Style.FILL
            }
            mPath.apply {
                reset()
                moveTo(getRTLCompactX(0f), mHighPolylineY[0].toFloat())
                lineTo(
                    getRTLCompactX((measuredWidth / 2.0).toFloat()),
                    mHighPolylineY[1].toFloat()
                )
                lineTo(getRTLCompactX(measuredWidth.toFloat()), mHighPolylineY[2].toFloat())
                lineTo(
                    getRTLCompactX(measuredWidth.toFloat()),
                    (measuredHeight - marginBottom).toFloat()
                )
                lineTo(getRTLCompactX(0f), (measuredHeight - marginBottom).toFloat())
                close()
            }
            canvas.drawPath(mPath, mPaint)

            // line.
            mPaint.apply {
                shader = null
                style = Paint.Style.STROKE
                strokeWidth = mPolylineWidth.toFloat()
                color = mLineColors[0]
            }
            mPath.apply {
                reset()
                moveTo(getRTLCompactX(0f), mHighPolylineY[0].toFloat())
                lineTo(
                    getRTLCompactX((measuredWidth / 2.0).toFloat()),
                    mHighPolylineY[1].toFloat()
                )
                lineTo(getRTLCompactX(measuredWidth.toFloat()), mHighPolylineY[2].toFloat())
            }
            canvas.drawPath(mPath, mPaint)
        } else if (mHighPolylineValues!![0] == null) {
            // shadow.
            mPaint.apply {
                color = Color.BLACK
                shader = mShaderWrapper.shader
                style = Paint.Style.FILL
            }
            mPath.apply {
                reset()
                moveTo(
                    getRTLCompactX((measuredWidth / 2.0).toFloat()),
                    mHighPolylineY[1].toFloat()
                )
                lineTo(getRTLCompactX(measuredWidth.toFloat()), mHighPolylineY[2].toFloat())
                lineTo(
                    getRTLCompactX(measuredWidth.toFloat()),
                    (measuredHeight - marginBottom).toFloat()
                )
                lineTo(
                    getRTLCompactX((measuredWidth / 2.0).toFloat()),
                    (measuredHeight - marginBottom).toFloat()
                )
                close()
            }
            canvas.drawPath(mPath, mPaint)

            // line.
            mPaint.apply {
                shader = null
                style = Paint.Style.STROKE
                strokeWidth = mPolylineWidth.toFloat()
                color = mLineColors[0]
            }
            mPath.apply {
                reset()
                moveTo(
                    getRTLCompactX((measuredWidth / 2.0).toFloat()),
                    mHighPolylineY[1].toFloat()
                )
                lineTo(getRTLCompactX(measuredWidth.toFloat()), mHighPolylineY[2].toFloat())
            }
            canvas.drawPath(mPath, mPaint)
        } else {
            // shadow.
            mPaint.apply {
                color = Color.BLACK
                shader = mShaderWrapper.shader
                style = Paint.Style.FILL
            }
            mPath.apply {
                reset()
                moveTo(getRTLCompactX(0f), mHighPolylineY[0].toFloat())
                lineTo(
                    getRTLCompactX((measuredWidth / 2.0).toFloat()),
                    mHighPolylineY[1].toFloat()
                )
                lineTo(
                    getRTLCompactX((measuredWidth / 2.0).toFloat()),
                    (measuredHeight - marginBottom).toFloat()
                )
                lineTo(getRTLCompactX(0f), (measuredHeight - marginBottom).toFloat())
                close()
            }
            canvas.drawPath(mPath, mPaint)

            // line.
            mPaint.apply {
                shader = null
                style = Paint.Style.STROKE
                strokeWidth = mPolylineWidth.toFloat()
                color = mLineColors[0]
            }
            mPath.apply {
                reset()
                moveTo(getRTLCompactX(0f), mHighPolylineY[0].toFloat())
                lineTo(
                    getRTLCompactX((measuredWidth / 2.0).toFloat()),
                    mHighPolylineY[1].toFloat()
                )
            }
            canvas.drawPath(mPath, mPaint)
        }

        // text.
        mPaint.apply {
            color = mHighTextColor
            style = Paint.Style.FILL
            textAlign = Paint.Align.CENTER
            textSize = mPolylineTextSize.toFloat()
            setShadowLayer(2f, 0f, 1f, mTextShadowColor)
        }
        canvas.drawText(
            mHighPolylineValueStr ?: "",
            getRTLCompactX((measuredWidth / 2.0).toFloat()),
            mHighPolylineY[1] - mPaint.fontMetrics.bottom - mTextMargin,
            mPaint
        )
        mPaint.setShadowLayer(0f, 0f, 0f, Color.TRANSPARENT)
    }

    private fun drawLowPolyline(canvas: Canvas) {
        assert(mLowPolylineValues != null)
        assert(mLowPolylineValueStr != null)
        if (mLowPolylineValues!![0] != null && mLowPolylineValues!![2] != null) {
            mPaint.apply {
                shader = null
                style = Paint.Style.STROKE
                mPaint.strokeWidth = mPolylineWidth.toFloat()
                mPaint.color = mLineColors[1]
            }
            mPath.apply {
                reset()
                moveTo(getRTLCompactX(0f), mLowPolylineY[0].toFloat())
                lineTo(
                    getRTLCompactX((measuredWidth / 2.0).toFloat()),
                    mLowPolylineY[1].toFloat()
                )
                lineTo(getRTLCompactX(measuredWidth.toFloat()), mLowPolylineY[2].toFloat())
            }
            canvas.drawPath(mPath, mPaint)
        } else if (mLowPolylineValues!![0] == null) {
            mPaint.apply {
                shader = null
                style = Paint.Style.STROKE
                strokeWidth = mPolylineWidth.toFloat()
                color = mLineColors[1]
            }
            mPath.apply {
                reset()
                moveTo(
                    getRTLCompactX((measuredWidth / 2.0).toFloat()),
                    mLowPolylineY[1].toFloat()
                )
                lineTo(getRTLCompactX(measuredWidth.toFloat()), mLowPolylineY[2].toFloat())
            }
            canvas.drawPath(mPath, mPaint)
        } else {
            mPaint.apply {
                shader = null
                style = Paint.Style.STROKE
                strokeWidth = mPolylineWidth.toFloat()
                color = mLineColors[1]
            }
            mPath.apply {
                reset()
                moveTo(getRTLCompactX(0f), mLowPolylineY[0].toFloat())
                lineTo(
                    getRTLCompactX((measuredWidth / 2.0).toFloat()),
                    mLowPolylineY[1].toFloat()
                )
            }
            canvas.drawPath(mPath, mPaint)
        }

        // text.
        mPaint.apply {
            color = mLowTextColor
            style = Paint.Style.FILL
            textAlign = Paint.Align.CENTER
            textSize = mPolylineTextSize.toFloat()
            setShadowLayer(2f, 0f, 1f, mTextShadowColor)
        }
        canvas.drawText(
            mLowPolylineValueStr ?: "",
            getRTLCompactX((measuredWidth / 2.0).toFloat()),
            mLowPolylineY[1] - mPaint.fontMetrics.top + mTextMargin,
            mPaint
        )
        mPaint.setShadowLayer(0f, 0f, 0f, Color.TRANSPARENT)
    }

    private fun drawHistogram(canvas: Canvas) {
        assert(mHistogramValueStr != null)
        mPaint.apply {
            color = mLineColors[1]
            alpha = (255 * mHistogramAlpha).toInt()
            style = Paint.Style.FILL
        }
        canvas.drawRoundRect(
            RectF(
                (measuredWidth / 2.0 - mHistogramWidth).toFloat(),
                mHistogramY.toFloat(),
                (measuredWidth / 2.0 + mHistogramWidth).toFloat(),
                (measuredHeight - marginBottom).toFloat()
            ),
            mHistogramWidth.toFloat(),
            mHistogramWidth.toFloat(),
            mPaint
        )
        mPaint.apply {
            color = mHistogramTextColor
            alpha = 255
            textAlign = Paint.Align.CENTER
            textSize = mHistogramTextSize.toFloat()
        }
        canvas.drawText(
            mHistogramValueStr ?: "",
            (measuredWidth / 2.0).toFloat(),
            (
                (measuredHeight - marginBottom - mPaint.fontMetrics.top) + 2.0 * mTextMargin + mPolylineTextSize
                ).toFloat(),
            mPaint
        )
        mPaint.alpha = 255
    }

    // control.
    fun setData(
        @Size(3) highPolylineValues: Array<Float?>?,
        @Size(3) lowPolylineValues: Array<Float?>?,
        highPolylineValueStr: String?,
        lowPolylineValueStr: String?,
        highestPolylineValue: Float?,
        lowestPolylineValue: Float?,
        histogramValue: Float?,
        histogramValueStr: String?,
        highestHistogramValue: Float?,
        lowestHistogramValue: Float?,
    ) {
        mHighPolylineValues = highPolylineValues
        mLowPolylineValues = lowPolylineValues
        mHighPolylineValueStr = highPolylineValueStr
        mLowPolylineValueStr = lowPolylineValueStr
        mHighestPolylineValue = highestPolylineValue
        mLowestPolylineValue = lowestPolylineValue
        mHistogramValue = histogramValue
        mHistogramValueStr = histogramValueStr
        mHighestHistogramValue = highestHistogramValue
        mLowestHistogramValue = lowestHistogramValue
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

    fun setShadowColors(
        @ColorInt colorHigh: Int,
        @ColorInt colorLow: Int,
        lightTheme: Boolean,
    ) {
        mShadowColors[0] = if (lightTheme) {
            ColorUtils.setAlphaComponent(colorHigh, (255 * SHADOW_ALPHA_FACTOR_LIGHT).toInt())
        } else {
            ColorUtils.setAlphaComponent(colorLow, (255 * SHADOW_ALPHA_FACTOR_DARK).toInt())
        }
        mShadowColors[1] = Color.TRANSPARENT
        ensureShader(lightTheme)
        invalidate()
    }

    fun setTextColors(
        @ColorInt highTextColor: Int,
        @ColorInt lowTextColor: Int,
        @ColorInt histogramTextColor: Int,
    ) {
        mHighTextColor = highTextColor
        mLowTextColor = lowTextColor
        mTextShadowColor = Color.argb((255 * 0.2).toInt(), 0, 0, 0)
        mHistogramTextColor = histogramTextColor
        invalidate()
    }

    fun setHistogramAlpha(
        @FloatRange(from = 0.0, to = 1.0) histogramAlpha: Float,
    ) {
        mHistogramAlpha = histogramAlpha
        invalidate()
    }

    private fun ensureShader(lightTheme: Boolean) {
        if (mShaderWrapper.isDifferent(measuredWidth, measuredHeight, lightTheme, mShadowColors)) {
            mShaderWrapper.setShader(
                LinearGradient(
                    0f,
                    marginTop.toFloat(),
                    0f,
                    (measuredHeight - marginBottom).toFloat(),
                    mShadowColors[0],
                    mShadowColors[1],
                    Shader.TileMode.CLAMP
                ),
                measuredWidth,
                measuredHeight,
                lightTheme,
                mShadowColors
            )
        }
    }

    private fun computeCoordinates() {
        val canvasHeight = (measuredHeight - marginTop - marginBottom).toFloat()
        if (mHighestPolylineValue != null && mLowestPolylineValue != null) {
            mHighPolylineValues?.let {
                for (i in it.indices) {
                    if (it[i] == null) {
                        mHighPolylineY[i] = 0
                    } else {
                        mHighPolylineY[i] = computeSingleCoordinate(
                            canvasHeight,
                            it[i]!!,
                            mHighestPolylineValue!!,
                            mLowestPolylineValue!!
                        )
                    }
                }
            }
            mLowPolylineValues?.let {
                for (i in it.indices) {
                    if (it[i] == null) {
                        mLowPolylineY[i] = 0
                    } else {
                        mLowPolylineY[i] = computeSingleCoordinate(
                            canvasHeight,
                            it[i]!!,
                            mHighestPolylineValue!!,
                            mLowestPolylineValue!!
                        )
                    }
                }
            }
        }
        if (mHistogramValue != null && mHighestHistogramValue != null && mLowestHistogramValue != null) {
            mHistogramY = computeSingleCoordinate(
                canvasHeight,
                mHistogramValue!!,
                mHighestHistogramValue!!,
                mLowestHistogramValue!!
            )
        }
    }

    private fun computeSingleCoordinate(
        canvasHeight: Float,
        value: Float,
        max: Float,
        min: Float,
    ): Int {
        return ((measuredHeight - marginBottom - (canvasHeight * (value - min) / (max - min)))).toInt()
    }

    private fun getRTLCompactX(x: Float): Float {
        return if (layoutDirection == LAYOUT_DIRECTION_RTL) (measuredWidth - x) else x
    }

    companion object {
        private const val MARGIN_TOP_DIP = 24f
        private const val MARGIN_BOTTOM_DIP = 36f
        private const val POLYLINE_SIZE_DIP = 5f
        private const val POLYLINE_TEXT_SIZE_DIP = 14f
        private const val HISTOGRAM_WIDTH_DIP = 4.5f
        private const val HISTOGRAM_TEXT_SIZE_DIP = 12f
        private const val CHART_LINE_SIZE_DIP = 1f
        private const val TEXT_MARGIN_DIP = 2f
        private const val SHADOW_ALPHA_FACTOR_LIGHT = 0.15f
        private const val SHADOW_ALPHA_FACTOR_DARK = 0.3f
    }
}
