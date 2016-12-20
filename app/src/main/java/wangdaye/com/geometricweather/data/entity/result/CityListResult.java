package wangdaye.com.geometricweather.data.entity.result;

import java.util.List;

/**
 * City list result.
 * */

public class CityListResult {

    /**
     * city : 南子岛
     * cnty : 中国
     * id : CN101310230
     * lat : 11.26
     * lon : 114.20
     * prov : 海南
     */

    public List<CityInfo> city_info;

    public static class CityInfo {
        public String id;
        public String city;
        public String cnty;
        public String lat;
        public String lon;
        public String prov;
    }
}
