package wangdaye.com.geometricweather.basic.model;

import androidx.annotation.StringDef;

import wangdaye.com.geometricweather.db.entity.CNCityEntity;

/**
 * CN city.
 * */

public class CNCity {

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

    @StringDef({Location.WEATHER_SOURCE_CN, Location.WEATHER_SOURCE_CAIYUN})
    public @interface CNCityWeatherSourceRule {}

    public CNCityEntity toCNCityEntity() {
        CNCityEntity entity = new CNCityEntity();
        entity.province = getProvince();
        entity.city = getCity();
        entity.district = getDistrict();
        entity.lat = getLatitude();
        entity.lon = getLongitude();
        entity.requestKey = getCityId();
        return entity;
    }

    public Location toLocation(@CNCityWeatherSourceRule String source) {
        return new Location(
                getCityId(),
                getDistrict().equals("无") ? "" : getDistrict(),
                getCity(), getProvince(), "中国",
                getLatitude(), getLongitude(), source,
                null, null, false, true
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
