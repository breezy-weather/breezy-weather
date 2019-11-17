package wangdaye.com.geometricweather.weather.json.accu;

import java.util.Date;
import java.util.List;

/**
 * Accu daily result.
 * */

public class AccuDailyResult {

    /**
     * EffectiveDate : 2016-12-22T07:00:00+08:00
     * EffectiveEpochDate : 1482361200
     * Severity : 7
     * Text : 从星期四上午至星期五下午有微风
     * Category : wind
     * EndDate : 2016-12-23T19:00:00+08:00
     * EndEpochDate : 1482490800
     * MobileLink : http://m.accuweather.com/zh/cn/qingdao/106573/extended-weather-forecast/106573?unit=c&lang=zh-cn
     * Link : http://www.accuweather.com/zh/cn/qingdao/106573/daily-weather-forecast/106573?unit=c&lang=zh-cn
     */

    public Headline Headline;
    /**
     * Date : 2016-12-22T07:00:00+08:00
     * EpochDate : 1482361200
     * Sun : {"Rise":"2016-12-22T07:06:00+08:00","EpochRise":1482361560,"Set":"2016-12-22T16:49:00+08:00","EpochSet":1482396540}
     * Moon : {"Rise":"2016-12-22T00:27:00+08:00","EpochRise":1482337620,"Set":"2016-12-22T12:39:00+08:00","EpochSet":1482381540,"Phase":"WaningCrescent","Age":23}
     * Temperature : {"Minimum":{"Value":-1,"Unit":"C","UnitType":17},"Maximum":{"Value":5,"Unit":"C","UnitType":17}}
     * RealFeelTemperature : {"Minimum":{"Value":-10.2,"Unit":"C","UnitType":17},"Maximum":{"Value":-2,"Unit":"C","UnitType":17}}
     * RealFeelTemperatureShade : {"Minimum":{"Value":-10.2,"Unit":"C","UnitType":17},"Maximum":{"Value":-2,"Unit":"C","UnitType":17}}
     * HoursOfSun : 4.8
     * DegreeDaySummary : {"Heating":{"Value":16,"Unit":"C","UnitType":17},"Cooling":{"Value":0,"Unit":"C","UnitType":17}}
     * AirAndPollen : [{"Name":"AirQuality","Value":0,"Category":"较适宜","CategoryValue":1,"Type":"臭氧"},{"Name":"Grass","Value":0,"Category":"低","CategoryValue":1},{"Name":"Mold","Value":0,"Category":"低","CategoryValue":1},{"Name":"Ragweed","Value":0,"Category":"低","CategoryValue":1},{"Name":"Tree","Value":0,"Category":"低","CategoryValue":1},{"Name":"UVIndex","Value":2,"Category":"低","CategoryValue":1}]
     * Day : {"Icon":7,"IconPhrase":"多云","LocalSource":{"Id":7,"Name":"Huafeng","WeatherCode":"02"},"ShortPhrase":"多云","LongPhrase":"多云","PrecipitationProbability":25,"ThunderstormProbability":0,"RainProbability":18,"SnowProbability":4,"IceProbability":0,"DailyWind":{"Speed":{"Value":31.5,"Unit":"km/h","UnitType":7},"Direction":{"Degrees":337,"Localized":"西北偏北","English":"NNW"}},"WindGust":{"Speed":{"Value":40.7,"Unit":"km/h","UnitType":7},"Direction":{"Degrees":339,"Localized":"西北偏北","English":"NNW"}},"TotalLiquid":{"Value":0,"Unit":"mm","UnitType":3},"Rain":{"Value":0,"Unit":"mm","UnitType":3},"Snow":{"Value":0,"Unit":"cm","UnitType":4},"Ice":{"Value":0,"Unit":"mm","UnitType":3},"HoursOfPrecipitation":0,"HoursOfRain":0,"HoursOfSnow":0,"HoursOfIce":0,"CloudCover":66}
     * Night : {"Icon":35,"IconPhrase":"多云转阴","LocalSource":{"Id":7,"Name":"Huafeng","WeatherCode":"01"},"ShortPhrase":"多云转阴","LongPhrase":"多云转阴","PrecipitationProbability":1,"ThunderstormProbability":0,"RainProbability":0,"SnowProbability":0,"IceProbability":0,"DailyWind":{"Speed":{"Value":31.5,"Unit":"km/h","UnitType":7},"Direction":{"Degrees":337,"Localized":"西北偏北","English":"NNW"}},"WindGust":{"Speed":{"Value":50,"Unit":"km/h","UnitType":7},"Direction":{"Degrees":347,"Localized":"西北偏北","English":"NNW"}},"TotalLiquid":{"Value":0,"Unit":"mm","UnitType":3},"Rain":{"Value":0,"Unit":"mm","UnitType":3},"Snow":{"Value":0,"Unit":"cm","UnitType":4},"Ice":{"Value":0,"Unit":"mm","UnitType":3},"HoursOfPrecipitation":0,"HoursOfRain":0,"HoursOfSnow":0,"HoursOfIce":0,"CloudCover":11}
     * Sources : ["AccuWeatherService","Huafeng"]
     * MobileLink : http://m.accuweather.com/zh/cn/qingdao/106573/daily-weather-forecast/106573?day=1&unit=c&lang=zh-cn
     * Link : http://www.accuweather.com/zh/cn/qingdao/106573/daily-weather-forecast/106573?day=1&unit=c&lang=zh-cn
     */

