package wangdaye.com.geometricweather.data.entity.table;

import android.database.sqlite.SQLiteDatabase;
import android.text.TextUtils;

import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Id;
import org.greenrobot.greendao.query.QueryBuilder;

import java.util.ArrayList;
import java.util.List;

import wangdaye.com.geometricweather.data.entity.model.CNCityList;
import wangdaye.com.geometricweather.utils.LanguageUtils;

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

    private static CNCityEntity buildCNCityEntity(CNCityList.CNCity city) {
        CNCityEntity entity = new CNCityEntity();
        entity.province = city.getProvince();
        entity.city = city.getCity();
        entity.district = city.getDistrict();
        entity.lat = city.getLatitude();
        entity.lon = city.getLongitude();
        entity.requestKey = city.getCityId();
        return entity;
    }

    private static String formatLocationString(String str) {
        if (TextUtils.isEmpty(str)) {
            return "";
        }

        if (str.endsWith("地区")) {
            return str.substring(0, str.length() - 2);
        }
        if (str.endsWith("区")
                && !str.endsWith("新区")
                && !str.endsWith("矿区")
                && !str.endsWith("郊区")
                && !str.endsWith("风景区")
                && !str.endsWith("东区")
                && !str.endsWith("西区")) {
            return str.substring(0, str.length() - 1);
        }
        if (str.endsWith("县")
                && str.length() != 2
                && !str.endsWith("通化县")
                && !str.endsWith("本溪县")
                && !str.endsWith("辽阳县")
                && !str.endsWith("建平县")
                && !str.endsWith("承德县")
                && !str.endsWith("大同县")
                && !str.endsWith("五台县")
                && !str.endsWith("乌鲁木齐县")
                && !str.endsWith("伊宁县")
                && !str.endsWith("南昌县")
                && !str.endsWith("上饶县")
                && !str.endsWith("吉安县")
                && !str.endsWith("长沙县")
                && !str.endsWith("衡阳县")
                && !str.endsWith("邵阳县")
                && !str.endsWith("宜宾县")) {
            return str.substring(0, str.length() - 1);
        }

        if (str.endsWith("市")
                && !str.endsWith("新市")
                && !str.endsWith("沙市")
                && !str.endsWith("津市")
                && !str.endsWith("芒市")
                && !str.endsWith("西市")
                && !str.endsWith("峨眉山市")) {
            return str.substring(0, str.length() - 1);
        }
        if (str.endsWith("回族自治州")
                || str.endsWith("藏族自治州")
                || str.endsWith("彝族自治州")
                || str.endsWith("白族自治州")
                || str.endsWith("傣族自治州")
                || str.endsWith("蒙古自治州")) {
            return str.substring(0, str.length() - 5);
        }
        if (str.endsWith("朝鲜族自治州")
                || str.endsWith("哈萨克自治州")
                || str.endsWith("傈僳族自治州")
                || str.endsWith("蒙古族自治州")) {
            return str.substring(0, str.length() - 6);
        }
        if (str.endsWith("哈萨克族自治州")
                || str.endsWith("苗族侗族自治州")
                || str.endsWith("藏族羌族自治州")
                || str.endsWith("壮族苗族自治州")
                || str.endsWith("柯尔克孜自治州")) {
            return str.substring(0, str.length() - 7);
        }
        if (str.endsWith("布依族苗族自治州")
                || str.endsWith("土家族苗族自治州")
                || str.endsWith("蒙古族藏族自治州")
                || str.endsWith("柯尔克孜族自治州")
                || str.endsWith("傣族景颇族自治州")
                || str.endsWith("哈尼族彝族自治州")) {
            return str.substring(0, str.length() - 8);
        }
        if (str.endsWith("自治州")) {
            return str.substring(0, str.length() - 3);
        }

        if (str.endsWith("省")) {
            return str.substring(0, str.length() - 1);
        }
        if (str.endsWith("壮族自治区") || str.endsWith("回族自治区")) {
            return str.substring(0, str.length() - 5);
        }
        if (str.endsWith("维吾尔自治区")) {
            return str.substring(0, str.length() - 6);
        }
        if (str.endsWith("维吾尔族自治区")) {
            return str.substring(0, str.length() - 7);
        }
        if (str.endsWith("自治区")) {
            return str.substring(0, str.length() - 3);
        }
        return str;
    }

    public static void insertCNCityList(SQLiteDatabase database, CNCityList list) {
        if (list == null || list.getCities() == null || list.getCities().size() == 0) {
            return;
        }

        List<CNCityEntity> entityList = new ArrayList<>(list.getCities().size());
        for (int i = 0; i < list.getCities().size(); i ++) {
            entityList.add(buildCNCityEntity(list.getCities().get(i)));
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

        name = formatLocationString(convertChinese(name));

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
        district = formatLocationString(convertChinese(district));
        city = formatLocationString(convertChinese(city));
        province = formatLocationString(convertChinese(province));

        CNCityEntityDao dao = new DaoMaster(database)
                .newSession()
                .getCNCityEntityDao();

        List<WhereCondition> conditionList = new ArrayList<>();
        conditionList.add(
                dao.queryBuilder().and(
                        CNCityEntityDao.Properties.District.eq(district),
                        CNCityEntityDao.Properties.City.eq(city)));
        conditionList.add(
                dao.queryBuilder().and(
                        CNCityEntityDao.Properties.District.eq(district),
                        CNCityEntityDao.Properties.Province.eq(province)));
        conditionList.add(
                dao.queryBuilder().and(
                        CNCityEntityDao.Properties.City.eq(city),
                        CNCityEntityDao.Properties.Province.eq(province)));
        conditionList.add(CNCityEntityDao.Properties.City.eq(city));
        conditionList.add(
                dao.queryBuilder().and(
                        CNCityEntityDao.Properties.District.eq(city),
                        CNCityEntityDao.Properties.Province.eq(province)));
        conditionList.add(
                dao.queryBuilder().and(
                        CNCityEntityDao.Properties.District.eq(city),
                        CNCityEntityDao.Properties.City.eq(province)));
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
                return CNCityList.CNCity.buildCNCity(entityList.get(0));
            }
        }

        return null;
    }

    public static List<CNCityList.CNCity> fuzzySearchCNCity(SQLiteDatabase database, String name) {
        if (TextUtils.isEmpty(name)) {
            return null;
        }

        name = formatLocationString(convertChinese(name));

        CNCityEntityDao dao = new DaoMaster(database)
                .newSession()
                .getCNCityEntityDao();

        QueryBuilder<CNCityEntity> builder = dao.queryBuilder();
        builder.whereOr(
                CNCityEntityDao.Properties.District.like("%" + name + "%"),
                CNCityEntityDao.Properties.City.like("%" + name + "%"),
                CNCityEntityDao.Properties.Province.like("%" + name + "%"));

        List<CNCityList.CNCity> cityList = new ArrayList<>();
        try {
            List<CNCityEntity> entityList = builder.list();
            if (entityList != null && entityList.size() > 0) {
                for (int i = 0; i < entityList.size(); i ++) {
                    cityList.add(CNCityList.CNCity.buildCNCity(entityList.get(i)));
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

    private static String convertChinese(String text) {
        try {
            return LanguageUtils.traditionalToSimplified(text);
        } catch (Exception e) {
            return text;
        }
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
