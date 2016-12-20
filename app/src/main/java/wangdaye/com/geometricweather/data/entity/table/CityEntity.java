package wangdaye.com.geometricweather.data.entity.table;

import android.database.sqlite.SQLiteDatabase;
import android.text.TextUtils;

import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Id;

import wangdaye.com.geometricweather.data.entity.model.Location;
import wangdaye.com.geometricweather.data.entity.result.CityListResult;

import java.util.ArrayList;
import java.util.List;
import org.greenrobot.greendao.annotation.Generated;

/**
 * City entity.
 * */

@Entity
public class CityEntity {
    @Id
    public Long id;

    /**
     * city : 南子岛
     * cnty : 中国
     * id : 101310230
     * lat : 11.26
     * lon : 114.20
     * prov : 海南
     */
    public String cityId;
    public String city;
    public String cnty;
    public String lat;
    public String lon;
    public String prov;

    @Generated(hash = 287673386)
    public CityEntity(Long id, String cityId, String city, String cnty, String lat, String lon, String prov) {
        this.id = id;
        this.cityId = cityId;
        this.city = city;
        this.cnty = cnty;
        this.lat = lat;
        this.lon = lon;
        this.prov = prov;
    }

    @Generated(hash = 2001321047)
    public CityEntity() {
    }

    /** <br> life cycle. */

    private static CityEntity buildCityEntity(CityListResult.CityInfo info) {
        CityEntity entity = new CityEntity();
        entity.cityId = info.id;
        entity.city = info.city;
        entity.cnty = info.cnty;
        entity.lat = info.lat;
        entity.lon = info.lon;
        entity.prov = info.prov;
        return entity;
    }

    /** <br> database. */

    // insert.

    public static void insertCityList(SQLiteDatabase database, CityListResult result) {
        if (result == null) {
            return;
        }

        clearCityEntity(database);
        CityEntityDao dao = new DaoMaster(database)
                .newSession()
                .getCityEntityDao();
        for (int i = 0; i < result.city_info.size(); i ++) {
            dao.insert(buildCityEntity(result.city_info.get(i)));
        }
    }

    public static boolean isNeedWriteData(SQLiteDatabase database) {
        return new DaoMaster(database)
                .newSession()
                .getCityEntityDao()
                .queryBuilder()
                .count() < 2567;
    }

    // delete.

    private static void clearCityEntity(SQLiteDatabase database) {
        new DaoMaster(database)
                .newSession()
                .getCityEntityDao()
                .deleteAll();
    }

    // search.

    public static List<Location> readCityLocation(SQLiteDatabase database) {
        List<CityEntity> entityList = new DaoMaster(database)
                .newSession()
                .getCityEntityDao()
                .queryBuilder()
                .list();
        List<Location> locationList = new ArrayList<>(entityList.size());
        for (int i = 0; i < entityList.size(); i ++) {
            locationList.add(Location.buildLocation(entityList.get(i)));
        }
        return locationList;
    }

    public static List<Location> accurateSearchCity(SQLiteDatabase database, String txt) {
        if (!txt.equals("呼市郊区") && !txt.equals("尖草坪区") && !txt.equals("小店区") && !txt.equals("淮阴区")
                && !txt.equals("淮安区") && !txt.equals("黄山区") && !txt.equals("黄山风景区") && !txt.equals("赫山区")
                && !txt.equals("青白江区") && !txt.equals("沙市") && !txt.equals("津市") && !txt.equals("芒市")) {
            txt = txt.replace("区", "").replaceAll("市", "");
        }

        List<CityEntity> entityList = new DaoMaster(database)
                .newSession()
                .getCityEntityDao()
                .queryBuilder()
                .where(CityEntityDao.Properties.City.eq(txt))
                .list();

        List<Location> locationList = new ArrayList<>(entityList.size());
        for (int i = 0; i < entityList.size(); i ++) {
            locationList.add(Location.buildLocation(entityList.get(i)));
        }
        return locationList;
    }

    public static List<Location> fuzzySearchCity(SQLiteDatabase database, String txt) {
        List<CityEntity> entityList = searchCityEntity(database, txt);
        List<Location> locationList = new ArrayList<>(entityList.size());
        for (int i = 0; i < entityList.size(); i ++) {
            locationList.add(Location.buildLocation(entityList.get(i)));
        }
        return locationList;
    }

    private static List<CityEntity> searchCityEntity(SQLiteDatabase database, String txt) {
        if (TextUtils.isEmpty(txt)) {
            return new DaoMaster(database)
                    .newSession()
                    .getCityEntityDao()
                    .queryBuilder()
                    .list();
        }
        List<CityEntity> entityList = new ArrayList<>();
        entityList.addAll(
                new DaoMaster(database)
                        .newSession()
                        .getCityEntityDao()
                        .queryBuilder()
                        .where(CityEntityDao.Properties.City.like("%" + txt + "%"))
                        .list());
        entityList.addAll(
                new DaoMaster(database)
                        .newSession()
                        .getCityEntityDao()
                        .queryBuilder()
                        .where(CityEntityDao.Properties.Prov.like("%" + txt + "%"))
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
}
