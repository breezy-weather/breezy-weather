package wangdaye.com.geometricweather.db.controller;

import androidx.annotation.NonNull;

import java.util.List;

import wangdaye.com.geometricweather.basic.model.option.provider.WeatherSource;
import wangdaye.com.geometricweather.db.entity.DailyEntity;
import wangdaye.com.geometricweather.db.entity.DailyEntityDao;
import wangdaye.com.geometricweather.db.entity.DaoSession;
import wangdaye.com.geometricweather.db.propertyConverter.WeatherSourceConverter;

public class DailyEntityController extends AbsEntityController<DailyEntity> {
    
    public DailyEntityController(DaoSession session) {
        super(session);
    }

    // insert.

    public void insertDailyList(@NonNull String cityId, @NonNull WeatherSource source, @NonNull List<DailyEntity> entityList) {
        if (entityList.size() != 0) {
            deleteDailyEntityList(cityId, source);
            getSession().getDailyEntityDao().insertInTx(entityList);
            getSession().clear();
        }
    }

    // delete.

    public void deleteDailyEntityList( @NonNull String cityId, @NonNull WeatherSource source) {
        getSession().getDailyEntityDao().deleteInTx(selectDailyEntityList(cityId, source));
        getSession().clear();
    }

    // select.

    @NonNull
    public List<DailyEntity> selectDailyEntityList( @NonNull String cityId, @NonNull WeatherSource source) {
        return getNonNullList(
                getSession().getDailyEntityDao()
                        .queryBuilder()
                        .where(
                                DailyEntityDao.Properties.CityId.eq(cityId),
                                DailyEntityDao.Properties.WeatherSource.eq(
                                        new WeatherSourceConverter().convertToDatabaseValue(source)
                                )
                        ).list()
        );
    }
}
