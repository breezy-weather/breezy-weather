package wangdaye.com.geometricweather.main.ui.adapter;

import androidx.annotation.NonNull;
import androidx.annotation.Px;
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
import wangdaye.com.geometricweather.ui.widget.trendView.i.TrendParent;
import wangdaye.com.geometricweather.ui.widget.trendView.i.TrendRecyclerViewAdapter;
import wangdaye.com.geometricweather.ui.widget.trendView.item.HourlyItemView;

/**
 * Hourly trend adapter.
 * */

public class HourlyTrendAdapter extends TrendRecyclerViewAdapter<HourlyTrendAdapter.ViewHolder> {

    private GeoActivity activity;

    private Weather weather;
    private ResourceProvider provider;
    private MainColorPicker picker;
    private TemperatureUnit unit;

    private float[] temperatures;
    private int highestTemperature;
    private int lowestTemperature;

    private int[] themeColors;

    class ViewHolder extends RecyclerView.ViewHolder {

        private HourlyItemView hourlyItem;

        ViewHolder(View itemView) {
            super(itemView);
            hourlyItem = itemView.findViewById(R.id.item_trend_hourly);
            hourlyItem.setParent(getTrendParent());
            hourlyItem.setWidth(getItemWidth());
            hourlyItem.setHeight(getItemHeight());
        }

        void onBindView(int position) {
            Context context = itemView.getContext();
            Hourly hourly = weather.getHourlyForecast().get(position);

            hourlyItem.setHourText(hourly.getHour(context));

            hourlyItem.setTextColor(picker.getTextContentColor(context));

            hourlyItem.setIconDrawable(
                    ResourceHelper.getWeatherIcon(provider, hourly.getWeatherCode(), hourly.isDaylight())
            );

            hourlyItem.getOverviewItem().setData(
                    buildTempArrayForItem(temperatures, position),
                    null,
                    hourly.getPrecipitationProbability().getTotal(),
                    highestTemperature,
                    lowestTemperature,
                    unit
            );
            hourlyItem.getOverviewItem().setLineColors(
                    themeColors[1], themeColors[2], picker.getLineColor(context)
            );
            hourlyItem.getOverviewItem().setShadowColors(
                    themeColors[1], themeColors[2], picker.isLightTheme());
            hourlyItem.getOverviewItem().setTextColors(
                    picker.getTextContentColor(context),
                    picker.getTextSubtitleColor(context)
            );
            hourlyItem.getOverviewItem().setPrecipitationAlpha(picker.isLightTheme() ? 0.2f : 0.5f);

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
        private Float[] buildTempArrayForItem(float[] temps, int adapterPosition) {
            Float[] a = new Float[3];
            a[1] = temps[2 * adapterPosition];
            if (2 * adapterPosition - 1 < 0) {
                a[0] = null;
            } else {
                a[0] = temps[2 * adapterPosition - 1];
            }
            if (2 * adapterPosition + 1 >= temps.length) {
                a[2] = null;
            } else {
                a[2] = temps[2 * adapterPosition + 1];
            }
            return a;
        }
    }

    public HourlyTrendAdapter(GeoActivity activity,
                              TrendParent parent, float marginHorizontalPx, int itemCountPerLine,
                              @Px float itemHeight,
                              @NonNull Weather weather, int[] themeColors,
                              ResourceProvider provider, MainColorPicker picker, TemperatureUnit unit) {
        super(activity, parent, marginHorizontalPx, itemCountPerLine, itemHeight);
        this.activity = activity;

        this.weather = weather;
        this.provider = provider;
        this.picker = picker;
        this.unit = unit;

        this.temperatures = new float[Math.max(0, weather.getHourlyForecast().size() * 2 - 1)];
        for (int i = 0; i < temperatures.length; i += 2) {
            temperatures[i] = weather.getHourlyForecast().get(i / 2).getTemperature().getTemperature();
        }
        for (int i = 1; i < temperatures.length; i += 2) {
            temperatures[i] = (temperatures[i - 1] + temperatures[i + 1]) * 0.5F;
        }

        highestTemperature = weather.getYesterday() == null
                ? Integer.MIN_VALUE
                : weather.getYesterday().getDaytimeTemperature();
        lowestTemperature = weather.getYesterday() == null
                ? Integer.MAX_VALUE
                : weather.getYesterday().getNighttimeTemperature();
        for (int i = 0; i < weather.getHourlyForecast().size(); i ++) {
            if (weather.getHourlyForecast().get(i).getTemperature().getTemperature() > highestTemperature) {
                highestTemperature = weather.getHourlyForecast().get(i).getTemperature().getTemperature();
            }
            if (weather.getHourlyForecast().get(i).getTemperature().getTemperature() < lowestTemperature) {
                lowestTemperature = weather.getHourlyForecast().get(i).getTemperature().getTemperature();
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