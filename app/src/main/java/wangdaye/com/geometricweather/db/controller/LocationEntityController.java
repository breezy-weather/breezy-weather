package wangdaye.com.geometricweather.db.controller;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.List;

import wangdaye.com.geometricweather.db.entity.DaoSession;
import wangdaye.com.geometricweather.db.entity.LocationEntity;
import wangdaye.com.geometricweather.db.entity.LocationEntityDao;

public class LocationEntityController extends AbsEntityController {

    // insert.

    public static void insertLocationEntity(@NonNull DaoSession session,
                                            @NonNull LocationEntity entity) {
        session.getLocationEntityDao().insert(entity);
    }

    public static void insertLocationEntityList(@NonNull DaoSession session,
                                                @NonNull List<LocationEntity> entityList) {
        if (entityList.size() != 0) {
            session.getLocationEntityDao().insertInTx(entityList);
        }
    }

    // delete.

    public static void deleteLocationEntity(@NonNull DaoSession session,
                                            @NonNull LocationEntity entity) {
        session.getLocationEntityDao().deleteByKey(entity.formattedId);
    }

    public static void deleteLocationEntityList(@NonNull DaoSession session) {
        session.getLocationEntityDao().deleteAll();
    }

    // update.

    public static void updateLocationEntity(@NonNull DaoSession session,
                                            @NonNull LocationEntity entity) {
        session.getLocationEntityDao().update(entity);
    }

    // select.

    @Nullable
    public static LocationEntity selectLocationEntity(@NonNull DaoSession session,
                                                      @NonNull String formattedId) {
        List<LocationEntity> entityList = session.getLocationEntityDao()
                .queryBuilder()
                .where(LocationEntityDao.Properties.FormattedId.eq(formattedId))
                .list();
        if (entityList == null || entityList.size() <= 0) {
            return null;
        } else {
            return entityList.get(0);
        }
    }

    @NonNull
    public static List<LocationEntity> selectLocationEntityList(@NonNull DaoSession session) {
        return getNonNullList(
                session.getLocationEntityDao()
                        .queryBuilder()
                        .list()
        );
    }

    public static int countLocationEntity(@NonNull DaoSession session) {
        return (int) session.getLocationEntityDao()
                .queryBuilder()
                .count();
    }
}
