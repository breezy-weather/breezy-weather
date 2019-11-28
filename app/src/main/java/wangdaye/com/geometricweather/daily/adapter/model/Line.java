package wangdaye.com.geometricweather.daily.adapter.model;

import wangdaye.com.geometricweather.daily.adapter.DailyWeatherAdapter;

public class Line implements DailyWeatherAdapter.ViewModel {

    public static boolean isCode(int code) {
        return code == -1;
    }

    @Override
    public int getCode() {
        return -1;
    }
}
