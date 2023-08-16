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
import org.breezyweather.remoteviews.presenters.WeekWidgetIMP

/**
 * Week widget config activity.
 */
class WeekWidgetConfigActivity : AbstractWidgetConfigActivity() {
    override fun initData() {
        super.initData()
        val widgetStyles = resources.getStringArray(R.array.widget_week_styles)
        val widgetStyleValues = resources.getStringArray(R.array.widget_week_style_values)
        viewTypeValueNow = "5_days"
        viewTypes = arrayOf(widgetStyles[0], widgetStyles[1])
        viewTypeValues = arrayOf(widgetStyleValues[0], widgetStyleValues[1])
    }

    override fun initView() {
        super.initView()
        mViewTypeContainer?.visibility = View.VISIBLE
        mCardStyleContainer?.visibility = View.VISIBLE
        mCardAlphaContainer?.visibility = View.VISIBLE
        mTextColorContainer?.visibility = View.VISIBLE
        mTextSizeContainer?.visibility = View.VISIBLE
    }

    override val remoteViews: RemoteViews
        get() {
            return WeekWidgetIMP.getRemoteViews(
                this, locationNow,
                viewTypeValueNow, cardStyleValueNow, cardAlpha, textColorValueNow, textSize
            )
        }

    override val configStoreName: String
        get() {
            return getString(R.string.sp_widget_week_setting)
        }
}