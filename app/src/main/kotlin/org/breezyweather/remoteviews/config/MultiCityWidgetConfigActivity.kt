/*
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

package org.breezyweather.remoteviews.config

import android.view.View
import android.widget.RemoteViews
import breezyweather.data.location.LocationRepository
import breezyweather.data.weather.WeatherRepository
import breezyweather.domain.location.model.Location
import dagger.hilt.android.AndroidEntryPoint
import org.breezyweather.R
import org.breezyweather.remoteviews.presenters.MultiCityWidgetIMP
import javax.inject.Inject

/**
 * Multi city widget config activity.
 */
@AndroidEntryPoint
class MultiCityWidgetConfigActivity : AbstractWidgetConfigActivity() {
    var locationList = mutableListOf<Location>()
        private set

    @Inject
    lateinit var locationRepository: LocationRepository

    @Inject
    lateinit var weatherRepository: WeatherRepository

    override suspend fun initLocations() {
        locationList = locationRepository.getXLocations(3, withParameters = false).toMutableList()
        for (i in locationList.indices) {
            locationList[i] = locationList[i].copy(
                weather = weatherRepository.getWeatherByLocationId(
                    locationList[i].formattedId,
                    withDaily = true,
                    withHourly = false,
                    withMinutely = false,
                    withAlerts = false,
                    withNormals = false
                )
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

    override fun updateWidgetView() {
        MultiCityWidgetIMP.updateWidgetView(this, locationList)
    }

    override val remoteViews: RemoteViews
        get() {
            return MultiCityWidgetIMP.getRemoteViews(
                this,
                locationList,
                cardStyleValueNow,
                cardAlpha,
                textColorValueNow,
                textSize
            )
        }

    override val configStoreName: String
        get() {
            return getString(R.string.sp_widget_multi_city)
        }
}
