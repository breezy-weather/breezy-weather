package wangdaye.com.geometricweather.main.ui;

import android.content.Context;

import androidx.annotation.ColorInt;
import androidx.core.content.ContextCompat;

import wangdaye.com.geometricweather.resource.ResourceUtils;

public class MainColorPicker {

    private boolean daytime;
    private String darkMode;

    private boolean lightTheme;

    public MainColorPicker(boolean daytime, String darkMode) {
        this.daytime = daytime;
        this.darkMode = darkMode;

        switch (darkMode) {
            case "light":
                this.lightTheme = true;
                break;

            case "dark":
                this.lightTheme = false;
                break;

            default:
                this.lightTheme = daytime;
                break;
        }
    }

    public boolean isDaytime() {
        return daytime;
    }

    public String getDarkMode() {
        return darkMode;
    }

    public boolean isLightTheme() {
        return lightTheme;
    }

    @ColorInt
    private int getColor(Context context, String resName, boolean light) {
        return ContextCompat.getColor(
                context,
                ResourceUtils.getResId(
                        context,
                        resName + "_" + (light ? "light" : "dark"),
                        "color"
                )
        );
    }

    @ColorInt
    public int getAccentColor(Context context) {
        return getColor(context, "colorAccent", isLightTheme());
    }

    @ColorInt
    public int getLineColor(Context context) {
        return getColor(context, "colorLine", isLightTheme());
    }

    @ColorInt
    public int getRootColor(Context context) {
        return getColor(context, "colorRoot", isLightTheme());
    }

    @ColorInt
    public int getTextTitleColor(Context context) {
        return getColor(context, "colorTextTitle", isLightTheme());
    }

    @ColorInt
    public int getTextSubtitleColor(Context context) {
        return getColor(context, "colorTextSubtitle", isLightTheme());
    }

    @ColorInt
    public int getTextContentColor(Context context) {
        return getColor(context, "colorTextContent", isLightTheme());
    }
}
