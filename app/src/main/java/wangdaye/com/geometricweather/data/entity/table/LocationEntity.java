package wangdaye.com.geometricweather.data.entity.table;

import android.database.sqlite.SQLiteDatabase;

import org.greenrobot.greendao.annotation.Entity;

import wangdaye.com.geometricweather.data.entity.model.Location;
import wangdaye.com.geometricweather.data.entity.table.weather.DaoMaster;

import org.greenrobot.greendao.annotation.Id;

import java.util.ArrayList;
import java.util.List;
import org.greenrobot.greendao.annotation.Generated;

/**
 * Location entity.
 * */

@Entity
public class LocationEntity {

    @Id
    public Long id;

    public String cityId;
    public String district;
    public String city;
    public String province;
    public String country;
    public String lat;
    public String lon;
    public String source;

    public boolean local;
    public boolean china;

    @Generated(hash = 2018059014)
    public LocationEntity(Long id, String cityId, String district, String city, String province,
            String country, String lat, String lon, String source, boolean local, boolean china) {
        this.id = id;
        this.cityId = cityId;
        this.district = district;
        this.city = city;
        this.province = province;
        this.country = country;
        this.lat = lat;
        this.lon = lon;
        this.source = source;
        this.local = local;
        this.china = china;
    }

    @Generated(hash = 1723987110)
    public LocationEntity() {
    }

    private static LocationEntity buildLocationEntity(Location location) {
        LocationEntity entity = new LocationEntity();
        entity.cityId = location.cityId;
        entity.district = location.district;
        entity.city = location.city;
        entity.province = location.province;
        entity.country = location.country;
        entity.lat = location.lat;
        entity.lon = location.lon;
        entity.source = location.source;
        entity.local = location.local;
        entity.china = location.china;
        return entity;
    }

    private static LocationEntity buildLocationEntity(long id, Location location) {
        LocationEntity entity = new LocationEntity();
        entity.id = id;
        entity.cityId = location.cityId;
        entity.district = location.district;
        entity.city = location.city;
        entity.province = location.province;
        entity.country = location.country;
        entity.lat = location.lat;
        entity.lon = location.lon;
        entity.source = location.source;
        entity.local = location.local;
        entity.china = location.china;
        return entity;
    }

    // insert.

    public static void insertLocation(SQLiteDatabase database, Location location) {
        if (location == null) {
            return;
        }

        LocationEntity entity = searchLocationEntity(database, location);

        if (entity == null) {
            new DaoMaster(database)
                    .newSession()
                    .getLocationEntityDao()
                    .insert(buildLocationEntity(location));
        } else {
            updateLocation(database, buildLocationEntity(entity.id, location));
        }
    }

    public static void writeLocationList(SQLiteDatabase database, List<Location> list) {
        if (list == null || list.size() == 0) {
            return;
        }

        clearLocation(database);
        List<LocationEntity> entityList = new ArrayList<>();
        for (int i = 0; i < list.size(); i ++) {
            entityList.add(buildLocationEntity(list.get(i)));
        }
        new DaoMaster(database)
                .newSession()
                .getLocationEntityDao()
                .insertInTx(entityList);
    }

    // delete.

    public static void deleteLocation(SQLiteDatabase database, Location location) {
        if (location == null) {
            return;
        }

        LocationEntity entity = searchLocationEntity(database, location);
        if (entity != null) {
            new DaoMaster(database)
                    .newSession()
                    .getLocationEntityDao()
                    .delete(entity);
        }
    }

    public static void clearLocation(SQLiteDatabase database) {
        new DaoMaster(database)
                .newSession()
                .getLocationEntityDao()
                .deleteAll();
    }

    // updateRotation

    private static void updateLocation(SQLiteDatabase database, LocationEntity entity) {
        new DaoMaster(database)
                .newSession()
                .getLocationEntityDao()
                .update(entity);
    }

    // search.

    private static LocationEntity searchLocationEntity(SQLiteDatabase database, Location location) {
        List<LocationEntity> entityList = new DaoMaster(database)
                .newSession()
                .getLocationEntityDao()
                .queryBuilder()
                .where(location.isLocal() ?
                        LocationEntityDao.Properties.Local.eq(location.local)
                        :
                        LocationEntityDao.Properties.CityId.eq(location.cityId))
                .list();
        if (entityList == null || entityList.size() <= 0) {
            return null;
        } else {
            return entityList.get(0);
        }
    }

    public static List<Location> readLocationList(SQLiteDatabase database) {
        List<LocationEntity> entityList = new DaoMaster(database)
                .newSession()
                .getLocationEntityDao()
                .queryBuilder()
                .list();
        List<Location> locationList = new ArrayList<>();
        for (int i = 0; i < entityList.size(); i ++) {
            locationList.add(Location.buildLocation(entityList.get(i)));
        }
        if (locationList.size() <= 0) {
            locationList.add(Location.buildLocal());
            insertLocation(database, locationList.get(0));
        }
        return locationList;
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

    public String getDistrict() {
        return this.district;
    }

    public void setDistrict(String district) {
        this.district = district;
    }

    public String getCity() {
        return this.city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getProvince() {
        return this.province;
    }

    public void setProvince(String province) {
        this.province = province;
    }

    public String getCountry() {
        return this.country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public String getLat() {
        return this.lat;
    }

    public void setLat(String lat) {
        this.lat = lat;
    }

    public String getLon() {
        return this.lon;
    }

    public void setLon(String lon) {
        this.lon = lon;
    }

    public String getSource() {
        return this.source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public boolean getLocal() {
        return this.local;
    }

    public void setLocal(boolean local) {
        this.local = local;
    }

    public boolean getChina() {
        return this.china;
    }

    public void setChina(boolean china) {
        this.china = china;
    }
}
