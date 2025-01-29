package org.breezyweather.ui.pollen

import breezyweather.domain.location.model.Location
import org.breezyweather.common.source.PollenIndexSource

data class PollenUiState(
    val location: Location? = null,
    val pollenIndexSource: PollenIndexSource? = null,
)
