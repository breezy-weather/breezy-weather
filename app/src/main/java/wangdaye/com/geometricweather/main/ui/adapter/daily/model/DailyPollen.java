package wangdaye.com.geometricweather.main.ui.adapter.daily.model;

import wangdaye.com.geometricweather.basic.model.weather.Pollen;
import wangdaye.com.geometricweather.main.ui.adapter.daily.DailyWeatherAdapter;

public class DailyPollen implements DailyWeatherAdapter.ViewModel {

    private Pollen pollen;

    public DailyPollen(Pollen pollen) {
        this.pollen = pollen;
    }

    public Pollen getPollen() {
        return pollen;
    }

    public void setPollen(Pollen pollen) {
        this.pollen = pollen;
    }

    public static boolean isCode(int code) {
        return code == 6;
    }

    @Override
    public int getCode() {
        return 6;
    }
}
