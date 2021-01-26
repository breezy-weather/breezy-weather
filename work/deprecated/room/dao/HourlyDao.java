package wangdaye.com.geometricweather.room.dao;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;

import java.util.List;

import wangdaye.com.geometricweather.basic.model.option.provider.WeatherSource;
import wangdaye.com.geometricweather.room.entity.HourlyEntity;

@Dao
public interface HourlyDao {

    @Insert
    void insertHourlyList(List<HourlyEntity> entityList);

    @Delete
    void deleteHourlyEntityList(List<HourlyEntity> entityList);

    @Query("SELECT * FROM HOURLY_ENTITY " +
            "WHERE CITY_ID = :cityId AND WEATHER_SOURCE = :source " +
            "ORDER BY DATE ASC")
    List<HourlyEntity> selectHourlyEntityList(String cityId, WeatherSource source);
}
