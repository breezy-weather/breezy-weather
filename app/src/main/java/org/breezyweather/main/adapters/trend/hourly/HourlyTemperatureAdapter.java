package org.breezyweather.main.adapters.trend.hourly;

import android.content.Context;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Size;

import java.util.ArrayList;
import java.util.List;

import org.breezyweather.common.basic.models.Location;
import org.breezyweather.common.basic.models.options.unit.ProbabilityUnit;
import org.breezyweather.common.basic.models.options.unit.TemperatureUnit;
import org.breezyweather.common.basic.models.weather.Hourly;
import org.breezyweather.common.basic.models.weather.Temperature;
import org.breezyweather.common.basic.models.weather.Weather;
import org.breezyweather.common.ui.widgets.trend.TrendRecyclerView;
import org.breezyweather.common.ui.widgets.trend.chart.PolylineAndHistogramView;
import org.breezyweather.theme.ThemeManager;
import org.breezyweather.theme.resource.ResourceHelper;
import org.breezyweather.theme.resource.providers.ResourceProvider;
import org.breezyweather.theme.weatherView.WeatherViewController;
import org.breezyweather.R;
import org.breezyweather.common.basic.GeoActivity;
import org.breezyweather.main.utils.MainThemeColorProvider;
import org.breezyweather.settings.SettingsManager;

/**
 * Hourly temperature adapter.
 * */

public class HourlyTemperatureAdapter extends AbsHourlyTrendAdapter {

    private final ResourceProvider mResourceProvider;
    private final TemperatureUnit mTemperatureUnit;

    private final Float[] mTemperatures;
    private Integer mHighestTemperature;
    private Integer mLowestTemperature;

    private final boolean mShowPrecipitationProbability;

    class ViewHolder extends AbsHourlyTrendAdapter.ViewHolder {

        private final PolylineAndHistogramView mPolylineAndHistogramView;

        ViewHolder(View itemView) {
            super(itemView);
            mPolylineAndHistogramView = new PolylineAndHistogramView(itemView.getContext());
            hourlyItem.setChartItemView(mPolylineAndHistogramView);
        }

        void onBindView(GeoActivity activity, Location location, int position) {
            StringBuilder talkBackBuilder = new StringBuilder(activity.getString(R.string.tag_temperature));

            super.onBindView(activity, location, talkBackBuilder, position);

            Weather weather = location.getWeather();
            assert weather != null;
            Hourly hourly = weather.getHourlyForecast().get(position);

            if (!TextUtils.isEmpty(hourly.getWeatherText())) {
                talkBackBuilder.append(", ").append(hourly.getWeatherText());
            }
            if (weather.getHourlyForecast().get(position).getTemperature() != null) {
                talkBackBuilder.append(", ").append(getTemperatureString(weather, position, mTemperatureUnit));
            }

            if (hourly.getWeatherCode() != null) {
                hourlyItem.setIconDrawable(
                        ResourceHelper.getWeatherIcon(mResourceProvider, hourly.getWeatherCode(), hourly.isDaylight())
                );
            }

            Float precipitationProbability = hourly.getPrecipitationProbability() != null ? hourly.getPrecipitationProbability().getTotal() : null;
            float p = precipitationProbability == null ? 0 : precipitationProbability;
            if (!mShowPrecipitationProbability) {
                p = 0;
            }
            mPolylineAndHistogramView.setData(
                    buildTemperatureArrayForItem(mTemperatures, position),
                    null,
                    getShortTemperatureString(weather, position, mTemperatureUnit),
                    null,
                    mHighestTemperature != null ? Float.valueOf(mHighestTemperature) : null,
                    mLowestTemperature != null ? Float.valueOf(mLowestTemperature) : null,
                    p < 5 ? null : p,
                    p < 5 ? null : ProbabilityUnit.PERCENT.getValueText(activity, (int) p),
                    100f,
                    0f
            );
            int[] themeColors = ThemeManager
                    .getInstance(itemView.getContext())
                    .getWeatherThemeDelegate()
                    .getThemeColors(
                            itemView.getContext(),
                            WeatherViewController.getWeatherKind(location.getWeather()),
                            location.isDaylight()
                    );
            boolean lightTheme = MainThemeColorProvider.isLightTheme(itemView.getContext(), location);
            mPolylineAndHistogramView.setLineColors(
                    themeColors[lightTheme ? 1 : 2],
                    themeColors[2],
                    MainThemeColorProvider.getColor(location, com.google.android.material.R.attr.colorOutline)
            );
            mPolylineAndHistogramView.setShadowColors(
                    themeColors[lightTheme ? 1 : 2],
                    themeColors[2],
                    lightTheme
            );
            mPolylineAndHistogramView.setTextColors(
                    MainThemeColorProvider.getColor(location, R.attr.colorTitleText),
                    MainThemeColorProvider.getColor(location, R.attr.colorBodyText),
                    MainThemeColorProvider.getColor(location, R.attr.colorPrecipitationProbability)
            );
            mPolylineAndHistogramView.setHistogramAlpha(lightTheme ? 0.2f : 0.5f);

            hourlyItem.setContentDescription(talkBackBuilder.toString());
        }

