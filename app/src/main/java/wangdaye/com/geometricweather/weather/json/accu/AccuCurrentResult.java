package wangdaye.com.geometricweather.weather.json.accu;

import java.util.Date;

/**
 * Accu realtime result.
 * */

public class AccuCurrentResult {

    /**
     * LocalObservationDateTime : 2016-12-22T11:48:00+08:00
     * EpochTime : 1482378480
     * WeatherText : 雾
     * WeatherIcon : 11
     * LocalSource : {"Id":7,"Name":"Huafeng","WeatherCode":"18"}
     * IsDayTime : true
     * Temperature : {"Metric":{"Value":3.9,"Unit":"C","UnitType":17},"Imperial":{"Value":39,"Unit":"F","UnitType":18}}
     * RealFeelTemperature : {"Metric":{"Value":1.4,"Unit":"C","UnitType":17},"Imperial":{"Value":35,"Unit":"F","UnitType":18}}
     * RealFeelTemperatureShade : {"Metric":{"Value":-0.6,"Unit":"C","UnitType":17},"Imperial":{"Value":31,"Unit":"F","UnitType":18}}
     * RelativeHumidity : 75
     * DewPoint : {"Metric":{"Value":-0.2,"Unit":"C","UnitType":17},"Imperial":{"Value":32,"Unit":"F","UnitType":18}}
     * DailyWind : {"Direction":{"Degrees":315,"Localized":"西北","English":"NW"},"Speed":{"Metric":{"Value":16.7,"Unit":"km/h","UnitType":7},"Imperial":{"Value":10.4,"Unit":"mi/h","UnitType":9}}}
     * WindGust : {"Speed":{"Metric":{"Value":16.7,"Unit":"km/h","UnitType":7},"Imperial":{"Value":10.4,"Unit":"mi/h","UnitType":9}}}
     * UVIndex : 1
     * UVIndexText : 低
     * Visibility : {"Metric":{"Value":4.8,"Unit":"km","UnitType":6},"Imperial":{"Value":3,"Unit":"mi","UnitType":2}}
     * ObstructionsToVisibility : F
     * CloudCover : 20
     * Ceiling : {"Metric":{"Value":579,"Unit":"m","UnitType":5},"Imperial":{"Value":1900,"Unit":"ft","UnitType":0}}
     * Pressure : {"Metric":{"Value":1022,"Unit":"mb","UnitType":14},"Imperial":{"Value":30.18,"Unit":"inHg","UnitType":12}}
     * PressureTendency : {"LocalizedText":"稳定","Code":"S"}
     * Past24HourTemperatureDeparture : {"Metric":{"Value":-1.1,"Unit":"C","UnitType":17},"Imperial":{"Value":-2,"Unit":"F","UnitType":18}}
     * ApparentTemperature : {"Metric":{"Value":3.9,"Unit":"C","UnitType":17},"Imperial":{"Value":39,"Unit":"F","UnitType":18}}
     * WindChillTemperature : {"Metric":{"Value":0,"Unit":"C","UnitType":17},"Imperial":{"Value":32,"Unit":"F","UnitType":18}}
     * WetBulbTemperature : {"Metric":{"Value":2.4,"Unit":"C","UnitType":17},"Imperial":{"Value":36,"Unit":"F","UnitType":18}}
     * Precip1hr : {"Metric":{"Value":0,"Unit":"mm","UnitType":3},"Imperial":{"Value":0,"Unit":"in","UnitType":1}}
     * PrecipitationSummary : {"Precipitation":{"Metric":{"Value":0,"Unit":"mm","UnitType":3},"Imperial":{"Value":0,"Unit":"in","UnitType":1}},"PastHour":{"Metric":{"Value":0,"Unit":"mm","UnitType":3},"Imperial":{"Value":0,"Unit":"in","UnitType":1}},"Past3Hours":{"Metric":{"Value":0,"Unit":"mm","UnitType":3},"Imperial":{"Value":0,"Unit":"in","UnitType":1}},"Past6Hours":{"Metric":{"Value":0,"Unit":"mm","UnitType":3},"Imperial":{"Value":0,"Unit":"in","UnitType":1}},"Past9Hours":{"Metric":{"Value":0,"Unit":"mm","UnitType":3},"Imperial":{"Value":0,"Unit":"in","UnitType":1}},"Past12Hours":{"Metric":{"Value":0,"Unit":"mm","UnitType":3},"Imperial":{"Value":0,"Unit":"in","UnitType":1}},"Past18Hours":{"Metric":{"Value":0,"Unit":"mm","UnitType":3},"Imperial":{"Value":0,"Unit":"in","UnitType":1}},"Past24Hours":{"Metric":{"Value":1,"Unit":"mm","UnitType":3},"Imperial":{"Value":0.05,"Unit":"in","UnitType":1}}}
     * TemperatureSummary : {"Past6HourRange":{"Minimum":{"Metric":{"Value":1,"Unit":"C","UnitType":17},"Imperial":{"Value":34,"Unit":"F","UnitType":18}},"Maximum":{"Metric":{"Value":4.1,"Unit":"C","UnitType":17},"Imperial":{"Value":39,"Unit":"F","UnitType":18}}},"Past12HourRange":{"Minimum":{"Metric":{"Value":1,"Unit":"C","UnitType":17},"Imperial":{"Value":34,"Unit":"F","UnitType":18}},"Maximum":{"Metric":{"Value":4.9,"Unit":"C","UnitType":17},"Imperial":{"Value":41,"Unit":"F","UnitType":18}}},"Past24HourRange":{"Minimum":{"Metric":{"Value":1,"Unit":"C","UnitType":17},"Imperial":{"Value":34,"Unit":"F","UnitType":18}},"Maximum":{"Metric":{"Value":5.4,"Unit":"C","UnitType":17},"Imperial":{"Value":42,"Unit":"F","UnitType":18}}}}
     * MobileLink : http://m.accuweather.com/zh/cn/qingdao/1-106573_13_al/current-weather/1-106573_13_al?lang=zh-cn
     * Link : http://www.accuweather.com/zh/cn/qingdao/1-106573_13_al/current-weather/1-106573_13_al?lang=zh-cn
     */

