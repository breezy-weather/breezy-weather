package wangdaye.com.geometricweather.weather.json.accu;

import java.util.Date;

/**
 * Accu hourly result.
 * */

public class AccuHourlyResult {

    /**
     * DateTime : 2016-12-22T10:00:00+08:00
     * EpochDateTime : 1482372000
     * WeatherIcon : 6
     * IconPhrase : 多云转阴
     * IsDaylight : true
     * Temperature : {"Value":4.1,"Unit":"C","UnitType":17}
     * PrecipitationProbability : 7
     * MobileLink : http://m.accuweather.com/zh/cn/qingdao/106573/hourly-weather-forecast/106573?day=1&hbhhour=10&unit=c&lang=zh-cn
     * Link : http://www.accuweather.com/zh/cn/qingdao/106573/hourly-weather-forecast/106573?day=1&hbhhour=10&unit=c&lang=zh-cn
     */

    public Date DateTime;
    public long EpochDateTime;
    public int WeatherIcon;
    public String IconPhrase;
    public boolean IsDaylight;
    /**
     * Value : 4.1
     * Unit : C
     * UnitType : 17
     */

    public Temperature Temperature;
    public int PrecipitationProbability;
    public String MobileLink;
    public String Link;

    public static class Temperature {
        public double Value;
        public String Unit;
        public int UnitType;
    }
}
