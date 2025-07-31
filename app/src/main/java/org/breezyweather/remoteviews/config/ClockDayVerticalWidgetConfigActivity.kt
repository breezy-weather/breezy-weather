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
import org.breezyweather.remoteviews.presenters.ClockDayVerticalWidgetIMP
import org.breezyweather.sources.SourceManager
import javax.inject.Inject

/**
 * Clock day vertical widget config activity.
 */
@AndroidEntryPoint
class ClockDayVerticalWidgetConfigActivity : AbstractWidgetConfigActivity() {
    var locationNow: Location? = null
        private set

    @Inject
    lateinit var locationRepository: LocationRepository

    @Inject
    lateinit var weatherRepository: WeatherRepository

    @Inject
    lateinit var sourceManager: SourceManager

    override suspend fun initLocations() {
        val location = locationRepository.getFirstLocation(withParameters = false)
        locationNow = location?.copy(
            weather = weatherRepository.getWeatherByLocationId(
                location.formattedId,
                withDaily = true,
                withHourly = false,
                withMinutely = false,
                withAlerts = true, // Custom subtitle
                withNormals = false
            )
        )
    }

    override fun initData() {
        super.initData()
        val widgetStyles = resources.getStringArray(R.array.widget_styles)
        val widgetStyleValues = resources.getStringArray(R.array.widget_style_values)
        viewTypeValueNow = "rectangle"
        viewTypes = arrayOf(
            widgetStyles[0],
            widgetStyles[1],
            widgetStyles[2],
            widgetStyles[3],
            widgetStyles[6],
            widgetStyles[8]
        )
        viewTypeValues = arrayOf(
            widgetStyleValues[0],
            widgetStyleValues[1],
            widgetStyleValues[2],
            widgetStyleValues[3],
            widgetStyleValues[6],
            widgetStyleValues[8]
        )
    }

    override fun initView() {
        super.initView()
        mViewTypeContainer?.visibility = View.VISIBLE
        mCardStyleContainer?.visibility = View.VISIBLE
        mCardAlphaContainer?.visibility = View.VISIBLE
        mHideSubtitleContainer?.visibility = View.VISIBLE
        mSubtitleDataContainer?.visibility = View.VISIBLE
        mTextColorContainer?.visibility = View.VISIBLE
        mTextSizeContainer?.visibility = View.VISIBLE
        mClockFontContainer?.visibility = View.VISIBLE
    }

    override fun updateWidgetView() {
        ClockDayVerticalWidgetIMP.updateWidgetView(
            this,
            locationNow,
            locationNow?.let { location ->
                sourceManager.getPollenIndexSource(
                    (location.pollenSource ?: "").ifEmpty { location.forecastSource }
                )
            }
        )
    }

    override val remoteViews: RemoteViews
        get() {
            return ClockDayVerticalWidgetIMP.getRemoteViews(
                this,
                locationNow,
                viewTypeValueNow,
                cardStyleValueNow,
                cardAlpha,
                textColorValueNow,
                textSize,
                hideSubtitle,
                subtitleDataValueNow,
                clockFontValueNow,
                locationNow?.let { location ->
                    sourceManager.getPollenIndexSource(
                        (location.pollenSource ?: "").ifEmpty { location.forecastSource }
                    )
                }
            )
        }

    override val configStoreName: String
        get() {
            return getString(R.string.sp_widget_clock_day_vertical_setting)
        }
}
