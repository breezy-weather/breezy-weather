package wangdaye.com.geometricweather.db.controller;

import androidx.annotation.NonNull;

import java.util.List;

import wangdaye.com.geometricweather.db.entity.DailyEntity;
import wangdaye.com.geometricweather.db.entity.DailyEntityDao;
import wangdaye.com.geometricweather.db.entity.DaoSession;

public class DailyEntitiyController extends AbsEntityController<DailyEntity> {
    
    public DailyEntitiyController(DaoSession session) {
        super(session);
    }

    // insert.

    public void insertDailyList(@NonNull String cityId, @NonNull List<DailyEntity> entityList) {
        if (entityList.size() != 0) {
            deleteDailyEntityList(cityId);
            getSession().getDailyEntityDao().insertInTx(entityList);
            getSession().clear();
        }
    }

    // delete.

    public void deleteDailyEntityList( @NonNull String cityId) {
        getSession().getDailyEntityDao().deleteInTx(selectDailyEntityList(cityId));
        getSession().clear();
    }

    // select.

    @NonNull
    public List<DailyEntity> selectDailyEntityList( @NonNull String cityId) {
        return getNonNullList(
                getSession().getDailyEntityDao()
                        .queryBuilder()
                        .where(DailyEntityDao.Properties.CityId.eq(cityId))
                        .list()
        );
    }
}
