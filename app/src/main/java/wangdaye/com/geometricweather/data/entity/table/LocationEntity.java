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
    public String city;
    public String cnty;
    public String lat;
    public String lon;
    public String prov;
    public boolean local;

    @Generated(hash = 513543808)
    public LocationEntity(Long id, String cityId, String city, String cnty, String lat, String lon,
            String prov, boolean local) {
        this.id = id;
        this.cityId = cityId;
        this.city = city;
        this.cnty = cnty;
        this.lat = lat;
        this.lon = lon;
        this.prov = prov;
        this.local = local;
    }

    @Generated(hash = 1723987110)
    public LocationEntity() {
    }

    private static LocationEntity buildLocationEntity(Location location) {
        LocationEntity entity = new LocationEntity();
        entity.cityId = location.cityId;
        entity.city = location.city;
        entity.cnty = location.cnty;
        entity.lat = location.lat;
        entity.lon = location.lon;
        entity.prov = location.prov;
        entity.local = location.local;
        return entity;
    }

    private static LocationEntity buildLocationEntity(long id, Location location) {
        LocationEntity entity = new LocationEntity();
        entity.id = id;
        entity.cityId = location.cityId;
        entity.city = location.city;
        entity.cnty = location.cnty;
        entity.lat = location.lat;
        entity.lon = location.lon;
        entity.prov = location.prov;
        entity.local = location.local;
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

    public String getCity() {
        return this.city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getCnty() {
        return this.cnty;
    }

    public void setCnty(String cnty) {
        this.cnty = cnty;
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

    public String getProv() {
        return this.prov;
    }

    public void setProv(String prov) {
        this.prov = prov;
    }

    public boolean getLocal() {
        return this.local;
    }

    public void setLocal(boolean local) {
        this.local = local;
    }
}
