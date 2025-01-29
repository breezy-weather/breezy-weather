package org.breezyweather.ui.alert

import breezyweather.domain.location.model.Location

data class AlertUiState(
    val location: Location? = null,
    val alertId: String? = null,
)
