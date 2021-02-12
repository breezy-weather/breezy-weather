package wangdaye.com.geometricweather.basic.models.options.unit;

import android.content.Context;
import android.text.BidiFormatter;

import wangdaye.com.geometricweather.R;
import wangdaye.com.geometricweather.utils.DisplayUtils;

public enum DurationUnit {

    H("h", 0, 1f);

    private final String unitId;
    private final int unitArrayIndex;
    private final float unitFactor; // actual duration = duration(h) * factor.

    DurationUnit(String id, int arrayIndex, float factor) {
        unitId = id;
        unitArrayIndex = arrayIndex;
        unitFactor = factor;
    }

    public String getUnitId() {
        return unitId;
    }

    public String getDurationText(Context context, float h) {
        return getDurationText(context, h, DisplayUtils.isRtl(context));
    }

    private String getDurationText(Context context, float h, boolean rtl) {
        if (rtl) {
            return BidiFormatter.getInstance().unicodeWrap(
                    UnitUtils.formatFloat(h * unitFactor, 1)
            ) + context.getResources().getStringArray(R.array.duration_units)[unitArrayIndex];
        } else {
            return UnitUtils.formatFloat(h * unitFactor, 1)
                    + context.getResources().getStringArray(R.array.duration_units)[unitArrayIndex];
        }
    }

    public String getDurationVoice(Context context, float h) {
        return getDurationVoice(context, h, DisplayUtils.isRtl(context));
    }

    private String getDurationVoice(Context context, float h, boolean rtl) {
        if (rtl) {
            return BidiFormatter.getInstance().unicodeWrap(
                    UnitUtils.formatFloat(h * unitFactor, 1)
            ) + context.getResources().getStringArray(R.array.duration_unit_voices)[unitArrayIndex];
        } else {
            return UnitUtils.formatFloat(h * unitFactor, 1)
                    + context.getResources().getStringArray(R.array.duration_unit_voices)[unitArrayIndex];
        }
    }
}
