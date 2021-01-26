package wangdaye.com.geometricweather.room.converter;

import androidx.room.TypeConverter;

import java.util.TimeZone;

public class TimeZoneConverter {

    @TypeConverter
    public static TimeZone convertToEntityProperty(String databaseValue) {
        return TimeZone.getTimeZone(databaseValue);
    }

    @TypeConverter
    public static String convertToDatabaseValue(TimeZone entityProperty) {
        return entityProperty.getID();
    }
}
