package org.breezyweather.domain.weather.model

import android.content.Context
import breezyweather.domain.location.model.Location
import breezyweather.domain.weather.model.Alert
import org.breezyweather.R
import org.breezyweather.common.extensions.getFormattedFullDayAndMonth
import org.breezyweather.common.extensions.getFormattedMediumDayAndMonth
import org.breezyweather.common.extensions.getFormattedTime
import org.breezyweather.common.extensions.is12Hour

fun Alert.getFormattedDates(
    location: Location,
    context: Context,
    full: Boolean = false,
): String {
    val builder = StringBuilder()
    startDate?.let { startDate ->
        val startDateDay = if (full) {
            startDate.getFormattedFullDayAndMonth(location, context)
        } else {
            startDate.getFormattedMediumDayAndMonth(location, context)
        }
        builder.append(startDateDay)
            .append(context.getString(R.string.comma_separator))
            .append(startDate.getFormattedTime(location, context, context.is12Hour))
        endDate?.let { endDate ->
            builder.append(" — ")
            val endDateDay = if (full) {
                startDate.getFormattedFullDayAndMonth(location, context)
            } else {
                endDate.getFormattedMediumDayAndMonth(location, context)
            }
            if (startDateDay != endDateDay) {
                builder.append(endDateDay).append(context.getString(R.string.comma_separator))
            }
            builder.append(endDate.getFormattedTime(location, context, context.is12Hour))
        }
    }
    return builder.toString()
}
