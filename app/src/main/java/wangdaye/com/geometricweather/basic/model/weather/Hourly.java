package wangdaye.com.geometricweather.basic.model.weather;

/**
 * Hourly.
 * */

public class Hourly {

    public String time;
    public boolean dayTime;
    public String weather;
    public String weatherKind;
    public int temp;
    public int precipitation;

    public Hourly(String time, boolean dayTime,
                  String weather, String weatherKind,
                  int temp, int precipitation) {
        this.time = time;
        this.dayTime = dayTime;
        this.weather = weather;
        this.weatherKind = weatherKind;
        this.temp = temp;
        this.precipitation = precipitation;
    }
}
