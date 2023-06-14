package org.breezyweather.daily.adapter.model;

import org.breezyweather.daily.adapter.DailyWeatherAdapter;

public class Margin implements DailyWeatherAdapter.ViewModel {

    public static boolean isCode(int code) {
        return code == -2;
    }

    @Override
    public int getCode() {
        return -2;
    }
}
