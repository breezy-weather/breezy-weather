package org.breezyweather.remoteviews.config

import android.view.View
import android.widget.RemoteViews
import dagger.hilt.android.AndroidEntryPoint
import org.breezyweather.R
import org.breezyweather.remoteviews.presenters.DailyTrendWidgetIMP

/**
 * Daily trend widget config activity.
 */
@AndroidEntryPoint
class DailyTrendWidgetConfigActivity : AbstractWidgetConfigActivity() {
    override fun initData() {
        super.initData()
        val cardStyles = resources.getStringArray(R.array.widget_card_styles)
        val cardStyleValues = resources.getStringArray(R.array.widget_card_style_values)
        cardStyleValueNow = "light"
        this.cardStyles = arrayOf(cardStyles[2], cardStyles[3], cardStyles[1])
        this.cardStyleValues = arrayOf(cardStyleValues[2], cardStyleValues[3], cardStyleValues[1])
    }

    override fun initView() {
        super.initView()
        mCardStyleContainer.visibility = View.VISIBLE
        mCardAlphaContainer.visibility = View.VISIBLE
    }

    override fun getRemoteViews(): RemoteViews {
        return DailyTrendWidgetIMP.getRemoteViews(
            this, locationNow, resources.displayMetrics.widthPixels, cardStyleValueNow, cardAlpha
        )
    }

    override fun getConfigStoreName(): String {
        return getString(R.string.sp_widget_daily_trend_setting)
    }
}