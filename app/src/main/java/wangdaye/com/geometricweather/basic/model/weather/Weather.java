package wangdaye.com.geometricweather.basic.model.weather;

import java.util.List;

/**
 * Weather
 * */

public class Weather {

    public Base base;
    public RealTime realTime;
    public List<Daily> dailyList;
    public List<Hourly> hourlyList;
    public Aqi aqi;
    public Index index;
    public List<Alert> alertList;

    public static final String KIND_CLEAR = "CLEAR";
    public static final String KIND_PARTLY_CLOUDY = "PARTLY_CLOUDY";
    public static final String KIND_CLOUDY = "CLOUDY";
    public static final String KIND_RAIN = "RAIN";
    public static final String KIND_SNOW = "SNOW";
    public static final String KIND_WIND = "WIND";
    public static final String KIND_FOG = "FOG";
    public static final String KIND_HAZE = "HAZE";
    public static final String KIND_SLEET = "SLEET";
    public static final String KIND_HAIL = "HAIL";
    public static final String KIND_THUNDER = "THUNDER";
    public static final String KIND_THUNDERSTORM = "THUNDERSTORM";

    public Weather(Base base, RealTime realTime,
                   List<Daily> dailyList, List<Hourly> hourlyList,
                   Aqi aqi, Index index,
                   List<Alert> alertList) {
        this.base = base;
        this.realTime = realTime;
        this.dailyList = dailyList;
        this.hourlyList = hourlyList;
        this.aqi = aqi;
        this.index = index;
        this.alertList = alertList;
    }

    public boolean isValid(float hours) {
        return Math.abs(System.currentTimeMillis() - base.timeStamp) < hours * 60 * 60 * 1000;
    }
}
