package wangdaye.com.geometricweather.main.ui.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.Size;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.text.SimpleDateFormat;
import java.util.Date;

import wangdaye.com.geometricweather.R;
import wangdaye.com.geometricweather.basic.GeoActivity;
import wangdaye.com.geometricweather.basic.model.History;
import wangdaye.com.geometricweather.basic.model.weather.Daily;
import wangdaye.com.geometricweather.basic.model.weather.Weather;
import wangdaye.com.geometricweather.main.ui.MainColorPicker;
import wangdaye.com.geometricweather.resource.provider.ResourceProvider;
import wangdaye.com.geometricweather.main.ui.dialog.WeatherDialog;
import wangdaye.com.geometricweather.ui.widget.trendView.DailyItemView;
import wangdaye.com.geometricweather.ui.widget.trendView.TrendItemView;
import wangdaye.com.geometricweather.weather.WeatherHelper;

/**
 * Daily trend adapter.
 * */

public class DailyTrendAdapter extends RecyclerView.Adapter<DailyTrendAdapter.ViewHolder> {

    private GeoActivity activity;

    private Weather weather;
    private ResourceProvider provider;
    private MainColorPicker picker;

    private float[] maxiTemps;
    private float[] miniTemps;
    private int highestTemp;
    private int lowestTemp;

    private int[] themeColors;

    private SimpleDateFormat format;

    class ViewHolder extends RecyclerView.ViewHolder {

        private DailyItemView dailyItem;

        ViewHolder(View itemView) {
            super(itemView);
            this.dailyItem = itemView.findViewById(R.id.item_trend_daily);
        }

        @SuppressLint("SetTextI18n, InflateParams")
        void onBindView(int position) {
            Context context = itemView.getContext();
            Daily daily = weather.dailyList.get(position);

            if (daily.date.equals(format.format(new Date()))) {
                dailyItem.setWeekText(context.getString(R.string.today));
            } else {
                dailyItem.setWeekText(daily.week);
            }

            dailyItem.setDateText(
                    daily.getDateInFormat(context.getString(R.string.date_format_short))
            );

            dailyItem.setTextColor(
                    picker.getTextContentColor(context),
                    picker.getTextSubtitleColor(context)
            );

            dailyItem.setDayIconDrawable(
                    WeatherHelper.getWeatherIcon(provider, daily.weatherKinds[0], true)
            );

            dailyItem.getTrendItemView().setData(
                    buildTempArrayForItem(maxiTemps, position),
                    buildTempArrayForItem(miniTemps, position),
                    Math.max(daily.precipitations[0], daily.precipitations[1]),
                    highestTemp,
                    lowestTemp
            );
            dailyItem.getTrendItemView().setLineColors(
                    themeColors[1], themeColors[2], picker.getLineColor(context)
            );
            dailyItem.getTrendItemView().setShadowColors(
                    themeColors[1], themeColors[2], picker.isLightTheme());
            dailyItem.getTrendItemView().setTextColors(
                    picker.getTextContentColor(context),
                    picker.getTextSubtitleColor(context)
            );
            dailyItem.getTrendItemView().setPrecipitationAlpha(picker.isLightTheme() ? 0.2f : 0.5f);

            dailyItem.setNightIconDrawable(
                    WeatherHelper.getWeatherIcon(provider, daily.weatherKinds[1], false)
            );

            dailyItem.setOnClickListener(v -> {
                if (activity.isForeground()) {
                    WeatherDialog weatherDialog = new WeatherDialog();
                    weatherDialog.setData(weather, getAdapterPosition(), true, themeColors[0]);
                    weatherDialog.setColorPicker(picker);
                    weatherDialog.show(activity.getSupportFragmentManager(), null);
                }
            });
        }

        @Size(3)
        private float[] buildTempArrayForItem(float[] temps, int adapterPosition) {
            float[] a = new float[3];
            a[1] = temps[2 * adapterPosition];
            if (2 * adapterPosition - 1 < 0) {
                a[0] = TrendItemView.NONEXISTENT_VALUE;
            } else {
                a[0] = temps[2 * adapterPosition - 1];
            }
            if (2 * adapterPosition + 1 >= temps.length) {
                a[2] = TrendItemView.NONEXISTENT_VALUE;
            } else {
                a[2] = temps[2 * adapterPosition + 1];
            }
            return a;
        }
    }

    @SuppressLint("SimpleDateFormat")
    public DailyTrendAdapter(GeoActivity activity,
                             @NonNull Weather weather, @Nullable History history,
                             int[] themeColors, ResourceProvider provider, MainColorPicker picker) {
        this.activity = activity;

        this.weather = weather;
        this.provider = provider;
        this.picker = picker;

        this.maxiTemps = new float[Math.max(0, weather.dailyList.size() * 2 - 1)];
        for (int i = 0; i < maxiTemps.length; i += 2) {
            maxiTemps[i] = weather.dailyList.get(i / 2).temps[0];
        }
        for (int i = 1; i < maxiTemps.length; i += 2) {
            maxiTemps[i] = (maxiTemps[i - 1] + maxiTemps[i + 1]) * 0.5F;
        }

        this.miniTemps = new float[Math.max(0, weather.dailyList.size() * 2 - 1)];
        for (int i = 0; i < miniTemps.length; i += 2) {
            miniTemps[i] = weather.dailyList.get(i / 2).temps[1];
        }
        for (int i = 1; i < miniTemps.length; i += 2) {
            miniTemps[i] = (miniTemps[i - 1] + miniTemps[i + 1]) * 0.5F;
        }

        highestTemp = history == null ? Integer.MIN_VALUE : history.maxiTemp;
        lowestTemp = history == null ? Integer.MAX_VALUE : history.miniTemp;
        for (int i = 0; i < weather.dailyList.size(); i ++) {
            if (weather.dailyList.get(i).temps[0] > highestTemp) {
                highestTemp = weather.dailyList.get(i).temps[0];
            }
            if (weather.dailyList.get(i).temps[1] < lowestTemp) {
                lowestTemp = weather.dailyList.get(i).temps[1];
            }
        }

        this.themeColors = themeColors;

        this.format = new SimpleDateFormat("yyyy-MM-dd");
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
        return weather.dailyList.size();
    }
}