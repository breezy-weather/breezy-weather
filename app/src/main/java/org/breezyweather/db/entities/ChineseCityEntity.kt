package org.breezyweather.db.entities

import io.objectbox.annotation.Entity
import io.objectbox.annotation.Id

/**
 * Chinese city entity.
 *
 * [ChineseCity].
 */
@Entity
data class ChineseCityEntity(
    @field:Id var id: Long = 0,
    var cityId: String,
    var province: String,
    var city: String,
    var district: String,
    var latitude: String,
    var longitude: String
)