        @Size(3)
        private Float[] buildTemperatureArrayForItem(Float[] temps, int adapterPosition) {
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

    public HourlyTemperatureAdapter(GeoActivity activity, Location location,
                                    ResourceProvider provider, TemperatureUnit unit) {
        this(activity, location, true, provider, unit);
    }

    public HourlyTemperatureAdapter(GeoActivity activity,
                                    Location location,
                                    boolean showPrecipitationProbability,
                                    ResourceProvider provider,
                                    TemperatureUnit unit) {
        super(activity, location);

        Weather weather = location.getWeather();
        assert weather != null;
        mResourceProvider = provider;
        mTemperatureUnit = unit;

        mTemperatures = new Float[Math.max(0, weather.getHourlyForecast().size() * 2 - 1)];
        for (int i = 0; i < mTemperatures.length; i += 2) {
            mTemperatures[i] = getTemperatureC(weather, i / 2) != null ?
                    Float.valueOf(getTemperatureC(weather, i / 2)) : null;
        }
        for (int i = 1; i < mTemperatures.length; i += 2) {
            if (mTemperatures[i - 1] != null && mTemperatures[i + 1] != null) {
                mTemperatures[i] = (mTemperatures[i - 1] + mTemperatures[i + 1]) * 0.5F;
            } else {
                mTemperatures[i] = null;
            }
        }

        if (weather.getYesterday() != null) {
            if (weather.getYesterday().getDaytimeTemperature() != null) {
                mHighestTemperature = weather.getYesterday().getDaytimeTemperature();
            }
            if (weather.getYesterday().getNighttimeTemperature() != null) {
                mLowestTemperature = weather.getYesterday().getNighttimeTemperature();
            }
        }
        for (int i = 0; i < weather.getHourlyForecast().size(); i++) {
            if (getTemperatureC(weather, i) != null && (mHighestTemperature == null || getTemperatureC(weather, i) > mHighestTemperature)) {
                mHighestTemperature = getTemperatureC(weather, i);
            }
            if (getTemperatureC(weather, i) != null && (mLowestTemperature == null || getTemperatureC(weather, i) < mLowestTemperature)) {
                mLowestTemperature = getTemperatureC(weather, i);
            }
        }

        mShowPrecipitationProbability = showPrecipitationProbability;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_trend_hourly, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull AbsHourlyTrendAdapter.ViewHolder holder, int position) {
        ((ViewHolder) holder).onBindView(getActivity(), getLocation(), position);
    }

    @Override
    public int getItemCount() {
        assert getLocation().getWeather() != null;
        return getLocation().getWeather().getHourlyForecast().size();
    }

    @Override
    public boolean isValid(Location location) {
        return true;
    }

    @Override
    public String getDisplayName(Context context) {
        return context.getString(R.string.tag_temperature);
    }

    @Override
    public void bindBackgroundForHost(TrendRecyclerView host) {
        Weather weather = getLocation().getWeather();
        if (weather == null) {
            return;
        }

        if (weather.getYesterday() == null
                || weather.getYesterday().getDaytimeTemperature() == null
                || weather.getYesterday().getNighttimeTemperature() == null) {
            host.setData(null,0, 0);
        } else {
            List<TrendRecyclerView.KeyLine> keyLineList = new ArrayList<>();
            keyLineList.add(
                    new TrendRecyclerView.KeyLine(
                            weather.getYesterday().getDaytimeTemperature(),
                            Temperature.getShortTemperature(
                                    getActivity(),
                                    weather.getYesterday().getDaytimeTemperature(),
                                    SettingsManager.getInstance(getActivity()).getTemperatureUnit()
                            ),
                            getActivity().getString(R.string.short_yesterday),
                            TrendRecyclerView.KeyLine.ContentPosition.ABOVE_LINE
                    )
            );
            keyLineList.add(
                    new TrendRecyclerView.KeyLine(
                            weather.getYesterday().getNighttimeTemperature(),
                            Temperature.getShortTemperature(
                                    getActivity(),
                                    weather.getYesterday().getNighttimeTemperature(),
                                    SettingsManager.getInstance(getActivity()).getTemperatureUnit()
                            ),
                            getActivity().getString(R.string.short_yesterday),
                            TrendRecyclerView.KeyLine.ContentPosition.BELOW_LINE
                    )
            );
            host.setData(keyLineList, mHighestTemperature, mLowestTemperature);
        }
    }

    protected Integer getTemperatureC(Weather weather, int index) {
        if (weather.getHourlyForecast().get(index).getTemperature() != null) {
            return weather.getHourlyForecast().get(index).getTemperature().getTemperature();
        } else {
            return null;
        }
    }

    protected Integer getTemperature(Weather weather, int index, TemperatureUnit unit) {
        return unit.getValueWithoutUnit(getTemperatureC(weather, index));
    }

    protected String getTemperatureString(Weather weather, int index, TemperatureUnit unit) {
        if (weather.getHourlyForecast().get(index).getTemperature() != null) {
            return weather.getHourlyForecast().get(index).getTemperature().getTemperature(getActivity(), unit);
        } else {
            return null;
        }
    }

    protected String getShortTemperatureString(Weather weather, int index, TemperatureUnit unit) {
        if (weather.getHourlyForecast().get(index).getTemperature() != null) {
            return weather.getHourlyForecast().get(index).getTemperature().getShortTemperature(getActivity(), unit);
        } else {
            return null;
        }
    }
}