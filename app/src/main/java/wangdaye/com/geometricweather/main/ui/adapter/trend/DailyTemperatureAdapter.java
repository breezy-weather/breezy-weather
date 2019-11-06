package wangdaye.com.geometricweather.main.ui.adapter.trend;

import androidx.annotation.NonNull;

import wangdaye.com.geometricweather.basic.GeoActivity;
import wangdaye.com.geometricweather.basic.model.option.unit.TemperatureUnit;
import wangdaye.com.geometricweather.basic.model.weather.Weather;
import wangdaye.com.geometricweather.main.ui.MainColorPicker;
import wangdaye.com.geometricweather.resource.provider.ResourceProvider;
import wangdaye.com.geometricweather.ui.widget.trend.TrendRecyclerView;

/**
 * Daily temperature adapter.
 * */

public class DailyTemperatureAdapter extends wangdaye.com.geometricweather.ui.widget.trend.adapter.DailyTemperatureAdapter {


    public DailyTemperatureAdapter(GeoActivity activity, TrendRecyclerView parent,
                                   float cardMarginsVertical, float cardMarginsHorizontal,
                                   int itemCountPerLine, float itemHeight,
                                   @NonNull Weather weather, int[] themeColors,
                                   ResourceProvider provider, MainColorPicker picker, TemperatureUnit unit) {
        super(activity, parent, cardMarginsVertical, cardMarginsHorizontal, itemCountPerLine, itemHeight,
                weather, themeColors, true, provider, picker, unit);
    }

    @Override
    protected int getDaytimeTemperatureC(Weather weather, int index) {
        return weather.getDailyForecast().get(index).day().getTemperature().getTemperature();
    }

    @Override
    protected int getNighttimeTemperatureC(Weather weather, int index) {
        return weather.getDailyForecast().get(index).night().getTemperature().getTemperature();
    }

    @Override
    protected int getDaytimeTemperature(Weather weather, int index, TemperatureUnit unit) {
        return unit.getTemperature(getDaytimeTemperatureC(weather, index));
    }

    @Override
    protected int getNighttimeTemperature(Weather weather, int index, TemperatureUnit unit) {
        return unit.getTemperature(getNighttimeTemperatureC(weather, index));
    }

    @Override
    protected String getDaytimeTemperatureString(Weather weather, int index, TemperatureUnit unit) {
        return weather.getDailyForecast().get(index).day().getTemperature().getTemperature(unit);
    }

    @Override
    protected String getNighttimeTemperatureString(Weather weather, int index, TemperatureUnit unit) {
        return weather.getDailyForecast().get(index).night().getTemperature().getTemperature(unit);
    }

    @Override
    protected String getShortDaytimeTemperatureString(Weather weather, int index, TemperatureUnit unit) {
        return weather.getDailyForecast().get(index).day().getTemperature().getShortTemperature(unit);
    }

    @Override
    protected String getShortNighttimeTemperatureString(Weather weather, int index, TemperatureUnit unit) {
        return weather.getDailyForecast().get(index).night().getTemperature().getShortTemperature(unit);
    }
}