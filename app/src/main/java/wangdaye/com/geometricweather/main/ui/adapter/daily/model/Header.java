package wangdaye.com.geometricweather.main.ui.adapter.daily.model;

import androidx.annotation.ColorInt;

import wangdaye.com.geometricweather.basic.model.weather.Daily;
import wangdaye.com.geometricweather.basic.model.weather.HalfDay;
import wangdaye.com.geometricweather.main.ui.adapter.daily.DailyWeatherAdapter;

public class Header implements DailyWeatherAdapter.ViewModel {

    private Daily daily;
    private @ColorInt int weatherColor;

    public Header(Daily daily, int weatherColor) {
        this.daily = daily;
        this.weatherColor = weatherColor;
    }

    public Daily getDaily() {
        return daily;
    }

    public void setDaily(Daily daily) {
        this.daily = daily;
    }

    public int getWeatherColor() {
        return weatherColor;
    }

    public void setWeatherColor(int weatherColor) {
        this.weatherColor = weatherColor;
    }

    public static boolean isCode(int code) {
        return code == 0;
    }

    @Override
    public int getCode() {
        return 0;
    }
}
