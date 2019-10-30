package wangdaye.com.geometricweather.ui.widget.trend.abs;

import android.content.Context;

import androidx.annotation.Px;
import androidx.recyclerview.widget.RecyclerView;

public abstract class TrendRecyclerViewAdapter<VH extends RecyclerView.ViewHolder>
        extends RecyclerView.Adapter<VH> {

    private TrendParent trendParent;
    private @Px float cardMarginsVertical;
    private @Px float cardMarginsHorizontal;
    private int itemCountPerLine;
    private @Px float itemWidth;
    private @Px float itemHeight;

    public TrendRecyclerViewAdapter(Context context,
                                    TrendParent trendParent,
                                    @Px float cardMarginsVertical, @Px float cardMarginsHorizontal,
                                    int itemCountPerLine, @Px float itemHeight) {
        this.trendParent = trendParent;
        this.cardMarginsVertical = cardMarginsVertical;
        this.cardMarginsHorizontal = cardMarginsHorizontal;
        this.itemCountPerLine = itemCountPerLine;
        this.itemWidth = (context.getResources().getDisplayMetrics().widthPixels - 2 * cardMarginsHorizontal)
                / itemCountPerLine;
        this.itemHeight = itemHeight;
    }

    public TrendParent getTrendParent() {
        return trendParent;
    }

    public float getCardMarginsVertical() {
        return cardMarginsVertical;
    }

    public float getCardMarginsHorizontal() {
        return cardMarginsHorizontal;
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
