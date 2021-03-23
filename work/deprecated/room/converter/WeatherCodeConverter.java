package wangdaye.com.geometricweather.room.converter;

import androidx.room.TypeConverter;

import wangdaye.com.geometricweather.basic.models.weather.WeatherCode;

public class WeatherCodeConverter {

    @TypeConverter
    public static WeatherCode convertToEntityProperty(String databaseValue) {
        return WeatherCode.valueOf(databaseValue);
    }

    @TypeConverter
    public static String convertToDatabaseValue(WeatherCode entityProperty) {
        return entityProperty.name();
    }
}
