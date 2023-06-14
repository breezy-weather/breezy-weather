package org.breezyweather.db.entities;

import java.util.Date;

import io.objectbox.annotation.Entity;
import io.objectbox.annotation.Id;
import org.breezyweather.common.basic.models.weather.Alert;

/**
 * Alert entity.
 *
 * {@link Alert}
 * */
@Entity
public class AlertEntity {

    @Id public Long id;
    public String cityId;
    public String weatherSource;
    
    public long alertId;
    public Date startDate;
    public Date endDate;

    public String description;
    public String content;

    public String type;
    public int priority;
    
    public AlertEntity(Long id, String cityId, String weatherSource, long alertId,
                       Date startDate, Date endDate, String description, String content, String type,
                       int priority) {
        this.id = id;
        this.cityId = cityId;
        this.weatherSource = weatherSource;
        this.alertId = alertId;
        this.startDate = startDate;
        this.endDate = endDate;
        this.description = description;
        this.content = content;
        this.type = type;
        this.priority = priority;
    }
    
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
    public Date getStartDate() {
        return this.startDate;
    }
    public void setStartDate(Date startDate) {
        this.startDate = startDate;
    }
    public Date getEndDate() {
        return this.endDate;
    }
    public void setEndDate(Date endDate) {
        this.endDate = endDate;
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

}
