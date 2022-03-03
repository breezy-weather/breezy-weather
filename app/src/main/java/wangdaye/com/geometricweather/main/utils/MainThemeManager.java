package wangdaye.com.geometricweather.main.utils;

import android.content.Context;
import android.graphics.Color;

import androidx.annotation.ColorInt;
import androidx.annotation.Nullable;
import androidx.annotation.Px;
import androidx.annotation.Size;
import androidx.core.content.ContextCompat;

import java.util.TimeZone;

import javax.inject.Inject;

import dagger.hilt.android.qualifiers.ApplicationContext;
import wangdaye.com.geometricweather.R;
import wangdaye.com.geometricweather.common.basic.models.Location;
import wangdaye.com.geometricweather.theme.weatherView.WeatherView;
import wangdaye.com.geometricweather.common.utils.DisplayUtils;
import wangdaye.com.geometricweather.theme.ThemeManager;
import wangdaye.com.geometricweather.theme.resource.utils.ResourceUtils;
import wangdaye.com.geometricweather.settings.ConfigStore;
import wangdaye.com.geometricweather.settings.SettingsManager;

public class MainThemeManager extends ThemeManager {

    private @Nullable WeatherView mWeatherView;
    private boolean mDaytime;
    private boolean mLightTheme;

    private static final String PREFERENCE_NAME = "time_preference";
    private static final String KEY_DAY_TIME = "day_time";

    @Inject
    public MainThemeManager(@ApplicationContext Context context) {
        mWeatherView = null;
        mDaytime = ConfigStore.getInstance(context, PREFERENCE_NAME).getBoolean(
                KEY_DAY_TIME, DisplayUtils.isDaylight(TimeZone.getDefault()));
        update(context, null);
    }

    public synchronized MainThemeManager update(Context context, @Nullable Location location) {
        updateDaytime(context, location);

        switch (SettingsManager.getInstance(context).getDarkMode()) {
            case AUTO:
                mLightTheme = mDaytime;
                break;

            case SYSTEM:
                mLightTheme = isSystemLightMode(context);
                break;

            case LIGHT:
                mLightTheme = true;
                break;

            case DARK:
                mLightTheme = false;
                break;
        }

        return this;
    }

    private synchronized void updateDaytime(Context context, @Nullable Location location) {
        if (location == null) {
            return;
        }

        mDaytime = location.isDaylight();

        ConfigStore.getInstance(context, PREFERENCE_NAME)
                .edit()
                .putBoolean(KEY_DAY_TIME, mDaytime)
                .apply();
    }

    public synchronized void registerWeatherView(WeatherView weatherView) {
        mWeatherView = weatherView;
    }

    public synchronized void unregisterWeatherView() {
        mWeatherView = null;
    }

    public synchronized boolean isLightTheme() {
        return mLightTheme;
    }

    public boolean isDaytime() {
        return mDaytime;
    }

    @ColorInt
    private static int getColor(Context context, String resName, boolean light) {
        return ContextCompat.getColor(
                context,
                ResourceUtils.getResId(
                        context,
                        resName + "_" + (light ? "light" : "dark"),
                        "color"
                )
        );
    }

    /**
     * @return colors[] {
     *     theme color,
     *     color of daytime chart line,
     *     color of nighttime chart line
     * }
     *
     * */
    @ColorInt @Size(3)
    public synchronized int[] getWeatherThemeColors() {
        if (mWeatherView != null) {
            return mWeatherView.getThemeColors(isLightTheme());
        }
        return new int[] {Color.TRANSPARENT, Color.TRANSPARENT, Color.TRANSPARENT};
    }

    @ColorInt
    public synchronized int getWeatherBackgroundColor() {
        if (mWeatherView != null) {
            return mWeatherView.getBackgroundColor();
        }
        return Color.TRANSPARENT;
    }

    @Px
    public synchronized int getHeaderHeight() {
        if (mWeatherView != null) {
            return mWeatherView.getHeaderHeight();
        }
        return 0;
    }

    @ColorInt
    public synchronized int getHeaderTextColor(Context context) {
        return Color.WHITE;
    }

    @Override
    public boolean isLightTheme(Context context) {
        return isLightTheme();
    }

    @Override
    public int getAccentColor(Context context) {
        return getColor(context, "colorAccent", isLightTheme());
    }

    @Override
    public int getLineColor(Context context) {
        return getColor(context, "colorLine", isLightTheme());
    }

    @Override
    public int getRootColor(Context context) {
        return getColor(context, "colorRoot", isLightTheme());
    }

    @Override
    public int getSearchBarColor(Context context) {
        return getColor(context, "colorSearchBarBackground", isLightTheme());
    }

    @Override
    public int getTextTitleColor(Context context) {
        return getColor(context, "colorTextTitle", isLightTheme());
    }

    @Override
    public int getTextSubtitleColor(Context context) {
        return getColor(context, "colorTextSubtitle", isLightTheme());
    }

    @Override
    public int getTextContentColor(Context context) {
        return getColor(context, "colorTextContent", isLightTheme());
    }

    @Px
    public int getCardMarginsVertical(Context context) {
        return context.getResources().getDimensionPixelSize(R.dimen.little_margin);
    }

    @Px
    public int getCardMarginsHorizontal(Context context) {
        return context.getResources().getDimensionPixelSize(R.dimen.little_margin);
    }

    @Px
    public int getCardRadius(Context context) {
        return context.getResources().getDimensionPixelSize(R.dimen.view_corner_radius);
    }

    @Px
    public int getCardElevation(Context context) {
        return (int) DisplayUtils.dpToPx(context, 2);
    }
}
