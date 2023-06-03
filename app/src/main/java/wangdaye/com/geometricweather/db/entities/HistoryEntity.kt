package wangdaye.com.geometricweather.db.entities

import io.objectbox.annotation.Entity
import io.objectbox.annotation.Id
import java.util.Date

/**
 * History entity.
 *
 * [History].
 */
@Entity
data class HistoryEntity(
    @field:Id var id: Long = 0,
    var cityId: String,
    var weatherSource: String,
    var date: Date,
    var daytimeTemperature: Int? = null,
    var nighttimeTemperature: Int? = null
)
