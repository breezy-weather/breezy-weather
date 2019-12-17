package wangdaye.com.geometricweather.main.ui.adapter.trend;

import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import wangdaye.com.geometricweather.basic.GeoActivity;
import wangdaye.com.geometricweather.basic.model.option.unit.PrecipitationUnit;
import wangdaye.com.geometricweather.basic.model.option.unit.TemperatureUnit;
import wangdaye.com.geometricweather.basic.model.weather.Weather;
import wangdaye.com.geometricweather.main.ui.MainColorPicker;
import wangdaye.com.geometricweather.resource.provider.ResourceProvider;
import wangdaye.com.geometricweather.ui.widget.trend.TrendRecyclerView;
import wangdaye.com.geometricweather.ui.widget.trend.adapter.hourly.AbsHourlyTrendAdapter;
import wangdaye.com.geometricweather.ui.widget.trend.adapter.hourly.HourlyPrecipitationAdapter;

public class HourlyTrendAdapter  extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private @Nullable AbsHourlyTrendAdapter adapter;

    public HourlyTrendAdapter() {
        adapter = null;
    }

    public void temperature(GeoActivity activity, TrendRecyclerView parent,
                            float cardMarginsVertical, float cardMarginsHorizontal,
                            int itemCountPerLine, float itemHeight,
                            @NonNull Weather weather,
                            int[] themeColors, ResourceProvider provider, MainColorPicker picker,
                            TemperatureUnit unit) {
        adapter = new HourlyTemperatureAdapter(activity, parent, cardMarginsVertical, cardMarginsHorizontal,
                itemCountPerLine, itemHeight, weather, themeColors, provider, picker, unit);
    }

    public void precipitation(GeoActivity activity, TrendRecyclerView parent,
                              float cardMarginsVertical, float cardMarginsHorizontal,
                              int itemCountPerLine, float itemHeight,
                              @NonNull Weather weather, int[] themeColors,
                              ResourceProvider provider, MainColorPicker picker,
                              PrecipitationUnit unit) {
        adapter = new HourlyPrecipitationAdapter(activity, parent, cardMarginsVertical, cardMarginsHorizontal,
                itemCountPerLine, itemHeight, weather, themeColors, provider, picker, unit);
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        assert adapter != null;
        return adapter.onCreateViewHolder(parent, viewType);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        assert adapter != null;
        adapter.onBindViewHolder(holder, position);
    }

    @Override
    public int getItemCount() {
        return adapter == null ? 0 : adapter.getItemCount();
    }

    @Override
    public int getItemViewType(int position) {
        if (adapter == null) {
            return 0;
        } else if (adapter instanceof HourlyTemperatureAdapter) {
            return 1;
        } else if (adapter instanceof HourlyPrecipitationAdapter) {
            return 2;
        }
        return -1;
    }

}

class HourlyTemperatureAdapter extends wangdaye.com.geometricweather.ui.widget.trend.adapter.hourly.HourlyTemperatureAdapter {

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
