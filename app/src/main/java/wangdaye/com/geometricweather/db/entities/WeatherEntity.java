package wangdaye.com.geometricweather.db.entities;

import java.util.Date;
import java.util.List;

import io.objectbox.Box;
import io.objectbox.BoxStore;
import io.objectbox.annotation.Convert;
import io.objectbox.annotation.Entity;
import io.objectbox.annotation.Id;
import io.objectbox.annotation.Transient;
import io.objectbox.query.Query;
import wangdaye.com.geometricweather.common.basic.models.weather.Weather;
import wangdaye.com.geometricweather.common.basic.models.weather.WeatherCode;
import wangdaye.com.geometricweather.common.basic.models.weather.WindDegree;
import wangdaye.com.geometricweather.db.converters.WeatherCodeConverter;
import wangdaye.com.geometricweather.db.converters.WindDegreeConverter;

/**
 * Weather entity.
 * {@link Weather}.
 */
@Entity
public class WeatherEntity {

    @Id
    public Long id;

    // base.
    public String cityId;
    public String weatherSource;
    public Date publishDate;
    public Date updateDate;

    // current.
    public String weatherText;
    @Convert(converter = WeatherCodeConverter.class, dbType = String.class)
    public WeatherCode weatherCode;

    public Integer temperature;
    public Integer realFeelTemperature;
    public Integer realFeelShaderTemperature;
    public Integer apparentTemperature;
    public Integer windChillTemperature;
    public Integer wetBulbTemperature;
    public Integer degreeDayTemperature;

    public String windDirection;
    @Convert(converter = WindDegreeConverter.class, dbType = Float.class)
    public WindDegree windDegree;
    public Float windSpeed;
    public String windLevel;

    public Integer uvIndex;
    public String uvLevel;
    public String uvDescription;

    public Float pm25;
    public Float pm10;
    public Float so2;
    public Float no2;
    public Float o3;
    public Float co;

    public Float relativeHumidity;
    public Float pressure;
    public Float visibility;
    public Integer dewPoint;
    public Integer cloudCover;
    public Float ceiling;

    public String dailyForecast;
    public String hourlyForecast;

    @Transient
    protected List<DailyEntity> dailyEntityList = null;
    @Transient
    protected List<HourlyEntity> hourlyEntityList = null;
    @Transient
    protected List<MinutelyEntity> minutelyEntityList = null;
    @Transient
    protected List<AlertEntity> alertEntityList = null;

    public WeatherEntity(Long id, String cityId, String weatherSource,
                         Date publishDate, Date updateDate,
                         String weatherText, WeatherCode weatherCode,
                         Integer temperature, Integer realFeelTemperature,
                         Integer realFeelShaderTemperature, Integer apparentTemperature,
                         Integer windChillTemperature, Integer wetBulbTemperature,
                         Integer degreeDayTemperature, String windDirection,
                         WindDegree windDegree, Float windSpeed, String windLevel,
                         Integer uvIndex, String uvLevel, String uvDescription,
                         Float pm25, Float pm10, Float so2, Float no2,
                         Float o3, Float co, Float relativeHumidity, Float pressure,
                         Float visibility, Integer dewPoint, Integer cloudCover, Float ceiling,
                         String dailyForecast, String hourlyForecast) {
        this.id = id;
        this.cityId = cityId;
        this.weatherSource = weatherSource;
        this.publishDate = publishDate;
        this.updateDate = updateDate;
        this.weatherText = weatherText;
        this.weatherCode = weatherCode;
        this.temperature = temperature;
        this.realFeelTemperature = realFeelTemperature;
        this.realFeelShaderTemperature = realFeelShaderTemperature;
        this.apparentTemperature = apparentTemperature;
        this.windChillTemperature = windChillTemperature;
        this.wetBulbTemperature = wetBulbTemperature;
        this.degreeDayTemperature = degreeDayTemperature;
        this.windDirection = windDirection;
        this.windDegree = windDegree;
        this.windSpeed = windSpeed;
        this.windLevel = windLevel;
        this.uvIndex = uvIndex;
        this.uvLevel = uvLevel;
        this.uvDescription = uvDescription;
        this.pm25 = pm25;
        this.pm10 = pm10;
        this.so2 = so2;
        this.no2 = no2;
        this.o3 = o3;
        this.co = co;
        this.relativeHumidity = relativeHumidity;
        this.pressure = pressure;
        this.visibility = visibility;
        this.dewPoint = dewPoint;
        this.cloudCover = cloudCover;
        this.ceiling = ceiling;
        this.dailyForecast = dailyForecast;
        this.hourlyForecast = hourlyForecast;
    }