    public List<DailyForecasts> DailyForecasts;

    public static class Headline {
        public Date EffectiveDate;
        public long EffectiveEpochDate;
        public int Severity;
        public String Text;
        public String Category;
        public Date EndDate;
        public long EndEpochDate;
        public String MobileLink;
        public String Link;
    }

    public static class DailyForecasts {
        public Date Date;
        public long EpochDate;
        /**
         * Rise : 2016-12-22T07:06:00+08:00
         * EpochRise : 1482361560
         * Set : 2016-12-22T16:49:00+08:00
         * EpochSet : 1482396540
         */

        public Sun Sun;
        /**
         * Rise : 2016-12-22T00:27:00+08:00
         * EpochRise : 1482337620
         * Set : 2016-12-22T12:39:00+08:00
         * EpochSet : 1482381540
         * Phase : WaningCrescent
         * Age : 23
         */

        public Moon Moon;
        /**
         * Minimum : {"Value":-1,"Unit":"C","UnitType":17}
         * Maximum : {"Value":5,"Unit":"C","UnitType":17}
         */

        public Temperature Temperature;
        /**
         * Minimum : {"Value":-10.2,"Unit":"C","UnitType":17}
         * Maximum : {"Value":-2,"Unit":"C","UnitType":17}
         */

        public RealFeelTemperature RealFeelTemperature;
        /**
         * Minimum : {"Value":-10.2,"Unit":"C","UnitType":17}
         * Maximum : {"Value":-2,"Unit":"C","UnitType":17}
         */

        public RealFeelTemperatureShade RealFeelTemperatureShade;
        public double HoursOfSun;
        /**
         * Heating : {"Value":16,"Unit":"C","UnitType":17}
         * Cooling : {"Value":0,"Unit":"C","UnitType":17}
         */

        public DegreeDaySummary DegreeDaySummary;
        /**
         * Icon : 7
         * IconPhrase : 多云
         * LocalSource : {"Id":7,"Name":"Huafeng","WeatherCode":"02"}
         * ShortPhrase : 多云
         * LongPhrase : 多云
         * PrecipitationProbability : 25
         * ThunderstormProbability : 0
         * RainProbability : 18
         * SnowProbability : 4
         * IceProbability : 0
         * DailyWind : {"Speed":{"Value":31.5,"Unit":"km/h","UnitType":7},"Direction":{"Degrees":337,"Localized":"西北偏北","English":"NNW"}}
         * WindGust : {"Speed":{"Value":40.7,"Unit":"km/h","UnitType":7},"Direction":{"Degrees":339,"Localized":"西北偏北","English":"NNW"}}
         * TotalLiquid : {"Value":0,"Unit":"mm","UnitType":3}
         * Rain : {"Value":0,"Unit":"mm","UnitType":3}
         * Snow : {"Value":0,"Unit":"cm","UnitType":4}
         * Ice : {"Value":0,"Unit":"mm","UnitType":3}
         * HoursOfPrecipitation : 0.0
         * HoursOfRain : 0.0
         * HoursOfSnow : 0.0
         * HoursOfIce : 0.0
         * CloudCover : 66
         */

