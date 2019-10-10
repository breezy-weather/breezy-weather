package wangdaye.com.geometricweather.basic.model.option.unit;

public enum PressureUnit {

    MB("mb", "mb", 1f),
    KPA("kpa", "kPa", 0.1f),
    HPA("hpa", "hPa", 1f),
    ATM("atm", "atm", 0.0009869f),
    MMHG("mmhg", "mmHg", 0.75006f),
    INHG("inhg", "inHg", 0.02953f),
    KGFPSQCM("kgfpsqcm", "kgf/cm²", 0.00102f);

    private String unitId;
    private String unitAbbreviation;
    private float unitFactor; // actual pressure = pressure(mb) * factor.

    PressureUnit(String id, String abbreviation, float factor) {
        unitId = id;
        unitAbbreviation = abbreviation;
        unitFactor = factor;
    }

    public String getUnitId() {
        return unitId;
    }

    public float getPressure(float mb) {
        return mb * unitFactor;
    }

    public String getPressureText(float mb) {
        return UnitUtils.formatFloat(mb * unitFactor) + unitAbbreviation;
    }

    public String getAbbreviation() {
        return unitAbbreviation;
    }
}
