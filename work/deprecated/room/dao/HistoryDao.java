package wangdaye.com.geometricweather.room.dao;

import androidx.annotation.Nullable;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import java.util.Date;

import wangdaye.com.geometricweather.basic.model.option.provider.WeatherSource;
import wangdaye.com.geometricweather.room.entity.HistoryEntity;

@Dao
public interface HistoryDao {

    @Insert
    void insertHistoryEntity(HistoryEntity entity);

    @Query("DELETE FROM HISTORY_ENTITY WHERE CITY_ID = :cityId AND WEATHER_SOURCE = :source")
    void deleteLocationHistoryEntity(String cityId, WeatherSource source);

    // select.

    @Query("SELECT * FROM HISTORY_ENTITY " +
            "WHERE CITY_ID = :cityId " +
            "AND WEATHER_SOURCE = :source " +
            "AND (DATE BETWEEN :from AND :to)")
    @Nullable HistoryEntity selectHistoryEntity(String cityId, WeatherSource source, Date from, Date to);
}
