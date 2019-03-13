package wangdaye.com.geometricweather.basic.model.weather;

/**
 * Aqi.
 * */

public class Aqi {

    public String quality;
    public int aqi;
    public int pm25;
    public int pm10;
    public int so2;
    public int no2;
    public int o3;
    public float co;

    public Aqi(String quality, int aqi, int pm25, int pm10, int so2, int no2, int o3, float co) {
        this.quality = quality;
        this.aqi = aqi;
        this.pm25 = pm25;
        this.pm10 = pm10;
        this.so2 = so2;
        this.no2 = no2;
        this.o3 = o3;
        this.co = co;
    }
}