package wangdaye.com.geometricweather.db.controller;

import android.text.TextUtils;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import org.greenrobot.greendao.query.QueryBuilder;
import org.greenrobot.greendao.query.WhereCondition;

import java.util.ArrayList;
import java.util.List;

import wangdaye.com.geometricweather.db.entity.ChineseCityEntityDao;
import wangdaye.com.geometricweather.db.entity.ChineseCityEntity;
import wangdaye.com.geometricweather.db.entity.DaoSession;

public class ChineseCityEntityController extends AbsEntityController<ChineseCityEntity> {

    // insert.

    public void insertChineseCityEntityList(@NonNull DaoSession session,
                                            @NonNull List<ChineseCityEntity> entityList) {
        if (entityList.size() != 0) {
            session.getChineseCityEntityDao().insertInTx(entityList);
        }
    }

    // delete.

    public void deleteChineseCityEntityList(@NonNull DaoSession session) {
        session.getChineseCityEntityDao().deleteAll();
    }

    // select.

    @Nullable
    public ChineseCityEntity selectChineseCityEntity(@NonNull DaoSession session,
                                                     @NonNull String name) {
        if (TextUtils.isEmpty(name)) {
            return null;
        }

        ChineseCityEntityDao dao = session.getChineseCityEntityDao();

        QueryBuilder<ChineseCityEntity> builder = dao.queryBuilder();
        builder.whereOr(
                ChineseCityEntityDao.Properties.District.eq(name),
                ChineseCityEntityDao.Properties.City.eq(name)
        );

        List<ChineseCityEntity> entityList = builder.list();
        if (entityList == null || entityList.size() <= 0) {
            return null;
        } else {
            return entityList.get(0);
        }
    }

    @Nullable
    public ChineseCityEntity selectChineseCityEntity(@NonNull DaoSession session,
                                                     @NonNull String province,
                                                     @NonNull String city,
                                                     @NonNull String district) {
        ChineseCityEntityDao dao = session.getChineseCityEntityDao();

        List<WhereCondition> conditionList = new ArrayList<>();
        conditionList.add(
                dao.queryBuilder().and(
                        ChineseCityEntityDao.Properties.District.eq(district),
                        ChineseCityEntityDao.Properties.City.eq(city)
                )
        );
        conditionList.add(
                dao.queryBuilder().and(
                        ChineseCityEntityDao.Properties.District.eq(district),
                        ChineseCityEntityDao.Properties.Province.eq(province)
                )
        );
        conditionList.add(
                dao.queryBuilder().and(
                        ChineseCityEntityDao.Properties.City.eq(city),
                        ChineseCityEntityDao.Properties.Province.eq(province)
                )
        );
        conditionList.add(ChineseCityEntityDao.Properties.City.eq(city));
        conditionList.add(
                dao.queryBuilder().and(
                        ChineseCityEntityDao.Properties.District.eq(city),
                        ChineseCityEntityDao.Properties.Province.eq(province)
                )
        );
        conditionList.add(
                dao.queryBuilder().and(
                        ChineseCityEntityDao.Properties.District.eq(city),
                        ChineseCityEntityDao.Properties.City.eq(province)
                )
        );
        conditionList.add(ChineseCityEntityDao.Properties.District.eq(city));
        conditionList.add(ChineseCityEntityDao.Properties.City.eq(district));

        List<ChineseCityEntity> entityList;
        for (WhereCondition c : conditionList) {
            try {
                entityList = dao.queryBuilder().where(c).list();
            } catch (Exception e) {
                entityList = null;
            }
            if (entityList != null && entityList.size() > 0) {
                return entityList.get(0);
            }
        }

        return null;
    }

    @Nullable
    public ChineseCityEntity selectChineseCityEntity(@NonNull DaoSession session,
                                                     float latitude, float longitude) {
        List<ChineseCityEntity> entityList = getNonNullList(
                session.getChineseCityEntityDao()
                        .queryBuilder()
                        .list()
        );

        int minIndex = -1;
        double minDistance = Double.MAX_VALUE;
        for (int i = 0; i < entityList.size(); i ++) {
            double distance = Math.pow(latitude - Double.parseDouble(entityList.get(i).latitude), 2)
                    + Math.pow(longitude - Double.parseDouble(entityList.get(i).longitude), 2);
            if (distance < minDistance) {
                minIndex = i;
                minDistance = distance;
            }
        }
        if (0 <= minIndex && minIndex < entityList.size()) {
            return entityList.get(minIndex);
        } else {
            return null;
        }
    }

    @NonNull
    public List<ChineseCityEntity> selectChineseCityEntityList(@NonNull DaoSession session,
                                                               @NonNull String name) {
        if (TextUtils.isEmpty(name)) {
            return new ArrayList<>();
        }

        ChineseCityEntityDao dao = session.getChineseCityEntityDao();

        QueryBuilder<ChineseCityEntity> builder = dao.queryBuilder();
        builder.whereOr(
                ChineseCityEntityDao.Properties.District.like("%" + name + "%"),
                ChineseCityEntityDao.Properties.City.like("%" + name + "%")
        );

        return getNonNullList(builder.list());
    }

    public int countChineseCityEntity(@NonNull DaoSession session) {
        return (int) session.getChineseCityEntityDao().count();
    }
}
