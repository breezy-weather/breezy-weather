package wangdaye.com.geometricweather.basic.model.weather;

/**
 * Real time.
 * */

public class RealTime {

    public String weather;
    public String weatherKind;
    public int temp;
    public int sensibleTemp;
    public String windDir;
    public String windSpeed;
    public String windLevel;
    public int windDegree;
    public String simpleForecast;

    public RealTime(String weather, String weatherKind, int temp, int sensibleTemp,
                    String windDir, String windSpeed, String windLevel, int windDegree,
                    String simpleForecast) {
        this.weather = weather;
        this.weatherKind = weatherKind;
        this.temp = temp;
        this.sensibleTemp = sensibleTemp;
        this.windDir = windDir;
        this.windSpeed = windSpeed;
        this.windLevel = windLevel;
        this.windDegree = windDegree;
        this.simpleForecast = simpleForecast;
    }
}
