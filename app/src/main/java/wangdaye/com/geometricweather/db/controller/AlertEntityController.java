package wangdaye.com.geometricweather.db.controller;

import androidx.annotation.NonNull;

import java.util.List;

import wangdaye.com.geometricweather.db.entity.AlertEntity;
import wangdaye.com.geometricweather.db.entity.AlertEntityDao;
import wangdaye.com.geometricweather.db.entity.DaoSession;

public class AlertEntityController extends AbsEntityController<AlertEntity> {

    public AlertEntityController(DaoSession session) {
        super(session);
    }

    // insert.

    public void insertAlertList(@NonNull String cityId, @NonNull List<AlertEntity> entityList) {
        if (entityList.size() != 0) {
            deleteAlertList(cityId);
            getSession().getAlertEntityDao().insertInTx(entityList);
            getSession().clear();
        }
    }

    // delete.

    public void deleteAlertList(@NonNull String cityId) {
        getSession().getAlertEntityDao().deleteInTx(searchLocationAlarmEntity(cityId));
        getSession().clear();
    }

    // search.

    public List<AlertEntity> searchLocationAlarmEntity(@NonNull String cityId) {
        return getNonNullList(
                getSession().getAlertEntityDao()
                        .queryBuilder()
                        .where(AlertEntityDao.Properties.CityId.eq(cityId))
                        .list()
        );
    }
}
