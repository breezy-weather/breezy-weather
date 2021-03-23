package wangdaye.com.geometricweather.db.controllers;

import androidx.annotation.NonNull;

import java.util.List;

import wangdaye.com.geometricweather.common.basic.models.options.provider.WeatherSource;
import wangdaye.com.geometricweather.db.entities.DaoSession;
import wangdaye.com.geometricweather.db.entities.HourlyEntity;
import wangdaye.com.geometricweather.db.entities.HourlyEntityDao;
import wangdaye.com.geometricweather.db.converters.WeatherSourceConverter;

public class HourlyEntityController extends AbsEntityController {

    // insert.

    public static void insertHourlyList(@NonNull DaoSession session,
                                        @NonNull List<HourlyEntity> entityList) {
        session.getHourlyEntityDao().insertInTx(entityList);
    }

    // delete.

    public static void deleteHourlyEntityList(@NonNull DaoSession session,
                                              @NonNull List<HourlyEntity> entityList) {
        session.getHourlyEntityDao().deleteInTx(entityList);
    }

    // select.

    public static List<HourlyEntity> selectHourlyEntityList(@NonNull DaoSession session,
                                                            @NonNull String cityId,
                                                            @NonNull WeatherSource source) {
        return getNonNullList(
                session.getHourlyEntityDao()
                        .queryBuilder()
                        .where(
                                HourlyEntityDao.Properties.CityId.eq(cityId),
                                HourlyEntityDao.Properties.WeatherSource.eq(
                                        new WeatherSourceConverter().convertToDatabaseValue(source)
                                )
                        ).list()
        );
    }
}
