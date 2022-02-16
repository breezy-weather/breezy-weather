package wangdaye.com.geometricweather.main.adapters.trend.hourly;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Size;

import java.util.ArrayList;
import java.util.List;

import wangdaye.com.geometricweather.R;
import wangdaye.com.geometricweather.common.basic.GeoActivity;
import wangdaye.com.geometricweather.common.basic.models.Location;
import wangdaye.com.geometricweather.common.basic.models.options.unit.ProbabilityUnit;
import wangdaye.com.geometricweather.common.basic.models.options.unit.TemperatureUnit;
import wangdaye.com.geometricweather.common.basic.models.weather.Hourly;
import wangdaye.com.geometricweather.common.basic.models.weather.Temperature;
import wangdaye.com.geometricweather.common.basic.models.weather.Weather;
import wangdaye.com.geometricweather.main.utils.MainThemeManager;
import wangdaye.com.geometricweather.theme.resource.ResourceHelper;
import wangdaye.com.geometricweather.theme.resource.providers.ResourceProvider;
import wangdaye.com.geometricweather.common.ui.widgets.trend.TrendRecyclerView;
import wangdaye.com.geometricweather.common.ui.widgets.trend.chart.PolylineAndHistogramView;

/**
 * Hourly temperature adapter.
 * */

public class HourlyTemperatureAdapter extends AbsHourlyTrendAdapter<HourlyTemperatureAdapter.ViewHolder> {

    private final ResourceProvider mResourceProvider;
    private final MainThemeManager mThemeManager;
    private final TemperatureUnit mTemperatureUnit;

    private final float[] mTemperatures;
    private int mHighestTemperature;
    private int mLowestTemperature;

    private final boolean mShowPrecipitationProbability;

    class ViewHolder extends AbsHourlyTrendAdapter.ViewHolder {

        private final PolylineAndHistogramView mPolylineAndHistogramView;

        ViewHolder(View itemView) {
            super(itemView);
            mPolylineAndHistogramView = new PolylineAndHistogramView(itemView.getContext());
            hourlyItem.setChartItemView(mPolylineAndHistogramView);
        }

        void onBindView(GeoActivity activity, Location location, MainThemeManager themeManager, int position) {
            StringBuilder talkBackBuilder = new StringBuilder(activity.getString(R.string.tag_temperature));

            super.onBindView(activity, location, themeManager, talkBackBuilder, position);

            Weather weather = location.getWeather();
            assert weather != null;
            Hourly hourly = weather.getHourlyForecast().get(position);

            talkBackBuilder
                    .append(", ").append(hourly.getWeatherText())
                    .append(", ").append(getTemperatureString(weather, position, mTemperatureUnit));

            hourlyItem.setIconDrawable(
                    ResourceHelper.getWeatherIcon(mResourceProvider, hourly.getWeatherCode(), hourly.isDaylight())
            );

            Float precipitationProbability = hourly.getPrecipitationProbability().getTotal();
            float p = precipitationProbability == null ? 0 : precipitationProbability;
            if (!mShowPrecipitationProbability) {
                p = 0;
            }
            mPolylineAndHistogramView.setData(
                    buildTemperatureArrayForItem(mTemperatures, position),
                    null,
                    getShortTemperatureString(weather, position, mTemperatureUnit),
                    null,
                    (float) mHighestTemperature,
                    (float) mLowestTemperature,
                    p < 5 ? null : p,
                    p < 5 ? null : ProbabilityUnit.PERCENT.getProbabilityText(activity, p),
                    100f,
                    0f
            );
            int[] themeColors = mThemeManager.getWeatherThemeColors();
            mPolylineAndHistogramView.setLineColors(
                    themeColors[mThemeManager.isLightTheme() ? 1 : 2], themeColors[2], mThemeManager.getLineColor(activity));
            mPolylineAndHistogramView.setShadowColors(
                    themeColors[mThemeManager.isLightTheme() ? 1 : 2], themeColors[2], mThemeManager.isLightTheme());
            mPolylineAndHistogramView.setTextColors(
                    mThemeManager.getTextContentColor(activity),
                    mThemeManager.getTextSubtitleColor(activity)
            );
            mPolylineAndHistogramView.setHistogramAlpha(mThemeManager.isLightTheme() ? 0.2f : 0.5f);

            hourlyItem.setContentDescription(talkBackBuilder.toString());
        }

