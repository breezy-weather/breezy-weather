package wangdaye.com.geometricweather.ui.widget.trendView.i;

import android.content.Context;

import androidx.recyclerview.widget.RecyclerView;

public abstract class TrendRecyclerViewAdapter<VH extends RecyclerView.ViewHolder>
        extends RecyclerView.Adapter<VH> {

    private TrendParent trendParent;
    private float marginHorizontalPx;
    private int itemCountPerLine;
    private float itemWidth;

    public TrendRecyclerViewAdapter(Context context,
                                    TrendParent trendParent, float marginHorizontalPx, int itemCountPerLine) {
        this.trendParent = trendParent;
        this.marginHorizontalPx = marginHorizontalPx;
        this.itemCountPerLine = itemCountPerLine;
        this.itemWidth = (context.getResources().getDisplayMetrics().widthPixels - 2 * marginHorizontalPx)
                / itemCountPerLine;
    }

    public TrendParent getTrendParent() {
        return trendParent;
    }

    public float getMarginHorizontalPx() {
        return marginHorizontalPx;
    }

    public int getItemCountPerLine() {
        return itemCountPerLine;
    }

    public float getItemWidth() {
        return itemWidth;
    }
}
