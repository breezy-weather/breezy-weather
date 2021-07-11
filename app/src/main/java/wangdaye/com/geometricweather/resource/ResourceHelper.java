package wangdaye.com.geometricweather.resource;

import android.animation.Animator;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.Icon;
import android.net.Uri;
import android.os.Build;

import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.annotation.Size;

import wangdaye.com.geometricweather.R;
import wangdaye.com.geometricweather.common.basic.models.options.NotificationTextColor;
import wangdaye.com.geometricweather.common.basic.models.weather.WeatherCode;
import wangdaye.com.geometricweather.resource.providers.DefaultResourceProvider;
import wangdaye.com.geometricweather.resource.providers.ResourceProvider;
import wangdaye.com.geometricweather.resource.utils.ResourceUtils;

public class ResourceHelper {

    @NonNull
    public static Drawable getWeatherIcon(ResourceProvider provider,
                                          WeatherCode code, boolean dayTime) {
        return provider.getWeatherIcon(code, dayTime);
    }

    @Size(3)
    public static Drawable[] getWeatherIcons(ResourceProvider provider,
                                             WeatherCode code, boolean dayTime) {
        return provider.getWeatherIcons(code, dayTime);
    }

    @Size(3)
    public static Animator[] getWeatherAnimators(ResourceProvider provider,
                                                 WeatherCode code, boolean dayTime) {
        return provider.getWeatherAnimators(code, dayTime);
    }

    @NonNull
    public static Drawable getWidgetNotificationIcon(ResourceProvider provider,
                                                     WeatherCode code, boolean dayTime,
                                                     boolean minimal, String textColor) {
        if (minimal) {
            switch (textColor) {
                case "light":
                    return provider.getMinimalLightIcon(code, dayTime);

                case "grey":
                    return provider.getMinimalGreyIcon(code, dayTime);

                case "dark":
                    return provider.getMinimalDarkIcon(code, dayTime);
            }
        }

        return provider.getWeatherIcon(code, dayTime);
    }

    @NonNull
    public static Drawable getWidgetNotificationIcon(ResourceProvider provider,
                                                     WeatherCode code, boolean dayTime,
                                                     boolean minimal, boolean darkText) {
        return getWidgetNotificationIcon(
                provider, code, dayTime, minimal, darkText ? "dark" : "light");
    }

    @NonNull
    public static Uri getWidgetNotificationIconUri(ResourceProvider provider,
                                                   WeatherCode code, boolean dayTime,
                                                   boolean minimal, NotificationTextColor textColor) {
        if (minimal) {
            switch (textColor) {
                case LIGHT:
                    return provider.getMinimalLightIconUri(code, dayTime);

                case GREY:
                    return provider.getMinimalGreyIconUri(code, dayTime);

                case DARK:
                    return provider.getMinimalDarkIconUri(code, dayTime);
            }
        }

        return provider.getWeatherIconUri(code, dayTime);
    }

    @NonNull
    public static Drawable getMinimalXmlIcon(ResourceProvider provider,
                                             WeatherCode code, boolean daytime) {
        return provider.getMinimalXmlIcon(code, daytime);
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    @NonNull
    public static Icon getMinimalIcon(ResourceProvider provider,
                                      WeatherCode code, boolean daytime) {
        return provider.getMinimalIcon(code, daytime);
    }

    @DrawableRes
    public static int getDefaultMinimalXmlIconId(WeatherCode code, boolean daytime) {
        int id = new DefaultResourceProvider().getMinimalXmlIconId(code, daytime);
        if (id == 0) {
            return R.drawable.weather_clear_day_mini_xml;
        } else {
            return id;
        }
    }

    @NonNull
    public static Drawable getShortcutsIcon(ResourceProvider provider,
                                            WeatherCode code, boolean dayTime) {
        return provider.getShortcutsIcon(code, dayTime);
    }

    @NonNull
    public static Drawable getShortcutsForegroundIcon(ResourceProvider provider,
                                                      WeatherCode code, boolean dayTime) {
        return provider.getShortcutsForegroundIcon(code, dayTime);
    }

    @NonNull
    public static Drawable getSunDrawable(ResourceProvider provider) {
        return provider.getSunDrawable();
    }

    @NonNull
    public static Drawable getMoonDrawable(ResourceProvider provider) {
        return provider.getMoonDrawable();
    }

    @DrawableRes
    public static int getTempIconId(Context context, int temp) {
        StringBuilder builder = new StringBuilder("notif_temp_");
        if (temp < 0) {
            builder.append("neg_");
        }
        builder.append(Math.abs(temp));

        int id = ResourceUtils.getResId(context, builder.toString(), "drawable");
        if (id == 0) {
            return R.drawable.notif_temp_0;
        } else {
            return id;
        }
    }
}
