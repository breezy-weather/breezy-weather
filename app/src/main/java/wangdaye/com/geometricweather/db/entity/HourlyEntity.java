package wangdaye.com.geometricweather.db.entity;

import org.greenrobot.greendao.annotation.Convert;
import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Id;

import java.util.Date;

import wangdaye.com.geometricweather.basic.model.option.provider.WeatherSource;
import wangdaye.com.geometricweather.basic.model.weather.WeatherCode;
import wangdaye.com.geometricweather.db.propertyConverter.WeatherCodeConverter;

import org.greenrobot.greendao.annotation.Generated;

/**
 * Hourly entity.
 *
 * {@link wangdaye.com.geometricweather.basic.model.weather.Hourly}.
 * */
@Entity
public class HourlyEntity {

    @Id public Long id;
    public String cityId;
    public String weatherSource;

    public Date date;
    public long time;
    public boolean daylight;

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
    @Generated(hash = 1553100243)
    public HourlyEntity(Long id, String cityId, String weatherSource, Date date,
            long time, boolean daylight, String weatherText,
            WeatherCode weatherCode, int temperature, Integer realFeelTemperature,
            Integer realFeelShaderTemperature, Integer apparentTemperature,
            Integer windChillTemperature, Integer wetBulbTemperature,
            Integer degreeDayTemperature, Float totalPrecipitation,
            Float thunderstormPrecipitation, Float rainPrecipitation,
            Float snowPrecipitation, Float icePrecipitation,
            Float totalPrecipitationProbability,
            Float thunderstormPrecipitationProbability,
            Float rainPrecipitationProbability, Float snowPrecipitationProbability,
            Float icePrecipitationProbability) {
        this.id = id;
        this.cityId = cityId;
        this.weatherSource = weatherSource;
        this.date = date;
        this.time = time;
        this.daylight = daylight;
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
    }
    @Generated(hash = 617074574)
    public HourlyEntity() {
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
    public boolean getDaylight() {
        return this.daylight;
    }
    public void setDaylight(boolean daylight) {
        this.daylight = daylight;
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

}
