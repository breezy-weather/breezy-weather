package wangdaye.com.geometricweather.room.entity;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import java.util.Date;

import wangdaye.com.geometricweather.basic.models.options.provider.WeatherSource;
import wangdaye.com.geometricweather.basic.models.weather.WeatherCode;
import wangdaye.com.geometricweather.basic.models.weather.WindDegree;

/**
 * Weather entity.
 *
 * {@link wangdaye.com.geometricweather.basic.models.weather.Weather}.
 * */
@Entity(tableName = "WEATHER_ENTITY")
public class WeatherEntity {

    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "_id") public Long id;

    // base.
    @ColumnInfo(name = "CITY_ID") public String cityId;
    @ColumnInfo(name = "WEATHER_SOURCE") public WeatherSource weatherSource;
    @ColumnInfo(name = "TIME_STAMP") public long timeStamp;
    @ColumnInfo(name = "PUBLISH_DATE") public Date publishDate;
    @ColumnInfo(name = "PUBLISH_TIME") public long publishTime;
    @ColumnInfo(name = "UPDATE_DATE") public Date updateDate;
    @ColumnInfo(name = "UPDATE_TIME") public long updateTime;

    // current.
    @ColumnInfo(name = "WEATHER_TEXT") public String weatherText;
    @ColumnInfo(name = "WEATHER_CODE") public WeatherCode weatherCode;

    @ColumnInfo(name = "TEMPERATURE") public int temperature;
    @ColumnInfo(name = "REAL_FEEL_TEMPERATURE") public Integer realFeelTemperature;
    @ColumnInfo(name = "REAL_FEEL_SHADER_TEMPERATURE") public Integer realFeelShaderTemperature;
    @ColumnInfo(name = "APPARENT_TEMPERATURE") public Integer apparentTemperature;
    @ColumnInfo(name = "WIND_CHILL_TEMPERATURE") public Integer windChillTemperature;
    @ColumnInfo(name = "WET_BULB_TEMPERATURE") public Integer wetBulbTemperature;
    @ColumnInfo(name = "DEGREE_DAY_TEMPERATURE") public Integer degreeDayTemperature;

    @ColumnInfo(name = "TOTAL_PRECIPITATION") public Float totalPrecipitation;
    @ColumnInfo(name = "THUNDERSTORM_PRECIPITATION") public Float thunderstormPrecipitation;
    @ColumnInfo(name = "RAIN_PRECIPITATION") public Float rainPrecipitation;
    @ColumnInfo(name = "SNOW_PRECIPITATION") public Float snowPrecipitation;
    @ColumnInfo(name = "ICE_PRECIPITATION") public Float icePrecipitation;

    @ColumnInfo(name = "TOTAL_PRECIPITATION_PROBABILITY") public Float totalPrecipitationProbability;
    @ColumnInfo(name = "THUNDERSTORM_PRECIPITATION_PROBABILITY") public Float thunderstormPrecipitationProbability;
    @ColumnInfo(name = "RAIN_PRECIPITATION_PROBABILITY") public Float rainPrecipitationProbability;
    @ColumnInfo(name = "SNOW_PRECIPITATION_PROBABILITY") public Float snowPrecipitationProbability;
    @ColumnInfo(name = "ICE_PRECIPITATION_PROBABILITY") public Float icePrecipitationProbability;

    @ColumnInfo(name = "WIND_DIRECTION") public String windDirection;
    @ColumnInfo(name = "WIND_DEGREE") public WindDegree windDegree;
    @ColumnInfo(name = "WIND_SPEED") public Float windSpeed;
    @ColumnInfo(name = "WIND_LEVEL") public String windLevel;

    @ColumnInfo(name = "UV_INDEX") public Integer uvIndex;
    @ColumnInfo(name = "UV_LEVEL") public String uvLevel;
    @ColumnInfo(name = "UV_DESCRIPTION") public String uvDescription;

    @ColumnInfo(name = "AQI_TEXT") public String aqiText;
    @ColumnInfo(name = "AQI_INDEX") public Integer aqiIndex;
    @ColumnInfo(name = "PM25") public Float pm25;
    @ColumnInfo(name = "PM10") public Float pm10;
    @ColumnInfo(name = "SO2") public Float so2;
    @ColumnInfo(name = "NO2") public Float no2;
    @ColumnInfo(name = "O3") public Float o3;
    @ColumnInfo(name = "CO") public Float co;

    @ColumnInfo(name = "RELATIVE_HUMIDITY") public Float relativeHumidity;
    @ColumnInfo(name = "PRESSURE") public Float pressure;
    @ColumnInfo(name = "VISIBILITY") public Float visibility;
    @ColumnInfo(name = "DEW_POINT") public Integer dewPoint;
    @ColumnInfo(name = "CLOUD_COVER") public Integer cloudCover;
    @ColumnInfo(name = "CEILING") public Float ceiling;

    @ColumnInfo(name = "DAILY_FORECAST") public String dailyForecast;
    @ColumnInfo(name = "HOURLY_FORECAST") public String hourlyForecast;
}
