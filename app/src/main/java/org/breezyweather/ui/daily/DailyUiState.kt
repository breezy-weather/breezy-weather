package org.breezyweather.ui.daily

import breezyweather.domain.location.model.Location
import org.breezyweather.common.basic.models.options.appearance.ChartDisplay

data class DailyUiState(
    val location: Location? = null,
    val selectedChart: ChartDisplay = ChartDisplay.TAG_OTHER_DETAILS,
    val initialIndex: Int = 0,
)
