package wangdaye.com.geometricweather.basic.model.option;

import android.content.Context;

import androidx.annotation.Nullable;

import wangdaye.com.geometricweather.R;
import wangdaye.com.geometricweather.basic.model.option.utils.OptionMapper;

public enum NotificationStyle {
    NATIVE("native"),
    CITIES("cities"),
    DAILY("daily"),
    HOURLY("hourly");

    private String styleId;

    NotificationStyle(String styleId) {
        this.styleId = styleId;
    }

    @Nullable
    public String getNotificationStyleName(Context context) {
        return OptionMapper.getNameByValue(
                context,
                styleId,
                R.array.notification_styles,
                R.array.notification_style_values
        );
    }
}
