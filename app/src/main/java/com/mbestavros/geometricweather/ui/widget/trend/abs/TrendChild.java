package com.mbestavros.geometricweather.ui.widget.trend.abs;

import androidx.annotation.NonNull;

public interface TrendChild {

    void setParent(@NonNull TrendParent parent);

    void setChartItemView(ChartItemView t);
    ChartItemView getChartItemView();
}
