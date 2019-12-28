package wangdaye.com.geometricweather.ui.widget.trend.abs;

import androidx.annotation.Px;
import androidx.recyclerview.widget.RecyclerView;

public abstract class TrendRecyclerViewAdapter<VH extends RecyclerView.ViewHolder>
        extends RecyclerView.Adapter<VH> {

    private TrendParent trendParent;
    private int itemCountPerLine;
    private @Px float itemWidth;
    private @Px float itemHeight;

    public TrendRecyclerViewAdapter(TrendParent trendParent,
                                    @Px float parentWidth, @Px float parentHeight, int itemCountPerLine) {
        this.trendParent = trendParent;
        this.itemCountPerLine = itemCountPerLine;
        this.itemWidth = parentWidth / itemCountPerLine;
        this.itemHeight = parentHeight;
    }

    public TrendParent getTrendParent() {
        return trendParent;
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
