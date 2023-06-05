package wangdaye.com.geometricweather.db.converters

import io.objectbox.converter.PropertyConverter
import wangdaye.com.geometricweather.common.basic.models.weather.WeatherCode
import wangdaye.com.geometricweather.common.basic.models.weather.WeatherCode.Companion.getInstance

class WeatherCodeConverter : PropertyConverter<WeatherCode?, String?> {
    override fun convertToEntityProperty(databaseValue: String?): WeatherCode? =
        if (databaseValue.isNullOrEmpty()) null else getInstance(databaseValue)

    override fun convertToDatabaseValue(entityProperty: WeatherCode?): String? =
        entityProperty?.id
}
