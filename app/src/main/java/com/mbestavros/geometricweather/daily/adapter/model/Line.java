package com.mbestavros.geometricweather.daily.adapter.model;

import com.mbestavros.geometricweather.daily.adapter.DailyWeatherAdapter;

public class Line implements DailyWeatherAdapter.ViewModel {

    public static boolean isCode(int code) {
        return code == -1;
    }

    @Override
    public int getCode() {
        return -1;
    }
}
