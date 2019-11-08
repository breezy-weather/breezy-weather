package wangdaye.com.geometricweather.basic.model.option.unit;

public enum PollenUnit {

    PPCM("/m³");

    private String unitAbbreviation;

    PollenUnit(String abbreviation) {
        unitAbbreviation = abbreviation;
    }

    public String getPollenText(int value) {
        return value + unitAbbreviation;
    }
}
