package org.breezyweather.common.ui.widgets.trend.chart;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.Nullable;

public abstract class AbsChartItemView extends View {

    public AbsChartItemView(Context context) {
        super(context);
    }

    public AbsChartItemView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public AbsChartItemView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public abstract int getMarginTop();

    public abstract int getMarginBottom();
}
