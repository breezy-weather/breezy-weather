package wangdaye.com.geometricweather.db.entity;

import android.database.sqlite.SQLiteDatabase;

import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Id;

import java.util.List;

import wangdaye.com.geometricweather.basic.model.Location;
import wangdaye.com.geometricweather.basic.model.weather.Weather;
import wangdaye.com.geometricweather.db.entity.table.DaoMaster;
import wangdaye.com.geometricweather.db.entity.weather.HourlyEntityDao;

import org.greenrobot.greendao.annotation.Generated;

/**
 * Hourly entity.
 * */

@Entity
public class HourlyEntity {
    // data
    @Id
    public Long id;
    public String cityId;
    public String city;
    
    public String time;
    public boolean dayTime;
    public String weather;
    public String weatherKind;
    public int temp;
    public int precipitation;

    @Generated(hash = 88370744)
    public HourlyEntity(Long id, String cityId, String city, String time, boolean dayTime, String weather, String weatherKind,
            int temp, int precipitation) {
        this.id = id;
        this.cityId = cityId;
        this.city = city;
        this.time = time;
        this.dayTime = dayTime;
        this.weather = weather;
        this.weatherKind = weatherKind;
        this.temp = temp;
        this.precipitation = precipitation;
    }

    @Generated(hash = 617074574)
    public HourlyEntity() {
    }

    // insert.

    public static void insertDailyList(SQLiteDatabase database, Location location, Weather weather) {
        if (weather == null) {
            return;
        }

        deleteHourlyEntityList(
                database,
                searchLocationHourlyEntity(database, location));

        new DaoMaster(database)
                .newSession()
                .getHourlyEntityDao()
                .insertInTx(weather.toHourlyEntityList());
    }

    // delete.

    public static void deleteHourlyList(SQLiteDatabase database, Location location) {
        List<HourlyEntity> entityList = searchLocationHourlyEntity(database, location);
        deleteHourlyEntityList(database, entityList);
    }

    private static void deleteHourlyEntityList(SQLiteDatabase database, List<HourlyEntity> list) {
        new DaoMaster(database)
                .newSession()
                .getHourlyEntityDao()
                .deleteInTx(list);
    }

    // search.

    public static List<HourlyEntity> searchLocationHourlyEntity(SQLiteDatabase database, Location location) {
        return new DaoMaster(database)
                .newSession()
                .getHourlyEntityDao()
                .queryBuilder()
                .where(HourlyEntityDao.Properties.CityId.eq(location.cityId))
                .list();
    }

    public Long getId() {
        return this.id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getCityId() {
        return this.cityId;
    }

    public void setCityId(String cityId) {
        this.cityId = cityId;
    }

    public String getCity() {
        return this.city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getTime() {
        return this.time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public boolean getDayTime() {
        return this.dayTime;
    }

    public void setDayTime(boolean dayTime) {
        this.dayTime = dayTime;
    }

    public String getWeather() {
        return this.weather;
    }

    public void setWeather(String weather) {
        this.weather = weather;
    }

    public String getWeatherKind() {
        return this.weatherKind;
    }

    public void setWeatherKind(String weatherKind) {
        this.weatherKind = weatherKind;
    }

    public int getTemp() {
        return this.temp;
    }

    public void setTemp(int temp) {
        this.temp = temp;
    }

    public int getPrecipitation() {
        return this.precipitation;
    }

    public void setPrecipitation(int precipitation) {
        this.precipitation = precipitation;
    }
}
