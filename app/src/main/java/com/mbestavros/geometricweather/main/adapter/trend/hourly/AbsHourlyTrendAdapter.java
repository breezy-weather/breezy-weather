package com.mbestavros.geometricweather.main.adapter.trend.hourly;

import androidx.recyclerview.widget.RecyclerView;

import com.mbestavros.geometricweather.basic.GeoActivity;
import com.mbestavros.geometricweather.basic.model.weather.Weather;
import com.mbestavros.geometricweather.main.dialog.HourlyWeatherDialog;
import com.mbestavros.geometricweather.ui.widget.trend.abs.TrendParent;
import com.mbestavros.geometricweather.ui.widget.trend.abs.TrendRecyclerViewAdapter;
import com.mbestavros.geometricweather.utils.manager.ThemeManager;

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