    public Date LocalObservationDateTime;
    public long EpochTime;
    public String WeatherText;
    public int WeatherIcon;
    /**
     * Id : 7
     * Name : Huafeng
     * WeatherCode : 18
     */

    public LocalSource LocalSource;
    public boolean IsDayTime;
    /**
     * Metric : {"Value":3.9,"Unit":"C","UnitType":17}
     * Imperial : {"Value":39,"Unit":"F","UnitType":18}
     */

    public Temperature Temperature;
    /**
     * Metric : {"Value":1.4,"Unit":"C","UnitType":17}
     * Imperial : {"Value":35,"Unit":"F","UnitType":18}
     */

    public RealFeelTemperature RealFeelTemperature;
    /**
     * Metric : {"Value":-0.6,"Unit":"C","UnitType":17}
     * Imperial : {"Value":31,"Unit":"F","UnitType":18}
     */

    public RealFeelTemperatureShade RealFeelTemperatureShade;
    public int RelativeHumidity;
    /**
     * Metric : {"Value":-0.2,"Unit":"C","UnitType":17}
     * Imperial : {"Value":32,"Unit":"F","UnitType":18}
     */

    public DewPoint DewPoint;
    /**
     * Direction : {"Degrees":315,"Localized":"西北","English":"NW"}
     * Speed : {"Metric":{"Value":16.7,"Unit":"km/h","UnitType":7},"Imperial":{"Value":10.4,"Unit":"mi/h","UnitType":9}}
     */

    public Wind Wind;
    /**
     * Speed : {"Metric":{"Value":16.7,"Unit":"km/h","UnitType":7},"Imperial":{"Value":10.4,"Unit":"mi/h","UnitType":9}}
     */

    public WindGust WindGust;
    public int UVIndex;
    public String UVIndexText;
    /**
     * Metric : {"Value":4.8,"Unit":"km","UnitType":6}
     * Imperial : {"Value":3,"Unit":"mi","UnitType":2}
     */

    public Visibility Visibility;
    public String ObstructionsToVisibility;
    public int CloudCover;
    /**
     * Metric : {"Value":579,"Unit":"m","UnitType":5}
     * Imperial : {"Value":1900,"Unit":"ft","UnitType":0}
     */

    public Ceiling Ceiling;
    /**
     * Metric : {"Value":1022,"Unit":"mb","UnitType":14}
     * Imperial : {"Value":30.18,"Unit":"inHg","UnitType":12}
     */

    public Pressure Pressure;
    /**
     * LocalizedText : 稳定
     * Code : S
     */

