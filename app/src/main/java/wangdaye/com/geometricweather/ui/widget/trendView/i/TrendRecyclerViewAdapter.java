package wangdaye.com.geometricweather.ui.widget.trendView.i;

import android.content.Context;

import androidx.annotation.Px;
import androidx.recyclerview.widget.RecyclerView;

public abstract class TrendRecyclerViewAdapter<VH extends RecyclerView.ViewHolder>
        extends RecyclerView.Adapter<VH> {

    private TrendParent trendParent;
    private float marginHorizontalPx;
    private int itemCountPerLine;
    private @Px float itemWidth;
    private @Px float itemHeight;

    public TrendRecyclerViewAdapter(Context context,
                                    TrendParent trendParent, float marginHorizontalPx, int itemCountPerLine,
                                    @Px float itemHeight) {
        this.trendParent = trendParent;
        this.marginHorizontalPx = marginHorizontalPx;
        this.itemCountPerLine = itemCountPerLine;
        this.itemWidth = (context.getResources().getDisplayMetrics().widthPixels - 2 * marginHorizontalPx)
                / itemCountPerLine;
        this.itemHeight = itemHeight;
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

    public @Px float getItemWidth() {
        return itemWidth;
    }

    public @Px float getItemHeight() {
        return itemHeight;
    }
}
