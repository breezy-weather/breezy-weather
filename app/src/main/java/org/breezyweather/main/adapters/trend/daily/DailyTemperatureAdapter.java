package org.breezyweather.main.adapters.trend.daily;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Size;

import java.util.ArrayList;
import java.util.List;

import org.breezyweather.common.basic.GeoActivity;
import org.breezyweather.common.basic.models.Location;
import org.breezyweather.common.basic.models.options.unit.ProbabilityUnit;
import org.breezyweather.common.basic.models.options.unit.TemperatureUnit;
import org.breezyweather.common.basic.models.weather.Daily;
import org.breezyweather.common.basic.models.weather.Temperature;
import org.breezyweather.common.basic.models.weather.Weather;
import org.breezyweather.common.ui.widgets.trend.TrendRecyclerView;
import org.breezyweather.common.ui.widgets.trend.chart.PolylineAndHistogramView;
import org.breezyweather.theme.ThemeManager;
import org.breezyweather.theme.resource.ResourceHelper;
import org.breezyweather.theme.resource.providers.ResourceProvider;
import org.breezyweather.theme.weatherView.WeatherViewController;
import org.breezyweather.R;
import org.breezyweather.main.utils.MainThemeColorProvider;
import org.breezyweather.settings.SettingsManager;

/**
 * Daily temperature adapter.
 * */

public class DailyTemperatureAdapter extends AbsDailyTrendAdapter {

    private final ResourceProvider mResourceProvider;
    private final TemperatureUnit mTemperatureUnit;

    private final Float[] mDaytimeTemperatures;
    private final Float[] mNighttimeTemperatures;
    private Integer mHighestTemperature;
    private Integer mLowestTemperature;

    private final boolean mShowPrecipitationProbability;

    class ViewHolder extends AbsDailyTrendAdapter.ViewHolder {

        private final PolylineAndHistogramView mPolylineAndHistogramView;

        ViewHolder(View itemView) {
            super(itemView);
            mPolylineAndHistogramView = new PolylineAndHistogramView(itemView.getContext());
            dailyItem.setChartItemView(mPolylineAndHistogramView);
        }

