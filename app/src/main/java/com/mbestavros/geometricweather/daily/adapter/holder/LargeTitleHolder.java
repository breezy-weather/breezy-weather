package com.mbestavros.geometricweather.daily.adapter.holder;

import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.TextView;

import com.mbestavros.geometricweather.R;
import com.mbestavros.geometricweather.daily.adapter.DailyWeatherAdapter;
import com.mbestavros.geometricweather.daily.adapter.model.LargeTitle;

public class LargeTitleHolder extends DailyWeatherAdapter.ViewHolder {

    public LargeTitleHolder(ViewGroup parent) {
        super(LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_weather_daily_title_large, parent, false));
    }

    @Override
    public void onBindView(DailyWeatherAdapter.ViewModel model, int position) {
        LargeTitle title = (LargeTitle) model;
        ((TextView) itemView).setText(title.getTitle());
    }
}
