package wangdaye.com.geometricweather.data.entity.table.weather;

import android.database.sqlite.SQLiteDatabase;

import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Id;

import java.util.ArrayList;
import java.util.List;

import wangdaye.com.geometricweather.data.entity.model.Location;
import wangdaye.com.geometricweather.data.entity.model.Weather;
import wangdaye.com.geometricweather.data.entity.table.DaoMaster;
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
    
    public String content;
    public String description;
    public String name;
    public String level;
    public String color;
    public String typeCode;
    public String typeDescription;
    public String precaution;
    public String publishTime;

    @Generated(hash = 526696603)
    public AlarmEntity(Long id, String cityId, String city, String content, String description, String name, String level,
            String color, String typeCode, String typeDescription, String precaution, String publishTime) {
        this.id = id;
        this.cityId = cityId;
        this.city = city;
        this.content = content;
        this.description = description;
        this.name = name;
        this.level = level;
        this.color = color;
        this.typeCode = typeCode;
        this.typeDescription = typeDescription;
        this.precaution = precaution;
        this.publishTime = publishTime;
    }

    @Generated(hash = 163591880)
    public AlarmEntity() {
    }

    /** <br> life cycle. */

    private static List<AlarmEntity> buildAlarmEntityList(Weather weather) {
        List<AlarmEntity> entityList = new ArrayList<>(weather.alarmList.size());
        for (int i = 0; i < weather.alarmList.size(); i ++) {
            AlarmEntity entity = new AlarmEntity();
            entity.cityId = weather.base.cityId;
            entity.city = weather.base.city;
            entity.content = weather.alarmList.get(i).content;
            entity.description = weather.alarmList.get(i).description;
            entity.name = weather.alarmList.get(i).name;
            entity.level = weather.alarmList.get(i).level;
            entity.color = weather.alarmList.get(i).color;
            entity.typeCode = weather.alarmList.get(i).typeCode;
            entity.typeDescription = weather.alarmList.get(i).typeDescription;
            entity.precaution = weather.alarmList.get(i).precaution;
            entity.publishTime = weather.alarmList.get(i).publishTime;
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
        AlarmEntityDao dao = new DaoMaster(database)
                .newSession()
                .getAlarmEntityDao();
        for (int i = 0; i < entityList.size(); i ++) {
            dao.insert(entityList.get(i));
        }
    }

    // delete.

    private static void deleteAlarmEntityList(SQLiteDatabase database, List<AlarmEntity> list) {
        for (int i = 0; i < list.size(); i ++) {
            new DaoMaster(database)
                    .newSession()
                    .getAlarmEntityDao()
                    .delete(list.get(i));
        }
    }

    // search.

    public static List<AlarmEntity> searchLocationAlarmEntity(SQLiteDatabase database, Location location) {
        return new DaoMaster(database)
                .newSession()
                .getAlarmEntityDao()
                .queryBuilder()
                .where(location.isEngLocation() ?
                        AlarmEntityDao.Properties.City.eq(location.city) : AlarmEntityDao.Properties.CityId.eq(location.cityId))
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

    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getLevel() {
        return this.level;
    }

    public void setLevel(String level) {
        this.level = level;
    }

    public String getColor() {
        return this.color;
    }

    public void setColor(String color) {
        this.color = color;
    }

    public String getTypeCode() {
        return this.typeCode;
    }

    public void setTypeCode(String typeCode) {
        this.typeCode = typeCode;
    }

    public String getTypeDescription() {
        return this.typeDescription;
    }

    public void setTypeDescription(String typeDescription) {
        this.typeDescription = typeDescription;
    }

    public String getPrecaution() {
        return this.precaution;
    }

    public void setPrecaution(String precaution) {
        this.precaution = precaution;
    }

    public String getPublishTime() {
        return this.publishTime;
    }

    public void setPublishTime(String publishTime) {
        this.publishTime = publishTime;
    }
}
