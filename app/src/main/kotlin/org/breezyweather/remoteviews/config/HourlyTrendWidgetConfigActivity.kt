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
import org.breezyweather.remoteviews.presenters.HourlyTrendWidgetIMP
import javax.inject.Inject

/**
 * Hourly trend widget config activity.
 */
@AndroidEntryPoint
class HourlyTrendWidgetConfigActivity : AbstractWidgetConfigActivity() {
    var locationNow: Location? = null
        private set

    @Inject
    lateinit var locationRepository: LocationRepository

    @Inject
    lateinit var weatherRepository: WeatherRepository

    override suspend fun initLocations() {
        val location = locationRepository.getFirstLocation(withParameters = false)
        locationNow = location?.copy(
            weather = weatherRepository.getWeatherByLocationId(
                location.formattedId,
                withDaily = true, // isDaylight
                withHourly = true,
                withMinutely = false,
                withAlerts = false,
                withNormals = true // Threshold lines
            )
        )
    }

    override fun initData() {
        super.initData()
        val cardStyles = resources.getStringArray(R.array.widget_card_styles)
        val cardStyleValues = resources.getStringArray(R.array.widget_card_style_values)
        cardStyleValueNow = "auto"
        this.cardStyles = arrayOf(cardStyles[1], cardStyles[2], cardStyles[3])
        this.cardStyleValues = arrayOf(cardStyleValues[1], cardStyleValues[2], cardStyleValues[3])
    }

    override fun initView() {
        super.initView()
        mCardStyleContainer?.visibility = View.VISIBLE
        mCardAlphaContainer?.visibility = View.VISIBLE
    }

    override fun updateWidgetView() {
        HourlyTrendWidgetIMP.updateWidgetView(this, locationNow)
    }

    override val remoteViews: RemoteViews
        get() {
            return HourlyTrendWidgetIMP.getRemoteViews(
                this,
                locationNow,
                resources.displayMetrics.widthPixels,
                this.cardStyleValueNow,
                cardAlpha
            )
        }

    override val configStoreName: String
        get() {
            return getString(R.string.sp_widget_hourly_trend_setting)
        }
}
