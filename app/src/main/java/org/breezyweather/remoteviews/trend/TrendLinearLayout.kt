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

package org.breezyweather.remoteviews.trend

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import android.widget.LinearLayout
import androidx.annotation.ColorInt
import androidx.core.content.ContextCompat
import androidx.core.graphics.ColorUtils
import org.breezyweather.R
import org.breezyweather.common.extensions.dpToPx
import org.breezyweather.common.extensions.formatMeasure
import org.breezyweather.common.extensions.getTypefaceFromTextAppearance
import org.breezyweather.unit.formatting.UnitWidth
import org.breezyweather.unit.temperature.Temperature.Companion.deciCelsius
import org.breezyweather.unit.temperature.TemperatureUnit

/**
 * Trend linear layout.
 */
class TrendLinearLayout @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyle: Int = 0,
) : LinearLayout(context, attrs, defStyle) {
    private val mPaint = Paint().apply {
        isAntiAlias = true
        strokeCap = Paint.Cap.ROUND
    }
    private var mHistoryTemps: Array<Float> = emptyArray()
    private var mHistoryTempYs: Array<Float> = emptyArray()
    private var mHighestTemp: Float? = null
    private var mLowestTemp: Float? = null
    private var temperatureUnit: TemperatureUnit = TemperatureUnit.CELSIUS

    private var mKeyLineVisibility: Boolean = false

    @ColorInt
    private var mLineColor = 0

    @ColorInt
    private var mTextColor = 0
    private var trendItemHeight = 0f
    private var bottomMargin = 0f
    private var trendMarginTop = 24f
    private var trendMarginBottom = 36f
    private var chartLineSize = 1f
    private var textSize = 12f
    private var marginText = 2f

    init {
        setWillNotDraw(false)
        mPaint.setTypeface(getContext().getTypefaceFromTextAppearance(R.style.subtitle_text))
        mPaint.textSize = textSize
        setColor(true)
        trendMarginTop = getContext().dpToPx(trendMarginTop.toInt().toFloat())
        trendMarginBottom = getContext().dpToPx(trendMarginBottom.toInt().toFloat())
        textSize = getContext().dpToPx(textSize.toInt().toFloat())
        chartLineSize = getContext().dpToPx(chartLineSize.toInt().toFloat())
        marginText = getContext().dpToPx(marginText.toInt().toFloat())
    }

    public override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        if (mHistoryTemps.isEmpty()) return
        computeCoordinates()
        if (mHistoryTempYs.isEmpty()) return
        if (!mKeyLineVisibility) return

        mPaint.style = Paint.Style.STROKE
        mPaint.strokeWidth = chartLineSize
        mPaint.color = mLineColor
        canvas.drawLine(
            0f,
            mHistoryTempYs[0],
            measuredWidth.toFloat(),
            mHistoryTempYs[0],
            mPaint
        )
        canvas.drawLine(
            0f,
            mHistoryTempYs[1],
            measuredWidth.toFloat(),
            mHistoryTempYs[1],
            mPaint
        )

        mPaint.style = Paint.Style.FILL
        mPaint.textSize = textSize
        mPaint.textAlign = Paint.Align.LEFT
        mPaint.color = mTextColor
        canvas.drawText(
            mHistoryTemps[0].toDouble().deciCelsius.formatMeasure(
                context,
                temperatureUnit,
                valueWidth = UnitWidth.NARROW,
                unitWidth = UnitWidth.NARROW
            ),
            2 * marginText,
            mHistoryTempYs[0] - mPaint.fontMetrics.bottom - marginText,
            mPaint
        )
        canvas.drawText(
            mHistoryTemps[1].toDouble().deciCelsius.formatMeasure(
                context,
                temperatureUnit,
                valueWidth = UnitWidth.NARROW,
                unitWidth = UnitWidth.NARROW
            ),
            2 * marginText,
            mHistoryTempYs[1] - mPaint.fontMetrics.top + marginText,
            mPaint
        )
        mPaint.textAlign = Paint.Align.RIGHT
        canvas.drawText(
            context.getString(R.string.temperature_normal_short),
            measuredWidth - 2 * marginText,
            mHistoryTempYs[0] - mPaint.fontMetrics.bottom - marginText,
            mPaint
        )
        canvas.drawText(
            context.getString(R.string.temperature_normal_short),
            measuredWidth - 2 * marginText,
            mHistoryTempYs[1] - mPaint.fontMetrics.top + marginText,
            mPaint
        )
    }

    fun setColor(lightTheme: Boolean) {
        if (lightTheme) {
            mLineColor = ColorUtils.setAlphaComponent(Color.BLACK, (255 * 0.075).toInt())
            mTextColor = ColorUtils.setAlphaComponent(
                ContextCompat.getColor(context, R.color.colorTextDark2nd),
                (255 * 0.65).toInt()
            )
        } else {
            mLineColor = ColorUtils.setAlphaComponent(Color.WHITE, (255 * 0.15).toInt())
            mTextColor = ColorUtils.setAlphaComponent(
                ContextCompat.getColor(context, R.color.colorTextLight2nd),
                (255 * 0.65).toInt()
            )
        }
    }

    fun setData(
        historyTemps: Array<Float>,
        highestTemp: Float,
        lowestTemp: Float,
        unit: TemperatureUnit,
        daily: Boolean,
    ) {
        mHistoryTemps = historyTemps
        mHighestTemp = highestTemp
        mLowestTemp = lowestTemp
        temperatureUnit = unit
        if (daily) {
            trendItemHeight = context.dpToPx(WidgetItemView.TREND_VIEW_HEIGHT_DIP_2X.toFloat())
            bottomMargin = context.dpToPx(
                (
                    WidgetItemView.ICON_SIZE_DIP + WidgetItemView.ICON_MARGIN_DIP + WidgetItemView.MARGIN_VERTICAL_DIP
                    ).toFloat()
            )
        } else {
            trendItemHeight = context.dpToPx(WidgetItemView.TREND_VIEW_HEIGHT_DIP_1X.toFloat())
            bottomMargin = context.dpToPx(WidgetItemView.MARGIN_VERTICAL_DIP.toFloat())
        }
        invalidate()
    }

    private fun computeCoordinates() {
        mHistoryTempYs = if (mHighestTemp != null && mLowestTemp != null) {
            arrayOf(
                computeSingleCoordinate(mHistoryTemps[0], mHighestTemp!!, mLowestTemp!!),
                computeSingleCoordinate(mHistoryTemps[1], mHighestTemp!!, mLowestTemp!!)
            )
        } else {
            emptyArray()
        }
    }

    private fun computeSingleCoordinate(value: Float, max: Float, min: Float): Float {
        val canvasHeight = trendItemHeight - trendMarginTop - trendMarginBottom
        return (measuredHeight - bottomMargin - trendMarginBottom - canvasHeight * (value - min) / (max - min))
    }

    fun setKeyLineVisibility(visibility: Boolean) {
        mKeyLineVisibility = visibility
        invalidate()
    }
}
