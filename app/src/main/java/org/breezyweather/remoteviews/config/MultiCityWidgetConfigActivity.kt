/**
 * This file is part of Breezy Weather.
 *
 * Breezy Weather is free software: you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published by the
 * Free Software Foundation, either version 3 of the License, or (at your
 * option) any later version.
 *
 * Breezy Weather is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public
 * License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Breezy Weather. If not, see <https://www.gnu.org/licenses/>.
 */

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
