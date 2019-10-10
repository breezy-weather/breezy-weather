package wangdaye.com.geometricweather.basic.model.option.unit;

public enum DistanceUnit {

    KM("km", "km", 1f),
    M("m", "m", 1000f),
    MI("mi", "mi", 0.6213f),
    NMI("nmi", "nmi", 0.5399f),
    FT("ft", "ft", 3280.8398f);

    private String unitId;
    private String unitAbbreviation;
    private float unitFactor; // actual distance = distance(km) * factor.

    DistanceUnit(String id, String abbreviation, float factor) {
        unitId = id;
        unitAbbreviation = abbreviation;
        unitFactor = factor;
    }

    public String getUnitId() {
        return unitId;
    }

    public float getDistance(float km) {
        return km * unitFactor;
    }

    public String getDistanceText(float km) {
        return UnitUtils.formatFloat(km * unitFactor, 2) + unitAbbreviation;
    }

    public String getAbbreviation() {
        return unitAbbreviation;
    }
}
