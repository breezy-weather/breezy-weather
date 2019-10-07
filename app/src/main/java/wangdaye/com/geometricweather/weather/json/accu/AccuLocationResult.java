package wangdaye.com.geometricweather.weather.json.accu;

import java.util.List;

/**
 * Accu location result.
 * */

public class AccuLocationResult {

    /**
     * Version : 1
     * Key : 1-106573_13_AL
     * Type : City
     * Rank : 13
     * LocalizedName : 青岛市
     * EnglishName : Qingdao
     * PrimaryPostalCode :
     * Region : {"ID":"ASI","LocalizedName":"亚洲","EnglishName":"Asia"}
     * Country : {"ID":"CN","LocalizedName":"中国","EnglishName":"China"}
     * AdministrativeArea : {"ID":"37","LocalizedName":"山东省","EnglishName":"Shandong","Level":1,"LocalizedType":"省","EnglishType":"Province","CountryID":"CN"}
     * TimeZone : {"Code":"CST","Name":"Asia/Shanghai","GmtOffset":8,"IsDaylightSaving":false,"NextOffsetChange":null}
     * GeoPosition : {"Latitude":36.07,"Longitude":120.323,"Elevation":{"Metric":{"Value":32,"Unit":"m","UnitType":5},"Imperial":{"Value":104,"Unit":"ft","UnitType":0}}}
     * IsAlias : true
     * SupplementalAdminAreas : []
     * DataSets : ["AirQuality","Alerts","PremiumAirQuality"]
     */

    public int Version;
    public String Key;
    public String Type;
    public int Rank;
    public String LocalizedName;
    public String EnglishName;
    public String PrimaryPostalCode;
    /**
     * ID : ASI
     * LocalizedName : 亚洲
     * EnglishName : Asia
     */

    public Region Region;
    /**
     * ID : CN
     * LocalizedName : 中国
     * EnglishName : China
     */

    public Country Country;
    /**
     * ID : 37
     * LocalizedName : 山东省
     * EnglishName : Shandong
     * Level : 1
     * LocalizedType : 省
     * EnglishType : Province
     * CountryID : CN
     */

    public AdministrativeArea AdministrativeArea;
    /**
     * Code : CST
     * Name : Asia/Shanghai
     * GmtOffset : 8.0
     * IsDaylightSaving : false
     * NextOffsetChange : null
     */

    public TimeZone TimeZone;
    /**
     * Latitude : 36.07
     * Longitude : 120.323
     * Elevation : {"Metric":{"Value":32,"Unit":"m","UnitType":5},"Imperial":{"Value":104,"Unit":"ft","UnitType":0}}
     */

    public GeoPosition GeoPosition;
    public boolean IsAlias;
    public List<?> SupplementalAdminAreas;
    public List<String> DataSets;

    public static class Region {
        public String ID;
        public String LocalizedName;
        public String EnglishName;
    }

    public static class Country {
        public String ID;
        public String LocalizedName;
        public String EnglishName;
    }

    public static class AdministrativeArea {
        public String ID;
        public String LocalizedName;
        public String EnglishName;
        public int Level;
        public String LocalizedType;
        public String EnglishType;
        public String CountryID;
    }

    public static class TimeZone {
        public String Code;
        public String Name;
        public double GmtOffset;
        public boolean IsDaylightSaving;
        public Object NextOffsetChange;
    }

    public static class GeoPosition {
        public double Latitude;
        public double Longitude;
        /**
         * Metric : {"Value":32,"Unit":"m","UnitType":5}
         * Imperial : {"Value":104,"Unit":"ft","UnitType":0}
         */

        public Elevation Elevation;

        public static class Elevation {
            /**
             * Value : 32.0
             * Unit : m
             * UnitType : 5
             */

            public Metric Metric;
            /**
             * Value : 104.0
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
    }
}
