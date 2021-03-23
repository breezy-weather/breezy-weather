package wangdaye.com.geometricweather.common.basic.models.options.unit;

import android.content.Context;
import android.text.BidiFormatter;

import wangdaye.com.geometricweather.R;
import wangdaye.com.geometricweather.common.utils.DisplayUtils;

public enum TemperatureUnit {

    C("c", 0, c -> c),
    F("f", 1, c -> (int) (32 + c * 1.8f)),
    K("k", 2, c -> (int) (273.15 + c));

    private final String unitId;
    private final int unitArrayIndex;
    private final Calculator unitCalculator;

    public interface Calculator {
        int getTemperature(int c);
    }

    TemperatureUnit(String id, int index, Calculator calculator) {
        unitId = id;
        unitArrayIndex = index;
        unitCalculator = calculator;
    }

    public String getUnitId() {
        return unitId;
    }

    public int getTemperature(int c) {
        return unitCalculator.getTemperature(c);
    }

    public String getTemperatureText(Context context, int c) {
        return getTemperatureText(context, c, DisplayUtils.isRtl(context));
    }

    private String getTemperatureText(Context context, int c, boolean rtl) {
        if (rtl) {
            return BidiFormatter.getInstance().unicodeWrap(
                    UnitUtils.formatInt(getTemperature(c))
            ) + "\u202f" +  getAbbreviation(context);
        } else {
            return getTemperature(c) + "\u202f" +  getAbbreviation(context);
        }
    }

    public String getLongTemperatureText(Context context, int c) {
        return getLongTemperatureText(context, c, DisplayUtils.isRtl(context));
    }

    private String getLongTemperatureText(Context context, int c, boolean rtl) {
        if (rtl) {
            return BidiFormatter.getInstance().unicodeWrap(
                    UnitUtils.formatInt(getTemperature(c))
            ) + "\u202f" +  getLongAbbreviation(context);
        } else {
            return getTemperature(c) + "\u202f" +  getLongAbbreviation(context);
        }
    }

    public String getShortTemperatureText(Context context, int c) {
        return getShortTemperatureText(context, c, DisplayUtils.isRtl(context));
    }

    private String getShortTemperatureText(Context context, int c, boolean rtl) {
        if (rtl) {
            return BidiFormatter.getInstance().unicodeWrap(
                    UnitUtils.formatInt(getTemperature(c))
            ) + getShortAbbreviation(context);
        } else {
            return getTemperature(c) + getShortAbbreviation(context);
        }
    }

    public String getAbbreviation(Context context) {
        return context.getResources().getStringArray(R.array.temperature_units)[unitArrayIndex];
    }

    public String getLongAbbreviation(Context context) {
        return context.getResources().getStringArray(R.array.temperature_units_long)[unitArrayIndex];
    }

    public String getShortAbbreviation(Context context) {
        return context.getResources().getStringArray(R.array.temperature_units_short)[unitArrayIndex];
    }

    public static TemperatureUnit getInstance(String value) {
        switch (value) {
            case "f":
                return F;

            case "k":
                return K;

            default:
                return C;
        }
    }
}