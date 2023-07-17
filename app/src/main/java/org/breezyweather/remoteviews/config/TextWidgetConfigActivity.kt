package org.breezyweather.remoteviews.config

import android.view.View
import android.widget.RemoteViews
import org.breezyweather.R
import org.breezyweather.remoteviews.presenters.TextWidgetIMP.getRemoteViews

/**
 * Text widget config activity.
 */
class TextWidgetConfigActivity : AbstractWidgetConfigActivity() {
    override fun initView() {
        super.initView()
        mTextColorContainer?.visibility = View.VISIBLE
        mTextSizeContainer?.visibility = View.VISIBLE
        mAlignEndContainer?.visibility = View.VISIBLE
    }

    override val remoteViews: RemoteViews
        get() {
            return getRemoteViews(this, locationNow, textColorValueNow, textSize, alignEnd)
        }

    override val configStoreName: String
        get() {
            return getString(R.string.sp_widget_text_setting)
        }
}