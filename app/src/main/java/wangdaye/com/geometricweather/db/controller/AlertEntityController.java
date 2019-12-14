package wangdaye.com.geometricweather.db.controller;

import androidx.annotation.NonNull;

import java.util.List;

import wangdaye.com.geometricweather.basic.model.option.provider.WeatherSource;
import wangdaye.com.geometricweather.db.entity.AlertEntity;
import wangdaye.com.geometricweather.db.entity.AlertEntityDao;
import wangdaye.com.geometricweather.db.entity.DaoSession;
import wangdaye.com.geometricweather.db.propertyConverter.WeatherSourceConverter;

public class AlertEntityController extends AbsEntityController<AlertEntity> {

    public AlertEntityController(DaoSession session) {
        super(session);
    }

    // insert.

    public void insertAlertList(@NonNull String cityId, @NonNull WeatherSource source,
                                @NonNull List<AlertEntity> entityList) {
        if (entityList.size() != 0) {
            deleteAlertList(cityId, source);
            getSession().getAlertEntityDao().insertInTx(entityList);
            getSession().clear();
        }
    }

    // delete.

    public void deleteAlertList(@NonNull String cityId, @NonNull WeatherSource source) {
        getSession().getAlertEntityDao().deleteInTx(searchLocationAlarmEntity(cityId, source));
        getSession().clear();
    }

    // search.

    public List<AlertEntity> searchLocationAlarmEntity(@NonNull String cityId, @NonNull WeatherSource source) {
        return getNonNullList(
                getSession().getAlertEntityDao()
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
