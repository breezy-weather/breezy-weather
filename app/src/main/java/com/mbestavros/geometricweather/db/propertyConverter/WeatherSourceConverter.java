package com.mbestavros.geometricweather.db.propertyConverter;

import org.greenrobot.greendao.converter.PropertyConverter;

import com.mbestavros.geometricweather.basic.model.option.provider.WeatherSource;

public class WeatherSourceConverter implements PropertyConverter<WeatherSource, String> {

    @Override
    public WeatherSource convertToEntityProperty(String databaseValue) {
        return WeatherSource.valueOf(databaseValue);
    }

    @Override
    public String convertToDatabaseValue(WeatherSource entityProperty) {
        return entityProperty.name();
    }
}
