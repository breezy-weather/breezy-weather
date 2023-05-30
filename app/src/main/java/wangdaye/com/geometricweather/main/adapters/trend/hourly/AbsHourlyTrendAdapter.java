package wangdaye.com.geometricweather.main.adapters.trend.hourly;

import android.content.Context;
import android.view.View;

import androidx.recyclerview.widget.RecyclerView;

import wangdaye.com.geometricweather.R;
import wangdaye.com.geometricweather.common.basic.GeoActivity;
import wangdaye.com.geometricweather.common.basic.models.Location;
import wangdaye.com.geometricweather.common.basic.models.weather.Hourly;
import wangdaye.com.geometricweather.common.basic.models.weather.Weather;
import wangdaye.com.geometricweather.common.ui.widgets.trend.TrendRecyclerView;
import wangdaye.com.geometricweather.common.ui.widgets.trend.TrendRecyclerViewAdapter;
import wangdaye.com.geometricweather.common.ui.widgets.trend.item.HourlyTrendItemView;
import wangdaye.com.geometricweather.main.dialogs.HourlyWeatherDialog;
import wangdaye.com.geometricweather.main.utils.MainThemeColorProvider;

public abstract class AbsHourlyTrendAdapter extends TrendRecyclerViewAdapter<AbsHourlyTrendAdapter.ViewHolder>  {

    private final GeoActivity mActivity;

    public static class ViewHolder extends RecyclerView.ViewHolder {

        public final HourlyTrendItemView hourlyItem;

        ViewHolder(View itemView) {
            super(itemView);
            hourlyItem = itemView.findViewById(R.id.item_trend_hourly);
        }

        void onBindView(GeoActivity activity, Location location,
                        StringBuilder talkBackBuilder, int position) {
            Context context = itemView.getContext();
            Weather weather = location.getWeather();

            assert weather != null;
            Hourly hourly = weather.getHourlyForecast().get(position);

            talkBackBuilder.append(", ").append(hourly.getLongDate(context, location.getTimeZone()));
            hourlyItem.setDayText(hourly.getShortDate(context, location.getTimeZone()));

            talkBackBuilder
                    .append(", ").append(hourly.getLongDate(activity, location.getTimeZone()))
                    .append(", ").append(hourly.getHour(activity, location.getTimeZone()));
            hourlyItem.setHourText(hourly.getHour(context, location.getTimeZone()));

            boolean useAccentColorForDate = position == 0 || hourly.getHourIn24Format(location.getTimeZone()) == 0;
            hourlyItem.setTextColor(
                    MainThemeColorProvider.getColor(location, R.attr.colorTitleText),
                    MainThemeColorProvider.getColor(
                            location,
                            useAccentColorForDate ? R.attr.colorBodyText : R.attr.colorCaptionText
                    )
            );

            hourlyItem.setOnClickListener(v -> onItemClicked(
                    activity, location, getBindingAdapterPosition()
            ));
        }
    }

    public AbsHourlyTrendAdapter(GeoActivity activity, Location location) {
        super(location);
        mActivity = activity;
    }

    protected static void onItemClicked(GeoActivity activity,
                                        Location location,
                                        int adapterPosition) {
        if (activity.isActivityResumed()) {
            HourlyWeatherDialog.show(
                    activity,
                    location,
                    location.getWeather().getHourlyForecast().get(adapterPosition)
            );
        }
    }

    public GeoActivity getActivity() {
        return mActivity;
    }

    public abstract boolean isValid(Location location);

    public abstract String getDisplayName(Context context);

    public abstract void bindBackgroundForHost(TrendRecyclerView host);
}