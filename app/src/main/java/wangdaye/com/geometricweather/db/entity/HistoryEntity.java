package wangdaye.com.geometricweather.db.entity;

import org.greenrobot.greendao.annotation.Entity;

import org.greenrobot.greendao.annotation.Id;

import java.util.Date;
import org.greenrobot.greendao.annotation.Generated;

/**
 * History entity.
 *
 * {@link wangdaye.com.geometricweather.basic.model.weather.History}.
 * */

@Entity
public class HistoryEntity {

    @Id public Long id;
    public String cityId;

    public Date date;
    public long time;

    public int daytimeTemperature;
    public int nighttimeTemperature;

    @Generated(hash = 902661261)
    public HistoryEntity(Long id, String cityId, Date date, long time,
            int daytimeTemperature, int nighttimeTemperature) {
        this.id = id;
        this.cityId = cityId;
        this.date = date;
        this.time = time;
        this.daytimeTemperature = daytimeTemperature;
        this.nighttimeTemperature = nighttimeTemperature;
    }
    @Generated(hash = 1235354573)
    public HistoryEntity() {
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
    public Date getDate() {
        return this.date;
    }
    public void setDate(Date date) {
        this.date = date;
    }
    public long getTime() {
        return this.time;
    }
    public void setTime(long time) {
        this.time = time;
    }
    public int getDaytimeTemperature() {
        return this.daytimeTemperature;
    }
    public void setDaytimeTemperature(int daytimeTemperature) {
        this.daytimeTemperature = daytimeTemperature;
    }
    public int getNighttimeTemperature() {
        return this.nighttimeTemperature;
    }
    public void setNighttimeTemperature(int nighttimeTemperature) {
        this.nighttimeTemperature = nighttimeTemperature;
    }
}
