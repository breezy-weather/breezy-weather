package org.breezyweather.db.entities

import io.objectbox.annotation.Entity
import io.objectbox.annotation.Id
import java.util.*

/**
 * Alert entity.
 *
 * [Alert]
 */
@Entity
class AlertEntity(
    @field:Id var id: Long = 0,
    var cityId: String,
    var weatherSource: String,
    var alertId: Long,
    var startDate: Date? = null,
    var endDate: Date? = null,
    var description: String,
    var content: String? = null,
    var priority: Int,
    var color: Int
)
