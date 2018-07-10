package wangdaye.com.geometricweather.ui.adapter;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;

import wangdaye.com.geometricweather.GeometricWeather;
import wangdaye.com.geometricweather.R;
import wangdaye.com.geometricweather.basic.GeoActivity;
import wangdaye.com.geometricweather.data.entity.model.History;
import wangdaye.com.geometricweather.data.entity.model.weather.Hourly;
import wangdaye.com.geometricweather.data.entity.model.weather.Weather;
import wangdaye.com.geometricweather.ui.dialog.WeatherDialog;
import wangdaye.com.geometricweather.ui.widget.trendView.TrendItemView;
import wangdaye.com.geometricweather.utils.helpter.WeatherHelper;

/**
 * Hourly trend adapter.
 * */

public class HourlyTrendAdapter extends RecyclerView.Adapter<HourlyTrendAdapter.ViewHolder> {

    private Context context;

    private Weather weather;

    private float[] maxiTemps;
    private int highestTemp;
    private int lowestTemp;

    private int[] themeColors;

    class ViewHolder extends RecyclerView.ViewHolder
            implements View.OnClickListener {

        private TextView text;
        private ImageView dayIcon;
        private TrendItemView trendItemView;

        ViewHolder(View itemView) {
            super(itemView);

            this.text = itemView.findViewById(R.id.item_trend_hourly_txt);
            this.dayIcon = itemView.findViewById(R.id.item_trend_hourly_icon_day);
            this.trendItemView = itemView.findViewById(R.id.item_trend_hourly_trendItem);

            itemView.findViewById(R.id.item_trend_hourly).setOnClickListener(this);
        }

        void onBindView(int position) {
            Hourly hourly = weather.hourlyList.get(position);

            text.setText(hourly.time);
            Glide.with(context)
                    .load(WeatherHelper.getWeatherIcon(hourly.weatherKind, hourly.dayTime)[3])
                    .diskCacheStrategy(DiskCacheStrategy.SOURCE)
                    .into(dayIcon);
            trendItemView.setData(
                    buildTempArrayForItem(maxiTemps, position),
                    null,
                    hourly.precipitation,
                    highestTemp,
                    lowestTemp);
            trendItemView.setLineColors(themeColors[1], themeColors[2]);
        }

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

        @Override
        public void onClick(View view) {
            switch (view.getId()) {
                case R.id.item_trend_hourly:
                    GeoActivity activity = GeometricWeather.getInstance().getTopActivity();
                    if (activity != null) {
                        WeatherDialog weatherDialog = new WeatherDialog();
                        weatherDialog.setData(weather, getAdapterPosition(), false);
                        weatherDialog.show(activity.getFragmentManager(), null);
                    }
                    break;
            }
        }
    }

    public HourlyTrendAdapter(Context context, @NonNull Weather weather, @Nullable History history,
                              int[] themeColors) {
        this.context = context;
        this.weather = weather;

        this.maxiTemps = new float[weather.hourlyList.size() * 2 - 1];
        for (int i = 0; i < maxiTemps.length; i += 2) {
            maxiTemps[i] = weather.hourlyList.get(i / 2).temp;
        }
        for (int i = 1; i < maxiTemps.length; i += 2) {
            maxiTemps[i] = (maxiTemps[i - 1] + maxiTemps[i + 1]) * 0.5F;
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