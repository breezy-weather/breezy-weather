package wangdaye.com.geometricweather.db.converters

import io.objectbox.converter.PropertyConverter
import wangdaye.com.geometricweather.common.basic.models.weather.WindDegree

class WindDegreeConverter : PropertyConverter<WindDegree?, Float?> {
    override fun convertToEntityProperty(databaseValue: Float?): WindDegree? =
        when (databaseValue) {
            null -> null
            in 0F..360F -> WindDegree(databaseValue, false)
            -1F -> WindDegree(null, true)
            else -> null
        }

    override fun convertToDatabaseValue(entityProperty: WindDegree?): Float? =
        when {
            entityProperty != null && entityProperty.isNoDirection -> -1F
            else -> entityProperty?.degree
        }
}
