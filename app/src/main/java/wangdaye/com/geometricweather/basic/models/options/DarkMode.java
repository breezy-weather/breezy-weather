package wangdaye.com.geometricweather.basic.models.options;

import android.content.Context;

import androidx.annotation.Nullable;

import wangdaye.com.geometricweather.R;
import wangdaye.com.geometricweather.basic.models.options._utils.Utils;

public enum DarkMode {

    AUTO("auto"),
    SYSTEM("system"),
    LIGHT("light"),
    DARK("dark");

    private final String modeId;

    DarkMode(String modeId) {
        this.modeId = modeId;
    }

    @Nullable
    public String getDarkModeName(Context context) {
        return Utils.getNameByValue(
                context.getResources(),
                modeId,
                R.array.dark_modes,
                R.array.dark_mode_values
        );
    }

    public static DarkMode getInstance(String value) {
        switch (value) {
            case "system":
                return SYSTEM;

            case "light":
                return LIGHT;

            case "dark":
                return DARK;

            default:
                return AUTO;
        }
    }
}
