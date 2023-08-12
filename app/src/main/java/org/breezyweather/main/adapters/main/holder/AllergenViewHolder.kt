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

package org.breezyweather.main.adapters.main.holder

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.TextView
import androidx.viewpager2.widget.ViewPager2
import org.breezyweather.R
import org.breezyweather.common.basic.GeoActivity
import org.breezyweather.common.basic.models.Location
import org.breezyweather.common.utils.helpers.IntentHelper
import org.breezyweather.main.adapters.HomeAllergenAdapter
import org.breezyweather.main.adapters.HomePollenViewHolder
import org.breezyweather.main.utils.MainThemeColorProvider
import org.breezyweather.theme.ThemeManager
import org.breezyweather.theme.resource.providers.ResourceProvider
import org.breezyweather.theme.weatherView.WeatherViewController

class AllergenViewHolder(parent: ViewGroup) : AbstractMainCardViewHolder(
    LayoutInflater
        .from(parent.context)
        .inflate(R.layout.container_main_pollen, parent, false)
) {
    private val mTitle: TextView = itemView.findViewById(R.id.container_main_pollen_title)
    private val mSubtitle: TextView = itemView.findViewById(R.id.container_main_pollen_subtitle)
    private val mIndicator: TextView = itemView.findViewById(R.id.container_main_pollen_indicator)
    private val mPager: ViewPager2 = itemView.findViewById(R.id.container_main_pollen_pager)
    private var mCallback: DailyPollenPageChangeCallback? = null

    private class DailyAllergenPagerAdapter(location: Location) : HomeAllergenAdapter(location) {
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HomePollenViewHolder {
            val holder = super.onCreateViewHolder(parent, viewType)
            holder.itemView.layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
            return holder
        }
    }

    private inner class DailyPollenPageChangeCallback(
        private val mContext: Context,
        private val mLocation: Location
    ) : ViewPager2.OnPageChangeCallback() {
        @SuppressLint("SetTextI18n")
        override fun onPageSelected(position: Int) {
            val timeZone = mLocation.timeZone
            val daily = mLocation.weather!!.dailyForecast[position]
            if (daily.isToday(timeZone)) {
                mIndicator.text = mContext.getString(R.string.short_today)
            } else {
                mIndicator.text = (position + 1).toString() + "/" + mLocation.weather.dailyForecast.filter { it.allergen?.isIndexValid == true }.size
            }
        }
    }

    @SuppressLint("SetTextI18n")
    override fun onBindView(
        activity: GeoActivity, location: Location,
        provider: ResourceProvider,
        listAnimationEnabled: Boolean, itemAnimationEnabled: Boolean, firstCard: Boolean
    ) {
        super.onBindView(
            activity, location, provider,
            listAnimationEnabled, itemAnimationEnabled, firstCard
        )
        mTitle.setTextColor(
            ThemeManager.getInstance(context)
                .weatherThemeDelegate
                .getThemeColors(
                    context,
                    WeatherViewController.getWeatherKind(location.weather),
                    location.isDaylight
                )[0]
        )
        mSubtitle.setTextColor(MainThemeColorProvider.getColor(location, R.attr.colorCaptionText))
        mPager.adapter = DailyAllergenPagerAdapter(location)
        mPager.currentItem = 0
        mCallback = DailyPollenPageChangeCallback(activity, location)
        mPager.registerOnPageChangeCallback(mCallback!!)
        itemView.contentDescription = mTitle.text
        itemView.setOnClickListener { IntentHelper.startAllergenActivity(context as GeoActivity, location) }
    }

    override fun onRecycleView() {
        super.onRecycleView()
        if (mCallback != null) {
            mPager.unregisterOnPageChangeCallback(mCallback!!)
            mCallback = null
        }
    }
}