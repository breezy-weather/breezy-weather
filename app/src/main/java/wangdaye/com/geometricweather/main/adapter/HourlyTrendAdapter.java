package wangdaye.com.geometricweather.main.adapter;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.Size;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;

import wangdaye.com.geometricweather.GeometricWeather;
import wangdaye.com.geometricweather.R;
import wangdaye.com.geometricweather.basic.GeoActivity;
import wangdaye.com.geometricweather.basic.model.History;
import wangdaye.com.geometricweather.basic.model.weather.Hourly;
import wangdaye.com.geometricweather.basic.model.weather.Weather;
import wangdaye.com.geometricweather.main.dialog.WeatherDialog;
import wangdaye.com.geometricweather.ui.widget.trendView.HourlyItemView;
import wangdaye.com.geometricweather.ui.widget.trendView.TrendItemView;
import wangdaye.com.geometricweather.weather.WeatherHelper;

/**
 * Hourly trend adapter.
 * */

public class HourlyTrendAdapter extends RecyclerView.Adapter<HourlyTrendAdapter.ViewHolder> {

    private Weather weather;

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
            Hourly hourly = weather.hourlyList.get(position);

            hourlyItem.setHourText(hourly.time);

            Glide.with(context)
                    .load(WeatherHelper.getWeatherIcon(hourly.weatherKind, hourly.dayTime)[3])
                    .diskCacheStrategy(DiskCacheStrategy.SOURCE)
                    .into(hourlyItem.getIconTarget());

            hourlyItem.getTrendItemView().setData(
                    buildTempArrayForItem(temps, position),
                    null,
                    hourly.precipitation,
                    highestTemp,
                    lowestTemp
            );
            hourlyItem.getTrendItemView().setLineColors(themeColors[1], themeColors[2]);

            hourlyItem.setOnClickListener(v -> {
                GeoActivity activity = GeometricWeather.getInstance().getTopActivity();
                if (activity != null && activity.isForeground()) {
                    WeatherDialog weatherDialog = new WeatherDialog();
                    weatherDialog.setData(weather, getAdapterPosition(), false);
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

    public HourlyTrendAdapter(@NonNull Weather weather, @Nullable History history,
                              int[] themeColors) {
        this.weather = weather;

        this.temps = new float[Math.max(0, weather.hourlyList.size() * 2 - 1)];
        for (int i = 0; i < temps.length; i += 2) {
            temps[i] = weather.hourlyList.get(i / 2).temp;
        }
        for (int i = 1; i < temps.length; i += 2) {
            temps[i] = (temps[i - 1] + temps[i + 1]) * 0.5F;
        }

        highestTemp = history == null ? Integer.MIN_VALUE : history.maxiTemp;
        lowestTemp = history == null ? Integer.MAX_VALUE : history.miniTemp;
        for (int i = 0; i < weather.hourlyList.size(); i ++) {
            if (weather.hourlyList.get(i).temp > highestTemp) {
                highestTemp = weather.hourlyList.get(i).temp;
            }
            if (weather.hourlyList.get(i).temp < lowestTemp) {
                lowestTemp = weather.hourlyList.get(i).temp;
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
        return weather.hourlyList.size();
    }
}