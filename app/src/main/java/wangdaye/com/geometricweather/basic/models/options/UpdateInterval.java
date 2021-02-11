package wangdaye.com.geometricweather.basic.models.options;

import android.content.Context;

import androidx.annotation.Nullable;

import wangdaye.com.geometricweather.R;
import wangdaye.com.geometricweather.basic.models.options._utils.Utils;

public enum UpdateInterval {

    INTERVAL_0_30("0:30", 0.5f),
    INTERVAL_1_00("1:00", 1.0f),
    INTERVAL_1_30("1:30", 1.5f),
    INTERVAL_2_00("2:00", 2.0f),
    INTERVAL_2_30("2:30", 2.5f),
    INTERVAL_3_00("3:00", 3.0f),
    INTERVAL_3_30("3:30", 3.5f),
    INTERVAL_4_00("4:00", 4.0f),
    INTERVAL_4_30("4:30", 4.5f),
    INTERVAL_5_00("5:00", 5.0f),
    INTERVAL_5_30("5:30", 5.5f),
    INTERVAL_6_00("6:00", 6.0f);

    private final String value;
    private final float intervalInHour;

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
        return Utils.getNameByValue(
                context.getResources(),
                value,
                R.array.automatic_refresh_rates,
                R.array.automatic_refresh_rate_values
        );
    }

    public static UpdateInterval getInstance(String value) {
        switch (value) {
            case "0:30":
                return INTERVAL_0_30;

            case "1:00":
                return INTERVAL_1_00;

            case "2:00":
                return INTERVAL_2_00;

            case "2:30":
                return INTERVAL_2_30;

            case "3:00":
                return INTERVAL_3_00;

            case "3:30":
                return INTERVAL_3_30;

            case "4:00":
                return INTERVAL_4_00;

            case "4:30":
                return INTERVAL_4_30;

            case "5:00":
                return INTERVAL_5_00;

            case "5:30":
                return INTERVAL_5_30;

            case "6:00":
                return INTERVAL_6_00;

            default:
                return INTERVAL_1_30;

        }
    }
}
