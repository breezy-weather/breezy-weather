package org.breezyweather.daily.adapter.model;

import org.breezyweather.daily.adapter.DailyWeatherAdapter;

public class Line implements DailyWeatherAdapter.ViewModel {

    public static boolean isCode(int code) {
        return code == -1;
    }

    @Override
    public int getCode() {
        return -1;
    }
}
