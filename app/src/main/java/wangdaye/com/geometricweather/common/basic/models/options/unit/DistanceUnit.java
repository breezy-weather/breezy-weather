package wangdaye.com.geometricweather.common.basic.models.options.unit;

import android.content.Context;

import wangdaye.com.geometricweather.R;

public enum DistanceUnit {

    KM("km", 0, 1f),
    M("m", 1, 1000f),
    MI("mi", 2, 0.6213f),
    NMI("nmi", 3, 0.5399f),
    FT("ft", 4, 3280.8398f);

    private final String unitId;
    private final int unitArrayIndex;
    private final float unitFactor; // actual distance = distance(km) * factor.

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
        return UnitUtils.formatFloat(km * unitFactor, 2) + "\u202f" +  getAbbreviation(context);
    }

    public String getAbbreviation(Context context) {
        return context.getResources().getStringArray(R.array.distance_units)[unitArrayIndex];
    }

    public String getDistanceVoice(Context context, float km) {
        return UnitUtils.formatFloat(km * unitFactor, 2)
                + "\u202f" +  context.getResources().getStringArray(R.array.distance_unit_voices)[unitArrayIndex];
    }

    public static DistanceUnit getInstance(String value) {
        switch (value) {
            case "m":
                return M;

            case "mi":
                return MI;

            case "nmi":
                return NMI;

            case "ft":
                return FT;

            default:
                return KM;
        }
    }
}
