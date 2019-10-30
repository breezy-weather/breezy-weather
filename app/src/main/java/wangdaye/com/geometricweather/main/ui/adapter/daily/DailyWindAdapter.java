package wangdaye.com.geometricweather.main.ui.adapter.daily;

import androidx.annotation.NonNull;

import wangdaye.com.geometricweather.basic.GeoActivity;
import wangdaye.com.geometricweather.basic.model.option.unit.SpeedUnit;
import wangdaye.com.geometricweather.basic.model.weather.Weather;
import wangdaye.com.geometricweather.main.ui.MainColorPicker;
import wangdaye.com.geometricweather.ui.widget.trend.TrendRecyclerView;

public class DailyWindAdapter extends wangdaye.com.geometricweather.ui.widget.trend.adapter.DailyWindAdapter {

    public DailyWindAdapter(GeoActivity activity, TrendRecyclerView parent,
                            float cardMarginsVertical, float cardMarginsHorizontal,
                            int itemCountPerLine, float itemHeight,
                            @NonNull Weather weather, int[] themeColors,
                            MainColorPicker picker, SpeedUnit unit) {
        super(activity, parent, cardMarginsVertical, cardMarginsHorizontal, itemCountPerLine, itemHeight,
                weather, themeColors, picker, unit);
    }
}
