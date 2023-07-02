package org.breezyweather.main.adapters.location

import android.content.Context
import org.breezyweather.R
import org.breezyweather.common.basic.models.Location
import org.breezyweather.common.basic.models.options.provider.WeatherSource
import org.breezyweather.common.basic.models.options.unit.TemperatureUnit
import org.breezyweather.common.basic.models.weather.WeatherCode
import org.breezyweather.common.utils.DisplayUtils

class LocationModel(
    context: Context,
    val location: Location,
    unit: TemperatureUnit,
    var selected: Boolean
) {
    var weatherCode: WeatherCode? = null
    var weatherText: String? = null
    val weatherSource: WeatherSource = location.weatherSource
    val currentPosition: Boolean = location.isCurrentPosition
    val residentPosition: Boolean = location.isResidentPosition
    val title: String = if (location.isCurrentPosition) context.getString(R.string.location_current) else location.place()
    val body: String = if (location.isUsable) location.administrationLevels() else context.getString(R.string.location_current_not_found_yet)
    var alerts: String? = null

    init {
        // TODO: Use current instead
        if (location.weather != null) {
            if (location.weather.dailyForecast.isNotEmpty()) {
                if (location.isDaylight && location.weather.dailyForecast[0].day?.weatherCode != null) {
                    weatherCode = location.weather.dailyForecast[0].day!!.weatherCode
                    weatherText = location.weather.dailyForecast[0].day!!.weatherText
                }
                if (!location.isDaylight && location.weather.dailyForecast[0].night?.weatherCode != null) {
                    weatherCode = location.weather.dailyForecast[0].night!!.weatherCode
                    weatherText = location.weather.dailyForecast[0].night!!.weatherText
                }
            }
            if (location.weather.currentAlertList.size > 0) {
                val builder = StringBuilder()
                location.weather.currentAlertList.forEach { alert ->
                    if (builder.toString().isNotEmpty()) {
                        builder.append("\n")
                    }
                    builder.append(alert.description)
                    if (alert.startDate != null) {
                        val startDateDay = DisplayUtils.getFormattedDate(
                            alert.startDate,
                            location.timeZone,
                            context.getString(R.string.date_format_short)
                        )
                        builder.append(", ")
                            .append(startDateDay)
                            .append(", ")
                            .append(
                                DisplayUtils.getFormattedDate(
                                    alert.startDate,
                                    location.timeZone,
                                    if (DisplayUtils.is12Hour(context)) "h:mm aa" else "HH:mm"
                                )
                            )
                        if (alert.endDate != null) {
                            builder.append("-")
                            val endDateDay = DisplayUtils.getFormattedDate(
                                alert.endDate,
                                location.timeZone,
                                context.getString(R.string.date_format_short)
                            )
                            if (startDateDay != endDateDay) {
                                builder.append(endDateDay)
                                    .append(", ")
                            }
                            builder.append(
                                DisplayUtils.getFormattedDate(
                                    alert.endDate,
                                    location.timeZone,
                                    if (DisplayUtils.is12Hour(context)) "h:mm aa" else "HH:mm"
                                )
                            )
                        }
                    }
                }
                alerts = builder.toString()
            }
        }
    }

    fun areItemsTheSame(newItem: LocationModel) = location.formattedId == newItem.location.formattedId

    fun areContentsTheSame(newItem: LocationModel) = location == newItem.location && selected == newItem.selected
}
