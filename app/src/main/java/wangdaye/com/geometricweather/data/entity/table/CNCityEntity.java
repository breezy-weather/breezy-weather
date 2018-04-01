package wangdaye.com.geometricweather.data.entity.table;

import android.database.sqlite.SQLiteDatabase;
import android.text.TextUtils;

import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Id;
import org.greenrobot.greendao.annotation.Generated;
import org.greenrobot.greendao.query.QueryBuilder;

import java.util.ArrayList;
import java.util.List;

import wangdaye.com.geometricweather.data.entity.model.CNCityList;

/**
 * CN city entity.
 * */

@Entity
public class CNCityEntity {

    @Id
    public Long id;

    public String cityName;
    public String cityId;
    public String province;

    @Generated(hash = 1006448496)
    public CNCityEntity(Long id, String cityName, String cityId, String province) {
        this.id = id;
        this.cityName = cityName;
        this.cityId = cityId;
        this.province = province;
    }

    @Generated(hash = 744669023)
    public CNCityEntity() {
    }

    private static CNCityEntity buildCNCityEntity(CNCityList.CNCity city) {
        CNCityEntity entity = new CNCityEntity();
        entity.cityName = city.name;
        entity.cityId = city.id;
        entity.province = city.province_name;
        return entity;
    }

    public static void insertCNCityList(SQLiteDatabase database, CNCityList list) {
        if (list == null || list.citylist == null || list.citylist.size() == 0) {
            return;
        }

        List<CNCityEntity> entityList = new ArrayList<>(list.citylist.size());
        for (int i = 0; i < list.citylist.size(); i ++) {
            entityList.add(buildCNCityEntity(list.citylist.get(i)));
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
        builder.where(CNCityEntityDao.Properties.CityName.eq(name));

        List<CNCityEntity> entityList = builder.list();
        if (entityList == null || entityList.size() <= 0) {
            return null;
        } else {
            return CNCityList.CNCity.buildCNCity(entityList.get(0));
        }
    }

    public static CNCityList.CNCity fuzzySearchCNCity(SQLiteDatabase database, String name) {
        if (TextUtils.isEmpty(name)) {
            return null;
        }

        CNCityEntityDao dao = new DaoMaster(database)
                .newSession()
                .getCNCityEntityDao();

        QueryBuilder<CNCityEntity> builder = dao.queryBuilder();
        builder.where(CNCityEntityDao.Properties.CityName.like("%" + name + "%"));

        List<CNCityEntity> entityList = builder.list();
        if (entityList == null || entityList.size() <= 0) {
            return null;
        } else {
            return CNCityList.CNCity.buildCNCity(entityList.get(0));
        }
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

    public String getCityName() {
        return this.cityName;
    }

    public void setCityName(String cityName) {
        this.cityName = cityName;
    }

    public String getCityId() {
        return this.cityId;
    }

    public void setCityId(String cityId) {
        this.cityId = cityId;
    }

    public String getProvince() {
        return this.province;
    }

    public void setProvince(String province) {
        this.province = province;
    }
}
