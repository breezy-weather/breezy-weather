package wangdaye.com.geometricweather.db.entity;

import org.greenrobot.greendao.annotation.Convert;
import org.greenrobot.greendao.annotation.Entity;

import wangdaye.com.geometricweather.basic.model.option.provider.WeatherSource;
import wangdaye.com.geometricweather.db.propertyConverter.TimeZoneConverter;
import wangdaye.com.geometricweather.db.propertyConverter.WeatherSourceConverter;

import org.greenrobot.greendao.annotation.Id;

import org.greenrobot.greendao.annotation.Generated;

import java.util.TimeZone;

/**
 * Location entity.
 *
 * {@link wangdaye.com.geometricweather.basic.model.location.Location}.
 * */

@Entity
public class LocationEntity {

    @Id public String formattedId;

    public String cityId;

    public float latitude;
    public float longitude;

    @Convert(converter = TimeZoneConverter.class, columnType = String.class)
    public TimeZone timeZone;

    public String country;
    public String province;
    public String city;
    public String district;

    @Convert(converter = WeatherSourceConverter.class, columnType = String.class)
    public WeatherSource weatherSource;

    public boolean currentPosition;
    public boolean residentPosition;
    public boolean china;

    public long sequence;

    @Generated(hash = 212187184)
    public LocationEntity(String formattedId, String cityId, float latitude,
            float longitude, TimeZone timeZone, String country, String province,
            String city, String district, WeatherSource weatherSource,
            boolean currentPosition, boolean residentPosition, boolean china,
            long sequence) {
        this.formattedId = formattedId;
        this.cityId = cityId;
        this.latitude = latitude;
        this.longitude = longitude;
        this.timeZone = timeZone;
        this.country = country;
        this.province = province;
        this.city = city;
        this.district = district;
        this.weatherSource = weatherSource;
        this.currentPosition = currentPosition;
        this.residentPosition = residentPosition;
        this.china = china;
        this.sequence = sequence;
    }

    @Generated(hash = 1723987110)
    public LocationEntity() {
    }

    public String getFormattedId() {
        return this.formattedId;
    }

    public void setFormattedId(String formattedId) {
        this.formattedId = formattedId;
    }

    public String getCityId() {
        return this.cityId;
    }

    public void setCityId(String cityId) {
        this.cityId = cityId;
    }

    public float getLatitude() {
        return this.latitude;
    }

    public void setLatitude(float latitude) {
        this.latitude = latitude;
    }

    public float getLongitude() {
        return this.longitude;
    }

    public void setLongitude(float longitude) {
        this.longitude = longitude;
    }

    public TimeZone getTimeZone() {
        return this.timeZone;
    }

    public void setTimeZone(TimeZone timeZone) {
        this.timeZone = timeZone;
    }

    public String getCountry() {
        return this.country;
    }

    public void setCountry(String country) {
        this.country = country;
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

    public WeatherSource getWeatherSource() {
        return this.weatherSource;
    }

    public void setWeatherSource(WeatherSource weatherSource) {
        this.weatherSource = weatherSource;
    }

    public boolean getCurrentPosition() {
        return this.currentPosition;
    }

    public void setCurrentPosition(boolean currentPosition) {
        this.currentPosition = currentPosition;
    }

    public boolean getResidentPosition() {
        return this.residentPosition;
    }

    public void setResidentPosition(boolean residentPosition) {
        this.residentPosition = residentPosition;
    }

    public boolean getChina() {
        return this.china;
    }

    public void setChina(boolean china) {
        this.china = china;
    }

    public long getSequence() {
        return this.sequence;
    }

    public void setSequence(long sequence) {
        this.sequence = sequence;
    }
}