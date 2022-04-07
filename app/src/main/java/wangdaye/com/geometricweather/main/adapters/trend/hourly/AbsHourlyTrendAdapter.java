package wangdaye.com.geometricweather.main.adapters.trend.hourly;

import android.content.Context;
import android.view.View;

import androidx.recyclerview.widget.RecyclerView;

import wangdaye.com.geometricweather.R;
import wangdaye.com.geometricweather.common.basic.GeoActivity;
import wangdaye.com.geometricweather.common.basic.models.Location;
import wangdaye.com.geometricweather.common.basic.models.weather.Hourly;
import wangdaye.com.geometricweather.common.basic.models.weather.Weather;
import wangdaye.com.geometricweather.main.dialogs.HourlyWeatherDialog;
import wangdaye.com.geometricweather.common.ui.widgets.trend.TrendRecyclerViewAdapter;
import wangdaye.com.geometricweather.common.ui.widgets.trend.item.HourlyTrendItemView;
import wangdaye.com.geometricweather.main.utils.MainModuleUtils;
import wangdaye.com.geometricweather.theme.ThemeManager;

public abstract class AbsHourlyTrendAdapter<VH extends RecyclerView.ViewHolder> extends TrendRecyclerViewAdapter<VH>  {

    private final GeoActivity mActivity;
    private final Context mThemeCtx;

    protected static class ViewHolder extends RecyclerView.ViewHolder {

        public final HourlyTrendItemView hourlyItem;

        ViewHolder(View itemView) {
            super(itemView);
            hourlyItem = itemView.findViewById(R.id.item_trend_hourly);
        }

        void onBindView(GeoActivity activity, Context themeCtx, Location location,
                        StringBuilder talkBackBuilder, int position) {
            Context context = itemView.getContext();
            Weather weather = location.getWeather();

            assert weather != null;
            Hourly hourly = weather.getHourlyForecast().get(position);

            talkBackBuilder
                    .append(", ").append(hourly.getLongDate(activity))
                    .append(", ").append(hourly.getHour(activity));
            hourlyItem.setHourText(hourly.getHour(context));
            hourlyItem.setTextColor(
                    ThemeManager.getInstance(context).getThemeColor(themeCtx, R.attr.colorBodyText)
            );

            hourlyItem.setOnClickListener(v -> onItemClicked(
                    activity, location, getAdapterPosition()
            ));
        }
    }

    public AbsHourlyTrendAdapter(GeoActivity activity, Location location) {
        super(location);
        mActivity = activity;
        mThemeCtx = ThemeManager.getInstance(activity).generateThemeContext(
                activity,
                MainModuleUtils.isHomeLightTheme(
                        activity,
                        ThemeManager.getInstance(activity).isDaylight()
                )
        );
    }

    protected static void onItemClicked(GeoActivity activity,
                                        Location location,
                                        int adapterPosition) {
        if (activity.isActivityResumed()) {
            HourlyWeatherDialog.getInstance(
                    location.getWeather(),
                    adapterPosition
            ).show(activity.getSupportFragmentManager(), null);
        }
    }

    public GeoActivity getActivity() {
        return mActivity;
    }

    public Context getThemeCtx() {
        return mThemeCtx;
    }
}
