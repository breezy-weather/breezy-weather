package wangdaye.com.geometricweather.basic.model.option.unit;

public enum ProbabilityUnit {

    PERCENT("%");

    private String unitAbbreviation;

    ProbabilityUnit(String abbreviation) {
        unitAbbreviation = abbreviation;
    }

    public String getProbabilityText(float percent) {
        return (int) percent + unitAbbreviation;
    }
}
