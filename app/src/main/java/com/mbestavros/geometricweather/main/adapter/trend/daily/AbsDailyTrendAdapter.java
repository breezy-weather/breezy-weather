package com.mbestavros.geometricweather.main.adapter.trend.daily;

import androidx.recyclerview.widget.RecyclerView;

import com.mbestavros.geometricweather.basic.GeoActivity;
import com.mbestavros.geometricweather.ui.widget.trend.abs.TrendParent;
import com.mbestavros.geometricweather.ui.widget.trend.abs.TrendRecyclerViewAdapter;
import com.mbestavros.geometricweather.utils.helpter.IntentHelper;

public abstract class AbsDailyTrendAdapter<VH extends RecyclerView.ViewHolder> extends TrendRecyclerViewAdapter<VH>  {

    private GeoActivity activity;
    private String formattedId;

    public AbsDailyTrendAdapter(GeoActivity activity, TrendParent trendParent, String formattedId) {
        super(trendParent);
        this.activity = activity;
        this.formattedId = formattedId;
    }

    protected void onItemClicked(int adapterPosition) {
        if (activity.isForeground()) {
            IntentHelper.startDailyWeatherActivity(activity, formattedId, adapterPosition);
        }
    }
}
