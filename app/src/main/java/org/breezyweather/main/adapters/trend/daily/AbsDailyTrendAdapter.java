package org.breezyweather.main.adapters.trend.daily;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.View;

import androidx.recyclerview.widget.RecyclerView;

import java.util.TimeZone;

import org.breezyweather.common.basic.GeoActivity;
import org.breezyweather.common.basic.models.Location;
import org.breezyweather.common.basic.models.weather.Daily;
import org.breezyweather.common.basic.models.weather.Weather;
import org.breezyweather.common.ui.widgets.trend.TrendRecyclerView;
import org.breezyweather.common.ui.widgets.trend.TrendRecyclerViewAdapter;
import org.breezyweather.common.ui.widgets.trend.item.DailyTrendItemView;
import org.breezyweather.common.utils.helpers.IntentHelper;
import org.breezyweather.R;
import org.breezyweather.main.utils.MainThemeColorProvider;

public abstract class AbsDailyTrendAdapter extends TrendRecyclerViewAdapter<AbsDailyTrendAdapter.ViewHolder> {

    private final GeoActivity mActivity;

    public static class ViewHolder extends RecyclerView.ViewHolder {

        public final DailyTrendItemView dailyItem;

        ViewHolder(View itemView) {
            super(itemView);
            dailyItem = itemView.findViewById(R.id.item_trend_daily);
        }

        @SuppressLint({"SetTextI18n, InflateParams", "DefaultLocale"})
        void onBindView(GeoActivity activity, Location location,
                        StringBuilder talkBackBuilder, int position) {
            Context context = itemView.getContext();
            Weather weather = location.getWeather();
            TimeZone timeZone = location.getTimeZone();

            assert weather != null;
            Daily daily = weather.getDailyForecast().get(position);

            if (daily.isToday(timeZone)) {
                talkBackBuilder.append(", ").append(context.getString(R.string.today));
                dailyItem.setWeekText(context.getString(R.string.today));
            } else {
                talkBackBuilder.append(", ").append(daily.getWeek(context, location.getTimeZone()));
                dailyItem.setWeekText(daily.getWeek(context, location.getTimeZone()));
            }

            talkBackBuilder.append(", ").append(daily.getLongDate(context, location.getTimeZone()));
            dailyItem.setDateText(daily.getShortDate(context, location.getTimeZone()));

            dailyItem.setTextColor(
                    MainThemeColorProvider.getColor(location, R.attr.colorTitleText),
                    MainThemeColorProvider.getColor(location, R.attr.colorBodyText)
            );

            dailyItem.setOnClickListener(v -> onItemClicked(activity, location, getBindingAdapterPosition()));
        }
    }

    public AbsDailyTrendAdapter(GeoActivity activity, Location location) {
        super(location);
        mActivity = activity;
    }

    protected static void onItemClicked(GeoActivity activity, Location location, int adapterPosition) {
        if (activity.isActivityResumed()) {
            IntentHelper.startDailyWeatherActivity(activity, location.getFormattedId(), adapterPosition);
        }
    }

    public GeoActivity getActivity() {
        return mActivity;
    }

    public abstract boolean isValid(Location location);

    public String getKey() {
        return getClass().getName();
    }

    public abstract String getDisplayName(Context context);

    public abstract void bindBackgroundForHost(TrendRecyclerView host);
}
