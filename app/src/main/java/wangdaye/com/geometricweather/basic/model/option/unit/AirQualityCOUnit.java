package wangdaye.com.geometricweather.basic.model.option.unit;

import android.content.Context;
import android.text.BidiFormatter;

import wangdaye.com.geometricweather.R;
import wangdaye.com.geometricweather.utils.DisplayUtils;

public enum AirQualityCOUnit {

    MGPCUM("mgpcum", 0, 1f);

    private String unitId;
    private int unitArrayIndex;
    private float unitFactor; // actual air quality CO = quality(mg/m³) * factor.

    AirQualityCOUnit(String id, int arrayIndex, float factor) {
        unitId = id;
        unitArrayIndex = arrayIndex;
        unitFactor = factor;
    }

    public String getUnitId() {
        return unitId;
    }

    public float getDensity(float mgpcum) {
        return mgpcum * unitFactor;
    }

    public String getDensityText(Context context, float mgpcum) {
        if (DisplayUtils.isRtl(context)) {
            return BidiFormatter.getInstance().unicodeWrap(
                    UnitUtils.formatFloat(mgpcum * unitFactor, 1)
            ) + context.getResources().getStringArray(R.array.air_quality_units)[unitArrayIndex];
        } else {
            return UnitUtils.formatFloat(mgpcum * unitFactor, 1)
                    + context.getResources().getStringArray(R.array.air_quality_units)[unitArrayIndex];
        }
    }
}
