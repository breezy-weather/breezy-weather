package wangdaye.com.geometricweather.basic.models.options.unit;

import android.annotation.SuppressLint;

class UnitUtils {

    static String formatFloat(float value) {
        return formatFloat(value, 2);
    }

    static String formatFloat(float value, int decimalNumber) {
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
