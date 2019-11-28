package wangdaye.com.geometricweather.daily.adapter.model;

import wangdaye.com.geometricweather.daily.adapter.DailyWeatherAdapter;

public class Value implements DailyWeatherAdapter.ViewModel {

    private String title;
    private String value;

    public Value(String title, String value) {
        this.title = title;
        this.value = value;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public static boolean isCode(int code) {
        return code == 3;
    }

    @Override
    public int getCode() {
        return 3;
    }
}