    public PressureTendency PressureTendency;
    /**
     * Metric : {"Value":-1.1,"Unit":"C","UnitType":17}
     * Imperial : {"Value":-2,"Unit":"F","UnitType":18}
     */

    public Past24HourTemperatureDeparture Past24HourTemperatureDeparture;
    /**
     * Metric : {"Value":3.9,"Unit":"C","UnitType":17}
     * Imperial : {"Value":39,"Unit":"F","UnitType":18}
     */

    public ApparentTemperature ApparentTemperature;
    /**
     * Metric : {"Value":0,"Unit":"C","UnitType":17}
     * Imperial : {"Value":32,"Unit":"F","UnitType":18}
     */

    public WindChillTemperature WindChillTemperature;
    /**
     * Metric : {"Value":2.4,"Unit":"C","UnitType":17}
     * Imperial : {"Value":36,"Unit":"F","UnitType":18}
     */

    public WetBulbTemperature WetBulbTemperature;
    /**
     * Metric : {"Value":0,"Unit":"mm","UnitType":3}
     * Imperial : {"Value":0,"Unit":"in","UnitType":1}
     */

    public Precip1hr Precip1hr;
    /**
     * Precipitation : {"Metric":{"Value":0,"Unit":"mm","UnitType":3},"Imperial":{"Value":0,"Unit":"in","UnitType":1}}
     * PastHour : {"Metric":{"Value":0,"Unit":"mm","UnitType":3},"Imperial":{"Value":0,"Unit":"in","UnitType":1}}
     * Past3Hours : {"Metric":{"Value":0,"Unit":"mm","UnitType":3},"Imperial":{"Value":0,"Unit":"in","UnitType":1}}
     * Past6Hours : {"Metric":{"Value":0,"Unit":"mm","UnitType":3},"Imperial":{"Value":0,"Unit":"in","UnitType":1}}
     * Past9Hours : {"Metric":{"Value":0,"Unit":"mm","UnitType":3},"Imperial":{"Value":0,"Unit":"in","UnitType":1}}
     * Past12Hours : {"Metric":{"Value":0,"Unit":"mm","UnitType":3},"Imperial":{"Value":0,"Unit":"in","UnitType":1}}
     * Past18Hours : {"Metric":{"Value":0,"Unit":"mm","UnitType":3},"Imperial":{"Value":0,"Unit":"in","UnitType":1}}
     * Past24Hours : {"Metric":{"Value":1,"Unit":"mm","UnitType":3},"Imperial":{"Value":0.05,"Unit":"in","UnitType":1}}
     */

    public PrecipitationSummary PrecipitationSummary;
    /**
     * Past6HourRange : {"Minimum":{"Metric":{"Value":1,"Unit":"C","UnitType":17},"Imperial":{"Value":34,"Unit":"F","UnitType":18}},"Maximum":{"Metric":{"Value":4.1,"Unit":"C","UnitType":17},"Imperial":{"Value":39,"Unit":"F","UnitType":18}}}
     * Past12HourRange : {"Minimum":{"Metric":{"Value":1,"Unit":"C","UnitType":17},"Imperial":{"Value":34,"Unit":"F","UnitType":18}},"Maximum":{"Metric":{"Value":4.9,"Unit":"C","UnitType":17},"Imperial":{"Value":41,"Unit":"F","UnitType":18}}}
     * Past24HourRange : {"Minimum":{"Metric":{"Value":1,"Unit":"C","UnitType":17},"Imperial":{"Value":34,"Unit":"F","UnitType":18}},"Maximum":{"Metric":{"Value":5.4,"Unit":"C","UnitType":17},"Imperial":{"Value":42,"Unit":"F","UnitType":18}}}
     */

    public TemperatureSummary TemperatureSummary;
    public String MobileLink;
    public String Link;

    public static class LocalSource {
        public int Id;
        public String Name;
        public String WeatherCode;
    }

    public static class Temperature {
        /**
         * Value : 3.9
         * Unit : C
         * UnitType : 17
         */

        public Metric Metric;
        /**
         * Value : 39.0
         * Unit : F
         * UnitType : 18
         */

        public Imperial Imperial;

        public static class Metric {
            public double Value;
            public String Unit;
            public int UnitType;
        }

        public static class Imperial {
            public double Value;
            public String Unit;
            public int UnitType;
        }
    }

