package wangdaye.com.geometricweather.basic.models.options.unit;

import android.content.Context;
import android.text.BidiFormatter;

import wangdaye.com.geometricweather.utils.DisplayUtils;

public enum ProbabilityUnit {

    PERCENT("%");

    private final String unitAbbreviation;

    ProbabilityUnit(String abbreviation) {
        unitAbbreviation = abbreviation;
    }

    public String getProbabilityText(Context context, float percent) {
        if (DisplayUtils.isRtl(context)) {
            return BidiFormatter.getInstance().unicodeWrap(UnitUtils.formatInt((int) percent))
                    + unitAbbreviation;
        } else {
            return UnitUtils.formatInt((int) percent) + unitAbbreviation;
        }
    }
}
