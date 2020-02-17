package wangdaye.com.geometricweather.ui.widget.trend.abs;

import androidx.recyclerview.widget.RecyclerView;

public abstract class TrendRecyclerViewAdapter<VH extends RecyclerView.ViewHolder>
        extends RecyclerView.Adapter<VH> {

    private TrendParent trendParent;

    public TrendRecyclerViewAdapter(TrendParent trendParent) {
        this.trendParent = trendParent;
    }

    public TrendParent getTrendParent() {
        return trendParent;
    }
}
