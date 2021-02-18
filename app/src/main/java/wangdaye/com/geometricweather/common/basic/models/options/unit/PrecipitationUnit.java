package wangdaye.com.geometricweather.common.basic.models.options.unit;

import android.content.Context;

import wangdaye.com.geometricweather.R;

public enum PrecipitationUnit {

    MM("mm", 0, 1f),
    CM("cm", 1, 0.1f),
    IN("in", 2, 0.0394f),
    LPSQM("lpsqm", 3, 1f);

    private final String unitId;
    private final int unitArrayIndex;
    private final float unitFactor; // actual precipitation = precipitation(mm) * factor.

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

    public float getMilliMeters(float value) {
        return value / unitFactor;
    }

    public String getMilliMetersTextWithoutUnit(float value) {
        return UnitUtils.formatFloat(value / unitFactor, 1);
    }

    public String getAbbreviation(Context context) {
        return context.getResources().getStringArray(R.array.precipitation_units)[unitArrayIndex];
    }

    public String getPrecipitationVoice(Context context, float mm) {
        return getPrecipitationTextWithoutUnit(mm)
                + context.getResources().getStringArray(R.array.precipitation_unit_voices)[unitArrayIndex];
    }

    public static PrecipitationUnit getInstance(String value) {
        switch (value) {
            case "cm":
                return CM;

            case "in":
                return IN;

            case "lpsqm":
                return LPSQM;

            default:
                return MM;
        }
    }
}
