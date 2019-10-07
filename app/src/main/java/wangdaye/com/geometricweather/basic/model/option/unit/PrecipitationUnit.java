package wangdaye.com.geometricweather.basic.model.option.unit;

public enum PrecipitationUnit {

    MM("mm", "mm", 1f),
    LPSQM("lpsqm", "L/m²", 1f);

    private String unitId;
    private String unitAbbreviation;
    private float unitFactor; // actual precipitation = precipitation(mm) * factor.

    PrecipitationUnit(String id, String abbreviation, float factor) {
        unitId = id;
        unitAbbreviation = abbreviation;
        unitFactor = factor;
    }

    public String getUnitId() {
        return unitId;
    }

    public float getPrecipitation(float mm) {
        return mm * unitFactor;
    }

    public String getPrecipitationText(float mm) {
        return UnitUtils.formatFloat(mm * unitFactor, 1) + unitAbbreviation;
    }
}
