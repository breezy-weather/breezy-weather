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

package org.breezyweather.ui.common.widgets.trend.item

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.view.View
import androidx.annotation.ColorInt
import androidx.annotation.IntDef
import org.breezyweather.R
import org.breezyweather.common.extensions.dpToPx
import org.breezyweather.common.extensions.getTypefaceFromTextAppearance
import org.breezyweather.ui.common.widgets.trend.TrendRecyclerView
import org.breezyweather.ui.common.widgets.trend.chart.AbsChartItemView

/**
 * Hourly trend item view.
 */
class HourlyTrendItemView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
    defStyleRes: Int = 0,
) : AbsTrendItemView(context, attrs, defStyleAttr, defStyleRes) {
    private var mChartItem: AbsChartItemView? = null
    private val mHourTextPaint = Paint().apply {
        isAntiAlias = true
        textAlign = Paint.Align.CENTER
    }
    private val mDateTextPaint = Paint().apply {
        isAntiAlias = true
        textAlign = Paint.Align.CENTER
    }
    private var mHourText: String? = null
    private var mDayText: String? = null

    @IntDef(View.INVISIBLE, View.GONE)
    internal annotation class IconVisibility

    @IconVisibility
    private var mMissingIconVisibility: Int = View.GONE
    private var mIconDrawable: Drawable? = null

    @ColorInt
    private var mContentColor = 0

    @ColorInt
    private var mSubTitleColor = 0
    private var mDayTextBaseLine = 0f
    private var mHourTextBaseLine = 0f
    private var mIconLeft = 0f
    private var mIconTop = 0f
    private var mTrendViewTop = 0f
    private val mIconSize: Int
    override var chartTop: Int = 0
        private set
    override var chartBottom: Int = 0
        private set

    init {
        setWillNotDraw(false)
        mHourTextPaint.apply {
            typeface = getContext().getTypefaceFromTextAppearance(R.style.title_text)
            textSize = getContext().resources.getDimensionPixelSize(R.dimen.title_text_size).toFloat()
        }
        mDateTextPaint.apply {
            typeface = getContext().getTypefaceFromTextAppearance(R.style.content_text)
            textSize = getContext().resources.getDimensionPixelSize(R.dimen.content_text_size).toFloat()
        }
        setTextColor(Color.BLACK, Color.GRAY)
        mIconSize = getContext().dpToPx(ICON_SIZE_DIP.toFloat()).toInt()
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val width = MeasureSpec.getSize(widthMeasureSpec)
        val height = MeasureSpec.getSize(heightMeasureSpec)
        var y = 0f
        val textMargin = context.dpToPx(TEXT_MARGIN_DIP.toFloat())
        val iconMargin = context.dpToPx(ICON_MARGIN_DIP.toFloat())

        // hour text.
        var fontMetrics = mHourTextPaint.fontMetrics
        y += textMargin
        mHourTextBaseLine = y - fontMetrics.top
        y += fontMetrics.bottom - fontMetrics.top
        y += textMargin

        // day text.
        fontMetrics = mDateTextPaint.fontMetrics
        y += textMargin
        mDayTextBaseLine = y - fontMetrics.top
        y += fontMetrics.bottom - fontMetrics.top
        y += textMargin

        // hourly icon.
        if (mIconDrawable != null || mMissingIconVisibility == View.INVISIBLE) {
            y += iconMargin
            mIconLeft = (width - mIconSize) / 2f
            mIconTop = y
            y += mIconSize.toFloat()
            y += iconMargin
        }

        // margin bottom.
        val marginBottom = context.dpToPx(TrendRecyclerView.ITEM_MARGIN_BOTTOM_DIP.toFloat())

        // chartItem item view.
        mChartItem?.measure(
            MeasureSpec.makeMeasureSpec(
                width,
                MeasureSpec.EXACTLY
            ),
            MeasureSpec.makeMeasureSpec(
                (height - marginBottom - y).toInt(),
                MeasureSpec.EXACTLY
            )
        )

        mTrendViewTop = y
        chartTop = (mTrendViewTop + mChartItem!!.marginTop).toInt()
        chartBottom = (mTrendViewTop + mChartItem!!.measuredHeight - mChartItem!!.marginBottom).toInt()
        setMeasuredDimension(width, height)
    }

    override fun onLayout(changed: Boolean, l: Int, t: Int, r: Int, b: Int) {
        mChartItem?.layout(
            0,
            mTrendViewTop.toInt(),
            mChartItem!!.measuredWidth,
            mTrendViewTop.toInt() + mChartItem!!.measuredHeight
        )
    }

    override fun onDraw(canvas: Canvas) {
        // hour text.
        mHourText?.let {
            mHourTextPaint.color = mContentColor
            canvas.drawText(it, measuredWidth / 2f, mHourTextBaseLine, mHourTextPaint)
        }

        // day text.
        mDayText?.let {
            mDateTextPaint.color = mSubTitleColor
            canvas.drawText(it, measuredWidth / 2f, mDayTextBaseLine, mDateTextPaint)
        }

        // day icon.
        mIconDrawable?.let {
            val restoreCount = canvas.save()
            canvas.translate(mIconLeft, mIconTop)
            it.draw(canvas)
            canvas.restoreToCount(restoreCount)
        }
    }

    fun setDayText(dayText: String?) {
        mDayText = dayText
        invalidate()
    }

    fun setHourText(hourText: String?) {
        mHourText = hourText
        invalidate()
    }

    fun setTextColor(@ColorInt contentColor: Int, @ColorInt subTitleColor: Int) {
        mContentColor = contentColor
        mSubTitleColor = subTitleColor
        invalidate()
    }

    fun setIconDrawable(d: Drawable?, @IconVisibility missingIconVisibility: Int) {
        val nullDrawable = mIconDrawable == null
        mIconDrawable = d
        mMissingIconVisibility = missingIconVisibility
        if (d != null) {
            d.setVisible(true, true)
            d.callback = this
            d.setBounds(0, 0, mIconSize, mIconSize)
        }
        if (nullDrawable != (d == null)) {
            requestLayout()
        } else {
            invalidate()
        }
    }

    override var chartItemView: AbsChartItemView?
        get() = mChartItem
        set(t) {
            mChartItem = t
            removeAllViews()
            addView(mChartItem)
            requestLayout()
        }

    companion object {
        private const val ICON_SIZE_DIP = 32
        private const val TEXT_MARGIN_DIP = 2
        private const val ICON_MARGIN_DIP = 8
    }
}
