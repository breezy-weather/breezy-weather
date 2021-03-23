package wangdaye.com.geometricweather.room.entity;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import java.util.Date;

import wangdaye.com.geometricweather.basic.models.options.provider.WeatherSource;
import wangdaye.com.geometricweather.basic.models.weather.WeatherCode;

/**
 * Minutely entity.
 *
 * {@link wangdaye.com.geometricweather.basic.models.weather.Minutely}.
 * */
@Entity(tableName = "MINUTELY_ENTITY")
public class MinutelyEntity {

    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "_id") public Long id;
    @ColumnInfo(name = "CITY_ID") public String cityId;
    @ColumnInfo(name = "WEATHER_SOURCE") public WeatherSource weatherSource;

    @ColumnInfo(name = "DATE") public Date date;
    @ColumnInfo(name = "TIME") public long time;
    @ColumnInfo(name = "DAYLIGHT") public boolean daylight;

    @ColumnInfo(name = "WEATHER_TEXT") public String weatherText;
    @ColumnInfo(name = "WEATHER_CODE") public WeatherCode weatherCode;

    @ColumnInfo(name = "MINUTE_INTERVAL") public int minuteInterval;
    @ColumnInfo(name = "DBZ") public Integer dbz;
    @ColumnInfo(name = "CLOUD_COVER") public Integer cloudCover;
}
