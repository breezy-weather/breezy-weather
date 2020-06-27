package com.mbestavros.geometricweather.daily.adapter.holder;

import android.annotation.SuppressLint;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.core.graphics.ColorUtils;

import com.mbestavros.geometricweather.R;
import com.mbestavros.geometricweather.basic.model.weather.AirQuality;
import com.mbestavros.geometricweather.daily.adapter.DailyWeatherAdapter;
import com.mbestavros.geometricweather.daily.adapter.model.DailyAirQuality;
import com.mbestavros.geometricweather.ui.widget.RoundProgress;

public class AirQualityHolder extends DailyWeatherAdapter.ViewHolder {

    private RoundProgress progress;
    private TextView content;

    public AirQualityHolder(ViewGroup parent) {
        super(LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_weather_daily_air, parent, false));
        progress = itemView.findViewById(R.id.item_weather_daily_air_progress);
        content = itemView.findViewById(R.id.item_weather_daily_air_content);
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onBindView(DailyWeatherAdapter.ViewModel model, int position) {
        AirQuality airQuality = ((DailyAirQuality) model).getAirQuality();

        int aqi = airQuality.getAqiIndex();
        int color = airQuality.getAqiColor(itemView.getContext());

        progress.setMax(400);
        progress.setProgress(aqi);
        progress.setProgressColor(color);
        progress.setProgressBackgroundColor(
                ColorUtils.setAlphaComponent(color, (int) (255 * 0.1))
        );

        content.setText(aqi + " / " + airQuality.getAqiText());
    }
}