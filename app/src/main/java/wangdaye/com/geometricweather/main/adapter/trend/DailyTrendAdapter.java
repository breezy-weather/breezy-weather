package wangdaye.com.geometricweather.main.adapter.trend;

import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.Px;
import androidx.recyclerview.widget.RecyclerView;

import java.util.TimeZone;

import wangdaye.com.geometricweather.basic.GeoActivity;
import wangdaye.com.geometricweather.basic.model.option.unit.PrecipitationUnit;
import wangdaye.com.geometricweather.basic.model.option.unit.SpeedUnit;
import wangdaye.com.geometricweather.basic.model.option.unit.TemperatureUnit;
import wangdaye.com.geometricweather.basic.model.weather.Weather;
import wangdaye.com.geometricweather.main.MainThemePicker;
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

    public void temperature(GeoActivity activity, TrendRecyclerView parent,
                            @Px float parentWidth, @Px float parentHeight, int itemCountPerLine,
                            String formattedId, @NonNull Weather weather, @NonNull TimeZone timeZone,
                            ResourceProvider provider, MainThemePicker picker,
                            TemperatureUnit unit) {
        adapter = new DailyTemperatureAdapter(activity, parent, parentWidth, parentHeight, itemCountPerLine,
                formattedId, weather, timeZone, provider, picker, unit);
    }

    public void airQuality(GeoActivity activity, TrendRecyclerView parent,
                           @Px float parentWidth, @Px float parentHeight, int itemCountPerLine,
                           String formattedId, @NonNull Weather weather, @NonNull TimeZone timeZone,
                           MainThemePicker picker) {
        adapter = new DailyAirQualityAdapter(activity, parent, parentWidth, parentHeight, itemCountPerLine,
                formattedId, weather, timeZone, picker);
    }

    public void wind(GeoActivity activity, TrendRecyclerView parent,
                     @Px float parentWidth, @Px float parentHeight, int itemCountPerLine,
                     String formattedId, @NonNull Weather weather, @NonNull TimeZone timeZone,
                     MainThemePicker picker, SpeedUnit unit) {
        adapter = new DailyWindAdapter(activity, parent, parentWidth, parentHeight, itemCountPerLine,
                formattedId, weather, timeZone, picker, unit);
    }

    public void uv(GeoActivity activity, TrendRecyclerView parent,
                   @Px float parentWidth, @Px float parentHeight, int itemCountPerLine,
                   String formattedId, @NonNull Weather weather, @NonNull TimeZone timeZone,
                   MainThemePicker picker) {
        adapter = new DailyUVAdapter(activity, parent, parentWidth, parentHeight, itemCountPerLine,
                formattedId, weather, timeZone, picker);
    }

    public void precipitation(GeoActivity activity, TrendRecyclerView parent,
                              @Px float parentWidth, @Px float parentHeight, int itemCountPerLine,
                              String formattedId, @NonNull Weather weather, @NonNull TimeZone timeZone,
                              ResourceProvider provider, MainThemePicker picker, PrecipitationUnit unit) {
        adapter = new DailyPrecipitationAdapter(activity, parent, parentWidth, parentHeight, itemCountPerLine,
                formattedId, weather, timeZone, provider, picker, unit);
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

class DailyTemperatureAdapter extends wangdaye.com.geometricweather.main.adapter.trend.daily.DailyTemperatureAdapter {

    public DailyTemperatureAdapter(GeoActivity activity, TrendRecyclerView parent,
                                   @Px float parentWidth, @Px float parentHeight, int itemCountPerLine,
                                   String formattedId, @NonNull Weather weather, @NonNull TimeZone timeZone,
                                   ResourceProvider provider, MainThemePicker picker, TemperatureUnit unit) {
        super(activity, parent, parentWidth, parentHeight, itemCountPerLine, formattedId, weather, timeZone,
                true, provider, picker, unit);

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