    public static class RealFeelTemperature {
        /**
         * Value : 1.4
         * Unit : C
         * UnitType : 17
         */

        public Metric Metric;
        /**
         * Value : 35.0
         * Unit : F
         * UnitType : 18
         */

        public Imperial Imperial;

        public static class Metric {
            public double Value;
            public String Unit;
            public int UnitType;
        }

        public static class Imperial {
            public double Value;
            public String Unit;
            public int UnitType;
        }
    }

    public static class RealFeelTemperatureShade {
        /**
         * Value : -0.6
         * Unit : C
         * UnitType : 17
         */

        public Metric Metric;
        /**
         * Value : 31.0
         * Unit : F
         * UnitType : 18
         */

        public Imperial Imperial;

        public static class Metric {
            public double Value;
            public String Unit;
            public int UnitType;
        }

        public static class Imperial {
            public double Value;
            public String Unit;
            public int UnitType;
        }
    }

    public static class DewPoint {
        /**
         * Value : -0.2
         * Unit : C
         * UnitType : 17
         */

        public Metric Metric;
        /**
         * Value : 32.0
         * Unit : F
         * UnitType : 18
         */

        public Imperial Imperial;

        public static class Metric {
            public double Value;
            public String Unit;
            public int UnitType;
        }

        public static class Imperial {
            public double Value;
            public String Unit;
            public int UnitType;
        }
    }

    public static class Wind {
        /**
         * Degrees : 315
         * Localized : 西北
         * English : NW
         */

        public Direction Direction;
        /**
         * Metric : {"Value":16.7,"Unit":"km/h","UnitType":7}
         * Imperial : {"Value":10.4,"Unit":"mi/h","UnitType":9}
         */

        public Speed Speed;

        public static class Direction {
            public int Degrees;
            public String Localized;
            public String English;
        }

        public static class Speed {
            /**
             * Value : 16.7
             * Unit : km/h
             * UnitType : 7
             */

            public Metric Metric;
            /**
             * Value : 10.4
             * Unit : mi/h
             * UnitType : 9
             */

            public Imperial Imperial;

            public static class Metric {
                public double Value;
                public String Unit;
                public int UnitType;
            }

            public static class Imperial {
                public double Value;
                public String Unit;
                public int UnitType;
            }
        }
    }

    public static class WindGust {
        /**
         * Metric : {"Value":16.7,"Unit":"km/h","UnitType":7}
         * Imperial : {"Value":10.4,"Unit":"mi/h","UnitType":9}
         */

        public Speed Speed;

        public static class Speed {
            /**
             * Value : 16.7
             * Unit : km/h
             * UnitType : 7
             */

            public Metric Metric;
            /**
             * Value : 10.4
             * Unit : mi/h
             * UnitType : 9
             */

            public Imperial Imperial;

            public static class Metric {
                public double Value;
                public String Unit;
                public int UnitType;
            }

            public static class Imperial {
                public double Value;
                public String Unit;
                public int UnitType;
            }
        }
    }

    public static class Visibility {
        /**
         * Value : 4.8
         * Unit : km
         * UnitType : 6
         */

        public Metric Metric;
        /**
         * Value : 3.0
         * Unit : mi
         * UnitType : 2
         */

        public Imperial Imperial;

        public static class Metric {
            public double Value;
            public String Unit;
            public int UnitType;
        }

        public static class Imperial {
            public double Value;
            public String Unit;
            public int UnitType;
        }
    }

    public static class Ceiling {
        /**
         * Value : 579.0
         * Unit : m
         * UnitType : 5
         */

        public Metric Metric;
        /**
         * Value : 1900.0
         * Unit : ft
         * UnitType : 0
         */

        public Imperial Imperial;

        public static class Metric {
            public double Value;
            public String Unit;
            public int UnitType;
        }

        public static class Imperial {
            public double Value;
            public String Unit;
            public int UnitType;
        }
    }

    public static class Pressure {
        /**
         * Value : 1022.0
         * Unit : mb
         * UnitType : 14
         */

        public Metric Metric;
        /**
         * Value : 30.18
         * Unit : inHg
         * UnitType : 12
         */

        public Imperial Imperial;

        public static class Metric {
            public double Value;
            public String Unit;
            public int UnitType;
        }

        public static class Imperial {
            public double Value;
            public String Unit;
            public int UnitType;
        }
    }

    public static class PressureTendency {
        public String LocalizedText;
        public String Code;
    }

