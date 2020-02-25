package wangdaye.com.geometricweather.basic.model.option.unit;

public enum TemperatureUnit {

    C("c", "℃", "°C", "°", c -> c),
    F("f", "℉", "°F", "°", c -> (int) (32 + c * 1.8f)),
    K("k", "K", "K", "K", c -> (int) (273.15 + c));

    private String unitId;
    private String unitAbbreviation;
    private String unitLongAbbreviation;
    private String unitShortAbbreviation;
    private Calculator unitCalculator;

    public interface Calculator {
        int getTemperature(int c);
    }

    TemperatureUnit(String id, String abbreviation, String longAbbreviation, String shortAbbreviation,
                    Calculator calculator) {
        unitId = id;
        unitAbbreviation = abbreviation;
        unitLongAbbreviation = longAbbreviation;
        unitShortAbbreviation = shortAbbreviation;
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

    public String getLongTemperatureText(int c) {
        return unitCalculator.getTemperature(c) + unitLongAbbreviation;
    }

    public String getShortTemperatureText(int c) {
        return unitCalculator.getTemperature(c) + unitShortAbbreviation;
    }

    public String getAbbreviation() {
        return unitAbbreviation;
    }

    public String getLongAbbreviation() {
        return unitLongAbbreviation;
    }

    public String getUnitShortAbbreviation() {
        return unitShortAbbreviation;
    }
}