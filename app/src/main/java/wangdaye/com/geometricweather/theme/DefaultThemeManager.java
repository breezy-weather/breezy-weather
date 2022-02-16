package wangdaye.com.geometricweather.theme;

import android.content.Context;

import androidx.core.content.ContextCompat;

import wangdaye.com.geometricweather.R;

public class DefaultThemeManager extends ThemeManager {

    @Override
    public boolean isLightTheme(Context context) {
        return isSystemLightMode(context);
    }

    @Override
    public int getAccentColor(Context context) {
        return ContextCompat.getColor(context, R.color.colorAccent);
    }

    @Override
    public int getLineColor(Context context) {
        return ContextCompat.getColor(context, R.color.colorLine);
    }

    @Override
    public int getRootColor(Context context) {
        return ContextCompat.getColor(context, R.color.colorRoot);
    }

    @Override
    public int getSearchBarColor(Context context) {
        return ContextCompat.getColor(context, R.color.colorSearchBarBackground);
    }

    @Override
    public int getTextTitleColor(Context context) {
        return ContextCompat.getColor(context, R.color.colorTextTitle);
    }

    @Override
    public int getTextSubtitleColor(Context context) {
        return ContextCompat.getColor(context, R.color.colorTextSubtitle);
    }

    @Override
    public int getTextContentColor(Context context) {
        return ContextCompat.getColor(context, R.color.colorTextContent);
    }
}
