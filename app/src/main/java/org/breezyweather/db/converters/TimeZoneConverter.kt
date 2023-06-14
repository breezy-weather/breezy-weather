package org.breezyweather.db.converters

import io.objectbox.converter.PropertyConverter
import java.util.TimeZone

class TimeZoneConverter : PropertyConverter<TimeZone?, String?> {
    override fun convertToEntityProperty(databaseValue: String?): TimeZone? =
        if (databaseValue.isNullOrEmpty()) null else TimeZone.getTimeZone(databaseValue)

    override fun convertToDatabaseValue(entityProperty: TimeZone?): String? =
        entityProperty?.id
}
