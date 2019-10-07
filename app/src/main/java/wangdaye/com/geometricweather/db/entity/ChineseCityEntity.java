package wangdaye.com.geometricweather.db.entity;

import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Id;
import org.greenrobot.greendao.annotation.Generated;

/**
 * Chinese city entity.
 *
 * {@link wangdaye.com.geometricweather.basic.model.location.ChineseCity}.
 * */

@Entity
public class ChineseCityEntity {

    @Id public Long id;

    public String cityId;
    public String province;
    public String city;
    public String district;
    public String latitude;
    public String longitude;

    @Generated(hash = 787683596)
    public ChineseCityEntity(Long id, String cityId, String province, String city,
            String district, String latitude, String longitude) {
        this.id = id;
        this.cityId = cityId;
        this.province = province;
        this.city = city;
        this.district = district;
        this.latitude = latitude;
        this.longitude = longitude;
    }
    @Generated(hash = 1803922116)
    public ChineseCityEntity() {
    }
    public Long getId() {
        return this.id;
    }
    public void setId(Long id) {
        this.id = id;
    }
    public String getCityId() {
        return this.cityId;
    }
    public void setCityId(String cityId) {
        this.cityId = cityId;
    }
    public String getProvince() {
        return this.province;
    }
    public void setProvince(String province) {
        this.province = province;
    }
    public String getCity() {
        return this.city;
    }
    public void setCity(String city) {
        this.city = city;
    }
    public String getDistrict() {
        return this.district;
    }
    public void setDistrict(String district) {
        this.district = district;
    }
    public String getLatitude() {
        return this.latitude;
    }
    public void setLatitude(String latitude) {
        this.latitude = latitude;
    }
    public String getLongitude() {
        return this.longitude;
    }
    public void setLongitude(String longitude) {
        this.longitude = longitude;
    }
}
