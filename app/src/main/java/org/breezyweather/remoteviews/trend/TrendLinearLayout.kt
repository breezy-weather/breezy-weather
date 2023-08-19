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
import org.breezyweather.common.basic.models.options.unit.TemperatureUnit
import org.breezyweather.common.basic.models.weather.Temperature
import org.breezyweather.common.extensions.dpToPx
import org.breezyweather.common.extensions.getTypefaceFromTextAppearance

/**
 * Trend linear layout.
 */
class TrendLinearLayout @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyle: Int = 0
) : LinearLayout(context, attrs, defStyle) {
    private val mPaint = Paint().apply {
        isAntiAlias = true
        strokeCap = Paint.Cap.ROUND
    }
    private var mHistoryTemps: Array<Float> = emptyArray()
    private var mHistoryTempYs: Array<Float> = emptyArray()
    private var mHighestTemp: Float? = null
    private var mLowestTemp: Float? = null
    private var mTemperatureUnit: TemperatureUnit

    private var mKeyLineVisibility: Boolean = false

    @ColorInt
    private var mLineColor = 0

    @ColorInt
    private var mTextColor = 0
    private var TREND_ITEM_HEIGHT = 0f
    private var BOTTOM_MARGIN = 0f
    private var TREND_MARGIN_TOP = 24f
    private var TREND_MARGIN_BOTTOM = 36f
    private var CHART_LINE_SIZE = 1f
    private var TEXT_SIZE = 12f
    private var MARGIN_TEXT = 2f

    var normals = false

    init {
        setWillNotDraw(false)
        mPaint.setTypeface(getContext().getTypefaceFromTextAppearance(R.style.subtitle_text))
        mPaint.textSize = TEXT_SIZE
        mTemperatureUnit = TemperatureUnit.C
        setColor(true)
        TREND_MARGIN_TOP = getContext().dpToPx(TREND_MARGIN_TOP.toInt().toFloat())
        TREND_MARGIN_BOTTOM = getContext().dpToPx(TREND_MARGIN_BOTTOM.toInt().toFloat())
        TEXT_SIZE = getContext().dpToPx(TEXT_SIZE.toInt().toFloat())
        CHART_LINE_SIZE = getContext().dpToPx(CHART_LINE_SIZE.toInt().toFloat())
        MARGIN_TEXT = getContext().dpToPx(MARGIN_TEXT.toInt().toFloat())
    }

    public override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        if (mHistoryTemps.isEmpty()) return
        computeCoordinates()
        if (mHistoryTempYs.isEmpty()) return
        if (!mKeyLineVisibility) return

        mPaint.style = Paint.Style.STROKE
        mPaint.strokeWidth = CHART_LINE_SIZE
        mPaint.color = mLineColor
        canvas.drawLine(
            0f, mHistoryTempYs[0],
            measuredWidth.toFloat(), mHistoryTempYs[0],
            mPaint
        )
        canvas.drawLine(
            0f, mHistoryTempYs[1],
            measuredWidth.toFloat(), mHistoryTempYs[1],
            mPaint
        )

        mPaint.style = Paint.Style.FILL
        mPaint.textSize = TEXT_SIZE
        mPaint.textAlign = Paint.Align.LEFT
        mPaint.color = mTextColor
        canvas.drawText(
            Temperature.getShortTemperature(context, mHistoryTemps[0], mTemperatureUnit) ?: "",
            2 * MARGIN_TEXT,
            mHistoryTempYs[0] - mPaint.fontMetrics.bottom - MARGIN_TEXT,
            mPaint
        )
        canvas.drawText(
            Temperature.getShortTemperature(context, mHistoryTemps[1], mTemperatureUnit) ?: "",
            2 * MARGIN_TEXT,
            mHistoryTempYs[1] - mPaint.fontMetrics.top + MARGIN_TEXT,
            mPaint
        )
        mPaint.textAlign = Paint.Align.RIGHT
        canvas.drawText(
            context.getString(if (normals) R.string.temperature_normal_short else R.string.temperature_average_short),
            measuredWidth - 2 * MARGIN_TEXT,
            mHistoryTempYs[0] - mPaint.fontMetrics.bottom - MARGIN_TEXT,
            mPaint
        )
        canvas.drawText(
            context.getString(if (normals) R.string.temperature_normal_short else R.string.temperature_average_short),
            measuredWidth - 2 * MARGIN_TEXT,
            mHistoryTempYs[1] - mPaint.fontMetrics.top + MARGIN_TEXT,
            mPaint
        )
    }

    // control.
    fun setColor(lightTheme: Boolean) {
        if (lightTheme) {
            mLineColor = ColorUtils.setAlphaComponent(Color.BLACK, (255 * 0.05).toInt())
            mTextColor = ContextCompat.getColor(context, R.color.colorTextGrey2nd)
        } else {
            mLineColor = ColorUtils.setAlphaComponent(Color.WHITE, (255 * 0.1).toInt())
            mTextColor = ContextCompat.getColor(context, R.color.colorTextGrey)
        }
    }

    fun setData(
        historyTemps: Array<Float>, highestTemp: Float, lowestTemp: Float, unit: TemperatureUnit, daily: Boolean
    ) {
        mHistoryTemps = historyTemps
        mHighestTemp = highestTemp
        mLowestTemp = lowestTemp
        mTemperatureUnit = unit
        if (daily) {
            TREND_ITEM_HEIGHT = context.dpToPx(WidgetItemView.TREND_VIEW_HEIGHT_DIP_2X.toFloat())
            BOTTOM_MARGIN = context.dpToPx(
                (WidgetItemView.ICON_SIZE_DIP
                        + WidgetItemView.ICON_MARGIN_DIP
                        + WidgetItemView.MARGIN_VERTICAL_DIP).toFloat()
            )
        } else {
            TREND_ITEM_HEIGHT = context.dpToPx(WidgetItemView.TREND_VIEW_HEIGHT_DIP_1X.toFloat())
            BOTTOM_MARGIN = context.dpToPx(WidgetItemView.MARGIN_VERTICAL_DIP.toFloat())
        }
        invalidate()
    }

    private fun computeCoordinates() {
        mHistoryTempYs = if (mHighestTemp != null && mLowestTemp != null) {
            arrayOf(
                computeSingleCoordinate(mHistoryTemps[0], mHighestTemp!!, mLowestTemp!!),
                computeSingleCoordinate(mHistoryTemps[1], mHighestTemp!!, mLowestTemp!!)
            )
        } else emptyArray()
    }

    private fun computeSingleCoordinate(value: Float, max: Float, min: Float): Float {
        val canvasHeight = TREND_ITEM_HEIGHT - TREND_MARGIN_TOP - TREND_MARGIN_BOTTOM
        return (measuredHeight - BOTTOM_MARGIN - TREND_MARGIN_BOTTOM - canvasHeight * (value - min) / (max - min))
    }

    fun setKeyLineVisibility(visibility: Boolean) {
        mKeyLineVisibility = visibility
        invalidate()
    }
}