        public Day Day;
        /**
         * Icon : 35
         * IconPhrase : 多云转阴
         * LocalSource : {"Id":7,"Name":"Huafeng","WeatherCode":"01"}
         * ShortPhrase : 多云转阴
         * LongPhrase : 多云转阴
         * PrecipitationProbability : 1
         * ThunderstormProbability : 0
         * RainProbability : 0
         * SnowProbability : 0
         * IceProbability : 0
         * DailyWind : {"Speed":{"Value":31.5,"Unit":"km/h","UnitType":7},"Direction":{"Degrees":337,"Localized":"西北偏北","English":"NNW"}}
         * WindGust : {"Speed":{"Value":50,"Unit":"km/h","UnitType":7},"Direction":{"Degrees":347,"Localized":"西北偏北","English":"NNW"}}
         * TotalLiquid : {"Value":0,"Unit":"mm","UnitType":3}
         * Rain : {"Value":0,"Unit":"mm","UnitType":3}
         * Snow : {"Value":0,"Unit":"cm","UnitType":4}
         * Ice : {"Value":0,"Unit":"mm","UnitType":3}
         * HoursOfPrecipitation : 0.0
         * HoursOfRain : 0.0
         * HoursOfSnow : 0.0
         * HoursOfIce : 0.0
         * CloudCover : 11
         */

        public Night Night;
        public String MobileLink;
        public String Link;
        /**
         * Name : AirQuality
         * Value : 0
         * Category : 较适宜
         * CategoryValue : 1
         * Type : 臭氧
         */

        public List<AirAndPollen> AirAndPollen;
        public List<String> Sources;

        public static class Sun {
            public Date Rise;
            public long EpochRise;
            public Date Set;
            public long EpochSet;
        }

        public static class Moon {
            public Date Rise;
            public long EpochRise;
            public Date Set;
            public long EpochSet;
            public String Phase;
            public int Age;
        }

        public static class Temperature {
            /**
             * Value : -1.0
             * Unit : C
             * UnitType : 17
             */

            public Minimum Minimum;
            /**
             * Value : 5.0
             * Unit : C
             * UnitType : 17
             */

            public Maximum Maximum;

            public static class Minimum {
                public double Value;
                public String Unit;
                public int UnitType;
            }

            public static class Maximum {
                public double Value;
                public String Unit;
                public int UnitType;
            }
        }

        public static class RealFeelTemperature {
            /**
             * Value : -10.2
             * Unit : C
             * UnitType : 17
             */

            public Minimum Minimum;
            /**
             * Value : -2.0
             * Unit : C
             * UnitType : 17
             */

            public Maximum Maximum;

            public static class Minimum {
                public double Value;
                public String Unit;
                public int UnitType;
            }

            public static class Maximum {
                public double Value;
                public String Unit;
                public int UnitType;
            }
        }

        public static class RealFeelTemperatureShade {
            /**
             * Value : -10.2
             * Unit : C
             * UnitType : 17
             */

            public Minimum Minimum;
            /**
             * Value : -2.0
             * Unit : C
             * UnitType : 17
             */

            public Maximum Maximum;

            public static class Minimum {
                public double Value;
                public String Unit;
                public int UnitType;
            }

            public static class Maximum {
                public double Value;
                public String Unit;
                public int UnitType;
            }
        }

