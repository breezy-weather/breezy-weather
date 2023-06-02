package wangdaye.com.geometricweather.db.converters;

import android.text.TextUtils;

import java.util.TimeZone;

import io.objectbox.converter.PropertyConverter;

public class TimeZoneConverter implements PropertyConverter<TimeZone, String> {

    @Override
    public TimeZone convertToEntityProperty(String databaseValue) {
        if (TextUtils.isEmpty(databaseValue)) {
            return null;
        } else {
            return TimeZone.getTimeZone(databaseValue);
        }
    }

    @Override
    public String convertToDatabaseValue(TimeZone entityProperty) {
        return entityProperty.getID();
    }
}
