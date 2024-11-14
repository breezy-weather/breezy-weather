/**
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

package org.breezyweather.common.ui.composables

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import breezyweather.domain.weather.model.Pollen
import org.breezyweather.R
import org.breezyweather.common.basic.models.options.unit.PollenUnit
import org.breezyweather.common.extensions.currentLocale
import org.breezyweather.common.source.PollenIndexSource
import org.breezyweather.domain.weather.index.PollenIndex
import org.breezyweather.domain.weather.model.getColor
import org.breezyweather.domain.weather.model.getColorFromSource
import org.breezyweather.domain.weather.model.getConcentration
import org.breezyweather.domain.weather.model.getIndexName
import org.breezyweather.domain.weather.model.getIndexNameFromSource
import org.breezyweather.domain.weather.model.validPollens
import org.breezyweather.theme.compose.DayNightTheme
import java.text.Collator

@Composable
fun PollenGrid(
    pollen: Pollen,
    pollenIndexSource: PollenIndexSource? = null,
    specificPollens: Set<PollenIndex> = setOf(),
) {
    val context = LocalContext.current
    val unit = PollenUnit.PPCM
    FlowRow(
        maxItemsInEachRow = 2
    ) {
        specificPollens.ifEmpty { pollen.validPollens }
            .sortedWith { va1, va2 ->
                Collator.getInstance(context.currentLocale)
                    .compare(
                        context.getString(va1.pollenName),
                        context.getString(va2.pollenName)
                    )
            }
            .forEach { validPollen ->
                PollenItem(
                    title = stringResource(validPollen.pollenName),
                    subtitle = if (pollenIndexSource != null) {
                        pollen.getIndexNameFromSource(context, validPollen, pollenIndexSource) ?: ""
                    } else {
                        unit.getValueText(
                            context,
                            pollen.getConcentration(validPollen) ?: 0
                        ) + " - " + pollen.getIndexName(context, validPollen)
                    },
                    tintColor = Color(
                        if (pollenIndexSource != null) {
                            pollen.getColorFromSource(context, validPollen, pollenIndexSource)
                        } else {
                            pollen.getColor(context, validPollen)
                        }
                    ),
                    modifier = Modifier.fillMaxWidth(0.5f)
                )
            }
    }
}

@Composable
private fun PollenItem(
    title: String,
    subtitle: String,
    tintColor: Color,
    modifier: Modifier = Modifier,
) = Row(
    modifier = modifier,
    verticalAlignment = Alignment.CenterVertically
) {
    Icon(
        modifier = Modifier
            .padding(dimensionResource(R.dimen.normal_margin))
            .size(dimensionResource(R.dimen.material_icon_size)),
        painter = painterResource(R.drawable.ic_circle_medium),
        contentDescription = null,
        tint = tintColor
    )
    Column(
        Modifier.padding(
            end = dimensionResource(R.dimen.normal_margin),
            top = dimensionResource(R.dimen.normal_margin),
            bottom = dimensionResource(R.dimen.normal_margin)
        )
    ) {
        Text(
            text = title,
            color = DayNightTheme.colors.titleColor,
            style = MaterialTheme.typography.titleMedium,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        Text(
            text = subtitle,
            color = DayNightTheme.colors.bodyColor,
            style = MaterialTheme.typography.bodyMedium,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}