        public static class DegreeDaySummary {
            /**
             * Value : 16.0
             * Unit : C
             * UnitType : 17
             */

            public Heating Heating;
            /**
             * Value : 0.0
             * Unit : C
             * UnitType : 17
             */

            public Cooling Cooling;

            public static class Heating {
                public double Value;
                public String Unit;
                public int UnitType;
            }

            public static class Cooling {
                public double Value;
                public String Unit;
                public int UnitType;
            }
        }

        public static class Day {
            public int Icon;
            public String IconPhrase;
            /**
             * Id : 7
             * Name : Huafeng
             * WeatherCode : 02
             */

            public LocalSource LocalSource;
            public String ShortPhrase;
            public String LongPhrase;
            public int PrecipitationProbability;
            public int ThunderstormProbability;
            public int RainProbability;
            public int SnowProbability;
            public int IceProbability;
            /**
             * Speed : {"Value":31.5,"Unit":"km/h","UnitType":7}
             * Direction : {"Degrees":337,"Localized":"西北偏北","English":"NNW"}
             */

            public Wind Wind;
            /**
             * Speed : {"Value":40.7,"Unit":"km/h","UnitType":7}
             * Direction : {"Degrees":339,"Localized":"西北偏北","English":"NNW"}
             */

            public WindGust WindGust;
            /**
             * Value : 0.0
             * Unit : mm
             * UnitType : 3
             */

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
            public double HoursOfPrecipitation;
            public double HoursOfRain;
            public double HoursOfSnow;
            public double HoursOfIce;
            public int CloudCover;

            public static class LocalSource {
                public int Id;
                public String Name;
                public String WeatherCode;
            }

            public static class Wind {
                /**
                 * Value : 31.5
                 * Unit : km/h
                 * UnitType : 7
                 */

                public Speed Speed;
                /**
                 * Degrees : 337
                 * Localized : 西北偏北
                 * English : NNW
                 */

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
                /**
                 * Value : 40.7
                 * Unit : km/h
                 * UnitType : 7
                 */

                public Speed Speed;
                /**
                 * Degrees : 339
                 * Localized : 西北偏北
                 * English : NNW
                 */

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

        public static class Night {
            public int Icon;
            public String IconPhrase;
            /**
             * Id : 7
             * Name : Huafeng
             * WeatherCode : 01
             */

            public LocalSource LocalSource;
            public String ShortPhrase;
            public String LongPhrase;
            public int PrecipitationProbability;
            public int ThunderstormProbability;
            public int RainProbability;
            public int SnowProbability;
            public int IceProbability;
            /**
             * Speed : {"Value":31.5,"Unit":"km/h","UnitType":7}
             * Direction : {"Degrees":337,"Localized":"西北偏北","English":"NNW"}
             */

            public Wind Wind;
            /**
             * Speed : {"Value":50,"Unit":"km/h","UnitType":7}
             * Direction : {"Degrees":347,"Localized":"西北偏北","English":"NNW"}
             */

            public WindGust WindGust;
            /**
             * Value : 0.0
             * Unit : mm
             * UnitType : 3
             */

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
            public double HoursOfPrecipitation;
            public double HoursOfRain;
            public double HoursOfSnow;
            public double HoursOfIce;
            public int CloudCover;

            public static class LocalSource {
                public int Id;
                public String Name;
                public String WeatherCode;
            }

            public static class Wind {
                /**
                 * Value : 31.5
                 * Unit : km/h
                 * UnitType : 7
                 */

                public Speed Speed;
                /**
                 * Degrees : 337
                 * Localized : 西北偏北
                 * English : NNW
                 */

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
                /**
                 * Value : 50.0
                 * Unit : km/h
                 * UnitType : 7
                 */

                public Speed Speed;
                /**
                 * Degrees : 347
                 * Localized : 西北偏北
                 * English : NNW
                 */

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

        public static class AirAndPollen {
            public String Name;
            public int Value;
            public String Category;
            public int CategoryValue;
            public String Type;
        }
    }
}
