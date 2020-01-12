package wangdaye.com.geometricweather.basic.model.option.unit;

import android.content.Context;

import wangdaye.com.geometricweather.R;

public enum SpeedUnit {

    KPH("kph", 0, 1f),
    MPS("mps", 1, 1f / 3.6f),
    KN("kn", 2, 1f / 1.852f),
    MPH("mph", 3, 1f / 1.609f),
    FTPS("ftps", 4, 0.9113f);

    private String unitId;
    private int unitArrayIndex;
    private float unitFactor; // actual speed = speed(km/h) * factor.

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
        return getSpeedTextWithoutUnit(kph) + getAbbreviation(context);
    }

    public String getSpeedTextWithoutUnit(float kph) {
        return UnitUtils.formatFloat(kph * unitFactor, 1);
    }

    public String getAbbreviation(Context context) {
        return context.getResources().getStringArray(R.array.speed_units)[unitArrayIndex];
    }
}
