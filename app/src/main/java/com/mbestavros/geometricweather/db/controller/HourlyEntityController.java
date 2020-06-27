package com.mbestavros.geometricweather.db.controller;

import androidx.annotation.NonNull;

import java.util.List;

import com.mbestavros.geometricweather.basic.model.option.provider.WeatherSource;
import com.mbestavros.geometricweather.db.entity.DaoSession;
import com.mbestavros.geometricweather.db.entity.HourlyEntity;
import com.mbestavros.geometricweather.db.entity.HourlyEntityDao;
import com.mbestavros.geometricweather.db.propertyConverter.WeatherSourceConverter;

public class HourlyEntityController extends AbsEntityController<HourlyEntity> {
    
    public HourlyEntityController(DaoSession session) {
        super(session);
    }

    // insert.

    public void insertHourlyList(@NonNull String cityId, @NonNull WeatherSource source,
                                 @NonNull List<HourlyEntity> entityList) {
        deleteHourlyEntityList(cityId, source);
        getSession().getHourlyEntityDao().insertInTx(entityList);
        getSession().clear();
    }

    // delete.

    public void deleteHourlyEntityList(@NonNull String cityId, @NonNull WeatherSource source) {
        getSession().getHourlyEntityDao().deleteInTx(selectHourlyEntityList(cityId, source));
        getSession().clear();
    }

    // select.

    public List<HourlyEntity> selectHourlyEntityList(@NonNull String cityId, @NonNull WeatherSource source) {
        return getNonNullList(
                getSession().getHourlyEntityDao()
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
