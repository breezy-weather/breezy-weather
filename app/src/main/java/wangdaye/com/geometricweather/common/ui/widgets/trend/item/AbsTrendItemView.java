package wangdaye.com.geometricweather.common.ui.widgets.trend.item;

import android.content.Context;
import android.os.Build;
import android.util.AttributeSet;
import android.view.ViewGroup;

import androidx.annotation.RequiresApi;

import wangdaye.com.geometricweather.common.ui.widgets.trend.chart.AbsChartItemView;

public abstract class AbsTrendItemView extends ViewGroup {

    public AbsTrendItemView(Context context) {
        super(context);
    }

    public AbsTrendItemView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public AbsTrendItemView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public AbsTrendItemView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    public abstract void setChartItemView(AbsChartItemView view);
    public abstract AbsChartItemView getChartItemView();

    public abstract int getChartTop();
    public abstract int getChartBottom();
}
