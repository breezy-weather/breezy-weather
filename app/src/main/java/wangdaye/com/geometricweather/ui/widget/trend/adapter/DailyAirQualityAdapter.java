package wangdaye.com.geometricweather.ui.widget.trend.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Px;
import androidx.recyclerview.widget.RecyclerView;


import java.util.ArrayList;
import java.util.List;
import java.util.TimeZone;

import wangdaye.com.geometricweather.R;
import wangdaye.com.geometricweather.basic.GeoActivity;
import wangdaye.com.geometricweather.basic.model.weather.AirQuality;
import wangdaye.com.geometricweather.basic.model.weather.Daily;
import wangdaye.com.geometricweather.basic.model.weather.Weather;
import wangdaye.com.geometricweather.main.ui.MainColorPicker;
import wangdaye.com.geometricweather.main.ui.dialog.DailyWeatherDialog;
import wangdaye.com.geometricweather.ui.widget.trend.TrendRecyclerView;
import wangdaye.com.geometricweather.ui.widget.trend.abs.TrendRecyclerViewAdapter;
import wangdaye.com.geometricweather.ui.widget.trend.chart.PolylineAndHistogramView;
import wangdaye.com.geometricweather.ui.widget.trend.item.DailyTrendItemView;

/**
 * Daily air quality adapter.
 * */

public abstract class DailyAirQualityAdapter extends TrendRecyclerViewAdapter<DailyAirQualityAdapter.ViewHolder> {

    private GeoActivity activity;

    private Weather weather;
    private TimeZone timeZone;
    private MainColorPicker picker;

    private int highestIndex;
    private int[] themeColors;

    private int size;

    class ViewHolder extends RecyclerView.ViewHolder {

        private DailyTrendItemView dailyItem;
        private PolylineAndHistogramView polylineAndHistogramView;

        ViewHolder(View itemView) {
            super(itemView);
            dailyItem = itemView.findViewById(R.id.item_trend_daily);
            dailyItem.setParent(getTrendParent());
            dailyItem.setWidth(getItemWidth());
            dailyItem.setHeight(getItemHeight());

            polylineAndHistogramView = new PolylineAndHistogramView(itemView.getContext());
            dailyItem.setChartItemView(polylineAndHistogramView);
        }

        @SuppressLint("SetTextI18n, InflateParams")
        void onBindView(int position) {
            Context context = itemView.getContext();
            Daily daily = weather.getDailyForecast().get(position);

            if (daily.isToday(timeZone)) {
                dailyItem.setWeekText(context.getString(R.string.today));
            } else {
                dailyItem.setWeekText(daily.getWeek(context));
            }

            dailyItem.setDateText(daily.getShortDate(context));

            dailyItem.setTextColor(
                    picker.getTextContentColor(context),
                    picker.getTextSubtitleColor(context)
            );

            Integer index = daily.getAirQuality().getAqiIndex();
            polylineAndHistogramView.setData(
                    null, null,
                    null, null,
                    null, null,
                    (float) (index == null ? 0 : index),
                    String.valueOf(index == null ? 0 : index),
                    (float) highestIndex,
                    0f
            );
            polylineAndHistogramView.setLineColors(
                    daily.getAirQuality().getAqiColor(context),
                    daily.getAirQuality().getAqiColor(context),
                    picker.getLineColor(context)
            );
            polylineAndHistogramView.setShadowColors(
                    themeColors[1], themeColors[2], picker.isLightTheme());
            polylineAndHistogramView.setTextColors(
                    picker.getTextContentColor(context),
                    picker.getTextSubtitleColor(context)
            );
            polylineAndHistogramView.setHistogramAlpha(picker.isLightTheme() ? 1f : 0.5f);

            dailyItem.setOnClickListener(v -> {
                if (activity.isForeground()) {
                    DailyWeatherDialog dialog = new DailyWeatherDialog();
                    dialog.setData(weather, getAdapterPosition(), themeColors[0]);
                    dialog.setColorPicker(picker);
                    dialog.show(activity.getSupportFragmentManager(), null);
                }
            });
        }
    }

    @SuppressLint("SimpleDateFormat")
    public DailyAirQualityAdapter(GeoActivity activity, TrendRecyclerView parent,
                                  @Px float cardMarginsVertical, @Px float cardMarginsHorizontal,
                                  int itemCountPerLine, @Px float itemHeight,
                                  @NonNull Weather weather, @NonNull TimeZone timeZone,
                                  int[] themeColors, MainColorPicker picker) {
        super(activity, parent, cardMarginsVertical, cardMarginsHorizontal, itemCountPerLine, itemHeight);
        this.activity = activity;

        this.weather = weather;
        this.timeZone = timeZone;
        this.picker = picker;

        highestIndex = Integer.MIN_VALUE;
        boolean valid = false;
        for (int i = weather.getDailyForecast().size() - 1; i >= 0; i --) {
            Integer index = weather.getDailyForecast().get(i).getAirQuality().getAqiIndex();
            if (index != null && index > highestIndex) {
                highestIndex = index;
            }
            if ((index != null && index != 0) || valid) {
                valid = true;
                size ++;
            }
        }
        if (highestIndex == 0) {
            highestIndex = AirQuality.AQI_INDEX_5;
        }

        this.themeColors = themeColors;

        List<TrendRecyclerView.KeyLine> keyLineList = new ArrayList<>();
        keyLineList.add(
                new TrendRecyclerView.KeyLine(
                        AirQuality.AQI_INDEX_1,
                        activity.getString(R.string.aqi_1),
                        String.valueOf(AirQuality.AQI_INDEX_1),
                        TrendRecyclerView.KeyLine.ContentPosition.ABOVE_LINE
                )
        );
        keyLineList.add(
                new TrendRecyclerView.KeyLine(
                        AirQuality.AQI_INDEX_3,
                        activity.getString(R.string.aqi_3),
                        String.valueOf(AirQuality.AQI_INDEX_3),
                        TrendRecyclerView.KeyLine.ContentPosition.ABOVE_LINE
                )
        );
        keyLineList.add(
                new TrendRecyclerView.KeyLine(
                        AirQuality.AQI_INDEX_5,
                        activity.getString(R.string.aqi_5),
                        String.valueOf(AirQuality.AQI_INDEX_5),
                        TrendRecyclerView.KeyLine.ContentPosition.ABOVE_LINE
                )
        );
        parent.setLineColor(picker.getLineColor(activity));
        parent.setData(keyLineList, highestIndex, 0);
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
        holder.onBindView(position);
    }

    @Override
    public int getItemCount() {
        return size;
    }
}