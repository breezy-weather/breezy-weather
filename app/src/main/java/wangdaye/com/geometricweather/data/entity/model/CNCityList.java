package wangdaye.com.geometricweather.data.entity.model;

import java.util.List;

import wangdaye.com.geometricweather.data.entity.table.CNCityEntity;

/**
 * CN city.
 * */

public class CNCityList {

    public List<CNCity> citylist;

    public static class CNCity {
        /**
         * name : 北京
         * id : 101010100
         * province_name : 北京
         */

        public String name;
        public String id;
        public String province_name;

        public static CNCity buildCNCity(CNCityEntity entity) {
            CNCity city = new CNCity();
            city.name = entity.cityName;
            city.id = entity.cityId;
            city.province_name = entity.province;
            return city;
        }
    }
}
