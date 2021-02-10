package wangdaye.com.geometricweather.main.adapter.trend.hourly;

import android.content.Context;
import android.view.View;

import androidx.recyclerview.widget.RecyclerView;

import wangdaye.com.geometricweather.R;
import wangdaye.com.geometricweather.basic.GeoActivity;
import wangdaye.com.geometricweather.basic.model.Location;
import wangdaye.com.geometricweather.basic.model.weather.Hourly;
import wangdaye.com.geometricweather.basic.model.weather.Weather;
import wangdaye.com.geometricweather.main.dialog.HourlyWeatherDialog;
import wangdaye.com.geometricweather.ui.widget.trend.TrendRecyclerViewAdapter;
import wangdaye.com.geometricweather.ui.widget.trend.item.HourlyTrendItemView;
import wangdaye.com.geometricweather.utils.manager.ThemeManager;

public abstract class AbsHourlyTrendAdapter<VH extends RecyclerView.ViewHolder> extends TrendRecyclerViewAdapter<VH>  {

    private final GeoActivity mActivity;

    protected static class ViewHolder extends RecyclerView.ViewHolder {

        public final HourlyTrendItemView hourlyItem;

        ViewHolder(View itemView) {
            super(itemView);
            hourlyItem = itemView.findViewById(R.id.item_trend_hourly);
        }

        void onBindView(GeoActivity activity, Location location, ThemeManager themeManager,
                        StringBuilder talkBackBuilder, int position) {
            Context context = itemView.getContext();
            Weather weather = location.getWeather();

            assert weather != null;
            Hourly hourly = weather.getHourlyForecast().get(position);

            talkBackBuilder
                    .append(", ").append(hourly.getLongDate(activity))
                    .append(", ").append(hourly.getHour(activity));
            hourlyItem.setHourText(hourly.getHour(context));
            hourlyItem.setTextColor(themeManager.getTextContentColor(context));

            hourlyItem.setOnClickListener(v -> onItemClicked(activity, location, getAdapterPosition()));
        }
    }

    public AbsHourlyTrendAdapter(GeoActivity activity, Location location) {
        super(location);
        mActivity = activity;
    }

    protected static void onItemClicked(GeoActivity activity, Location location, int adapterPosition) {
        if (activity.isForeground()) {
            HourlyWeatherDialog dialog = new HourlyWeatherDialog();
            dialog.setData(
                    location.getWeather(),
                    adapterPosition,
                    ThemeManager.getInstance(activity).getWeatherThemeColors()[0]
            );
            dialog.show(activity.getSupportFragmentManager(), null);
        }
    }

    public GeoActivity getActivity() {
        return mActivity;
    }
}
