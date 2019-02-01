package wangdaye.com.geometricweather.data.entity.table;

import android.database.sqlite.SQLiteDatabase;
import android.text.TextUtils;

import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Id;
import org.greenrobot.greendao.query.QueryBuilder;

import java.util.ArrayList;
import java.util.List;

import wangdaye.com.geometricweather.data.entity.model.CNCityList;
import org.greenrobot.greendao.annotation.Generated;

/**
 * CN city entity.
 * */

@Entity
public class CNCityEntity {

    @Id
    public Long id;
    public String province;
    public String city;
    public String district;
    public String lat;
    public String lon;
    public String requestKey;

    @Generated(hash = 587338222)
    public CNCityEntity(Long id, String province, String city, String district, String lat, String lon,
            String requestKey) {
        this.id = id;
        this.province = province;
        this.city = city;
        this.district = district;
        this.lat = lat;
        this.lon = lon;
        this.requestKey = requestKey;
    }

    @Generated(hash = 744669023)
    public CNCityEntity() {
    }

    private static CNCityEntity buildCNCityEntity(CNCityList.CNCity city) {
        CNCityEntity entity = new CNCityEntity();
        entity.province = city.province;
        entity.city = city.city;
        entity.district = city.district;
        entity.lat = city.lat;
        entity.lon = city.lon;
        entity.requestKey = city.requestKey;
        return entity;
    }

    public static void insertCNCityList(SQLiteDatabase database, CNCityList list) {
        if (list == null || list.citys == null || list.citys.size() == 0) {
            return;
        }

        List<CNCityEntity> entityList = new ArrayList<>(list.citys.size());
        for (int i = 0; i < list.citys.size(); i ++) {
            entityList.add(buildCNCityEntity(list.citys.get(i)));
        }

        new DaoMaster(database)
                .newSession()
                .getCNCityEntityDao()
                .insertInTx(entityList);
    }

    public static void removeCNCityList(SQLiteDatabase database) {
        new DaoMaster(database)
                .newSession()
                .getCNCityEntityDao()
                .deleteAll();
    }

    public static CNCityList.CNCity searchCNCity(SQLiteDatabase database, String name) {
        if (TextUtils.isEmpty(name)) {
            return null;
        }

        CNCityEntityDao dao = new DaoMaster(database)
                .newSession()
                .getCNCityEntityDao();

        QueryBuilder<CNCityEntity> builder = dao.queryBuilder();
        builder.whereOr(
                CNCityEntityDao.Properties.District.eq(name),
                CNCityEntityDao.Properties.City.eq(name));

        List<CNCityEntity> entityList = builder.list();
        if (entityList == null || entityList.size() <= 0) {
            return null;
        } else {
            return CNCityList.CNCity.buildCNCity(entityList.get(0));
        }
    }

    public static CNCityList.CNCity searchCNCity(SQLiteDatabase database,
                                                 String district, String city, String province) {
        if ((TextUtils.isEmpty(district) && TextUtils.isEmpty(city))
                || TextUtils.isEmpty(province)) {
            return null;
        }

        CNCityEntityDao dao = new DaoMaster(database)
                .newSession()
                .getCNCityEntityDao();

        List<CNCityEntity> entityList;

        // district, city.
        try {
            entityList = dao.queryBuilder()
                    .where(
                            CNCityEntityDao.Properties.District.eq(district),
                            CNCityEntityDao.Properties.City.eq(city)).list();
        } catch (Exception e) {
            entityList = null;
        }
        if (entityList != null && entityList.size() > 0) {
            return CNCityList.CNCity.buildCNCity(entityList.get(0));
        }

        // district, province.
        try {
            entityList = dao.queryBuilder()
                    .where(
                            CNCityEntityDao.Properties.District.eq(district),
                            CNCityEntityDao.Properties.Province.eq(province)).list();
        } catch (Exception e) {
            entityList = null;
        }
        if (entityList != null && entityList.size() > 0) {
            return CNCityList.CNCity.buildCNCity(entityList.get(0));
        }

        // city, province.
        try {
            entityList = dao.queryBuilder()
                    .where(
                            CNCityEntityDao.Properties.City.eq(city),
                            CNCityEntityDao.Properties.Province.eq(province)).list();
        } catch (Exception e) {
            entityList = null;
        }
        if (entityList != null && entityList.size() > 0) {
            return CNCityList.CNCity.buildCNCity(entityList.get(0));
        }

        return null;
    }

    public static List<CNCityList.CNCity> fuzzySearchCNCity(SQLiteDatabase database, String name) {
        if (TextUtils.isEmpty(name)) {
            return null;
        }

        CNCityEntityDao dao = new DaoMaster(database)
                .newSession()
                .getCNCityEntityDao();

        QueryBuilder<CNCityEntity> builder = dao.queryBuilder();
        builder.whereOr(
                CNCityEntityDao.Properties.District.like("%" + name + "%"),
                CNCityEntityDao.Properties.City.like("%" + name + "%"),
                CNCityEntityDao.Properties.Province.like("%" + name + "%"));

        List<CNCityList.CNCity> cityList = new ArrayList<>();
        List<CNCityEntity> entityList = builder.list();
        if (entityList != null && entityList.size() > 0) {
            for (int i = 0; i < entityList.size(); i ++) {
                cityList.add(CNCityList.CNCity.buildCNCity(entityList.get(i)));
            }
        }
        return cityList;
    }

    public static long countCNCity(SQLiteDatabase database) {
        return new DaoMaster(database)
                .newSession()
                .getCNCityEntityDao()
                .count();
    }

    public Long getId() {
        return this.id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getProvince() {
        return this.province;
    }

    public void setProvince(String province) {
        this.province = province;
    }

    public String getCity() {
        return this.city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getDistrict() {
        return this.district;
    }

    public void setDistrict(String district) {
        this.district = district;
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

    public String getRequestKey() {
        return this.requestKey;
    }

    public void setRequestKey(String requestKey) {
        this.requestKey = requestKey;
    }
}
