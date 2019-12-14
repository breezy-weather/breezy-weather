package wangdaye.com.geometricweather.db.entity;

import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Id;

import java.util.Date;
import org.greenrobot.greendao.annotation.Generated;

import wangdaye.com.geometricweather.basic.model.option.provider.WeatherSource;

/**
 * Alert entity.
 *
 * {@link wangdaye.com.geometricweather.basic.model.weather.Alert}
 * */
@Entity
public class AlertEntity {

    @Id public Long id;
    public String cityId;
    public String weatherSource;
    
    public long alertId;
    public Date date;
    public long time;

    public String description;
    public String content;

    public String type;
    public int priority;
    public int color;
    @Generated(hash = 1514490199)
    public AlertEntity(Long id, String cityId, String weatherSource, long alertId,
            Date date, long time, String description, String content, String type,
            int priority, int color) {
        this.id = id;
        this.cityId = cityId;
        this.weatherSource = weatherSource;
        this.alertId = alertId;
        this.date = date;
        this.time = time;
        this.description = description;
        this.content = content;
        this.type = type;
        this.priority = priority;
        this.color = color;
    }
    @Generated(hash = 307089144)
    public AlertEntity() {
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
    public long getAlertId() {
        return this.alertId;
    }
    public void setAlertId(long alertId) {
        this.alertId = alertId;
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
    public String getDescription() {
        return this.description;
    }
    public void setDescription(String description) {
        this.description = description;
    }
    public String getContent() {
        return this.content;
    }
    public void setContent(String content) {
        this.content = content;
    }
    public String getType() {
        return this.type;
    }
    public void setType(String type) {
        this.type = type;
    }
    public int getPriority() {
        return this.priority;
    }
    public void setPriority(int priority) {
        this.priority = priority;
    }
    public int getColor() {
        return this.color;
    }
    public void setColor(int color) {
        this.color = color;
    }

}
