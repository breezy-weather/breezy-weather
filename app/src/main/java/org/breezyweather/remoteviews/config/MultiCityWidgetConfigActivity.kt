package org.breezyweather.remoteviews.config

import android.view.View
import android.widget.RemoteViews
import org.breezyweather.R
import org.breezyweather.common.basic.models.Location
import org.breezyweather.db.repositories.LocationEntityRepository
import org.breezyweather.db.repositories.WeatherEntityRepository
import org.breezyweather.remoteviews.presenters.MultiCityWidgetIMP

/**
 * Multi city widget config activity.
 */
class MultiCityWidgetConfigActivity : AbstractWidgetConfigActivity() {
    private var locationList = mutableListOf<Location>()

    override fun initData() {
        super.initData()
        locationList = LocationEntityRepository.readLocationList().toMutableList()
        for (i in locationList.indices) {
            locationList[i] = locationList[i].copy(
                weather = WeatherEntityRepository.readWeather(locationList[i])
            )
        }
    }

    override fun initView() {
        super.initView()
        mCardStyleContainer?.visibility = View.VISIBLE
        mCardAlphaContainer?.visibility = View.VISIBLE
        mTextColorContainer?.visibility = View.VISIBLE
        mTextSizeContainer?.visibility = View.VISIBLE
    }

    override val remoteViews: RemoteViews
        get() {
            return MultiCityWidgetIMP.getRemoteViews(
                this, locationList, cardStyleValueNow, cardAlpha, textColorValueNow, textSize
            )
        }

    override val configStoreName: String
        get() {
            return getString(R.string.sp_widget_multi_city)
        }
}
