package org.breezyweather.ui.daily.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.text.font.FontStyle
import breezyweather.domain.weather.model.Pollen
import org.breezyweather.R
import org.breezyweather.common.source.PollenIndexSource
import org.breezyweather.domain.weather.model.isIndexValid
import org.breezyweather.ui.common.composables.PollenGrid
import org.breezyweather.ui.common.widgets.Material3CardListItem

/**
 * TODO: Replace with a graphic like on homepage
 */
@Composable
fun DailyPollen(
    pollen: Pollen?,
    pollenIndexSource: PollenIndexSource?,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .fillMaxSize()
    ) {
        if (pollen?.isIndexValid == true) {
            Material3CardListItem {
                PollenGrid(
                    pollen = pollen,
                    pollenIndexSource = pollenIndexSource
                )
            }
        } else {
            Text(
                text = "No data available",
                fontStyle = FontStyle.Italic,
                modifier = Modifier.padding(dimensionResource(R.dimen.normal_margin))
            )
        }
    }
}
