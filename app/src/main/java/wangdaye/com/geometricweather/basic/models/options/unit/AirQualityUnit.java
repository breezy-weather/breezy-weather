package wangdaye.com.geometricweather.basic.models.options.unit;

import android.content.Context;
import android.text.BidiFormatter;

import wangdaye.com.geometricweather.R;
import wangdaye.com.geometricweather.utils.DisplayUtils;

public enum AirQualityUnit {

    MUGPCUM("mugpcum", 0, 1f);

    private final String unitId;
    private final int unitArrayIndex;
    private final float unitFactor; // actual air quality = quality(μg/m³) * factor.

    AirQualityUnit(String id, int arrayIndex, float factor) {
        unitId = id;
        unitArrayIndex = arrayIndex;
        unitFactor = factor;
    }

    public String getUnitId() {
        return unitId;
    }

    public float getDensity(float mugpcum) {
        return mugpcum * unitFactor;
    }

    public String getDensityText(Context context, float mugpcum) {
        return getDensityText(context, mugpcum, DisplayUtils.isRtl(context));
    }

    private String getDensityText(Context context, float mugpcum, boolean rtl) {
        if (rtl) {
            return BidiFormatter.getInstance().unicodeWrap(
                    UnitUtils.formatFloat(mugpcum * unitFactor, 1)
            ) + context.getResources().getStringArray(R.array.air_quality_units)[unitArrayIndex];
        } else {
            return UnitUtils.formatFloat(mugpcum * unitFactor, 1)
                    + context.getResources().getStringArray(R.array.air_quality_units)[unitArrayIndex];
        }
    }

    public String getDensityVoice(Context context, float mugpcum) {
        return getDensityVoice(context, mugpcum, DisplayUtils.isRtl(context));
    }

    private String getDensityVoice(Context context, float mugpcum, boolean rtl) {
        if (rtl) {
            return BidiFormatter.getInstance().unicodeWrap(
                    UnitUtils.formatFloat(mugpcum * unitFactor, 1)
            ) + context.getResources().getStringArray(R.array.air_quality_unit_voices)[unitArrayIndex];
        } else {
            return UnitUtils.formatFloat(mugpcum * unitFactor, 1)
                    + context.getResources().getStringArray(R.array.air_quality_unit_voices)[unitArrayIndex];
        }
    }
}
