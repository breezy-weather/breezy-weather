package wangdaye.com.geometricweather.db.converters;

import io.objectbox.converter.PropertyConverter;
import wangdaye.com.geometricweather.common.basic.models.weather.WindDegree;

public class WindDegreeConverter implements PropertyConverter<WindDegree, Float> {

    @Override
    public WindDegree convertToEntityProperty(Float databaseValue) {
        if (databaseValue == null) {
            return null;
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
