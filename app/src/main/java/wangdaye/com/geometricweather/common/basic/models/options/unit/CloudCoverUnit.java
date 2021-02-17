package wangdaye.com.geometricweather.common.basic.models.options.unit;

public enum CloudCoverUnit {

    PERCENT("%");

    private final String unitAbbreviation;

    CloudCoverUnit(String abbreviation) {
        unitAbbreviation = abbreviation;
    }

    public String getCloudCoverText(int percent) {
        return UnitUtils.formatInt(percent) + unitAbbreviation;
    }
}
