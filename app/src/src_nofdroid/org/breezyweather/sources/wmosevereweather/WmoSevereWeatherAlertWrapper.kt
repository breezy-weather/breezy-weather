package org.breezyweather.sources.wmosevereweather

import breezyweather.domain.weather.model.Alert

data class WmoSevereWeatherAlertWrapper(
    val alert: Alert,
    val url: String?
)