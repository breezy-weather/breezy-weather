package wangdaye.com.geometricweather.common.basic.models.options.unit;

import android.content.Context;

import wangdaye.com.geometricweather.R;

public enum SpeedUnit {

    KPH("kph", 0, 1f),
    MPS("mps", 1, 1f / 3.6f),
    KN("kn", 2, 1f / 1.852f),
    MPH("mph", 3, 1f / 1.609f),
    FTPS("ftps", 4, 0.9113f);

    private final String unitId;
    private final int unitArrayIndex;
    private final float unitFactor; // actual speed = speed(km/h) * factor.

    SpeedUnit(String id, int arrayIndex, float factor) {
        unitId = id;
        unitArrayIndex = arrayIndex;
        unitFactor = factor;
    }

    public String getUnitId() {
        return unitId;
    }

    public float getSpeed(float kph) {
        return kph * unitFactor;
    }

    public String getSpeedText(Context context, float kph) {
        return getSpeedTextWithoutUnit(kph) + "\u202f" +  getAbbreviation(context);
    }

    public String getSpeedTextWithoutUnit(float kph) {
        return UnitUtils.formatFloat(kph * unitFactor, 1);
    }

    public String getAbbreviation(Context context) {
        return context.getResources().getStringArray(R.array.speed_units)[unitArrayIndex];
    }

    public String getSpeedVoice(Context context, float kph) {
        return getSpeedTextWithoutUnit(kph)
                + "\u202f" +  context.getResources().getStringArray(R.array.speed_unit_voices)[unitArrayIndex];
    }

    public static SpeedUnit getInstance(String value) {
        switch (value) {
            case "mps":
                return MPS;

            case "kn":
                return KN;

            case "mph":
                return MPH;

            case "ftps":
                return FTPS;

            default:
                return KPH;
        }
    }
}
