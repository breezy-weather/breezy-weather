package wangdaye.com.geometricweather.main.adapter.trend;

import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import wangdaye.com.geometricweather.basic.GeoActivity;
import wangdaye.com.geometricweather.basic.model.Location;
import wangdaye.com.geometricweather.basic.model.option.unit.PrecipitationUnit;
import wangdaye.com.geometricweather.basic.model.option.unit.TemperatureUnit;
import wangdaye.com.geometricweather.main.adapter.trend.hourly.HourlyTemperatureAdapter;
import wangdaye.com.geometricweather.resource.provider.ResourceProvider;
import wangdaye.com.geometricweather.ui.widget.trend.TrendRecyclerView;
import wangdaye.com.geometricweather.main.adapter.trend.hourly.AbsHourlyTrendAdapter;
import wangdaye.com.geometricweather.main.adapter.trend.hourly.HourlyPrecipitationAdapter;

public class HourlyTrendAdapter  extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private @Nullable AbsHourlyTrendAdapter adapter;

    public HourlyTrendAdapter() {
        adapter = null;
    }

    public void temperature(GeoActivity activity, TrendRecyclerView parent, Location location,
                            ResourceProvider provider, TemperatureUnit unit) {
        adapter = new HourlyTemperatureAdapter(activity, parent, location, provider, unit);
    }

    public void precipitation(GeoActivity activity, TrendRecyclerView parent, Location location,
                              ResourceProvider provider, PrecipitationUnit unit) {
        adapter = new HourlyPrecipitationAdapter(activity, parent, location, provider, unit);
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
