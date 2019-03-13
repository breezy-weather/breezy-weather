package wangdaye.com.geometricweather.db.entity;

import android.database.sqlite.SQLiteDatabase;

import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Id;

import java.util.List;

import wangdaye.com.geometricweather.basic.model.Location;
import wangdaye.com.geometricweather.basic.model.weather.Weather;
import wangdaye.com.geometricweather.db.entity.table.DaoMaster;
import wangdaye.com.geometricweather.db.entity.weather.AlarmEntityDao;

import org.greenrobot.greendao.annotation.Generated;

/**
 * Alarm entity.
 * */

@Entity
public class AlarmEntity {
    // data
    @Id
    public Long id;
    public String cityId;
    public String city;

    public int alertId;
    public String content;
    public String description;
    public String publishTime;

    @Generated(hash = 221670418)
    public AlarmEntity(Long id, String cityId, String city, int alertId, String content, String description,
            String publishTime) {
        this.id = id;
        this.cityId = cityId;
        this.city = city;
        this.alertId = alertId;
        this.content = content;
        this.description = description;
        this.publishTime = publishTime;
    }

    @Generated(hash = 163591880)
    public AlarmEntity() {
    }

    // insert.

    public static void insertAlarmList(SQLiteDatabase database, Location location, Weather weather) {
        if (weather == null) {
            return;
        }

        deleteAlarmEntityList(
                database,
                searchLocationAlarmEntity(database, location));

        new DaoMaster(database)
                .newSession()
                .getAlarmEntityDao()
                .insertInTx(weather.toAlarmEntityList());
    }

    // delete.

    public static void deleteAlarmList(SQLiteDatabase database, Location location) {
        List<AlarmEntity> entityList = searchLocationAlarmEntity(database, location);
        deleteAlarmEntityList(database, entityList);
    }

    private static void deleteAlarmEntityList(SQLiteDatabase database, List<AlarmEntity> list) {
        new DaoMaster(database)
                .newSession()
                .getAlarmEntityDao()
                .deleteInTx(list);
    }

    // search.

    public static List<AlarmEntity> searchLocationAlarmEntity(SQLiteDatabase database, Location location) {
        return new DaoMaster(database)
                .newSession()
                .getAlarmEntityDao()
                .queryBuilder()
                .where(AlarmEntityDao.Properties.CityId.eq(location.cityId))
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

    public int getAlertId() {
        return this.alertId;
    }

    public void setAlertId(int alertId) {
        this.alertId = alertId;
    }

    public String getContent() {
        return this.content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getDescription() {
        return this.description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getPublishTime() {
        return this.publishTime;
    }

    public void setPublishTime(String publishTime) {
        this.publishTime = publishTime;
    }
}
