package wangdaye.com.geometricweather.db.controller;

import androidx.annotation.NonNull;

import java.util.List;

import wangdaye.com.geometricweather.basic.model.option.provider.WeatherSource;
import wangdaye.com.geometricweather.db.entity.DaoSession;
import wangdaye.com.geometricweather.db.entity.HourlyEntity;
import wangdaye.com.geometricweather.db.entity.HourlyEntityDao;
import wangdaye.com.geometricweather.db.propertyConverter.WeatherSourceConverter;

public class HourlyEntityController extends AbsEntityController<HourlyEntity> {

    // insert.

    public void insertHourlyList(@NonNull DaoSession session,
                                 @NonNull List<HourlyEntity> entityList) {
        session.getHourlyEntityDao().insertInTx(entityList);
    }

    // delete.

    public void deleteHourlyEntityList(@NonNull DaoSession session,
                                       @NonNull List<HourlyEntity> entityList) {
        session.getHourlyEntityDao().deleteInTx(entityList);
    }

    // select.

    public List<HourlyEntity> selectHourlyEntityList(@NonNull DaoSession session,
                                                     @NonNull String cityId, @NonNull WeatherSource source) {
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
