package wangdaye.com.geometricweather.main.adapter.trend.hourly;

import android.content.Context;

import androidx.recyclerview.widget.RecyclerView;

import wangdaye.com.geometricweather.basic.GeoActivity;
import wangdaye.com.geometricweather.basic.model.Location;
import wangdaye.com.geometricweather.main.dialog.HourlyWeatherDialog;
import wangdaye.com.geometricweather.ui.widget.trend.TrendRecyclerViewAdapter;
import wangdaye.com.geometricweather.utils.manager.ThemeManager;

public abstract class AbsHourlyTrendAdapter<VH extends RecyclerView.ViewHolder> extends TrendRecyclerViewAdapter<VH>  {

    private GeoActivity activity;

    public AbsHourlyTrendAdapter(GeoActivity activity, Location location) {
        super(location);
        this.activity = activity;
    }

    protected void onItemClicked(int adapterPosition) {
        if (activity.isForeground()) {
            HourlyWeatherDialog dialog = new HourlyWeatherDialog();
            dialog.setData(
                    getLocation().getWeather(),
                    adapterPosition,
                    ThemeManager.getInstance(activity).getWeatherThemeColors()[0]
            );
            dialog.show(activity.getSupportFragmentManager(), null);
        }
    }

    public Context getContext() {
        return activity;
    }
}
