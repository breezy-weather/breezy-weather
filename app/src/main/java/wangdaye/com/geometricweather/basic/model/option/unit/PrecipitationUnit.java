package wangdaye.com.geometricweather.basic.model.option.unit;

import android.content.Context;

import wangdaye.com.geometricweather.R;

public enum PrecipitationUnit {

    MM("mm", 0, 1f),
    IN("in", 1, 0.0394f),
    LPSQM("lpsqm", 2, 1f);

    private String unitId;
    private int unitArrayIndex;
    private float unitFactor; // actual precipitation = precipitation(mm) * factor.

    PrecipitationUnit(String id, int arrayIndex, float factor) {
        unitId = id;
        unitArrayIndex = arrayIndex;
        unitFactor = factor;
    }

    public String getUnitId() {
        return unitId;
    }

    public float getPrecipitation(float mm) {
        return mm * unitFactor;
    }

    public String getPrecipitationText(Context context, float mm) {
        return getPrecipitationTextWithoutUnit(mm) + getAbbreviation(context);
    }

    public String getPrecipitationTextWithoutUnit(float mm) {
        return UnitUtils.formatFloat(mm * unitFactor, 1);
    }

    public String getAbbreviation(Context context) {
        return context.getResources().getStringArray(R.array.precipitation_units)[unitArrayIndex];
    }
}
