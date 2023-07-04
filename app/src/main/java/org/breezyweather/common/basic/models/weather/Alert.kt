package org.breezyweather.common.basic.models.weather

import android.graphics.Color
import androidx.annotation.ColorInt
import java.io.Serializable
import java.util.*

/**
 * Alert.
 *
 * All properties are [androidx.annotation.NonNull].
 */
class Alert(
    val alertId: Long,
    val startDate: Date? = null,
    val endDate: Date? = null,
    val description: String,
    val content: String? = null,
    val priority: Int,
    @ColorInt color: Int? = null
) : Serializable {

    @ColorInt
    val color: Int = color ?: Color.rgb(255, 184, 43)

}
