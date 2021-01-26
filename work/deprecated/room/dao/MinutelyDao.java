package wangdaye.com.geometricweather.room.dao;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;

import java.util.List;

import wangdaye.com.geometricweather.basic.model.option.provider.WeatherSource;
import wangdaye.com.geometricweather.room.entity.MinutelyEntity;

@Dao
public interface MinutelyDao {

    @Insert
    void insertMinutelyList(List<MinutelyEntity> entityList);

    @Delete
    void deleteMinutelyEntityList(List<MinutelyEntity> entityList);

    @Query("SELECT * FROM MINUTELY_ENTITY " +
            "WHERE CITY_ID = :cityId AND WEATHER_SOURCE = :source " +
            "ORDER BY DATE ASC")
    List<MinutelyEntity> selectMinutelyEntityList(String cityId, WeatherSource source);
}
