package wangdaye.com.geometricweather.basic.model.option.unit;

import android.content.Context;

import wangdaye.com.geometricweather.R;

public enum DurationUnit {

    H("h", 0, 1f);

    private String unitId;
    private int unitArrayIndex;
    private float unitFactor; // actual duration = duration(h) * factor.

    DurationUnit(String id, int arrayIndex, float factor) {
        unitId = id;
        unitArrayIndex = arrayIndex;
        unitFactor = factor;
    }

    public String getUnitId() {
        return unitId;
    }

    public String getDurationText(Context context, float h) {
        return UnitUtils.formatFloat(h * unitFactor, 1)
                + context.getResources().getStringArray(R.array.duration_units)[unitArrayIndex];
    }
}