        @SuppressLint("SetTextI18n, InflateParams")
        void onBindView(GeoActivity activity, Location location, int position) {
            StringBuilder talkBackBuilder = new StringBuilder(activity.getString(R.string.tag_temperature));

            super.onBindView(activity, location, talkBackBuilder, position);

            Weather weather = location.getWeather();
            assert weather != null;
            Daily daily = weather.getDailyForecast().get(position);

            if (daily.getDay() != null) {
                talkBackBuilder
                        .append(", ").append(activity.getString(R.string.daytime))
                        .append(" : ").append(daily.getDay().getWeatherText())
                        .append(", ").append(getDaytimeTemperatureString(weather, position, mTemperatureUnit));
            }

            if (daily.getNight() != null) {
                talkBackBuilder
                        .append(", ").append(activity.getString(R.string.nighttime))
                        .append(" : ").append(daily.getNight().getWeatherText())
                        .append(", ").append(getNighttimeTemperatureString(weather, position, mTemperatureUnit));
            }

            if (daily.getDay() != null && daily.getDay().getWeatherCode() != null) {
                dailyItem.setDayIconDrawable(
                        ResourceHelper.getWeatherIcon(
                                mResourceProvider,
                                daily.getDay().getWeatherCode(),
                                true
                        )
                );
            }

            Float daytimePrecipitationProbability = null;
            if (daily.getDay() != null && daily.getDay().getPrecipitationProbability() != null) {
                daytimePrecipitationProbability = daily.getDay().getPrecipitationProbability().getTotal();
            }
            Float nighttimePrecipitationProbability = null;
            if (daily.getNight() != null && daily.getNight().getPrecipitationProbability() != null) {
                nighttimePrecipitationProbability = daily.getNight().getPrecipitationProbability().getTotal();
            }
            float p = Math.max(
                    daytimePrecipitationProbability == null ? 0 : daytimePrecipitationProbability,
                    nighttimePrecipitationProbability == null ? 0 : nighttimePrecipitationProbability
            );
            if (!mShowPrecipitationProbability) {
                p = 0;
            }
            mPolylineAndHistogramView.setData(
                    buildTemperatureArrayForItem(mDaytimeTemperatures, position),
                    buildTemperatureArrayForItem(mNighttimeTemperatures, position),
                    getShortDaytimeTemperatureString(weather, position, mTemperatureUnit),
                    getShortNighttimeTemperatureString(weather, position, mTemperatureUnit),
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
                    themeColors[1],
                    themeColors[2],
                    MainThemeColorProvider.getColor(location, com.google.android.material.R.attr.colorOutline)
            );
            mPolylineAndHistogramView.setShadowColors(
                    themeColors[1],
                    themeColors[2],
                    lightTheme
            );
            mPolylineAndHistogramView.setTextColors(
                    MainThemeColorProvider.getColor(location, R.attr.colorTitleText),
                    MainThemeColorProvider.getColor(location, R.attr.colorBodyText),
                    MainThemeColorProvider.getColor(location, R.attr.colorPrecipitationProbability)
            );
            mPolylineAndHistogramView.setHistogramAlpha(lightTheme ? 0.2f : 0.5f);

            if (daily.getNight() != null && daily.getNight().getWeatherCode() != null) {
                dailyItem.setNightIconDrawable(
                        ResourceHelper.getWeatherIcon(
                                mResourceProvider,
                                daily.getNight().getWeatherCode(),
                                false
                        )
                );
            }

            dailyItem.setContentDescription(talkBackBuilder.toString());
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

    public DailyTemperatureAdapter(GeoActivity activity,
                                   Location location,
                                   ResourceProvider provider,
                                   TemperatureUnit unit) {
        this(activity, location, true, provider, unit);
    }

    public DailyTemperatureAdapter(GeoActivity activity,
                                   Location location,
                                   boolean showPrecipitationProbability,
                                   ResourceProvider provider,
                                   TemperatureUnit unit) {
        super(activity, location);

        Weather weather = location.getWeather();
        assert weather != null;
        mResourceProvider = provider;
        mTemperatureUnit = unit;

        mDaytimeTemperatures = new Float[Math.max(0, weather.getDailyForecast().size() * 2 - 1)];
        for (int i = 0; i < mDaytimeTemperatures.length; i += 2) {
            mDaytimeTemperatures[i] = getDaytimeTemperatureC(weather, i / 2) != null
                    ? Float.valueOf(getDaytimeTemperatureC(weather, i / 2))
                    : null;
        }
        for (int i = 1; i < mDaytimeTemperatures.length; i += 2) {
            if (mDaytimeTemperatures[i - 1] != null && mDaytimeTemperatures[i + 1] != null) {
                mDaytimeTemperatures[i] = (mDaytimeTemperatures[i - 1] + mDaytimeTemperatures[i + 1]) * 0.5F;
            } else {
                mDaytimeTemperatures[i] = null;
            }
        }

        mNighttimeTemperatures = new Float[Math.max(0, weather.getDailyForecast().size() * 2 - 1)];
        for (int i = 0; i < mNighttimeTemperatures.length; i += 2) {
            mNighttimeTemperatures[i] = getNighttimeTemperatureC(weather, i / 2) != null
                    ? Float.valueOf(getNighttimeTemperatureC(weather, i / 2))
                    : null;
        }
        for (int i = 1; i < mNighttimeTemperatures.length; i += 2) {
            if (mNighttimeTemperatures[i - 1] != null && mNighttimeTemperatures[i + 1] != null) {
                mNighttimeTemperatures[i] = (mNighttimeTemperatures[i - 1] + mNighttimeTemperatures[i + 1]) * 0.5F;
            } else {
                mNighttimeTemperatures[i] = null;
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
        for (int i = 0; i < weather.getDailyForecast().size(); i++) {
            if (getDaytimeTemperatureC(weather, i) != null && (mHighestTemperature == null || getDaytimeTemperatureC(weather, i) > mHighestTemperature)) {
                mHighestTemperature = getDaytimeTemperatureC(weather, i);
            }
            if (getNighttimeTemperatureC(weather, i) != null && (mLowestTemperature == null || getNighttimeTemperatureC(weather, i) < mLowestTemperature)) {
                mLowestTemperature = getNighttimeTemperatureC(weather, i);
            }
        }

        mShowPrecipitationProbability = showPrecipitationProbability;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_trend_daily, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull AbsDailyTrendAdapter.ViewHolder holder, int position) {
        ((ViewHolder) holder).onBindView(getActivity(), getLocation(), position);
    }

    @Override
    public int getItemCount() {
        assert getLocation().getWeather() != null;
        return getLocation().getWeather().getDailyForecast().size();
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

        if (weather.getYesterday() == null) {
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
                            getActivity().getString(R.string.yesterday),
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
                            getActivity().getString(R.string.yesterday),
                            TrendRecyclerView.KeyLine.ContentPosition.BELOW_LINE
                    )
            );
            host.setData(keyLineList, mHighestTemperature, mLowestTemperature);
        }
    }

    protected Integer getDaytimeTemperatureC(Weather weather, int index) {
        if (weather.getDailyForecast().get(index).getDay() != null
            && weather.getDailyForecast().get(index).getDay().getTemperature() != null) {
            return weather.getDailyForecast().get(index).getDay().getTemperature().getTemperature();
        } else {
            return null;
        }
    }

    protected Integer getNighttimeTemperatureC(Weather weather, int index) {
        if (weather.getDailyForecast().get(index).getNight() != null
                && weather.getDailyForecast().get(index).getNight().getTemperature() != null) {
            return weather.getDailyForecast().get(index).getNight().getTemperature().getTemperature();
        } else {
            return null;
        }
    }

    protected Integer getDaytimeTemperature(Weather weather, int index, TemperatureUnit unit) {
        return unit.getValueWithoutUnit(getDaytimeTemperatureC(weather, index));
    }

    protected Integer getNighttimeTemperature(Weather weather, int index, TemperatureUnit unit) {
        return unit.getValueWithoutUnit(getNighttimeTemperatureC(weather, index));
    }

    protected String getDaytimeTemperatureString(Weather weather, int index, TemperatureUnit unit) {
        if (weather.getDailyForecast().get(index).getDay() != null
                && weather.getDailyForecast().get(index).getDay().getTemperature() != null) {
            return weather.getDailyForecast().get(index).getDay().getTemperature().getTemperature(getActivity(), unit);
        } else {
            return null;
        }
    }

    protected String getNighttimeTemperatureString(Weather weather, int index, TemperatureUnit unit) {
        if (weather.getDailyForecast().get(index).getNight() != null
                && weather.getDailyForecast().get(index).getNight().getTemperature() != null) {
            return weather.getDailyForecast().get(index).getNight().getTemperature().getTemperature(getActivity(), unit);
        } else {
            return null;
        }
    }

    protected String getShortDaytimeTemperatureString(Weather weather, int index, TemperatureUnit unit) {
        if (weather.getDailyForecast().get(index).getDay() != null
                && weather.getDailyForecast().get(index).getDay().getTemperature() != null) {
            return weather.getDailyForecast().get(index).getDay().getTemperature().getShortTemperature(getActivity(), unit);
        } else {
            return null;
        }
    }

    protected String getShortNighttimeTemperatureString(Weather weather, int index, TemperatureUnit unit) {
        if (weather.getDailyForecast().get(index).getNight() != null
                && weather.getDailyForecast().get(index).getNight().getTemperature() != null) {
            return weather.getDailyForecast().get(index).getNight().getTemperature().getShortTemperature(getActivity(), unit);
        } else {
            return null;
        }
    }
}