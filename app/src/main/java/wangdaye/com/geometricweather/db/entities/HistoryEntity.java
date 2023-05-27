package wangdaye.com.geometricweather.db.entities;

import java.util.Date;

import io.objectbox.annotation.Entity;
import io.objectbox.annotation.Id;
import wangdaye.com.geometricweather.common.basic.models.weather.History;

/**
 * History entity.
 *
 * {@link History}.
 * */

@Entity
public class HistoryEntity {

    @Id public Long id;
    public String cityId;
    public String weatherSource;

    public Date date;
    public long time;

    public int daytimeTemperature;
    public int nighttimeTemperature;
    
    public HistoryEntity(Long id, String cityId, String weatherSource, Date date,
            long time, int daytimeTemperature, int nighttimeTemperature) {
        this.id = id;
        this.cityId = cityId;
        this.weatherSource = weatherSource;
        this.date = date;
        this.time = time;
        this.daytimeTemperature = daytimeTemperature;
        this.nighttimeTemperature = nighttimeTemperature;
    }
    
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
    public String getWeatherSource() {
        return this.weatherSource;
    }
    public void setWeatherSource(String weatherSource) {
        this.weatherSource = weatherSource;
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
