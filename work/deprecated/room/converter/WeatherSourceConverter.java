package wangdaye.com.geometricweather.room.converter;

import androidx.room.TypeConverter;

import wangdaye.com.geometricweather.basic.models.options.provider.WeatherSource;

public class WeatherSourceConverter {

    @TypeConverter
    public static WeatherSource convertToEntityProperty(String databaseValue) {
        return WeatherSource.valueOf(databaseValue);
    }

    @TypeConverter
    public static String convertToDatabaseValue(WeatherSource entityProperty) {
        return entityProperty.name();
    }
}
