package wangdaye.com.geometricweather.ui.widget.trend.abs;

import androidx.annotation.NonNull;
import androidx.annotation.Px;

public interface TrendChild {

    void setParent(@NonNull TrendParent parent);

    void setChartItemView(ChartItemView t);
    ChartItemView getChartItemView();

    void setWidth(@Px float width);
    void setHeight(@Px float height);
}
