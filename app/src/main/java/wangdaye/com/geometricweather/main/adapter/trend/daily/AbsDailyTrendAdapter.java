package wangdaye.com.geometricweather.main.adapter.trend.daily;

import android.content.Context;

import androidx.recyclerview.widget.RecyclerView;

import wangdaye.com.geometricweather.basic.GeoActivity;
import wangdaye.com.geometricweather.basic.model.Location;
import wangdaye.com.geometricweather.ui.widget.trend.TrendRecyclerViewAdapter;
import wangdaye.com.geometricweather.utils.helpter.IntentHelper;

public abstract class AbsDailyTrendAdapter<VH extends RecyclerView.ViewHolder> extends TrendRecyclerViewAdapter<VH>  {

    private GeoActivity activity;

    public AbsDailyTrendAdapter(GeoActivity activity, Location location) {
        super(location);
        this.activity = activity;
    }

    protected void onItemClicked(int adapterPosition) {
        if (activity.isForeground()) {
            IntentHelper.startDailyWeatherActivity(
                    activity, getLocation().getFormattedId(), adapterPosition);
        }
    }

    public Context getContext() {
        return activity;
    }
}
