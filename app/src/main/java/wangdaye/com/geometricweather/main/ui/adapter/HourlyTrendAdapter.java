package wangdaye.com.geometricweather.main.ui.adapter;

import androidx.annotation.NonNull;
import androidx.annotation.Size;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import wangdaye.com.geometricweather.R;
import wangdaye.com.geometricweather.basic.GeoActivity;
import wangdaye.com.geometricweather.basic.model.option.unit.TemperatureUnit;
import wangdaye.com.geometricweather.basic.model.weather.Hourly;
import wangdaye.com.geometricweather.basic.model.weather.Weather;
import wangdaye.com.geometricweather.main.ui.MainColorPicker;
import wangdaye.com.geometricweather.resource.ResourceHelper;
import wangdaye.com.geometricweather.resource.provider.ResourceProvider;
import wangdaye.com.geometricweather.main.ui.dialog.WeatherDialog;
import wangdaye.com.geometricweather.ui.widget.trendView.HourlyItemView;
import wangdaye.com.geometricweather.ui.widget.trendView.TrendItemView;

/**
 * Hourly trend adapter.
 * */

public class HourlyTrendAdapter extends RecyclerView.Adapter<HourlyTrendAdapter.ViewHolder> {

    private GeoActivity activity;

    private Weather weather;
    private ResourceProvider provider;
    private MainColorPicker picker;
    private TemperatureUnit unit;

    private float[] temps;
    private int highestTemp;
    private int lowestTemp;

    private int[] themeColors;

    class ViewHolder extends RecyclerView.ViewHolder {

        private HourlyItemView hourlyItem;

        ViewHolder(View itemView) {
            super(itemView);
            hourlyItem = itemView.findViewById(R.id.item_trend_hourly);
        }

        void onBindView(int position) {
            Context context = itemView.getContext();
            Hourly hourly = weather.getHourlyForecast().get(position);

            hourlyItem.setHourText(hourly.getHour(context));

            hourlyItem.setTextColor(picker.getTextContentColor(context));

            hourlyItem.setIconDrawable(
                    ResourceHelper.getWeatherIcon(provider, hourly.getWeatherCode(), hourly.isDaylight())
            );

            Float p = hourly.getPrecipitationProbability().getTotal();
            float fp = p == null ? 0 : p;
            hourlyItem.getTrendItemView().setData(
                    buildTempArrayForItem(temps, position),
                    null,
                    (int) fp,
                    highestTemp,
                    lowestTemp,
                    unit
            );
            hourlyItem.getTrendItemView().setLineColors(
                    themeColors[1], themeColors[2], picker.getLineColor(context)
            );
            hourlyItem.getTrendItemView().setShadowColors(
                    themeColors[1], themeColors[2], picker.isLightTheme());
            hourlyItem.getTrendItemView().setTextColors(
                    picker.getTextContentColor(context),
                    picker.getTextSubtitleColor(context)
            );
            hourlyItem.getTrendItemView().setPrecipitationAlpha(picker.isLightTheme() ? 0.2f : 0.5f);

            hourlyItem.setOnClickListener(v -> {
                if (activity.isForeground()) {
                    WeatherDialog weatherDialog = new WeatherDialog();
                    weatherDialog.setData(weather, getAdapterPosition(), false, themeColors[0]);
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

    public HourlyTrendAdapter(GeoActivity activity, @NonNull Weather weather, int[] themeColors,
                              ResourceProvider provider, MainColorPicker picker, TemperatureUnit unit) {
        this.activity = activity;

        this.weather = weather;
        this.provider = provider;
        this.picker = picker;
        this.unit = unit;

        this.temps = new float[Math.max(0, weather.getHourlyForecast().size() * 2 - 1)];
        for (int i = 0; i < temps.length; i += 2) {
            temps[i] = weather.getHourlyForecast().get(i / 2).getTemperature().getTemperature();
        }
        for (int i = 1; i < temps.length; i += 2) {
            temps[i] = (temps[i - 1] + temps[i + 1]) * 0.5F;
        }

        highestTemp = weather.getYesterday() == null
                ? Integer.MIN_VALUE
                : weather.getYesterday().getDaytimeTemperature();
        lowestTemp = weather.getYesterday() == null
                ? Integer.MAX_VALUE
                : weather.getYesterday().getNighttimeTemperature();
        for (int i = 0; i < weather.getHourlyForecast().size(); i ++) {
            if (weather.getHourlyForecast().get(i).getTemperature().getTemperature() > highestTemp) {
                highestTemp = weather.getHourlyForecast().get(i).getTemperature().getTemperature();
            }
            if (weather.getHourlyForecast().get(i).getTemperature().getTemperature() < lowestTemp) {
                lowestTemp = weather.getHourlyForecast().get(i).getTemperature().getTemperature();
            }
        }
        this.themeColors = themeColors;
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
        holder.onBindView(position);
    }

    @Override
    public int getItemCount() {
        return weather.getHourlyForecast().size();
    }
}