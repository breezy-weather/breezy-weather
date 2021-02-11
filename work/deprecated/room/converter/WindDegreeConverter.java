package wangdaye.com.geometricweather.room.converter;

import androidx.room.TypeConverter;

import wangdaye.com.geometricweather.basic.models.weather.WindDegree;

public class WindDegreeConverter {

    @TypeConverter
    public static WindDegree convertToEntityProperty(Float databaseValue) {
        if (databaseValue == null) {
            return new WindDegree(-1, true);
        } else {
            return new WindDegree(databaseValue, false);
        }
    }

    @TypeConverter
    public static Float convertToDatabaseValue(WindDegree entityProperty) {
        if (entityProperty.isNoDirection()) {
            return null;
        } else {
            return entityProperty.getDegree();
        }
    }
}
