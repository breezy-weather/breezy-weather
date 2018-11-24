package wangdaye.com.geometricweather.data.entity.model;

import java.util.List;

import wangdaye.com.geometricweather.data.entity.table.CNCityEntity;

/**
 * CN city list.
 * */

public class CNCityList {

    public List<CNCity> citys;

    public static class CNCity {
        /**
         * id : 110101
         * province : 北京市
         * city : 东城区
         * district : 无
         * lat : 39.928353
         * lon : 116.416357
         * requestKey : 101011600
         */

        public String id;
        public String province;
        public String city;
        public String district;
        public String lat;
        public String lon;
        public String requestKey;

        public static CNCity buildCNCity(CNCityEntity entity) {
            CNCity city = new CNCity();
            city.id = String.valueOf(entity.id);
            city.province = entity.province;
            city.city = entity.city;
            city.district = entity.district;
            city.lat = entity.lat;
            city.lon = entity.lon;
            city.requestKey = entity.requestKey;
            return city;
        }
    }
}
