package org.breezyweather.main.adapters.trend.daily

import android.annotation.SuppressLint
import android.content.Context
import android.view.View
import androidx.recyclerview.widget.RecyclerView
import org.breezyweather.R
import org.breezyweather.common.basic.GeoActivity
import org.breezyweather.common.basic.models.Location
import org.breezyweather.common.extensions.getFormattedDate
import org.breezyweather.common.ui.adapters.TagAdapter
import org.breezyweather.common.ui.widgets.trend.TrendRecyclerView
import org.breezyweather.common.ui.widgets.trend.TrendRecyclerViewAdapter
import org.breezyweather.common.ui.widgets.trend.item.DailyTrendItemView
import org.breezyweather.common.utils.helpers.IntentHelper
import org.breezyweather.main.utils.MainThemeColorProvider

abstract class AbsDailyTrendAdapter(val activity: GeoActivity, location: Location) :
    TrendRecyclerViewAdapter<AbsDailyTrendAdapter.ViewHolder>(location) {

    open class ViewHolder internal constructor(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val dailyItem: DailyTrendItemView = itemView.findViewById(R.id.item_trend_daily)

        @SuppressLint("SetTextI18n, InflateParams", "DefaultLocale")
        fun onBindView(
            activity: GeoActivity, location: Location,
            talkBackBuilder: StringBuilder, position: Int
        ) {
            val context = itemView.context
            val weather = location.weather
            val timeZone = location.timeZone
            val daily = weather!!.dailyForecast[position]
            if (daily.isToday(timeZone)) {
                talkBackBuilder.append(", ").append(context.getString(R.string.short_today))
                dailyItem.setWeekText(context.getString(R.string.short_today))
            } else {
                talkBackBuilder.append(", ").append(daily.getWeek(context, location.timeZone))
                dailyItem.setWeekText(daily.getWeek(context, location.timeZone))
            }
            talkBackBuilder.append(", ").append(daily.date.getFormattedDate(location.timeZone, context.getString(R.string.date_format_long)))
            dailyItem.setDateText(daily.date.getFormattedDate(location.timeZone, context.getString(R.string.date_format_short)))
            dailyItem.setTextColor(
                MainThemeColorProvider.getColor(location, R.attr.colorTitleText),
                MainThemeColorProvider.getColor(location, R.attr.colorBodyText)
            )
            dailyItem.setOnClickListener { onItemClicked(activity, location, bindingAdapterPosition) }
        }
    }

    val key: String = javaClass.name
    abstract fun isValid(location: Location): Boolean
    abstract fun getDisplayName(context: Context): String
    abstract fun bindBackgroundForHost(host: TrendRecyclerView)

    companion object {
        protected fun onItemClicked(activity: GeoActivity, location: Location, adapterPosition: Int) {
            if (activity.isActivityResumed) {
                IntentHelper.startDailyWeatherActivity(activity, location.formattedId, adapterPosition)
            }
        }
    }
}
