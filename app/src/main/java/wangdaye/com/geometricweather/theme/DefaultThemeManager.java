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
    public int getSeparatorColor(Context context) {
        return ContextCompat.getColor(context, R.color.colorSeparator);
    }

    @Override
    public int getRootColor(Context context) {
        return ContextCompat.getColor(context, R.color.colorRoot);
    }

    @Override
    public int getSurfaceColor(Context context) {
        return ContextCompat.getColor(context, R.color.colorSurface);
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