    public static class Past24HourTemperatureDeparture {
        /**
         * Value : -1.1
         * Unit : C
         * UnitType : 17
         */

        public Metric Metric;
        /**
         * Value : -2.0
         * Unit : F
         * UnitType : 18
         */

        public Imperial Imperial;

        public static class Metric {
            public double Value;
            public String Unit;
            public int UnitType;
        }

        public static class Imperial {
            public double Value;
            public String Unit;
            public int UnitType;
        }
    }

    public static class ApparentTemperature {
        /**
         * Value : 3.9
         * Unit : C
         * UnitType : 17
         */

        public Metric Metric;
        /**
         * Value : 39.0
         * Unit : F
         * UnitType : 18
         */

        public Imperial Imperial;

        public static class Metric {
            public double Value;
            public String Unit;
            public int UnitType;
        }

        public static class Imperial {
            public double Value;
            public String Unit;
            public int UnitType;
        }
    }

    public static class WindChillTemperature {
        /**
         * Value : 0.0
         * Unit : C
         * UnitType : 17
         */

        public Metric Metric;
        /**
         * Value : 32.0
         * Unit : F
         * UnitType : 18
         */

        public Imperial Imperial;

        public static class Metric {
            public double Value;
            public String Unit;
            public int UnitType;
        }

        public static class Imperial {
            public double Value;
            public String Unit;
            public int UnitType;
        }
    }

    public static class WetBulbTemperature {
        /**
         * Value : 2.4
         * Unit : C
         * UnitType : 17
         */

        public Metric Metric;
        /**
         * Value : 36.0
         * Unit : F
         * UnitType : 18
         */

        public Imperial Imperial;

        public static class Metric {
            public double Value;
            public String Unit;
            public int UnitType;
        }

        public static class Imperial {
            public double Value;
            public String Unit;
            public int UnitType;
        }
    }

    public static class Precip1hr {
        /**
         * Value : 0.0
         * Unit : mm
         * UnitType : 3
         */

        public Metric Metric;
        /**
         * Value : 0.0
         * Unit : in
         * UnitType : 1
         */

        public Imperial Imperial;

        public static class Metric {
            public double Value;
            public String Unit;
            public int UnitType;
        }

        public static class Imperial {
            public double Value;
            public String Unit;
            public int UnitType;
        }
    }

    public static class PrecipitationSummary {
        /**
         * Metric : {"Value":0,"Unit":"mm","UnitType":3}
         * Imperial : {"Value":0,"Unit":"in","UnitType":1}
         */

        public Precipitation Precipitation;
        /**
         * Metric : {"Value":0,"Unit":"mm","UnitType":3}
         * Imperial : {"Value":0,"Unit":"in","UnitType":1}
         */

        public PastHour PastHour;
        /**
         * Metric : {"Value":0,"Unit":"mm","UnitType":3}
         * Imperial : {"Value":0,"Unit":"in","UnitType":1}
         */

        public Past3Hours Past3Hours;
        /**
         * Metric : {"Value":0,"Unit":"mm","UnitType":3}
         * Imperial : {"Value":0,"Unit":"in","UnitType":1}
         */

        public Past6Hours Past6Hours;
        /**
         * Metric : {"Value":0,"Unit":"mm","UnitType":3}
         * Imperial : {"Value":0,"Unit":"in","UnitType":1}
         */

        public Past9Hours Past9Hours;
        /**
         * Metric : {"Value":0,"Unit":"mm","UnitType":3}
         * Imperial : {"Value":0,"Unit":"in","UnitType":1}
         */

        public Past12Hours Past12Hours;
        /**
         * Metric : {"Value":0,"Unit":"mm","UnitType":3}
         * Imperial : {"Value":0,"Unit":"in","UnitType":1}
         */

        public Past18Hours Past18Hours;
        /**
         * Metric : {"Value":1,"Unit":"mm","UnitType":3}
         * Imperial : {"Value":0.05,"Unit":"in","UnitType":1}
         */

        public Past24Hours Past24Hours;

        public static class Precipitation {
            /**
             * Value : 0.0
             * Unit : mm
             * UnitType : 3
             */

            public Metric Metric;
            /**
             * Value : 0.0
             * Unit : in
             * UnitType : 1
             */

            public Imperial Imperial;

            public static class Metric {
                public double Value;
                public String Unit;
                public int UnitType;
            }

