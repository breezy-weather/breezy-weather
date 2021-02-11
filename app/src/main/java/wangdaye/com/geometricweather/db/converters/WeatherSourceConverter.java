package wangdaye.com.geometricweather.db.converters;

import org.greenrobot.greendao.converter.PropertyConverter;

import wangdaye.com.geometricweather.basic.models.options.provider.WeatherSource;

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
