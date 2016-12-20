package wangdaye.com.geometricweather.data.entity.result;

import java.util.List;

/**
 * Oversea city result.
 * */

public class OverseaCityListResult {

    /**
     * id : JP1850147
     * cityEn : Tokyo
     * cityZh : 东京
     * continent : 亚洲
     * countryCode : JP
     * countryEn : Japan
     * lat : 35.689499
     * lon : 139.691711
     */

    public List<CityInfo> city_info;

    public static class CityInfo {
        public String id;
        public String cityEn;
        public String cityZh;
        public String continent;
        public String countryCode;
        public String countryEn;
        public String lat;
        public String lon;
    }
}
