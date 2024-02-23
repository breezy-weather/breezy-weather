/**
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
import org.breezyweather.remoteviews.presenters.ClockDayWeekWidgetIMP
import javax.inject.Inject

/**
 * Clock day week widget config activity.
 */
@AndroidEntryPoint
class ClockDayWeekWidgetConfigActivity : AbstractWidgetConfigActivity() {
    var locationNow: Location? = null

    @Inject
    lateinit var locationRepository: LocationRepository

    @Inject
    lateinit var weatherRepository: WeatherRepository

    override suspend fun initLocations() {
        val location = locationRepository.getFirstLocation(withParameters = false)
        locationNow = location?.copy(
            weather = weatherRepository.getWeatherByLocationId(
                location.formattedId,
                withDaily = true,
                withHourly = false,
                withMinutely = false,
                withAlerts = false
            )
        )
    }

    override fun initData() {
        super.initData()
        val clockFonts = resources.getStringArray(R.array.widget_clock_fonts)
        val clockFontValues = resources.getStringArray(R.array.widget_clock_font_values)
        clockFontValueNow = "light"
        this.clockFonts = arrayOf(clockFonts[0], clockFonts[1], clockFonts[2])
        this.clockFontValues = arrayOf(clockFontValues[0], clockFontValues[1], clockFontValues[2])
    }

    override fun initView() {
        super.initView()
        mCardStyleContainer?.visibility = View.VISIBLE
        mCardAlphaContainer?.visibility = View.VISIBLE
        mTextColorContainer?.visibility = View.VISIBLE
        mTextSizeContainer?.visibility = View.VISIBLE
        mClockFontContainer?.visibility = View.VISIBLE
        mHideLunarContainer?.visibility = isHideLunarContainerVisible
    }

    override val remoteViews: RemoteViews
        get() {
            return ClockDayWeekWidgetIMP.getRemoteViews(
                this, locationNow,
                cardStyleValueNow, cardAlpha, textColorValueNow, textSize, clockFontValueNow, hideLunar
            )
        }

    override val configStoreName: String
        get() {
            return getString(R.string.sp_widget_clock_day_week_setting)
        }
}