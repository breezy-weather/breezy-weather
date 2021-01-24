package wangdaye.com.geometricweather.db.controller;

import androidx.annotation.NonNull;

import java.util.List;

import wangdaye.com.geometricweather.basic.model.option.provider.WeatherSource;
import wangdaye.com.geometricweather.db.entity.AlertEntity;
import wangdaye.com.geometricweather.db.entity.AlertEntityDao;
import wangdaye.com.geometricweather.db.entity.DaoSession;
import wangdaye.com.geometricweather.db.propertyConverter.WeatherSourceConverter;

public class AlertEntityController extends AbsEntityController<AlertEntity> {

    // insert.

    public void insertAlertList(@NonNull DaoSession session,
                                @NonNull List<AlertEntity> entityList) {
        session.getAlertEntityDao().insertInTx(entityList);
    }

    // delete.

    public void deleteAlertList(@NonNull DaoSession session,
                                @NonNull List<AlertEntity> entityList) {
        session.getAlertEntityDao().deleteInTx(entityList);
    }

    // search.

    public List<AlertEntity> selectLocationAlertEntity(@NonNull DaoSession session,
                                                       @NonNull String cityId, @NonNull WeatherSource source) {
        return getNonNullList(
                session.getAlertEntityDao()
                        .queryBuilder()
                        .where(
                                AlertEntityDao.Properties.CityId.eq(cityId),
                                AlertEntityDao.Properties.WeatherSource.eq(
                                        new WeatherSourceConverter().convertToDatabaseValue(source)
                                )
                        ).list()
        );
    }
}
