package wangdaye.com.geometricweather.common.basic.models.options.unit;

import android.content.Context;
import android.text.BidiFormatter;

import androidx.annotation.Nullable;

import wangdaye.com.geometricweather.R;
import wangdaye.com.geometricweather.common.utils.DisplayUtils;

public enum PollenUnit {

    PPCM("ppcm", 0);

    private final String unitId;
    private final int unitArrayIndex;

    PollenUnit(String id, int arrayIndex) {
        unitId = id;
        unitArrayIndex = arrayIndex;
    }

    public String getUnitId() {
        return unitId;
    }

    public String getPollenText(Context context, @Nullable Integer value) {
        if (value == null) {
            return getPollenText(context, 0);
        } else {
            return getPollenText(context, (int) value);
        }
    }

    public String getPollenText(Context context, int value) {
        return getPollenText(context, value, DisplayUtils.isRtl(context));
    }

    private String getPollenText(Context context, int value, boolean rtl) {
        if (rtl) {
            return BidiFormatter.getInstance().unicodeWrap(UnitUtils.formatInt(value))
                    + context.getResources().getStringArray(R.array.pollen_units)[unitArrayIndex];
        } else {
            return UnitUtils.formatInt(value)
                    + context.getResources().getStringArray(R.array.pollen_units)[unitArrayIndex];
        }
    }

    public String getPollenVoice(Context context, @Nullable Integer value) {
        if (value == null) {
            return getPollenVoice(context, 0);
        } else {
            return getPollenVoice(context, (int) value);
        }
    }

    public String getPollenVoice(Context context, int value) {
        return getPollenVoice(context, value, DisplayUtils.isRtl(context));
    }

    private String getPollenVoice(Context context, int value, boolean rtl) {
        if (rtl) {
            return BidiFormatter.getInstance().unicodeWrap(UnitUtils.formatInt(value))
                    + context.getResources().getStringArray(R.array.pollen_unit_voices)[unitArrayIndex];
        } else {
            return UnitUtils.formatInt(value)
                    + context.getResources().getStringArray(R.array.pollen_unit_voices)[unitArrayIndex];
        }
    }
}
