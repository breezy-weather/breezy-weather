package wangdaye.com.geometricweather.main.ui.adapter.daily.model;

import wangdaye.com.geometricweather.main.ui.adapter.daily.DailyWeatherAdapter;

public class Line implements DailyWeatherAdapter.ViewModel {

    public static boolean isCode(int code) {
        return code == -1;
    }

    @Override
    public int getCode() {
        return -1;
    }
}
