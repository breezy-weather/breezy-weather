package wangdaye.com.geometricweather.basic.model.option.unit;

public enum SpeedUnit {

    KPH("kph", "km/h", 1f),
    MPS("mps", "m/s", 1f / 3.6f),
    KN("kn", "kn", 1f / 1.852f),
    MPH("mph", "mi/h", 1f / 1.609f),
    FTPS("ftps", "ft/s", 0.9113f);

    private String unitId;
    private String unitAbbreviation;
    private float unitFactor; // actual speed = speed(km/h) * factor.

    SpeedUnit(String id, String abbreviation, float factor) {
        unitId = id;
        unitAbbreviation = abbreviation;
        unitFactor = factor;
    }

    public String getUnitId() {
        return unitId;
    }

    public float getSpeed(float kph) {
        return kph * unitFactor;
    }

    public String getSpeedText(float kph) {
        return getSpeedTextWithoutUnit(kph) + unitAbbreviation;
    }

    public String getSpeedTextWithoutUnit(float kph) {
        return UnitUtils.formatFloat(kph * unitFactor, 1);
    }

    public String getAbbreviation() {
        return unitAbbreviation;
    }
}
