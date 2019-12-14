package wangdaye.com.geometricweather.db.entity;

import org.greenrobot.greendao.annotation.Convert;
import org.greenrobot.greendao.annotation.Entity;

import org.greenrobot.greendao.annotation.Id;

import java.util.Date;
import java.util.List;

import org.greenrobot.greendao.annotation.JoinProperty;
import org.greenrobot.greendao.annotation.OrderBy;
import org.greenrobot.greendao.annotation.ToMany;

import wangdaye.com.geometricweather.basic.model.weather.WeatherCode;
import wangdaye.com.geometricweather.basic.model.weather.WindDegree;
import wangdaye.com.geometricweather.db.propertyConverter.WeatherCodeConverter;
import wangdaye.com.geometricweather.db.propertyConverter.WindDegreeConverter;

import org.greenrobot.greendao.annotation.Generated;
import org.greenrobot.greendao.DaoException;

/**
 * Weather entity.
 *
 * {@link wangdaye.com.geometricweather.basic.model.weather.Weather}.
 * */
@Entity
public class WeatherEntity {

    @Id public Long id;

    // base.
    public String cityId;
    public String weatherSource;
    public long timeStamp;
    public Date publishDate;
    public long publishTime;
    public Date updateDate;
    public long updateTime;

    // current.
    public String weatherText;
    @Convert(converter = WeatherCodeConverter.class, columnType = String.class)
    public WeatherCode weatherCode;

    public int temperature;
    public Integer realFeelTemperature;
    public Integer realFeelShaderTemperature;
    public Integer apparentTemperature;
    public Integer windChillTemperature;
    public Integer wetBulbTemperature;
    public Integer degreeDayTemperature;

    public Float totalPrecipitation;
    public Float thunderstormPrecipitation;
    public Float rainPrecipitation;
    public Float snowPrecipitation;
    public Float icePrecipitation;

    public Float totalPrecipitationProbability;
    public Float thunderstormPrecipitationProbability;
    public Float rainPrecipitationProbability;
    public Float snowPrecipitationProbability;
    public Float icePrecipitationProbability;

    public String windDirection;
    @Convert(converter = WindDegreeConverter.class, columnType = Float.class)
    public WindDegree windDegree;
    public Float windSpeed;
    public String windLevel;

    public Integer uvIndex;
    public String uvLevel;
    public String uvDescription;

    public String aqiText;
    public Integer aqiIndex;
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

    @ToMany(joinProperties = {
            @JoinProperty(name = "cityId", referencedName = "cityId"),
            @JoinProperty(name = "weatherSource", referencedName = "weatherSource")
    })
    @OrderBy("date ASC")
    public List<DailyEntity> dailyEntityList;

    @ToMany(joinProperties = {
            @JoinProperty(name = "cityId", referencedName = "cityId"),
            @JoinProperty(name = "weatherSource", referencedName = "weatherSource")
    })
    @OrderBy("date ASC")
    public List<HourlyEntity> hourlyEntityList;

    @ToMany(joinProperties = {
            @JoinProperty(name = "cityId", referencedName = "cityId"),
            @JoinProperty(name = "weatherSource", referencedName = "weatherSource")
    })
    @OrderBy("date ASC")
    public List<MinutelyEntity> minutelyEntityList;

