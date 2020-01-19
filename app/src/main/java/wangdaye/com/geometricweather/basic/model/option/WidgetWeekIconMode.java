package wangdaye.com.geometricweather.basic.model.option;

import android.content.Context;

import androidx.annotation.Nullable;

import wangdaye.com.geometricweather.R;
import wangdaye.com.geometricweather.basic.model.option.utils.OptionMapper;

public enum WidgetWeekIconMode {
    AUTO("auto"),
    DAY("day"),
    NIGHT("night");

    private String modeId;

    WidgetWeekIconMode(String modeId) {
        this.modeId = modeId;
    }

    @Nullable
    public String getWidgetWeekIconModeName(Context context) {
        return OptionMapper.getNameByValue(
                context,
                modeId,
                R.array.week_icon_modes,
                R.array.week_icon_mode_values
        );
    }
}
