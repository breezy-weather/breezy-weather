package com.mbestavros.geometricweather.main.adapter.trend;

import android.content.Context;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import com.mbestavros.geometricweather.basic.GeoActivity;
import com.mbestavros.geometricweather.basic.model.option.unit.PrecipitationUnit;
import com.mbestavros.geometricweather.basic.model.option.unit.TemperatureUnit;
import com.mbestavros.geometricweather.basic.model.weather.Weather;
import com.mbestavros.geometricweather.resource.provider.ResourceProvider;
import com.mbestavros.geometricweather.ui.widget.trend.TrendRecyclerView;
import com.mbestavros.geometricweather.main.adapter.trend.hourly.AbsHourlyTrendAdapter;
import com.mbestavros.geometricweather.main.adapter.trend.hourly.HourlyPrecipitationAdapter;

public class HourlyTrendAdapter  extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private @Nullable AbsHourlyTrendAdapter adapter;

    public HourlyTrendAdapter() {
        adapter = null;
    }

    public void temperature(GeoActivity activity, TrendRecyclerView parent, @NonNull Weather weather,
                            ResourceProvider provider, TemperatureUnit unit) {
        adapter = new HourlyTemperatureAdapter(activity, parent, weather, provider, unit);
    }

    public void precipitation(GeoActivity activity, TrendRecyclerView parent, @NonNull Weather weather,
                              ResourceProvider provider, PrecipitationUnit unit) {
        adapter = new HourlyPrecipitationAdapter(activity, parent, weather, provider, unit);
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

class HourlyTemperatureAdapter extends com.mbestavros.geometricweather.main.adapter.trend.hourly.HourlyTemperatureAdapter {

    private Context c;

    public HourlyTemperatureAdapter(GeoActivity activity, TrendRecyclerView parent, @NonNull Weather weather,
                                    ResourceProvider provider, TemperatureUnit unit) {
        super(activity, parent, weather, true, provider, unit);
        this.c = activity;
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
        return weather.getHourlyForecast().get(index).getTemperature().getTemperature(c, unit);
    }

    @Override
    protected String getShortTemperatureString(Weather weather, int index, TemperatureUnit unit) {
        return weather.getHourlyForecast().get(index).getTemperature().getShortTemperature(c, unit);
    }
}
