package wangdaye.com.geometricweather.room.converter;

import androidx.room.TypeConverter;

import java.util.Date;

public class DateConverter {

    @TypeConverter
    public static Date convertToEntityProperty(Long value) {
        return value == null ? null : new Date(value);
    }

    @TypeConverter
    public static Long convertToDatabaseValue(Date date) {
        return date == null ? null : date.getTime();
    }
}