    @ToMany(joinProperties = {
            @JoinProperty(name = "cityId", referencedName = "cityId"),
            @JoinProperty(name = "weatherSource", referencedName = "weatherSource")
    })
    @OrderBy("date ASC")
    public List<AlertEntity> alertEntityList;

/** Used to resolve relations */
@Generated(hash = 2040040024)
private transient DaoSession daoSession;

/** Used for active entity operations. */
@Generated(hash = 428286643)
private transient WeatherEntityDao myDao;

@Generated(hash = 836344586)
public WeatherEntity(Long id, String cityId, String weatherSource,
        long timeStamp, Date publishDate, long publishTime, Date updateDate,
        long updateTime, String weatherText, WeatherCode weatherCode,
        int temperature, Integer realFeelTemperature,
        Integer realFeelShaderTemperature, Integer apparentTemperature,
        Integer windChillTemperature, Integer wetBulbTemperature,
        Integer degreeDayTemperature, Float totalPrecipitation,
        Float thunderstormPrecipitation, Float rainPrecipitation,
        Float snowPrecipitation, Float icePrecipitation,
        Float totalPrecipitationProbability,
        Float thunderstormPrecipitationProbability,
        Float rainPrecipitationProbability, Float snowPrecipitationProbability,
        Float icePrecipitationProbability, String windDirection,
        WindDegree windDegree, Float windSpeed, String windLevel,
        Integer uvIndex, String uvLevel, String uvDescription, String aqiText,
        Integer aqiIndex, Float pm25, Float pm10, Float so2, Float no2,
        Float o3, Float co, Float relativeHumidity, Float pressure,
        Float visibility, Integer dewPoint, Integer cloudCover, Float ceiling,
        String dailyForecast, String hourlyForecast) {
    this.id = id;
    this.cityId = cityId;
    this.weatherSource = weatherSource;
    this.timeStamp = timeStamp;
    this.publishDate = publishDate;
    this.publishTime = publishTime;
    this.updateDate = updateDate;
    this.updateTime = updateTime;
    this.weatherText = weatherText;
    this.weatherCode = weatherCode;
    this.temperature = temperature;
    this.realFeelTemperature = realFeelTemperature;
    this.realFeelShaderTemperature = realFeelShaderTemperature;
    this.apparentTemperature = apparentTemperature;
    this.windChillTemperature = windChillTemperature;
    this.wetBulbTemperature = wetBulbTemperature;
    this.degreeDayTemperature = degreeDayTemperature;
    this.totalPrecipitation = totalPrecipitation;
    this.thunderstormPrecipitation = thunderstormPrecipitation;
    this.rainPrecipitation = rainPrecipitation;
    this.snowPrecipitation = snowPrecipitation;
    this.icePrecipitation = icePrecipitation;
    this.totalPrecipitationProbability = totalPrecipitationProbability;
    this.thunderstormPrecipitationProbability = thunderstormPrecipitationProbability;
    this.rainPrecipitationProbability = rainPrecipitationProbability;
    this.snowPrecipitationProbability = snowPrecipitationProbability;
    this.icePrecipitationProbability = icePrecipitationProbability;
    this.windDirection = windDirection;
    this.windDegree = windDegree;
    this.windSpeed = windSpeed;
    this.windLevel = windLevel;
    this.uvIndex = uvIndex;
    this.uvLevel = uvLevel;
    this.uvDescription = uvDescription;
    this.aqiText = aqiText;
    this.aqiIndex = aqiIndex;
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

@Generated(hash = 1598697471)
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

public long getTimeStamp() {
    return this.timeStamp;
}

public void setTimeStamp(long timeStamp) {
    this.timeStamp = timeStamp;
}

public Date getPublishDate() {
    return this.publishDate;
}

public void setPublishDate(Date publishDate) {
    this.publishDate = publishDate;
}

public long getPublishTime() {
    return this.publishTime;
}

public void setPublishTime(long publishTime) {
    this.publishTime = publishTime;
}

public Date getUpdateDate() {
    return this.updateDate;
}

public void setUpdateDate(Date updateDate) {
    this.updateDate = updateDate;
}

public long getUpdateTime() {
    return this.updateTime;
}

public void setUpdateTime(long updateTime) {
    this.updateTime = updateTime;
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

public int getTemperature() {
    return this.temperature;
}

public void setTemperature(int temperature) {
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

public Float getTotalPrecipitation() {
    return this.totalPrecipitation;
}

public void setTotalPrecipitation(Float totalPrecipitation) {
    this.totalPrecipitation = totalPrecipitation;
}

public Float getThunderstormPrecipitation() {
    return this.thunderstormPrecipitation;
}

public void setThunderstormPrecipitation(Float thunderstormPrecipitation) {
    this.thunderstormPrecipitation = thunderstormPrecipitation;
}

public Float getRainPrecipitation() {
    return this.rainPrecipitation;
}

public void setRainPrecipitation(Float rainPrecipitation) {
    this.rainPrecipitation = rainPrecipitation;
}

public Float getSnowPrecipitation() {
    return this.snowPrecipitation;
}

public void setSnowPrecipitation(Float snowPrecipitation) {
    this.snowPrecipitation = snowPrecipitation;
}

public Float getIcePrecipitation() {
    return this.icePrecipitation;
}

public void setIcePrecipitation(Float icePrecipitation) {
    this.icePrecipitation = icePrecipitation;
}

public Float getTotalPrecipitationProbability() {
    return this.totalPrecipitationProbability;
}

public void setTotalPrecipitationProbability(
        Float totalPrecipitationProbability) {
    this.totalPrecipitationProbability = totalPrecipitationProbability;
}

public Float getThunderstormPrecipitationProbability() {
    return this.thunderstormPrecipitationProbability;
}

public void setThunderstormPrecipitationProbability(
        Float thunderstormPrecipitationProbability) {
    this.thunderstormPrecipitationProbability = thunderstormPrecipitationProbability;
}

public Float getRainPrecipitationProbability() {
    return this.rainPrecipitationProbability;
}

public void setRainPrecipitationProbability(
        Float rainPrecipitationProbability) {
    this.rainPrecipitationProbability = rainPrecipitationProbability;
}

public Float getSnowPrecipitationProbability() {
    return this.snowPrecipitationProbability;
}

public void setSnowPrecipitationProbability(
        Float snowPrecipitationProbability) {
    this.snowPrecipitationProbability = snowPrecipitationProbability;
}

public Float getIcePrecipitationProbability() {
    return this.icePrecipitationProbability;
}

public void setIcePrecipitationProbability(Float icePrecipitationProbability) {
    this.icePrecipitationProbability = icePrecipitationProbability;
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

public String getAqiText() {
    return this.aqiText;
}

public void setAqiText(String aqiText) {
    this.aqiText = aqiText;
}

public Integer getAqiIndex() {
    return this.aqiIndex;
}

public void setAqiIndex(Integer aqiIndex) {
    this.aqiIndex = aqiIndex;
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
@Generated(hash = 693799015)
public List<DailyEntity> getDailyEntityList() {
    if (dailyEntityList == null) {
        final DaoSession daoSession = this.daoSession;
        if (daoSession == null) {
            throw new DaoException("Entity is detached from DAO context");
        }
        DailyEntityDao targetDao = daoSession.getDailyEntityDao();
        List<DailyEntity> dailyEntityListNew = targetDao
                ._queryWeatherEntity_DailyEntityList(cityId, weatherSource);
        synchronized (this) {
            if (dailyEntityList == null) {
                dailyEntityList = dailyEntityListNew;
            }
        }
    }
    return dailyEntityList;
}

/** Resets a to-many relationship, making the next get call to query for a fresh result. */
@Generated(hash = 1145290907)
public synchronized void resetDailyEntityList() {
    dailyEntityList = null;
}

/**
 * To-many relationship, resolved on first access (and after reset).
 * Changes to to-many relations are not persisted, make changes to the target entity.
 */
@Generated(hash = 92300296)
public List<HourlyEntity> getHourlyEntityList() {
    if (hourlyEntityList == null) {
        final DaoSession daoSession = this.daoSession;
        if (daoSession == null) {
            throw new DaoException("Entity is detached from DAO context");
        }
        HourlyEntityDao targetDao = daoSession.getHourlyEntityDao();
        List<HourlyEntity> hourlyEntityListNew = targetDao
                ._queryWeatherEntity_HourlyEntityList(cityId, weatherSource);
        synchronized (this) {
            if (hourlyEntityList == null) {
                hourlyEntityList = hourlyEntityListNew;
            }
        }
    }
    return hourlyEntityList;
}

/** Resets a to-many relationship, making the next get call to query for a fresh result. */
@Generated(hash = 1603476353)
public synchronized void resetHourlyEntityList() {
    hourlyEntityList = null;
}

/**
 * To-many relationship, resolved on first access (and after reset).
 * Changes to to-many relations are not persisted, make changes to the target entity.
 */
@Generated(hash = 176390241)
public List<MinutelyEntity> getMinutelyEntityList() {
    if (minutelyEntityList == null) {
        final DaoSession daoSession = this.daoSession;
        if (daoSession == null) {
            throw new DaoException("Entity is detached from DAO context");
        }
        MinutelyEntityDao targetDao = daoSession.getMinutelyEntityDao();
        List<MinutelyEntity> minutelyEntityListNew = targetDao
                ._queryWeatherEntity_MinutelyEntityList(cityId, weatherSource);
        synchronized (this) {
            if (minutelyEntityList == null) {
                minutelyEntityList = minutelyEntityListNew;
            }
        }
    }
    return minutelyEntityList;
}

/** Resets a to-many relationship, making the next get call to query for a fresh result. */
@Generated(hash = 1906829833)
public synchronized void resetMinutelyEntityList() {
    minutelyEntityList = null;
}

/**
 * To-many relationship, resolved on first access (and after reset).
 * Changes to to-many relations are not persisted, make changes to the target entity.
 */
@Generated(hash = 1421248109)
public List<AlertEntity> getAlertEntityList() {
    if (alertEntityList == null) {
        final DaoSession daoSession = this.daoSession;
        if (daoSession == null) {
            throw new DaoException("Entity is detached from DAO context");
        }
        AlertEntityDao targetDao = daoSession.getAlertEntityDao();
        List<AlertEntity> alertEntityListNew = targetDao
                ._queryWeatherEntity_AlertEntityList(cityId, weatherSource);
        synchronized (this) {
            if (alertEntityList == null) {
                alertEntityList = alertEntityListNew;
            }
        }
    }
    return alertEntityList;
}

/** Resets a to-many relationship, making the next get call to query for a fresh result. */
@Generated(hash = 2026104948)
public synchronized void resetAlertEntityList() {
    alertEntityList = null;
}

/**
 * Convenient call for {@link org.greenrobot.greendao.AbstractDao#delete(Object)}.
 * Entity must attached to an entity context.
 */
@Generated(hash = 128553479)
public void delete() {
    if (myDao == null) {
        throw new DaoException("Entity is detached from DAO context");
    }
    myDao.delete(this);
}

/**
 * Convenient call for {@link org.greenrobot.greendao.AbstractDao#refresh(Object)}.
 * Entity must attached to an entity context.
 */
@Generated(hash = 1942392019)
public void refresh() {
    if (myDao == null) {
        throw new DaoException("Entity is detached from DAO context");
    }
    myDao.refresh(this);
}

/**
 * Convenient call for {@link org.greenrobot.greendao.AbstractDao#update(Object)}.
 * Entity must attached to an entity context.
 */
@Generated(hash = 713229351)
public void update() {
    if (myDao == null) {
        throw new DaoException("Entity is detached from DAO context");
    }
    myDao.update(this);
}

/** called by internal mechanisms, do not call yourself. */
@Generated(hash = 1493194274)
public void __setDaoSession(DaoSession daoSession) {
    this.daoSession = daoSession;
    myDao = daoSession != null ? daoSession.getWeatherEntityDao() : null;
}


}
