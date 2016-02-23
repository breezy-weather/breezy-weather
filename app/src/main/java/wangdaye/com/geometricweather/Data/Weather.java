package wangdaye.com.geometricweather.Data;

/**
 * Created by WangDaYe on 2016/2/23.
 */
public class Weather {
    // data
    public String location;
    public String weather;
    public String temp;
    public String time;

    public Weather(String location, String weather, String temp, String time) {
        this.location = location;
        this.weather = weather;
        this.temp = temp;
        this.time = time;
    }
}
