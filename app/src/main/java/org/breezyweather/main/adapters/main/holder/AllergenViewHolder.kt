package org.breezyweather.main.adapters.main.holder

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.TextView
import org.breezyweather.R
import org.breezyweather.common.basic.GeoActivity
import org.breezyweather.common.basic.models.Location
import org.breezyweather.common.ui.widgets.horizontal.HorizontalViewPager2
import org.breezyweather.common.utils.helpers.IntentHelper
import org.breezyweather.main.adapters.HomePollenAdapter
import org.breezyweather.main.adapters.HomePollenViewHolder
import org.breezyweather.main.utils.MainThemeColorProvider
import org.breezyweather.theme.ThemeManager
import org.breezyweather.theme.resource.providers.ResourceProvider
import org.breezyweather.theme.weatherView.WeatherViewController.getWeatherKind

class AllergenViewHolder(parent: ViewGroup) : AbstractMainCardViewHolder(
    LayoutInflater
        .from(parent.context)
        .inflate(R.layout.container_main_pollen, parent, false)
) {
    private val mTitle: TextView = itemView.findViewById(R.id.container_main_pollen_title)
    private val mSubtitle: TextView = itemView.findViewById(R.id.container_main_pollen_subtitle)
    private val mIndicator: TextView = itemView.findViewById(R.id.container_main_pollen_indicator)
    private val mPager: HorizontalViewPager2 = itemView.findViewById(R.id.container_main_pollen_pager)
    private var mCallback: DailyPollenPageChangeCallback? = null

    private class DailyPollenPagerAdapter(location: Location) : HomePollenAdapter(location) {
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
    ) : HorizontalViewPager2.OnPageChangeCallback() {
        @SuppressLint("SetTextI18n")
        override fun onPageSelected(position: Int) {
            val timeZone = mLocation.timeZone
            val daily = mLocation.weather!!.dailyForecast[position]
            if (daily.isToday(timeZone)) {
                mIndicator.text = mContext.getString(R.string.short_today)
            } else {
                mIndicator.text = (position + 1).toString() + "/" + mLocation.weather.dailyForecast.size
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
                    getWeatherKind(location.weather),
                    location.isDaylight
                )[0]
        )
        mSubtitle.setTextColor(MainThemeColorProvider.getColor(location, R.attr.colorCaptionText))
        mPager.adapter = DailyPollenPagerAdapter(location)
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