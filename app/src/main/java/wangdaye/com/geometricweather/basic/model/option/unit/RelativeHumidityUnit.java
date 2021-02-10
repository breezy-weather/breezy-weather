package wangdaye.com.geometricweather.basic.model.option.unit;

public enum RelativeHumidityUnit {

    PERCENT("%");

    private final String unitAbbreviation;

    RelativeHumidityUnit(String abbreviation) {
        unitAbbreviation = abbreviation;
    }

    public String getRelativeHumidityText(float percent) {
        return UnitUtils.formatInt((int) percent) + unitAbbreviation;
    }
}
