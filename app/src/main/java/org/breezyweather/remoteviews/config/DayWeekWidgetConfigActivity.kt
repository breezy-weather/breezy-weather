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
import org.breezyweather.remoteviews.presenters.DayWeekWidgetIMP

/**
 * Day week widget config activity.
 */
class DayWeekWidgetConfigActivity : AbstractWidgetConfigActivity() {
    override fun initData() {
        super.initData()
        val widgetStyles = resources.getStringArray(R.array.widget_styles)
        val widgetStyleValues = resources.getStringArray(R.array.widget_style_values)
        viewTypeValueNow = "rectangle"
        viewTypes = arrayOf(
            widgetStyles[0],
            widgetStyles[1],
            widgetStyles[2]
        )
        viewTypeValues = arrayOf(
            widgetStyleValues[0],
            widgetStyleValues[1],
            widgetStyleValues[2]
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
    }

    override val remoteViews: RemoteViews
        get() {
            return DayWeekWidgetIMP.getRemoteViews(
                this, locationNow,
                viewTypeValueNow, cardStyleValueNow, cardAlpha, textColorValueNow, textSize,
                hideSubtitle, subtitleDataValueNow
            )
        }

    override val configStoreName: String
        get() {
            return getString(R.string.sp_widget_day_week_setting)
        }
}