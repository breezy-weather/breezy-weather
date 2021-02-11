package wangdaye.com.geometricweather.room.entity;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import java.util.Date;

import wangdaye.com.geometricweather.basic.models.options.provider.WeatherSource;
import wangdaye.com.geometricweather.basic.models.weather.WeatherCode;

/**
 * Hourly entity.
 *
 * {@link wangdaye.com.geometricweather.basic.models.weather.Hourly}.
 * */
@Entity(tableName = "HOURLY_ENTITY")
public class HourlyEntity {

    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "_id") public Long id;
    @ColumnInfo(name = "CITY_ID") public String cityId;
    @ColumnInfo(name = "WEATHER_SOURCE") public WeatherSource weatherSource;

    @ColumnInfo(name = "DATE") public Date date;
    @ColumnInfo(name = "TIME") public long time;
    @ColumnInfo(name = "DAYLIGHT") public boolean daylight;

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
}
