package wangdaye.com.geometricweather.common.ui.widgets.trend;

import android.content.Context;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

public class TrendLayoutManager extends LinearLayoutManager {

    public TrendLayoutManager(Context context) {
        super(context, RecyclerView.HORIZONTAL, false);
    }
}
