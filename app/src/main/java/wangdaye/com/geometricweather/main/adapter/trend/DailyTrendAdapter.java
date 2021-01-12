package wangdaye.com.geometricweather.main.adapter.trend;

import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import wangdaye.com.geometricweather.basic.GeoActivity;
import wangdaye.com.geometricweather.basic.model.Location;
import wangdaye.com.geometricweather.basic.model.option.unit.PrecipitationUnit;
import wangdaye.com.geometricweather.basic.model.option.unit.SpeedUnit;
import wangdaye.com.geometricweather.basic.model.option.unit.TemperatureUnit;
import wangdaye.com.geometricweather.main.adapter.trend.daily.DailyTemperatureAdapter;
import wangdaye.com.geometricweather.resource.provider.ResourceProvider;
import wangdaye.com.geometricweather.ui.widget.trend.TrendRecyclerView;
import wangdaye.com.geometricweather.main.adapter.trend.daily.AbsDailyTrendAdapter;
import wangdaye.com.geometricweather.main.adapter.trend.daily.DailyAirQualityAdapter;
import wangdaye.com.geometricweather.main.adapter.trend.daily.DailyPrecipitationAdapter;
import wangdaye.com.geometricweather.main.adapter.trend.daily.DailyUVAdapter;
import wangdaye.com.geometricweather.main.adapter.trend.daily.DailyWindAdapter;

public class DailyTrendAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    @Nullable private AbsDailyTrendAdapter adapter;

    public DailyTrendAdapter() {
        adapter = null;
    }

    public void temperature(GeoActivity activity, TrendRecyclerView parent, Location location,
                            ResourceProvider provider, TemperatureUnit unit) {
        adapter = new DailyTemperatureAdapter(activity, parent, location, provider, unit);
    }

    public void airQuality(GeoActivity activity, TrendRecyclerView parent, Location location) {
        adapter = new DailyAirQualityAdapter(activity, parent, location);
    }

    public void wind(GeoActivity activity, TrendRecyclerView parent, Location location, SpeedUnit unit) {
        adapter = new DailyWindAdapter(activity, parent, location, unit);
    }

    public void uv(GeoActivity activity, TrendRecyclerView parent, Location location) {
        adapter = new DailyUVAdapter(activity, parent, location);
    }

    public void precipitation(GeoActivity activity, TrendRecyclerView parent, Location location,
                              ResourceProvider provider, PrecipitationUnit unit) {
        adapter = new DailyPrecipitationAdapter(activity, parent, location, provider, unit);
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
        } else if (adapter instanceof DailyTemperatureAdapter) {
            return 1;
        } else if (adapter instanceof DailyAirQualityAdapter) {
            return 2;
        } else if (adapter instanceof DailyWindAdapter) {
            return 3;
        } else if (adapter instanceof DailyUVAdapter) {
            return 4;
        } else if (adapter instanceof DailyPrecipitationAdapter) {
            return 5;
        }
        return -1;
    }
}