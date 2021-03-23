package wangdaye.com.geometricweather.db.controllers;

import androidx.annotation.NonNull;

import java.util.List;

import wangdaye.com.geometricweather.common.basic.models.options.provider.WeatherSource;
import wangdaye.com.geometricweather.db.entities.AlertEntity;
import wangdaye.com.geometricweather.db.entities.AlertEntityDao;
import wangdaye.com.geometricweather.db.entities.DaoSession;
import wangdaye.com.geometricweather.db.converters.WeatherSourceConverter;

public class AlertEntityController extends AbsEntityController {

    // insert.

    public static void insertAlertList(@NonNull DaoSession session,
                                @NonNull List<AlertEntity> entityList) {
        session.getAlertEntityDao().insertInTx(entityList);
    }

    // delete.

    public static void deleteAlertList(@NonNull DaoSession session,
                                @NonNull List<AlertEntity> entityList) {
        session.getAlertEntityDao().deleteInTx(entityList);
    }

    // search.

    public static List<AlertEntity> selectLocationAlertEntity(@NonNull DaoSession session,
                                                              @NonNull String cityId,
                                                              @NonNull WeatherSource source) {
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
