package wangdaye.com.geometricweather.db.controller;

import androidx.annotation.NonNull;

import java.util.List;

import wangdaye.com.geometricweather.db.entity.DaoSession;
import wangdaye.com.geometricweather.db.entity.MinutelyEntity;
import wangdaye.com.geometricweather.db.entity.MinutelyEntityDao;

public class MinutelyEntityController extends AbsEntityController<MinutelyEntity> {
    
    public MinutelyEntityController(DaoSession session) {
        super(session);
    }

    // insert.

    public void insertMinutelyList(@NonNull String cityId, @NonNull List<MinutelyEntity> entityList) {
        if (entityList.size() != 0) {
            deleteMinutelyEntityList(cityId);
            getSession().getMinutelyEntityDao().insertInTx(entityList);
            getSession().clear();
        }
    }

    // delete.

    public void deleteMinutelyEntityList(@NonNull String cityId) {
        getSession().getMinutelyEntityDao().deleteInTx(selectMinutelyEntityList(cityId));
        getSession().clear();
    }

    // select.

    public List<MinutelyEntity> selectMinutelyEntityList(@NonNull String cityId) {
        return getNonNullList(
                getSession().getMinutelyEntityDao()
                        .queryBuilder()
                        .where(MinutelyEntityDao.Properties.CityId.eq(cityId))
                        .list()
        );
    }
}
