package org.breezyweather.ui.daily

import breezyweather.domain.location.model.Location

data class DailyUiState(
    val location: Location? = null,
    val initialIndex: Int = 0,
)
