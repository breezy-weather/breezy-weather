package wangdaye.com.geometricweather.basic.model.location;

import java.util.TimeZone;

import wangdaye.com.geometricweather.basic.model.option.provider.WeatherSource;

/**
 * Chinese city.
 * */

public class ChineseCity {

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

    public ChineseCity(String cityId,
                       String province, String city, String district,
                       String latitude, String longitude) {
        this.cityId = cityId;
        this.province = province;
        this.city = city;
        this.district = district;
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public enum CNWeatherSource {

        CN(WeatherSource.CN),
        CAIYUN(WeatherSource.CAIYUN);

        CNWeatherSource(WeatherSource source) {
            this.source = source;
        }

        private WeatherSource source;

        private WeatherSource getSource() {
            return source;
        }
    }

    public Location toLocation(CNWeatherSource source) {
        return new Location(
                getCityId(),
                Float.parseFloat(getLatitude()), Float.parseFloat(getLongitude()), TimeZone.getTimeZone("Asia/Shanghai"),
                "中国", getProvince(), getCity(), getDistrict().equals("无") ? "" : getDistrict(),
                null, source.getSource(),
                false, false, true
        );
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
