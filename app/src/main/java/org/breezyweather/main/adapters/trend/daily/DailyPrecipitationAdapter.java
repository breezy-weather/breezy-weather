package org.breezyweather.main.adapters.trend.daily;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;

import java.util.ArrayList;
import java.util.List;

import org.breezyweather.common.basic.GeoActivity;
import org.breezyweather.common.basic.models.Location;
import org.breezyweather.common.basic.models.options.unit.PrecipitationUnit;
import org.breezyweather.common.basic.models.weather.Daily;
import org.breezyweather.common.basic.models.weather.Precipitation;
import org.breezyweather.common.basic.models.weather.Weather;
import org.breezyweather.common.ui.widgets.trend.TrendRecyclerView;
import org.breezyweather.common.ui.widgets.trend.chart.DoubleHistogramView;
import org.breezyweather.theme.resource.ResourceHelper;
import org.breezyweather.theme.resource.providers.ResourceProvider;
import org.breezyweather.R;
import org.breezyweather.main.utils.MainThemeColorProvider;
import org.breezyweather.settings.SettingsManager;

/**
 * Daily precipitation adapter.
 * */
public class DailyPrecipitationAdapter extends AbsDailyTrendAdapter {

    private final ResourceProvider mResourceProvider;
    private final PrecipitationUnit mPrecipitationUnit;
    private float mHighestPrecipitation;

    class ViewHolder extends AbsDailyTrendAdapter.ViewHolder {

        private final DoubleHistogramView mDoubleHistogramView;

        ViewHolder(View itemView) {
            super(itemView);
            mDoubleHistogramView = new DoubleHistogramView(itemView.getContext());
            dailyItem.setChartItemView(mDoubleHistogramView);
        }

        @SuppressLint("SetTextI18n, InflateParams")
        void onBindView(GeoActivity activity, Location location, int position) {
            StringBuilder talkBackBuilder = new StringBuilder(activity.getString(R.string.tag_precipitation));

            super.onBindView(activity, location, talkBackBuilder, position);

            Weather weather = location.getWeather();
            assert weather != null;
            Daily daily = weather.getDailyForecast().get(position);

            Float daytimePrecipitation = null;
            Float nighttimePrecipitation = null;
            if (daily.getDay() != null && daily.getDay().getPrecipitation() != null) {
                daytimePrecipitation = daily.getDay().getPrecipitation().getTotal();
            }
            if (daily.getNight() != null && daily.getNight().getPrecipitation() != null) {
                nighttimePrecipitation = daily.getNight().getPrecipitation().getTotal();
            }

            daytimePrecipitation = daytimePrecipitation != null ? daytimePrecipitation : 0;
            nighttimePrecipitation = nighttimePrecipitation != null ? nighttimePrecipitation : 0;

            if (daytimePrecipitation != 0 || nighttimePrecipitation != 0) {
                talkBackBuilder.append(", ")
                        .append(activity.getString(R.string.daytime))
                        .append(" : ")
                        .append(mPrecipitationUnit.getValueVoice(activity, daytimePrecipitation));
                talkBackBuilder.append(", ")
                        .append(activity.getString(R.string.nighttime))
                        .append(" : ")
                        .append(mPrecipitationUnit.getValueVoice(activity, nighttimePrecipitation));
            } else {
                talkBackBuilder.append(", ")
                        .append(activity.getString(R.string.content_des_no_precipitation));
            }

            if (daily.getDay() != null && daily.getDay().getWeatherCode() != null) {
                dailyItem.setDayIconDrawable(
                        ResourceHelper.getWeatherIcon(mResourceProvider, daily.getDay().getWeatherCode(), true));
            }

            mDoubleHistogramView.setData(
                    daily.getDay() != null && daily.getDay().getPrecipitation() != null ? daily.getDay().getPrecipitation().getTotal() : null,
                    daily.getNight() != null && daily.getNight().getPrecipitation() != null ? daily.getNight().getPrecipitation().getTotal() : null,
                    mPrecipitationUnit.getValueTextWithoutUnit(daytimePrecipitation),
                    mPrecipitationUnit.getValueTextWithoutUnit(nighttimePrecipitation),
                    mHighestPrecipitation
            );
            mDoubleHistogramView.setLineColors(
                    daily.getDay() != null && daily.getDay().getPrecipitation() != null ? daily.getDay().getPrecipitation().getPrecipitationColor(activity) : Color.TRANSPARENT,
                    daily.getNight() != null && daily.getNight().getPrecipitation() != null ? daily.getNight().getPrecipitation().getPrecipitationColor(activity) : Color.TRANSPARENT,
                    MainThemeColorProvider.getColor(location, com.google.android.material.R.attr.colorOutline)
            );
            mDoubleHistogramView.setTextColors(
                    MainThemeColorProvider.getColor(location, R.attr.colorBodyText)
            );
            mDoubleHistogramView.setHistogramAlphas(1f, 0.5f);

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
    }

