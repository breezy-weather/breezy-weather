package wangdaye.com.geometricweather.main.adapters.trend.daily;

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
import wangdaye.com.geometricweather.common.basic.models.weather.Daily;
import wangdaye.com.geometricweather.common.basic.models.weather.Weather;
import wangdaye.com.geometricweather.common.ui.widgets.trend.TrendRecyclerView;
import wangdaye.com.geometricweather.common.ui.widgets.trend.chart.PolylineAndHistogramView;
import wangdaye.com.geometricweather.main.utils.MainThemeColorProvider;
import wangdaye.com.geometricweather.theme.ThemeManager;
import wangdaye.com.geometricweather.theme.weatherView.WeatherViewController;

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
            Integer index = daily.getAirQuality().getAqiIndex();
            talkBackBuilder.append(", ").append(index).append(", ").append(daily.getAirQuality().getAqiText());
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
                    daily.getAirQuality().getAqiColor(activity),
                    daily.getAirQuality().getAqiColor(activity),
                    MainThemeColorProvider.getColor(location, R.attr.colorOutline)
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

            dailyItem.setContentDescription(talkBackBuilder.toString());
        }
    }

    @SuppressLint("SimpleDateFormat")
    public DailyAirQualityAdapter(GeoActivity activity, Location location) {
        super(activity, location);

        Weather weather = location.getWeather();
        assert weather != null;

        mHighestIndex = 0;
        for (int i = weather.getDailyForecast().size() - 1; i >= 0; i --) {
            Integer index = weather.getDailyForecast().get(i).getAirQuality().getAqiIndex();
            if (index != null && index > mHighestIndex) {
                mHighestIndex = index;
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
        keyLineList.add(
                new TrendRecyclerView.KeyLine(
                        AirQuality.AQI_INDEX_1,
                        String.valueOf(AirQuality.AQI_INDEX_1),
                        getActivity().getString(R.string.aqi_1),
                        TrendRecyclerView.KeyLine.ContentPosition.ABOVE_LINE
                )
        );
        keyLineList.add(
                new TrendRecyclerView.KeyLine(
                        AirQuality.AQI_INDEX_3,
                        String.valueOf(AirQuality.AQI_INDEX_3),
                        getActivity().getString(R.string.aqi_3),
                        TrendRecyclerView.KeyLine.ContentPosition.ABOVE_LINE
                )
        );
        keyLineList.add(
                new TrendRecyclerView.KeyLine(
                        AirQuality.AQI_INDEX_5,
                        String.valueOf(AirQuality.AQI_INDEX_5),
                        getActivity().getString(R.string.aqi_5),
                        TrendRecyclerView.KeyLine.ContentPosition.ABOVE_LINE
                )
        );
        host.setData(keyLineList, mHighestIndex, 0);
    }
}