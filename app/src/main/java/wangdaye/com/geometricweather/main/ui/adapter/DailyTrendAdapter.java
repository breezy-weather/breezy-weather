package wangdaye.com.geometricweather.main.ui.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import androidx.annotation.NonNull;
import androidx.annotation.Size;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import wangdaye.com.geometricweather.R;
import wangdaye.com.geometricweather.basic.GeoActivity;
import wangdaye.com.geometricweather.basic.model.option.unit.TemperatureUnit;
import wangdaye.com.geometricweather.basic.model.weather.Daily;
import wangdaye.com.geometricweather.basic.model.weather.Weather;
import wangdaye.com.geometricweather.main.ui.MainColorPicker;
import wangdaye.com.geometricweather.resource.ResourceHelper;
import wangdaye.com.geometricweather.resource.provider.ResourceProvider;
import wangdaye.com.geometricweather.main.ui.dialog.WeatherDialog;
import wangdaye.com.geometricweather.ui.widget.trendView.DailyItemView;
import wangdaye.com.geometricweather.ui.widget.trendView.TrendItemView;

/**
 * Daily trend adapter.
 * */

public class DailyTrendAdapter extends RecyclerView.Adapter<DailyTrendAdapter.ViewHolder> {

    private GeoActivity activity;

    private Weather weather;
    private ResourceProvider provider;
    private MainColorPicker picker;
    private TemperatureUnit unit;

    private float[] maxiTemps;
    private float[] miniTemps;
    private int highestTemp;
    private int lowestTemp;

    private int[] themeColors;

    class ViewHolder extends RecyclerView.ViewHolder {

        private DailyItemView dailyItem;

        ViewHolder(View itemView) {
            super(itemView);
            this.dailyItem = itemView.findViewById(R.id.item_trend_daily);
        }

        @SuppressLint("SetTextI18n, InflateParams")
        void onBindView(int position) {
            Context context = itemView.getContext();
            Daily daily = weather.getDailyForecast().get(position);

            if (daily.isToday()) {
                dailyItem.setWeekText(context.getString(R.string.today));
            } else {
                dailyItem.setWeekText(daily.getWeek(context));
            }

            dailyItem.setDateText(daily.getShortDate(context));

            dailyItem.setTextColor(
                    picker.getTextContentColor(context),
                    picker.getTextSubtitleColor(context)
            );

            dailyItem.setDayIconDrawable(
                    ResourceHelper.getWeatherIcon(provider, daily.day().getWeatherCode(), true));

            Float daytimePrecipitationProbability = daily.day().getPrecipitationProbability().getTotal();
            Float nighttimePrecipitationProbability = daily.night().getPrecipitationProbability().getTotal();
            dailyItem.getTrendItemView().setData(
                    buildTempArrayForItem(maxiTemps, position),
                    buildTempArrayForItem(miniTemps, position),
                    (int) Math.max(
                            daytimePrecipitationProbability == null ? 0 : daytimePrecipitationProbability,
                            nighttimePrecipitationProbability == null ? 0 : nighttimePrecipitationProbability
                    ),
                    highestTemp,
                    lowestTemp,
                    unit
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
                    ResourceHelper.getWeatherIcon(provider, daily.night().getWeatherCode(), false));

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
    public DailyTrendAdapter(GeoActivity activity, @NonNull Weather weather, int[] themeColors,
                             ResourceProvider provider, MainColorPicker picker, TemperatureUnit unit) {
        this.activity = activity;

        this.weather = weather;
        this.provider = provider;
        this.picker = picker;
        this.unit = unit;

        this.maxiTemps = new float[Math.max(0, weather.getDailyForecast().size() * 2 - 1)];
        for (int i = 0; i < maxiTemps.length; i += 2) {
            maxiTemps[i] = weather.getDailyForecast().get(i / 2).day().getTemperature().getTemperature();
        }
        for (int i = 1; i < maxiTemps.length; i += 2) {
            maxiTemps[i] = (maxiTemps[i - 1] + maxiTemps[i + 1]) * 0.5F;
        }

        this.miniTemps = new float[Math.max(0, weather.getDailyForecast().size() * 2 - 1)];
        for (int i = 0; i < miniTemps.length; i += 2) {
            miniTemps[i] = weather.getDailyForecast().get(i / 2).night().getTemperature().getTemperature();
        }
        for (int i = 1; i < miniTemps.length; i += 2) {
            miniTemps[i] = (miniTemps[i - 1] + miniTemps[i + 1]) * 0.5F;
        }

        highestTemp = weather.getYesterday() == null
                ? Integer.MIN_VALUE
                : weather.getYesterday().getDaytimeTemperature();
        lowestTemp = weather.getYesterday() == null
                ? Integer.MAX_VALUE
                : weather.getYesterday().getNighttimeTemperature();
        for (int i = 0; i < weather.getDailyForecast().size(); i ++) {
            if (weather.getDailyForecast().get(i).day().getTemperature().getTemperature() > highestTemp) {
                highestTemp = weather.getDailyForecast().get(i).day().getTemperature().getTemperature();
            }
            if (weather.getDailyForecast().get(i).night().getTemperature().getTemperature() < lowestTemp) {
                lowestTemp = weather.getDailyForecast().get(i).night().getTemperature().getTemperature();
            }
        }

        this.themeColors = themeColors;
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
        return weather.getDailyForecast().size();
    }
}