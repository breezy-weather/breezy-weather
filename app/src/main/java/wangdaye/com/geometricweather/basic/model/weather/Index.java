package wangdaye.com.geometricweather.basic.model.weather;

/**
 * Index.
 * */

public class Index {

    public String simpleForecast;
    public String briefing;
    public String currentWind;
    public String dailyWind;
    public String sensibleTemp;
    public String humidity;
    public String uv;
    public String pressure;
    public String visibility;
    public String dewPoint;

    public Index(String simpleForecast, String briefing, String currentWind, String dailyWind,
                 String sensibleTemp, String humidity, String uv, String pressure,
                 String visibility, String dewPoint) {
        this.simpleForecast = simpleForecast;
        this.briefing = briefing;
        this.currentWind = currentWind;
        this.dailyWind = dailyWind;
        this.sensibleTemp = sensibleTemp;
        this.humidity = humidity;
        this.uv = uv;
        this.pressure = pressure;
        this.visibility = visibility;
        this.dewPoint = dewPoint;
    }
}