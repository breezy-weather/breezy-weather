package wangdaye.com.geometricweather.theme;

import android.content.Context;
import android.content.res.Configuration;

import androidx.annotation.ColorInt;

public abstract class ThemeManager {

    public abstract boolean isLightTheme(Context context);

    @ColorInt
    public abstract int getAccentColor(Context context);

    @ColorInt
    public abstract int getLineColor(Context context);

    @ColorInt
    public abstract int getRootColor(Context context);

    @ColorInt
    public abstract int getSearchBarColor(Context context);

    @ColorInt
    public abstract int getTextTitleColor(Context context);

    @ColorInt
    public abstract int getTextSubtitleColor(Context context);

    @ColorInt
    public abstract int getTextContentColor(Context context);

    protected static boolean isSystemLightMode(Context context) {
        Configuration configuration = context.getResources().getConfiguration();
        int currentNightMode = configuration.uiMode & Configuration.UI_MODE_NIGHT_MASK;
        switch (currentNightMode) {
            case Configuration.UI_MODE_NIGHT_NO:
                return true;

            case Configuration.UI_MODE_NIGHT_YES:
                return false;
        }
        return true;
    }
}
