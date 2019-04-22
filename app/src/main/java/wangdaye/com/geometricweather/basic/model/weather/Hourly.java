package wangdaye.com.geometricweather.basic.model.weather;

import wangdaye.com.geometricweather.db.entity.HourlyEntity;

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

    public HourlyEntity toHourlyEntity(Base base) {
        HourlyEntity entity = new HourlyEntity();
        entity.cityId = base.cityId;
        entity.city = base.city;
        entity.time = time;
        entity.dayTime = dayTime;
        entity.weather = weather;
        entity.weatherKind = weatherKind;
        entity.temp = temp;
        entity.precipitation = precipitation;
        return entity;
    }
}
