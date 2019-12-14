package wangdaye.com.geometricweather.db.entity;

import org.greenrobot.greendao.annotation.Convert;
import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Id;

import java.util.Date;

import wangdaye.com.geometricweather.basic.model.option.provider.WeatherSource;
import wangdaye.com.geometricweather.basic.model.weather.WeatherCode;
import wangdaye.com.geometricweather.basic.model.weather.WindDegree;
import wangdaye.com.geometricweather.db.propertyConverter.WeatherCodeConverter;
import wangdaye.com.geometricweather.db.propertyConverter.WindDegreeConverter;

import org.greenrobot.greendao.annotation.Generated;

/**
 * Daily entity.
 *
 * {@link wangdaye.com.geometricweather.basic.model.weather.Daily}.
 * */
@Entity
public class DailyEntity {

    @Id public Long id;
    public String cityId;
    public String weatherSource;

    public Date date;
    public long time;

    // daytime.
    public String daytimeWeatherText;
    public String daytimeWeatherPhase;
    @Convert(converter = WeatherCodeConverter.class, columnType = String.class)
    public WeatherCode daytimeWeatherCode;

    public int daytimeTemperature;
    public Integer daytimeRealFeelTemperature;
    public Integer daytimeRealFeelShaderTemperature;
    public Integer daytimeApparentTemperature;
    public Integer daytimeWindChillTemperature;
    public Integer daytimeWetBulbTemperature;
    public Integer daytimeDegreeDayTemperature;

    public Float daytimeTotalPrecipitation;
    public Float daytimeThunderstormPrecipitation;
    public Float daytimeRainPrecipitation;
    public Float daytimeSnowPrecipitation;
    public Float daytimeIcePrecipitation;

    public Float daytimeTotalPrecipitationProbability;
    public Float daytimeThunderstormPrecipitationProbability;
    public Float daytimeRainPrecipitationProbability;
    public Float daytimeSnowPrecipitationProbability;
    public Float daytimeIcePrecipitationProbability;

    public Float daytimeTotalPrecipitationDuration;
    public Float daytimeThunderstormPrecipitationDuration;
    public Float daytimeRainPrecipitationDuration;
    public Float daytimeSnowPrecipitationDuration;
    public Float daytimeIcePrecipitationDuration;

    public String daytimeWindDirection;
    @Convert(converter = WindDegreeConverter.class, columnType = Float.class)
    public WindDegree daytimeWindDegree;
    public Float daytimeWindSpeed;
    public String daytimeWindLevel;

    public Integer daytimeCloudCover;

    // nighttime.
    public String nighttimeWeatherText;
    public String nighttimeWeatherPhase;
    @Convert(converter = WeatherCodeConverter.class, columnType = String.class)
    public WeatherCode nighttimeWeatherCode;

    public int nighttimeTemperature;
    public Integer nighttimeRealFeelTemperature;
    public Integer nighttimeRealFeelShaderTemperature;
    public Integer nighttimeApparentTemperature;
    public Integer nighttimeWindChillTemperature;
    public Integer nighttimeWetBulbTemperature;
    public Integer nighttimeDegreeDayTemperature;

    public Float nighttimeTotalPrecipitation;
    public Float nighttimeThunderstormPrecipitation;
    public Float nighttimeRainPrecipitation;
    public Float nighttimeSnowPrecipitation;
    public Float nighttimeIcePrecipitation;

    public Float nighttimeTotalPrecipitationProbability;
    public Float nighttimeThunderstormPrecipitationProbability;
    public Float nighttimeRainPrecipitationProbability;
    public Float nighttimeSnowPrecipitationProbability;
    public Float nighttimeIcePrecipitationProbability;

    public Float nighttimeTotalPrecipitationDuration;
    public Float nighttimeThunderstormPrecipitationDuration;
    public Float nighttimeRainPrecipitationDuration;
    public Float nighttimeSnowPrecipitationDuration;
    public Float nighttimeIcePrecipitationDuration;

    public String nighttimeWindDirection;
    @Convert(converter = WindDegreeConverter.class, columnType = Float.class)
    public WindDegree nighttimeWindDegree;
    public Float nighttimeWindSpeed;
    public String nighttimeWindLevel;

    public Integer nighttimeCloudCover;

    // sun.
    public Date sunRiseDate;
    public Date sunSetDate;

    // moon.
    public Date moonRiseDate;
    public Date moonSetDate;

    // moon phase.
    public Integer moonPhaseAngle;
    public String moonPhaseDescription;

