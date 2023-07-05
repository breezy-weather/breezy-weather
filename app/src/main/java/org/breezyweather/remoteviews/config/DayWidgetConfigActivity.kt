package org.breezyweather.remoteviews.config

import android.os.Build
import android.view.View
import android.widget.RemoteViews
import dagger.hilt.android.AndroidEntryPoint
import org.breezyweather.R
import org.breezyweather.remoteviews.presenters.DayWidgetIMP

/**
 * Day widget config activity.
 */
@AndroidEntryPoint
class DayWidgetConfigActivity : AbstractWidgetConfigActivity() {
    override fun initData() {
        super.initData()
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
            val widgetStyles = resources.getStringArray(R.array.widget_styles)
            val widgetStyleValues = resources.getStringArray(R.array.widget_style_values)
            viewTypeValueNow = "rectangle"
            viewTypes = arrayOf(
                widgetStyles[0],
                widgetStyles[1],
                widgetStyles[2],
                widgetStyles[3],
                widgetStyles[4],
                widgetStyles[5],
                widgetStyles[6],
                widgetStyles[7],
                widgetStyles[9]
            )
            viewTypeValues = arrayOf(
                widgetStyleValues[0],
                widgetStyleValues[1],
                widgetStyleValues[2],
                widgetStyleValues[3],
                widgetStyleValues[4],
                widgetStyleValues[5],
                widgetStyleValues[6],
                widgetStyleValues[7],
                widgetStyleValues[9]
            )
        }
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
            return DayWidgetIMP.getRemoteViews(
                this, locationNow,
                viewTypeValueNow, cardStyleValueNow, cardAlpha, textColorValueNow, textSize,
                hideSubtitle, subtitleDataValueNow
            )
        }

    override val configStoreName: String
        get() {
            return getString(R.string.sp_widget_day_setting)
        }
}