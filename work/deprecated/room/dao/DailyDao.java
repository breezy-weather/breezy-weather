package wangdaye.com.geometricweather.room.dao;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;

import java.util.List;

import wangdaye.com.geometricweather.basic.model.option.provider.WeatherSource;
import wangdaye.com.geometricweather.room.entity.DailyEntity;

@Dao
public interface DailyDao {

    @Insert
    void insertDailyList(List<DailyEntity> entityList);

    @Delete
    void deleteDailyEntityList(List<DailyEntity> entityList);

    @Query("SELECT * FROM DAILY_ENTITY " +
            "WHERE CITY_ID = :cityId AND WEATHER_SOURCE = :source " +
            "ORDER BY DATE ASC")
    List<DailyEntity> selectDailyEntityList(String cityId, WeatherSource source);
}
