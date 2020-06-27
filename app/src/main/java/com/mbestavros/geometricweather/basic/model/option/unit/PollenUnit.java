package com.mbestavros.geometricweather.basic.model.option.unit;

import android.content.Context;
import android.text.BidiFormatter;

import androidx.annotation.Nullable;

import com.mbestavros.geometricweather.R;
import com.mbestavros.geometricweather.utils.DisplayUtils;

public enum PollenUnit {

    PPCM("ppcm", 0);

    private String unitId;
    private int unitArrayIndex;

    PollenUnit(String id, int arrayIndex) {
        unitId = id;
        unitArrayIndex = arrayIndex;
    }

    public String getUnitId() {
        return unitId;
    }

    public String getPollenText(Context context, int value) {
        if (DisplayUtils.isRtl(context)) {
            return BidiFormatter.getInstance().unicodeWrap(UnitUtils.formatInt(value))
                    + context.getResources().getStringArray(R.array.pollen_units)[unitArrayIndex];
        } else {
            return UnitUtils.formatInt(value)
                    + context.getResources().getStringArray(R.array.pollen_units)[unitArrayIndex];
        }
    }

    public String getPollenText(Context context, @Nullable Integer value) {
        if (value == null) {
            return getPollenText(context, 0);
        } else {
            return getPollenText(context, (int) value);
        }
    }
}
