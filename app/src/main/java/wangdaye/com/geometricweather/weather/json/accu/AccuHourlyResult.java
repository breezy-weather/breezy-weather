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
    public RealFeelTemperature RealFeelTemperature;
    public RealFeelTemperatureShade RealFeelTemperatureShade;
    public WetBulbTemperature WetBulbTemperature;

    public int PrecipitationProbability;
    public int ThunderstormProbability;
    public int RainProbability;
    public int SnowProbability;
    public int IceProbability;

    public static class Wind {
        public Speed Speed;
        public Direction Direction;

        public static class Speed {
            public double Value;
            public String Unit;
            public int UnitType;
        }

        public static class Direction {
            public int Degrees;
            public String Localized;
            public String English;
        }
    }

    public static class WindGust {
        public Speed Speed;
        public Direction Direction;

        public static class Speed {
            public double Value;
            public String Unit;
            public int UnitType;
        }

        public static class Direction {
            public int Degrees;
            public String Localized;
            public String English;
        }
    }

    public Wind Wind;
    public WindGust WindGust;

    public int UVIndex;
    public String UVIndexText;

    public TotalLiquid TotalLiquid;
    /**
     * Value : 0.0
     * Unit : mm
     * UnitType : 3
     */

    public Rain Rain;
    /**
     * Value : 0.0
     * Unit : cm
     * UnitType : 4
     */

    public Snow Snow;
    /**
     * Value : 0.0
     * Unit : mm
     * UnitType : 3
     */

    public Ice Ice;

    public String MobileLink;
    public String Link;

    public static class Temperature {
        public double Value;
        public String Unit;
        public int UnitType;
    }

    public static class RealFeelTemperature {
        public double Value;
        public String Unit;
        public int UnitType;
    }

    public static class RealFeelTemperatureShade {
        public double Value;
        public String Unit;
        public int UnitType;
    }

    public static class WetBulbTemperature {
        public double Value;
        public String Unit;
        public int UnitType;
    }

    public static class TotalLiquid {
        public double Value;
        public String Unit;
        public int UnitType;
    }

    public static class Rain {
        public double Value;
        public String Unit;
        public int UnitType;
    }

    public static class Snow {
        public double Value;
        public String Unit;
        public int UnitType;
    }

    public static class Ice {
        public double Value;
        public String Unit;
        public int UnitType;
    }
}