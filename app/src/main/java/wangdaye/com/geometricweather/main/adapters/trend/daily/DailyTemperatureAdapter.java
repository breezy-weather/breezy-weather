package wangdaye.com.geometricweather.main.adapters.trend.daily;

import android.annotation.SuppressLint;
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
import wangdaye.com.geometricweather.common.basic.models.weather.Daily;
import wangdaye.com.geometricweather.common.basic.models.weather.Temperature;
import wangdaye.com.geometricweather.common.basic.models.weather.Weather;
import wangdaye.com.geometricweather.main.utils.MainThemeManager;
import wangdaye.com.geometricweather.theme.resource.ResourceHelper;
import wangdaye.com.geometricweather.theme.resource.providers.ResourceProvider;
import wangdaye.com.geometricweather.common.ui.widgets.trend.TrendRecyclerView;
import wangdaye.com.geometricweather.common.ui.widgets.trend.chart.PolylineAndHistogramView;

/**
 * Daily temperature adapter.
 * */

public class DailyTemperatureAdapter extends AbsDailyTrendAdapter<DailyTemperatureAdapter.ViewHolder> {

    private final ResourceProvider mResourceProvider;
    private final MainThemeManager mThemeManager;
    private final TemperatureUnit mTemperatureUnit;

    private final float[] mDaytimeTemperatures;
    private final float[] mNighttimeTemperatures;
    private int mHighestTemperature;
    private int mLowestTemperature;

    private final boolean mShowPrecipitationProbability;

    class ViewHolder extends AbsDailyTrendAdapter.ViewHolder {

        private final PolylineAndHistogramView mPolylineAndHistogramView;

        ViewHolder(View itemView) {
            super(itemView);
            mPolylineAndHistogramView = new PolylineAndHistogramView(itemView.getContext());
            dailyItem.setChartItemView(mPolylineAndHistogramView);
        }

        @SuppressLint("SetTextI18n, InflateParams")
        void onBindView(GeoActivity activity, Location location, MainThemeManager themeManager, int position) {
            StringBuilder talkBackBuilder = new StringBuilder(activity.getString(R.string.tag_temperature));

            super.onBindView(activity, location, themeManager, talkBackBuilder, position);

            Weather weather = location.getWeather();
            assert weather != null;
            Daily daily = weather.getDailyForecast().get(position);

            talkBackBuilder
                    .append(", ").append(activity.getString(R.string.daytime))
                    .append(" : ").append(daily.day().getWeatherText())
                    .append(", ").append(getDaytimeTemperatureString(weather, position, mTemperatureUnit))
                    .append(", ").append(activity.getString(R.string.nighttime))
                    .append(" : ").append(daily.night().getWeatherText())
                    .append(", ").append(getNighttimeTemperatureString(weather, position, mTemperatureUnit));

            dailyItem.setDayIconDrawable(
                    ResourceHelper.getWeatherIcon(mResourceProvider, daily.day().getWeatherCode(), true));

            Float daytimePrecipitationProbability = daily.day().getPrecipitationProbability().getTotal();
            Float nighttimePrecipitationProbability = daily.night().getPrecipitationProbability().getTotal();
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
                    (float) mHighestTemperature,
                    (float) mLowestTemperature,
                    p < 5 ? null : p,
                    p < 5 ? null : ProbabilityUnit.PERCENT.getProbabilityText(activity, p),
                    100f,
                    0f
            );
            int[] themeColors = mThemeManager.getWeatherThemeColors();
            mPolylineAndHistogramView.setLineColors(
                    themeColors[1], themeColors[2], mThemeManager.getLineColor(activity));
            mPolylineAndHistogramView.setShadowColors(
                    themeColors[1], themeColors[2], mThemeManager.isLightTheme());
            mPolylineAndHistogramView.setTextColors(
                    mThemeManager.getTextContentColor(activity),
                    mThemeManager.getTextSubtitleColor(activity)
            );
            mPolylineAndHistogramView.setHistogramAlpha(mThemeManager.isLightTheme() ? 0.2f : 0.5f);

            dailyItem.setNightIconDrawable(
                    ResourceHelper.getWeatherIcon(mResourceProvider, daily.night().getWeatherCode(), false));

