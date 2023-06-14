package org.breezyweather.main.adapters.trend.daily;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;

import java.util.ArrayList;
import java.util.List;

import org.breezyweather.common.basic.GeoActivity;
import org.breezyweather.common.basic.models.Location;
import org.breezyweather.common.basic.models.options.index.PollutantIndex;
import org.breezyweather.common.basic.models.weather.Daily;
import org.breezyweather.common.basic.models.weather.Weather;
import org.breezyweather.common.ui.widgets.trend.TrendRecyclerView;
import org.breezyweather.common.ui.widgets.trend.chart.PolylineAndHistogramView;
import org.breezyweather.theme.ThemeManager;
import org.breezyweather.theme.weatherView.WeatherViewController;
import org.breezyweather.R;
import org.breezyweather.main.utils.MainThemeColorProvider;

/**
 * Daily air quality adapter.
 * */

public class DailyAirQualityAdapter extends AbsDailyTrendAdapter {

    private int mHighestIndex;

    class ViewHolder extends AbsDailyTrendAdapter.ViewHolder {

        private final PolylineAndHistogramView mPolylineAndHistogramView;

        ViewHolder(View itemView) {
            super(itemView);
            mPolylineAndHistogramView = new PolylineAndHistogramView(itemView.getContext());
            dailyItem.setChartItemView(mPolylineAndHistogramView);
        }

        @SuppressLint("DefaultLocale")
        void onBindView(GeoActivity activity,
                        Location location,
                        int position) {
            StringBuilder talkBackBuilder = new StringBuilder(activity.getString(R.string.tag_aqi));

            super.onBindView(activity, location, talkBackBuilder, position);

            assert location.getWeather() != null;
            Daily daily = location.getWeather().getDailyForecast().get(position);
            if (daily.getAirQuality() != null) {
                Integer index = daily.getAirQuality().getIndex(null);
                talkBackBuilder.append(", ").append(index).append(", ").append(daily.getAirQuality().getName(itemView.getContext(), null));
                mPolylineAndHistogramView.setData(
                        null, null,
                        null, null,
                        null, null,
                        (float) (index == null ? 0 : index),
                        String.format("%d", index == null ? 0 : index),
                        (float) mHighestIndex,
                        0f
                );
                mPolylineAndHistogramView.setLineColors(
                        daily.getAirQuality().getColor(activity, null),
                        daily.getAirQuality().getColor(activity, null),
                        MainThemeColorProvider.getColor(location, com.google.android.material.R.attr.colorOutline)
                );
            }
            int[] themeColors = ThemeManager
                    .getInstance(itemView.getContext())
                    .getWeatherThemeDelegate()
                    .getThemeColors(
                            itemView.getContext(),
                            WeatherViewController.getWeatherKind(location.getWeather()),
                            location.isDaylight()
                    );
            boolean lightTheme = MainThemeColorProvider.isLightTheme(itemView.getContext(), location);
            mPolylineAndHistogramView.setShadowColors(themeColors[1], themeColors[2], lightTheme);
            mPolylineAndHistogramView.setTextColors(
                    MainThemeColorProvider.getColor(location, R.attr.colorTitleText),
                    MainThemeColorProvider.getColor(location, R.attr.colorBodyText),
                    MainThemeColorProvider.getColor(location, R.attr.colorTitleText)
            );
            mPolylineAndHistogramView.setHistogramAlpha(lightTheme ? 1f : 0.5f);

            dailyItem.setContentDescription(talkBackBuilder.toString());
        }
    }

    public DailyAirQualityAdapter(GeoActivity activity, Location location) {
        super(activity, location);

        Weather weather = location.getWeather();
        assert weather != null;

        mHighestIndex = 0;
        for (int i = weather.getDailyForecast().size() - 1; i >= 0; i --) {
            if (weather.getDailyForecast().get(i).getAirQuality() != null) {
                Integer index = weather.getDailyForecast().get(i).getAirQuality().getIndex(null);
                if (index != null && index > mHighestIndex) {
                    mHighestIndex = index;
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
        ((ViewHolder)  holder).onBindView(getActivity(), getLocation(), position);
    }

    @Override
    public int getItemCount() {
        return getLocation().getWeather().getDailyForecast().size();
    }

    @Override
    public boolean isValid(Location location) {
        return mHighestIndex > 0;
    }

    @Override
    public String getDisplayName(Context context) {
        return context.getString(R.string.tag_aqi);
    }

    @Override
    public void bindBackgroundForHost(TrendRecyclerView host) {
        List<TrendRecyclerView.KeyLine> keyLineList = new ArrayList<>();
        Integer goodPollutionLevel = PollutantIndex.getIndexFreshAir();
        keyLineList.add(
                new TrendRecyclerView.KeyLine(
                        goodPollutionLevel,
                        String.valueOf(goodPollutionLevel),
                        getActivity().getResources().getStringArray(R.array.air_quality_levels)[1],
                        TrendRecyclerView.KeyLine.ContentPosition.ABOVE_LINE
                )
        );
        Integer moderatePollutionLevel = PollutantIndex.getIndexHighPollution();
        keyLineList.add(
                new TrendRecyclerView.KeyLine(
                        moderatePollutionLevel,
                        String.valueOf(moderatePollutionLevel),
                        getActivity().getResources().getStringArray(R.array.air_quality_levels)[3],
                        TrendRecyclerView.KeyLine.ContentPosition.ABOVE_LINE
                )
        );
        Integer heavyPollutionLevel = PollutantIndex.getIndexExcessivePollution();
        keyLineList.add(
                new TrendRecyclerView.KeyLine(
                        heavyPollutionLevel,
                        String.valueOf(heavyPollutionLevel),
                        getActivity().getResources().getStringArray(R.array.air_quality_levels)[5],
                        TrendRecyclerView.KeyLine.ContentPosition.ABOVE_LINE
                )
        );
        host.setData(keyLineList, mHighestIndex, 0);
    }
}