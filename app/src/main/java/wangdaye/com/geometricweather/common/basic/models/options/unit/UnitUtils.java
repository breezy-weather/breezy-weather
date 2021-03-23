package wangdaye.com.geometricweather.common.basic.models.options.unit;

import android.annotation.SuppressLint;

class UnitUtils {

    static String formatFloat(float value) {
        return formatFloat(value, 2);
    }

    static String formatFloat(float value, int decimalNumber) {
        float factor = (float) Math.pow(10, decimalNumber);
        if (Math.round(value) * factor == Math.round(value * factor)) {
            return String.valueOf(Math.round(value));
        }
        return String.format(
                "%." + decimalNumber + "f",
                value
        );
    }

    @SuppressLint("DefaultLocale")
    static String formatInt(int value) {
        return String.format("%d", value);
    }
}
