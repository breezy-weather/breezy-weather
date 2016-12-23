package wangdaye.com.geometricweather.basic.deprecated;
/*
import android.database.sqlite.SQLiteDatabase;
import android.text.TextUtils;

import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Id;

import java.util.ArrayList;
import java.util.List;

import wangdaye.com.geometricweather.data.entity.model.Location;
import wangdaye.com.geometricweather.data.entity.result.cityList.OverseaCityListResult;

import org.greenrobot.greendao.annotation.Generated;
*/
/**
 * Oversea city entity.
 * */
/*
@Entity
public class OverseaCityEntity {
    // data
    @Id
    public Long id;

    public String cityId;
    public String cityEn;
    public String cityZh;
    public String continent;
    public String countryCode;
    public String countryEn;
    public String lat;
    public String lon;

    @Generated(hash = 610934963)
    public OverseaCityEntity(Long id, String cityId, String cityEn, String cityZh, String continent,
            String countryCode, String countryEn, String lat, String lon) {
        this.id = id;
        this.cityId = cityId;
        this.cityEn = cityEn;
        this.cityZh = cityZh;
        this.continent = continent;
        this.countryCode = countryCode;
        this.countryEn = countryEn;
        this.lat = lat;
        this.lon = lon;
    }

    @Generated(hash = 2094011740)
    public OverseaCityEntity() {
    }
*/
    /** <br> database. */
/*
    // insert.

    public static void insertOverseaCityList(SQLiteDatabase database, OverseaCityListResult result) {
        if (result == null) {
            return;
        }

        clearOverseaCityEntity(database);
        new DaoMaster(database)
                .newSession()
                .getOverseaCityEntityDao()
                .insertInTx(result.city_info);
    }

    public static boolean isNeedWriteData(SQLiteDatabase database) {
        return new DaoMaster(database)
                .newSession()
                .getOverseaCityEntityDao()
                .queryBuilder()
                .count() < 4572;
    }

    // delete.

    private static void clearOverseaCityEntity(SQLiteDatabase database) {
        new DaoMaster(database)
                .newSession()
                .getOverseaCityEntityDao()
                .deleteAll();
    }

    // search.

    public static List<Location> readOverseaCityLocation(SQLiteDatabase database) {
        List<OverseaCityEntity> entityList = new DaoMaster(database)
                .newSession()
                .getOverseaCityEntityDao()
                .queryBuilder()
                .list();
        List<Location> locationList = new ArrayList<>(entityList.size());
        for (int i = 0; i < entityList.size(); i ++) {
            locationList.add(Location.buildLocation(entityList.get(i)));
        }
        return locationList;
    }

    public static List<Location> accurateSearchOverseaCity(SQLiteDatabase database, String city) {
        city = city.replaceAll("市", "");

        List<Location> locationList = new ArrayList<>();
        List<OverseaCityEntity> entityList = new ArrayList<>();
        entityList.addAll(new DaoMaster(database)
                .newSession()
                .getOverseaCityEntityDao()
                .queryBuilder()
                .where(OverseaCityEntityDao.Properties.CityEn.eq(city))
                .list());
        entityList.addAll(new DaoMaster(database)
                .newSession()
                .getOverseaCityEntityDao()
                .queryBuilder()
                .where(OverseaCityEntityDao.Properties.CityZh.eq(city))
                .list());

        for (int i = 0; i < entityList.size(); i ++) {
            locationList.add(Location.buildLocation(entityList.get(i)));
        }
        return locationList;
    }

    public static List<Location> fuzzySearchOverseaCity(SQLiteDatabase database, String txt) {
        List<OverseaCityEntity> entityList = searchOverseaCityEntity(database, txt);
        List<Location> locationList = new ArrayList<>(entityList.size());
        for (int i = 0; i < entityList.size(); i ++) {
            locationList.add(Location.buildLocation(entityList.get(i)));
        }
        return locationList;
    }

    private static List<OverseaCityEntity> searchOverseaCityEntity(SQLiteDatabase database, String txt) {
        if (TextUtils.isEmpty(txt)) {
            return new DaoMaster(database)
                    .newSession()
                    .getOverseaCityEntityDao()
                    .queryBuilder()
                    .list();
        }
        List<OverseaCityEntity> entityList = new ArrayList<>();
        entityList.addAll(
                new DaoMaster(database)
                        .newSession()
                        .getOverseaCityEntityDao()
                        .queryBuilder()
                        .where(OverseaCityEntityDao.Properties.CityEn.like("%" + txt + "%"))
                        .list());
        return entityList;
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

    public String getCityEn() {
        return this.cityEn;
    }

    public void setCityEn(String cityEn) {
        this.cityEn = cityEn;
    }

    public String getCityZh() {
        return this.cityZh;
    }

    public void setCityZh(String cityZh) {
        this.cityZh = cityZh;
    }

    public String getContinent() {
        return this.continent;
    }

    public void setContinent(String continent) {
        this.continent = continent;
    }

    public String getCountryCode() {
        return this.countryCode;
    }

    public void setCountryCode(String countryCode) {
        this.countryCode = countryCode;
    }

    public String getCountryEn() {
        return this.countryEn;
    }

    public void setCountryEn(String countryEn) {
        this.countryEn = countryEn;
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
}
*/