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
import org.breezyweather.remoteviews.presenters.ClockDayHourWeekWidgetIMP
import javax.inject.Inject

/**
 * Clock day hour week widget config activity.
 */
@AndroidEntryPoint
class ClockDayHourWeekWidgetConfigActivity : AbstractWidgetConfigActivity() {
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
                withDaily = true, // Needed for daily forecast and isDaylight
                withHourly = true, // Needed for hourly forecast
                withMinutely = false,
                withAlerts = false
            )
        )
    }

    override fun initData() {
        super.initData()
        val clockFonts = resources.getStringArray(R.array.widget_clock_fonts)
        val clockFontValues = resources.getStringArray(R.array.widget_clock_font_values)
        clockFontValueNow = "light" // Default clock font
        this.clockFonts = arrayOf(clockFonts[0], clockFonts[1], clockFonts[2])
        this.clockFontValues = arrayOf(clockFontValues[0], clockFontValues[1], clockFontValues[2])
        // Other defaults (cardStyle, textColor, textSize, etc.) are handled in AbstractWidgetConfigActivity
    }

    override fun initView() {
        super.initView()
        // Show relevant config options for this widget
        mCardStyleContainer?.visibility = View.VISIBLE
        mCardAlphaContainer?.visibility = View.VISIBLE
        mTextColorContainer?.visibility = View.VISIBLE
        mTextSizeContainer?.visibility = View.VISIBLE
        mClockFontContainer?.visibility = View.VISIBLE
        mHideAlternateCalendarContainer?.visibility = isHideAlternateCalendarContainerVisible
    }

    override fun updateWidgetView() {
        // Trigger an update using the current settings
        ClockDayHourWeekWidgetIMP.updateWidgetView(this, locationNow)
    }

    override val remoteViews: RemoteViews
        get() {
            // Generate a preview RemoteViews based on current config selections
            return ClockDayHourWeekWidgetIMP.getRemoteViews(
                this,
                locationNow,
                cardStyleValueNow,
                cardAlpha,
                textColorValueNow,
                textSize,
                clockFontValueNow,
                hideAlternateCalendar
            )
        }

    override val configStoreName: String
        get() {
            // Return the unique preference key for this widget's settings
            return getString(R.string.sp_widget_clock_day_hour_week_setting)
        }
}
