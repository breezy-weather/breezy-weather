package wangdaye.com.geometricweather.main.adapters.trend.hourly;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;

import java.util.ArrayList;
import java.util.List;

import wangdaye.com.geometricweather.R;
import wangdaye.com.geometricweather.common.basic.GeoActivity;
import wangdaye.com.geometricweather.common.basic.models.Location;
import wangdaye.com.geometricweather.common.basic.models.weather.AirQuality;
import wangdaye.com.geometricweather.common.basic.models.weather.Hourly;
import wangdaye.com.geometricweather.common.basic.models.weather.Weather;
import wangdaye.com.geometricweather.common.ui.widgets.trend.TrendRecyclerView;
import wangdaye.com.geometricweather.common.ui.widgets.trend.chart.PolylineAndHistogramView;
import wangdaye.com.geometricweather.main.utils.MainThemeColorProvider;
import wangdaye.com.geometricweather.theme.ThemeManager;
import wangdaye.com.geometricweather.theme.weatherView.WeatherViewController;

/**
 * Hourly air quality adapter.
 * */

public class HourlyAirQualityAdapter extends AbsHourlyTrendAdapter {

    private int mHighestIndex;

    class ViewHolder extends AbsHourlyTrendAdapter.ViewHolder {

        private final PolylineAndHistogramView mPolylineAndHistogramView;

        ViewHolder(View itemView) {
            super(itemView);
            mPolylineAndHistogramView = new PolylineAndHistogramView(itemView.getContext());
            hourlyItem.setChartItemView(mPolylineAndHistogramView);
        }

        @SuppressLint("DefaultLocale")
        void onBindView(GeoActivity activity,
                        Location location,
                        int position) {
            StringBuilder talkBackBuilder = new StringBuilder(activity.getString(R.string.tag_aqi));

            super.onBindView(activity, location, talkBackBuilder, position);

            assert location.getWeather() != null;
            Hourly hourly = location.getWeather().getHourlyForecast().get(position);
            Integer index = hourly.getAirQuality() != null ? hourly.getAirQuality().getIndex(itemView.getContext()) : null;
            talkBackBuilder.append(", ").append(index).append(", ").append(hourly.getAirQuality().getAqiText(itemView.getContext()));
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
                    hourly.getAirQuality().getAqiColor(activity),
                    hourly.getAirQuality().getAqiColor(activity),
                    MainThemeColorProvider.getColor(location, com.google.android.material.R.attr.colorOutline)
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
            mPolylineAndHistogramView.setShadowColors(themeColors[1], themeColors[2], lightTheme);
            mPolylineAndHistogramView.setTextColors(
                    MainThemeColorProvider.getColor(location, R.attr.colorTitleText),
                    MainThemeColorProvider.getColor(location, R.attr.colorBodyText),
                    MainThemeColorProvider.getColor(location, R.attr.colorTitleText)
            );
            mPolylineAndHistogramView.setHistogramAlpha(lightTheme ? 1f : 0.5f);

            hourlyItem.setContentDescription(talkBackBuilder.toString());
        }
    }

    public HourlyAirQualityAdapter(GeoActivity activity, Location location) {
        super(activity, location);

        Weather weather = location.getWeather();
        assert weather != null;

        mHighestIndex = 0;
        for (int i = weather.getHourlyForecast().size() - 1; i >= 0; i--) {
            if (weather.getHourlyForecast().get(i).getAirQuality() != null) {
                Integer index = weather.getHourlyForecast().get(i).getAirQuality() != null ? weather.getHourlyForecast().get(i).getAirQuality().getIndex(activity) : null;
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
                .inflate(R.layout.item_trend_hourly, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull AbsHourlyTrendAdapter.ViewHolder holder, int position) {
        ((ViewHolder)  holder).onBindView(getActivity(), getLocation(), position);
    }

    @Override
    public int getItemCount() {
        return getLocation().getWeather().getHourlyForecast().size();
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
        Integer goodPollutionLevel = AirQuality.getIndexFreshAir(getActivity());
        keyLineList.add(
                new TrendRecyclerView.KeyLine(
                        goodPollutionLevel,
                        String.valueOf(goodPollutionLevel),
                        getActivity().getString(R.string.fresh_air),
                        TrendRecyclerView.KeyLine.ContentPosition.ABOVE_LINE
                )
        );
        Integer moderatePollutionLevel = AirQuality.getIndexHighPollution(getActivity());
        keyLineList.add(
                new TrendRecyclerView.KeyLine(
                        moderatePollutionLevel,
                        String.valueOf(moderatePollutionLevel),
                        getActivity().getString(R.string.high_pollution),
                        TrendRecyclerView.KeyLine.ContentPosition.ABOVE_LINE
                )
        );
        Integer heavyPollutionLevel = AirQuality.getIndexExcessivePollution(getActivity());
        keyLineList.add(
                new TrendRecyclerView.KeyLine(
                        heavyPollutionLevel,
                        String.valueOf(heavyPollutionLevel),
                        getActivity().getString(R.string.excessive_pollution),
                        TrendRecyclerView.KeyLine.ContentPosition.ABOVE_LINE
                )
        );
        host.setData(keyLineList, mHighestIndex, 0);
    }
}