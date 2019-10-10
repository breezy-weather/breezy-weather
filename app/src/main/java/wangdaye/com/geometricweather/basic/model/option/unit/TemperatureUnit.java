package wangdaye.com.geometricweather.basic.model.option.unit;

public enum TemperatureUnit {

    C("c", "℃", c -> c),
    F("f", "℉", c -> (int) (32 + c * 1.8f));

    private String unitId;
    private String unitAbbreviation;
    private Calculator unitCalculator;

    public interface Calculator {
        int getTemperature(int c);
    }

    TemperatureUnit(String id, String abbreviation, Calculator calculator) {
        unitId = id;
        unitAbbreviation = abbreviation;
        unitCalculator = calculator;
    }

    public String getUnitId() {
        return unitId;
    }

    public int getTemperature(int c) {
        return unitCalculator.getTemperature(c);
    }

    public String getTemperatureText(int c) {
        return unitCalculator.getTemperature(c) + unitAbbreviation;
    }

    public String getAbbreviation() {
        return unitAbbreviation;
    }
}