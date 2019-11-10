package wangdaye.com.geometricweather.basic.model.option.unit;

import androidx.annotation.Nullable;

public enum PollenUnit {

    PPCM("/m³");

    private String unitAbbreviation;

    PollenUnit(String abbreviation) {
        unitAbbreviation = abbreviation;
    }

    public String getPollenText(int value) {
        return value + unitAbbreviation;
    }

    public String getPollenText(@Nullable Integer value) {
        if (value == null) {
            return getPollenText(0);
        } else {
            return getPollenText((int) value);
        }
    }
}
