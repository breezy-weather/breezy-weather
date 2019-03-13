package wangdaye.com.geometricweather.basic.model;

/**
 * History
 * */

public class History {

    public String cityId;
    public String city;
    public String date;

    public int maxiTemp;
    public int miniTemp;

    public History(String cityId, String city, String date, int maxiTemp, int miniTemp) {
        this.cityId = cityId;
        this.city = city;
        this.date = date;
        this.maxiTemp = maxiTemp;
        this.miniTemp = miniTemp;
    }
}