    // aqi.
    public String aqiText;
    public Integer aqiIndex;
    public Float pm25;
    public Float pm10;
    public Float so2;
    public Float no2;
    public Float o3;
    public Float co;

    // pollen.
    public Integer grassIndex;
    public Integer grassLevel;
    public String grassDescription;
    public Integer moldIndex;
    public Integer moldLevel;
    public String moldDescription;
    public Integer ragweedIndex;
    public Integer ragweedLevel;
    public String ragweedDescription;
    public Integer treeIndex;
    public Integer treeLevel;
    public String treeDescription;

    // uv.
    public Integer uvIndex;
    public String uvLevel;
    public String uvDescription;

    public float hoursOfSun;

    @Generated(hash = 1727023126)
    public DailyEntity(Long id, String cityId, String weatherSource, Date date,
            long time, String daytimeWeatherText, String daytimeWeatherPhase,
            WeatherCode daytimeWeatherCode, int daytimeTemperature,
            Integer daytimeRealFeelTemperature,
            Integer daytimeRealFeelShaderTemperature,
            Integer daytimeApparentTemperature, Integer daytimeWindChillTemperature,
            Integer daytimeWetBulbTemperature, Integer daytimeDegreeDayTemperature,
            Float daytimeTotalPrecipitation, Float daytimeThunderstormPrecipitation,
            Float daytimeRainPrecipitation, Float daytimeSnowPrecipitation,
            Float daytimeIcePrecipitation,
            Float daytimeTotalPrecipitationProbability,
            Float daytimeThunderstormPrecipitationProbability,
            Float daytimeRainPrecipitationProbability,
            Float daytimeSnowPrecipitationProbability,
            Float daytimeIcePrecipitationProbability,
            Float daytimeTotalPrecipitationDuration,
            Float daytimeThunderstormPrecipitationDuration,
            Float daytimeRainPrecipitationDuration,
            Float daytimeSnowPrecipitationDuration,
            Float daytimeIcePrecipitationDuration, String daytimeWindDirection,
            WindDegree daytimeWindDegree, Float daytimeWindSpeed,
            String daytimeWindLevel, Integer daytimeCloudCover,
            String nighttimeWeatherText, String nighttimeWeatherPhase,
            WeatherCode nighttimeWeatherCode, int nighttimeTemperature,
            Integer nighttimeRealFeelTemperature,
            Integer nighttimeRealFeelShaderTemperature,
            Integer nighttimeApparentTemperature,
            Integer nighttimeWindChillTemperature,
            Integer nighttimeWetBulbTemperature,
            Integer nighttimeDegreeDayTemperature,
            Float nighttimeTotalPrecipitation,
            Float nighttimeThunderstormPrecipitation,
            Float nighttimeRainPrecipitation, Float nighttimeSnowPrecipitation,
            Float nighttimeIcePrecipitation,
            Float nighttimeTotalPrecipitationProbability,
            Float nighttimeThunderstormPrecipitationProbability,
            Float nighttimeRainPrecipitationProbability,
            Float nighttimeSnowPrecipitationProbability,
            Float nighttimeIcePrecipitationProbability,
            Float nighttimeTotalPrecipitationDuration,
            Float nighttimeThunderstormPrecipitationDuration,
            Float nighttimeRainPrecipitationDuration,
            Float nighttimeSnowPrecipitationDuration,
            Float nighttimeIcePrecipitationDuration, String nighttimeWindDirection,
            WindDegree nighttimeWindDegree, Float nighttimeWindSpeed,
            String nighttimeWindLevel, Integer nighttimeCloudCover,
            Date sunRiseDate, Date sunSetDate, Date moonRiseDate, Date moonSetDate,
            Integer moonPhaseAngle, String moonPhaseDescription, String aqiText,
            Integer aqiIndex, Float pm25, Float pm10, Float so2, Float no2,
            Float o3, Float co, Integer grassIndex, Integer grassLevel,
            String grassDescription, Integer moldIndex, Integer moldLevel,
            String moldDescription, Integer ragweedIndex, Integer ragweedLevel,
            String ragweedDescription, Integer treeIndex, Integer treeLevel,
            String treeDescription, Integer uvIndex, String uvLevel,
            String uvDescription, float hoursOfSun) {
        this.id = id;
        this.cityId = cityId;
        this.weatherSource = weatherSource;
        this.date = date;
        this.time = time;
        this.daytimeWeatherText = daytimeWeatherText;
        this.daytimeWeatherPhase = daytimeWeatherPhase;
        this.daytimeWeatherCode = daytimeWeatherCode;
        this.daytimeTemperature = daytimeTemperature;
        this.daytimeRealFeelTemperature = daytimeRealFeelTemperature;
        this.daytimeRealFeelShaderTemperature = daytimeRealFeelShaderTemperature;
        this.daytimeApparentTemperature = daytimeApparentTemperature;
        this.daytimeWindChillTemperature = daytimeWindChillTemperature;
        this.daytimeWetBulbTemperature = daytimeWetBulbTemperature;
        this.daytimeDegreeDayTemperature = daytimeDegreeDayTemperature;
        this.daytimeTotalPrecipitation = daytimeTotalPrecipitation;
        this.daytimeThunderstormPrecipitation = daytimeThunderstormPrecipitation;
        this.daytimeRainPrecipitation = daytimeRainPrecipitation;
        this.daytimeSnowPrecipitation = daytimeSnowPrecipitation;
        this.daytimeIcePrecipitation = daytimeIcePrecipitation;
        this.daytimeTotalPrecipitationProbability = daytimeTotalPrecipitationProbability;
        this.daytimeThunderstormPrecipitationProbability = daytimeThunderstormPrecipitationProbability;
        this.daytimeRainPrecipitationProbability = daytimeRainPrecipitationProbability;
        this.daytimeSnowPrecipitationProbability = daytimeSnowPrecipitationProbability;
        this.daytimeIcePrecipitationProbability = daytimeIcePrecipitationProbability;
        this.daytimeTotalPrecipitationDuration = daytimeTotalPrecipitationDuration;
        this.daytimeThunderstormPrecipitationDuration = daytimeThunderstormPrecipitationDuration;
        this.daytimeRainPrecipitationDuration = daytimeRainPrecipitationDuration;
        this.daytimeSnowPrecipitationDuration = daytimeSnowPrecipitationDuration;
        this.daytimeIcePrecipitationDuration = daytimeIcePrecipitationDuration;
        this.daytimeWindDirection = daytimeWindDirection;
        this.daytimeWindDegree = daytimeWindDegree;
        this.daytimeWindSpeed = daytimeWindSpeed;
        this.daytimeWindLevel = daytimeWindLevel;
        this.daytimeCloudCover = daytimeCloudCover;
        this.nighttimeWeatherText = nighttimeWeatherText;
        this.nighttimeWeatherPhase = nighttimeWeatherPhase;
        this.nighttimeWeatherCode = nighttimeWeatherCode;
        this.nighttimeTemperature = nighttimeTemperature;
        this.nighttimeRealFeelTemperature = nighttimeRealFeelTemperature;
        this.nighttimeRealFeelShaderTemperature = nighttimeRealFeelShaderTemperature;
        this.nighttimeApparentTemperature = nighttimeApparentTemperature;
        this.nighttimeWindChillTemperature = nighttimeWindChillTemperature;
        this.nighttimeWetBulbTemperature = nighttimeWetBulbTemperature;
        this.nighttimeDegreeDayTemperature = nighttimeDegreeDayTemperature;
        this.nighttimeTotalPrecipitation = nighttimeTotalPrecipitation;
        this.nighttimeThunderstormPrecipitation = nighttimeThunderstormPrecipitation;
        this.nighttimeRainPrecipitation = nighttimeRainPrecipitation;
        this.nighttimeSnowPrecipitation = nighttimeSnowPrecipitation;
        this.nighttimeIcePrecipitation = nighttimeIcePrecipitation;
        this.nighttimeTotalPrecipitationProbability = nighttimeTotalPrecipitationProbability;
        this.nighttimeThunderstormPrecipitationProbability = nighttimeThunderstormPrecipitationProbability;
        this.nighttimeRainPrecipitationProbability = nighttimeRainPrecipitationProbability;
        this.nighttimeSnowPrecipitationProbability = nighttimeSnowPrecipitationProbability;
        this.nighttimeIcePrecipitationProbability = nighttimeIcePrecipitationProbability;
        this.nighttimeTotalPrecipitationDuration = nighttimeTotalPrecipitationDuration;
        this.nighttimeThunderstormPrecipitationDuration = nighttimeThunderstormPrecipitationDuration;
        this.nighttimeRainPrecipitationDuration = nighttimeRainPrecipitationDuration;
        this.nighttimeSnowPrecipitationDuration = nighttimeSnowPrecipitationDuration;
        this.nighttimeIcePrecipitationDuration = nighttimeIcePrecipitationDuration;
        this.nighttimeWindDirection = nighttimeWindDirection;
        this.nighttimeWindDegree = nighttimeWindDegree;
        this.nighttimeWindSpeed = nighttimeWindSpeed;
        this.nighttimeWindLevel = nighttimeWindLevel;
        this.nighttimeCloudCover = nighttimeCloudCover;
        this.sunRiseDate = sunRiseDate;
        this.sunSetDate = sunSetDate;
        this.moonRiseDate = moonRiseDate;
        this.moonSetDate = moonSetDate;
        this.moonPhaseAngle = moonPhaseAngle;
        this.moonPhaseDescription = moonPhaseDescription;
        this.aqiText = aqiText;
        this.aqiIndex = aqiIndex;
        this.pm25 = pm25;
        this.pm10 = pm10;
        this.so2 = so2;
        this.no2 = no2;
        this.o3 = o3;
        this.co = co;
        this.grassIndex = grassIndex;
        this.grassLevel = grassLevel;
        this.grassDescription = grassDescription;
        this.moldIndex = moldIndex;
        this.moldLevel = moldLevel;
        this.moldDescription = moldDescription;
        this.ragweedIndex = ragweedIndex;
        this.ragweedLevel = ragweedLevel;
        this.ragweedDescription = ragweedDescription;
        this.treeIndex = treeIndex;
        this.treeLevel = treeLevel;
        this.treeDescription = treeDescription;
        this.uvIndex = uvIndex;
        this.uvLevel = uvLevel;
        this.uvDescription = uvDescription;
        this.hoursOfSun = hoursOfSun;
    }

