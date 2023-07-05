package org.breezyweather.remoteviews.config

import android.view.View
import android.widget.RemoteViews
import dagger.hilt.android.AndroidEntryPoint
import org.breezyweather.R
import org.breezyweather.remoteviews.presenters.DayWeekWidgetIMP

/**
 * Day week widget config activity.
 */
@AndroidEntryPoint
class DayWeekWidgetConfigActivity : AbstractWidgetConfigActivity() {
    override fun initData() {
        super.initData()
        val widgetStyles = resources.getStringArray(R.array.widget_styles)
        val widgetStyleValues = resources.getStringArray(R.array.widget_style_values)
        viewTypeValueNow = "rectangle"
        viewTypes = arrayOf(
            widgetStyles[0],
            widgetStyles[1],
            widgetStyles[2]
        )
        viewTypeValues = arrayOf(
            widgetStyleValues[0],
            widgetStyleValues[1],
            widgetStyleValues[2]
        )
    }

    override fun initView() {
        super.initView()
        mViewTypeContainer?.visibility = View.VISIBLE
        mCardStyleContainer?.visibility = View.VISIBLE
        mCardAlphaContainer?.visibility = View.VISIBLE
        mHideSubtitleContainer?.visibility = View.VISIBLE
        mSubtitleDataContainer?.visibility = View.VISIBLE
        mTextColorContainer?.visibility = View.VISIBLE
        mTextSizeContainer?.visibility = View.VISIBLE
    }

    override val remoteViews: RemoteViews
        get() {
            return DayWeekWidgetIMP.getRemoteViews(
                this, locationNow,
                viewTypeValueNow, cardStyleValueNow, cardAlpha, textColorValueNow, textSize,
                hideSubtitle, subtitleDataValueNow
            )
        }

    override val configStoreName: String
        get() {
            return getString(R.string.sp_widget_day_week_setting)
        }
}