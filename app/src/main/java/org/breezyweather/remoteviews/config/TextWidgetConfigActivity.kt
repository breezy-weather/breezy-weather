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
import org.breezyweather.remoteviews.presenters.TextWidgetIMP.getRemoteViews

/**
 * Text widget config activity.
 */
class TextWidgetConfigActivity : AbstractWidgetConfigActivity() {
    override fun initView() {
        super.initView()
        mTextColorContainer?.visibility = View.VISIBLE
        mTextSizeContainer?.visibility = View.VISIBLE
        mAlignEndContainer?.visibility = View.VISIBLE
    }

    override val remoteViews: RemoteViews
        get() {
            return getRemoteViews(this, locationNow, textColorValueNow, textSize, alignEnd)
        }

    override val configStoreName: String
        get() {
            return getString(R.string.sp_widget_text_setting)
        }
}