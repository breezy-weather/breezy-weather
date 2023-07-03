package org.breezyweather.remoteviews.config

import android.view.View
import android.widget.RemoteViews
import dagger.hilt.android.AndroidEntryPoint
import org.breezyweather.R
import org.breezyweather.remoteviews.presenters.TextWidgetIMP.getRemoteViews

/**
 * Text widget config activity.
 */
@AndroidEntryPoint
class TextWidgetConfigActivity : AbstractWidgetConfigActivity() {
    override fun initView() {
        super.initView()
        mTextColorContainer.visibility = View.VISIBLE
        mTextSizeContainer.visibility = View.VISIBLE
        mAlignEndContainer.visibility = View.VISIBLE
    }

    override fun getRemoteViews(): RemoteViews {
        return getRemoteViews(this, getLocationNow(), textColorValueNow, textSize, alignEnd)
    }

    override fun getConfigStoreName(): String {
        return getString(R.string.sp_widget_text_setting)
    }
}