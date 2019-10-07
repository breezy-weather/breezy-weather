package wangdaye.com.geometricweather.basic.model.option;

import android.content.Context;

import androidx.annotation.Nullable;

import wangdaye.com.geometricweather.R;
import wangdaye.com.geometricweather.basic.model.option.utils.OptionUtils;

public enum DarkMode {
    AUTO("auto"),
    LIGHT("light"),
    DARK("dark");

    private String modeId;

    DarkMode(String modeId) {
        this.modeId = modeId;
    }

    @Nullable
    public String getDarkModeName(Context context) {
        return OptionUtils.getNameByValue(
                context,
                modeId,
                R.array.dark_modes,
                R.array.dark_mode_values
        );
    }
}
