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
import org.breezyweather.R
import org.breezyweather.remoteviews.presenters.HourlyTrendWidgetIMP

/**
 * Hourly trend widget config activity.
 */
class HourlyTrendWidgetConfigActivity : AbstractWidgetConfigActivity() {
    override fun initData() {
        super.initData()
        val cardStyles = resources.getStringArray(R.array.widget_card_styles)
        val cardStyleValues = resources.getStringArray(R.array.widget_card_style_values)
        cardStyleValueNow = "app"
        this.cardStyles = arrayOf(cardStyles[1], cardStyles[2], cardStyles[3], cardStyles[4])
        this.cardStyleValues = arrayOf(cardStyleValues[1], cardStyleValues[2], cardStyleValues[3], cardStyleValues[4])
    }

    override fun initView() {
        super.initView()
        mCardStyleContainer?.visibility = View.VISIBLE
        mCardAlphaContainer?.visibility = View.VISIBLE
    }

    override val remoteViews: RemoteViews
        get() {
            return HourlyTrendWidgetIMP.getRemoteViews(
                this, locationNow,
                resources.displayMetrics.widthPixels, cardStyleValueNow, cardAlpha
            )
        }

    override val configStoreName: String
        get() {
            return getString(R.string.sp_widget_hourly_trend_setting)
        }
}