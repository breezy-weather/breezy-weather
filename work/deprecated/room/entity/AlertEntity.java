package wangdaye.com.geometricweather.room.entity;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import java.util.Date;

import wangdaye.com.geometricweather.basic.model.option.provider.WeatherSource;

/**
 * Alert entity.
 *
 * {@link wangdaye.com.geometricweather.basic.model.weather.Alert}
 * */
@Entity(tableName = "ALERT_ENTITY")
public class AlertEntity {

    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "_id") public Long id;
    @ColumnInfo(name = "CITY_ID") public String cityId;
    @ColumnInfo(name = "WEATHER_SOURCE") public WeatherSource weatherSource;

    @ColumnInfo(name = "ALERT_ID") public long alertId;
    @ColumnInfo(name = "DATE") public Date date;
    @ColumnInfo(name = "TIME") public long time;

    @ColumnInfo(name = "DESCRIPTION") public String description;
    @ColumnInfo(name = "CONTENT") public String content;

    @ColumnInfo(name = "TYPE") public String type;
    @ColumnInfo(name = "PRIORITY") public int priority;
    @ColumnInfo(name = "COLOR") public int color;
}
