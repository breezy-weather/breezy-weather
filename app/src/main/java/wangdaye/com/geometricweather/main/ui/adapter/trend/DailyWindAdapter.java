package wangdaye.com.geometricweather.main.ui.adapter.trend;

import androidx.annotation.NonNull;

import java.util.TimeZone;

import wangdaye.com.geometricweather.basic.GeoActivity;
import wangdaye.com.geometricweather.basic.model.option.unit.SpeedUnit;
import wangdaye.com.geometricweather.basic.model.weather.Weather;
import wangdaye.com.geometricweather.main.ui.MainColorPicker;
import wangdaye.com.geometricweather.ui.widget.trend.TrendRecyclerView;

public class DailyWindAdapter extends wangdaye.com.geometricweather.ui.widget.trend.adapter.DailyWindAdapter {

    public DailyWindAdapter(GeoActivity activity, TrendRecyclerView parent, float cardMarginsVertical, float cardMarginsHorizontal, int itemCountPerLine, float itemHeight, String formattedId, @NonNull Weather weather, @NonNull TimeZone timeZone, int[] themeColors, MainColorPicker picker, SpeedUnit unit) {
        super(activity, parent, cardMarginsVertical, cardMarginsHorizontal, itemCountPerLine, itemHeight, formattedId, weather, timeZone, picker, unit);
    }
}
