package wangdaye.com.geometricweather.basic.model.weather;

/**
 * Base.
 * */

public class Base {

    public String cityId;
    public String city;
    public String date;
    public String time;
    public long timeStamp;

    public Base(String cityId, String city, String date, String time, long timeStamp) {
        this.cityId = cityId;
        this.city = city;
        this.date = date;
        this.time = time;
        this.timeStamp = timeStamp;
    }


}
