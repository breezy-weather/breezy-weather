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

    /** <br> life cycle. */

    private static List<AlarmEntity> buildAlarmEntityList(Weather weather) {
        List<AlarmEntity> entityList = new ArrayList<>(weather.alertList.size());
        for (int i = 0; i < weather.alertList.size(); i ++) {
            AlarmEntity entity = new AlarmEntity();
            entity.cityId = weather.base.cityId;
            entity.city = weather.base.city;
            entity.alertId = weather.alertList.get(i).id;
            entity.content = weather.alertList.get(i).content;
            entity.description = weather.alertList.get(i).description;
            entity.publishTime = weather.alertList.get(i).publishTime;
            entityList.add(entity);
        }
        return entityList;
    }

    /** <br> database. */

    // insert.

    public static void insertAlarmList(SQLiteDatabase database, Location location, Weather weather) {
        if (weather == null) {
            return;
        }

        deleteAlarmEntityList(
                database,
                searchLocationAlarmEntity(database, location));

        List<AlarmEntity> entityList = buildAlarmEntityList(weather);
        new DaoMaster(database)
                .newSession()
                .getAlarmEntityDao()
                .insertInTx(entityList);
    }

    // delete.

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