    @Generated(hash = 1809948821)
    public DailyEntity() {
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

    public String getDaytimeWeatherText() {
        return this.daytimeWeatherText;
    }

    public void setDaytimeWeatherText(String daytimeWeatherText) {
        this.daytimeWeatherText = daytimeWeatherText;
    }

    public String getDaytimeWeatherPhase() {
        return this.daytimeWeatherPhase;
    }

    public void setDaytimeWeatherPhase(String daytimeWeatherPhase) {
        this.daytimeWeatherPhase = daytimeWeatherPhase;
    }

    public WeatherCode getDaytimeWeatherCode() {
        return this.daytimeWeatherCode;
    }

    public void setDaytimeWeatherCode(WeatherCode daytimeWeatherCode) {
        this.daytimeWeatherCode = daytimeWeatherCode;
    }

    public int getDaytimeTemperature() {
        return this.daytimeTemperature;
    }

    public void setDaytimeTemperature(int daytimeTemperature) {
        this.daytimeTemperature = daytimeTemperature;
    }

    public Integer getDaytimeRealFeelTemperature() {
        return this.daytimeRealFeelTemperature;
    }

    public void setDaytimeRealFeelTemperature(Integer daytimeRealFeelTemperature) {
        this.daytimeRealFeelTemperature = daytimeRealFeelTemperature;
    }

    public Integer getDaytimeRealFeelShaderTemperature() {
        return this.daytimeRealFeelShaderTemperature;
    }

    public void setDaytimeRealFeelShaderTemperature(
            Integer daytimeRealFeelShaderTemperature) {
        this.daytimeRealFeelShaderTemperature = daytimeRealFeelShaderTemperature;
    }

    public Integer getDaytimeApparentTemperature() {
        return this.daytimeApparentTemperature;
    }

    public void setDaytimeApparentTemperature(Integer daytimeApparentTemperature) {
        this.daytimeApparentTemperature = daytimeApparentTemperature;
    }

    public Integer getDaytimeWindChillTemperature() {
        return this.daytimeWindChillTemperature;
    }

    public void setDaytimeWindChillTemperature(
            Integer daytimeWindChillTemperature) {
        this.daytimeWindChillTemperature = daytimeWindChillTemperature;
    }

    public Integer getDaytimeWetBulbTemperature() {
        return this.daytimeWetBulbTemperature;
    }

    public void setDaytimeWetBulbTemperature(Integer daytimeWetBulbTemperature) {
        this.daytimeWetBulbTemperature = daytimeWetBulbTemperature;
    }

    public Integer getDaytimeDegreeDayTemperature() {
        return this.daytimeDegreeDayTemperature;
    }

    public void setDaytimeDegreeDayTemperature(
            Integer daytimeDegreeDayTemperature) {
        this.daytimeDegreeDayTemperature = daytimeDegreeDayTemperature;
    }

    public Float getDaytimeTotalPrecipitation() {
        return this.daytimeTotalPrecipitation;
    }

    public void setDaytimeTotalPrecipitation(Float daytimeTotalPrecipitation) {
        this.daytimeTotalPrecipitation = daytimeTotalPrecipitation;
    }

    public Float getDaytimeThunderstormPrecipitation() {
        return this.daytimeThunderstormPrecipitation;
    }

    public void setDaytimeThunderstormPrecipitation(
            Float daytimeThunderstormPrecipitation) {
        this.daytimeThunderstormPrecipitation = daytimeThunderstormPrecipitation;
    }

    public Float getDaytimeRainPrecipitation() {
        return this.daytimeRainPrecipitation;
    }

    public void setDaytimeRainPrecipitation(Float daytimeRainPrecipitation) {
        this.daytimeRainPrecipitation = daytimeRainPrecipitation;
    }

    public Float getDaytimeSnowPrecipitation() {
        return this.daytimeSnowPrecipitation;
    }

    public void setDaytimeSnowPrecipitation(Float daytimeSnowPrecipitation) {
        this.daytimeSnowPrecipitation = daytimeSnowPrecipitation;
    }

    public Float getDaytimeIcePrecipitation() {
        return this.daytimeIcePrecipitation;
    }

    public void setDaytimeIcePrecipitation(Float daytimeIcePrecipitation) {
        this.daytimeIcePrecipitation = daytimeIcePrecipitation;
    }

    public Float getDaytimeTotalPrecipitationProbability() {
        return this.daytimeTotalPrecipitationProbability;
    }

    public void setDaytimeTotalPrecipitationProbability(
            Float daytimeTotalPrecipitationProbability) {
        this.daytimeTotalPrecipitationProbability = daytimeTotalPrecipitationProbability;
    }

    public Float getDaytimeThunderstormPrecipitationProbability() {
        return this.daytimeThunderstormPrecipitationProbability;
    }

    public void setDaytimeThunderstormPrecipitationProbability(
            Float daytimeThunderstormPrecipitationProbability) {
        this.daytimeThunderstormPrecipitationProbability = daytimeThunderstormPrecipitationProbability;
    }

    public Float getDaytimeRainPrecipitationProbability() {
        return this.daytimeRainPrecipitationProbability;
    }

    public void setDaytimeRainPrecipitationProbability(
            Float daytimeRainPrecipitationProbability) {
        this.daytimeRainPrecipitationProbability = daytimeRainPrecipitationProbability;
    }

    public Float getDaytimeSnowPrecipitationProbability() {
        return this.daytimeSnowPrecipitationProbability;
    }

    public void setDaytimeSnowPrecipitationProbability(
            Float daytimeSnowPrecipitationProbability) {
        this.daytimeSnowPrecipitationProbability = daytimeSnowPrecipitationProbability;
    }

    public Float getDaytimeIcePrecipitationProbability() {
        return this.daytimeIcePrecipitationProbability;
    }

    public void setDaytimeIcePrecipitationProbability(
            Float daytimeIcePrecipitationProbability) {
        this.daytimeIcePrecipitationProbability = daytimeIcePrecipitationProbability;
    }

    public Float getDaytimeTotalPrecipitationDuration() {
        return this.daytimeTotalPrecipitationDuration;
    }

    public void setDaytimeTotalPrecipitationDuration(
            Float daytimeTotalPrecipitationDuration) {
        this.daytimeTotalPrecipitationDuration = daytimeTotalPrecipitationDuration;
    }

    public Float getDaytimeThunderstormPrecipitationDuration() {
        return this.daytimeThunderstormPrecipitationDuration;
    }

    public void setDaytimeThunderstormPrecipitationDuration(
            Float daytimeThunderstormPrecipitationDuration) {
        this.daytimeThunderstormPrecipitationDuration = daytimeThunderstormPrecipitationDuration;
    }

    public Float getDaytimeRainPrecipitationDuration() {
        return this.daytimeRainPrecipitationDuration;
    }

    public void setDaytimeRainPrecipitationDuration(
            Float daytimeRainPrecipitationDuration) {
        this.daytimeRainPrecipitationDuration = daytimeRainPrecipitationDuration;
    }

    public Float getDaytimeSnowPrecipitationDuration() {
        return this.daytimeSnowPrecipitationDuration;
    }

    public void setDaytimeSnowPrecipitationDuration(
            Float daytimeSnowPrecipitationDuration) {
        this.daytimeSnowPrecipitationDuration = daytimeSnowPrecipitationDuration;
    }

    public Float getDaytimeIcePrecipitationDuration() {
        return this.daytimeIcePrecipitationDuration;
    }

    public void setDaytimeIcePrecipitationDuration(
            Float daytimeIcePrecipitationDuration) {
        this.daytimeIcePrecipitationDuration = daytimeIcePrecipitationDuration;
    }

    public String getDaytimeWindDirection() {
        return this.daytimeWindDirection;
    }

    public void setDaytimeWindDirection(String daytimeWindDirection) {
        this.daytimeWindDirection = daytimeWindDirection;
    }

    public WindDegree getDaytimeWindDegree() {
        return this.daytimeWindDegree;
    }

    public void setDaytimeWindDegree(WindDegree daytimeWindDegree) {
        this.daytimeWindDegree = daytimeWindDegree;
    }

    public Float getDaytimeWindSpeed() {
        return this.daytimeWindSpeed;
    }

    public void setDaytimeWindSpeed(Float daytimeWindSpeed) {
        this.daytimeWindSpeed = daytimeWindSpeed;
    }

    public String getDaytimeWindLevel() {
        return this.daytimeWindLevel;
    }

    public void setDaytimeWindLevel(String daytimeWindLevel) {
        this.daytimeWindLevel = daytimeWindLevel;
    }

    public Integer getDaytimeCloudCover() {
        return this.daytimeCloudCover;
    }

    public void setDaytimeCloudCover(Integer daytimeCloudCover) {
        this.daytimeCloudCover = daytimeCloudCover;
    }

    public String getNighttimeWeatherText() {
        return this.nighttimeWeatherText;
    }

    public void setNighttimeWeatherText(String nighttimeWeatherText) {
        this.nighttimeWeatherText = nighttimeWeatherText;
    }

    public String getNighttimeWeatherPhase() {
        return this.nighttimeWeatherPhase;
    }

    public void setNighttimeWeatherPhase(String nighttimeWeatherPhase) {
        this.nighttimeWeatherPhase = nighttimeWeatherPhase;
    }

    public WeatherCode getNighttimeWeatherCode() {
        return this.nighttimeWeatherCode;
    }

    public void setNighttimeWeatherCode(WeatherCode nighttimeWeatherCode) {
        this.nighttimeWeatherCode = nighttimeWeatherCode;
    }

    public int getNighttimeTemperature() {
        return this.nighttimeTemperature;
    }

    public void setNighttimeTemperature(int nighttimeTemperature) {
        this.nighttimeTemperature = nighttimeTemperature;
    }

    public Integer getNighttimeRealFeelTemperature() {
        return this.nighttimeRealFeelTemperature;
    }

    public void setNighttimeRealFeelTemperature(
            Integer nighttimeRealFeelTemperature) {
        this.nighttimeRealFeelTemperature = nighttimeRealFeelTemperature;
    }

    public Integer getNighttimeRealFeelShaderTemperature() {
        return this.nighttimeRealFeelShaderTemperature;
    }

    public void setNighttimeRealFeelShaderTemperature(
            Integer nighttimeRealFeelShaderTemperature) {
        this.nighttimeRealFeelShaderTemperature = nighttimeRealFeelShaderTemperature;
    }

    public Integer getNighttimeApparentTemperature() {
        return this.nighttimeApparentTemperature;
    }

    public void setNighttimeApparentTemperature(
            Integer nighttimeApparentTemperature) {
        this.nighttimeApparentTemperature = nighttimeApparentTemperature;
    }

    public Integer getNighttimeWindChillTemperature() {
        return this.nighttimeWindChillTemperature;
    }

    public void setNighttimeWindChillTemperature(
            Integer nighttimeWindChillTemperature) {
        this.nighttimeWindChillTemperature = nighttimeWindChillTemperature;
    }

    public Integer getNighttimeWetBulbTemperature() {
        return this.nighttimeWetBulbTemperature;
    }

    public void setNighttimeWetBulbTemperature(
            Integer nighttimeWetBulbTemperature) {
        this.nighttimeWetBulbTemperature = nighttimeWetBulbTemperature;
    }

    public Integer getNighttimeDegreeDayTemperature() {
        return this.nighttimeDegreeDayTemperature;
    }

    public void setNighttimeDegreeDayTemperature(
            Integer nighttimeDegreeDayTemperature) {
        this.nighttimeDegreeDayTemperature = nighttimeDegreeDayTemperature;
    }

    public Float getNighttimeTotalPrecipitation() {
        return this.nighttimeTotalPrecipitation;
    }

    public void setNighttimeTotalPrecipitation(Float nighttimeTotalPrecipitation) {
        this.nighttimeTotalPrecipitation = nighttimeTotalPrecipitation;
    }

    public Float getNighttimeThunderstormPrecipitation() {
        return this.nighttimeThunderstormPrecipitation;
    }

    public void setNighttimeThunderstormPrecipitation(
            Float nighttimeThunderstormPrecipitation) {
        this.nighttimeThunderstormPrecipitation = nighttimeThunderstormPrecipitation;
    }

    public Float getNighttimeRainPrecipitation() {
        return this.nighttimeRainPrecipitation;
    }

    public void setNighttimeRainPrecipitation(Float nighttimeRainPrecipitation) {
        this.nighttimeRainPrecipitation = nighttimeRainPrecipitation;
    }

    public Float getNighttimeSnowPrecipitation() {
        return this.nighttimeSnowPrecipitation;
    }

    public void setNighttimeSnowPrecipitation(Float nighttimeSnowPrecipitation) {
        this.nighttimeSnowPrecipitation = nighttimeSnowPrecipitation;
    }

    public Float getNighttimeIcePrecipitation() {
        return this.nighttimeIcePrecipitation;
    }

    public void setNighttimeIcePrecipitation(Float nighttimeIcePrecipitation) {
        this.nighttimeIcePrecipitation = nighttimeIcePrecipitation;
    }

    public Float getNighttimeTotalPrecipitationProbability() {
        return this.nighttimeTotalPrecipitationProbability;
    }

    public void setNighttimeTotalPrecipitationProbability(
            Float nighttimeTotalPrecipitationProbability) {
        this.nighttimeTotalPrecipitationProbability = nighttimeTotalPrecipitationProbability;
    }

    public Float getNighttimeThunderstormPrecipitationProbability() {
        return this.nighttimeThunderstormPrecipitationProbability;
    }

    public void setNighttimeThunderstormPrecipitationProbability(
            Float nighttimeThunderstormPrecipitationProbability) {
        this.nighttimeThunderstormPrecipitationProbability = nighttimeThunderstormPrecipitationProbability;
    }

    public Float getNighttimeRainPrecipitationProbability() {
        return this.nighttimeRainPrecipitationProbability;
    }

    public void setNighttimeRainPrecipitationProbability(
            Float nighttimeRainPrecipitationProbability) {
        this.nighttimeRainPrecipitationProbability = nighttimeRainPrecipitationProbability;
    }

    public Float getNighttimeSnowPrecipitationProbability() {
        return this.nighttimeSnowPrecipitationProbability;
    }

    public void setNighttimeSnowPrecipitationProbability(
            Float nighttimeSnowPrecipitationProbability) {
        this.nighttimeSnowPrecipitationProbability = nighttimeSnowPrecipitationProbability;
    }

    public Float getNighttimeIcePrecipitationProbability() {
        return this.nighttimeIcePrecipitationProbability;
    }

    public void setNighttimeIcePrecipitationProbability(
            Float nighttimeIcePrecipitationProbability) {
        this.nighttimeIcePrecipitationProbability = nighttimeIcePrecipitationProbability;
    }

    public Float getNighttimeTotalPrecipitationDuration() {
        return this.nighttimeTotalPrecipitationDuration;
    }

    public void setNighttimeTotalPrecipitationDuration(
            Float nighttimeTotalPrecipitationDuration) {
        this.nighttimeTotalPrecipitationDuration = nighttimeTotalPrecipitationDuration;
    }

    public Float getNighttimeThunderstormPrecipitationDuration() {
        return this.nighttimeThunderstormPrecipitationDuration;
    }

    public void setNighttimeThunderstormPrecipitationDuration(
            Float nighttimeThunderstormPrecipitationDuration) {
        this.nighttimeThunderstormPrecipitationDuration = nighttimeThunderstormPrecipitationDuration;
    }

    public Float getNighttimeRainPrecipitationDuration() {
        return this.nighttimeRainPrecipitationDuration;
    }

    public void setNighttimeRainPrecipitationDuration(
            Float nighttimeRainPrecipitationDuration) {
        this.nighttimeRainPrecipitationDuration = nighttimeRainPrecipitationDuration;
    }

    public Float getNighttimeSnowPrecipitationDuration() {
        return this.nighttimeSnowPrecipitationDuration;
    }

    public void setNighttimeSnowPrecipitationDuration(
            Float nighttimeSnowPrecipitationDuration) {
        this.nighttimeSnowPrecipitationDuration = nighttimeSnowPrecipitationDuration;
    }

    public Float getNighttimeIcePrecipitationDuration() {
        return this.nighttimeIcePrecipitationDuration;
    }

    public void setNighttimeIcePrecipitationDuration(
            Float nighttimeIcePrecipitationDuration) {
        this.nighttimeIcePrecipitationDuration = nighttimeIcePrecipitationDuration;
    }

    public String getNighttimeWindDirection() {
        return this.nighttimeWindDirection;
    }

    public void setNighttimeWindDirection(String nighttimeWindDirection) {
        this.nighttimeWindDirection = nighttimeWindDirection;
    }

    public WindDegree getNighttimeWindDegree() {
        return this.nighttimeWindDegree;
    }

    public void setNighttimeWindDegree(WindDegree nighttimeWindDegree) {
        this.nighttimeWindDegree = nighttimeWindDegree;
    }

    public Float getNighttimeWindSpeed() {
        return this.nighttimeWindSpeed;
    }

    public void setNighttimeWindSpeed(Float nighttimeWindSpeed) {
        this.nighttimeWindSpeed = nighttimeWindSpeed;
    }

    public String getNighttimeWindLevel() {
        return this.nighttimeWindLevel;
    }

    public void setNighttimeWindLevel(String nighttimeWindLevel) {
        this.nighttimeWindLevel = nighttimeWindLevel;
    }

    public Integer getNighttimeCloudCover() {
        return this.nighttimeCloudCover;
    }

    public void setNighttimeCloudCover(Integer nighttimeCloudCover) {
        this.nighttimeCloudCover = nighttimeCloudCover;
    }

    public Date getSunRiseDate() {
        return this.sunRiseDate;
    }

    public void setSunRiseDate(Date sunRiseDate) {
        this.sunRiseDate = sunRiseDate;
    }

    public Date getSunSetDate() {
        return this.sunSetDate;
    }

    public void setSunSetDate(Date sunSetDate) {
        this.sunSetDate = sunSetDate;
    }

    public Date getMoonRiseDate() {
        return this.moonRiseDate;
    }

    public void setMoonRiseDate(Date moonRiseDate) {
        this.moonRiseDate = moonRiseDate;
    }

    public Date getMoonSetDate() {
        return this.moonSetDate;
    }

    public void setMoonSetDate(Date moonSetDate) {
        this.moonSetDate = moonSetDate;
    }

    public Integer getMoonPhaseAngle() {
        return this.moonPhaseAngle;
    }

    public void setMoonPhaseAngle(Integer moonPhaseAngle) {
        this.moonPhaseAngle = moonPhaseAngle;
    }

    public String getMoonPhaseDescription() {
        return this.moonPhaseDescription;
    }

    public void setMoonPhaseDescription(String moonPhaseDescription) {
        this.moonPhaseDescription = moonPhaseDescription;
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

    public Integer getGrassIndex() {
        return this.grassIndex;
    }

    public void setGrassIndex(Integer grassIndex) {
        this.grassIndex = grassIndex;
    }

    public Integer getGrassLevel() {
        return this.grassLevel;
    }

    public void setGrassLevel(Integer grassLevel) {
        this.grassLevel = grassLevel;
    }

    public String getGrassDescription() {
        return this.grassDescription;
    }

    public void setGrassDescription(String grassDescription) {
        this.grassDescription = grassDescription;
    }

    public Integer getMoldIndex() {
        return this.moldIndex;
    }

    public void setMoldIndex(Integer moldIndex) {
        this.moldIndex = moldIndex;
    }

    public Integer getMoldLevel() {
        return this.moldLevel;
    }

    public void setMoldLevel(Integer moldLevel) {
        this.moldLevel = moldLevel;
    }

    public String getMoldDescription() {
        return this.moldDescription;
    }

    public void setMoldDescription(String moldDescription) {
        this.moldDescription = moldDescription;
    }

    public Integer getRagweedIndex() {
        return this.ragweedIndex;
    }

    public void setRagweedIndex(Integer ragweedIndex) {
        this.ragweedIndex = ragweedIndex;
    }

    public Integer getRagweedLevel() {
        return this.ragweedLevel;
    }

    public void setRagweedLevel(Integer ragweedLevel) {
        this.ragweedLevel = ragweedLevel;
    }

    public String getRagweedDescription() {
        return this.ragweedDescription;
    }

    public void setRagweedDescription(String ragweedDescription) {
        this.ragweedDescription = ragweedDescription;
    }

    public Integer getTreeIndex() {
        return this.treeIndex;
    }

    public void setTreeIndex(Integer treeIndex) {
        this.treeIndex = treeIndex;
    }

    public Integer getTreeLevel() {
        return this.treeLevel;
    }

    public void setTreeLevel(Integer treeLevel) {
        this.treeLevel = treeLevel;
    }

    public String getTreeDescription() {
        return this.treeDescription;
    }

    public void setTreeDescription(String treeDescription) {
        this.treeDescription = treeDescription;
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

    public float getHoursOfSun() {
        return this.hoursOfSun;
    }

    public void setHoursOfSun(float hoursOfSun) {
        this.hoursOfSun = hoursOfSun;
    }

}
