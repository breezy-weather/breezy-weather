package com.mbestavros.geometricweather.db.propertyConverter;

import org.greenrobot.greendao.converter.PropertyConverter;

import com.mbestavros.geometricweather.basic.model.weather.WindDegree;

public class WindDegreeConverter implements PropertyConverter<WindDegree, Float> {

    @Override
    public WindDegree convertToEntityProperty(Float databaseValue) {
        if (databaseValue == null) {
            return new WindDegree(-1, true);
        } else {
            return new WindDegree(databaseValue, false);
        }
    }

    @Override
    public Float convertToDatabaseValue(WindDegree entityProperty) {
        if (entityProperty.isNoDirection()) {
            return null;
        } else {
            return entityProperty.getDegree();
        }
    }
}
