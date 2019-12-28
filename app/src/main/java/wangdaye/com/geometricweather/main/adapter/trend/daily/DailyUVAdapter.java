package wangdaye.com.geometricweather.main.adapter.trend.daily;

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
import wangdaye.com.geometricweather.basic.model.weather.Daily;
import wangdaye.com.geometricweather.basic.model.weather.UV;
import wangdaye.com.geometricweather.basic.model.weather.Weather;
import wangdaye.com.geometricweather.main.MainThemePicker;
import wangdaye.com.geometricweather.ui.widget.trend.TrendRecyclerView;
import wangdaye.com.geometricweather.ui.widget.trend.chart.PolylineAndHistogramView;
import wangdaye.com.geometricweather.ui.widget.trend.item.DailyTrendItemView;

/**
 * Daily UV adapter.
 * */

public class DailyUVAdapter extends AbsDailyTrendAdapter<DailyUVAdapter.ViewHolder> {

    private Weather weather;
    private TimeZone timeZone;
    private MainThemePicker picker;

    private int highestIndex;

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

            Integer index = daily.getUV().getIndex();
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
                    daily.getUV().getUVColor(context),
                    daily.getUV().getUVColor(context),
                    picker.getLineColor(context)
            );
            int[] themeColors = picker.getWeatherThemeColors();
            polylineAndHistogramView.setShadowColors(
                    themeColors[1], themeColors[2], picker.isLightTheme());
            polylineAndHistogramView.setTextColors(
                    picker.getTextContentColor(context),
                    picker.getTextSubtitleColor(context)
            );
            polylineAndHistogramView.setHistogramAlpha(picker.isLightTheme() ? 1f : 0.5f);

            dailyItem.setOnClickListener(v -> onItemClicked(getAdapterPosition()));
        }
    }

    @SuppressLint("SimpleDateFormat")
    public DailyUVAdapter(GeoActivity activity, TrendRecyclerView parent,
                          @Px float parentWidth, @Px float parentHeight, int itemCountPerLine,
                          String formattedId, @NonNull Weather weather, @NonNull TimeZone timeZone,
                          MainThemePicker picker) {
        super(activity, parent, formattedId, parentWidth, parentHeight, itemCountPerLine);

        this.weather = weather;
        this.timeZone = timeZone;
        this.picker = picker;

        highestIndex = Integer.MIN_VALUE;
        boolean valid = false;
        for (int i = weather.getDailyForecast().size() - 1; i >= 0; i --) {
            Integer index = weather.getDailyForecast().get(i).getUV().getIndex();
            if (index != null && index > highestIndex) {
                highestIndex = index;
            }
            if ((index != null && index != 0) || valid) {
                valid = true;
                size ++;
            }
        }
        if (highestIndex == 0) {
            highestIndex = UV.UV_INDEX_EXCESSIVE;
        }

        List<TrendRecyclerView.KeyLine> keyLineList = new ArrayList<>();
        keyLineList.add(
                new TrendRecyclerView.KeyLine(
                        UV.UV_INDEX_HIGH,
                        activity.getString(R.string.action_alert),
                        String.valueOf(UV.UV_INDEX_HIGH),
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