package wangdaye.com.geometricweather.main.adapter.trend.hourly;

import androidx.recyclerview.widget.RecyclerView;

import wangdaye.com.geometricweather.basic.GeoActivity;
import wangdaye.com.geometricweather.basic.model.weather.Weather;
import wangdaye.com.geometricweather.main.dialog.HourlyWeatherDialog;
import wangdaye.com.geometricweather.ui.widget.trend.abs.TrendParent;
import wangdaye.com.geometricweather.ui.widget.trend.abs.TrendRecyclerViewAdapter;
import wangdaye.com.geometricweather.utils.manager.ThemeManager;

public abstract class AbsHourlyTrendAdapter<VH extends RecyclerView.ViewHolder> extends TrendRecyclerViewAdapter<VH>  {

    private GeoActivity activity;
    private Weather weather;

    public AbsHourlyTrendAdapter(GeoActivity activity, TrendParent trendParent, Weather weather) {
        super(trendParent);
        this.activity = activity;
        this.weather = weather;
    }

    protected void onItemClicked(int adapterPosition) {
        if (activity.isForeground()) {
            HourlyWeatherDialog dialog = new HourlyWeatherDialog();
            dialog.setData(weather, adapterPosition, ThemeManager.getInstance(activity).getWeatherThemeColors()[0]);
            dialog.show(activity.getSupportFragmentManager(), null);
        }
    }
}
