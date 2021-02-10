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

    @Nullable private AbsDailyTrendAdapter mAdapter;

    public DailyTrendAdapter() {
        mAdapter = null;
    }

    public void temperature(GeoActivity activity, TrendRecyclerView parent, Location location,
                            ResourceProvider provider, TemperatureUnit unit) {
        mAdapter = new DailyTemperatureAdapter(activity, parent, location, provider, unit);
    }

    public void airQuality(GeoActivity activity, TrendRecyclerView parent, Location location) {
        mAdapter = new DailyAirQualityAdapter(activity, parent, location);
    }

    public void wind(GeoActivity activity, TrendRecyclerView parent, Location location, SpeedUnit unit) {
        mAdapter = new DailyWindAdapter(activity, parent, location, unit);
    }

    public void uv(GeoActivity activity, TrendRecyclerView parent, Location location) {
        mAdapter = new DailyUVAdapter(activity, parent, location);
    }

    public void precipitation(GeoActivity activity, TrendRecyclerView parent, Location location,
                              ResourceProvider provider, PrecipitationUnit unit) {
        mAdapter = new DailyPrecipitationAdapter(activity, parent, location, provider, unit);
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        assert mAdapter != null;
        return mAdapter.onCreateViewHolder(parent, viewType);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        assert mAdapter != null;
        mAdapter.onBindViewHolder(holder, position);
    }

    @Override
    public int getItemCount() {
        return mAdapter == null ? 0 : mAdapter.getItemCount();
    }

    @Override
    public int getItemViewType(int position) {
        if (mAdapter == null) {
            return 0;
        } else if (mAdapter instanceof DailyTemperatureAdapter) {
            return 1;
        } else if (mAdapter instanceof DailyAirQualityAdapter) {
            return 2;
        } else if (mAdapter instanceof DailyWindAdapter) {
            return 3;
        } else if (mAdapter instanceof DailyUVAdapter) {
            return 4;
        } else if (mAdapter instanceof DailyPrecipitationAdapter) {
            return 5;
        }
        return -1;
    }
}