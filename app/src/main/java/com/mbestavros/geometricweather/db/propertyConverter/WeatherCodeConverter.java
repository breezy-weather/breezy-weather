package com.mbestavros.geometricweather.db.propertyConverter;

import org.greenrobot.greendao.converter.PropertyConverter;

import com.mbestavros.geometricweather.basic.model.weather.WeatherCode;

public class WeatherCodeConverter implements PropertyConverter<WeatherCode, String> {

    @Override
    public WeatherCode convertToEntityProperty(String databaseValue) {
        return WeatherCode.valueOf(databaseValue);
    }

    @Override
    public String convertToDatabaseValue(WeatherCode entityProperty) {
        return entityProperty.name();
    }
}
