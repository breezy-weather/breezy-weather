package wangdaye.com.geometricweather.daily.adapter.model;

import wangdaye.com.geometricweather.basic.model.weather.Wind;
import wangdaye.com.geometricweather.daily.adapter.DailyWeatherAdapter;

public class DailyWind implements DailyWeatherAdapter.ViewModel {

    private Wind wind;

    public DailyWind(Wind wind) {
        this.wind = wind;
    }

    public Wind getWind() {
        return wind;
    }

    public void setWind(Wind wind) {
        this.wind = wind;
    }

    public static boolean isCode(int code) {
        return code == 4;
    }

    @Override
    public int getCode() {
        return 4;
    }
}
