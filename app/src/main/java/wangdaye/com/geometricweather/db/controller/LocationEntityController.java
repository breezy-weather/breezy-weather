package wangdaye.com.geometricweather.db.controller;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.List;

import wangdaye.com.geometricweather.db.entity.DaoSession;
import wangdaye.com.geometricweather.db.entity.LocationEntity;
import wangdaye.com.geometricweather.db.entity.LocationEntityDao;

public class LocationEntityController extends AbsEntityController<LocationEntity> {
    
    public LocationEntityController(DaoSession session) {
        super(session);
    }

    // insert.

    public void insertOrUpdateLocationEntity(@NonNull LocationEntity entity) {
        getSession().getLocationEntityDao().insertOrReplace(entity);
        getSession().clear();
    }

    public void insertOrUpdateLocationEntityList(@NonNull List<LocationEntity> entityList) {
        if (entityList.size() != 0) {
            getSession().getLocationEntityDao().insertOrReplaceInTx(entityList);
            getSession().clear();
        }
    }

    // delete.

    public void deleteLocationEntity(@NonNull LocationEntity entity) {
        getSession().getLocationEntityDao().deleteByKey(entity.formattedId);
        getSession().clear();
    }

    // select.

    @Nullable
    public LocationEntity selectLocationEntity(@NonNull String formattedId) {
        List<LocationEntity> entityList = getSession().getLocationEntityDao()
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
    public List<LocationEntity> selectLocationEntityList() {
        return getNonNullList(
                getSession().getLocationEntityDao()
                        .queryBuilder()
                        .orderAsc(LocationEntityDao.Properties.Sequence)
                        .list()
        );
    }

    public int countLocationEntity() {
        return (int) getSession().getLocationEntityDao()
                .queryBuilder()
                .count();
    }
}
