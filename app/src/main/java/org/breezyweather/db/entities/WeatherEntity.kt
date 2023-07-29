package org.breezyweather.db.entities

import io.objectbox.BoxStore
import io.objectbox.annotation.Convert
import io.objectbox.annotation.Entity
import io.objectbox.annotation.Id
import io.objectbox.annotation.Transient
import org.breezyweather.common.basic.models.weather.WeatherCode
import org.breezyweather.db.converters.WeatherCodeConverter
import java.util.*

/**
 * Weather entity.
 * [Weather].
 */
@Entity
class WeatherEntity(
    @field:Id var id: Long = 0,

    // base.
    var formattedId: String,
    var publishDate: Date,
    var updateDate: Date,

    // current.
    var weatherText: String? = null,
    @field:Convert(
        converter = WeatherCodeConverter::class,
        dbType = String::class
    ) var weatherCode: WeatherCode? = null,

    var temperature: Float? = null,
    var realFeelTemperature: Float? = null,
    var realFeelShaderTemperature: Float? = null,
    var apparentTemperature: Float? = null,
    var windChillTemperature: Float? = null,
    var wetBulbTemperature: Float? = null,

    var windDegree: Float? = null,
    var windSpeed: Float? = null,

    var uvIndex: Float? = null,

    var pm25: Float? = null,
    var pm10: Float? = null,
    var so2: Float? = null,
    var no2: Float? = null,
    var o3: Float? = null,
    var co: Float? = null,

    var relativeHumidity: Float? = null,
    var dewPoint: Float? = null,
    var pressure: Float? = null,
    var visibility: Float? = null,
    var cloudCover: Int? = null,
    var ceiling: Float? = null,
    var dailyForecast: String? = null,
    var hourlyForecast: String? = null
) {

    @Transient
    protected var dailyEntityList: List<DailyEntity>? = null

    @Transient
    protected var hourlyEntityList: List<HourlyEntity>? = null

    @Transient
    protected var minutelyEntityList: List<MinutelyEntity>? = null

    @Transient
    protected var alertEntityList: List<AlertEntity>? = null

    /**
     * To-many relationship, resolved on first access (and after reset).
     * Changes to to-many relations are not persisted, make changes to the target entity.
     */
    fun getDailyEntityList(boxStore: BoxStore): List<DailyEntity> {
        if (dailyEntityList == null) {
            val dailyEntityBox = boxStore.boxFor(DailyEntity::class.java)
            val query = dailyEntityBox.query(DailyEntity_.formattedId.equal(formattedId))
                .order(DailyEntity_.date)
                .build()
            val dailyEntityListNew = query.find()
            query.close()
            synchronized(this) {
                if (dailyEntityList == null) {
                    dailyEntityList = dailyEntityListNew
                }
            }
        }
        return dailyEntityList ?: emptyList()
    }

    /**
     * To-many relationship, resolved on first access (and after reset).
     * Changes to to-many relations are not persisted, make changes to the target entity.
     */
    fun getHourlyEntityList(boxStore: BoxStore): List<HourlyEntity> {
        if (hourlyEntityList == null) {
            val hourlyEntityBox = boxStore.boxFor(HourlyEntity::class.java)
            val query = hourlyEntityBox.query(HourlyEntity_.formattedId.equal(formattedId))
                .order(HourlyEntity_.date)
                .build()
            val hourlyEntityListNew = query.find()
            query.close()
            synchronized(this) {
                if (hourlyEntityList == null) {
                    hourlyEntityList = hourlyEntityListNew
                }
            }
        }
        return hourlyEntityList ?: emptyList()
    }

    /**
     * To-many relationship, resolved on first access (and after reset).
     * Changes to to-many relations are not persisted, make changes to the target entity.
     */
    fun getMinutelyEntityList(boxStore: BoxStore): List<MinutelyEntity> {
        if (minutelyEntityList == null) {
            val minutelyEntityBox = boxStore.boxFor(MinutelyEntity::class.java)
            val query = minutelyEntityBox.query(MinutelyEntity_.formattedId.equal(formattedId))
                .order(MinutelyEntity_.date)
                .build()
            val minutelyEntityListNew = query.find()
            query.close()
            synchronized(this) {
                if (minutelyEntityList == null) {
                    minutelyEntityList = minutelyEntityListNew
                }
            }
        }
        return minutelyEntityList ?: emptyList()
    }

    /**
     * To-many relationship, resolved on first access (and after reset).
     * Changes to to-many relations are not persisted, make changes to the target entity.
     */
    fun getAlertEntityList(boxStore: BoxStore): List<AlertEntity> {
        if (alertEntityList == null) {
            val alertEntityBox = boxStore.boxFor(AlertEntity::class.java)
            val query = alertEntityBox.query(AlertEntity_.formattedId.equal(formattedId))
                .order(AlertEntity_.priority)
                .order(AlertEntity_.startDate)
                .build()
            val alertEntityListNew = query.find()
            query.close()
            synchronized(this) {
                if (alertEntityList == null) {
                    alertEntityList = alertEntityListNew
                }
            }
        }
        return alertEntityList ?: emptyList()
    }
}
