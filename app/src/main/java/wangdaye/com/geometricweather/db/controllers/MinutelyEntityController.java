package wangdaye.com.geometricweather.db.controllers;

import androidx.annotation.NonNull;

import java.util.List;

import wangdaye.com.geometricweather.common.basic.models.options.provider.WeatherSource;
import wangdaye.com.geometricweather.db.converters.WeatherSourceConverter;
import wangdaye.com.geometricweather.db.entities.DaoSession;
import wangdaye.com.geometricweather.db.entities.MinutelyEntity;
import wangdaye.com.geometricweather.db.entities.MinutelyEntityDao;

public class MinutelyEntityController extends AbsEntityController {

    // insert.

    public static void insertMinutelyList(@NonNull DaoSession session,
                                          @NonNull List<MinutelyEntity> entityList) {
        session.getMinutelyEntityDao().insertInTx(entityList);
    }

    // delete.

    public static void deleteMinutelyEntityList(@NonNull DaoSession session,
                                                @NonNull List<MinutelyEntity> entityList) {
        session.getMinutelyEntityDao().deleteInTx(entityList);
    }

    // select.

    public static List<MinutelyEntity> selectMinutelyEntityList(@NonNull DaoSession session,
                                                                @NonNull String cityId, @NonNull WeatherSource source) {
        return getNonNullList(
                session.getMinutelyEntityDao()
                        .queryBuilder()
                        .where(
                                MinutelyEntityDao.Properties.CityId.eq(cityId),
                                MinutelyEntityDao.Properties.WeatherSource.eq(
                                        new WeatherSourceConverter().convertToDatabaseValue(source)
                                )
                        ).list()
        );
    }
}
