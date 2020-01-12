package wangdaye.com.geometricweather.basic.model.option.unit;

import android.content.Context;

import androidx.annotation.Nullable;

import wangdaye.com.geometricweather.R;

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
        return value + context.getResources().getStringArray(R.array.pollen_units)[unitArrayIndex];
    }

    public String getPollenText(Context context, @Nullable Integer value) {
        if (value == null) {
            return getPollenText(context, 0);
        } else {
            return getPollenText(context, (int) value);
        }
    }
}
