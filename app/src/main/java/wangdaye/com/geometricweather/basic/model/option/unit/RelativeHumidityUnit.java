package wangdaye.com.geometricweather.basic.model.option.unit;

public enum RelativeHumidityUnit {

    PERCENT("%");

    private String unitAbbreviation;

    RelativeHumidityUnit(String abbreviation) {
        unitAbbreviation = abbreviation;
    }

    public String getRelativeHumidityText(float percent) {
        return (int) percent + unitAbbreviation;
    }
}
