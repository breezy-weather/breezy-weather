package wangdaye.com.geometricweather.main.adapters.trend.hourly;

import android.annotation.SuppressLint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;

import java.util.ArrayList;
import java.util.List;

import wangdaye.com.geometricweather.R;
import wangdaye.com.geometricweather.common.basic.GeoActivity;
import wangdaye.com.geometricweather.common.basic.models.Location;
import wangdaye.com.geometricweather.common.basic.models.weather.Hourly;
import wangdaye.com.geometricweather.common.basic.models.weather.UV;
import wangdaye.com.geometricweather.common.basic.models.weather.Weather;
import wangdaye.com.geometricweather.common.ui.widgets.trend.TrendRecyclerView;
import wangdaye.com.geometricweather.common.ui.widgets.trend.chart.PolylineAndHistogramView;
import wangdaye.com.geometricweather.main.utils.MainThemeColorProvider;
import wangdaye.com.geometricweather.theme.ThemeManager;
import wangdaye.com.geometricweather.theme.weatherView.WeatherViewController;

/**
 * Hourly UV adapter.
 * */

public class HourlyUVAdapter extends AbsHourlyTrendAdapter<HourlyUVAdapter.ViewHolder> {

    private int highestIndex;
    private int mSize;

    class ViewHolder extends AbsHourlyTrendAdapter.ViewHolder {

        private final PolylineAndHistogramView mPolylineAndHistogramView;

        ViewHolder(View itemView) {
            super(itemView);
            mPolylineAndHistogramView = new PolylineAndHistogramView(itemView.getContext());
            hourlyItem.setChartItemView(mPolylineAndHistogramView);
        }

        @SuppressLint({"SetTextI18n, InflateParams", "DefaultLocale"})
        void onBindView(GeoActivity activity, Location location, int position) {
            StringBuilder talkBackBuilder = new StringBuilder(activity.getString(R.string.tag_uv));

            super.onBindView(activity, location, talkBackBuilder, position);

            Weather weather = location.getWeather();
            assert weather != null;
            Hourly hourly = weather.getHourlyForecast().get(position);

            Integer index = hourly.getUV().getIndex();
            talkBackBuilder.append(", ").append(index).append(", ").append(hourly.getUV().getLevel());
            mPolylineAndHistogramView.setData(
                    null, null,
                    null, null,
                    null, null,
                    (float) (index == null ? 0 : index),
                    String.format("%d", index == null ? 0 : index),
                    (float) highestIndex,
                    0f
            );
            mPolylineAndHistogramView.setLineColors(
                    hourly.getUV().getUVColor(activity),
                    hourly.getUV().getUVColor(activity),
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
            mPolylineAndHistogramView.setShadowColors(
                    themeColors[lightTheme ? 1 : 2],
                    themeColors[2],
                    lightTheme
            );
            mPolylineAndHistogramView.setTextColors(
                    MainThemeColorProvider.getColor(location, R.attr.colorTitleText),
                    MainThemeColorProvider.getColor(location, R.attr.colorBodyText),
                    MainThemeColorProvider.getColor(location, R.attr.colorTitleText)
            );
            mPolylineAndHistogramView.setHistogramAlpha(lightTheme ? 1f : 0.5f);

            hourlyItem.setContentDescription(talkBackBuilder.toString());
        }
    }

    @SuppressLint("SimpleDateFormat")
    public HourlyUVAdapter(GeoActivity activity,
                           TrendRecyclerView parent,
                           Location location) {
        super(activity, location);

        Weather weather = location.getWeather();
        assert weather != null;

        highestIndex = Integer.MIN_VALUE;
        boolean valid = false;
        for (int i = weather.getHourlyForecast().size() - 1; i >= 0; i --) {
            Integer index = weather.getHourlyForecast().get(i).getUV().getIndex();
            if (index != null && index > highestIndex) {
                highestIndex = index;
            }
            if ((index != null && index != 0) || valid) {
                valid = true;
                mSize ++;
            }
        }
        if (highestIndex == 0) {
            highestIndex = UV.UV_INDEX_EXCESSIVE;
        }

        List<TrendRecyclerView.KeyLine> keyLineList = new ArrayList<>();
        keyLineList.add(
                new TrendRecyclerView.KeyLine(
                        UV.UV_INDEX_HIGH,
                        String.valueOf(UV.UV_INDEX_HIGH),
                        activity.getString(R.string.action_alert),
                        TrendRecyclerView.KeyLine.ContentPosition.ABOVE_LINE
                )
        );
        parent.setData(keyLineList, highestIndex, 0);
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
        holder.onBindView(getActivity(), getLocation(), position);
    }

    @Override
    public int getItemCount() {
        return mSize;
    }
}