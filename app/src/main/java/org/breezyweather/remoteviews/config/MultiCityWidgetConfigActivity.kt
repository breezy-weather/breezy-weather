package org.breezyweather.remoteviews.config

import android.view.View
import android.widget.RemoteViews
import dagger.hilt.android.AndroidEntryPoint
import org.breezyweather.R
import org.breezyweather.common.basic.models.Location
import org.breezyweather.db.repositories.LocationEntityRepository.readLocationList
import org.breezyweather.db.repositories.WeatherEntityRepository.readWeather
import org.breezyweather.remoteviews.presenters.MultiCityWidgetIMP

/**
 * Multi city widget config activity.
 */
@AndroidEntryPoint
class MultiCityWidgetConfigActivity : AbstractWidgetConfigActivity() {
    private var locationList: MutableList<Location>? = null
    override fun initData() {
        super.initData()
        locationList = readLocationList(this).map {
            it.copy(weather = readWeather(it))
        }.toMutableList()
    }

    override fun initView() {
        super.initView()
        mCardStyleContainer.visibility = View.VISIBLE
        mCardAlphaContainer.visibility = View.VISIBLE
        mTextColorContainer.visibility = View.VISIBLE
        mTextSizeContainer.visibility = View.VISIBLE
    }

    override fun getRemoteViews(): RemoteViews {
        return MultiCityWidgetIMP.getRemoteViews(
            this, locationList, cardStyleValueNow, cardAlpha, textColorValueNow, textSize
        )
    }

    override fun getConfigStoreName(): String {
        return getString(R.string.sp_widget_multi_city)
    }
}
