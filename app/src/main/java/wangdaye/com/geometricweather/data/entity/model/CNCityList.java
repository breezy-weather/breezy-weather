package wangdaye.com.geometricweather.data.entity.model;

import java.util.List;

import wangdaye.com.geometricweather.data.entity.table.CNCityEntity;

/**
 * CN city list.
 * */

public class CNCityList {

    private List<CNCity> cities;

    public List<CNCity> getCities() {
        return cities;
    }

    public void setCities(List<CNCity> cities) {
        this.cities = cities;
    }

    public static class CNCity {
        /**
         * cityId : 101010100
         * province : 北京
         * city : 北京
         * district : 北京
         * latitude : 39.904987
         * longitude : 116.40529
         */

        private String cityId;
        private String province;
        private String city;
        private String district;
        private String latitude;
        private String longitude;

        public static CNCity buildCNCity(CNCityEntity entity) {
            CNCity city = new CNCity();
            city.setCityId(entity.requestKey);
            city.setProvince(entity.province);
            city.setCity(entity.city);
            city.setDistrict(entity.district);
            city.setLatitude(entity.lat);
            city.setLongitude(entity.lon);
            return city;
        }

        public String getCityId() {
            return cityId;
        }

        public void setCityId(String cityId) {
            this.cityId = cityId;
        }

        public String getProvince() {
            return province;
        }

        public void setProvince(String province) {
            this.province = province;
        }

        public String getCity() {
            return city;
        }

        public void setCity(String city) {
            this.city = city;
        }

        public String getDistrict() {
            return district;
        }

        public void setDistrict(String district) {
            this.district = district;
        }

        public String getLatitude() {
            return latitude;
        }

        public void setLatitude(String latitude) {
            this.latitude = latitude;
        }

        public String getLongitude() {
            return longitude;
        }

        public void setLongitude(String longitude) {
            this.longitude = longitude;
        }
    }
}
