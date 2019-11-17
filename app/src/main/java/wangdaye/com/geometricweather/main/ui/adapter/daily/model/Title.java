package wangdaye.com.geometricweather.main.ui.adapter.daily.model;

import wangdaye.com.geometricweather.main.ui.adapter.daily.DailyWeatherAdapter;

public class Title implements DailyWeatherAdapter.ViewModel {

    private String title;

    public Title(String title) {
        this.title = title;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public static boolean isCode(int code) {
        return code == 2;
    }

    @Override
    public int getCode() {
        return 2;
    }
}
