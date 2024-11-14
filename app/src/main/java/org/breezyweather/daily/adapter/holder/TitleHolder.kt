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

package org.breezyweather.daily.adapter.holder

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import org.breezyweather.R
import org.breezyweather.daily.adapter.DailyWeatherAdapter
import org.breezyweather.daily.adapter.model.Title

class TitleHolder(parent: ViewGroup) : DailyWeatherAdapter.ViewHolder(
    LayoutInflater.from(parent.context).inflate(R.layout.item_weather_daily_title, parent, false)
) {
    private val mIcon: ImageView = itemView.findViewById(R.id.item_weather_daily_title_icon)
    private val mTitle: TextView = itemView.findViewById(R.id.item_weather_daily_title_title)

    override fun onBindView(model: DailyWeatherAdapter.ViewModel, position: Int) {
        val t = model as Title
        if (t.resId != null) {
            mIcon.visibility = View.VISIBLE
            mIcon.setImageResource(t.resId)
        } else {
            mIcon.visibility = View.GONE
        }
        mTitle.text = t.title
    }
}
