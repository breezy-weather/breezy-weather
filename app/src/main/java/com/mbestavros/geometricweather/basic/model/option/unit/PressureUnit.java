package com.mbestavros.geometricweather.basic.model.option.unit;

import android.content.Context;

import com.mbestavros.geometricweather.R;

public enum PressureUnit {

    MB("mb", 0, 1f),
    KPA("kpa", 1, 0.1f),
    HPA("hpa", 2, 1f),
    ATM("atm", 3, 0.0009869f),
    MMHG("mmhg", 4, 0.75006f),
    INHG("inhg", 5, 0.02953f),
    KGFPSQCM("kgfpsqcm", 6, 0.00102f);

    private String unitId;
    private int unitArrayIndex;
    private float unitFactor; // actual pressure = pressure(mb) * factor.

    PressureUnit(String id, int arrayIndex, float factor) {
        unitId = id;
        unitArrayIndex = arrayIndex;
        unitFactor = factor;
    }

    public String getUnitId() {
        return unitId;
    }

    public float getPressure(float mb) {
        return mb * unitFactor;
    }

    public String getPressureText(Context context, float mb) {
        return UnitUtils.formatFloat(mb * unitFactor) + getAbbreviation(context);
    }

    public String getAbbreviation(Context context) {
        return context.getResources().getStringArray(R.array.pressure_units)[unitArrayIndex];
    }
}