            dailyItem.setContentDescription(talkBackBuilder.toString());
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

    @SuppressLint("SimpleDateFormat")
    public DailyTemperatureAdapter(GeoActivity activity,
                                   TrendRecyclerView parent,
                                   Location location,
                                   ResourceProvider provider,
                                   MainThemeManager themeManager,
                                   TemperatureUnit unit) {
        this(activity, parent, location, true, provider, themeManager, unit);
    }

    @SuppressLint("SimpleDateFormat")
    public DailyTemperatureAdapter(GeoActivity activity,
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

        mDaytimeTemperatures = new float[Math.max(0, weather.getDailyForecast().size() * 2 - 1)];
        for (int i = 0; i < mDaytimeTemperatures.length; i += 2) {
            mDaytimeTemperatures[i] = getDaytimeTemperatureC(weather, i / 2);
        }
        for (int i = 1; i < mDaytimeTemperatures.length; i += 2) {
            mDaytimeTemperatures[i] = (mDaytimeTemperatures[i - 1] + mDaytimeTemperatures[i + 1]) * 0.5F;
        }

        mNighttimeTemperatures = new float[Math.max(0, weather.getDailyForecast().size() * 2 - 1)];
        for (int i = 0; i < mNighttimeTemperatures.length; i += 2) {
            mNighttimeTemperatures[i] = getNighttimeTemperatureC(weather, i / 2);
        }
        for (int i = 1; i < mNighttimeTemperatures.length; i += 2) {
            mNighttimeTemperatures[i] = (mNighttimeTemperatures[i - 1] + mNighttimeTemperatures[i + 1]) * 0.5F;
        }

        mHighestTemperature = weather.getYesterday() == null
                ? Integer.MIN_VALUE
                : weather.getYesterday().getDaytimeTemperature();
        mLowestTemperature = weather.getYesterday() == null
                ? Integer.MAX_VALUE
                : weather.getYesterday().getNighttimeTemperature();
        for (int i = 0; i < weather.getDailyForecast().size(); i ++) {
            if (getDaytimeTemperatureC(weather, i) > mHighestTemperature) {
                mHighestTemperature = getDaytimeTemperatureC(weather, i);
            }
            if (getNighttimeTemperatureC(weather, i) < mLowestTemperature) {
                mLowestTemperature = getNighttimeTemperatureC(weather, i);
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
                .inflate(R.layout.item_trend_daily, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.onBindView(getActivity(), getLocation(), mThemeManager, position);
    }

    @Override
    public int getItemCount() {
        assert getLocation().getWeather() != null;
        return getLocation().getWeather().getDailyForecast().size();
    }

    protected int getDaytimeTemperatureC(Weather weather, int index) {
        return weather.getDailyForecast().get(index).day().getTemperature().getTemperature();
    }

    protected int getNighttimeTemperatureC(Weather weather, int index) {
        return weather.getDailyForecast().get(index).night().getTemperature().getTemperature();
    }

    protected int getDaytimeTemperature(Weather weather, int index, TemperatureUnit unit) {
        return unit.getTemperature(getDaytimeTemperatureC(weather, index));
    }

    protected int getNighttimeTemperature(Weather weather, int index, TemperatureUnit unit) {
        return unit.getTemperature(getNighttimeTemperatureC(weather, index));
    }

    protected String getDaytimeTemperatureString(Weather weather, int index, TemperatureUnit unit) {
        return weather.getDailyForecast().get(index).day().getTemperature().getTemperature(getActivity(), unit);
    }

    protected String getNighttimeTemperatureString(Weather weather, int index, TemperatureUnit unit) {
        return weather.getDailyForecast().get(index).night().getTemperature().getTemperature(getActivity(), unit);
    }

    protected String getShortDaytimeTemperatureString(Weather weather, int index, TemperatureUnit unit) {
        return weather.getDailyForecast().get(index).day().getTemperature().getShortTemperature(getActivity(), unit);
    }

    protected String getShortNighttimeTemperatureString(Weather weather, int index, TemperatureUnit unit) {
        return weather.getDailyForecast().get(index).night().getTemperature().getShortTemperature(getActivity(), unit);
    }
}