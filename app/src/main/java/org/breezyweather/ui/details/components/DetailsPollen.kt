/*
 * This file is part of Breezy Weather.
 *
 * Breezy Weather is free software: you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published by the
 * Free Software Foundation, version 3 of the License.
 *
 * Breezy Weather is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public
 * License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Breezy Weather. If not, see <https://www.gnu.org/licenses/>.
 */

package org.breezyweather.ui.details.components

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontStyle
import breezyweather.domain.weather.model.Pollen
import org.breezyweather.R
import org.breezyweather.common.source.PollenIndexSource
import org.breezyweather.domain.weather.model.getPollenWithMaxIndex
import org.breezyweather.domain.weather.model.isIndexValid
import org.breezyweather.ui.common.composables.PollenGrid
import org.breezyweather.ui.common.widgets.Material3ExpressiveCardListItem
import org.breezyweather.ui.settings.preference.bottomInsetItem

/**
 * TODO: Save pollen hourly data, and display a hourly index chart of the pollen with the maximum index on that day
 */
@Composable
fun DetailsPollen(
    pollen: Pollen?,
    pollenIndexSource: PollenIndexSource?,
    modifier: Modifier = Modifier,
) {
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(
            horizontal = dimensionResource(R.dimen.normal_margin),
            vertical = dimensionResource(R.dimen.small_margin)
        )
    ) {
        if (pollen?.isIndexValid == true) {
            pollen.getPollenWithMaxIndex()?.let {
                item {
                    DetailsSectionHeader(
                        stringResource(R.string.pollen_primary),
                        stringResource(it.pollenName)
                    )
                }
                item {
                    Spacer(modifier = Modifier.height(dimensionResource(R.dimen.normal_margin)))
                }
            }
            item {
                DetailsSectionHeader(stringResource(R.string.pollen_details))
            }
            item {
                Material3ExpressiveCardListItem(isFirst = true, isLast = true) {
                    PollenGrid(
                        pollen = pollen,
                        pollenIndexSource = pollenIndexSource
                    )
                }
            }
        } else {
            item {
                Text(
                    text = stringResource(R.string.chart_no_daily_data),
                    fontStyle = FontStyle.Italic,
                    modifier = Modifier.padding(dimensionResource(R.dimen.normal_margin))
                )
            }
        }
        bottomInsetItem()
    }
}
