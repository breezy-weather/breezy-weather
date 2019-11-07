package wangdaye.com.geometricweather.main.ui.adapter.trend;

import androidx.annotation.NonNull;

import java.util.TimeZone;

import wangdaye.com.geometricweather.basic.GeoActivity;
import wangdaye.com.geometricweather.basic.model.weather.Weather;
import wangdaye.com.geometricweather.main.ui.MainColorPicker;
import wangdaye.com.geometricweather.ui.widget.trend.TrendRecyclerView;

public class DailyUVAdapter extends wangdaye.com.geometricweather.ui.widget.trend.adapter.DailyUVAdapter {
    public DailyUVAdapter(GeoActivity activity, TrendRecyclerView parent, float cardMarginsVertical, float cardMarginsHorizontal, int itemCountPerLine, float itemHeight, @NonNull Weather weather, @NonNull TimeZone timeZone, int[] themeColors, MainColorPicker picker) {
        super(activity, parent, cardMarginsVertical, cardMarginsHorizontal, itemCountPerLine, itemHeight, weather, timeZone, themeColors, picker);
    }
}
