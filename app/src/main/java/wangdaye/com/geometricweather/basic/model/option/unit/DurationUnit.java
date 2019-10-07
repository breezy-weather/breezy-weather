package wangdaye.com.geometricweather.basic.model.option.unit;

public enum DurationUnit {

    H("h", "h", 1f);

    private String unitId;
    private String unitAbbreviation;
    private float unitFactor; // actual duration = duration(h) * factor.

    DurationUnit(String id, String abbreviation, float factor) {
        unitId = id;
        unitAbbreviation = abbreviation;
        unitFactor = factor;
    }

    public String getUnitId() {
        return unitId;
    }

    public String getDurationText(float h) {
        return UnitUtils.formatFloat(h * unitFactor, 1) + unitAbbreviation;
    }
}
