package wangdaye.com.geometricweather.room.dao;

import androidx.annotation.Nullable;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import java.util.List;

import wangdaye.com.geometricweather.room.entity.ChineseCityEntity;

@Dao
public interface ChineseCityDao {

    @Insert
    void insertChineseCityEntityList(List<ChineseCityEntity> entityList);

    @Query("DELETE FROM CHINESE_CITY_ENTITY")
    void deleteChineseCityEntityList();

    // select.

    @Query("SELECT * FROM CHINESE_CITY_ENTITY WHERE DISTRICT = :name OR CITY = :name LIMIT 1")
    @Nullable ChineseCityEntity selectChineseCityEntity(String name);

    @Query("SELECT * FROM CHINESE_CITY_ENTITY WHERE DISTRICT = :district AND CITY = :city LIMIT 1")
    @Nullable ChineseCityEntity selectChineseCityEntity1(String city, String district);

    @Query("SELECT * FROM CHINESE_CITY_ENTITY WHERE DISTRICT = :district AND PROVINCE = :province")
    @Nullable ChineseCityEntity selectChineseCityEntity2(String province, String district);

    @Query("SELECT * FROM CHINESE_CITY_ENTITY WHERE CITY = :city AND PROVINCE = :province")
    @Nullable ChineseCityEntity selectChineseCityEntity3(String province, String city);

    @Query("SELECT * FROM CHINESE_CITY_ENTITY WHERE CITY = :city")
    @Nullable ChineseCityEntity selectChineseCityEntity4(String city);

    @Query("SELECT * FROM CHINESE_CITY_ENTITY WHERE DISTRICT = :city AND PROVINCE = :province")
    @Nullable ChineseCityEntity selectChineseCityEntity5(String province, String city);

    @Query("SELECT * FROM CHINESE_CITY_ENTITY WHERE DISTRICT = :city AND CITY = :province")
    @Nullable ChineseCityEntity selectChineseCityEntity6(String province, String city);

    @Query("SELECT * FROM CHINESE_CITY_ENTITY WHERE DISTRICT = :city")
    @Nullable ChineseCityEntity selectChineseCityEntity7(String city);

    @Query("SELECT * FROM CHINESE_CITY_ENTITY WHERE CITY = :district")
    @Nullable ChineseCityEntity selectChineseCityEntity1(String district);

    @Query("SELECT * FROM CHINESE_CITY_ENTITY")
    List<ChineseCityEntity> selectChineseCityEntityList();

    @Query("SELECT * FROM CHINESE_CITY_ENTITY " +
            "WHERE DISTRICT LIKE '%' || :name || '%' " +
            "OR CITY LIKE '%' || :name || '%'")
    List<ChineseCityEntity> selectChineseCityEntityList(String name);

    @Query("SELECT COUNT(*) FROM CHINESE_CITY_ENTITY")
    int countChineseCityEntity();
}
