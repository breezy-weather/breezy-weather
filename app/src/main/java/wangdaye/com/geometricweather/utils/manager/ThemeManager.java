package wangdaye.com.geometricweather.utils.manager;

import android.content.Context;
import android.graphics.Color;

import androidx.annotation.ColorInt;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.Px;
import androidx.annotation.Size;
import androidx.core.content.ContextCompat;

import wangdaye.com.geometricweather.R;
import wangdaye.com.geometricweather.basic.model.option.DarkMode;
import wangdaye.com.geometricweather.resource.ResourceUtils;
import wangdaye.com.geometricweather.settings.SettingsOptionManager;
import wangdaye.com.geometricweather.ui.widget.weatherView.WeatherView;
import wangdaye.com.geometricweather.utils.DisplayUtils;

public class ThemeManager {

    private static volatile ThemeManager instance;
    public static ThemeManager getInstance(Context context) {
        if (instance == null) {
            synchronized (ThemeManager.class) {
                instance = new ThemeManager(context);
            }
        }
        return instance;
    }

    private @Nullable WeatherView weatherView;
    private boolean daytime;
    private DarkMode darkMode;

    private boolean lightTheme;

    public ThemeManager(Context context) {
        this.weatherView = null;
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
            this.weatherView = weatherView;
        }
        this.daytime = TimeManager.getInstance(context).isDayTime();
        this.darkMode = SettingsOptionManager.getInstance(context).getDarkMode();

        switch (darkMode) {
            case LIGHT:
                this.lightTheme = true;
                break;

            case DARK:
                this.lightTheme = false;
                break;

            default:
                this.lightTheme = daytime;
                break;
        }
    }

    @Nullable
    public WeatherView getWeatherView() {
        return weatherView;
    }

    public boolean isDaytime() {
        return daytime;
    }

    public DarkMode getDarkMode() {
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
        if (weatherView != null) {
            return weatherView.getThemeColors(lightTheme);
        }
        return new int[] {Color.TRANSPARENT, Color.TRANSPARENT, Color.TRANSPARENT};
    }

    @ColorInt
    public int getWeatherBackgroundColor() {
        if (weatherView != null) {
            return weatherView.getBackgroundColor();
        }
        return Color.TRANSPARENT;
    }

    @Px
    public int getHeaderHeight() {
        if (weatherView != null) {
            return weatherView.getHeaderHeight();
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

    @Px
    public int getListItemAdaptiveWidth(Context context) {
        if (!DisplayUtils.isLandscape(context)) {
            int containerWidth = context.getResources().getDisplayMetrics().widthPixels;
            return DisplayUtils.getTabletListAdaptiveWidth(context, containerWidth) - 2 * getCardMarginsHorizontal(context);
        } else {
            int containerWidth = context.getResources().getDisplayMetrics().widthPixels
                    - context.getResources().getDimensionPixelSize(R.dimen.main_location_container_width);
            return DisplayUtils.getTabletListAdaptiveWidth(context, containerWidth) - 2 * getCardMarginsHorizontal(context);
        }
    }
}
