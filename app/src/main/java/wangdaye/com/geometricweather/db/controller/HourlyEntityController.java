package wangdaye.com.geometricweather.db.controller;

import androidx.annotation.NonNull;

import java.util.List;

import wangdaye.com.geometricweather.db.entity.DaoSession;
import wangdaye.com.geometricweather.db.entity.HourlyEntity;
import wangdaye.com.geometricweather.db.entity.HourlyEntityDao;

public class HourlyEntityController extends AbsEntityController<HourlyEntity> {
    
    public HourlyEntityController(DaoSession session) {
        super(session);
    }

    // insert.

    public void insertHourlyList(@NonNull String cityId, @NonNull List<HourlyEntity> entityList) {
        if (entityList.size() != 0) {
            deleteHourlyEntityList(cityId);
            getSession().getHourlyEntityDao().insertInTx(entityList);
            getSession().clear();
        }
    }

    // delete.

    public void deleteHourlyEntityList(@NonNull String cityId) {
        getSession().getHourlyEntityDao().deleteInTx(selectHourlyEntityList(cityId));
        getSession().clear();
    }

    // select.

    public List<HourlyEntity> selectHourlyEntityList(@NonNull String cityId) {
        return getNonNullList(
                getSession().getHourlyEntityDao()
                        .queryBuilder()
                        .where(HourlyEntityDao.Properties.CityId.eq(cityId))
                        .list()
        );
    }
}
