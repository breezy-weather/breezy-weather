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
import android.graphics.Paint
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.view.ViewGroup
import androidx.annotation.ColorInt
import androidx.core.content.ContextCompat
import androidx.core.graphics.ColorUtils
import org.breezyweather.R
import org.breezyweather.common.extensions.dpToPx
import org.breezyweather.common.extensions.getTypefaceFromTextAppearance
import org.breezyweather.common.ui.widgets.trend.chart.PolylineAndHistogramView

/**
 * Widget item view.
 */
class WidgetItemView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
    defStyleRes: Int = 0,
) : ViewGroup(context, attrs, defStyleAttr, defStyleRes) {
    val trendItemView: PolylineAndHistogramView
    private val mTitleTextPaint: Paint
    private val mSubtitleTextPaint: Paint
    private var mWidth = 0f
    private var mTitleText: String? = null
    private var mSubtitleText: String? = null
    private var mTopIconDrawable: Drawable? = null
    private var mBottomIconDrawable: Drawable? = null

    @ColorInt
    private var mContentColor = 0

    @ColorInt
    private var mSubtitleColor = 0
    private var mTitleTextBaseLine = 0f
    private var mSubtitleTextBaseLine = 0f
    private var mTopIconLeft = 0f
    private var mTopIconTop = 0f
    private var mTrendViewTop = 0f
    private var mBottomIconLeft = 0f
    private var mBottomIconTop = 0f
    val iconSize: Int

    init {
        setWillNotDraw(false)
        trendItemView = PolylineAndHistogramView(getContext())
        addView(trendItemView)
        mTitleTextPaint = Paint().apply {
            isAntiAlias = true
            textAlign = Paint.Align.CENTER
            typeface = getContext().getTypefaceFromTextAppearance(R.style.title_text)
            textSize = getContext().resources.getDimensionPixelSize(R.dimen.title_text_size).toFloat()
        }
        mSubtitleTextPaint = Paint().apply {
            isAntiAlias = true
            textAlign = Paint.Align.CENTER
            typeface = getContext().getTypefaceFromTextAppearance(R.style.content_text)
            textSize = getContext().resources.getDimensionPixelSize(R.dimen.content_text_size).toFloat()
        }
        setColor(true)
        iconSize = getContext().dpToPx(ICON_SIZE_DIP.toFloat()).toInt()
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        var height = 0f
        val textMargin = context.dpToPx(TEXT_MARGIN_DIP.toFloat())
        val iconMargin = context.dpToPx(ICON_MARGIN_DIP.toFloat())

        // title text.
        if (mTitleText != null) {
            val fontMetrics = mTitleTextPaint.fontMetrics
            height += context.dpToPx(MARGIN_VERTICAL_DIP.toFloat())
            mTitleTextBaseLine = height - fontMetrics.top
            height += fontMetrics.bottom - fontMetrics.top
            height += textMargin
        }

        // subtitle text.
        if (mSubtitleText != null) {
            val fontMetrics = mSubtitleTextPaint.fontMetrics
            height += textMargin
            mSubtitleTextBaseLine = height - fontMetrics.top
            height += fontMetrics.bottom - fontMetrics.top
            height += textMargin
        }

        // top icon.
        // TODO: Shouldn’t we let some space here regardless of whether there is an icon?
        // Known issue when icon is missing, line is shifted to the top, but maybe that's not this class
        if (mTopIconDrawable != null) {
            height += iconMargin
            mTopIconLeft = (mWidth - iconSize) / 2f
            mTopIconTop = height
            height += iconSize.toFloat()
            height += iconMargin
        }

        // trend item view.
        mTrendViewTop = height
        val trendViewHeight = if (mBottomIconDrawable == null) {
            context.dpToPx(TREND_VIEW_HEIGHT_DIP_1X.toFloat()).toInt()
        } else {
            context.dpToPx(TREND_VIEW_HEIGHT_DIP_2X.toFloat()).toInt()
        }
        trendItemView.measure(
            MeasureSpec.makeMeasureSpec(mWidth.toInt(), MeasureSpec.EXACTLY),
            MeasureSpec.makeMeasureSpec(trendViewHeight, MeasureSpec.EXACTLY)
        )
        height += trendItemView.measuredHeight.toFloat()

        // bottom icon.
        if (mBottomIconDrawable != null) {
            height += iconMargin
            mBottomIconLeft = (mWidth - iconSize) / 2f
            mBottomIconTop = height
            height += iconSize.toFloat()
        }

        // margin bottom.
        height += context.dpToPx(MARGIN_VERTICAL_DIP.toFloat()).toInt().toFloat()
        setMeasuredDimension(mWidth.toInt(), height.toInt())
    }

    override fun onLayout(changed: Boolean, l: Int, t: Int, r: Int, b: Int) {
        trendItemView.layout(
            0,
            mTrendViewTop.toInt(),
            trendItemView.measuredWidth,
            (mTrendViewTop + trendItemView.measuredHeight).toInt()
        )
    }

    override fun onDraw(canvas: Canvas) {
        // week text.
        mTitleText?.let {
            mTitleTextPaint.color = mContentColor
            canvas.drawText(it, measuredWidth / 2f, mTitleTextBaseLine, mTitleTextPaint)
        }

        // date text.
        mSubtitleText?.let {
            mSubtitleTextPaint.color = mSubtitleColor
            canvas.drawText(it, measuredWidth / 2f, mSubtitleTextBaseLine, mSubtitleTextPaint)
        }
        var restoreCount: Int

        // day icon.
        mTopIconDrawable?.let {
            restoreCount = canvas.save()
            canvas.translate(mTopIconLeft, mTopIconTop)
            it.draw(canvas)
            canvas.restoreToCount(restoreCount)
        }

        // night icon.
        mBottomIconDrawable?.let {
            restoreCount = canvas.save()
            canvas.translate(mBottomIconLeft, mBottomIconTop)
            it.draw(canvas)
            canvas.restoreToCount(restoreCount)
        }
    }

    fun setColor(daytime: Boolean) {
        if (daytime) {
            mContentColor = ContextCompat.getColor(context, R.color.colorTextDark)
            mSubtitleColor = ColorUtils.setAlphaComponent(
                ContextCompat.getColor(context, R.color.colorTextDark),
                (255 * 0.7).toInt()
            )
        } else {
            mContentColor = ContextCompat.getColor(context, R.color.colorTextLight)
            mSubtitleColor = ColorUtils.setAlphaComponent(
                ContextCompat.getColor(context, R.color.colorTextLight),
                (255 * 0.7).toInt()
            )
        }
    }

    fun setSize(width: Float) {
        mWidth = width
    }

    fun setTitleText(titleText: String?) {
        mTitleText = titleText
    }

    fun setSubtitleText(subtitleText: String?) {
        mSubtitleText = subtitleText
    }

    fun setTopIconDrawable(d: Drawable?) {
        mTopIconDrawable = d
        if (d != null) {
            d.setVisible(true, true)
            d.callback = this
            d.setBounds(0, 0, iconSize, iconSize)
        }
    }

    fun setBottomIconDrawable(d: Drawable?) {
        mBottomIconDrawable = d
        if (d != null) {
            d.setVisible(true, true)
            d.callback = this
            d.setBounds(0, 0, iconSize, iconSize)
        }
    }

    companion object {
        const val ICON_SIZE_DIP = 32
        const val TREND_VIEW_HEIGHT_DIP_1X = 96
        const val TREND_VIEW_HEIGHT_DIP_2X = 108
        const val TEXT_MARGIN_DIP = 2
        const val ICON_MARGIN_DIP = 4
        const val MARGIN_VERTICAL_DIP = 8
    }
}
