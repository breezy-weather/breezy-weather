package wangdaye.com.geometricweather.common.basic.models.options;

import android.content.Context;

import androidx.annotation.Nullable;

import wangdaye.com.geometricweather.R;
import wangdaye.com.geometricweather.common.basic.models.options._utils.Utils;

public enum NotificationStyle {

    NATIVE("native"),
    CITIES("cities"),
    DAILY("daily"),
    HOURLY("hourly");

    private final String styleId;

    NotificationStyle(String styleId) {
        this.styleId = styleId;
    }

    @Nullable
    public String getNotificationStyleName(Context context) {
        return Utils.getNameByValue(
                context.getResources(),
                styleId,
                R.array.notification_styles,
                R.array.notification_style_values
        );
    }

    public static NotificationStyle getInstance(String value) {
        switch (value) {
            case "native":
                return NATIVE;

            case "cities":
                return CITIES;

            case "daily":
                return DAILY;

            default:
                return HOURLY;
        }
    }
}
