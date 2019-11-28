package wangdaye.com.geometricweather.daily.adapter.model;

import androidx.annotation.DrawableRes;
import androidx.annotation.Nullable;

import wangdaye.com.geometricweather.daily.adapter.DailyWeatherAdapter;

public class Title implements DailyWeatherAdapter.ViewModel {

    private @Nullable @DrawableRes Integer resId;
    private String title;

    public Title(String title) {
        this(null, title);
    }

    public Title(@Nullable Integer resId, String title) {
        this.resId = resId;
        this.title = title;
    }

    @Nullable
    public Integer getResId() {
        return resId;
    }

    public void setResId(@Nullable Integer resId) {
        this.resId = resId;
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
