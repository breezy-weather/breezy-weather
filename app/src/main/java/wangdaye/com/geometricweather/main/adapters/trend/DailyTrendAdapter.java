package wangdaye.com.geometricweather.main.adapters.trend;

import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import wangdaye.com.geometricweather.common.basic.GeoActivity;
import wangdaye.com.geometricweather.common.basic.models.Location;
import wangdaye.com.geometricweather.common.basic.models.options.unit.PrecipitationUnit;
import wangdaye.com.geometricweather.common.basic.models.options.unit.SpeedUnit;
import wangdaye.com.geometricweather.common.basic.models.options.unit.TemperatureUnit;
import wangdaye.com.geometricweather.main.adapters.trend.daily.DailyTemperatureAdapter;
import wangdaye.com.geometricweather.main.utils.MainThemeManager;
import wangdaye.com.geometricweather.theme.resource.providers.ResourceProvider;
import wangdaye.com.geometricweather.common.ui.widgets.trend.TrendRecyclerView;
import wangdaye.com.geometricweather.main.adapters.trend.daily.AbsDailyTrendAdapter;
import wangdaye.com.geometricweather.main.adapters.trend.daily.DailyAirQualityAdapter;
import wangdaye.com.geometricweather.main.adapters.trend.daily.DailyPrecipitationAdapter;
import wangdaye.com.geometricweather.main.adapters.trend.daily.DailyUVAdapter;
import wangdaye.com.geometricweather.main.adapters.trend.daily.DailyWindAdapter;

public class DailyTrendAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    @Nullable private AbsDailyTrendAdapter mAdapter;

    public DailyTrendAdapter() {
        mAdapter = null;
    }

    public void temperature(GeoActivity activity, TrendRecyclerView parent, Location location,
                            ResourceProvider provider, MainThemeManager themeManager, TemperatureUnit unit) {
        mAdapter = new DailyTemperatureAdapter(activity, parent, location, provider, themeManager, unit);
    }

    public void airQuality(GeoActivity activity, TrendRecyclerView parent, Location location, MainThemeManager themeManager) {
        mAdapter = new DailyAirQualityAdapter(activity, parent, location, themeManager);
    }

    public void wind(GeoActivity activity, TrendRecyclerView parent, Location location, MainThemeManager themeManager, SpeedUnit unit) {
        mAdapter = new DailyWindAdapter(activity, parent, location, themeManager, unit);
    }

    public void uv(GeoActivity activity, TrendRecyclerView parent, Location location, MainThemeManager themeManager) {
        mAdapter = new DailyUVAdapter(activity, parent, location, themeManager);
    }

    public void precipitation(GeoActivity activity, TrendRecyclerView parent, Location location,
                              ResourceProvider provider, MainThemeManager themeManager, PrecipitationUnit unit) {
        mAdapter = new DailyPrecipitationAdapter(activity, parent, location, provider, themeManager, unit);
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