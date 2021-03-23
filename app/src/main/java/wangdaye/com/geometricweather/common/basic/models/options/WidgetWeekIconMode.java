package wangdaye.com.geometricweather.common.basic.models.options;

import android.content.Context;

import androidx.annotation.Nullable;

import wangdaye.com.geometricweather.R;
import wangdaye.com.geometricweather.common.basic.models.options._utils.Utils;

public enum WidgetWeekIconMode {

    AUTO("auto"),
    DAY("day"),
    NIGHT("night");

    private final String modeId;

    WidgetWeekIconMode(String modeId) {
        this.modeId = modeId;
    }

    @Nullable
    public String getWidgetWeekIconModeName(Context context) {
        return Utils.getNameByValue(
                context.getResources(),
                modeId,
                R.array.week_icon_modes,
                R.array.week_icon_mode_values
        );
    }

    public static WidgetWeekIconMode getInstance(String value) {
        switch (value) {
            case "day":
                return DAY;

            case "night":
                return NIGHT;

            default:
                return AUTO;
        }
    }
}
