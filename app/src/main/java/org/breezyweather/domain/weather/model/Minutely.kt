package org.breezyweather.domain.weather.model

import android.content.Context
import breezyweather.domain.location.model.Location
import breezyweather.domain.weather.model.Minutely
import breezyweather.domain.weather.model.Precipitation
import org.breezyweather.R
import org.breezyweather.common.extensions.getFormattedTime
import org.breezyweather.common.extensions.is12Hour
import java.util.Date
import kotlin.time.Duration.Companion.minutes

fun Minutely.getLevel(context: Context): String {
    return context.getString(
        if (precipitationIntensity == null) {
            R.string.precipitation_none
        } else {
            when (precipitationIntensity!!) {
                0.0 -> R.string.precipitation_none
                in 0.0..Precipitation.PRECIPITATION_HOURLY_LIGHT -> R.string.precipitation_intensity_light
                in Precipitation.PRECIPITATION_HOURLY_LIGHT..Precipitation.PRECIPITATION_HOURLY_MEDIUM,
                -> R.string.precipitation_intensity_medium
                in Precipitation.PRECIPITATION_HOURLY_MEDIUM..Double.MAX_VALUE,
                -> R.string.precipitation_intensity_heavy
                else -> R.string.precipitation_none
            }
        }
    )
}

fun List<Minutely>.getContentDescription(context: Context, location: Location): String {
    val contentDescription = StringBuilder()

    var startingIndex: Int? = null
    forEachIndexed { index, minutely ->
        if (minutely.precipitationIntensity != null && minutely.precipitationIntensity!! > 0) {
            if (startingIndex == null) {
                startingIndex = index
            }
        } else {
            if (startingIndex != null) {
                if (contentDescription.toString().isNotEmpty()) {
                    contentDescription.append(context.getString(R.string.comma_separator))
                }

                val slice = subList(startingIndex!!, index)
                contentDescription.append(
                    context.getString(
                        R.string.precipitation_between_time,
                        slice.first().date.getFormattedTime(location, context, context.is12Hour),
                        Date(slice.last().date.time + slice.last().minuteInterval.minutes.inWholeMilliseconds)
                            .getFormattedTime(location, context, context.is12Hour)
                    )
                )
                contentDescription.append(context.getString(R.string.colon_separator))
                contentDescription.append(slice.maxBy { it.precipitationIntensity!! }.getLevel(context))
                startingIndex = null
            }
        }
    }

    if (startingIndex != null) {
        val slice = subList(startingIndex!!, size)
        if (contentDescription.toString().isNotEmpty()) {
            contentDescription.append(context.getString(R.string.comma_separator))
        }
        contentDescription.append(
            context.getString(
                R.string.precipitation_between_time,
                slice.first().date.getFormattedTime(location, context, context.is12Hour),
                Date(slice.last().date.time + slice.last().minuteInterval.minutes.inWholeMilliseconds)
                    .getFormattedTime(location, context, context.is12Hour)
            )
        )
        contentDescription.append(context.getString(R.string.colon_separator))
        contentDescription.append(slice.maxBy { it.precipitationIntensity!! }.getLevel(context))
    }

    return contentDescription.toString()
}
