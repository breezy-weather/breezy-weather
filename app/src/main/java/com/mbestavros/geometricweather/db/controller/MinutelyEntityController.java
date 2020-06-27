package com.mbestavros.geometricweather.db.controller;

import androidx.annotation.NonNull;

import java.util.List;

import com.mbestavros.geometricweather.basic.model.option.provider.WeatherSource;
import com.mbestavros.geometricweather.db.entity.DaoSession;
import com.mbestavros.geometricweather.db.entity.MinutelyEntity;
import com.mbestavros.geometricweather.db.entity.MinutelyEntityDao;
import com.mbestavros.geometricweather.db.propertyConverter.WeatherSourceConverter;

public class MinutelyEntityController extends AbsEntityController<MinutelyEntity> {
    
    public MinutelyEntityController(DaoSession session) {
        super(session);
    }

    // insert.

    public void insertMinutelyList(@NonNull String cityId, @NonNull WeatherSource source,
                                   @NonNull List<MinutelyEntity> entityList) {
        deleteMinutelyEntityList(cityId, source);
        getSession().getMinutelyEntityDao().insertInTx(entityList);
        getSession().clear();
    }

    // delete.

    public void deleteMinutelyEntityList(@NonNull String cityId, @NonNull WeatherSource source) {
        getSession().getMinutelyEntityDao().deleteInTx(selectMinutelyEntityList(cityId, source));
        getSession().clear();
    }

    // select.

    public List<MinutelyEntity> selectMinutelyEntityList(@NonNull String cityId, @NonNull WeatherSource source) {
        return getNonNullList(
                getSession().getMinutelyEntityDao()
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