            public static class Imperial {
                public double Value;
                public String Unit;
                public int UnitType;
            }
        }

        public static class PastHour {
            /**
             * Value : 0.0
             * Unit : mm
             * UnitType : 3
             */

            public Metric Metric;
            /**
             * Value : 0.0
             * Unit : in
             * UnitType : 1
             */

            public Imperial Imperial;

            public static class Metric {
                public double Value;
                public String Unit;
                public int UnitType;
            }

            public static class Imperial {
                public double Value;
                public String Unit;
                public int UnitType;
            }
        }

        public static class Past3Hours {
            /**
             * Value : 0.0
             * Unit : mm
             * UnitType : 3
             */

            public Metric Metric;
            /**
             * Value : 0.0
             * Unit : in
             * UnitType : 1
             */

            public Imperial Imperial;

            public static class Metric {
                public double Value;
                public String Unit;
                public int UnitType;
            }

            public static class Imperial {
                public double Value;
                public String Unit;
                public int UnitType;
            }
        }

        public static class Past6Hours {
            /**
             * Value : 0.0
             * Unit : mm
             * UnitType : 3
             */

            public Metric Metric;
            /**
             * Value : 0.0
             * Unit : in
             * UnitType : 1
             */

            public Imperial Imperial;

            public static class Metric {
                public double Value;
                public String Unit;
                public int UnitType;
            }

            public static class Imperial {
                public double Value;
                public String Unit;
                public int UnitType;
            }
        }

        public static class Past9Hours {
            /**
             * Value : 0.0
             * Unit : mm
             * UnitType : 3
             */

            public Metric Metric;
            /**
             * Value : 0.0
             * Unit : in
             * UnitType : 1
             */

            public Imperial Imperial;

            public static class Metric {
                public double Value;
                public String Unit;
                public int UnitType;
            }

            public static class Imperial {
                public double Value;
                public String Unit;
                public int UnitType;
            }
        }

        public static class Past12Hours {
            /**
             * Value : 0.0
             * Unit : mm
             * UnitType : 3
             */

            public Metric Metric;
            /**
             * Value : 0.0
             * Unit : in
             * UnitType : 1
             */

            public Imperial Imperial;

            public static class Metric {
                public double Value;
                public String Unit;
                public int UnitType;
            }

            public static class Imperial {
                public double Value;
                public String Unit;
                public int UnitType;
            }
        }

        public static class Past18Hours {
            /**
             * Value : 0.0
             * Unit : mm
             * UnitType : 3
             */

            public Metric Metric;
            /**
             * Value : 0.0
             * Unit : in
             * UnitType : 1
             */

            public Imperial Imperial;

            public static class Metric {
                public double Value;
                public String Unit;
                public int UnitType;
            }

            public static class Imperial {
                public double Value;
                public String Unit;
                public int UnitType;
            }
        }

        public static class Past24Hours {
            /**
             * Value : 1.0
             * Unit : mm
             * UnitType : 3
             */

            public Metric Metric;
            /**
             * Value : 0.05
             * Unit : in
             * UnitType : 1
             */

            public Imperial Imperial;

            public static class Metric {
                public double Value;
                public String Unit;
                public int UnitType;
            }

            public static class Imperial {
                public double Value;
                public String Unit;
                public int UnitType;
            }
        }
    }

    public static class TemperatureSummary {
        /**
         * Minimum : {"Metric":{"Value":1,"Unit":"C","UnitType":17},"Imperial":{"Value":34,"Unit":"F","UnitType":18}}
         * Maximum : {"Metric":{"Value":4.1,"Unit":"C","UnitType":17},"Imperial":{"Value":39,"Unit":"F","UnitType":18}}
         */

        public Past6HourRange Past6HourRange;
        /**
         * Minimum : {"Metric":{"Value":1,"Unit":"C","UnitType":17},"Imperial":{"Value":34,"Unit":"F","UnitType":18}}
         * Maximum : {"Metric":{"Value":4.9,"Unit":"C","UnitType":17},"Imperial":{"Value":41,"Unit":"F","UnitType":18}}
         */

        public Past12HourRange Past12HourRange;
        /**
         * Minimum : {"Metric":{"Value":1,"Unit":"C","UnitType":17},"Imperial":{"Value":34,"Unit":"F","UnitType":18}}
         * Maximum : {"Metric":{"Value":5.4,"Unit":"C","UnitType":17},"Imperial":{"Value":42,"Unit":"F","UnitType":18}}
         */

