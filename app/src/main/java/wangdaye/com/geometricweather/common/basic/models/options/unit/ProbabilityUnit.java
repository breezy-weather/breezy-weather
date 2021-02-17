package wangdaye.com.geometricweather.common.basic.models.options.unit;

import android.content.Context;
import android.text.BidiFormatter;

import wangdaye.com.geometricweather.common.utils.DisplayUtils;

public enum ProbabilityUnit {

    PERCENT("%");

    private final String unitAbbreviation;

    ProbabilityUnit(String abbreviation) {
        unitAbbreviation = abbreviation;
    }

    public String getProbabilityText(Context context, float percent) {
        return getProbabilityText(context, percent, DisplayUtils.isRtl(context));
    }

    private String getProbabilityText(Context context, float percent, boolean rtl) {
        if (rtl) {
            return BidiFormatter.getInstance().unicodeWrap(UnitUtils.formatInt((int) percent))
                    + unitAbbreviation;
        } else {
            return UnitUtils.formatInt((int) percent) + unitAbbreviation;
        }
    }
}
