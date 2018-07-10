package wangdaye.com.geometricweather.data.entity.table.weather;

import android.database.sqlite.SQLiteDatabase;

import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Id;

import java.util.ArrayList;
import java.util.List;

import wangdaye.com.geometricweather.data.entity.model.Location;
import wangdaye.com.geometricweather.data.entity.model.weather.Weather;
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

    /** <br> life cycle. */

    private static List<HourlyEntity> buildHourlyEntityList(Weather weather) {
        List<HourlyEntity> entityList = new ArrayList<>(weather.hourlyList.size());
        for (int i = 0; i < weather.hourlyList.size(); i ++) {
            HourlyEntity entity = new HourlyEntity();
            entity.cityId = weather.base.cityId;
            entity.city = weather.base.city;
            entity.time = weather.hourlyList.get(i).time;
            entity.dayTime = weather.hourlyList.get(i).dayTime;
            entity.weather = weather.hourlyList.get(i).weather;
            entity.weatherKind = weather.hourlyList.get(i).weatherKind;
            entity.temp = weather.hourlyList.get(i).temp;
            entity.precipitation = weather.hourlyList.get(i).precipitation;
            entityList.add(entity);
        }
        return entityList;
    }

    /** <br> database. */

    // insert.

    public static void insertDailyList(SQLiteDatabase database, Location location, Weather weather) {
        if (weather == null) {
            return;
        }

        deleteHourlyEntityList(
                database,
                searchLocationHourlyEntity(database, location));

        List<HourlyEntity> entityList = buildHourlyEntityList(weather);
        new DaoMaster(database)
                .newSession()
                .getHourlyEntityDao()
                .insertInTx(entityList);
    }

    // delete.

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
