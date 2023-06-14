package org.breezyweather.db.converters

import io.objectbox.converter.PropertyConverter
import org.breezyweather.common.basic.models.options.provider.WeatherSource

class WeatherSourceConverter : PropertyConverter<WeatherSource, String?> {

    override fun convertToEntityProperty(databaseValue: String?) =
        // use get instance method but not getValue method.
        WeatherSource.getInstance(databaseValue)

    override fun convertToDatabaseValue(entityProperty: WeatherSource) =
        entityProperty.id
}