package wangdaye.com.geometricweather.main.layout;

import android.content.Context;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

public class TrendHorizontalLinearLayoutManager extends LinearLayoutManager {

    public TrendHorizontalLinearLayoutManager(Context context) {
        super(context, RecyclerView.HORIZONTAL, false);
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
}
