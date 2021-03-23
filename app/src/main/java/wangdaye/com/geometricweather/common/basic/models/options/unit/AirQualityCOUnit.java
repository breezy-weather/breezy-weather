package wangdaye.com.geometricweather.common.basic.models.options.unit;

import android.content.Context;
import android.text.BidiFormatter;

import wangdaye.com.geometricweather.R;
import wangdaye.com.geometricweather.common.utils.DisplayUtils;

public enum AirQualityCOUnit {

    MGPCUM("mgpcum", 0, 1f);

    private final String unitId;
    private final int unitArrayIndex;
    private final float unitFactor; // actual air quality CO = quality(mg/m³) * factor.

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
        return getDensityText(context, mgpcum, DisplayUtils.isRtl(context));
    }

    private String getDensityText(Context context, float mgpcum, boolean rtl) {
        if (rtl) {
            return BidiFormatter.getInstance().unicodeWrap(
                    UnitUtils.formatFloat(mgpcum * unitFactor, 1)
            ) + "\u202f" + context.getResources().getStringArray(R.array.air_quality_co_units)[unitArrayIndex];
        } else {
            return UnitUtils.formatFloat(mgpcum * unitFactor, 1)
                    + "\u202f" +  context.getResources().getStringArray(R.array.air_quality_co_units)[unitArrayIndex];
        }
    }

    public String getDensityVoice(Context context, float mgpcum) {
        return getDensityVoice(context, mgpcum, DisplayUtils.isRtl(context));
    }

    private String getDensityVoice(Context context, float mgpcum, boolean rtl) {
        if (rtl) {
            return BidiFormatter.getInstance().unicodeWrap(
                    UnitUtils.formatFloat(mgpcum * unitFactor, 1)
            ) + "\u202f" +  context.getResources().getStringArray(R.array.air_quality_co_unit_voices)[unitArrayIndex];
        } else {
            return UnitUtils.formatFloat(mgpcum * unitFactor, 1)
                    + "\u202f" +  context.getResources().getStringArray(R.array.air_quality_co_unit_voices)[unitArrayIndex];
        }
    }
}
