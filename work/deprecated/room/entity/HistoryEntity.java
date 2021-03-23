package wangdaye.com.geometricweather.room.entity;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import java.util.Date;

import wangdaye.com.geometricweather.basic.models.options.provider.WeatherSource;

/**
 * History entity.
 *
 * {@link wangdaye.com.geometricweather.basic.models.weather.History}.
 * */

@Entity(tableName = "HISTORY_ENTITY")
public class HistoryEntity {

    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "_id") public Long id;
    @ColumnInfo(name = "CITY_ID") public String cityId;
    @ColumnInfo(name = "WEATHER_SOURCE") public WeatherSource weatherSource;

    @ColumnInfo(name = "DATE") public Date date;
    @ColumnInfo(name = "TIME") public long time;

    @ColumnInfo(name = "DAYTIME_TEMPERATURE") public int daytimeTemperature;
    @ColumnInfo(name = "NIGHTTIME_TEMPERATURE") public int nighttimeTemperature;
}
