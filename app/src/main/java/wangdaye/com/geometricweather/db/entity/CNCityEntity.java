package wangdaye.com.geometricweather.db.entity;

import android.database.sqlite.SQLiteDatabase;
import android.text.TextUtils;

import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Id;
import org.greenrobot.greendao.query.QueryBuilder;

import java.util.ArrayList;
import java.util.List;

import wangdaye.com.geometricweather.basic.model.CNCity;

import org.greenrobot.greendao.annotation.Generated;
import org.greenrobot.greendao.query.WhereCondition;

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

    private CNCity toCNCity() {
        CNCity c = new CNCity();
        c.setCityId(requestKey);
        c.setProvince(province);
        c.setCity(city);
        c.setDistrict(district);
        c.setLatitude(lat);
        c.setLongitude(lon);
        return c;
    }

    public static void insertCNCityList(SQLiteDatabase database, List<CNCity> list) {
        if (list == null || list.size() == 0) {
            return;
        }

        List<CNCityEntity> entityList = new ArrayList<>(list.size());
        for (int i = 0; i < list.size(); i ++) {
            entityList.add(list.get(i).toCNCityEntity());
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

    public static CNCity searchCNCity(SQLiteDatabase database, String name) {
        if (TextUtils.isEmpty(name)) {
            return null;
        }

        CNCityEntityDao dao = new DaoMaster(database)
                .newSession()
                .getCNCityEntityDao();

        QueryBuilder<CNCityEntity> builder = dao.queryBuilder();
        builder.whereOr(
                CNCityEntityDao.Properties.District.eq(name),
                CNCityEntityDao.Properties.City.eq(name)
        );

        List<CNCityEntity> entityList = builder.list();
        if (entityList == null || entityList.size() <= 0) {
            return null;
        } else {
            return entityList.get(0).toCNCity();
        }
    }

    public static CNCity searchCNCity(SQLiteDatabase database,
                                      String district, String city, String province) {

        CNCityEntityDao dao = new DaoMaster(database)
                .newSession()
                .getCNCityEntityDao();

        List<WhereCondition> conditionList = new ArrayList<>();
        conditionList.add(
                dao.queryBuilder().and(
                        CNCityEntityDao.Properties.District.eq(district),
                        CNCityEntityDao.Properties.City.eq(city)
                )
        );
        conditionList.add(
                dao.queryBuilder().and(
                        CNCityEntityDao.Properties.District.eq(district),
                        CNCityEntityDao.Properties.Province.eq(province)
                )
        );
        conditionList.add(
                dao.queryBuilder().and(
                        CNCityEntityDao.Properties.City.eq(city),
                        CNCityEntityDao.Properties.Province.eq(province)
                )
        );
        conditionList.add(CNCityEntityDao.Properties.City.eq(city));
        conditionList.add(
                dao.queryBuilder().and(
                        CNCityEntityDao.Properties.District.eq(city),
                        CNCityEntityDao.Properties.Province.eq(province)
                )
        );
        conditionList.add(
                dao.queryBuilder().and(
                        CNCityEntityDao.Properties.District.eq(city),
                        CNCityEntityDao.Properties.City.eq(province)
                )
        );
        conditionList.add(CNCityEntityDao.Properties.District.eq(city));
        conditionList.add(CNCityEntityDao.Properties.City.eq(district));

        List<CNCityEntity> entityList;
        for (WhereCondition c : conditionList) {
            try {
                entityList = dao.queryBuilder().where(c).list();
            } catch (Exception e) {
                entityList = null;
            }
            if (entityList != null && entityList.size() > 0) {
                return entityList.get(0).toCNCity();
            }
        }

        return null;
    }

    public static List<CNCity> fuzzySearchCNCity(SQLiteDatabase database, String name) {
        if (TextUtils.isEmpty(name)) {
            return null;
        }

        CNCityEntityDao dao = new DaoMaster(database)
                .newSession()
                .getCNCityEntityDao();

        QueryBuilder<CNCityEntity> builder = dao.queryBuilder();
        builder.whereOr(
                CNCityEntityDao.Properties.District.like("%" + name + "%"),
                CNCityEntityDao.Properties.City.like("%" + name + "%")
        );

        List<CNCity> cityList = new ArrayList<>();
        try {
            List<CNCityEntity> entityList = builder.list();
            if (entityList != null && entityList.size() > 0) {
                for (int i = 0; i < entityList.size(); i ++) {
                    cityList.add(entityList.get(i).toCNCity());
                }
            }
        } catch (Exception ignore) {

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
