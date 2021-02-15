package wangdaye.com.geometricweather.utils.managers;

import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Color;

import androidx.annotation.ColorInt;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.Px;
import androidx.annotation.Size;
import androidx.core.content.ContextCompat;

import wangdaye.com.geometricweather.R;
import wangdaye.com.geometricweather.basic.models.options.DarkMode;
import wangdaye.com.geometricweather.resource.ResourceUtils;
import wangdaye.com.geometricweather.settings.SettingsOptionManager;
import wangdaye.com.geometricweather.ui.widgets.weatherView.WeatherView;
import wangdaye.com.geometricweather.utils.DisplayUtils;

public class ThemeManager {

    private static volatile ThemeManager sInstance;
    public static ThemeManager getInstance(Context context) {
        if (sInstance == null) {
            synchronized (ThemeManager.class) {
                sInstance = new ThemeManager(context);
            }
        }
        return sInstance;
    }

    private @Nullable WeatherView mWeatherView;
    private boolean mDaytime;
    private DarkMode mDarkMode;

    private boolean mLightTheme;

    public ThemeManager(Context context) {
        mWeatherView = null;
        update(context);
    }

    public void update(Context context) {
        innerUpdate(context, null);
    }

    public void update(Context context, @NonNull WeatherView weatherView) {
        innerUpdate(context, weatherView);
    }

    public void innerUpdate(Context context, @Nullable WeatherView weatherView) {
        if (weatherView != null) {
            mWeatherView = weatherView;
        }
        mDaytime = TimeManager.getInstance(context).isDayTime();
        mDarkMode = SettingsOptionManager.getInstance(context).getDarkMode();

        switch (mDarkMode) {
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
    }

    @Nullable
    public WeatherView getWeatherView() {
        return mWeatherView;
    }

    public boolean isDaytime() {
        return mDaytime;
    }

    public DarkMode getDarkMode() {
        return mDarkMode;
    }

    public boolean isLightTheme() {
        return mLightTheme;
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

    /**
     * @return colors[] {
     *     theme color,
     *     color of daytime chart line,
     *     color of nighttime chart line
     * }
     *
     * */
    @ColorInt @Size(3)
    public int[] getWeatherThemeColors() {
        if (mWeatherView != null) {
            return mWeatherView.getThemeColors(mLightTheme);
        }
        return new int[] {Color.TRANSPARENT, Color.TRANSPARENT, Color.TRANSPARENT};
    }

    @ColorInt
    public int getWeatherBackgroundColor() {
        if (mWeatherView != null) {
            return mWeatherView.getBackgroundColor();
        }
        return Color.TRANSPARENT;
    }

    @Px
    public int getHeaderHeight() {
        if (mWeatherView != null) {
            return mWeatherView.getHeaderHeight();
        }
        return 0;
    }

    @ColorInt
    public int getHeaderTextColor(Context context) {
        return Color.WHITE;
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
    public int getSearchBarColor(Context context) {
        return getColor(context, "colorSearchBarBackground", isLightTheme());
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
        return (int) DisplayUtils.dpToPx(context, 8);
    }

    @Px
    public int getCardElevation(Context context) {
        return (int) DisplayUtils.dpToPx(context, 2);
    }

    public static boolean isSystemLightMode(Context context) {
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
