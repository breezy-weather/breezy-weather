package wangdaye.com.geometricweather.main.layout;

import android.content.Context;
import android.util.AttributeSet;
import android.view.ViewGroup;

import androidx.recyclerview.widget.RecyclerView;

import wangdaye.com.geometricweather.ui.widget.trend.TrendLayoutManager;
import wangdaye.com.geometricweather.utils.DisplayUtils;

public class TrendHorizontalLinearLayoutManager extends TrendLayoutManager {

    private Context context;
    private int fillCount;

    private static final int MIN_ITEM_WIDTH = 56;
    private static final int MIN_ITEM_HEIGHT = 144;

    public TrendHorizontalLinearLayoutManager(Context context) {
        this(context, 0);
    }

    public TrendHorizontalLinearLayoutManager(Context context, int fillCount) {
        super(context);
        this.context = context;
        this.fillCount = fillCount;
    }

    @Override
    public int scrollHorizontallyBy(int dx, RecyclerView.Recycler recycler, RecyclerView.State state) {
        int consumed = super.scrollHorizontallyBy(dx, recycler, state);
        if (consumed == 0) {
            return 0;
        } else if (Math.abs(consumed) < Math.abs(dx)) {
            return dx;
        }
        return consumed;
    }

    @Override
    public RecyclerView.LayoutParams generateDefaultLayoutParams() {
        if (fillCount > 0) {
            int minWidth = (int) DisplayUtils.dpToPx(context, MIN_ITEM_WIDTH);
            int minHeight = (int) DisplayUtils.dpToPx(context, MIN_ITEM_HEIGHT);
            return new RecyclerView.LayoutParams(
                    Math.max(minWidth, getWidth() / fillCount),
                    getHeight() > minHeight ? ViewGroup.LayoutParams.MATCH_PARENT : minHeight
            );
        } else {
            return new RecyclerView.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT);
        }
    }

    @Override
    public RecyclerView.LayoutParams generateLayoutParams(Context c, AttributeSet attrs) {
        return generateDefaultLayoutParams();
    }

    @Override
    public RecyclerView.LayoutParams generateLayoutParams(ViewGroup.LayoutParams lp) {
        return generateDefaultLayoutParams();
    }
}
