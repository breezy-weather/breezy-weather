package wangdaye.com.geometricweather.room.dao;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;

import java.util.List;

import wangdaye.com.geometricweather.basic.model.option.provider.WeatherSource;
import wangdaye.com.geometricweather.room.entity.AlertEntity;

@Dao
public interface AlertDao {

    @Insert
    void insertAlertList(List<AlertEntity> entityList);

    @Delete
    void deleteAlertList(List<AlertEntity> entityList);

    @Query("SELECT * FROM ALERT_ENTITY WHERE CITY_ID = :cityId AND WEATHER_SOURCE = :source")
    List<AlertEntity> selectLocationAlertEntity(String cityId, WeatherSource source);
}
