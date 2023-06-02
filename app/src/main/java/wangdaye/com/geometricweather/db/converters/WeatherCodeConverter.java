package wangdaye.com.geometricweather.db.converters;

import android.text.TextUtils;

import io.objectbox.converter.PropertyConverter;
import wangdaye.com.geometricweather.common.basic.models.weather.WeatherCode;

public class WeatherCodeConverter implements PropertyConverter<WeatherCode, String> {

    @Override
    public WeatherCode convertToEntityProperty(String databaseValue) {
        if (TextUtils.isEmpty(databaseValue)) {
            return null;
        } else {
            return WeatherCode.getInstance(databaseValue);
        }
    }

    @Override
    public String convertToDatabaseValue(WeatherCode entityProperty) {
        return entityProperty.getId();
    }
}
