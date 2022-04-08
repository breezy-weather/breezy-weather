package wangdaye.com.geometricweather.main.adapters.trend.daily;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.View;

import androidx.recyclerview.widget.RecyclerView;

import java.util.TimeZone;

import wangdaye.com.geometricweather.R;
import wangdaye.com.geometricweather.common.basic.GeoActivity;
import wangdaye.com.geometricweather.common.basic.models.Location;
import wangdaye.com.geometricweather.common.basic.models.weather.Daily;
import wangdaye.com.geometricweather.common.basic.models.weather.Weather;
import wangdaye.com.geometricweather.common.ui.widgets.trend.TrendRecyclerViewAdapter;
import wangdaye.com.geometricweather.common.ui.widgets.trend.item.DailyTrendItemView;
import wangdaye.com.geometricweather.common.utils.helpers.IntentHelper;
import wangdaye.com.geometricweather.main.utils.MainModuleUtils;
import wangdaye.com.geometricweather.theme.ThemeManager;

public abstract class AbsDailyTrendAdapter<VH extends RecyclerView.ViewHolder> extends TrendRecyclerViewAdapter<VH>  {

    private final GeoActivity mActivity;
    private final Context mThemeCtx;

    protected static class ViewHolder extends RecyclerView.ViewHolder {

        public final DailyTrendItemView dailyItem;

        ViewHolder(View itemView) {
            super(itemView);
            dailyItem = itemView.findViewById(R.id.item_trend_daily);
        }

        @SuppressLint({"SetTextI18n, InflateParams", "DefaultLocale"})
        void onBindView(GeoActivity activity, Context themeCtx, Location location,
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
                talkBackBuilder.append(", ").append(daily.getWeek(context));
                dailyItem.setWeekText(daily.getWeek(context));
            }

            talkBackBuilder.append(", ").append(daily.getLongDate(context));
            dailyItem.setDateText(daily.getShortDate(context));

            dailyItem.setTextColor(
                    ThemeManager.getInstance(context).getThemeColor(themeCtx, R.attr.colorBodyText),
                    ThemeManager.getInstance(context).getThemeColor(themeCtx, R.attr.colorCaptionText)
            );

            dailyItem.setOnClickListener(v -> onItemClicked(activity, location, getAdapterPosition()));
        }
    }

    public AbsDailyTrendAdapter(GeoActivity activity, Location location) {
        super(location);
        mActivity = activity;
        mThemeCtx = ThemeManager.getInstance(activity).generateThemeContext(
                activity,
                MainModuleUtils.isHomeLightTheme(activity, location.isDaylight())
        );
    }

    protected static void onItemClicked(GeoActivity activity, Location location, int adapterPosition) {
        if (activity.isActivityResumed()) {
            IntentHelper.startDailyWeatherActivity(activity, location.getFormattedId(), adapterPosition);
        }
    }

    public GeoActivity getActivity() {
        return mActivity;
    }

    public Context getThemeCtx() {
        return mThemeCtx;
    }
}
