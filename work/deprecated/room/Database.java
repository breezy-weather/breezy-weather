package wangdaye.com.geometricweather.room;

import androidx.room.RoomDatabase;
import androidx.room.TypeConverters;

import wangdaye.com.geometricweather.room.converter.DateConverter;
import wangdaye.com.geometricweather.room.converter.TimeZoneConverter;
import wangdaye.com.geometricweather.room.converter.WeatherCodeConverter;
import wangdaye.com.geometricweather.room.converter.WeatherSourceConverter;
import wangdaye.com.geometricweather.room.converter.WindDegreeConverter;
import wangdaye.com.geometricweather.room.dao.AlertDao;
import wangdaye.com.geometricweather.room.dao.ChineseCityDao;
import wangdaye.com.geometricweather.room.dao.DailyDao;
import wangdaye.com.geometricweather.room.dao.HistoryDao;
import wangdaye.com.geometricweather.room.dao.HourlyDao;
import wangdaye.com.geometricweather.room.dao.LocationDao;
import wangdaye.com.geometricweather.room.dao.MinutelyDao;
import wangdaye.com.geometricweather.room.dao.WeatherDao;
import wangdaye.com.geometricweather.room.entity.AlertEntity;
import wangdaye.com.geometricweather.room.entity.ChineseCityEntity;
import wangdaye.com.geometricweather.room.entity.DailyEntity;
import wangdaye.com.geometricweather.room.entity.HistoryEntity;
import wangdaye.com.geometricweather.room.entity.HourlyEntity;
import wangdaye.com.geometricweather.room.entity.LocationEntity;
import wangdaye.com.geometricweather.room.entity.MinutelyEntity;
import wangdaye.com.geometricweather.room.entity.WeatherEntity;

@androidx.room.Database(
        entities = {
                AlertEntity.class,
                ChineseCityEntity.class,
                DailyEntity.class,
                HistoryEntity.class,
                HourlyEntity.class,
                LocationEntity.class,
                MinutelyEntity.class,
                WeatherEntity.class
        }, version = 53, exportSchema = false
)
@TypeConverters({
        DateConverter.class,
        TimeZoneConverter.class,
        WeatherCodeConverter.class,
        WeatherSourceConverter.class,
        WindDegreeConverter.class
})
public abstract class Database extends RoomDatabase {

    public abstract AlertDao alertDao();
    public abstract ChineseCityDao chineseCityDao();
    public abstract DailyDao dailyDao();
    public abstract HistoryDao historyDao();
    public abstract HourlyDao hourlyDao();
    public abstract LocationDao locationDao();
    public abstract MinutelyDao minutelyDao();
    public abstract WeatherDao weatherDao();
}
