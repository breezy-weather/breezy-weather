package wangdaye.com.geometricweather.basic.model.option;

import android.content.Context;

import androidx.annotation.Nullable;

import wangdaye.com.geometricweather.R;
import wangdaye.com.geometricweather.basic.model.option.utils.OptionUtils;

public enum UpdateInterval {

    INTERVAL_0_30("0:30", 0.5f),
    INTERVAL_1_00("1:00", 1.0f),
    INTERVAL_1_30("1:30", 1.5f),
    INTERVAL_2_00("2:00", 2.0f),
    INTERVAL_2_30("2:30", 2.5f),
    INTERVAL_3_00("3:00", 3.0f),
    INTERVAL_3_30("3:30", 3.5f),
    INTERVAL_4_00("4:00", 4.0f);

    private String value;
    private float intervalInHour;

    UpdateInterval(String value, float intervalInHour) {
        this.value = value;
        this.intervalInHour = intervalInHour;
    }

    public String getValue() {
        return value;
    }

    public float getIntervalInHour() {
        return intervalInHour;
    }

    @Nullable
    public String getUpdateIntervalName(Context context) {
        return OptionUtils.getNameByValue(
                context,
                value,
                R.array.automatic_refresh_rates,
                R.array.automatic_refresh_rate_values
        );
    }
}
