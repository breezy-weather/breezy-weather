package wangdaye.com.geometricweather.main.adapter.trend.daily;

import androidx.annotation.Px;
import androidx.recyclerview.widget.RecyclerView;

import wangdaye.com.geometricweather.basic.GeoActivity;
import wangdaye.com.geometricweather.ui.widget.trend.abs.TrendParent;
import wangdaye.com.geometricweather.ui.widget.trend.abs.TrendRecyclerViewAdapter;
import wangdaye.com.geometricweather.utils.helpter.IntentHelper;

public abstract class AbsDailyTrendAdapter<VH extends RecyclerView.ViewHolder> extends TrendRecyclerViewAdapter<VH>  {

    private GeoActivity activity;
    private String formattedId;

    public AbsDailyTrendAdapter(GeoActivity activity, TrendParent trendParent, String formattedId,
                                @Px float parentWidth, @Px float parentHeight, int itemCountPerLine) {
        super(trendParent, parentWidth, parentHeight, itemCountPerLine);
        this.activity = activity;
        this.formattedId = formattedId;
    }

    protected void onItemClicked(int adapterPosition) {
        if (activity.isForeground()) {
            IntentHelper.startDailyWeatherActivity(activity, formattedId, adapterPosition);
        }
    }
}