    public DailyPrecipitationAdapter(GeoActivity activity,
                                     Location location,
                                     ResourceProvider provider,
                                     PrecipitationUnit unit) {
        super(activity, location);

        Weather weather = location.getWeather();
        assert weather != null;
        mResourceProvider = provider;
        mPrecipitationUnit = unit;

        mHighestPrecipitation = 0;
        Float daytimePrecipitation;
        Float nighttimePrecipitation;
        for (int i = weather.getDailyForecast().size() - 1; i >= 0; i --) {
            if (weather.getDailyForecast().get(i).getDay() != null && weather.getDailyForecast().get(i).getDay().getPrecipitation() != null) {
                daytimePrecipitation = weather.getDailyForecast().get(i).getDay().getPrecipitation().getTotal();
                if (daytimePrecipitation != null && daytimePrecipitation > mHighestPrecipitation) {
                    mHighestPrecipitation = daytimePrecipitation;
                }
            }
            if (weather.getDailyForecast().get(i).getNight() != null && weather.getDailyForecast().get(i).getNight().getPrecipitation() != null) {
                nighttimePrecipitation = weather.getDailyForecast().get(i).getNight().getPrecipitation().getTotal();
                if (nighttimePrecipitation != null && nighttimePrecipitation > mHighestPrecipitation) {
                    mHighestPrecipitation = nighttimePrecipitation;
                }
            }
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
        return mHighestPrecipitation > 0;
    }

    @Override
    public String getDisplayName(Context context) {
        return context.getString(R.string.tag_precipitation);
    }

    @Override
    public void bindBackgroundForHost(TrendRecyclerView host) {
        PrecipitationUnit unit = SettingsManager.getInstance(getActivity()).getPrecipitationUnit();

        List<TrendRecyclerView.KeyLine> keyLineList = new ArrayList<>();
        keyLineList.add(
                new TrendRecyclerView.KeyLine(
                        Precipitation.PRECIPITATION_LIGHT,
                        unit.getValueTextWithoutUnit(Precipitation.PRECIPITATION_LIGHT),
                        getActivity().getString(R.string.precipitation_light),
                        TrendRecyclerView.KeyLine.ContentPosition.ABOVE_LINE
                )
        );
        keyLineList.add(
                new TrendRecyclerView.KeyLine(
                        Precipitation.PRECIPITATION_HEAVY,
                        unit.getValueTextWithoutUnit(Precipitation.PRECIPITATION_HEAVY),
                        getActivity().getString(R.string.precipitation_heavy),
                        TrendRecyclerView.KeyLine.ContentPosition.ABOVE_LINE
                )
        );
        keyLineList.add(
                new TrendRecyclerView.KeyLine(
                        -Precipitation.PRECIPITATION_LIGHT,
                        unit.getValueTextWithoutUnit(Precipitation.PRECIPITATION_LIGHT),
                        getActivity().getString(R.string.precipitation_light),
                        TrendRecyclerView.KeyLine.ContentPosition.BELOW_LINE
                )
        );
        keyLineList.add(
                new TrendRecyclerView.KeyLine(
                        -Precipitation.PRECIPITATION_HEAVY,
                        unit.getValueTextWithoutUnit(Precipitation.PRECIPITATION_HEAVY),
                        getActivity().getString(R.string.precipitation_heavy),
                        TrendRecyclerView.KeyLine.ContentPosition.BELOW_LINE
                )
        );
        host.setData(keyLineList, mHighestPrecipitation, -mHighestPrecipitation);
    }
}