        public Past24HourRange Past24HourRange;

        public static class Past6HourRange {
            /**
             * Metric : {"Value":1,"Unit":"C","UnitType":17}
             * Imperial : {"Value":34,"Unit":"F","UnitType":18}
             */

            public Minimum Minimum;
            /**
             * Metric : {"Value":4.1,"Unit":"C","UnitType":17}
             * Imperial : {"Value":39,"Unit":"F","UnitType":18}
             */

            public Maximum Maximum;

            public static class Minimum {
                /**
                 * Value : 1.0
                 * Unit : C
                 * UnitType : 17
                 */

                public Metric Metric;
                /**
                 * Value : 34.0
                 * Unit : F
                 * UnitType : 18
                 */

                public Imperial Imperial;

                public static class Metric {
                    public double Value;
                    public String Unit;
                    public int UnitType;
                }

                public static class Imperial {
                    public double Value;
                    public String Unit;
                    public int UnitType;
                }
            }

            public static class Maximum {
                /**
                 * Value : 4.1
                 * Unit : C
                 * UnitType : 17
                 */

                public Metric Metric;
                /**
                 * Value : 39.0
                 * Unit : F
                 * UnitType : 18
                 */

                public Imperial Imperial;

                public static class Metric {
                    public double Value;
                    public String Unit;
                    public int UnitType;
                }

                public static class Imperial {
                    public double Value;
                    public String Unit;
                    public int UnitType;
                }
            }
        }

        public static class Past12HourRange {
            /**
             * Metric : {"Value":1,"Unit":"C","UnitType":17}
             * Imperial : {"Value":34,"Unit":"F","UnitType":18}
             */

            public Minimum Minimum;
            /**
             * Metric : {"Value":4.9,"Unit":"C","UnitType":17}
             * Imperial : {"Value":41,"Unit":"F","UnitType":18}
             */

            public Maximum Maximum;

            public static class Minimum {
                /**
                 * Value : 1.0
                 * Unit : C
                 * UnitType : 17
                 */

                public Metric Metric;
                /**
                 * Value : 34.0
                 * Unit : F
                 * UnitType : 18
                 */

                public Imperial Imperial;

                public static class Metric {
                    public double Value;
                    public String Unit;
                    public int UnitType;
                }

                public static class Imperial {
                    public double Value;
                    public String Unit;
                    public int UnitType;
                }
            }

            public static class Maximum {
                /**
                 * Value : 4.9
                 * Unit : C
                 * UnitType : 17
                 */

                public Metric Metric;
                /**
                 * Value : 41.0
                 * Unit : F
                 * UnitType : 18
                 */

                public Imperial Imperial;

                public static class Metric {
                    public double Value;
                    public String Unit;
                    public int UnitType;
                }

                public static class Imperial {
                    public double Value;
                    public String Unit;
                    public int UnitType;
                }
            }
        }

        public static class Past24HourRange {
            /**
             * Metric : {"Value":1,"Unit":"C","UnitType":17}
             * Imperial : {"Value":34,"Unit":"F","UnitType":18}
             */

            public Minimum Minimum;
            /**
             * Metric : {"Value":5.4,"Unit":"C","UnitType":17}
             * Imperial : {"Value":42,"Unit":"F","UnitType":18}
             */

            public Maximum Maximum;

            public static class Minimum {
                /**
                 * Value : 1.0
                 * Unit : C
                 * UnitType : 17
                 */

                public Metric Metric;
                /**
                 * Value : 34.0
                 * Unit : F
                 * UnitType : 18
                 */

                public Imperial Imperial;

                public static class Metric {
                    public double Value;
                    public String Unit;
                    public int UnitType;
                }

                public static class Imperial {
                    public double Value;
                    public String Unit;
                    public int UnitType;
                }
            }

            public static class Maximum {
                /**
                 * Value : 5.4
                 * Unit : C
                 * UnitType : 17
                 */

                public Metric Metric;
                /**
                 * Value : 42.0
                 * Unit : F
                 * UnitType : 18
                 */

                public Imperial Imperial;

                public static class Metric {
                    public double Value;
                    public String Unit;
                    public int UnitType;
                }

                public static class Imperial {
                    public double Value;
                    public String Unit;
                    public int UnitType;
                }
            }
        }
    }
}
