package com.mbestavros.geometricweather.daily.adapter.holder;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import com.mbestavros.geometricweather.R;
import com.mbestavros.geometricweather.daily.adapter.DailyWeatherAdapter;

public class MarginHolder extends DailyWeatherAdapter.ViewHolder {

    public MarginHolder(ViewGroup parent) {
        super(LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_weather_daily_margin, parent, false));
    }

    @Override
    public void onBindView(DailyWeatherAdapter.ViewModel model, int position) {
        // do nothing.
    }
}
