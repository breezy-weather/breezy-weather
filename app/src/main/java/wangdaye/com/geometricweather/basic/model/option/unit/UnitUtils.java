package wangdaye.com.geometricweather.basic.model.option.unit;

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
}
