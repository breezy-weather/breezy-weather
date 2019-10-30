package wangdaye.com.geometricweather.main.ui.adapter.hourly;

import androidx.annotation.NonNull;

import wangdaye.com.geometricweather.basic.GeoActivity;
import wangdaye.com.geometricweather.basic.model.option.unit.TemperatureUnit;
import wangdaye.com.geometricweather.basic.model.weather.Weather;
import wangdaye.com.geometricweather.main.ui.MainColorPicker;
import wangdaye.com.geometricweather.resource.provider.ResourceProvider;
import wangdaye.com.geometricweather.ui.widget.trend.TrendRecyclerView;

/**
 * Hourly temperature adapter.
 * */

public class HourlyTemperatureAdapter extends wangdaye.com.geometricweather.ui.widget.trend.adapter.HourlyTemperatureAdapter {

    public HourlyTemperatureAdapter(GeoActivity activity, TrendRecyclerView parent,
                                    float cardMarginsVertical, float cardMarginsHorizontal,
                                    int itemCountPerLine, float itemHeight,
                                    @NonNull Weather weather, int[] themeColors,
                                    ResourceProvider provider, MainColorPicker picker, TemperatureUnit unit) {
        super(activity, parent, cardMarginsVertical, cardMarginsHorizontal, itemCountPerLine, itemHeight,
                weather, themeColors, true, provider, picker, unit);
    }

    @Override
    protected int getTemperatureC(Weather weather, int index) {
        return weather.getHourlyForecast().get(index).getTemperature().getTemperature();
    }

    @Override
    protected int getTemperature(Weather weather, int index, TemperatureUnit unit) {
        return unit.getTemperature(getTemperatureC(weather, index));
    }

    @Override
    protected String getTemperatureString(Weather weather, int index, TemperatureUnit unit) {
        return weather.getHourlyForecast().get(index).getTemperature().getTemperature(unit);
    }

    @Override
    protected String getShortTemperatureString(Weather weather, int index, TemperatureUnit unit) {
        return weather.getHourlyForecast().get(index).getTemperature().getShortTemperature(unit);
    }
}