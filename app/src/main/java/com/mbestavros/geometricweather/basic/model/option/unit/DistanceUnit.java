package com.mbestavros.geometricweather.basic.model.option.unit;

import android.content.Context;

import com.mbestavros.geometricweather.R;

public enum DistanceUnit {

    KM("km", 0, 1f),
    M("m", 1, 1000f),
    MI("mi", 2, 0.6213f),
    NMI("nmi", 3, 0.5399f),
    FT("ft", 4, 3280.8398f);

    private String unitId;
    private int unitArrayIndex;
    private float unitFactor; // actual distance = distance(km) * factor.

    DistanceUnit(String id, int arrayIndex, float factor) {
        unitId = id;
        unitArrayIndex = arrayIndex;
        unitFactor = factor;
    }

    public String getUnitId() {
        return unitId;
    }

    public float getDistance(float km) {
        return km * unitFactor;
    }

    public String getDistanceText(Context context, float km) {
        return UnitUtils.formatFloat(km * unitFactor, 2) + getAbbreviation(context);
    }

    public String getAbbreviation(Context context) {
        return context.getResources().getStringArray(R.array.distance_units)[unitArrayIndex];
    }
}