        @Size(3)
        private Float[] buildTemperatureArrayForItem(float[] temps, int adapterPosition) {
            Float[] a = new Float[3];
            a[1] = temps[2 * adapterPosition];
            if (2 * adapterPosition - 1 < 0) {
                a[0] = null;
            } else {
                a[0] = temps[2 * adapterPosition - 1];
            }
            if (2 * adapterPosition + 1 >= temps.length) {
                a[2] = null;
            } else {
                a[2] = temps[2 * adapterPosition + 1];
            }
            return a;
        }
    }

    public HourlyTemperatureAdapter(GeoActivity activity, TrendRecyclerView parent, Location location,
                                    ResourceProvider provider, MainThemeManager themeManager,
                                    TemperatureUnit unit) {
        this(activity, parent, location, true, provider, themeManager, unit);
    }

    public HourlyTemperatureAdapter(GeoActivity activity,
                                    TrendRecyclerView parent,
                                    Location location,
                                    boolean showPrecipitationProbability,
                                    ResourceProvider provider,
                                    MainThemeManager themeManager,
                                    TemperatureUnit unit) {
        super(activity, location);

        Weather weather = location.getWeather();
        assert weather != null;
        mResourceProvider = provider;
        mThemeManager = themeManager;
        mTemperatureUnit = unit;

        mTemperatures = new float[Math.max(0, weather.getHourlyForecast().size() * 2 - 1)];
        for (int i = 0; i < mTemperatures.length; i += 2) {
            mTemperatures[i] = getTemperatureC(weather, i / 2);
        }
        for (int i = 1; i < mTemperatures.length; i += 2) {
            mTemperatures[i] = (mTemperatures[i - 1] + mTemperatures[i + 1]) * 0.5F;
        }

        mHighestTemperature = weather.getYesterday() == null
                ? Integer.MIN_VALUE
                : weather.getYesterday().getDaytimeTemperature();
        mLowestTemperature = weather.getYesterday() == null
                ? Integer.MAX_VALUE
                : weather.getYesterday().getNighttimeTemperature();
        for (int i = 0; i < weather.getHourlyForecast().size(); i ++) {
            if (getTemperatureC(weather, i) > mHighestTemperature) {
                mHighestTemperature = getTemperatureC(weather, i);
            }
            if (getTemperatureC(weather, i) < mLowestTemperature) {
                mLowestTemperature = getTemperatureC(weather, i);
            }
        }

        mShowPrecipitationProbability = showPrecipitationProbability;

        parent.setLineColor(mThemeManager.getLineColor(activity));
        if (weather.getYesterday() == null) {
            parent.setData(null,0, 0);
        } else {
            List<TrendRecyclerView.KeyLine> keyLineList = new ArrayList<>();
            keyLineList.add(
                    new TrendRecyclerView.KeyLine(
                            weather.getYesterday().getDaytimeTemperature(),
                            Temperature.getShortTemperature(
                                    activity,
                                    weather.getYesterday().getDaytimeTemperature(),
                                    unit
                            ),
                            activity.getString(R.string.yesterday),
                            TrendRecyclerView.KeyLine.ContentPosition.ABOVE_LINE
                    )
            );
            keyLineList.add(
                    new TrendRecyclerView.KeyLine(
                            weather.getYesterday().getNighttimeTemperature(),
                            Temperature.getShortTemperature(
                                    activity,
                                    weather.getYesterday().getNighttimeTemperature(),
                                    unit
                            ),
                            activity.getString(R.string.yesterday),
                            TrendRecyclerView.KeyLine.ContentPosition.BELOW_LINE
                    )
            );
            parent.setData(keyLineList, mHighestTemperature, mLowestTemperature);
        }
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_trend_hourly, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.onBindView(getActivity(), getLocation(), mThemeManager, position);
    }

    @Override
    public int getItemCount() {
        assert getLocation().getWeather() != null;
        return getLocation().getWeather().getHourlyForecast().size();
    }

    protected int getTemperatureC(Weather weather, int index) {
        return weather.getHourlyForecast().get(index).getTemperature().getTemperature();
    }

    protected int getTemperature(Weather weather, int index, TemperatureUnit unit) {
        return unit.getTemperature(getTemperatureC(weather, index));
    }

    protected String getTemperatureString(Weather weather, int index, TemperatureUnit unit) {
        return weather.getHourlyForecast().get(index).getTemperature().getTemperature(getActivity(), unit);
    }

    protected String getShortTemperatureString(Weather weather, int index, TemperatureUnit unit) {
        return weather.getHourlyForecast().get(index).getTemperature().getShortTemperature(getActivity(), unit);
    }
}