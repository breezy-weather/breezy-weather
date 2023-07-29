package org.breezyweather.db.entities

import io.objectbox.annotation.Entity
import io.objectbox.annotation.Id
import java.util.*

/**
 * Minutely entity.
 *
 * [Minutely].
 */
@Entity
data class MinutelyEntity(
    @field:Id var id: Long = 0,
    var formattedId: String,
    var date: Date,
    var minuteInterval: Int,
    var dbz: Int? = null
)
