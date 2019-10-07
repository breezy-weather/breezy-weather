package wangdaye.com.geometricweather.basic.model.option.unit;

public enum AirQualityUnit {

    MUGPCUM("mugpcum", "μg/m³", 1f);

    private String unitId;
    private String unitAbbreviation;
    private float unitFactor; // actual air quality = quality(μg/m³) * factor.

    AirQualityUnit(String id, String abbreviation, float factor) {
        unitId = id;
        unitAbbreviation = abbreviation;
        unitFactor = factor;
    }

    public String getUnitId() {
        return unitId;
    }

    public float getDensity(float mugpcum) {
        return mugpcum * unitFactor;
    }

    public String getDensityText(float mugpcum) {
        return UnitUtils.formatFloat(mugpcum * unitFactor, 1) + unitAbbreviation;
    }
}
