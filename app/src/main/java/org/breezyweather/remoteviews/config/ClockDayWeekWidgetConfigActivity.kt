package org.breezyweather.remoteviews.config

import android.view.View
import android.widget.RemoteViews
import org.breezyweather.R
import org.breezyweather.remoteviews.presenters.ClockDayWeekWidgetIMP

/**
 * Clock day week widget config activity.
 */
class ClockDayWeekWidgetConfigActivity : AbstractWidgetConfigActivity() {
    override fun initData() {
        super.initData()
        val clockFonts = resources.getStringArray(R.array.widget_clock_fonts)
        val clockFontValues = resources.getStringArray(R.array.widget_clock_font_values)
        clockFontValueNow = "light"
        this.clockFonts = arrayOf(clockFonts[0], clockFonts[1], clockFonts[2])
        this.clockFontValues = arrayOf(clockFontValues[0], clockFontValues[1], clockFontValues[2])
    }

    override fun initView() {
        super.initView()
        mCardStyleContainer?.visibility = View.VISIBLE
        mCardAlphaContainer?.visibility = View.VISIBLE
        mTextColorContainer?.visibility = View.VISIBLE
        mTextSizeContainer?.visibility = View.VISIBLE
        mClockFontContainer?.visibility = View.VISIBLE
        mHideLunarContainer?.visibility = isHideLunarContainerVisible
    }

    override val remoteViews: RemoteViews
        get() {
            return ClockDayWeekWidgetIMP.getRemoteViews(
                this, locationNow,
                cardStyleValueNow, cardAlpha, textColorValueNow, textSize, clockFontValueNow, hideLunar
            )
        }

    override val configStoreName: String
        get() {
            return getString(R.string.sp_widget_clock_day_week_setting)
        }
}