    public WeatherEntity() {
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

    public Date getPublishDate() {
        return this.publishDate;
    }

    public void setPublishDate(Date publishDate) {
        this.publishDate = publishDate;
    }

    public Date getUpdateDate() {
        return this.updateDate;
    }

    public void setUpdateDate(Date updateDate) {
        this.updateDate = updateDate;
    }

    public String getWeatherText() {
        return this.weatherText;
    }

    public void setWeatherText(String weatherText) {
        this.weatherText = weatherText;
    }

    public WeatherCode getWeatherCode() {
        return this.weatherCode;
    }

    public void setWeatherCode(WeatherCode weatherCode) {
        this.weatherCode = weatherCode;
    }

    public Integer getTemperature() {
        return this.temperature;
    }

    public void setTemperature(Integer temperature) {
        this.temperature = temperature;
    }

    public Integer getRealFeelTemperature() {
        return this.realFeelTemperature;
    }

    public void setRealFeelTemperature(Integer realFeelTemperature) {
        this.realFeelTemperature = realFeelTemperature;
    }

    public Integer getRealFeelShaderTemperature() {
        return this.realFeelShaderTemperature;
    }

    public void setRealFeelShaderTemperature(Integer realFeelShaderTemperature) {
        this.realFeelShaderTemperature = realFeelShaderTemperature;
    }

    public Integer getApparentTemperature() {
        return this.apparentTemperature;
    }

    public void setApparentTemperature(Integer apparentTemperature) {
        this.apparentTemperature = apparentTemperature;
    }

    public Integer getWindChillTemperature() {
        return this.windChillTemperature;
    }

    public void setWindChillTemperature(Integer windChillTemperature) {
        this.windChillTemperature = windChillTemperature;
    }

    public Integer getWetBulbTemperature() {
        return this.wetBulbTemperature;
    }

    public void setWetBulbTemperature(Integer wetBulbTemperature) {
        this.wetBulbTemperature = wetBulbTemperature;
    }

    public Integer getDegreeDayTemperature() {
        return this.degreeDayTemperature;
    }

    public void setDegreeDayTemperature(Integer degreeDayTemperature) {
        this.degreeDayTemperature = degreeDayTemperature;
    }

    public String getWindDirection() {
        return this.windDirection;
    }

    public void setWindDirection(String windDirection) {
        this.windDirection = windDirection;
    }

    public WindDegree getWindDegree() {
        return this.windDegree;
    }

    public void setWindDegree(WindDegree windDegree) {
        this.windDegree = windDegree;
    }

    public Float getWindSpeed() {
        return this.windSpeed;
    }

    public void setWindSpeed(Float windSpeed) {
        this.windSpeed = windSpeed;
    }

    public String getWindLevel() {
        return this.windLevel;
    }

    public void setWindLevel(String windLevel) {
        this.windLevel = windLevel;
    }

    public Integer getUvIndex() {
        return this.uvIndex;
    }

    public void setUvIndex(Integer uvIndex) {
        this.uvIndex = uvIndex;
    }

    public String getUvLevel() {
        return this.uvLevel;
    }

    public void setUvLevel(String uvLevel) {
        this.uvLevel = uvLevel;
    }

    public String getUvDescription() {
        return this.uvDescription;
    }

    public void setUvDescription(String uvDescription) {
        this.uvDescription = uvDescription;
    }

    public Float getPm25() {
        return this.pm25;
    }

    public void setPm25(Float pm25) {
        this.pm25 = pm25;
    }

    public Float getPm10() {
        return this.pm10;
    }

    public void setPm10(Float pm10) {
        this.pm10 = pm10;
    }

    public Float getSo2() {
        return this.so2;
    }

    public void setSo2(Float so2) {
        this.so2 = so2;
    }

    public Float getNo2() {
        return this.no2;
    }

    public void setNo2(Float no2) {
        this.no2 = no2;
    }

    public Float getO3() {
        return this.o3;
    }

    public void setO3(Float o3) {
        this.o3 = o3;
    }

    public Float getCo() {
        return this.co;
    }

    public void setCo(Float co) {
        this.co = co;
    }

    public Float getRelativeHumidity() {
        return this.relativeHumidity;
    }

    public void setRelativeHumidity(Float relativeHumidity) {
        this.relativeHumidity = relativeHumidity;
    }

    public Float getPressure() {
        return this.pressure;
    }

    public void setPressure(Float pressure) {
        this.pressure = pressure;
    }

    public Float getVisibility() {
        return this.visibility;
    }

    public void setVisibility(Float visibility) {
        this.visibility = visibility;
    }

    public Integer getDewPoint() {
        return this.dewPoint;
    }

    public void setDewPoint(Integer dewPoint) {
        this.dewPoint = dewPoint;
    }

    public Integer getCloudCover() {
        return this.cloudCover;
    }

    public void setCloudCover(Integer cloudCover) {
        this.cloudCover = cloudCover;
    }

    public Float getCeiling() {
        return this.ceiling;
    }

    public void setCeiling(Float ceiling) {
        this.ceiling = ceiling;
    }

    public String getDailyForecast() {
        return this.dailyForecast;
    }

    public void setDailyForecast(String dailyForecast) {
        this.dailyForecast = dailyForecast;
    }

    public String getHourlyForecast() {
        return this.hourlyForecast;
    }

    public void setHourlyForecast(String hourlyForecast) {
        this.hourlyForecast = hourlyForecast;
    }

    /**
     * To-many relationship, resolved on first access (and after reset).
     * Changes to to-many relations are not persisted, make changes to the target entity.
     */
    public List<DailyEntity> getDailyEntityList(BoxStore boxStore) {
        if (dailyEntityList == null) {
            Box<DailyEntity> dailyEntityBox = boxStore.boxFor(DailyEntity.class);
            Query<DailyEntity> query = dailyEntityBox.query(
                            DailyEntity_.cityId.equal(cityId)
                                    .and(DailyEntity_.weatherSource.equal(weatherSource)
                                    ))
                    .order(DailyEntity_.date)
                    .build();
            List<DailyEntity> dailyEntityListNew = query.find();
            query.close();
            synchronized (this) {
                if (dailyEntityList == null) {
                    dailyEntityList = dailyEntityListNew;
                }
            }
        }
        return dailyEntityList;
    }

    /**
     * Resets a to-many relationship, making the next get call to query for a fresh result.
     */

    public synchronized void resetDailyEntityList() {
        dailyEntityList = null;
    }

    /**
     * To-many relationship, resolved on first access (and after reset).
     * Changes to to-many relations are not persisted, make changes to the target entity.
     */

    public List<HourlyEntity> getHourlyEntityList(BoxStore boxStore) {
        if (hourlyEntityList == null) {
            Box<HourlyEntity> hourlyEntityBox = boxStore.boxFor(HourlyEntity.class);
            Query<HourlyEntity> query = hourlyEntityBox.query(
                            HourlyEntity_.cityId.equal(cityId)
                                    .and(HourlyEntity_.weatherSource.equal(weatherSource)
                                    ))
                    .order(HourlyEntity_.date)
                    .build();
            List<HourlyEntity> hourlyEntityListNew = query.find();
            query.close();
            synchronized (this) {
                if (hourlyEntityList == null) {
                    hourlyEntityList = hourlyEntityListNew;
                }
            }
        }
        return hourlyEntityList;
    }

    /**
     * Resets a to-many relationship, making the next get call to query for a fresh result.
     */

    public synchronized void resetHourlyEntityList() {
        hourlyEntityList = null;
    }

    /**
     * To-many relationship, resolved on first access (and after reset).
     * Changes to to-many relations are not persisted, make changes to the target entity.
     */

    public List<MinutelyEntity> getMinutelyEntityList(BoxStore boxStore) {
        if (minutelyEntityList == null) {
            Box<MinutelyEntity> minutelyEntityBox = boxStore.boxFor(MinutelyEntity.class);
            Query<MinutelyEntity> query = minutelyEntityBox.query(
                            MinutelyEntity_.cityId.equal(cityId)
                                    .and(MinutelyEntity_.weatherSource.equal(weatherSource)
                                    ))
                    .order(MinutelyEntity_.date)
                    .build();
            List<MinutelyEntity> minutelyEntityListNew = query.find();
            query.close();
            synchronized (this) {
                if (minutelyEntityList == null) {
                    minutelyEntityList = minutelyEntityListNew;
                }
            }
        }
        return minutelyEntityList;
    }

    /**
     * Resets a to-many relationship, making the next get call to query for a fresh result.
     */

    public synchronized void resetMinutelyEntityList() {
        minutelyEntityList = null;
    }

    /**
     * To-many relationship, resolved on first access (and after reset).
     * Changes to to-many relations are not persisted, make changes to the target entity.
     */

    public List<AlertEntity> getAlertEntityList(BoxStore boxStore) {
        if (alertEntityList == null) {
            Box<AlertEntity> alertEntityBox = boxStore.boxFor(AlertEntity.class);
            Query<AlertEntity> query = alertEntityBox.query(
                            AlertEntity_.cityId.equal(cityId)
                                    .and(AlertEntity_.weatherSource.equal(weatherSource)
                                    ))
                    .order(AlertEntity_.date)
                    .build();
            List<AlertEntity> alertEntityListNew = query.find();
            query.close();
            synchronized (this) {
                if (alertEntityList == null) {
                    alertEntityList = alertEntityListNew;
                }
            }
        }
        return alertEntityList;
    }

    /**
     * Resets a to-many relationship, making the next get call to query for a fresh result.
     */
    public synchronized void resetAlertEntityList() {
        alertEntityList = null;
    }
}
